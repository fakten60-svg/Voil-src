package lol.lwes.voil.hook;

import lol.lwes.voil.module.miscellaneous.NameProtect;

public class NameProtectHook {
    public static String protectName(String s) {
        return s != null && !s.isEmpty() && NameProtect.active ? NameProtect.protectText(s) : s;
    }
}
