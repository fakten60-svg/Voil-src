package lol.lwes.voil.module.render;

import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.ParticleEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleTag;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ItemListSetting;
import lol.lwes.voil.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.ExplosionSmokeParticle;
import net.minecraft.client.particle.LargeFireSmokeParticle;

public class NoRender extends Module {
    public final BooleanSetting explosions = new BooleanSetting("Explosions", true, "Stops rendering explosions");
    public final BooleanSetting fireOverlay = new BooleanSetting("Fire Overlay", true, "Stops rendering fire overlay");
    public final BooleanSetting smoke = new BooleanSetting("Smoke", true, "Stops rendering smoke");
    public final BooleanSetting totemAnimation = new BooleanSetting("Totem Animation", true, "Stops rendering totem animation");
    public static final BooleanSetting hideBlocksEnabled = new BooleanSetting("Block Render", false, "Hide selected blocks");
    public static final ItemListSetting hiddenBlocks;

    public NoRender() {
        super("NoRender", "Stops rendering certain things", Category.Render);
        Setting[] asetting = new Setting[]{this.explosions, this.fireOverlay, this.smoke, this.totemAnimation, hideBlocksEnabled, hiddenBlocks};
        this.addSettings(asetting);
        ModuleTag[] amoduletag = new ModuleTag[]{ModuleTag.NOTOBSPROOF};
        this.addTags(amoduletag);
        Setting.dependSetting(hiddenBlocks, hideBlocksEnabled);
        BooleanSetting booleansetting = hideBlocksEnabled;
        Runnable runnable = NoRender::reloadWorld;
        booleansetting.setOnChange(runnable);
        ItemListSetting itemlistsetting = hiddenBlocks;
        Runnable runnable1 = NoRender::reloadWorld;
        itemlistsetting.setOnChange(runnable1);
    }

    static {
        ItemListSetting itemlistsetting = new ItemListSetting("Blocks", "Blocks affected by hidden block");
        Block[] ablock = new Block[]{Blocks.FIRE, Blocks.SOUL_FIRE};
        hiddenBlocks = itemlistsetting.addBlocks(ablock);
    }

    private static void reloadWorld() {
        mc.execute(() -> {
            if (mc.world != null && mc.worldRenderer != null) {
                mc.worldRenderer.reload();
            }
        });
    }

    @Override
    public void onDisable() {
        reloadWorld();
    }

    @Override
    public void onEnable() {
        reloadWorld();
    }

    @EventHandler(priority = 200)
    private void onParticle(ParticleEvent particleevent) {
        if (this.explosions.isEnabled()
            && (particleevent.particle instanceof ExplosionLargeParticle || particleevent.particle instanceof ExplosionSmokeParticle)) {
            particleevent.cancel();
        }

        if (this.smoke.isEnabled() && particleevent.particle instanceof LargeFireSmokeParticle) {
            particleevent.cancel();
        }
    }
}
