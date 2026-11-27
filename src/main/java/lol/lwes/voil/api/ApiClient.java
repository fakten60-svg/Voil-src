package lol.lwes.voil.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.config.ClientSettings;
import lol.lwes.voil.config.CloudConfig;
import lol.lwes.voil.config.ConfigManager;
import lol.lwes.voil.config.GuiTheme;
import lol.lwes.voil.config.ThemeInfo;
import lol.lwes.voil.config.ThemePreset;
import net.fabricmc.loader.api.FabricLoader;

public class ApiClient {
    public static final ApiClient INSTANCE = new ApiClient();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path root = FabricLoader.getInstance().getConfigDir().resolve("voil");
    private final Path configs = root.resolve("configs");
    private final Path themes = root.resolve("themes");

    private ApiClient() {
    }

    public void start() {
        try {
            Files.createDirectories(configs);
            Files.createDirectories(themes);
        } catch (IOException e) {
            VoilClient.LOGGER.warn("Could not create the config folder", e);
        }
        installBundledThemes();
        getAllConfigs(false);
    }

    private void installBundledThemes() {
        for (String name : bundledThemeNames()) {
            Path file = themes.resolve(name + ".json");
            if (Files.exists(file)) {
                continue;
            }
            try (InputStream in = ApiClient.class.getResourceAsStream("/assets/voil/themes/" + name + ".json")) {
                if (in != null) {
                    Files.createDirectories(themes);
                    Files.write(file, in.readAllBytes());
                }
            } catch (IOException e) {
                VoilClient.LOGGER.warn("Could not install theme {}", name, e);
            }
        }
    }

    public void loadSettings() {
        Path settings = root.resolve("settings.json");
        if (Files.exists(settings)) {
            ConfigManager.loadSettings(read(settings));
        }
        JsonObject client = readJson(root.resolve("client.json"));
        if (client != null) {
            ClientSettings.loadFromJson(client);
        }
        Path hud = root.resolve("hud.json");
        if (Files.exists(hud)) {
            VoilClient.getHudLayout().deserialize(read(hud));
        }
        getTheme();
        getUserData();
    }

    public void shutdown() {
        uploadSettings(ConfigManager.serializeModules());
        saveClientSettings();
        saveHudLayout();
    }

    public void uploadSettings(JsonObject settings) {
        write(root.resolve("settings.json"), GSON.toJson(settings));
    }

    public void saveClientSettings() {
        write(root.resolve("client.json"), GSON.toJson(ClientSettings.toJson()));
    }

    public void saveHudLayout() {
        write(root.resolve("hud.json"), VoilClient.getHudLayout().serialize());
    }

    public void getAllConfigs(boolean scripts) {
        if (scripts) {
            return;
        }
        ClientSettings.cloudConfigs.clear();
        for (Path file : list(configs)) {
            JsonObject stored = readJson(file);
            if (stored == null) {
                continue;
            }
            String fileName = file.getFileName().toString();
            String name = text(stored, "name", baseName(file));
            JsonObject data = configData(stored);
            String author = text(stored, "author", ClientSettings.username.getValue() == null ? "" : String.valueOf(ClientSettings.username.getValue()));
            String created = text(stored, "createdAt", "");
            String updated = text(stored, "updatedAt", created);
            ClientSettings.cloudConfigs.put(name, new CloudConfig(name, author, text(stored, "description", ""), fileName, "Local", data, 0, 0, created, updated, text(stored, "category", "")));
        }
    }

    public void getConfig(String name) {
        getConfigData(name).thenAccept(json -> VoilClient.mc.execute(() -> ConfigManager.loadSettings(json)));
    }

    public CompletableFuture<String> getConfigData(String name) {
        Path file = configs.resolve(fileName(name));
        if (!Files.exists(file)) {
            return CompletableFuture.failedFuture(new IOException("No config named " + name));
        }
        JsonObject stored = readJson(file);
        if (stored == null) {
            return CompletableFuture.failedFuture(new IOException("Config " + name + " is damaged"));
        }
        return CompletableFuture.completedFuture(GSON.toJson(configData(stored)));
    }

