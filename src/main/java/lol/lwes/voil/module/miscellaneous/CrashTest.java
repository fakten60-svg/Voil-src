package lol.lwes.voil.module.miscellaneous;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleTag;

public class CrashTest extends Module {
    private int crashCount = 0;
    private static final String CRASH_MESSAGE = "crash ";

    public CrashTest() {
        super("CrashTest", "Force crash to test crash handler", Category.Miscellaneous);
        ModuleTag[] amoduletag = new ModuleTag[]{ModuleTag.DEV};
        this.addTags(amoduletag);
    }

    @Override
    public void onTick() {
        int i = this.crashCount++;
        String s1 = CRASH_MESSAGE;
        int j = this.crashCount;
        throw new RuntimeException(s1 + j);
    }

    @Override
    public void onEnable() {
        this.crashCount = 0;
    }
}
