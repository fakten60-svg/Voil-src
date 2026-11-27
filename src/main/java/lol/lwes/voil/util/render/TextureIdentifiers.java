package lol.lwes.voil.util.render;

import java.lang.reflect.Field;
import java.util.Map.Entry;
import java.util.Map;
import lol.lwes.voil.VoilClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.ReloadableTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

public final class TextureIdentifiers {
    private static Field texturesField;
    private static boolean lookupFailed;

    private TextureIdentifiers() {
    }

    public static Identifier find(AbstractTexture texture) {
        if (texture == null) {
            return null;
        }

        if (texture instanceof ReloadableTexture reloadable) {
            return reloadable.getId();
        }

        Map<?, ?> textures = registeredTextures();
        if (textures != null) {
            for (Entry<?, ?> entry : textures.entrySet()) {
                if (entry.getValue() == texture && entry.getKey() instanceof Identifier id) {
                    return id;
                }
            }
        }

        return null;
    }

    private static Map<?, ?> registeredTextures() {
        TextureManager manager = VoilClient.mc.getTextureManager();
        if (manager == null || lookupFailed) {
            return null;
        }

        try {
            if (texturesField == null) {
                for (Field field : TextureManager.class.getDeclaredFields()) {
                    if (Map.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        texturesField = field;
                        break;
                    }
                }

                if (texturesField == null) {
                    lookupFailed = true;
                    return null;
                }
            }

            return (Map<?, ?>) texturesField.get(manager);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            lookupFailed = true;
            return null;
        }
    }
}
