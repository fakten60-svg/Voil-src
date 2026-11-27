package lol.lwes.voil.mixin;

import java.nio.file.Path;
import lol.lwes.voil.hook.PlayerSkinTextureDownloaderHooks;
import net.minecraft.client.texture.PlayerSkinTextureDownloader;
import net.minecraft.util.AssetInfo.SkinAssetInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerSkinTextureDownloader.class)
public abstract class PlayerSkinTextureDownloaderMixin {
    @Inject(method = "method_65866", at = @At("RETURN"))
    private void onDownloadAndRemapInLambda(Path path, SkinAssetInfo skinassetinfo, boolean flag, CallbackInfoReturnable callbackinforeturnable) {
        PlayerSkinTextureDownloaderHooks.onDownloadAndRemapInLambda(skinassetinfo, callbackinforeturnable);
    }
}
