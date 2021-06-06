package com.example.bstickers;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MetaLoader extends AsyncTask<Void, Void, List<StickerPack>> {

    private static final String SPLIT_CHAR = "\\|";

    @Override
    protected List<StickerPack> doInBackground(Void... v) {
        List<StickerPack> packs = new ArrayList<>();
        try {
            java.net.URL url = new URL(Global.META_URL);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String data;
            while(true) {
                data = in.readLine();
                if (data == null) break;

                String[] title = data.split(SPLIT_CHAR);
                StickerPack pack = new StickerPack(title[1]);
                pack.setName(title[0]);

                while (true) {
                    data = in.readLine();
                    if (data == null || data.equals("#")) {
                        packs.add(pack);
                        break;
                    }

                    String[] meta = data.split(SPLIT_CHAR);
                    Sticker sticker = new Sticker(pack.dir(), meta[0]);
                    if (meta.length >= 2) {
                        sticker.setName(meta[1]);
                    }
                    if (meta.length >= 3) {
                        sticker.setDescription(meta[2]);
                    }
                    if (meta.length >= 4) {
                        sticker.setKeywords(meta[3].split(","));
                    }

                    pack.addSticker(sticker);
                }
            }
        } catch (Exception e) {
            Log.wtf("wtf", e);
            return null;
        }
        return packs;
    }
}
