package dev.prestigeclient.renderlib.impl.render.outline;

import dev.prestigeclient.renderlib.api.entity.EntityOutlineHandler;
import dev.prestigeclient.renderlib.impl.render.Util;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;

public final class OutlineSeed {
    public static final int DEFAULT_COLOR = 0xFFFFFFFF;
    private static int color = DEFAULT_COLOR;
    private static Entity entity;

    private OutlineSeed() {
    }

    public static void set(EntityRenderState state) {
        if (state == null) {
            reset();
            return;
        }

        Entity owner = EntityOutlineHandler.entityOf(state);
        if (owner != null) {
            set(owner);
            return;
        }

        entity = null;
        color = state.hasOutline() ? 0xFF000000 | state.outlineColor : DEFAULT_COLOR;
    }

    public static void set(Entity target) {
        if (target == null) {
            reset();
            return;
        }

        entity = target;
        color = 0xFF000000 | target.getTeamColorValue();
    }

    public static void reset() {
        entity = null;
        color = DEFAULT_COLOR;
    }

    public static int color() {
        return color;
    }

    public static int vertexColor() {
        return Util.toVertexColor(color);
    }

    public static Entity entity() {
        return entity;
    }
}