    public CompletableFuture<Boolean> uploadConfig(JsonObject data, String name, String description, boolean shared, String category, String anticheat, String type) {
        Path file = configs.resolve(fileName(name));
        JsonObject previous = Files.exists(file) ? readJson(file) : null;
        String now = Instant.now().toString();
        JsonObject stored = new JsonObject();
        stored.addProperty("name", name);
        stored.addProperty("description", description == null ? "" : description);
        stored.addProperty("category", category == null ? "" : category);
        stored.addProperty("anticheat", anticheat == null ? "Any" : anticheat);
        stored.addProperty("type", type == null ? "Config" : type);
        if (ClientSettings.username.getValue() != null) {
            stored.addProperty("author", String.valueOf(ClientSettings.username.getValue()));
        }
        stored.addProperty("createdAt", previous != null ? text(previous, "createdAt", now) : now);
        stored.addProperty("updatedAt", now);
        stored.add("data", data);
        write(file, GSON.toJson(stored));
        getAllConfigs(false);
        return CompletableFuture.completedFuture(true);
    }

    public CompletableFuture<Boolean> updateConfig(String name, JsonObject data, String description, String category, boolean shared, String anticheat, String type, String id) {
        return uploadConfig(data, name, description, shared, category, anticheat, type);
    }

    public CompletableFuture<Boolean> updateConfig(CloudConfig config) {
        Path file = configs.resolve(config.getFileName());
        JsonObject stored = Files.exists(file) ? readJson(file) : null;
        String description = stored != null ? text(stored, "description", config.getDescription()) : config.getDescription();
        String category = stored != null ? text(stored, "category", "") : "";
        String anticheat = stored != null ? text(stored, "anticheat", "Any") : "Any";
        return uploadConfig(ConfigManager.serializeModules(), config.getName(), description, false, category, anticheat, "Config");
    }

    public void deleteConfig(String name) {
        try {
            Files.deleteIfExists(configs.resolve(fileName(name)));
        } catch (IOException e) {
            VoilClient.LOGGER.warn("Could not delete config {}", name, e);
        }
        getAllConfigs(false);
    }

    public void shareConfig(String name) {
    }

    public CompletableFuture<Object> upvoteConfig(String id) {
        return offline();
    }

    public CompletableFuture<Object> downvoteConfig(String id) {
        return offline();
    }

    public CompletableFuture<List<ThemeInfo>> getCustomThemes(boolean favourites) {
        return CompletableFuture.completedFuture(new ArrayList<>());
    }

