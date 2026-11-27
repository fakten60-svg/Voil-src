package lol.lwes.voil.hook;

import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.movement.SnapTap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil.Key;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class SnapTapKeyHook {
    public static void onIsKeyPressed(CallbackInfoReturnable callbackinforeturnable, Key key, boolean flag) {
        if (MinecraftClient.getInstance().player != null) {
            if (ModuleManager.INSTANCE.isEnabled(SnapTap.class)) {
                if (key.getCode() == 65) {
                    if (flag) {
                        if (SnapTap.rightPressTime == 0L) {
                            callbackinforeturnable.setReturnValue(true);
                            callbackinforeturnable.cancel();
                            return;
                        }

                        callbackinforeturnable.setReturnValue(SnapTap.rightPressTime <= SnapTap.leftPressTime);
                        callbackinforeturnable.cancel();
                    }
                } else if (key.getCode() == 68) {
                    if (flag) {
                        if (SnapTap.leftPressTime == 0L) {
                            callbackinforeturnable.setReturnValue(true);
                            callbackinforeturnable.cancel();
                            return;
                        }

                        callbackinforeturnable.setReturnValue(SnapTap.leftPressTime <= SnapTap.rightPressTime);
                        callbackinforeturnable.cancel();
                    }
                } else if (key.getCode() == 87) {
                    if (flag) {
                        if (SnapTap.backPressTime == 0L) {
                            callbackinforeturnable.setReturnValue(true);
                            callbackinforeturnable.cancel();
                            return;
                        }

                        callbackinforeturnable.setReturnValue(SnapTap.backPressTime <= SnapTap.forwardPressTime);
                        callbackinforeturnable.cancel();
                    }
                } else if (key.getCode() == 83 && flag) {
                    if (SnapTap.forwardPressTime == 0L) {
                        callbackinforeturnable.setReturnValue(true);
                        callbackinforeturnable.cancel();
                        return;
                    }

                    callbackinforeturnable.setReturnValue(SnapTap.forwardPressTime <= SnapTap.backPressTime);
                    callbackinforeturnable.cancel();
                }
            }
        }
    }

    public static void onKeyStateChange(boolean flag, Key key) {
        if (MinecraftClient.getInstance().player != null) {
            if (ModuleManager.INSTANCE.isEnabled(SnapTap.class)) {
                if (key.getCode() == 65) {
                    if (flag) {
                        SnapTap.leftPressTime = System.currentTimeMillis();
                    } else {
                        SnapTap.leftPressTime = 0L;
                    }
                } else if (key.getCode() == 68) {
                    if (flag) {
                        SnapTap.rightPressTime = System.currentTimeMillis();
                    } else {
                        SnapTap.rightPressTime = 0L;
                    }
                } else if (key.getCode() == 87) {
                    if (flag) {
                        SnapTap.forwardPressTime = System.currentTimeMillis();
                    } else {
                        SnapTap.forwardPressTime = 0L;
                    }
                } else if (key.getCode() == 83) {
                    if (flag) {
                        SnapTap.backPressTime = System.currentTimeMillis();
                    } else {
                        SnapTap.backPressTime = 0L;
                    }
                }
            }
        }
    }
}
