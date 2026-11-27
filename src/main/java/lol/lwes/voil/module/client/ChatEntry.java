package lol.lwes.voil.module.client;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lol.lwes.voil.util.render.EasingType;
import lol.lwes.voil.util.render.UiAnimation;
import net.minecraft.text.Text;

public class ChatEntry {
    public final Text message;
    public final String timestamp;
    public final long createdAt;
    public final UiAnimation slideAnim;
    public final UiAnimation fadeAnim;
    public final boolean important;
    private static final String timePattern = "HH:mm";

    public ChatEntry(Text text, boolean flag) {
        this.message = text;
        this.timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern(timePattern));
        this.createdAt = System.currentTimeMillis();
        this.important = flag;
        this.slideAnim = new UiAnimation(EasingType.FLUID_SMOOTH);
        this.fadeAnim = new UiAnimation(EasingType.FLUID_SMOOTH);
        this.slideAnim.snapTo(0.0);
        this.fadeAnim.snapTo(-20.0);
        this.slideAnim.setTarget(1.0);
        this.fadeAnim.setTarget(0.0);
    }

    public void update() {
        this.slideAnim.update();
        this.fadeAnim.update();
    }
}
