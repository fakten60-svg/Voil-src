package lol.lwes.voil.util.proxy;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public record ProxyConfig(ProxyType type, InetSocketAddress address, String username, String password) {
    private static final int MAX_PORT = 65535;

    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof ProxyConfig proxyconfig1
                && Objects.equals(this.password, proxyconfig1.password)
                && Objects.equals(this.username, proxyconfig1.username)
                && Objects.equals(this.address, proxyconfig1.address)
                && Objects.equals(this.type, proxyconfig1.type);
    }

    @Override
    public final String toString() {
        return "bs[" + "bq=" + this.type + ", br=" + this.address + ", bs=" + this.username + ", bt=" + this.password + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.type);
        i = i * 31 + Objects.hashCode(this.address);
        i = i * 31 + Objects.hashCode(this.username);
        return i * 31 + Objects.hashCode(this.password);
    }

    public static ProxyConfig create(ProxyType proxytype, String s, int i, String s1, String s2) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("host is empty");
        }

        if (i > 0 && i <= MAX_PORT) {
            InetAddress inetaddress;
            try {
                inetaddress = InetAddress.getByName(s.trim());
            } catch (UnknownHostException unknownhostexception) {
                throw new IllegalArgumentException("host could not be resolved: " + s, unknownhostexception);
            }

            InetSocketAddress inetsocketaddress = new InetSocketAddress(inetaddress, i);
            String s3 = s1 != null && !s1.isEmpty() ? s1 : null;
            String s4 = s2 != null && !s2.isEmpty() ? s2 : null;
            return new ProxyConfig(proxytype, inetsocketaddress, s3, s4);
        } else {
            throw new IllegalArgumentException("port out of range: " + i);
        }
    }

    public boolean hasCredentials() {
        return this.username != null && !this.username.isEmpty();
    }
}
