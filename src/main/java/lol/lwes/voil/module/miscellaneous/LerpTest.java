package lol.lwes.voil.module.miscellaneous;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleTag;
import lol.lwes.voil.setting.KeybindSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.ChatUtil;
import lol.lwes.voil.util.render.EasingFunction;
import lol.lwes.voil.util.render.UiEasings;
import lol.lwes.voil.util.rotation.Rotation;
import net.minecraft.client.network.ClientPlayerEntity;

public class LerpTest extends Module {
    private final KeybindSetting pressThingo;
    private final NumberSetting sampleCount;
    private final NumberSetting topYawCount;
    private final NumberSetting topPitchCount;
    boolean keyWasDown;
    boolean analyzed;
    int sampleIndex;
    List samples;

    public LerpTest() {
        super("LerpTest", "module for assisting in the research for easings that best imitate player rotations", Category.Miscellaneous);
        String s = "";
        this.pressThingo = new KeybindSetting("Press thingo", 0, false, "aaa");
        this.sampleCount = new NumberSetting("Sample count", 1.0, 1000.0, 10.0, 1.0, "samples for rots");
        this.topYawCount = new NumberSetting("Top yaw count", 0.0, UiEasings.getEasings().size(), 3.0, 1.0, "top easing yaw to print");
        this.topPitchCount = new NumberSetting("Top pitch count", 0.0, UiEasings.getEasings().size(), 0.0, 1.0, "top easing yaw to print");
        this.samples = new ArrayList();
        ModuleTag[] amoduletag = new ModuleTag[]{ModuleTag.DEV};
        this.addTags(amoduletag);
        Setting[] asetting = new Setting[]{this.pressThingo, this.sampleCount, this.topYawCount, this.topPitchCount};
        this.addSettings(asetting);
    }

    @Override
    public void onRender2D() {
        boolean flag = this.pressThingo.isPressed();
        if (this.keyWasDown != flag) {
            if (flag) {
                this.analyzed = false;
                this.sampleIndex = 0;
                this.samples.clear();
            }

            this.keyWasDown = flag;
        }

        if (flag) {
            int l = this.sampleIndex;
            LerpTest lerptest1 = this;
            lerptest1.sampleIndex = l + 1;
            if (l < this.sampleCount.getInt()) {
                ClientPlayerEntity clientplayerentity = mc.player;
                this.samples.add(Rotation.fromEntity(clientplayerentity));
            } else if (!this.analyzed) {
                Map<String, EasingFunction> map = UiEasings.getEasings();
                ArrayList<Entry<String, Double>> arraylist = new ArrayList<>();
                ArrayList<Entry<String, Double>> arraylist1 = new ArrayList<>();

                for (Entry<String, EasingFunction> entry : map.entrySet()) {
                    Rotation rotation = (Rotation)this.samples.get(0);
                    List list = this.samples;
                    int k = this.samples.size();
                    Rotation rotation1 = (Rotation)list.get(k - 1);
                    double d0 = 0.0;
                    double d1 = 0.0;
                    int i = this.samples.size();

                    for (int j = 1; j < i; j++) {
                        double d2 = entry.getValue().apply((float) j / i);
                        Rotation rotation2 = rotation.lerp(d2, rotation1);
                        Rotation rotation3 = (Rotation)this.samples.get(j);
                        d1 += rotation3.yawDifference(rotation2) / i;
                        d0 += rotation3.pitchDifference(rotation2) / i;
                    }

                    arraylist.add(Map.entry(entry.getKey(), d1));
                    arraylist1.add(Map.entry(entry.getKey(), d0));
                }

                arraylist.sort(Comparator.comparingDouble(entry3 -> Math.abs(entry3.getValue())));
                arraylist1.sort(Comparator.comparingDouble(entry3 -> Math.abs(entry3.getValue())));
                if (this.topYawCount.getInt() > 0) {
                    int i1 = this.topYawCount.getInt();
                    ChatUtil.info("Top " + i1 + " yaw:");

                    for (int k1 = 0; k1 < this.topYawCount.getInt(); k1++) {
                        Entry<String, Double> entry1 = arraylist.get(k1);
                        String s16 = entry1.getKey();
                        String s8 = String.valueOf(entry1.getValue());
                        ChatUtil.info(s16 + " -> " + s8);
                    }
                }

                if (this.topPitchCount.getInt() > 0) {
                    int j1 = this.topPitchCount.getInt();
                    ChatUtil.info("Top " + j1 + " pitch:");

                    for (int l1 = 0; l1 < this.topPitchCount.getInt(); l1++) {
                        Entry<String, Double> entry2 = arraylist1.get(l1);
                        String s17 = entry2.getKey();
                        String s13 = String.valueOf(entry2.getValue());
                        ChatUtil.info(s17 + " -> " + s13);
                    }
                }

                this.analyzed = true;
            }
        }
    }

    @Override
    public void onEnable() {
        this.keyWasDown = false;
    }
}
