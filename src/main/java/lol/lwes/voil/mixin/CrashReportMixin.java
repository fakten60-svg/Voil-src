package lol.lwes.voil.mixin;

import java.nio.file.Path;
import java.util.List;
import lol.lwes.voil.hook.CrashReportHooks;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.ReportType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrashReport.class)
public abstract class CrashReportMixin {
    @Inject(
        method = "writeToFile(Ljava/nio/file/Path;Lnet/minecraft/util/crash/ReportType;Ljava/util/List;)Z",
        at = @At(value = "FIELD", target = "Lnet/minecraft/util/crash/CrashReport;file:Ljava/nio/file/Path;", ordinal = 0)
    )
    private void onWTF(Path path, ReportType reporttype, List list, CallbackInfoReturnable callbackinforeturnable) {
        CrashReportHooks.onCrashReport(this, reporttype, list);
    }
}
