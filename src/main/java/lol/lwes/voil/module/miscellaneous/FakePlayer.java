package lol.lwes.voil.module.miscellaneous;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.setting.StringSetting;
import lol.lwes.voil.util.entity.FakePlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;

public class FakePlayer extends Module {
    private FakePlayerEntity fakePlayer;
    private final StringSetting name = new StringSetting("Name", "Voil", "The name of the fake player");
    private final NumberSetting removeAfterSeconds = new NumberSetting(
        "Remove After Seconds", 0.0, 360.0, 0.0, 1.0, "Time in seconds after which the fake player will be removed. Set to 0 to never remove."
    );
    private long spawnTime;

    public FakePlayer() {
        super("FakePlayer", "Spawns a Fake Player", Category.Miscellaneous);
        Setting[] asetting = new Setting[]{this.name, this.removeAfterSeconds};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (!Module.isNotInGame()) {
            double d0 = this.removeAfterSeconds.getValue();
            if (d0 > 0.0 && System.currentTimeMillis() - this.spawnTime >= d0 * 1000.0) {
                this.toggle();
            }
        }
    }

    @Override
    public void onDisable() {
        if (!Module.isNotInGame()) {
            if (this.fakePlayer != null) {
                this.fakePlayer.despawn();
                this.fakePlayer = null;
            }
        }
    }

    @Override
    public void onEnable() {
        if (!Module.isNotInGame()) {
            ClientPlayerEntity clientplayerentity = mc.player;
            this.fakePlayer = new FakePlayerEntity(clientplayerentity, this.name.getValue(), 20.0F, true);
            this.fakePlayer.spawn();
            this.spawnTime = System.currentTimeMillis();
        }
    }
}
