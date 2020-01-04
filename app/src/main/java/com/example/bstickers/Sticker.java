package com.example.bstickers;

import com.google.firebase.appindexing.builders.Indexables;
import com.google.firebase.appindexing.builders.StickerBuilder;
import com.google.firebase.appindexing.builders.StickerPackBuilder;

public class Sticker {

    private static final String SPLIT_CHAR = "\\|";

    private String fname;
    private String name;
    private String desc;
    private String[] keywords;

    public Sticker(String packDir, String data) {
        // TODO: do not put split logic here
        String[] meta = data.split(SPLIT_CHAR);
        fname = packDir + "/" + meta[0];
        name = meta[1];
        desc = meta[2];
        keywords = meta[3].split(",");
    }

    public String name() { return name; }
    public String desc() { return desc; }

    public String getUrl() {
        return Global.IMG_URL + fname;
    }

    public String getIndexUrl() {
        return Global.STICKER_URL_PREFIX + name;
    }

    public StickerBuilder getBuilder(String pack) {
        return Indexables.stickerBuilder()
                .setName(name)
                .setUrl(getIndexUrl())
                .setImage(getUrl())
                .setDescription(desc)
                .setKeywords(keywords)
                .setIsPartOf(Indexables.stickerPackBuilder().setName(pack));
    }
}
