package lol.lwes.voil.module.miscellaneous;

import java.util.regex.Pattern;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.util.PlayerTargetUtil;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class Teams extends Module {
    private static final Pattern COLOR_CODE_PATTERN;

    public Teams() {
        super("Teams", "Makes you not attack your teammates", Category.Miscellaneous);
    }

    static {
        COLOR_CODE_PATTERN = Pattern.compile("§[0-9A-Fk-o]", Pattern.CASE_INSENSITIVE);
    }

    private boolean hasSameTeamPrefix(Text text, Text text1) {
        String s = stripFormatting(text.getString());
        String s1 = stripFormatting(text1.getString());
        String[] astring = s.split(" ");
        String[] astring1 = s1.split(" ");
        return astring.length > 1 && astring1.length > 1 && astring[0].equals(astring1[0]);
    }

    private Integer getDyeableFlag(ItemStack itemstack) {
        return itemstack != null && itemstack.isIn(ItemTags.DYEABLE) ? DyedColorComponent.getColor(itemstack, -6265536) : null;
    }

    public static String stripFormatting(String s) {
        return COLOR_CODE_PATTERN.matcher(s).replaceAll("");
    }

    @Override
    public void onTick() {
        if (mc.world != null && mc.player != null) {
            mc.world
                .getPlayers()
                .stream()
                .filter(this::isTeammate)
                .forEach(abstractclientplayerentity -> PlayerTargetUtil.addIgnored(abstractclientplayerentity.getUuid()));
        }
    }

    public boolean isTeammate(LivingEntity livingentity) {
        if (mc.player.isTeammate(livingentity)) {
            return true;
        }

        Text text = mc.player.getDisplayName();
        Text text1 = livingentity.getDisplayName();
        if (text == null || text1 == null) {
            return false;
        }

        if (this.hasSameColor(text, text1) || this.hasSameTeamPrefix(text1, text)) {
            return true;
        }

        return livingentity instanceof PlayerEntity playerentity && this.hasSameArmor(playerentity);
    }

    @Override
    public void onDisable() {
        PlayerTargetUtil.clearIgnored();
    }

    @Override
    public void onEnable() {
        PlayerTargetUtil.clearIgnored();
    }

    private boolean hasSameColor(Text text, Text text1) {
        Style style = text1.getStyle();
        Style style1 = text.getStyle();
        return style.getColor() != null && style1.getColor() != null && style.getColor().equals(style1.getColor());
    }

    private boolean hasSameArmorColor(PlayerEntity playerentity, int i) {
        ItemStack itemstack = mc.player.getInventory().getStack(i);
        ItemStack itemstack1 = playerentity.getInventory().getStack(i);
        Integer integer = this.getDyeableFlag(itemstack);
        Integer integer1 = this.getDyeableFlag(itemstack1);
        return integer != null && integer.equals(integer1);
    }

    private boolean hasSameArmor(PlayerEntity playerentity) {
        return this.hasSameArmorColor(playerentity, 3) || this.hasSameArmorColor(playerentity, 2);
    }
}
