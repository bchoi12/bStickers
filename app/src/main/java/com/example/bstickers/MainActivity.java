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

        WebView web = (WebView) findViewById(R.id.stickerView);
        web.getSettings().setUseWideViewPort(true);
        web.getSettings().setLoadWithOverviewMode(true);

        StickerIndex.init();
        updatePack();
    }

    public void makeSticker(View view) {
        StickerIndex.indexSticker(MainActivity.this, pack);
    }
    public void removeSticker(View view) { StickerIndex.removeSticker(view.getContext(), pack); }

    public void decrement(View view) {
        if (!StickerIndex.hasIndex(pack-1)) {
            pack = StickerIndex.size() - 1;
        } else {
            pack--;
        }
        updatePack();
    }
    public void increment(View view) {
        if (!StickerIndex.hasIndex(pack+1)) {
            pack = 0;
        } else {
            pack++;
        }
        updatePack();
    }

    public void incrementSticker(View view) {
        sticker++;
        if (sticker >= StickerIndex.getPack(pack).stickers().size()) sticker = 0;

        updateSticker();
    }
    public void decrementSticker(View view) {
        sticker--;
        if (sticker < 0) sticker = StickerIndex.getPack(pack).stickers().size() - 1;

        updateSticker();
    }

    private void updatePack() {
        sticker = 0;
        updateSticker();
    }
    private void updateSticker() {
        StickerPack sp = StickerIndex.getPack(pack);

        ((WebView) findViewById(R.id.stickerView)).loadUrl(sp.getStickerUrl(sticker));
        ((TextView) findViewById(R.id.title)).setText(sp.name());

        Sticker s = sp.stickers().get(sticker);
        ((TextView) findViewById(R.id.stickerInfo)).setText(s.name() + "\n" + s.desc());
    }
}