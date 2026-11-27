package lol.lwes.voil.module.miscellaneous;

import java.util.Objects;

public record ProxyEntry(String[] fields) {

    @Override
    public final boolean equals(Object object) {
        return this == object || object instanceof ProxyEntry proxyconfig1 && Objects.equals(this.fields, proxyconfig1.fields);
    }

    @Override
    public final String toString() {
        return "io[" + "a4=" + this.fields + "]";
    }

    @Override
    public final int hashCode() {
        return 0 + Objects.hashCode(this.fields);
    }
}
