package lol.lwes.voil.util;

import lol.lwes.voil.VoilClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChatUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger("Voil/Chat");
    private static boolean enabled = true;

    private ChatUtil() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isDisabled() {
        return !enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static void info(Object... args) {
        info(stringArg(args, 0));
    }

    public static void info(String message) {
        if (message == null || !enabled) {
            return;
        }

        MinecraftClient mc = VoilClient.mc;
        if (mc == null || mc.inGameHud == null) {
            LOGGER.info(message);
            return;
        }

        if (mc.isOnThread()) {
            mc.inGameHud.getChatHud().addMessage(Text.literal(message));
        } else {
            mc.execute(() -> mc.inGameHud.getChatHud().addMessage(Text.literal(message)));
        }
    }

    public static void send(String message) {
        ClientPlayNetworkHandler network = networkHandler();
        if (message == null || message.isBlank() || network == null) {
            return;
        }

        if (message.startsWith("/")) {
            network.sendChatCommand(message.substring(1));
        } else {
            network.sendChatMessage(message);
        }
    }

    public static void whisper(String target, String message) {
        ClientPlayNetworkHandler network = networkHandler();
        if (target == null || message == null || target.isBlank() || network == null) {
            return;
        }

        network.sendChatCommand("msg " + target + " " + message);
    }

    private static ClientPlayNetworkHandler networkHandler() {
        MinecraftClient mc = VoilClient.mc;
        return mc == null || mc.player == null ? null : mc.getNetworkHandler();
    }

    private static String stringArg(Object[] args, int index) {
        int seen = 0;
        for (Object arg : args) {
            if (arg instanceof String value) {
                if (seen == index) {
                    return value;
                }
                seen++;
            }
        }
        return null;
    }
}
