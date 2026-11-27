package lol.lwes.voil.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public final class ModUnloader {
    private ModUnloader() {
    }

    public static void unload(String modId) {
        FabricLoader loader = FabricLoader.getInstance();
        Optional<ModContainer> container = loader.getModContainer(modId);
        if (container.isEmpty()) {
            return;
        }

        ModContainer mod = container.get();
        for (Class<?> type = loader.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    Object value = field.get(loader);
                    if (value instanceof List<?> list) {
                        removeFromList(loader, field, list, mod);
                    } else if (value instanceof Map<?, ?> map) {
                        removeFromMap(loader, field, map, modId, mod);
                    }
                } catch (ReflectiveOperationException | RuntimeException ignored) {
                }
            }
        }
    }

    private static void removeFromList(Object owner, Field field, List<?> list, ModContainer mod) throws IllegalAccessException {
        if (!list.contains(mod)) {
            return;
        }

        try {
            list.remove(mod);
        } catch (UnsupportedOperationException e) {
            List<Object> copy = new ArrayList<>(list);
            copy.remove(mod);
            field.set(owner, copy);
        }
    }

    private static void removeFromMap(Object owner, Field field, Map<?, ?> map, String modId, ModContainer mod) throws IllegalAccessException {
        boolean keyed = map.get(modId) == mod;
        boolean valued = map.containsValue(mod);
        if (!keyed && !valued) {
            return;
        }

        try {
            map.remove(modId);
            Collection<?> values = map.values();
            values.removeIf(value -> value == mod);
        } catch (UnsupportedOperationException e) {
            Map<Object, Object> copy = new LinkedHashMap<>(map);
            copy.remove(modId);
            copy.values().removeIf(value -> value == mod);
            field.set(owner, copy);
        }
    }
}
