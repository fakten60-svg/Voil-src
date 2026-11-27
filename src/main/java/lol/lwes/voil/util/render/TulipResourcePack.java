package lol.lwes.voil.util.render;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack.ResultConsumer;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.Identifier;

public class TulipResourcePack implements ResourcePack {
    @Override
    public void close() {
    }

    @Override
    public Set getNamespaces(ResourceType resourcetype) {
        return Collections.singleton("tulip");
    }

    @Override
    public void findResources(ResourceType resourcetype, String s, String s1, ResultConsumer resultconsumer) {
    }

    @Override
    public InputSupplier open(ResourceType resourcetype, Identifier identifier) {
        if (!identifier.getNamespace().equals("tulip")) {
            return null;
        }

        String s2 = identifier.getPath();
        String s3 = identifier.getNamespace();
        String s = "assets/" + s3 + "/" + s2;
        InputStream inputstream = MinecraftClient.class.getClassLoader().getResourceAsStream(s);
        return inputstream == null ? null : () -> inputstream;
    }

    @Override
    public Object parseMetadata(ResourceMetadataSerializer resourcemetadataserializer) throws IOException {
        return null;
    }

    @Override
    public InputSupplier openRoot(String[] astring) {
        return null;
    }

    @Override
    public String getId() {
        return "tulip";
    }

    @Override
    public ResourcePackInfo getInfo() {
        return null;
    }
}
