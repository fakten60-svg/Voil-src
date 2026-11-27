package lol.lwes.voil.module.render;

import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.HudLayout;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.util.PlayerTargetUtil;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class PotionHUD extends Module {
    private final Map<Identifier, Integer> maxDurations = new HashMap<>();
    private final Map<Identifier, Integer> lastDurations = new HashMap<>();

    public PotionHUD() {
        super("PotionHUD", "Shows active potion effects on the HUD", Category.Render);
    }

    private String formatEffectLabel(StatusEffectInstance statuseffectinstance) {
        String s = statuseffectinstance.getEffectType().value().getName().getString();
        int i = statuseffectinstance.getAmplifier();
        String s1 = this.toRomanNumeral(i);
        String s2 = this.formatDuration(statuseffectinstance);
        StringBuilder stringbuilder = new StringBuilder(s);
        if (!s1.isEmpty()) {
            stringbuilder.append(' ').append(s1);
        }

        stringbuilder.append(" (").append(s2).append(')');
        return stringbuilder.toString();
    }

    private String toRomanNumeral(int i) {
        return switch (i) {
            case 0 -> "";
            case 1 -> "II";
            case 2 -> "III";
            case 3 -> "IV";
            case 4 -> "V";
            case 5 -> "VI";
            case 6 -> "VII";
            case 7 -> "VIII";
            case 8 -> "IX";
            case 9 -> "X";
            default -> String.valueOf(i);
        };
    }

    @Override
    public void onRender2D() {
        if (!Module.isNotInGame()) {
            Collection<StatusEffectInstance> collection = mc.player.getStatusEffects();
            if (collection.isEmpty()) {
                VoilClient.getHudLayout().setSize(this, 100, 20);
                this.maxDurations.clear();
                this.lastDurations.clear();
            } else {
                float f = VoilClient.getHudLayout().getX(this);
                float f1 = VoilClient.getHudLayout().getY(this);
                float f2 = 0.0F;
                float f3 = f1;
                HashSet<Identifier> hashset = new HashSet<>();

                for (StatusEffectInstance statuseffectinstance : collection) {
                    String s = this.formatEffectLabel(statuseffectinstance);
                    float f4 = VoilClient.fonts.getSelectedFont().getWidth(s, 9.5F);
                    float f5 = 6.0F + f4 + 6.0F;
                    float f6 = 18.0F + 3.0F + f5;
                    Render2DUtil.drawBlurRoundedTinted(f, f3 - 1.0F, 18.0F, 18.0F, 6.0F, 14.0F, 1.0F, new Color(0, 0, 0, 120));
                    this.drawEffectIcon(statuseffectinstance, f + (18.0F - 16.0F) / 2.0F, f3 + -1.0F + 1.0F);
                    float f7 = f + 18.0F + 3.0F;
                    Render2DUtil.drawBlurRoundedTinted(f7, f3 + 1.0F, f5, 14.0F, 7.0F, 14.0F, 1.0F, new Color(0, 0, 0, 140));
                    float f8 = f3 + 1.0F;
                    this.drawDurationBar(statuseffectinstance, f7, f8, f5, hashset);
                    VoilClient.fonts
                        .getSelectedFont()
                        .render(s, f7 + 6.0F, f3 + (14.0F - VoilClient.fonts.getSelectedFont().getLineHeight()) / 2.0F + 1.0F, 9.5F, Color.WHITE.getRGB());
                    if (f6 > f2) {
                        f2 = f6;
                    }

                    f3 += 20.0F;
                }

                this.maxDurations.keySet().retainAll(hashset);
                this.lastDurations.keySet().retainAll(hashset);
                HudLayout hudlayout = VoilClient.getHudLayout();
                int k = (int)Math.ceil(f2);
                int i = (int)Math.ceil(f3 - 6.0F - f1);
                int j = k;
                PotionHUD potionhud1 = this;
                hudlayout.setSize(potionhud1, j, i);
            }
        }
    }

    private void drawDurationBar(StatusEffectInstance statuseffectinstance, float f, float f1, float f2, Set<Identifier> set) {
        Identifier identifier = Registries.STATUS_EFFECT.getId(statuseffectinstance.getEffectType().value());
        if (identifier != null) {
            set.add(identifier);
            float f3;
            if (statuseffectinstance.isInfinite()) {
                f3 = 1.0F;
            } else {
                int i = Math.max(0, statuseffectinstance.getDuration());
                Integer integer = this.lastDurations.get(identifier);
                int j = integer != null && i <= integer ? Math.max(this.maxDurations.getOrDefault(identifier, i), i) : i;
                this.maxDurations.put(identifier, j);
                this.lastDurations.put(identifier, i);
                f3 = j <= 0 ? 0.0F : Math.min(1.0F, (float)i / j);
            }

            int l = statuseffectinstance.getEffectType().value().getColor();
            int k = l >> 16;
            int i1 = k & 0xFF;
            k = l >> 8;
            Color color = new Color(i1, k & 0xFF, l & 0xFF, 90);
            float f4 = f2 * f3;
            if (f4 > 0.0F) {
                Render2DUtil.drawRoundedRect(f, f1, f4, 14.0F, 7.0F, 1.0F, color);
            }
        }
    }

    private String formatDuration(StatusEffectInstance statuseffectinstance) {
        if (statuseffectinstance.isInfinite()) {
            return "infinite";
        }

        int i = statuseffectinstance.getDuration() / 20;
        if (i >= 60) {
            return String.format("%d:%02d", i / 60, i % 60);
        }

        return i + "s";
    }

    private void drawEffectIcon(StatusEffectInstance statuseffectinstance, float f, float f1) {
        Identifier identifier1 = Registries.STATUS_EFFECT.getId(statuseffectinstance.getEffectType().value());
        if (identifier1 != null) {
            String s2 = identifier1.getNamespace();
            String s1 = identifier1.getPath();
            Identifier identifier = Identifier.of(s2, "textures/mob_effect/" + s1 + ".png");
            GlTexture gltexture = PlayerTargetUtil.loadTexture(identifier);
            if (gltexture != null) {
                Color color = Color.WHITE;
                float f2 = 1.0F;
                float f3 = 0.0F;
                float f4 = 16.0F;
                float f5 = 16.0F;
                float f6 = f1;
                float f7 = f;
                GlTexture gltexture1 = gltexture;
                Render2DUtil.drawGlTexture(gltexture1, f7, f6, f5, f4, f3, f2, color);
                return;
            }
        }
    }
}
