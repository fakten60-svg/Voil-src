package lol.lwes.voil.util.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Socks4ProxyHandler extends ChannelOutboundHandlerAdapter {
    private static final String DECODER_NAME = "voil_proxy_decoder";
    private static final int VERSION = 4;
    private static final int COMMAND_CONNECT = 1;
    private static final int REPLY_GRANTED = 0x5A;
    private static final int REPLY_LENGTH = 8;
    private final ProxyConfig proxy;
    private InetSocketAddress targetAddress;
    private ChannelPromise connectPromise;
    private ChannelHandlerContext context;

    public Socks4ProxyHandler(ProxyConfig proxy) {
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

            this.sendConnectRequest();
        });
        ctx.connect(this.proxy.address(), localAddress, proxyConnect);
    }

    private void sendConnectRequest() {
        this.context.pipeline().addBefore(this.context.name(), DECODER_NAME, new ResponseDecoder(this));
        Inet4Address destination;
        try {
            destination = this.resolveIpv4();
        } catch (Exception exception) {
            this.fail("dns: " + exception.getMessage());
            return;
        }

        if (destination == null) {
            this.fail("socks4 requires an ipv4 destination");
            return;
        }

        String username = this.proxy.username();
        byte[] userId = username != null ? username.getBytes(StandardCharsets.US_ASCII) : new byte[0];
        ByteBuf request = this.context.alloc().buffer(9 + userId.length);
        request.writeByte(VERSION);
        request.writeByte(COMMAND_CONNECT);
        request.writeShort(this.targetAddress.getPort());
        request.writeBytes(destination.getAddress());
        request.writeBytes(userId);
        request.writeByte(0);
        this.context.writeAndFlush(request);
    }

    private Inet4Address resolveIpv4() throws Exception {
        InetAddress resolved = this.targetAddress.getAddress();
        if (resolved instanceof Inet4Address ipv4) {
            return ipv4;
        }

        for (InetAddress address : InetAddress.getAllByName(this.targetAddress.getHostString())) {
            if (address instanceof Inet4Address ipv4) {
                return ipv4;
            }
        }
        return null;
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

    private static final class ResponseDecoder extends ByteToMessageDecoder {
        private final Socks4ProxyHandler handler;

        ResponseDecoder(Socks4ProxyHandler handler) {
            this.handler = handler;
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            if (in.readableBytes() < REPLY_LENGTH) {
                return;
            }

            in.readByte();
            int status = in.readUnsignedByte();
            in.skipBytes(REPLY_LENGTH - 2);
            if (status == REPLY_GRANTED) {
                this.handler.completeHandshake();
            } else {
                this.handler.fail("socks4 request rejected (0x" + Integer.toHexString(status) + ")");
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            this.handler.fail(String.valueOf(cause.getMessage()));
        }
    }
}
