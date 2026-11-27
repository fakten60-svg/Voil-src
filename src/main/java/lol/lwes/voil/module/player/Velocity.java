package lol.lwes.voil.module.player;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EntityPositionEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationManager;
import lol.lwes.voil.util.rotation.RotationPriority;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public class Velocity extends Module {
    NumberSetting ticks = new NumberSetting("Ticks", 1.0, 20.0, 6.0, 1.0, "How many ticks to prevent knockback for");
    int remainingTicks;

    public Velocity() {
        super("Velocity", "Prevents knockback", Category.Player);
        Setting[] asetting = new Setting[]{this.ticks};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (this.remainingTicks <= 0) {
            VoilClient.getRotationManager().reset();
        } else if (!mc.player.isOnGround()) {
            VoilClient.getRotationManager().reset();
        } else {
            int i = this.remainingTicks--;
            RotationManager rotationmanager = VoilClient.getRotationManager();
            Rotation rotation = new Rotation(mc.player.getYaw(), 90.0);
            rotationmanager.requestFromCaller(rotation, RotationPriority.HIGH);
            if (mc.crosshairTarget instanceof BlockHitResult blockhitresult) {
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockhitresult);
            }
        }
    }

    @EventHandler
    public void onKnockback(EntityPositionEvent entitypositionevent) {
        this.remainingTicks = this.ticks.getInt();
        entitypositionevent.setX(0.0);
        entitypositionevent.setY(0.0);
        entitypositionevent.setZ(0.0);
    }
}
