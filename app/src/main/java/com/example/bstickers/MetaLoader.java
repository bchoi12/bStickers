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
                StickerPack pack = new StickerPack(title[0], title[1]);

                while (true) {
                    data = in.readLine();
                    if (data == null || data.equals("#")) {
                        packs.add(pack);
                        break;
                    }

                    Sticker sticker = new Sticker(pack.dir(), data);
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
