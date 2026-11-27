package lol.lwes.voil.module.render;

import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.HeldItemRenderEvent;
import lol.lwes.voil.event.SwingProgressEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleTag;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class SwingAnimation extends Module {
    public static final ModeSetting mode = new ModeSetting(
        "Mode", "Exhibition", "Modes for swing animation", new String[]{"Sigma", "Astolfo", "No Swing", "Smooth", "Exhibition", "Block", "No Delay"}
    );
    private float smoothProgress = 0.0F;
    private float prevProgress = 0.0F;

    public SwingAnimation() {
        super("SwingAnimation", "Renders sword swing animation differently", Category.Render);
        Setting[] asetting = new Setting[]{mode};
        this.addSettings(asetting);
        ModuleTag[] amoduletag = new ModuleTag[]{ModuleTag.NOTOBSPROOF};
        this.addTags(amoduletag);
    }

    @EventHandler
    public void onSwing(SwingProgressEvent swingprogressevent) {
        float f = swingprogressevent.getProgress();
        switch (mode.getMode()) {
            case "Smooth":
                float f5 = 0.1F;
                float f7 = Math.min(1.0F, f * f5);
                swingprogressevent.setProgress((float)Math.sin(f7 * Math.PI));
                break;
            case "No Swing":
                swingprogressevent.setProgress(1.0F);
                break;
            case "Sigma":
                swingprogressevent.setProgress(Math.min(1.0F, f * 1.5F));
                break;
            case "Block":
                float f4 = 0.1F;
                float f6 = Math.min(1.0F, f * f4);
                float f3 = (float)Math.sin(f6 * Math.PI);
                this.smoothProgress = MathHelper.lerp(0.04F, this.prevProgress, f3);
                this.prevProgress = this.smoothProgress;
                swingprogressevent.setProgress(0.0F);
                swingprogressevent.cancel();
                break;
            case "Astolfo":
                swingprogressevent.setProgress((float)Math.sin(f * Math.PI));
                break;
            case "Exhibition":
                float f1 = 0.2F;
                float f2 = Math.min(1.0F, f * f1);
                swingprogressevent.setProgress(MathHelper.sin(f2 * (float) Math.PI));
            case "No Delay":
        }
    }

    @EventHandler
    public void onRenderItem(HeldItemRenderEvent helditemrenderevent) {
        if (helditemrenderevent.stack.contains(DataComponentTypes.WEAPON)) {
            String s = mode.getMode();
            if (!"Vanilla".equals(s)) {
                MatrixStack matrixstack = helditemrenderevent.matrices;
                matrixstack.push();
                switch (s) {
                    case "Exhibition" -> {
                        float f1 = 3.0F;
                        matrixstack.translate(0.3F, -2.5F, -2.0F);
                        matrixstack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f1 * 40.0F));
                        matrixstack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f1 * 15.0F));
                        matrixstack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(70.0F));
                    }
                    case "Block" -> {
                        helditemrenderevent.setEquipProgress(0.0F);
                        matrixstack.translate(0.6F, -0.5F, -0.1F);
                        matrixstack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(85.0F));
                        matrixstack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-75.0F));
                        float f = this.smoothProgress * 180.0F;
                        matrixstack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-f));
                        matrixstack.translate(0.0F, 0.6F, 0.7F);
                    }
                    default -> {
                    }
                }
            }
        }
    }
}
