package lol.lwes.voil.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.awt.Color;

public class CloudConfig {
    private String name;
    private String author;
    private String description;
    private String fileName;
    private String role;
    private JsonObject data;
    private int upvotes;
    private int downvotes;
    private String createdAt;
    private String updatedAt;
    private String version;
    final int ownerColor = -10496;
    final int modColor = -4144960;
    final int plusColor = -3309774;
    final int systemColor = -65536;
    final int defaultColor = -8388480;

    public CloudConfig(String s, String s1, String s2, String s3, String s4, JsonObject jsonobject, int i, int j, String s5, String s6, String s7) {
        this.name = s;
        this.author = s1;
        this.description = s2;
        this.fileName = s3;
        this.role = s4;
        this.data = jsonobject;
        this.upvotes = i;
        this.downvotes = j;
        this.createdAt = s5;
        this.updatedAt = s6;
        this.version = s7;
    }

    public String getDescription() {
        return this.description;
    }

    public int getDownvotes() {
        return this.downvotes;
    }

    public void setName(String s) {
        this.name = s;
    }

    public void setVersion(String s) {
        this.version = s;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public int getUpvotes() {
        return this.upvotes;
    }

    public void setDescription(String s) {
        this.description = s;
    }

    public JsonObject getData() {
        return this.data;
    }

    public void setAuthor(String s) {
        this.author = s;
    }

    public String getName() {
        return this.name;
    }

    public void setData(JsonObject jsonobject) {
        this.data = jsonobject;
    }

    public void setCreatedAt(String s) {
        this.createdAt = s;
    }

    public String getUpdatedAt() {
        return this.updatedAt;
    }

    public Color getRoleColor() {
        switch (this.role) {
            case "owner":
                return new Color(0);
            case "voil":
                return new Color(0);
            case "voil+":
                return new Color(0);
            case "staff":
                return new Color(0);
            case "beta":
                return new Color(0);
            default:
                return Color.WHITE;
        }
    }

    public static CloudConfig fromJson(String s) {
        return new Gson().fromJson(s, CloudConfig.class);
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getRole() {
        return this.role;
    }

    public void setUpdatedAt(String s) {
        this.updatedAt = s;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public String getVersion() {
        return this.version;
    }
}
