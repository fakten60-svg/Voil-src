package lol.lwes.voil.module.miscellaneous;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleTag;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationManager;

public class RotationTest extends Module {
    private final NumberSetting yaw;
    private final NumberSetting pitch;
    private static final long REQUEST_LIFETIME_MS = 100L;

    public RotationTest() {
        super("RotationTest", "TEST MODULE!", Category.Miscellaneous);
        this.yaw = new NumberSetting("Yaw", -180.0, 180.0, 90.0, 1.0, "TEST SETTING!");
        this.pitch = new NumberSetting("Pitch", -90.0, 90.0, 30.0, 1.0, "TEST SETTING!");
        ModuleTag[] amoduletag = new ModuleTag[]{ModuleTag.DEV};
        this.addTags(amoduletag);
        Setting[] asetting = new Setting[]{this.yaw, this.pitch};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        RotationManager rotationmanager = VoilClient.getRotationManager();
        Rotation rotation = new Rotation(this.yaw.getFloat(), this.pitch.getFloat());
        RotationTest rotationtest1 = this;
        rotationmanager.requestRotation(rotationtest1, rotation);
    }
}
