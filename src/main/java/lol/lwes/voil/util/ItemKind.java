package lol.lwes.voil.util;

public enum ItemKind {
    SWORD,
    AXE,
    PICKAXE,
    SHOVEL,
    HOE,
    BOW,
    FOOD,
    BLOCK,
    ARMOR,
    POTION,
    OTHER;

    private static ItemKind[] $values() {
        return new ItemKind[]{SWORD, AXE, PICKAXE, SHOVEL, HOE, BOW, FOOD, BLOCK, ARMOR, POTION, OTHER};
    }
}
