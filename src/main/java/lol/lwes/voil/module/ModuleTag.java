package lol.lwes.voil.module;

public enum ModuleTag {
    NEW("New"),
    BLATANT("Blatant"),
    NOTOBSPROOF("Not OBS Proof"),
    DEV("Dev");

    String label;

    ModuleTag(String s1) {
        this.label = s1;
    }

    private static ModuleTag[] $values() {
        return new ModuleTag[]{NEW, BLATANT, NOTOBSPROOF, DEV};
    }

    public String getLabel() {
        return this.label;
    }
}
