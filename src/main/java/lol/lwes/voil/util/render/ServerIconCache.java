package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;

/**
 * Offline-only server icon cache.
 *
 * <p>The client never resolves or leaks the address of a server to a third-party service, so no
 * icon is fetched. The API is kept so the UI falls back to its placeholder tile.</p>
 */
public final class ServerIconCache {
    private ServerIconCache() {
    }

    public static GlTexture getIcon(String address) {
        return null;
    }

    public static void invalidate(String address) {
    }

    public static void clear() {
    }
}
