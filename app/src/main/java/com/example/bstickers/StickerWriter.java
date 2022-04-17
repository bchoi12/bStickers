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
            try {
                final File outputFile = new File(dir, sticker.fname());
                writeFile(new URL(sticker.getUrl()), outputFile);

                final File previewOutputFile = new File(dir, sticker.imgFname());
                if (sticker.getPreviewUrl() != sticker.getUrl()) {
                    writeFile(new URL(sticker.getPreviewUrl()), previewOutputFile);
                }
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

    private void writeFile(URL url, File outputFile) throws Exception {
        InputStream in = new BufferedInputStream(url.openStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

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
    }
}