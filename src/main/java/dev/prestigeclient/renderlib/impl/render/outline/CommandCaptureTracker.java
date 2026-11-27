package dev.prestigeclient.renderlib.impl.render.outline;

import dev.prestigeclient.renderlib.api.entity.EntityOutlineHandler;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.state.EntityRenderState;

public class CommandCaptureTracker {
    private static final int MAX_TRACKED = 65536;
    private static EntityRenderState currentEntity;
    private static boolean currentOutlined;
    private static boolean handWindow;
    private static boolean handOutlined;
    private static final Map owners = new IdentityHashMap();
    private static final Set handCommands = Collections.newSetFromMap(new IdentityHashMap());

    private CommandCaptureTracker() {
    }

    public static void endEntity() {
        currentEntity = null;
        currentOutlined = false;
    }

    public static void clearFrame() {
        owners.clear();
        handCommands.clear();
        currentEntity = null;
        currentOutlined = false;
        handWindow = false;
        handOutlined = false;
    }

    public static void onCommand(Object object) {
        if (object != null) {
            if (handWindow) {
                if (handOutlined && handCommands.size() < 65536) {
                    handCommands.add(object);
                }
            } else if (currentOutlined && owners.size() < 65536) {
                owners.put(object, currentEntity);
            }
        }
    }

    public static boolean isHandCommand(Object object) {
        boolean flag = handCommands.contains(object);
        if (flag) {
            OutlineSeed.set(MinecraftClient.getInstance().player);
        }

        return flag;
    }

    public static void beginEntity(EntityRenderState entityrenderstate) {
        currentEntity = entityrenderstate;
        currentOutlined = entityrenderstate != null && EntityOutlineHandler.getActiveShader() != null && EntityOutlineHandler.shouldOutline(entityrenderstate);
    }

    public static void beginHand() {
        handWindow = true;
        handOutlined = EntityOutlineHandler.getActiveShader() != null && EntityOutlineHandler.isTargetEntity(MinecraftClient.getInstance().player);
    }

    public static void endHand() {
        handWindow = false;
        handOutlined = false;
    }

    public static EntityRenderState ownerOf(Object object) {
        EntityRenderState entityrenderstate = (EntityRenderState)owners.get(object);
        OutlineSeed.set(entityrenderstate);
        return entityrenderstate;
    }
}
