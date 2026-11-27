package lol.lwes.voil.util;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.CameraPosEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.RenderTickEvent;
import lol.lwes.voil.event.TaskExecuteEvent;
import lol.lwes.voil.event.TickEvent;
import lol.lwes.voil.mixin.accessor.ThreadExecutorAccessor;
import lol.lwes.voil.module.Module;
import net.minecraft.util.math.Vec3d;

public class TickFastForward {
    private static int ticksLeft = 0;
    private static boolean active = false;
    private static TickRequest request;
    public static Float tickDelta = null;
    private static long lastSyncTime = 0L;
    private static final long syncInterval = 10000L;

    private static boolean runPendingTicks() {
        if (ticksLeft > 0 && !Module.isNotInGame() && request != null) {
            ticksLeft = request.releaseTicks;
            active = true;
            if (request.holdPackets) {
                VoilClient.mc.getPacketApplyBatcher().apply();
                ((ThreadExecutorAccessor)VoilClient.mc).invokeDaRunTasks();
            }

            while (ticksLeft > 0 && VoilClient.mc.player != null) {
                VoilClient.mc.tick();
            }

            active = false;
            request = null;
            tickDelta = null;
            return true;
        } else {
            return false;
        }
    }

    public static boolean requestTicks(TickRequest tickrequest) {
        if (tickrequest == null) {
            return runPendingTicks();
        } else if (request == null) {
            request = tickrequest;
            tickDelta = SystemUtil.getTickDelta();
            return true;
        } else {
            return false;
        }
    }

    @EventHandler
    public static void onTick(TickEvent tickevent) {
        long i = System.currentTimeMillis();
        if (i - lastSyncTime > syncInterval) {
            lastSyncTime = i;
        }

        if (!Module.isNotInGame()) {
            if (active) {
                int k = ticksLeft--;
            } else {
                if (request != null) {
                    if (ticksLeft < request.chargeTicks) {
                        int j = ticksLeft++;
                        tickevent.cancel();
                    } else {
                        runPendingTicks();
                    }
                }
            }
        }
    }

    @EventHandler
    public static void camPosEvent(CameraPosEvent cameraposevent) {
        if (!Module.isNotInGame() && !active) {
            if (request != null && ticksLeft < request.chargeTicks && !cameraposevent.isCancelled()) {
                Vec3d vec3d = VoilClient.mc.player.getEyePos();
                cameraposevent.setX(vec3d.x);
                cameraposevent.setY(vec3d.y);
                cameraposevent.setZ(vec3d.z);
                cameraposevent.cancel();
            }
        }
    }

    @EventHandler
    public static void onRenderTickThing1(RenderTickEvent rendertickevent) {
        if (!Module.isNotInGame() && !active) {
            if (request != null && ticksLeft < request.chargeTicks && request.holdPackets) {
                rendertickevent.cancel();
            }
        }
    }

    @EventHandler
    public static void onRenderTickThing2(TaskExecuteEvent taskexecuteevent) {
        if (!Module.isNotInGame() && !active) {
            if (request != null && ticksLeft < request.chargeTicks && request.holdPackets) {
                taskexecuteevent.cancel();
            }
        }
    }
}
