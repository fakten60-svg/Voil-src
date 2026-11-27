package lol.lwes.voil.util.render;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ShaderReloadListener implements SimpleSynchronousResourceReloadListener {
    ShaderReloadListener() {
    }

    @Override
    public void reload(ResourceManager resourcemanager) {
        Render2DUtil.initRenderers();
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of("tulip", "shader_loader");
    }
}
