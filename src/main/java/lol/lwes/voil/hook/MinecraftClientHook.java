package lol.lwes.voil.hook;

import java.util.Arrays;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.AttackBlockEvent;
import lol.lwes.voil.event.AttackKeyEvent;
import lol.lwes.voil.event.BlockBreakStartEvent;
import lol.lwes.voil.event.BlockBreakingEvent;
import lol.lwes.voil.event.Cancellable;
import lol.lwes.voil.event.DoAttackEvent;
import lol.lwes.voil.event.DropKeyEvent;
import lol.lwes.voil.event.EventBus;
import lol.lwes.voil.event.HotbarKeyEvent;
import lol.lwes.voil.event.InputsHeadEvent;
import lol.lwes.voil.event.ItemUseEvent;
import lol.lwes.voil.event.PickItemKeyEvent;
import lol.lwes.voil.event.PostItemUseEvent;
import lol.lwes.voil.event.PreRenderEvent;
import lol.lwes.voil.event.ShutdownEvent;
import lol.lwes.voil.event.TickEvent;
import lol.lwes.voil.event.UseKeyEvent;
import lol.lwes.voil.script.ScriptManager;
import lol.lwes.voil.util.input.KeyUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class MinecraftClientHook {
    @Unique
    private static final boolean[] keyEventHandled;
    @Unique
    private static final boolean[] hotbarEventHandled;
    @Unique
    private static HitResult savedCrosshairTarget;
    @Unique
    private static Entity savedTargetedEntity;
    private static final String TICK_SECTION_NAME = "onTick";

    static {
        boolean[] aboolean = new boolean[6];
        boolean[] aboolean1 = new boolean[9];
        keyEventHandled = aboolean;
        hotbarEventHandled = aboolean1;
    }

    public static void onShutdown() {
        EventBus eventbus = VoilClient.eventBus;
        ShutdownEvent shutdownevent = ShutdownEvent.getInstance();
        eventbus.post(shutdownevent);
    }

    public static void onAttackKey(CallbackInfo callbackinfo) {
        if (!checkAndMarkKeyHandled(2)) {
            AttackKeyEvent attackkeyevent = new AttackKeyEvent();
            VoilClient.eventBus.post(attackkeyevent);
            if (attackkeyevent.isCancelled()) {
                callbackinfo.cancel();
            }

            KeyUtil.setTimesPressed(VoilClient.mc.options.attackKey, attackkeyevent.getTimesPressed());
        }
    }

    public static void b(CallbackInfo callbackinfo) {
        EventBus eventbus = VoilClient.eventBus;
        BlockBreakStartEvent blockbreakstartevent = BlockBreakStartEvent.getInstance();
        if (((BlockBreakStartEvent)eventbus.post(blockbreakstartevent)).isCancelled()) {
            callbackinfo.cancel();
        }
    }

    public static void onPreRender() {
        VoilClient.eventBus.postObject(PreRenderEvent.getInstance());
    }

    public static void onAttackBlock(CallbackInfoReturnable callbackinforeturnable) {
        EventBus eventbus = VoilClient.eventBus;
        AttackBlockEvent attackblockevent = AttackBlockEvent.getInstance();
        if (((AttackBlockEvent)eventbus.post(attackblockevent)).isCancelled()) {
            callbackinforeturnable.setReturnValue(false);
        }
    }

    public static void onItemUse(CallbackInfo callbackinfo) {
        EventBus eventbus = VoilClient.eventBus;
        ItemUseEvent itemuseevent = ItemUseEvent.getInstance();
        if (((ItemUseEvent)eventbus.post(itemuseevent)).isCancelled()) {
            callbackinfo.cancel();
        }
    }

    public static void onDropKey(CallbackInfo callbackinfo) {
        if (!checkAndMarkKeyHandled(1)) {
            DropKeyEvent dropkeyevent = new DropKeyEvent();
            VoilClient.eventBus.post(dropkeyevent);
            if (dropkeyevent.isCancelled()) {
                callbackinfo.cancel();
            }
        }
    }

    public static void saveCrosshairTarget() {
        Arrays.fill(keyEventHandled, false);
        Arrays.fill(hotbarEventHandled, false);
        savedCrosshairTarget = VoilClient.mc.crosshairTarget;
        savedTargetedEntity = VoilClient.mc.targetedEntity;
        VoilClient.eventBus.postObject(InputsHeadEvent.getInstance());
    }

    public static void onPostItemUse(CallbackInfo callbackinfo) {
        EventBus eventbus = VoilClient.eventBus;
        PostItemUseEvent postitemuseevent = PostItemUseEvent.getInstance();
        if (((PostItemUseEvent)eventbus.post((Cancellable)postitemuseevent)).wasCancelled()) {
            callbackinfo.cancel();
        }
    }

    public static void onHotbarKey(CallbackInfo callbackinfo, int i) {
        if (!hotbarEventHandled[i]) {
            hotbarEventHandled[i] = true;
            HotbarKeyEvent hotbarkeyevent = new HotbarKeyEvent(i);
            VoilClient.eventBus.post(hotbarkeyevent);
            if (hotbarkeyevent.isCancelled()) {
                callbackinfo.cancel();
            }

            KeyUtil.setTimesPressed(VoilClient.mc.options.hotbarKeys[i], hotbarkeyevent.getTimesPressed());
        }
    }

    @Unique
    private static boolean checkAndMarkKeyHandled(int i) {
        if (!keyEventHandled[i]) {
            keyEventHandled[i] = true;
            return false;
        } else {
            return true;
        }
    }

    public static void a(CallbackInfo callbackinfo) {
        EventBus eventbus = VoilClient.eventBus;
        BlockBreakingEvent blockbreakingevent = BlockBreakingEvent.getInstance();
        if (((BlockBreakingEvent)eventbus.post(blockbreakingevent)).isCancelled()) {
            callbackinfo.cancel();
        }
    }

    public static void onDoAttack(CallbackInfoReturnable callbackinforeturnable) {
        EventBus eventbus = VoilClient.eventBus;
        DoAttackEvent doattackevent = DoAttackEvent.getInstance();
        if (((DoAttackEvent)eventbus.post(doattackevent)).isCancelled()) {
            callbackinforeturnable.setReturnValue(false);
        }
    }

    public static void onTick(CallbackInfo callbackinfo) {
        if (!VoilClient.getInstance().disabled) {
            TickEvent tickevent = TickEvent.getInstance();
            VoilClient.eventBus.post(tickevent);
            ScriptManager scriptmanager = VoilClient.getScriptManager();
            String s = TICK_SECTION_NAME;
            scriptmanager.dispatchEvent(s);
            if (tickevent.isCancelled()) {
                callbackinfo.cancel();
            }
        }
    }

    public static void onPickItemKey(CallbackInfo callbackinfo) {
        if (!checkAndMarkKeyHandled(4)) {
            PickItemKeyEvent pickitemkeyevent = new PickItemKeyEvent();
            VoilClient.eventBus.post(pickitemkeyevent);
            if (pickitemkeyevent.isCancelled()) {
                callbackinfo.cancel();
            }

            KeyUtil.setTimesPressed(VoilClient.mc.options.pickItemKey, pickitemkeyevent.getTimesPressed());
        }
    }

    public static void restoreCrosshairTarget() {
        VoilClient.mc.crosshairTarget = savedCrosshairTarget;
        VoilClient.mc.targetedEntity = savedTargetedEntity;
    }

    public static void onUseKey(CallbackInfo callbackinfo) {
        if (!checkAndMarkKeyHandled(3)) {
            UseKeyEvent usekeyevent = new UseKeyEvent();
            VoilClient.eventBus.post(usekeyevent);
            if (usekeyevent.isCancelled()) {
                callbackinfo.cancel();
            }

            KeyUtil.setTimesPressed(VoilClient.mc.options.useKey, usekeyevent.getTimesPressed());
        }
    }
}
