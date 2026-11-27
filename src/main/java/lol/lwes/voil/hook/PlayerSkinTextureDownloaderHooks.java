package lol.lwes.voil.hook;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import lol.lwes.voil.util.PlayerTargetUtil;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.AssetInfo.SkinAssetInfo;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class PlayerSkinTextureDownloaderHooks {
    public static void onDownloadAndRemapInLambda(SkinAssetInfo skinassetinfo, CallbackInfoReturnable callbackinforeturnable) {
        RenderAPI.schedule(() -> {
            Identifier identifier = skinassetinfo.id();
            Object object = callbackinforeturnable.getReturnValue();
            PlayerTargetUtil.registerSkinImage(identifier, (NativeImage)object);
        });
    }
}
