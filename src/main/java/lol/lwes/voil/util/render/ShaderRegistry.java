package lol.lwes.voil.util.render;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class ShaderRegistry {
    public static final List<ShaderEntry> SHADER_ENTRIES = new ArrayList<>();
    public static final List<ShaderProgramEntry> PROGRAM_ENTRIES = new ArrayList<>();

    static {
        SHADER_ENTRIES.add(new ShaderEntry("core/rectangle"));
        SHADER_ENTRIES.add(new ShaderEntry("core/variableglow"));
        SHADER_ENTRIES.add(new ShaderEntry("core/texture_rectangle"));
        SHADER_ENTRIES.add(new ShaderEntry("core/background"));
        SHADER_ENTRIES.add(new ShaderEntry("core/blur"));
        SHADER_ENTRIES.add(new ShaderEntry("core/glow"));
        SHADER_ENTRIES.add(new ShaderEntry("core/outline"));
        SHADER_ENTRIES.add(new ShaderEntry("core/corner"));
        SHADER_ENTRIES.add(new ShaderEntry("core/msdf_icon"));
        SHADER_ENTRIES.add(new ShaderEntry("core/msdf"));
    }

    public static void registerReloadListener() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new ShaderReloadListener());
    }
}
