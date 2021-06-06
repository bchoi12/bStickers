package com.example.bstickers;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static int pack = 0;
    private static int sticker = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        WebView web = findViewById(R.id.stickerView);
        web.getSettings().setUseWideViewPort(true);
        web.getSettings().setLoadWithOverviewMode(true);
        web.setVerticalScrollBarEnabled(false);

        StickerLoader.init();
        updatePack();
    }

    public void makeSticker(View view) {
        StickerLoader.savePack(MainActivity.this, pack);
    }
    public void removeSticker(View view) { StickerLoader.removePack(MainActivity.this, pack); }

    public void decrement(View view) {
        if (!StickerLoader.hasIndex(pack-1)) {
            pack = StickerLoader.size() - 1;
        } else {
            pack--;
        }
        updatePack();
    }
    public void increment(View view) {
        if (!StickerLoader.hasIndex(pack+1)) {
            pack = 0;
        } else {
            pack++;
        }
        updatePack();
    }

    public void incrementSticker(View view) {
        sticker++;
        if (sticker >= StickerLoader.getPack(pack).stickers().size()) sticker = 0;

        updateSticker();
    }
    public void decrementSticker(View view) {
        sticker--;
        if (sticker < 0) sticker = StickerLoader.getPack(pack).stickers().size() - 1;

        updateSticker();
    }

    private void updatePack() {
        sticker = 0;
        updateSticker();
    }
    private void updateSticker() {
        StickerPack sp = StickerLoader.getPack(pack);

        ((WebView) findViewById(R.id.stickerView)).loadUrl(sp.getSticker(sticker).getUrl());
        ((TextView) findViewById(R.id.title)).setText(sp.name());

        Sticker s = sp.stickers().get(sticker);
        ((TextView) findViewById(R.id.stickerInfo)).setText(s.name() + "\n" + s.description());
    }
}