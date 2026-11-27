package dev.prestigeclient.renderlib.impl;

import dev.prestigeclient.renderlib.api.entity.EntityOutlineHandler;
import dev.prestigeclient.renderlib.api.entity.outline.NormalOutlineShader;
import dev.prestigeclient.renderlib.api.shader.RLEntityOutlineShader;
import dev.prestigeclient.renderlib.api.texture.BlurredRoundedShader;
import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.example.UIExamples;
import dev.prestigeclient.renderlib.example.shader.RoundedRectShader;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.RenderPrimitives;
import dev.prestigeclient.renderlib.impl.render.blur.BlurState;
import dev.prestigeclient.renderlib.impl.render.events.RenderEvents;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import java.awt.Color;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class Main implements ModInitializer {
    private static BlurState blurState;
    private static BlurredRoundedShader brr;
    private static RoundedRectShader rrs;

    @Override
    public void onInitialize() {
        initializeRenderAPI();
    }

    public static void initializeRenderAPI() {
        RenderAPI.setRenderThread(Thread.currentThread());
    }

    public static void initializeTestingFunctions() {
        RenderEvents.HUD_RENDER.register((buffercollector, renderapi$rendercontext) -> UIExamples.draw(renderapi$rendercontext));
        AtomicBoolean atomicboolean = new AtomicBoolean();
        RenderEvents.RENDER_POST.register((buffercollector, rendercontext) -> {
            RLEntityOutlineShader rlentityoutlineshader = EntityOutlineHandler.getActiveShader();
            if (rlentityoutlineshader == null) {
                if (!atomicboolean.get()) {
                    atomicboolean.set(true);
                    EntityOutlineHandler.setShader(EntityOutlineHandler.normalOutlineShader);
                    EntityOutlineHandler.setEntityFilter(entity -> entity instanceof AnimalEntity || entity instanceof PlayerEntity);
                }
            } else {
                if (rlentityoutlineshader instanceof NormalOutlineShader) {
                    rlentityoutlineshader.setUniformOverride(rluniforms -> rluniforms.setColor("color", new Color(178, 255, 254)));
                }
            }
        });
        RenderEvents.WORLD_RENDER
            .register(
                (buffercollector, renderapi$rendercontext) -> {
                    ClientPlayerEntity clientplayerentity = MinecraftClient.getInstance().player;
                    Vec3d vec3d = clientplayerentity.getLerpedPos(MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false));
                    RenderPrimitives.circleOutline3D(
                        renderapi$rendercontext,
                        buffercollector,
                        new Matrix4f(),
                        (float)(vec3d.getX() - 2.2),
                        (float)(vec3d.getY() + 0.0),
                        (float)(vec3d.getZ() - 0.2),
                        1.0F,
                        0.05F,
                        new Color(255, 0, 255, 100).getRGB()
                    );
                }
            );
    }
}
