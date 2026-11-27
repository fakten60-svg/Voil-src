package lol.lwes.voil.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.gui.NotificationManager;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.util.CrashEntry;

public class CrashHandler {
    public static final List crashLog = Collections.synchronizedList(new ArrayList());

    public static void handleModuleCrash(Module module, Throwable throwable) {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        throwable.printStackTrace(new PrintStream(bytearrayoutputstream));
        crashLog.add(new CrashEntry(module.getName(), bytearrayoutputstream.toString(), System.currentTimeMillis()));
        Module.LOGGER.error("[{}] Exception in module, disabling", module.getName(), throwable);

        try {
            module.forceDisable();
        } catch (Throwable throwable1) {
            Module.LOGGER.error("[{}] Failed to disable", module.getName(), throwable1);
        }

        NotificationManager notificationmanager = VoilClient.getNotifications();
        String s = module.getName();
        String s2 = s + " disabled due to crash";
        String s3 = "Crash Handler";
        notificationmanager.addNotification(s3, s2);
    }
}
