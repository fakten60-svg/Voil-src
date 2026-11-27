package dev.prestigeclient.renderlib.impl.core;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BlossomResourcePack implements ResourcePack {
    public static final String NAMESPACE = "blossom";
    private static final String ROOT = NAMESPACE + "/";
    private final ClassLoader classLoader;
    private final ResourcePackInfo info;

    public BlossomResourcePack() {
        this(MinecraftClient.class.getClassLoader());
    }

    public BlossomResourcePack(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.info = new ResourcePackInfo(NAMESPACE, Text.literal("Blossom"), ResourcePackSource.BUILTIN, Optional.empty());
    }

    @Override
    public InputSupplier<InputStream> openRoot(String... segments) {
        return this.supplierFor(ROOT + String.join("/", segments));
    }

    @Override
    public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
        if (!NAMESPACE.equals(id.getNamespace())) {
            return null;
        }

        return this.supplierFor(ROOT + id.getPath());
    }

    @Override
    public void findResources(ResourceType type, String namespace, String prefix, ResourcePack.ResultConsumer consumer) {
        if (!NAMESPACE.equals(namespace)) {
            return;
        }

        String directory = prefix.endsWith("/") ? prefix : prefix + "/";
        List<String> index = BlossomResources.getIndex();

        for (String path : index) {
            if (!path.startsWith(directory)) {
                continue;
            }

            Identifier id;
            try {
                id = Identifier.of(NAMESPACE, path);
            } catch (RuntimeException exception) {
                continue;
            }

            String location = ROOT + path;
            consumer.accept(id, () -> this.openStream(location));
        }
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return Set.of(NAMESPACE);
    }

    @Override
    public <T> T parseMetadata(ResourceMetadataSerializer<T> serializer) {
        return null;
    }

    @Override
    public ResourcePackInfo getInfo() {
        return this.info;
    }

    @Override
    public void close() {
    }

    private InputSupplier<InputStream> supplierFor(String location) {
        if (this.classLoader.getResource(location) == null) {
            return null;
        }

        return () -> this.openStream(location);
    }

    private InputStream openStream(String location) throws FileNotFoundException {
        InputStream stream = this.classLoader.getResourceAsStream(location);
        if (stream == null) {
            throw new FileNotFoundException("blossom resource not resolvable via classloader: " + location);
        }

        return stream;
    }
}
