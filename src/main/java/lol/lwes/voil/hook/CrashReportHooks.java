package lol.lwes.voil.hook;

import java.util.List;
import lol.lwes.voil.util.CrashReporter;
import net.minecraft.util.crash.ReportType;

public class CrashReportHooks {
    public static void onCrashReport(Object object, ReportType reporttype, List list) {
        String s = null;
        if (reporttype == ReportType.MINECRAFT_CRASH_REPORT) {
            s = "MINECRAFT_CRASH";
        } else if (reporttype == ReportType.MINECRAFT_NETWORK_PROTOCOL_ERROR_REPORT) {
            s = "PROTOCOL_ERROR";
        }

        if (s != null) {
            CrashReporter.collectAndSendReport(s);
        }
    }
}
