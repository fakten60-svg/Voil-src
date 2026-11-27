package lol.lwes.voil.community;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.util.Map;
import lol.lwes.voil.api.ApiResponse;
import lol.lwes.voil.gui.clickgui.page.IrcPage;

public record CommunityPost(
    long id,
    String authorId,
    String authorRole,
    String authorName,
    long createdAt,
    String title,
    String type,
    JsonObject data,
    long editedAt,
    String editor,
    String editorRole
) {
    public CommunityPost {
        authorId = emptyIfNull(authorId);
        authorRole = emptyIfNull(authorRole);
        authorName = emptyIfNull(authorName);
        type = emptyIfNull(type);
        editor = emptyIfNull(editor);
        editorRole = emptyIfNull(editorRole);
    }

    private static String emptyIfNull(String s) {
        return s == null ? "" : s;
    }

    public boolean isDiscordUser() {
        return this.authorId.startsWith("discord:");
    }

    public int getDataInt() {
        try {
            return this.getDataElement().getAsInt();
        } catch (RuntimeException runtimeexception) {
            return 0;
        }
    }

    private static long getLong(Map<?, ?> map, String s) {
        return map.get(s) instanceof Number number ? number.longValue() : 0L;
    }

    public boolean isConfigPost() {
        return "config".equals(this.type) && this.data != null;
    }

    public boolean isServerPost() {
        return "server".equals(this.type) && this.data != null;
    }

    private JsonElement getDataElement() {
        return this.data;
    }

    public Color getRoleColor() {
        return switch (this.authorRole) {
            case "owner" -> IrcPage.COLOR_RED;
            case "mod" -> IrcPage.COLOR_BLUE;
            case "voil+" -> IrcPage.COLOR_PURPLE;
            case "system" -> IrcPage.COLOR_GOLD;
            default -> IrcPage.COLOR_LIGHT_GRAY;
        };
    }

    public String getDisplayAuthorId() {
        return this.authorId;
    }

    public boolean isSharedContent() {
        return this.isConfigPost();
    }

    private static JsonObject parseObject(String s) {
        String s1 = s;

        try {
            return JsonParser.parseString(s1).getAsJsonObject();
        } catch (RuntimeException runtimeexception) {
            return null;
        }
    }
    public String getDataString() {
        try {
            return this.getDataElement().getAsString();
        } catch (RuntimeException runtimeexception) {
            return "";
        }
    }

    public boolean isEdited() {
        return this.editedAt != 0L;
    }

    public static CommunityPost fromEntry(ApiResponse apiresponse) {
        return new CommunityPost(
            apiresponse.getLong("id"),
            apiresponse.getString("username"),
            apiresponse.getString("rank"),
            apiresponse.getString("message"),
            apiresponse.getLong("timestamp"),
            apiresponse.getString("avatarId"),
            apiresponse.getString("kind"),
            parseObject(apiresponse.getString("payload")),
            apiresponse.getLong("replyTo"),
            apiresponse.getString("replyUser"),
            apiresponse.getString("replyText")
        );
    }

    public static CommunityPost fromMap(Map<?, ?> map) {
        return new CommunityPost(
            getLong(map, "id"),
            getString(map, "username"),
            getString(map, "rank"),
            getString(map, "message"),
            getLong(map, "timestamp"),
            getString(map, "avatarId"),
            getString(map, "kind"),
            parseObject(getString(map, "payload")),
            getLong(map, "replyTo"),
            getString(map, "replyUser"),
            getString(map, "replyText")
        );
    }

    private static String getString(Map<?, ?> map, String s) {
        return map.get(s) instanceof String string ? string : "";
    }

    public String getAvatarKey() {
        return this.authorId;
    }
}
