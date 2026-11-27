package dev.prestigeclient.renderlib.api.entity;

import dev.prestigeclient.renderlib.api.entity.outline.NormalOutlineShader;
import dev.prestigeclient.renderlib.api.entity.outline.RainbowOutlineShader;
import dev.prestigeclient.renderlib.api.shader.RLEntityOutlineShader;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.events.RenderEvents;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import dev.prestigeclient.renderlib.impl.render.mesh.GlBufferMeshBuilder;
import dev.prestigeclient.renderlib.impl.render.outline.CommandCaptureTracker;
import dev.prestigeclient.renderlib.impl.render.outline.OutlineUtils;
import it.unimi.dsi.fastutil.Pair;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;

public class EntityOutlineHandler {
    public static final RLEntityOutlineShader rainbowOutlineShader = new RainbowOutlineShader();
    public static final RLEntityOutlineShader normalOutlineShader = new NormalOutlineShader();
    public static final BufferCollector outlineBufferCollector = new BufferCollector();
    private static RLEntityOutlineShader activeShader;
    private static boolean enabled = true;
    private static final Map shaders = new LinkedHashMap();
    private static Predicate<Entity> entityFilter = entity -> true;
    private static final WeakHashMap stateToEntity = new WeakHashMap();
    private static final Set outlineStates = Collections.newSetFromMap(new WeakHashMap());
    private static boolean capturedThisFrame;

    static {
        shaders.put("rainbow", rainbowOutlineShader);
        shaders.put("normal", normalOutlineShader);
        RenderEvents.RENDER_POST.register((buffercollector, renderapi$rendercontext) -> {
            if (enabled && activeShader != null) {
                activeShader.post(buffercollector, renderapi$rendercontext);
                clearFrame();
            }
        });
        RenderEvents.WORLD_RENDER.register((buffercollector, renderapi$rendercontext) -> {
            if (enabled && activeShader != null) {
                activeShader.resizeOrInit();
                RLEntityOutlineShader rlentityoutlineshader = activeShader;
                if (rlentityoutlineshader instanceof RLEntityOutlineShader) {
                    rlentityoutlineshader.theFb.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
                }

                noteCaptures();
                outlineBufferCollector.draw(renderapi$rendercontext);
            }
        });
        RenderEvents.AFTER_WORLD.register((buffercollector, renderapi$rendercontext) -> {
            if (enabled && activeShader != null) {
                activeShader.resizeOrInit();
                noteCaptures();
                outlineBufferCollector.draw(renderapi$rendercontext);
            }
        });
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    public static boolean setShader(String s) {
        RLEntityOutlineShader rlentityoutlineshader = (RLEntityOutlineShader)shaders.get(s);
        if (rlentityoutlineshader != null) {
            activeShader = rlentityoutlineshader;
            return true;
        } else {
            return false;
        }
    }

    public static void setShader(RLEntityOutlineShader rlentityoutlineshader) {
        activeShader = rlentityoutlineshader;
    }

    public static RLEntityOutlineShader getShader(String s) {
        return (RLEntityOutlineShader)shaders.get(s);
    }

    public static Entity entityOf(EntityRenderState entityrenderstate) {
        return (Entity)stateToEntity.get(entityrenderstate);
    }

    public static void clearFrame() {
        outlineStates.clear();
        OutlineUtils.clearFrameCache();
        capturedThisFrame = false;
        CommandCaptureTracker.clearFrame();
    }

    public static void registerShader(String s, RLEntityOutlineShader rlentityoutlineshader) {
        shaders.put(s, rlentityoutlineshader);
    }

    public static void unregisterShader(String s) {
        shaders.remove(s);
    }

    private static void noteCaptures() {
        if (!capturedThisFrame) {
            capturedThisFrame = outlineBufferCollector.getAllActiveBuffers()
                .anyMatch(pair -> pair.value() != null && pair.value().countCompleteVertices() > 0);
        }
    }

    public static Set getShaderNames() {
        return Collections.unmodifiableSet(shaders.keySet());
    }

    public static boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    public static void setEnabled(boolean flag) {
        enabled = flag;
    }

    public static void setEntityFilter(Predicate<Entity> predicate) {
        entityFilter = predicate;
    }

    public static boolean shouldOutline(EntityRenderState entityrenderstate) {
        return enabled && entityrenderstate != null ? outlineStates.contains(entityrenderstate) : false;
    }

    public static void trackEntity(EntityRenderState entityrenderstate, Entity entity) {
        if (entityrenderstate != null && entity != null && getActiveShader() != null) {
            if (entityFilter.test(entity)) {
                outlineStates.add(entityrenderstate);
                stateToEntity.put(entityrenderstate, entity);
            }
        }
    }

    public static boolean hadCapturesThisFrame() {
        return capturedThisFrame;
    }

    public static boolean isTargetEntity(Entity entity) {
        return enabled && entity != null ? entityFilter.test(entity) : false;
    }

    public static RLEntityOutlineShader getActiveShader() {
        return enabled ? activeShader : null;
    }
}
