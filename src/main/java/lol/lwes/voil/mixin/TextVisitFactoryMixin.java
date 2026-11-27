package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.NameProtectHook;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TextVisitFactory.class)
public abstract class TextVisitFactoryMixin {
    @ModifyVariable(
        method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
        at = @At("HEAD"),
        argsOnly = true
    )
    private static String adjustText(String s, String s1, int i) {
        return i != 0 ? s : NameProtectHook.protectName(s);
    }
}
