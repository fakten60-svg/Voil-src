package lol.lwes.voil.setting;

public enum RegistrySelectionMode {
    CHOOSE_ONE("Single"),
    CHOOSE_MULTI("Multiple");

    private final String displayName;

    RegistrySelectionMode(String displayName) {
        this.displayName = displayName;
    }

    public boolean allowsMultiple() {
        return this == CHOOSE_MULTI;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
