package lol.lwes.voil.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineEvent;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.AttackEntityPreEvent;
import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.ItemUseEvent;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.client.Sounds;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;

public class SoundPlayer {
    private final Random random = new Random();

    public void playRandomSound(String[] astring, int i) {
        String s = astring[this.random.nextInt(astring.length)];
        this.playSound(s, i);
    }

    public void playSound(String s, int i) {
        String s1 = "/assets/tulip/sounds/" + s + ".wav";

        try {
            Path custom = FabricLoader.getInstance().getConfigDir().resolve("voil").resolve("sounds").resolve(s + ".wav");
            InputStream inputstream = Files.exists(custom) ? Files.newInputStream(custom) : this.getClass().getResourceAsStream(s1);
            if (inputstream == null) {
                return;
            }

            BufferedInputStream bufferedinputstream = new BufferedInputStream(inputstream);
            AudioInputStream audioinputstream = AudioSystem.getAudioInputStream(bufferedinputstream);
            Clip clip = AudioSystem.getClip();
            clip.open(audioinputstream);
            clip.loop(i - 1);
            clip.start();
            clip.addLineListener(lineevent -> {
                if (lineevent.getType() == Type.STOP) {
                    try {
                        clip.close();
                        audioinputstream.close();
                        bufferedinputstream.close();
                        inputstream.close();
                    } catch (Exception exception1) {
                        exception1.printStackTrace();
                    }
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @EventHandler
    public void attack(AttackEntityPreEvent attackentitypreevent) {
        if (Sounds.onAttackSound.isEnabled() && ModuleManager.INSTANCE.isEnabled(Sounds.class)) {
            if (VoilClient.mc.player.getMainHandStack().getItem() instanceof AxeItem) {
                String[] astring = new String[]{"axe1", "axe2"};
                this.playRandomSound(astring, 1);
            } else if (attackentitypreevent.getTarget() instanceof PlayerEntity) {
                String[] astring1 = new String[]{"sword1", "sword2", "sword3", "sword4"};
                this.playRandomSound(astring1, 1);
            }
        }
    }

    @EventHandler
    public void onItemUse(ItemUseEvent itemuseevent) {
        if (Sounds.onItemUseSound.isEnabled() && ModuleManager.INSTANCE.isEnabled(Sounds.class)) {
            if (VoilClient.mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
                this.playSound("double", 1);
            } else {
                this.playSound("click", 1);
            }
        }
    }
}
