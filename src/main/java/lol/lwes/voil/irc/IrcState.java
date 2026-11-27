package lol.lwes.voil.irc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import lol.lwes.voil.api.ApiResponse;
import lol.lwes.voil.community.CommunityPost;
import lol.lwes.voil.config.ClientSettings;

public class IrcState {
    private static final int MAX_MESSAGES = 200;
    private static final Deque<CommunityPost> messages = new ArrayDeque<>();
    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private static final Map<Long, Map<String, Integer>> reactions = new ConcurrentHashMap<>();
    private static final Map<Long, Set<String>> myReactions = new ConcurrentHashMap<>();
    private static final AtomicLong version = new AtomicLong();
    private static volatile boolean connected = true;
    private static volatile boolean muted;
    private static volatile long mutedUntil;
    private static volatile long noticeTime;
    private static volatile String noticeMessage = "";

    private IrcState() {
    }

    public static boolean hasReacted(long messageId, String emoji) {
        Set<String> set = myReactions.get(messageId);
        if (set == null) {
            return false;
        }

        return set.contains(emoji);
    }

    public static boolean isConnected() {
        return connected;
    }

    public static void onReaction(ApiResponse response) {
        long messageId = response.getLong("messageId");
        String emoji = response.getString("emoji");
        if (emoji == null || emoji.isEmpty()) {
            return;
        }

        int count = response.getInt("count");
        if (count <= 0) {
            Map<String, Integer> map = reactions.get(messageId);
            if (map != null) {
                map.remove(emoji);
                if (map.isEmpty()) {
                    reactions.remove(messageId);
                }
            }
        } else {
            reactions.computeIfAbsent(messageId, id -> new ConcurrentHashMap<>()).put(emoji, count);
        }

        if (getUsername().equals(response.getString("actor"))) {
            Set<String> set = myReactions.computeIfAbsent(messageId, id -> ConcurrentHashMap.newKeySet());
            if (response.getBoolean("added")) {
                set.add(emoji);
            } else {
                set.remove(emoji);
            }
        }

        notifyListeners();
    }

    public static List<CommunityPost> getMessages() {
        synchronized (messages) {
            return new ArrayList<>(messages);
        }
    }

    public static void onNotice(ApiResponse response) {
        String message = response.getString("message");
        noticeMessage = message == null ? "" : message;
        noticeTime = System.currentTimeMillis();
        String code = response.getString("code");
        switch (code == null ? "" : code) {
            case "muted" -> muted = true;
            case "unmuted" -> {
                muted = false;
                mutedUntil = 0L;
            }
            case "irc-disabled" -> connected = false;
            default -> {
            }
        }

        notifyListeners();
    }

    public static Map<String, Integer> getReactions(long messageId) {
        Map<String, Integer> map = reactions.get(messageId);
        return map == null ? Map.of() : map;
    }

    public static long getVersion() {
        return version.get();
    }

    public static void removeListener(Runnable runnable) {
        listeners.remove(runnable);
    }

    public static String getUsername() {
        return String.valueOf(ClientSettings.username.getValue());
    }

    public static String getNoticeMessage() {
        return noticeMessage;
    }

    public static void onHistory(ApiResponse response) {
        connected = response.getBoolean("enabled");
        muted = response.getBoolean("muted");
        mutedUntil = response.getLong("mutedUntil");
        List<Map> list = response.getList("messages");
        reactions.clear();
        myReactions.clear();
        synchronized (messages) {
            messages.clear();

            for (Map map : list) {
                CommunityPost post = CommunityPost.fromMap(map);
                messages.addLast(post);
                readReactions(post.id(), map.get("reactions"));
            }

            trimHistory();
        }

        notifyListeners();
    }

    public static void clear() {
        synchronized (messages) {
            messages.clear();
        }

        reactions.clear();
        myReactions.clear();
        notifyListeners();
    }

    public static void onMessage(ApiResponse response) {
        CommunityPost post = CommunityPost.fromEntry(response);
        synchronized (messages) {
            messages.addLast(post);
            trimHistory();
        }

        notifyListeners();
    }

    public static long getNoticeTime() {
        return noticeTime;
    }

    private static void trimHistory() {
        while (messages.size() > MAX_MESSAGES) {
            CommunityPost post = messages.removeFirst();
            reactions.remove(post.id());
            myReactions.remove(post.id());
        }
    }

    private static void readReactions(long messageId, Object json) {
        if (!(json instanceof String text) || text.isBlank()) {
            return;
        }

        JsonArray array;
        try {
            array = JsonParser.parseString(text).getAsJsonArray();
        } catch (RuntimeException exception) {
            return;
        }

        for (JsonElement element : array) {
            if (!(element instanceof JsonObject object)) {
                continue;
            }

            JsonElement emojiElement = object.get("emoji");
            JsonElement countElement = object.get("count");
            if (emojiElement == null || countElement == null) {
                continue;
            }

            String emoji = emojiElement.getAsString();
            reactions.computeIfAbsent(messageId, id -> new ConcurrentHashMap<>()).put(emoji, countElement.getAsInt());
            JsonElement mine = object.get("mine");
            if (mine != null && mine.getAsBoolean()) {
                myReactions.computeIfAbsent(messageId, id -> ConcurrentHashMap.newKeySet()).add(emoji);
            }
        }
    }

    public static long getMutedUntil() {
        return mutedUntil;
    }

    public static void addListener(Runnable runnable) {
        listeners.add(runnable);
    }

    public static boolean isMuted() {
        return muted;
    }

    private static void notifyListeners() {
        version.incrementAndGet();

        for (Runnable listener : listeners) {
            try {
                listener.run();
            } catch (RuntimeException exception) {
            }
        }
    }
}
