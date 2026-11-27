package lol.lwes.voil.util;

public enum ItemCategory {
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

    private static ItemCategory[] $values() {
        return new ItemCategory[]{SWORD, AXE, PICKAXE, SHOVEL, HOE, BOW, FOOD, BLOCK, ARMOR, POTION, OTHER};
    }
}
