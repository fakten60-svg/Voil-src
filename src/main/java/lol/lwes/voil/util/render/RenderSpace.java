package lol.lwes.voil.util.render;

public enum RenderSpace {
    HUD,
    WORLD;

    private static RenderSpace[] allValues() {
        return new RenderSpace[]{HUD, WORLD};
    }
}
