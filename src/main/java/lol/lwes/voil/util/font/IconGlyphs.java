package lol.lwes.voil.util.font;

import lol.lwes.voil.module.Category;

public class IconGlyphs {
    public static String iconE1D7() {
        return new String(Character.toChars(57815));
    }

    public static String iconE124() {
        return new String(Character.toChars(57636));
    }

    public static String iconLetterA() {
        return "A";
    }

    public static String iconLetterD() {
        return "D";
    }

    public static String getCategoryIcon(Category category) {
        return switch (category) {
            case Combat -> iconE2B4();
            case Movement -> iconE077();
            case Render -> iconE417();
            case Player -> iconE21E();
            case Miscellaneous -> iconE21A();
            case Client -> iconE19F();
            case CrystalPvP -> "";
        };
    }

    public static int opaqueTen() {
        return 10;
    }

    public static int opaqueZero() {
        return 0;
    }

    public static String iconE155() {
        return new String(Character.toChars(57685));
    }

    public static String iconE1E4() {
        return new String(Character.toChars(57828));
    }

    public static String iconE04C() {
        return new String(Character.toChars(57420));
    }

    public static String iconE18E() {
        return new String(Character.toChars(57742));
    }

    public static String iconE140() {
        return new String(Character.toChars(57664));
    }

    public static String iconE077() {
        return new String(Character.toChars(57463));
    }

    public static String iconE1E1() {
        return new String(Character.toChars(57825));
    }

    public static String iconE158() {
        return new String(Character.toChars(57688));
    }

    public static String iconE2B4() {
        return new String(Character.toChars(58036));
    }

    public static String iconE0D0() {
        return new String(Character.toChars(57552));
    }

    public static String iconE417() {
        return new String(Character.toChars(58391));
    }

    public static String iconE284() {
        return new String(Character.toChars(57988));
    }

    public static String iconE05D() {
        return new String(Character.toChars(57437));
    }

    public static String iconE21E() {
        return new String(Character.toChars(57886));
    }

    public static String iconE163() {
        return new String(Character.toChars(57699));
    }

    public static String iconE19F() {
        return new String(Character.toChars(57759));
    }

    public static String iconE1E0() {
        return new String(Character.toChars(57824));
    }

    public static String iconE21A() {
        return new String(Character.toChars(57882));
    }

    public static String iconE555() {
        return new String(Character.toChars(58709));
    }

    public static String iconE0BE() {
        return new String(Character.toChars(57534));
    }

    public static String iconE28D() {
        return new String(Character.toChars(57997));
    }

    public static String iconE164() {
        return new String(Character.toChars(57700));
    }

    public static String iconE132() {
        return new String(Character.toChars(57650));
    }

    public static String iconE0BF() {
        return new String(Character.toChars(57535));
    }

    public static String iconLetterB() {
        return "B";
    }

    public static String iconE07B() {
        return new String(Character.toChars(57467));
    }

    public static String iconLetterE() {
        return "E";
    }

    public static String iconLetterC() {
        return "C";
    }
}
