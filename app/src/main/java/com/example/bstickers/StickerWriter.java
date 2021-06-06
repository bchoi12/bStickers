package com.example.bstickers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class StickerWriter extends AsyncTask<StickerPack, Boolean, Boolean> {

    private Context context;
    private File filesDir;

    public StickerWriter(Context context) {
        this.context = context;
        this.filesDir = context.getFilesDir();
    }

    @Override
    protected Boolean doInBackground(StickerPack... stickerPacks) {

        StickerPack pack = stickerPacks[0];
        final File dir = new File(filesDir, pack.dir());

        if (!dir.exists()) {
            dir.mkdir();
        }

        for (Sticker sticker : pack.stickers()) {
            final File outputFile = new File(dir, sticker.fname());

            URL url;
            InputStream in;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                url = new URL(sticker.getUrl());
                in = new BufferedInputStream(url.openStream());
                byte[] buf = new byte[1024];
                int n;
                while (-1 != (n = in.read(buf))) {
                    out.write(buf, 0, n);
                }
                out.close();
                in.close();

                byte[] response = out.toByteArray();
                FileOutputStream fos = new FileOutputStream(outputFile);
                fos.write(response);
                fos.close();

                Log.i("bcd", "wrote file " + outputFile.getPath());
            } catch (Exception e) {
                publishProgress(false);
                return false;
            }
        }

        publishProgress(true);
        return true;
    }

    @Override
    protected void onProgressUpdate(Boolean... success) {
        if (success[0]) {
            Toast.makeText(context, "Added new sticker pack!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failed to save sticker pack to local storage!", Toast.LENGTH_LONG).show();
        }

    }
}