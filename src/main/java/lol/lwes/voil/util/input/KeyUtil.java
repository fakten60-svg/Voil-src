package lol.lwes.voil.util.input;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.mixin.accessor.KeyBindingAccessor;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class KeyUtil {
    public static int maskKeyCode(int i) {
        return i & 0;
    }

    public static boolean isMouseButton(int i) {
        return (i & 1073741824) != 0;
    }

    public static boolean isKeyDown(int i) {
        if (i < 0) {
            return false;
        } else {
            return i <= 8
                ? GLFW.glfwGetMouseButton(VoilClient.mc.getWindow().getHandle(), i) == 1
                : GLFW.glfwGetKey(VoilClient.mc.getWindow().getHandle(), i) == 1;
        }
    }

    public static void setTimesPressed(KeyBinding keybinding, int i) {
        ((KeyBindingAccessor)keybinding).setTimesPressed(i);
    }

    public static int getMouseButton(int i) {
        return i & 0xFF;
    }

    public static boolean isMouseButtonCode(int i) {
        return (i & 1073741824) != 0;
    }

    public static String getKeyName(int i) {
        if (i == 0) {
            return "None";
        }

        if (isMouseButton(i)) {
            int j = getMouseButton(i);
            String s3;
            if (j != 0) {
                if (j != 1) {
                    if (j != 2) {
                        int l = j + 1;
                        s3 = "MOUSE " + l;
                    } else {
                        s3 = "MMB";
                    }
                } else {
                    s3 = "RMB";
                }
            } else {
                s3 = "LMB";
            }

            return s3;
        } else {
            if (i >= 48 && i <= 57) {
                return String.valueOf((char)i);
            }

            if (i >= 65 && i <= 90) {
                return String.valueOf((char)i);
            }

            if (i >= 290 && i <= 314) {
                int k = i - 289;
                return "F" + k;
            }

            if (i >= 320 && i <= 329) {
                int i1 = i - 320;
                return "N " + i1;
            }

            switch (i) {
                case 330:
                    return "N .";
                case 331:
                    return "N /";
                case 332:
                    return "N *";
                case 333:
                    return "N -";
                case 334:
                    return "N +";
                case 335:
                    return "N ENTR";
                case 336:
                    return "N =";
                default:
                    switch (i) {
                        case 340:
                            return "L SHIFT";
                        case 341:
                            return "L CTRL";
                        case 342:
                            return "L ALT";
                        case 343:
                            return "WIN L";
                        case 344:
                            return "R SHIFT";
                        case 345:
                            return "R CTRL";
                        case 346:
                            return "R ALT";
                        case 347:
                            return "WIN R";
                        case 348:
                            return "MENU";
                        default:
                            switch (i) {
                                case 32:
                                    return "SPACE";
                                case 39:
                                    return "'";
                                case 44:
                                    return ",";
                                case 45:
                                    return "-";
                                case 46:
                                    return ".";
                                case 47:
                                    return "/";
                                case 59:
                                    return ";";
                                case 61:
                                    return "=";
                                case 91:
                                    return "[";
                                case 92:
                                    return "\\";
                                case 93:
                                    return "]";
                                case 96:
                                    return "`";
                                case 161:
                                    return "WORLD 1";
                                case 162:
                                    return "WORLD 2";
                                case 256:
                                    return "ESCAPE";
                                case 257:
                                    return "ENTER";
                                case 258:
                                    return "TAB";
                                case 260:
                                    return "INS";
                                case 261:
                                    return "DEL";
                                case 262:
                                    return "RIGHT";
                                case 263:
                                    return "LEFT";
                                case 264:
                                    return "DOWN";
                                case 265:
                                    return "UP";
                                case 266:
                                    return "P UP";
                                case 267:
                                    return "P DOWN";
                                case 268:
                                    return "HOME";
                                case 269:
                                    return "END";
                                case 280:
                                    return "C LOCK";
                                case 281:
                                    return "S LOCK";
                                case 282:
                                    return "N LOCK";
                                case 283:
                                    return "PRT SCN";
                                case 284:
                                    return "PAUSE";
                                default:
                                    return "None";
                            }
                    }
            }
        }
    }

    public static int getMouseButtonIndex(int i) {
        return i & 0xFF;
    }
}
