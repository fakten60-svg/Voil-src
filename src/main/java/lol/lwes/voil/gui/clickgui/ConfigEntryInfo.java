package lol.lwes.voil.gui.clickgui;

public class ConfigEntryInfo {
    private String id;
    private String name;
    private String author;
    private String date;
    private String description;
    final int reservedA = 0;
    final int reservedB = 0;
    final int reservedC = 0;
    final int reservedD = 0;
    final int reservedE = 0;

    public ConfigEntryInfo(String s, String s1, String s2, String s3, String s4) {
        this.id = s4;
        this.name = s;
        this.author = s1;
        this.date = s3;
        this.description = s2;
    }

    public String getDate() {
        return this.date;
    }

    public void setDescription(String s) {
        this.description = s;
    }

    public void setName(String s) {
        this.name = s;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getDescription() {
        return this.description;
    }

    public String getId() {
        return this.id;
    }

    public void setAuthor(String s) {
        this.author = s;
    }

    public String getName() {
        return this.name;
    }

    public void setId(String s) {
        this.id = s;
    }
}
