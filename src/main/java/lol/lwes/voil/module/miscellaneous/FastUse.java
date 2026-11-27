package lol.lwes.voil.module.miscellaneous;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.mixin.accessor.MinecraftClientAccessor;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.MultiSelectSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

public class FastUse extends Module {
    private final NumberSetting speed = new NumberSetting("Speed", 0.0, 4.0, 1.0, 1.0, "Ticks between uses");
    private final BooleanSetting mouseSimulation = new BooleanSetting("Mousesimulation", true, "Simulate clicks while using");
    private final MultiSelectSetting options;
    private final ModeSetting hand;
    private int useTicks;

    public FastUse() {
        super("FastUse", "Uses items faster", Category.Miscellaneous);
        String[] astring = new String[]{"Throwables", "Blocks", "Any item"};
        this.options = new MultiSelectSetting("Options", "Select options", astring);
        this.hand = new ModeSetting("Hand", "Both", "Which hand's item triggers fast use", new String[]{"Main", "Off", "Both"});
        Setting[] asetting = new Setting[]{this.speed, this.mouseSimulation, this.options, this.hand};
        this.addSettings(asetting);
    }

    private static boolean isSnowball(ItemStack itemstack) {
        return itemstack.isOf(Items.SNOWBALL)
            || itemstack.isOf(Items.EGG)
            || itemstack.isOf(Items.BLUE_EGG)
            || itemstack.isOf(Items.BROWN_EGG)
            || itemstack.isOf(Items.SPLASH_POTION)
            || itemstack.isOf(Items.LINGERING_POTION)
            || itemstack.isOf(Items.EXPERIENCE_BOTTLE);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.world != null && mc.currentScreen == null) {
            if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), 1) != 1) {
                this.useTicks = 0;
            } else if (!mc.player.isUsingItem()) {
                boolean flag = !this.hand.is("Off") && this.canUse(mc.player.getMainHandStack());
                boolean flag1 = !this.hand.is("Main") && this.canUse(mc.player.getOffHandStack());
                if (flag || flag1) {
                    int i = this.useTicks;
                    int j = i + 1;
                    FastUse fastuse1 = this;
                    fastuse1.useTicks = j;
                    if (j > this.speed.getInt()) {
                        this.useTicks = 0;
                        ((MinecraftClientAccessor)mc).setItemUseCooldown(0);
                        if (this.mouseSimulation.isEnabled()) {
                            VoilClient.getMouseSimulator().clickButton(1);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() {
        this.useTicks = 0;
    }

    private boolean canUse(ItemStack itemstack) {
        if (itemstack.isEmpty() || itemstack.getMaxUseTime(mc.player) > 0) {
            return false;
        }

        if (this.options.isSelected("Any item")) {
            return true;
        }

        if (this.options.isSelected("Blocks") && itemstack.getItem() instanceof BlockItem) {
            return true;
        }

        return this.options.isSelected("Throwables") && isSnowball(itemstack);
    }
}
