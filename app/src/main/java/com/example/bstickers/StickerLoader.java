package com.example.bstickers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class StickerLoader {

    private static List<StickerPack> stickerPacks;

    public static void init() {
        try {
            // TODO: make this async
            stickerPacks = new MetaLoader().execute().get();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't load initial sticker data!");
        }
    }

    public static StickerPack getPack(int index) {
        return stickerPacks.get(index);
    }
    public static int size() {
        return stickerPacks.size();
    }
    public static boolean hasIndex(int index) {
        return 0 <= index && index < stickerPacks.size();
    }

    public static void savePack(Context context, int index) {
        if (!hasIndex(index)) return;

        StickerPack pack = stickerPacks.get(index);
        try {
            Toast.makeText(context, "Downloading sticker pack " + pack.name() + "...", Toast.LENGTH_LONG).show();
            new StickerWriter(context).execute(pack);
        } catch (Exception e) {
            Log.wtf("bcd", e);
        }
    }

    public static void removePack(Context context, int index) {
        if (!hasIndex(index)) return;

        StickerPack pack = stickerPacks.get(index);
        File dir = new File(context.getFilesDir(), pack.dir());

        if (dir.exists() && !deleteDir(dir)) {
            Toast.makeText(context, "Failed to remove sticker pack " + pack.name(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Successfully removed sticker pack " + pack.name(), Toast.LENGTH_LONG).show();
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (String file : dir.list()) {
                if (!deleteDir(new File(dir, file))) return false;
            }
        }
        return dir.delete();
    }
}
