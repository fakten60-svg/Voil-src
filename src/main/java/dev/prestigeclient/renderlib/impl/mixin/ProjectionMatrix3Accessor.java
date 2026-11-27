package dev.prestigeclient.renderlib.impl.mixin;

import net.minecraft.client.render.ProjectionMatrix3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ProjectionMatrix3.class)
public interface ProjectionMatrix3Accessor {
    @Invoker("getMatrix")
    Matrix4f callGetMatrix(int i, int j, float f);
}
