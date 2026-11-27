package lol.lwes.voil.module.miscellaneous;

import java.util.concurrent.ThreadLocalRandom;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.ItemUseEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ItemListSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.world.BlockUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class Prevent extends Module {
    private static final BooleanSetting preventOptionA = new BooleanSetting("Prevent Anchor", true, "Prevents placing anchor on another empty anchor");
    private static final NumberSetting preventOptionAChance = new NumberSetting(
        "Anchor Chance", 0.0, 100.0, 100.0, 1.0, "% chance to prevent placing anchor on an empty anchor"
    );
    private static final BooleanSetting preventOptionB = new BooleanSetting("Prevent Glowstone", true, "Prevents placing glowstone on ground");
    private static final NumberSetting preventOptionBChance = new NumberSetting(
        "Glowstone Chance", 0.0, 100.0, 100.0, 1.0, "% chance to prevent placing glowstone on ground"
    );
    private static final BooleanSetting preventOptionC = new BooleanSetting("Prevent DoubleGlowstone", true, "Prevents placing double glowstone on anchor");
    private static final NumberSetting preventOptionCChance = new NumberSetting(
        "DoubleGlowstone Chance", 0.0, 100.0, 100.0, 1.0, "% chance to prevent charging an already charged anchor"
    );
    private static final BooleanSetting preventOptionD = new BooleanSetting(
        "Prevent Echest", true, "Prevents opening an echest with a whitelisted item in your main hand"
    );
    private static final NumberSetting preventOptionDChance = new NumberSetting(
        "Echest Chance", 0.0, 100.0, 100.0, 1.0, "% chance to prevent opening the echest"
    );
    private static final ItemListSetting items;
    private static final BooleanSetting preventOptionE = new BooleanSetting("On Obsidian Only", true, "Prevents opening echest if on obsidian");
    private static final BooleanSetting preventOptionF = new BooleanSetting("Prevent Shield", true, "Prevents blocking with shield when holding blocks");

    public Prevent() {
        super("Prevent", "Prevents certain actions", Category.Miscellaneous);
        Setting[] asetting = new Setting[]{
            preventOptionA,
            preventOptionAChance,
            preventOptionB,
            preventOptionBChance,
            preventOptionC,
            preventOptionCChance,
            preventOptionD,
            preventOptionDChance,
            preventOptionE,
            items,
            preventOptionF
        };
        this.addSettings(asetting);
        Setting.dependSetting(preventOptionAChance, preventOptionA);
        Setting.dependSetting(preventOptionBChance, preventOptionB);
        Setting.dependSetting(preventOptionCChance, preventOptionC);
        Setting.dependSetting(preventOptionDChance, preventOptionD);
        Setting.dependSetting(items, preventOptionD);
        Setting.dependSetting(preventOptionE, preventOptionD);
    }

    static {
        ItemListSetting itemlistsetting = new ItemListSetting(
            "Echest Items",
            "Main hand items that can't open an echest",
            new Item[]{
                Items.NETHERITE_SWORD,
                Items.DIAMOND_SWORD,
                Items.NETHERITE_AXE,
                Items.DIAMOND_AXE,
                Items.MACE,
                Items.TOTEM_OF_UNDYING,
                Items.END_CRYSTAL,
                Items.OBSIDIAN,
                Items.RESPAWN_ANCHOR,
                Items.GLOWSTONE
            }
        );
        Item[] aitem = new Item[]{
            Items.NETHERITE_SWORD,
            Items.NETHERITE_AXE,
            Items.MACE,
            Items.TOTEM_OF_UNDYING,
            Items.END_CRYSTAL,
            Items.OBSIDIAN,
            Items.RESPAWN_ANCHOR,
            Items.GLOWSTONE,
            Items.ENDER_PEARL
        };
        items = itemlistsetting.setRecommendedItems(aitem).allowCategoryMatch();
    }

    private static boolean isNotFullChance(NumberSetting numbersetting) {
        double d0 = numbersetting.getValue();
        return d0 >= 100.0 || ThreadLocalRandom.current().nextDouble(100.0) < d0;
    }

    @EventHandler
    public void onItemUse(ItemUseEvent itemuseevent) {
        if (mc.player != null && mc.world != null) {
            if (mc.crosshairTarget instanceof BlockHitResult blockhitresult) {
                BlockPos blockpos = blockhitresult.getBlockPos();
                boolean flag = mc.player.isHolding(Items.GLOWSTONE);
                if (flag && preventOptionC.isEnabled() && BlockUtil.isAnchorCharged(blockpos)) {
                    NumberSetting numbersetting = preventOptionCChance;
                    if (isNotFullChance(numbersetting)) {
                        itemuseevent.cancel();
                    }
                } else if (flag && preventOptionB.isEnabled() && !BlockUtil.isBlockAt(Blocks.RESPAWN_ANCHOR, blockpos)) {
                    NumberSetting numbersetting1 = preventOptionBChance;
                    if (isNotFullChance(numbersetting1)) {
                        itemuseevent.cancel();
                    }
                } else if (preventOptionA.isEnabled() && mc.player.isHolding(Items.RESPAWN_ANCHOR) && BlockUtil.hasAnchorCharges(blockpos)) {
                    NumberSetting numbersetting2 = preventOptionAChance;
                    if (isNotFullChance(numbersetting2)) {
                        itemuseevent.cancel();
                    }
                } else {
                    if (preventOptionD.isEnabled() && BlockUtil.isBlockAt(Blocks.ENDER_CHEST, blockpos)) {
                        ItemStack itemstack = mc.player.getMainHandStack();
                        if ((!preventOptionE.isEnabled() || BlockUtil.isBlockAt(Blocks.OBSIDIAN, blockpos.down())) && !itemstack.isEmpty()) {
                            ItemListSetting itemlistsetting = items;
                            Item item = itemstack.getItem();
                            if (itemlistsetting.matches(item)) {
                                NumberSetting numbersetting3 = preventOptionDChance;
                                if (isNotFullChance(numbersetting3)) {
                                    itemuseevent.cancel();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
