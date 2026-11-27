package lol.lwes.voil.event;

public class LookupProviderEntry {
    public final String name;
    public final LookupProvider provider;

    public LookupProviderEntry(String s, LookupProvider lookupprovider) {
        this.name = s;
        this.provider = lookupprovider;
    }
}
