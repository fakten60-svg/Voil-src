package lol.lwes.voil.util.proxy;

import io.netty.channel.ChannelOutboundHandlerAdapter;
import java.util.concurrent.atomic.AtomicReference;
import lol.lwes.voil.util.net.ProxyConnectHandler;

public class ProxyManager {
    private static final AtomicReference current = new AtomicReference();

    private ProxyManager() {
    }

    public static ChannelOutboundHandlerAdapter createCurrentHandler() {
        Object object = current.get();
        return createHandler((ProxyConfig)object);
    }

    public static void clear() {
        current.set(null);
    }

    public static ProxyConfig getProxy() {
        return (ProxyConfig)current.get();
    }

    public static ChannelOutboundHandlerAdapter createHandler(ProxyConfig proxyconfig) {
        if (proxyconfig == null) {
            return null;
        }

        return switch (proxyconfig.type()) {
            case SOCKS5 -> new Socks5ProxyHandler(proxyconfig);
            case SOCKS4 -> new Socks4ProxyHandler(proxyconfig);
            case HTTP -> new ProxyConnectHandler(proxyconfig);
        };
    }

    public static void setProxy(ProxyConfig proxyconfig) {
        current.set(proxyconfig);
    }
}
