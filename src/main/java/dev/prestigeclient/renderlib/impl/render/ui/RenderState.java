package dev.prestigeclient.renderlib.impl.render.ui;

import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import org.joml.Vector4f;

public record RenderState(Vector4f scissor, RenderLayer layer) {
}
