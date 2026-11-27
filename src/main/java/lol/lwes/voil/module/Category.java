package lol.lwes.voil.module;

public enum Category {
    Client("Client"),
    CrystalPvP("CrystalPvP"),
    Combat("Combat"),
    Movement("Movement"),
    Player("Player"),
    Render("Render"),
    Miscellaneous("Miscellaneous");

    public String name;

    Category(String s1) {
        this.name = s1;
    }

    private static Category[] $values() {
        return new Category[]{Client, CrystalPvP, Combat, Movement, Player, Render, Miscellaneous};
    }
}
