package com.example.bstickers;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

public class StickerPack {
    private String name;
    private String dir;

    private List<Sticker> stickers = new ArrayList<>();

    public StickerPack(File dir) {
        this(dir.getName());
    }

    public StickerPack(String dir) {
        this.dir = dir;
        this.name = dir;
    }

    public void setName(String name) { this.name = name; }

    public void addSticker(Sticker sticker) { stickers.add(sticker); }

    public String name() { return name; }
    public String dir() { return dir; }
    public List<Sticker> stickers() { return stickers; }

    public boolean empty() { return stickers.size() == 0; }

    public Sticker getSticker(int index) {
        index = index % stickers.size();
        if (index < 0) index += stickers.size();

        return stickers.get(index);
    }
}
