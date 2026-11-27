package dev.prestigeclient.renderlib.impl.core;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public final class BlossomResourceLookup {
    private static final BlossomResourcePack PACK = new BlossomResourcePack();

    private BlossomResourceLookup() {
    }

    public static Optional<Resource> resolve(Identifier id) {
        if (id == null || !BlossomResourcePack.NAMESPACE.equals(id.getNamespace())) {
            return Optional.empty();
        }

        Map<Identifier, Resource> cache = BlossomResources.getCache();
        Resource cached = cache.get(id);
        if (cached != null) {
            return Optional.of(cached);
        }

        InputSupplier<InputStream> supplier = PACK.open(ResourceType.CLIENT_RESOURCES, id);
        return supplier == null ? Optional.empty() : Optional.of(new Resource(PACK, supplier));
    }
}
