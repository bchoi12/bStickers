package com.example.bstickers;

import android.util.Log;

import java.io.File;

public class Sticker {
    private String pack;
    private String fname;
    private String imgFname;

    private String name;
    private String desc;
    private String[] keywords;

    public Sticker(File file) {
        this(file.getParentFile().getName(), file.getName());
    }

    public Sticker(String pack, String fname) {
        this.pack = pack;
        this.fname = fname;

        if (fname.endsWith(".mp3")) {
            int i = fname.lastIndexOf('.');
            imgFname = fname.substring(0, i) + ".png";
        } else {
            imgFname = fname;
        }

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
    public String imgFname() { return imgFname; }
    public String name() { return name; }
    public String description() { return desc; }
    public String getUrl() {
        return Global.IMG_URL + pack + "/" + fname;
    }
    public String getPreviewUrl() {
        return Global.IMG_URL + pack + "/" + imgFname;
    }
}
