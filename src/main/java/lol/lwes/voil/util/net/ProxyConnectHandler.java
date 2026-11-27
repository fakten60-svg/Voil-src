package lol.lwes.voil.util.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import lol.lwes.voil.util.proxy.ProxyConfig;

public class ProxyConnectHandler extends ChannelOutboundHandlerAdapter {
    private static final String DECODER_NAME = "voil_proxy_decoder";
    private static final int MAX_RESPONSE_HEADER = 8192;
    private final ProxyConfig proxy;
    private InetSocketAddress targetAddress;
    private ChannelPromise connectPromise;
    private ChannelHandlerContext context;

    public ProxyConnectHandler(ProxyConfig proxy) {
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

            this.startProxyHandshake();
        });
        ctx.connect(this.proxy.address(), localAddress, proxyConnect);
    }

    private void startProxyHandshake() {
        this.context.pipeline().addBefore(this.context.name(), DECODER_NAME, new ResponseDecoder(this));
        String host = this.targetAddress.getHostString();
        int port = this.targetAddress.getPort();
        String authority = host.indexOf(':') >= 0 ? "[" + host + "]:" + port : host + ":" + port;

        StringBuilder request = new StringBuilder();
        request.append("CONNECT ").append(authority).append(" HTTP/1.1\r\n");
        request.append("Host: ").append(authority).append("\r\n");
        String username = this.proxy.username();
        if (username != null && !username.isEmpty()) {
            String password = this.proxy.password() != null ? this.proxy.password() : "";
            String credentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
            request.append("Proxy-Authorization: Basic ").append(credentials).append("\r\n");
        }
        request.append("\r\n");

        this.context.writeAndFlush(Unpooled.copiedBuffer(request, StandardCharsets.US_ASCII));
    }

    private void completeHandshake() {
        this.context.pipeline().remove(DECODER_NAME);
        this.context.pipeline().remove(this);
        this.connectPromise.trySuccess();
    }

    private void failHandshake(String reason) {
        this.connectPromise.tryFailure(new RuntimeException("proxy failed: " + reason));
        this.context.close();
    }

    private static final class ResponseDecoder extends ByteToMessageDecoder {
        private static final byte[] HEADER_END = {'\r', '\n', '\r', '\n'};
        private final ProxyConnectHandler handler;

        ResponseDecoder(ProxyConnectHandler handler) {
            this.handler = handler;
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            int headerEnd = ByteBufUtil.indexOf(Unpooled.wrappedBuffer(HEADER_END), in);
            if (headerEnd < 0) {
                if (in.readableBytes() > MAX_RESPONSE_HEADER) {
                    in.skipBytes(in.readableBytes());
                    this.handler.failHandshake("response header too large");
                }
                return;
            }

            int length = headerEnd - in.readerIndex() + HEADER_END.length;
            String header = in.readCharSequence(length, StandardCharsets.US_ASCII).toString();
            String statusLine = header.substring(0, Math.max(0, header.indexOf("\r\n")));
            String[] parts = statusLine.split(" ", 3);
            if (parts.length >= 2 && parts[0].startsWith("HTTP/") && parts[1].equals("200")) {
                this.handler.completeHandshake();
            } else {
                this.handler.failHandshake(statusLine.isEmpty() ? "malformed response" : statusLine);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            this.handler.failHandshake(String.valueOf(cause.getMessage()));
        }
    }
}
