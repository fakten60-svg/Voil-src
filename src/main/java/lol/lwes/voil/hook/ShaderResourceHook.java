package lol.lwes.voil.hook;

import java.util.Map;
import java.util.Optional;
import lol.lwes.voil.util.render.ShaderEntry;
import lol.lwes.voil.util.render.ShaderProgramEntry;
import lol.lwes.voil.util.render.ShaderRegistry;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ShaderResourceHook {
    public static void onFindResources(ResourceManager resourcemanager, Map map) {
        for (ShaderEntry shaderentry : ShaderRegistry.SHADER_ENTRIES) {
            String s4 = shaderentry.getName();
            Identifier identifier = Identifier.of("tulip", "shaders/" + s4 + ".json");
            Optional optional = resourcemanager.getResource(identifier);
            if (optional.isPresent()) {
                map.put(identifier, (Resource)optional.get());
            }

            String s5 = shaderentry.getName();
            Identifier identifier1 = Identifier.of("tulip", "shaders/" + s5 + ".fsh");
            Optional optional1 = resourcemanager.getResource(identifier1);
            if (optional1.isPresent()) {
                map.put(identifier1, (Resource)optional1.get());
            }
        }

        for (ShaderProgramEntry shaderprogramentry : ShaderRegistry.PROGRAM_ENTRIES) {
            String s6 = shaderprogramentry.getName();
            Identifier identifier2 = Identifier.of("tulip", "shaders/program/" + s6 + ".json");
            Optional optional2 = resourcemanager.getResource(identifier2);
            if (optional2.isPresent()) {
                map.put(identifier2, (Resource)optional2.get());
            }

            String s7 = shaderprogramentry.getName();
            Identifier identifier3 = Identifier.of("tulip", "shaders/program/" + s7 + ".fsh");
            Optional optional3 = resourcemanager.getResource(identifier3);
            if (optional3.isPresent()) {
                map.put(identifier3, (Resource)optional3.get());
            }
        }
    }
}
