package lol.lwes.voil.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.resource.GlTexture;
import it.unimi.dsi.fastutil.Pair;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.ConfigManager;
import lol.lwes.voil.util.target.TargetSortMode;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public class PlayerTargetUtil {
    private static final Map<PlayerEntity, Set<PlayerActivity>> botFlags;
    public static final Set groundedPlayers;
    private static final Set<UUID> ignoredUuids;
    private static final Set<UUID> markedUuids;
    private static PlayerEntity currentTarget;
    private static final Map<Integer, GlTexture> textureCache;
    private static final Map<Identifier, GlTexture> loadedTextures;
    private static final Set<Identifier> missingTextures;
    private static Set<UUID> friendCache;
    public static Map skinColors;
    public static Pair defaultSkinTextures;

    static {
        botFlags = new ConcurrentHashMap<>();
        groundedPlayers = new HashSet();
        ignoredUuids = new HashSet<>();
        markedUuids = new HashSet<>();
        textureCache = new ConcurrentHashMap<>();
        loadedTextures = new HashMap<>();
        missingTextures = new HashSet<>();
        BufferedImage bufferedimage = readDefaultSkin();
        if (bufferedimage != null) {
            RenderAPI.schedule(() -> {
                BiFunction<Integer, Integer, Integer> bifunction = bufferedimage::getRGB;
                defaultSkinTextures = createSkinColors(bifunction);
            });
        }

        skinColors = new HashMap();
    }

    private static BufferedImage readDefaultSkin() {
        try (InputStream inputstream = PlayerTargetUtil.class.getClassLoader().getResourceAsStream("assets/tulip/skins/steve.png")) {
            if (inputstream != null) {
                return ImageIO.read(inputstream);
            }
        } catch (IOException ioexception) {
        }

        return null;
    }

    public static void clearIgnored() {
        ignoredUuids.clear();
    }

    public static PlayerEntity getCurrentTarget() {
        return currentTarget;
    }

    public static void clearCurrentTarget() {
        currentTarget = null;
    }

    public static List findAllTargets(TargetFilter[] atargetfilter) {
        return VoilClient.mc.world != null && VoilClient.mc.player != null
            ? VoilClient.mc
                .world
                .getPlayers()
                .stream()
                .filter(PlayerTargetUtil::isAlivePlayer)
                .filter(abstractclientplayerentity -> isValidTarget(abstractclientplayerentity, atargetfilter))
                .sorted(Comparator.comparingDouble(abstractclientplayerentity -> VoilClient.mc.player.squaredDistanceTo(abstractclientplayerentity)))
                .collect(Collectors.toList())
            : Collections.emptyList();
    }

    public static boolean isValidTarget(PlayerEntity playerentity, TargetFilter[] atargetfilter) {
        for (TargetFilter targetfilter : atargetfilter) {
            if (!targetfilter.matches(playerentity)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidTargetEntity(Entity entity) {
        return VoilClient.mc.player != null && VoilClient.mc.player.canSee(entity);
    }

    public static GlTexture createTexture(byte[] abyte) {
        int i = Arrays.hashCode(abyte);
        GlTexture gltexture = textureCache.get(i);
        if (gltexture != null) {
            return gltexture;
        }

        try {
            BufferedImage bufferedimage = ImageIO.read(new ByteArrayInputStream(abyte));
            if (bufferedimage == null) {
                return null;
            }

            GlTexture gltexture1 = createTextureFromBuffer(imageToBuffer(bufferedimage), bufferedimage.getWidth(), bufferedimage.getHeight());
            textureCache.put(i, gltexture1);
            return gltexture1;
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public static GlTexture loadTexture(Identifier identifier) {
        GlTexture gltexture = loadedTextures.get(identifier);
        if (gltexture != null) {
            return gltexture;
        }

        if (missingTextures.contains(identifier)) {
            return null;
        }

        Optional<Resource> optional = VoilClient.mc.getResourceManager().getResource(identifier);
        if (optional.isEmpty()) {
            missingTextures.add(identifier);
            return null;
        }

        try (
            InputStream inputstream = optional.get().getInputStream();
            NativeImage nativeimage = NativeImage.read(inputstream);
        ) {
            int k = nativeimage.getWidth();
            int l = nativeimage.getHeight();
            ByteBuffer bytebuffer = BufferUtils.createByteBuffer(k * l * 4);

            for (int i1 = 0; i1 < l; i1++) {
                for (int j1 = 0; j1 < k; j1++) {
                    int k1 = nativeimage.getColorArgb(j1, i1);
                    bytebuffer.put((byte)(k1 >> 16 & 0xFF));
                    bytebuffer.put((byte)(k1 >> 8 & 0xFF));
                    bytebuffer.put((byte)(k1 & 0xFF));
                    bytebuffer.put((byte)(k1 >> 24 & 0xFF));
                }
            }

            bytebuffer.flip();
            GlTexture gltexture1 = GlTexture.create(3553).withoutMipmaps();
            gltexture1.init(32856, k, l);
            GL15.glBindBuffer(35052, 0);
            GL11.glPixelStorei(3314, 0);
            GL11.glPixelStorei(3316, 0);
            GL11.glPixelStorei(3315, 0);
            GL11.glPixelStorei(3317, 4);
            gltexture1.setData(0, 0, k, l, 6408, 5121, bytebuffer);
            gltexture1.setFilter(9728);
            gltexture1.setEdgeMode(33071);
            loadedTextures.put(identifier, gltexture1);
            return gltexture1;
        } catch (Exception exception) {
            missingTextures.add(identifier);
            return null;
        }
    }

    public static Pair createSkinColors(BiFunction<Integer, Integer, Integer> bifunction) {
        BufferedImage bufferedimage = new BufferedImage(8, 8, 2);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                bufferedimage.setRGB(i, j, (Integer)bifunction.apply(8 + i, 8 + j));
            }
        }

        int[] aint = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), null, 0, bufferedimage.getWidth());
        ByteBuffer bytebuffer = BufferUtils.createByteBuffer(256);

        for (int k : aint) {
            int j1 = k >> 16;
            bytebuffer.put((byte)(j1 & 0xFF));
            bytebuffer.put((byte)(k >> 8 & 0xFF));
            bytebuffer.put((byte)(k & 0xFF));
            j1 = k >> 24;
            bytebuffer.put((byte)(j1 & 0xFF));
        }

        bytebuffer.flip();
        BufferedImage bufferedimage1 = new BufferedImage(8, 8, 2);

        for (int k1 = 0; k1 < 8; k1++) {
            for (int l = 0; l < 8; l++) {
                bufferedimage1.setRGB(k1, l, (Integer)bifunction.apply(40 + k1, 8 + l));
            }
        }

        int[] aint1 = bufferedimage1.getRGB(0, 0, bufferedimage1.getWidth(), bufferedimage1.getHeight(), null, 0, bufferedimage1.getWidth());
        ByteBuffer bytebuffer1 = BufferUtils.createByteBuffer(256);

        for (int i1 : aint1) {
            int l1 = i1 >> 16;
            bytebuffer1.put((byte)(l1 & 0xFF));
            l1 = i1 >> 8;
            bytebuffer1.put((byte)(l1 & 0xFF));
            bytebuffer1.put((byte)(i1 & 0xFF));
            l1 = i1 >> 24;
            bytebuffer1.put((byte)(l1 & 0xFF));
        }

        bytebuffer1.flip();
        GlTexture gltexture = GlTexture.create(3553).withoutMipmaps();
        GlTexture gltexture1 = GlTexture.create(3553).withoutMipmaps();
        gltexture.init(32856, 8, 8);
        gltexture1.init(32856, 8, 8);
        uploadTexture(gltexture, 8, 8, bytebuffer);
        uploadTexture(gltexture1, 8, 8, bytebuffer1);
        return Pair.of(gltexture, gltexture1);
    }

    public static boolean isAlivePlayer(PlayerEntity playerentity) {
        return VoilClient.mc.player == null || playerentity == null
            ? false
            : playerentity != VoilClient.mc.player && !playerentity.isRemoved() && playerentity.isAlive() && !playerentity.isSpectator();
    }

    public static Set<UUID> getIgnoredUuids() {
        return ignoredUuids;
    }

    public static void removeBotTracking(PlayerEntity playerentity) {
        botFlags.remove(playerentity);
    }

    private static void uploadTexture(GlTexture gltexture, int i, int j, ByteBuffer bytebuffer) {
        GL15.glBindBuffer(35052, 0);
        GL11.glPixelStorei(3314, 0);
        GL11.glPixelStorei(3316, 0);
        GL11.glPixelStorei(3315, 0);
        GL11.glPixelStorei(3317, 4);
        gltexture.setData(0, 0, i, j, 6408, 5121, bytebuffer);
        gltexture.setFilter(9728);
        gltexture.setEdgeMode(33071);
    }

    public static boolean isFriend(UUID uuid) {
        return getFriendSet().contains(uuid);
    }

    public static GlTexture createTextureFromImage(BufferedImage bufferedimage) {
        int i = bufferedimage.getWidth();
        int j = bufferedimage.getHeight();
        BufferedImage bufferedimage1 = new BufferedImage(i, j, 2);
        Graphics2D graphics2d = bufferedimage1.createGraphics();
        graphics2d.drawImage(bufferedimage, 0, 0, null);
        graphics2d.dispose();
        int[] aint = bufferedimage1.getRGB(0, 0, i, j, null, 0, i);
        ByteBuffer bytebuffer = BufferUtils.createByteBuffer(i * j * 4);

        for (int k : aint) {
            int l = k >> 16;
            bytebuffer.put((byte)(l & 0xFF));
            l = k >> 8;
            bytebuffer.put((byte)(l & 0xFF));
            bytebuffer.put((byte)(k & 0xFF));
            l = k >> 24;
            bytebuffer.put((byte)(l & 0xFF));
        }

        bytebuffer.flip();
        GlTexture gltexture = GlTexture.create(3553);
        gltexture.init(32856, i, j);
        GL11.glBindTexture(3553, gltexture.tex);
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexParameteri(3553, 10242, 33071);
        GL11.glTexParameteri(3553, 10243, 33071);
        GL11.glPixelStorei(3314, 0);
        GL11.glPixelStorei(3315, 0);
        GL11.glPixelStorei(3316, 0);
        GL11.glPixelStorei(3317, 1);
        GL11.glTexSubImage2D(3553, 0, 0, 0, i, j, 6408, 5121, bytebuffer);
        GL11.glBindTexture(3553, 0);
        return gltexture;
    }

    public static GlTexture createTextureFromBuffer(ByteBuffer bytebuffer, int i, int j) {
        GlTexture gltexture = GlTexture.create(3553);
        gltexture.init(32856, i, j);
        GL15.glBindBuffer(35052, 0);
        GL11.glPixelStorei(3314, 0);
        GL11.glPixelStorei(3316, 0);
        GL11.glPixelStorei(3315, 0);
        GL11.glPixelStorei(3317, 4);
        GL11.glBindTexture(3553, gltexture.tex);
        gltexture.setData(0, 0, i, j, 6408, 5121, bytebuffer);
        GL30.glGenerateMipmap(3553);
        GL11.glTexParameteri(3553, 10242, 33071);
        GL11.glTexParameteri(3553, 10243, 33071);
        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
        GL11.glBindTexture(3553, 0);
        return gltexture;
    }

    private static void saveFriends() {
        JsonArray jsonarray = new JsonArray();

        for (UUID uuid : getFriendSet()) {
            jsonarray.add(uuid.toString());
        }

        ConfigManager.setValue("friends", jsonarray);
    }

    public static GlTexture textureFromBase64(String s) {
        int i = s.hashCode();
        GlTexture gltexture = textureCache.get(i);
        if (gltexture != null) {
            return gltexture;
        }

        GlTexture gltexture1 = createTexture(Base64.getDecoder().decode(s));
        if (gltexture1 != null) {
            textureCache.put(i, gltexture1);
        }

        return gltexture1;
    }

    public static void registerSkinImage(Identifier identifier, NativeImage nativeimage) {
        Map map = skinColors;
        BiFunction<Integer, Integer, Integer> bifunction = nativeimage::getColorArgb;
        map.put(identifier, createSkinColors(bifunction));
    }

    public static Set<UUID> getFriends() {
        return Collections.unmodifiableSet(getFriendSet());
    }

    public static void addFriend(UUID uuid) {
        getFriendSet().add(uuid);
        saveFriends();
    }

    public static void removeFriend(UUID uuid) {
        getFriendSet().remove(uuid);
        saveFriends();
    }

    private static Set<UUID> loadFriends() {
        HashSet<UUID> hashset = new HashSet<>();
        JsonElement jsonelement = ConfigManager.getElement("friends");
        if (jsonelement != null && jsonelement.isJsonArray()) {
            for (JsonElement jsonelement1 : jsonelement.getAsJsonArray()) {
                try {
                    hashset.add(UUID.fromString(jsonelement1.getAsString()));
                } catch (Exception exception) {
                }
            }

            return hashset;
        } else {
            return hashset;
        }
    }

    public static Set getTrackedPlayers() {
        return botFlags.keySet();
    }

    public static ByteBuffer imageToBuffer(BufferedImage bufferedimage) {
        int i = bufferedimage.getWidth();
        int j = bufferedimage.getHeight();
        int j1 = i * j;
        ByteBuffer bytebuffer = BufferUtils.createByteBuffer(j1 * 4);

        for (int k = 0; k < j; k++) {
            for (int l = 0; l < i; l++) {
                int i1 = bufferedimage.getRGB(l, k);
                j1 = i1 >> 16;
                bytebuffer.put((byte)(j1 & 0xFF));
                j1 = i1 >> 8;
                bytebuffer.put((byte)(j1 & 0xFF));
                bytebuffer.put((byte)(i1 & 0xFF));
                j1 = i1 >> 24;
                bytebuffer.put((byte)(j1 & 0xFF));
            }
        }

        bytebuffer.flip();
        return bytebuffer;
    }

    public static void addBotFlag(PlayerEntity playerentity, PlayerActivity playeractivity) {
        botFlags.computeIfPresent(playerentity, (playerentity1, set) -> {
            set.add(playeractivity);
            return set;
        });
    }

    private static Set<UUID> getFriendSet() {
        if (friendCache == null) {
            friendCache = loadFriends();
        }

        return friendCache;
    }

    public static void clearFriendCache() {
        friendCache = null;
    }

    public static void removeIgnored(UUID uuid) {
        ignoredUuids.remove(uuid);
    }

    public static boolean hasMovedFlag(PlayerEntity playerentity) {
        Set<PlayerActivity> set = botFlags.getOrDefault(playerentity, Collections.emptySet());
        return set.contains(PlayerActivity.MOVED)
            && set.contains(PlayerActivity.TOUCHED_GROUND)
            && set.contains(PlayerActivity.ROTATED)
            && set.contains(PlayerActivity.PING_ISNT_0)
            && set.contains(PlayerActivity.SWINGED);
    }

    public static boolean isTracked(PlayerEntity playerentity) {
        return botFlags.containsKey(playerentity);
    }

    public static void trackPlayer(PlayerEntity playerentity) {
        botFlags.putIfAbsent(playerentity, new HashSet<>());
    }

    public static float getHealthWithAbsorption(PlayerEntity playerentity) {
        return playerentity.getHealth() + playerentity.getAbsorptionAmount();
    }

    public static void addIgnored(UUID uuid) {
        ignoredUuids.add(uuid);
    }

    public static PlayerEntity findClosestTarget(TargetFilter[] atargetfilter) {
        List list = findAllTargets(atargetfilter);
        currentTarget = list.isEmpty() ? null : (PlayerEntity)list.get(0);
        return currentTarget;
    }

    public static boolean isIgnored(UUID uuid) {
        return ignoredUuids.contains(uuid);
    }

    public static boolean isMoving(Entity entity) {
        return entity.getX() != entity.lastX || entity.getZ() != entity.lastZ;
    }

    public static Set<UUID> getMarkedUuids() {
        return markedUuids;
    }

    public static void addMarked(UUID uuid) {
        markedUuids.add(uuid);
    }

    public static boolean isMarked(UUID uuid) {
        return markedUuids.contains(uuid);
    }

    public static void sortPlayers(List list, TargetSortMode targetsortmode) {
        int i = targetsortmode.ordinal();
        Comparator<PlayerEntity> comparator;
        if (i != 0) {
            if (i != 1) {
                if (i != 2) {
                    if (i != 3) {
                        throw new MatchException(null, null);
                    }

                    comparator = Comparator.comparingInt(playerentity -> {
                        HitResult hitresult = VoilClient.mc.crosshairTarget;
                        return hitresult instanceof EntityHitResult && ((EntityHitResult)hitresult).getEntity() == playerentity ? 0 : 1;
                    });
                } else {
                    comparator = Comparator.comparingDouble(PlayerTargetUtil::getHealthWithAbsorption).reversed();
                }
            } else {
                comparator = Comparator.comparingDouble(PlayerTargetUtil::getHealthWithAbsorption);
            }
        } else {
            comparator = Comparator.comparingDouble(playerentity -> VoilClient.mc.player.squaredDistanceTo(playerentity));
        }

        list.sort(comparator);
    }

    public static void removeMarked(UUID uuid) {
        markedUuids.remove(uuid);
    }

    public static void clearMarked() {
        markedUuids.clear();
    }

    public static double getDistanceToPlayer(PlayerEntity playerentity) {
        return VoilClient.mc.player == null ? Double.MAX_VALUE : VoilClient.mc.player.squaredDistanceTo(playerentity);
    }

    public static PlayerEntity findTarget(TargetSortMode targetsortmode, TargetFilter[] atargetfilter) {
        List list = findAllTargets(atargetfilter);
        sortPlayers(list, targetsortmode);
        currentTarget = list.isEmpty() ? null : (PlayerEntity)list.get(0);
        return currentTarget;
    }
}
