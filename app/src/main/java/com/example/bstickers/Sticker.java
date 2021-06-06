package com.example.bstickers;

import java.io.File;

public class Sticker {

    private static final String SPLIT_CHAR = "\\|";

    private String pack;
    private String fname;

    private String name;
    private String desc;
    private String[] keywords;

    public Sticker(File file) {
        this(file.getParentFile().getName(), file.getName());
    }

    public Sticker(String pack, String fname) {
        this.pack = pack;
        this.fname = fname;

        // Set defaults for optional fields
        this.name = fname;
        this.desc = pack + "/" + fname;
        this.keywords = new String[1];
        keywords[0] = pack;
    }

    // Setters for optional fields
    public void setName(String name) { this.name = name; }
    public void setDescription(String desc) { this.desc = desc; }
    public void setKeywords(String [] keywords) { this.keywords = keywords; }

    public String fname() { return fname; }
    public String name() { return name; }
    public String description() { return desc; }
    public String getUrl() {
        return Global.IMG_URL + pack + "/" + fname;
    }
}
