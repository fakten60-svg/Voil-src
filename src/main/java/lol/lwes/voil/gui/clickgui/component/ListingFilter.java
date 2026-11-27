package lol.lwes.voil.gui.clickgui.component;

public enum ListingFilter {
    RECOMMENDED,
    ALL;

    private static ListingFilter[] $values() {
        return new ListingFilter[]{RECOMMENDED, ALL};
    }
}
