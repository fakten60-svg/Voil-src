package dev.prestigeclient.renderlib.api.path;

import java.io.InputStream;
import java.util.Objects;

public class RLPath {
    private final String namespace;
    private final String path;

    private RLPath(String s, String s1) {
        this.namespace = s;
        this.path = s1;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            return !(object instanceof RLPath rlpath1) ? false : this.namespace.equals(rlpath1.namespace) && this.path.equals(rlpath1.path);
        }
    }

    @Override
    public String toString() {
        return this.namespace + ":" + this.path;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.namespace, this.path);
    }

    public static RLPath of(String s) {
        String s1 = s.startsWith("/") ? s.substring(1) : s;
        return new RLPath("blossom", s1);
    }

    public static RLPath of(String s, String s1) {
        String s2 = s1.startsWith("/") ? s1.substring(1) : s1;
        return new RLPath(s, s2);
    }

    public InputStream openStream() {
        String s = this.namespace + "/" + this.path;
        InputStream inputstream = RLPath.class.getClassLoader().getResourceAsStream(s);
        if (inputstream == null) {
            throw new IllegalArgumentException("resource not found: " + this);
        } else {
            return inputstream;
        }
    }

    public String getPath() {
        return this.path;
    }

    public String getNamespace() {
        return this.namespace;
    }
}