    private static List<String> bundledThemeNames() {
        try (InputStream in = ApiClient.class.getResourceAsStream("/assets/voil/themes/index.txt")) {
            if (in == null) {
                return List.of();
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().map(String::trim).filter(line -> !line.isEmpty()).toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private Path findTheme(String key) {
        Path direct = themes.resolve(fileName(key));
        if (Files.exists(direct)) {
            return direct;
        }
        for (Path file : list(themes)) {
            JsonObject theme = readJson(file);
            if (theme != null && theme.has("id") && theme.get("id").getAsString().equals(key)) {
                return file;
            }
        }
        return null;
    }

    public CompletableFuture<List<ThemePreset>> getSharedThemes() {
        List<ThemePreset> presets = new ArrayList<>();
        for (Path file : list(themes)) {
            JsonObject theme = readJson(file);
            if (theme == null) {
                continue;
            }
            String name = theme.has("name") ? theme.get("name").getAsString() : baseName(file);
            String description = theme.has("description") ? theme.get("description").getAsString() : "";
            String type = theme.has("type") ? theme.get("type").getAsString() : "dark";
            String id = theme.has("id") ? theme.get("id").getAsString() : name;
            JsonObject preview = theme.has("preview") ? theme.getAsJsonObject("preview") : new JsonObject();
            Color accent = preview.has("accent") ? themeColor(preview, "accent") : themeColor(theme, "accentColor1");
            Color start = preview.has("start") ? themeColor(preview, "start") : accent;
            Color end = preview.has("end") ? themeColor(preview, "end") : themeColor(theme, "accentColor2");
            Color background = preview.has("background") ? themeColor(preview, "background") : themeColor(theme, "windowColor");
            presets.add(new ThemePreset(name, description, type, id, start, end, accent, background));
        }
        return CompletableFuture.completedFuture(presets);
    }

    private static Color themeColor(JsonObject theme, String key) {
        if (theme.has(key) && theme.get(key).isJsonArray() && theme.getAsJsonArray(key).size() >= 3) {
            var rgb = theme.getAsJsonArray(key);
            return new Color(rgb.get(0).getAsInt(), rgb.get(1).getAsInt(), rgb.get(2).getAsInt());
        }
        return Color.BLACK;
    }

    public CompletableFuture<String> getSharedThemeData(String name) {
        return getCustomThemeData(name);
    }

    public CompletableFuture<String> getCustomThemeData(String name) {
        Path file = findTheme(name);
        if (file == null) {
            return CompletableFuture.failedFuture(new IOException("No theme named " + name));
        }
        return CompletableFuture.supplyAsync(() -> read(file));
    }

    public void saveTheme(String json) {
        if (json == null || json.isBlank()) {
            return;
        }
        JsonObject theme = JsonParser.parseString(json).getAsJsonObject();
        String name = theme.has("name") ? theme.get("name").getAsString() : "theme";
        write(themes.resolve(fileName(name)), GSON.toJson(theme));
        VoilClient.mc.execute(() -> GuiTheme.loadTheme(json));
    }

    public CompletableFuture<Object> createTheme(String name, String description, boolean shared, String data, String preview) {
        saveTheme(data);
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Object> updateTheme(String id, String name, String description, boolean shared, String data, String preview) {
        saveTheme(data);
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Object> updateThemeInfo(String id, String name, boolean shared) {
        return offline();
    }

    public CompletableFuture<Object> deleteCustomTheme(String name) {
        try {
            Files.deleteIfExists(themes.resolve(fileName(name)));
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Object> voteTheme(String id, boolean up) {
        return offline();
    }

    public CompletableFuture<Boolean> favoriteCustomTheme(String id) {
        return CompletableFuture.completedFuture(false);
    }

    public void requestCustomThemeData(String name) {
        getCustomThemeData(name).thenAccept(json -> VoilClient.mc.execute(() -> GuiTheme.loadTheme(json)));
    }

    public void requestSharedThemeData(String name) {
        requestCustomThemeData(name);
    }

    public void requestFavoriteTheme(String id) {
    }

    public void getTheme() {
        installBundledThemes();
        Path file = findTheme(String.valueOf(ClientSettings.activeTheme.getValue()));
        String json = file != null ? read(file) : null;
        if (json == null) {
            try (InputStream in = ApiClient.class.getResourceAsStream("/assets/voil/themes/default.json")) {
                json = in == null ? null : new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                VoilClient.LOGGER.warn("Could not read the default theme", e);
            }
        }
        if (json != null) {
            GuiTheme.loadTheme(json);
        }
    }

    public void sendCrashReport(String report) {
    }

    public void getUserData() {
        if (VoilClient.mc != null && VoilClient.mc.getSession() != null) {
            ClientSettings.username.setValue(VoilClient.mc.getSession().getUsername());
        }
        ClientSettings.userRank.setValue("voil+");
        ClientSettings.userAvatarUrl = profileImage("avatar.png");
        ClientSettings.activeAvatarUrl = profileImage("banner.png");
    }

    private String profileImage(String name) {
        Path custom = root.resolve("profile").resolve(name);
        try {
            if (Files.exists(custom)) {
                return Base64.getEncoder().encodeToString(Files.readAllBytes(custom));
            }
            try (InputStream in = ApiClient.class.getResourceAsStream("/assets/voil/profile/" + name)) {
                return in == null ? null : Base64.getEncoder().encodeToString(in.readAllBytes());
            }
        } catch (IOException e) {
            VoilClient.LOGGER.warn("Could not read profile image {}", name, e);
            return null;
        }
    }

    public void getReleaseNotes() {
    }

    public void loadIrc() {
    }

    public void sendChatMessage(String message) {
    }

    public void sendReply(String message, long replyTo) {
    }

    public void reactToMessage(long messageId, String reaction) {
    }

    public void ping() {
    }

    private static <T> CompletableFuture<T> offline() {
        return CompletableFuture.failedFuture(new IOException("Offline"));
    }

    private static JsonObject configData(JsonObject stored) {
        return stored.has("data") && stored.get("data").isJsonObject() ? stored.getAsJsonObject("data") : stored;
    }

    private static String text(JsonObject object, String key, String fallback) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : fallback;
    }

    private static String fileName(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_") + ".json";
    }

    private static String baseName(Path file) {
        String name = file.getFileName().toString();
        return name.endsWith(".json") ? name.substring(0, name.length() - 5) : name;
    }

    private static List<Path> list(Path dir) {
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(p -> p.toString().endsWith(".json")).sorted().toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private static JsonObject readJson(Path file) {
        try {
            return JsonParser.parseString(read(file)).getAsJsonObject();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(Path file, String content) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            VoilClient.LOGGER.warn("Could not write {}", file, e);
        }
    }
}
