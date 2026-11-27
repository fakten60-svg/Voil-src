package lol.lwes.voil.util;

import java.util.ArrayList;
import java.util.List;
import lol.lwes.voil.api.ApiResponse;
import lol.lwes.voil.config.ConfigManager;

public class ReleaseNotes {
    private List<ReleaseEntry> entries = List.of();
    private String latestVersion = "";

    public void markAsSeen() {
        if (!this.latestVersion.isEmpty()) {
            String s = this.latestVersion;
            ConfigManager.setValue("lastSeenRelease", s);
        }

        this.entries = List.of();
    }

    private static List parseEntries(String s) {
        return s != null && !s.isEmpty() ? List.of(s.split("\u0000")) : List.of();
    }

    public boolean hasEntries() {
        return !this.entries.isEmpty();
    }

    public List<ReleaseEntry> getEntries() {
        return this.entries;
    }

    public void loadFromResponse(ApiResponse apiresponse) {
        this.latestVersion = apiresponse.getString("latest");
        int i = apiresponse.getInt("count");
        ArrayList<ReleaseEntry> arraylist = new ArrayList<>(Math.max(0, i));

        for (int j = 0; j < i; j++) {
            String s5 = "r" + j + ".id";
            String s11 = apiresponse.getString(s5);
            String s6 = "r" + j + ".version";
            String s12 = apiresponse.getString(s6);
            String s7 = "r" + j + ".added";
            List list = parseEntries(apiresponse.getString(s7));
            String s8 = "r" + j + ".modified";
            List list1 = parseEntries(apiresponse.getString(s8));
            String s9 = "r" + j + ".removed";
            arraylist.add(new ReleaseEntry(s11, s12, list, list1, parseEntries(apiresponse.getString(s9))));
        }

        this.entries = arraylist;
        if (this.entries.isEmpty() && !this.latestVersion.isEmpty()) {
            String s10 = this.latestVersion;
            ConfigManager.setValue("lastSeenRelease", s10);
        }
    }
}
