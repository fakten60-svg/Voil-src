package lol.lwes.voil.util.render;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class TulipResources {
    private static List<String> index = List.of();
    private static Map<Identifier, Resource> cache = Map.of();

    public static List<String> getIndex() {
        return index;
    }

    public static Map<Identifier, Resource> getCache() {
        return cache;
    }

    public static void setIndex(List<String> list) {
        List<String> arraylist = new ArrayList<>();

        for (String s : list) {
            s = s.trim();
            if (!s.isEmpty()) {
                arraylist.add(s);
            }
        }

        index = List.copyOf(arraylist);
        rebuildCache();
    }

    private static void rebuildCache() {
        ClassLoader classloader = MinecraftClient.class.getClassLoader();
        TulipResourcePack tulipresourcepack = new TulipResourcePack();
        Map<Identifier, Resource> hashmap = new HashMap<>();

        for (String s : index) {
            Identifier identifier = Identifier.of("tulip", s);
            hashmap.put(identifier, new Resource(tulipresourcepack, () -> {
                InputStream inputstream = classloader.getResourceAsStream("assets/tulip/" + s);
                if (inputstream == null) {
                    throw new FileNotFoundException("assets/tulip/" + s);
                } else {
                    return inputstream;
                }
            }));
        }

        cache = Map.copyOf(hashmap);
    }
}
