package lol.lwes.voil.hook;

import com.mojang.authlib.GameProfile;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.miscellaneous.NameProtect;
import lol.lwes.voil.util.entity.SkinFetcher;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class PlayerListEntryHooks {
    public static void obfuscate(GameProfile gameprofile, CallbackInfoReturnable callbackinforeturnable) {
        if (NameProtect.active && NameProtect.protectSkin.isEnabled()) {
            if (gameprofile != null && gameprofile.id() != null) {
                boolean flag = gameprofile.id().equals(VoilClient.mc.getSession().getUuidOrNull());
                if (switch (NameProtect.skinMode.getMode()) {
                    case "All" -> true;
                    case "Self" -> flag;
                    case "Enemy" -> !flag;
                    default -> false;
                }) {
                    SkinTextures skintextures = SkinFetcher.getSkinTextures(NameProtect.skinName.getValue());
                    callbackinforeturnable.setReturnValue(skintextures != null ? skintextures : DefaultSkinHelper.getSkinTextures(Util.NIL_UUID));
                }
            }
        }
    }
}
