package lol.lwes.voil.util.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Socks5ProxyHandler extends ChannelOutboundHandlerAdapter {
    private static final String DECODER_NAME = "voil_proxy_decoder";
    private static final int VERSION = 5;
    private static final int AUTH_VERSION = 1;
    private static final int METHOD_NONE = 0;
    private static final int METHOD_PASSWORD = 2;
    private static final int METHOD_UNACCEPTABLE = 0xFF;
    private static final int COMMAND_CONNECT = 1;
    private static final int ADDRESS_IPV4 = 1;
    private static final int ADDRESS_DOMAIN = 3;
    private static final int ADDRESS_IPV6 = 4;
    private final ProxyConfig proxy;
    private InetSocketAddress targetAddress;
    private ChannelPromise connectPromise;
    private ChannelHandlerContext context;

    public Socks5ProxyHandler(ProxyConfig proxy) {
        this.proxy = proxy;
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        this.targetAddress = (InetSocketAddress) remoteAddress;
        this.connectPromise = promise;
        this.context = ctx;
        ChannelPromise proxyConnect = ctx.newPromise();
        proxyConnect.addListener(future -> {
            if (!future.isSuccess()) {
                promise.tryFailure(future.cause());
                return;
            }

            this.startHandshake();
        });
        ctx.connect(this.proxy.address(), localAddress, proxyConnect);
    }

    private boolean hasCredentials() {
        String username = this.proxy.username();
        return username != null && !username.isEmpty();
    }

    private void startHandshake() {
        this.context.pipeline().addBefore(this.context.name(), DECODER_NAME, new ResponseDecoder(this));
        ByteBuf greeting = this.context.alloc().buffer(4);
        greeting.writeByte(VERSION);
        if (this.hasCredentials()) {
            greeting.writeByte(2);
            greeting.writeByte(METHOD_NONE);
            greeting.writeByte(METHOD_PASSWORD);
        } else {
            greeting.writeByte(1);
            greeting.writeByte(METHOD_NONE);
        }
        this.context.writeAndFlush(greeting);
    }

    private void sendAuthentication() {
        byte[] username = this.proxy.username().getBytes(StandardCharsets.UTF_8);
        byte[] password = this.proxy.password() != null ? this.proxy.password().getBytes(StandardCharsets.UTF_8) : new byte[0];
        if (username.length > 255 || password.length > 255) {
            this.fail("credentials too long");
            return;
        }

        ByteBuf request = this.context.alloc().buffer(3 + username.length + password.length);
        request.writeByte(AUTH_VERSION);
        request.writeByte(username.length);
        request.writeBytes(username);
        request.writeByte(password.length);
        request.writeBytes(password);
        this.context.writeAndFlush(request);
    }

    private void sendConnectRequest() {
        String host = this.targetAddress.getHostString();
        ByteBuf request = this.context.alloc().buffer(22 + host.length());
        request.writeByte(VERSION);
        request.writeByte(COMMAND_CONNECT);
        request.writeByte(0);

        if (host.indexOf(':') >= 0) {
            InetAddress address;
            try {
                address = this.targetAddress.getAddress() != null ? this.targetAddress.getAddress() : InetAddress.getByName(host);
            } catch (Exception exception) {
                request.release();
                this.fail("bad ipv6 literal: " + host);
                return;
            }

            if (!(address instanceof Inet6Address)) {
                request.release();
                this.fail("expected ipv6 address: " + host);
                return;
            }

            request.writeByte(ADDRESS_IPV6);
            request.writeBytes(address.getAddress());
        } else {
            byte[] domain = host.getBytes(StandardCharsets.US_ASCII);
            if (domain.length > 255) {
                request.release();
                this.fail("host name too long: " + host);
                return;
            }

            request.writeByte(ADDRESS_DOMAIN);
            request.writeByte(domain.length);
            request.writeBytes(domain);
        }

        request.writeShort(this.targetAddress.getPort());
        this.context.writeAndFlush(request);
    }

    private void completeHandshake() {
        this.context.pipeline().remove(DECODER_NAME);
        this.context.pipeline().remove(this);
        this.connectPromise.trySuccess();
    }

    private void fail(String reason) {
        this.connectPromise.tryFailure(new RuntimeException("proxy failed: " + reason));
        this.context.close();
    }

    private enum Stage {
        METHOD,
        AUTHENTICATION,
        CONNECT
    }

    private static final class ResponseDecoder extends ByteToMessageDecoder {
        private final Socks5ProxyHandler handler;
        private Stage stage = Stage.METHOD;

        ResponseDecoder(Socks5ProxyHandler handler) {
            this.handler = handler;
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            switch (this.stage) {
                case METHOD -> this.readMethod(in);
                case AUTHENTICATION -> this.readAuthentication(in);
                case CONNECT -> this.readConnect(in);
            }
        }

        private void readMethod(ByteBuf in) {
            if (in.readableBytes() < 2) {
                return;
            }

            int version = in.readUnsignedByte();
            int method = in.readUnsignedByte();
            if (version != VERSION) {
                this.handler.fail("unexpected socks version " + version);
            } else if (method == METHOD_NONE) {
                this.stage = Stage.CONNECT;
                this.handler.sendConnectRequest();
            } else if (method == METHOD_PASSWORD && this.handler.hasCredentials()) {
                this.stage = Stage.AUTHENTICATION;
                this.handler.sendAuthentication();
            } else if (method == METHOD_UNACCEPTABLE) {
                this.handler.fail("no acceptable authentication method");
            } else {
                this.handler.fail("unsupported authentication method " + method);
            }
        }

        private void readAuthentication(ByteBuf in) {
            if (in.readableBytes() < 2) {
                return;
            }

            in.readUnsignedByte();
            int status = in.readUnsignedByte();
            if (status != 0) {
                this.handler.fail("authentication rejected");
                return;
            }

            this.stage = Stage.CONNECT;
            this.handler.sendConnectRequest();
        }

        private void readConnect(ByteBuf in) {
            if (in.readableBytes() < 5) {
                return;
            }

            int start = in.readerIndex();
            int addressType = in.getUnsignedByte(start + 3);
            int addressLength = switch (addressType) {
                case ADDRESS_IPV4 -> 4;
                case ADDRESS_IPV6 -> 16;
                case ADDRESS_DOMAIN -> 1 + in.getUnsignedByte(start + 4);
                default -> -1;
            };

            if (addressLength < 0) {
                in.skipBytes(in.readableBytes());
                this.handler.fail("unknown address type " + addressType);
                return;
            }

            int length = 4 + addressLength + 2;
            if (in.readableBytes() < length) {
                return;
            }

            int version = in.readUnsignedByte();
            int reply = in.readUnsignedByte();
            in.skipBytes(length - 2);
            if (version != VERSION) {
                this.handler.fail("unexpected socks version " + version);
            } else if (reply != 0) {
                this.handler.fail("connect rejected (" + reply + ")");
            } else {
                this.handler.completeHandshake();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            this.handler.fail(String.valueOf(cause.getMessage()));
        }
    }
}
