package lol.lwes.voil.module.combat;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import java.awt.Color;
import java.util.function.BooleanSupplier;
import lol.lwes.voil.event.DisplacementEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.BooleanSetting;
import lol.lwes.voil.setting.ColorSetting;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.RangeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.inventory.InventoryUtil;
import lol.lwes.voil.util.prediction.DefaultKnockbackPredictor;
import lol.lwes.voil.util.prediction.KnockbackPredictor;
import lol.lwes.voil.util.prediction.KnockbackRequest;
import lol.lwes.voil.util.prediction.KnockbackResult;
import lol.lwes.voil.util.render.Render3DUtil;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class KnockbackDisplacement extends Module {
    private final RangeSetting yawOffset;
    private final BooleanSetting playerOnly;
    private final BooleanSetting onlyOnKey;
    private final KeybindSetting activateKey;
    private final BooleanSetting render;
    private final ColorSetting renderColor;
    private final BooleanSetting considerCollisions;
    private Vec3d displacedPos;
    private boolean displaceFailed;
    private Entity displacedEntity;

    public KnockbackDisplacement() {
        super("KnockbackDisplacement", "Changes knockback direction", Category.Combat);
        this.yawOffset = new RangeSetting("Yaw Offset", -180.0, 180.0, 80.0, 100.0, 1.0, "Offset to apply to knockback yaw");
        this.playerOnly = new BooleanSetting("Player Only", true, "Only perform knockback displacement on players");
        this.onlyOnKey = new BooleanSetting("Only On Key", false, "Only kb displace on key");
        this.activateKey = new KeybindSetting("Activate Key", 0, false, "Key to enable kb displacement with");
        this.render = new BooleanSetting("Render", true, "Draws a box where the target will be knocked to");
        Color color = new Color(255, 80, 80, 160);
        this.renderColor = new ColorSetting("Render Color", color, "Color of the prediction box", true);
        this.considerCollisions = new BooleanSetting("Consider Collisions", true, "Whether to consider collisions when predicting knockback");
        Setting[] asetting = new Setting[]{
            this.yawOffset, this.playerOnly, this.render, this.renderColor, this.considerCollisions, this.onlyOnKey, this.activateKey
        };
        this.addSettings(asetting);
        KeybindSetting keybindsetting = this.activateKey;
        BooleanSupplier booleansupplier = this.onlyOnKey::isEnabled;
        keybindsetting.setVisibleWhen(booleansupplier);
        ColorSetting colorsetting = this.renderColor;
        BooleanSupplier booleansupplier1 = this.render::isEnabled;
        colorsetting.setVisibleWhen(booleansupplier1);
        BooleanSetting booleansetting = this.considerCollisions;
        BooleanSupplier booleansupplier2 = this.render::isEnabled;
        booleansetting.setVisibleWhen(booleansupplier2);
    }

    @Override
    public String getSuffix() {
        long j = Math.round(this.yawOffset.getValueMax());
        long i = Math.round(this.yawOffset.getValueMin());
        return i + "-" + j;
    }

    @Override
    public void onEnable() {
        this.displacedPos = null;
        this.displacedEntity = null;
    }

    @Override
    public void onWorldRender(RenderAPI.RenderContext rendercontext) {
        if (this.render.isEnabled() && this.displacedPos != null && this.displacedEntity != null) {
            Color color = this.displaceFailed ? Color.RED : this.renderColor.getColor();
            Render3DUtil.drawPillar(new Matrix4f(), this.displacedPos, color, 0.3F, this.displacedEntity.getHeight());
        }
    }

    @EventHandler
    private void onDisplacement(DisplacementEvent displacementevent) {
        if (displacementevent.entity instanceof PlayerEntity || !this.playerOnly.isEnabled()) {
            if (!this.onlyOnKey.isEnabled() || this.activateKey.isPressed()) {
                displacementevent.handled = true;
                displacementevent.yawOffset = (float)this.yawOffset.getRandomValue();
                Entity entity = displacementevent.entity;
                displacementevent.callback = f -> {
                    if (this.render.isEnabled() && entity instanceof LivingEntity livingentity) {
                        Registry registry = mc.player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
                        ItemStack itemstack = mc.player.getMainHandStack();
                        RegistryKey registrykey = Enchantments.KNOCKBACK;
                        ItemStack itemstack1 = itemstack;
                        int i = InventoryUtil.getEnchantmentLevel(itemstack1, registrykey, registry);
                        KnockbackPredictor knockbackpredictor = DefaultKnockbackPredictor.create();
                        KnockbackRequest knockbackrequest = new KnockbackRequest(mc.player, livingentity, f, mc.player.isSprinting(), i);
                        KnockbackResult knockbackresult = this.considerCollisions.isEnabled()
                            ? knockbackpredictor.simulate(knockbackrequest)
                            : knockbackpredictor.simulateFast(knockbackrequest);
                        this.displacedPos = knockbackresult.position();
                        this.displaceFailed = knockbackresult.collided();
                        this.displacedEntity = entity;
                    }
                };
            }
        }
    }
}
