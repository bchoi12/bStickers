package com.example.bstickers;

import com.google.firebase.appindexing.builders.Indexables;
import com.google.firebase.appindexing.builders.StickerBuilder;
import com.google.firebase.appindexing.builders.StickerPackBuilder;

import java.util.ArrayList;
import java.util.List;

public class StickerPack {
    private String name;
    private String dir;
    private String url;

    private StickerPackBuilder pack;
    private List<Sticker> stickers = new ArrayList<>();
    private List<StickerBuilder> stickerBuilders = new ArrayList<>();

    public StickerPack(String name, String dir) {
        this.name = name;
        this.dir = dir;
        this.url = Global.PACK_URL_PREFIX + dir;

        pack = Indexables.stickerPackBuilder()
                .setName(name)
                .setUrl(url);
    }

    public void addSticker(Sticker sticker) {
        stickers.add(sticker);
        StickerBuilder builder = sticker.getBuilder(name);
        stickerBuilders.add(builder);
    }

    public String name() { return name; }
    public String dir() { return dir; }
    public String getIndexUrl() { return url; }

    public String getStickerUrl(int index) {
        index = index % stickers.size();
        if (index < 0) index += stickers.size();

        return stickers.get(index).getUrl();
    }

    public List<StickerBuilder> getStickerBuilders() {
        return stickerBuilders;
    }

    public StickerPackBuilder getPackBuilder() {
        pack.setHasSticker(stickerBuilders.toArray(new StickerBuilder[stickerBuilders.size()]));
        return pack;
    }
}
