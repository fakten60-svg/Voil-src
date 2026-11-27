package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.ClientPlayNetworkHandlerHooks;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onPlayerPositionLook", at = @At("RETURN"))
    private void sendPosLookMovementPacketsPre(CallbackInfo callbackinfo) {
        ClientPlayNetworkHandlerHooks.sendPosLookMovementPacketsPre();
    }

    @Inject(method = "onPlayerPositionLook", at = @At("RETURN"))
    private void sendPosLookMovementPacketsPost(CallbackInfo callbackinfo) {
        ClientPlayNetworkHandlerHooks.sendPosLookMovementPacketsPost();
    }

    @Inject(method = "onChunkData", at = @At("RETURN"))
    private void onChunkData(ChunkDataS2CPacket chunkdatas2cpacket, CallbackInfo callbackinfo) {
        ClientPlayNetworkHandlerHooks.onChunkData(chunkdatas2cpacket);
    }

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    public void onJoin(GameJoinS2CPacket gamejoins2cpacket, CallbackInfo callbackinfo) {
        ClientPlayNetworkHandlerHooks.onJoin();
    }

    @Inject(method = "onCommandSuggestions", at = @At("RETURN"))
    public void onCmdSuggest(CommandSuggestionsS2CPacket commandsuggestionss2cpacket, CallbackInfo callbackinfo) {
        ClientPlayNetworkHandlerHooks.onCommandSuggestions(commandsuggestionss2cpacket);
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(String s, CallbackInfo callbackinfo) {
        ClientPlayNetworkHandlerHooks.sendChatMessage(s, callbackinfo);
    }

    @Inject(method = "onPlayerList", at = @At("RETURN"))
    private void onPlayerList(PlayerListS2CPacket playerlists2cpacket, CallbackInfo callbackinfo) {
        ClientPlayNetworkHandlerHooks.onPlayerList(playerlists2cpacket);
    }
}
