package dev.prestigeclient.renderlib.impl.core;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlossomResources {
    private static final Logger log = LoggerFactory.getLogger(BlossomResources.class);
    private static List<String> index = List.of();
    private static Map<Identifier, Resource> cache = Map.of();

    public static List<String> getIndex() {
        return index;
    }

    public static Map<Identifier, Resource> getCache() {
        return cache;
    }

    public static void setIndex(List<String> list) {
        ArrayList<String> arraylist = new ArrayList<>(list.size());

        for (String s : list) {
            String s1 = s.trim();
            if (!s1.isEmpty()) {
                if (s1.startsWith("/")) {
                    s1 = s1.substring(1);
                }

                arraylist.add(s1);
            }
        }

        index = List.copyOf(arraylist);
        rebuildCache();
    }

    private static void rebuildCache() {
        ClassLoader classloader = MinecraftClient.class.getClassLoader();
        BlossomResourcePack blossomresourcepack = new BlossomResourcePack();
        HashMap<Identifier, Resource> hashmap = new HashMap<>();

        for (String s : index) {
            Identifier identifier;
            try {
                identifier = Identifier.of("blossom", s);
            } catch (RuntimeException runtimeexception) {
                continue;
            }

            String s1 = "blossom/" + s;
            hashmap.put(identifier, new Resource(blossomresourcepack, () -> {
                InputStream inputstream = classloader.getResourceAsStream(s1);
                if (inputstream == null) {
                    throw new FileNotFoundException("blossom resource not resolvable via classloader: " + s1);
                } else {
                    return inputstream;
                }
            }));
        }

        cache = Map.copyOf(hashmap);
    }
}
