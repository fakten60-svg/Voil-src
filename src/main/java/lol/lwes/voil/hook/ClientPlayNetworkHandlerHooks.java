package lol.lwes.voil.hook;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.command.Command;
import lol.lwes.voil.command.CommandManager;
import lol.lwes.voil.event.ChunkLoadEvent;
import lol.lwes.voil.event.CommandSuggestionsEvent;
import lol.lwes.voil.event.EventBus;
import lol.lwes.voil.event.HookPostEvent;
import lol.lwes.voil.event.HookPreEvent;
import lol.lwes.voil.event.WorldJoinEvent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Entry;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class ClientPlayNetworkHandlerHooks {
    @Unique
    private static final List<UUID> SUGGESTIONS = new ArrayList<>();

    public static void sendPosLookMovementPacketsPost() {
        if (!VoilClient.getInstance().disabled) {
            if (VoilClient.mc.player != null) {
                VoilClient.eventBus.postObject(HookPreEvent.create("onPlayerPositionLook"));
            }
        }
    }

    public static void onJoin() {
        EventBus eventbus = VoilClient.eventBus;
        WorldJoinEvent worldjoinevent = new WorldJoinEvent();
        eventbus.post(worldjoinevent);
    }

    public static void onChunkData(ChunkDataS2CPacket chunkdatas2cpacket) {
        WorldChunk worldchunk = VoilClient.mc.world.getChunk(chunkdatas2cpacket.getChunkX(), chunkdatas2cpacket.getChunkZ());
        EventBus eventbus = VoilClient.eventBus;
        ChunkLoadEvent chunkloadevent = new ChunkLoadEvent(worldchunk);
        eventbus.post(chunkloadevent);
    }

    public static void onCommandSuggestions(CommandSuggestionsS2CPacket commandsuggestionss2cpacket) {
        if (!VoilClient.getInstance().disabled) {
            EventBus eventbus = VoilClient.eventBus;
            CommandSuggestionsEvent commandsuggestionsevent = new CommandSuggestionsEvent(commandsuggestionss2cpacket);
            eventbus.post(commandsuggestionsevent);
        }
    }

    public static void onPlayerList(PlayerListS2CPacket playerlists2cpacket) {
        for (Entry entry : playerlists2cpacket.getPlayerAdditionEntries()) {
            SUGGESTIONS.add(entry.profileId());
        }

        if (VoilClient.mc.world != null) {
            for (UUID uuid : SUGGESTIONS) {
                PlayerEntity playerentity = VoilClient.mc.world.getPlayerByUuid(uuid);
                if (playerentity instanceof AbstractClientPlayerEntity) {
                    ((AbstractClientPlayerEntity)playerentity).getSkin();
                }
            }
        }
    }

    public static void sendPosLookMovementPacketsPre() {
        if (!VoilClient.getInstance().disabled) {
            if (VoilClient.mc.player != null) {
                VoilClient.eventBus.postObject(HookPostEvent.create("onPlayerPositionLook"));
            }
        }
    }

    public static void sendChatMessage(String s, CallbackInfo callbackinfo) {
        if (!VoilClient.getInstance().disabled) {
            StringBuilder stringbuilder = new StringBuilder();

            for (int i = 1; i < s.toCharArray().length; i++) {
                stringbuilder.append(s.toCharArray()[i]);
            }

            String[] astring1 = stringbuilder.toString().split(" ");
            if (s.startsWith(VoilClient.getCommandPrefix())) {
                for (Command command : CommandManager.INSTANCE.getCommands()) {
                    if (astring1[0].equalsIgnoreCase(command.getName())
                        || command.getAliases().contains(command.getName())) {
                        command.run(s, astring1);
                        callbackinfo.cancel();
                        break;
                    }
                }
            }
        }
    }
}
