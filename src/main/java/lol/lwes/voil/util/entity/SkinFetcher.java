package lol.lwes.voil.util.entity;

import com.mojang.authlib.GameProfile;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lol.lwes.voil.VoilClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.Util;

public final class SkinFetcher {
    private static final Map<String, CompletableFuture<Optional<SkinTextures>>> CACHE = new ConcurrentHashMap<>();

    private SkinFetcher() {
    }

    public static SkinTextures getSkinTextures(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String playerName = name.trim();
        CompletableFuture<Optional<SkinTextures>> future = CACHE.computeIfAbsent(playerName.toLowerCase(Locale.ROOT), key -> fetch(playerName));
        return future.getNow(Optional.empty()).orElse(null);
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static CompletableFuture<Optional<SkinTextures>> fetch(String name) {
        MinecraftClient mc = VoilClient.mc;
        return CompletableFuture.supplyAsync(() -> resolveProfile(mc, name), Util.getDownloadWorkerExecutor())
            .thenCompose(profile -> profile.isPresent() ? mc.getSkinProvider().fetchSkinTextures(profile.get()) : CompletableFuture.completedFuture(Optional.<SkinTextures>empty()))
            .exceptionally(throwable -> Optional.empty());
    }

    /**
     * Uses only the profile the current server already sent for that player. No profile lookup is
     * sent to Mojang, so renaming a player in the UI cannot trigger an external request.
     */
    private static Optional<GameProfile> resolveProfile(MinecraftClient mc, String name) {
        if (mc == null || mc.getNetworkHandler() == null) {
            return Optional.empty();
        }

        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            GameProfile profile = entry.getProfile();
            if (profile == null || !name.equalsIgnoreCase(profile.name())) {
                continue;
            }
            // Without signed textures the skin provider would ask Mojang for the profile, so skip it.
            return profile.properties().containsKey("textures") ? Optional.of(profile) : Optional.empty();
        }

        return Optional.empty();
    }
}
