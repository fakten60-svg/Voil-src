package lol.lwes.voil.module.movement;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;

public class SnapTap extends Module {
    public static long leftPressTime = 0L;
    public static long rightPressTime = 0L;
    public static long forwardPressTime = 0L;
    public static long backPressTime = 0L;

    public SnapTap() {
        super("SnapTap", "Allows you to strafe without stopping", Category.Movement);
    }
}
