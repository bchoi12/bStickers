package com.example.bstickers;

import android.app.AppOpsManager;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputConnection;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Board extends InputMethodService {

    private static final String AUTHORITY = "com.example.bstickers.inputcontent";
    private static Map<String, String> SUPPORTED_TYPES = new HashMap<String,String>(){{
        this.put("png", "image/png");
        this.put("gif", "image/gif");
        this.put("mp3", "audio/mp3");
    }};

    private ConstraintLayout container;
    private GridLayout packLayout;
    private GridLayout stickerLayout;

    private String currentPack = "";
    private LinkedHashMap<String, StickerPack> packs;

    private int width;
    private int height;
    private int numPacksInRow;
    private int numStickersInRow;

    @Override
    public View onCreateInputView() {
        super.onCreateInputView();
        container = new ConstraintLayout(this);
        container.setBackgroundColor(Color.LTGRAY);

        LinearLayout keyboard = new LinearLayout(this);
        keyboard.setOrientation(LinearLayout.VERTICAL);

        final HorizontalScrollView packScroll = new HorizontalScrollView(this);
        packLayout = new GridLayout(this);
        packLayout.setOrientation(GridLayout.HORIZONTAL);

        packScroll.addView(packLayout);
        keyboard.addView(packScroll);

        final ScrollView stickerScroll = new ScrollView(this);
        stickerLayout = new GridLayout(this);
        stickerLayout.setOrientation(GridLayout.HORIZONTAL);
        stickerScroll.addView(stickerLayout);
        keyboard.addView(stickerScroll);
        container.addView(keyboard);

        resizeKeyboard();
        populatePacks();

        return container;
    }

    private void resizeKeyboard() {
        width = Resources.getSystem().getDisplayMetrics().widthPixels;
        height = Resources.getSystem().getDisplayMetrics().heightPixels;

        numPacksInRow = width / 200 + 1;
        numStickersInRow = width / 400 + 1;

        double scale;
        if (Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            scale = 0.55;
        } else {
            scale = 0.45;
        }

        container.setMinHeight((int) (height * scale));
        container.setMaxHeight((int) (height * scale));
        stickerLayout.setColumnCount(numStickersInRow);
    }

    private void populatePacks() {
        int buttonDimension = width / numPacksInRow;
        if (packLayout.getChildCount() == 0) {
            ImageButton refresh = new ImageButton(this);
            refresh.setImageResource(R.drawable.ic_refresh);
            refresh.setMinimumWidth(buttonDimension);
            refresh.setMinimumHeight(buttonDimension);
            refresh.setMaxWidth(buttonDimension);
            refresh.setMaxHeight(buttonDimension);

            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    populatePacks();
                }
            });
            packLayout.addView(refresh);
        }

        if (packLayout.getChildCount() > 1) {
            packLayout.removeViews(1, packLayout.getChildCount()-1);
            stickerLayout.removeAllViews();
        }

        packs = getPacks();
        if (packs.keySet().isEmpty()) {
            Toast.makeText(this, "You have no stickers! Use the bStickers app to get some.", Toast.LENGTH_LONG).show();
            return;
        }

        for (Map.Entry<String, StickerPack> entry : packs.entrySet()) {
            final String packName = entry.getKey();
            StickerPack pack = entry.getValue();

            if (currentPack.length() == 0) currentPack = packName;

            ImageButton packButton = new ImageButton(this);
            packButton.setImageDrawable(Drawable.createFromPath(getPreviewImage(pack)));
            packButton.setMinimumWidth(buttonDimension);
            packButton.setMinimumHeight(buttonDimension);
            packButton.setMaxWidth(buttonDimension);
            packButton.setMaxHeight(buttonDimension);
            packButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentPack = packName;
                    populateStickers(packName);
                }
            });

            packLayout.addView(packButton);
        }

        populateStickers(currentPack);
    }

    private void populateStickers(String currentPack) {
        stickerLayout.removeAllViews();

        if (packs == null || currentPack.length() == 0) return;

        if (packs.get(currentPack) == null) {
            Toast.makeText(this, "Uh oh, looks like the selected pack was deleted?", Toast.LENGTH_LONG).show();
            return;
        }

        for (final File sticker : getStickers(packs.get(currentPack))) {
            ImageButton button = new ImageButton(this);
            button.setBackgroundColor(Color.WHITE);

            if (isAudio(sticker)) {
                button.setImageDrawable(Drawable.createFromPath(getAudioImage(sticker)));
            } else {
                button.setImageDrawable(Drawable.createFromPath(sticker.getPath()));
            }
            int size = width / numStickersInRow;
            button.setMinimumWidth(size);
            button.setMinimumHeight(size);
            button.setMaxWidth(size);
            button.setMaxHeight(size);

            button.setScaleType(ImageView.ScaleType.FIT_CENTER);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO: save description to some metadata file to be read here?
                    Board.this.doCommitContent("bSticker", SUPPORTED_TYPES.get(getExtension(sticker)), sticker);
                }
            });
            stickerLayout.addView(button);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        ViewTreeObserver observer = container.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                resizeKeyboard();
                container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        // In full-screen mode the inserted content is likely to be hidden by the IME.
        return false;
    }

    private LinkedHashMap<String, StickerPack> getPacks() {
        LinkedHashMap<String, StickerPack> packs = new LinkedHashMap<>();
        for (File dir : getFilesDir().listFiles()) {
            if (!dir.isDirectory()) continue;

            StickerPack pack = new StickerPack(dir.getName());
            for (File file: dir.listFiles()) {
                if (!isStickerSupported(file)) continue;

                pack.addSticker(new Sticker(file));
            }
            if (pack.empty()) continue;

            packs.put(pack.dir(), pack);
        }
        return packs;
    }

    private List<File> getStickers(StickerPack pack) {
        File dir = new File(getFilesDir() + "/" + pack.dir());

        List<File> stickers = new ArrayList<>();
        for (final File sticker : dir.listFiles()) {
            if (!isStickerSupported(sticker)) continue;

            stickers.add(sticker);
        }

        return stickers;
    }

    private boolean isStickerSupported(File file) {
        if (file.isDirectory()) return false;

        if (!SUPPORTED_TYPES.containsKey(getExtension(file))) return false;

        return true;
    }

    private String getAudioImage(File file) {
        int i = file.getPath().lastIndexOf('.');
        if (i <= 0) {
            return "";
        }
        return file.getPath().substring(0, i) + ".png";
    }

    private boolean isAudio(File file) {
        return file.getPath().endsWith("mp3");
    }

    private String getPreviewImage(StickerPack pack) {
        for (Sticker sticker : pack.stickers()) {
            if (sticker.fname().endsWith(".png")) {
                return getFilesDir().getPath() + "/" + pack.dir() + "/" + sticker.fname();
            }
        }

        return "";
    }

    private String getExtension(File file) {
        int i = file.getName().lastIndexOf('.');
        if (i > 0) {
            return file.getName().substring(i+1);
        }
        return "";
    }

    private void doCommitContent(@NonNull String description, @NonNull String mimeType,
                                 @NonNull File file) {
        if (!file.exists()) {
            populatePacks();
            return;
        }

        final EditorInfo editorInfo = getCurrentInputEditorInfo();

        // Validate packageName again just in case.
        if (!validatePackageName(editorInfo)) {
            return;
        }

        final Uri contentUri = FileProvider.getUriForFile(this, AUTHORITY, file);

        // As you as an IME author are most likely to have to implement your own content provider
        // to support CommitContent API, it is important to have a clear spec about what
        // applications are going to be allowed to access the content that your are going to share.
        final int flag;
        if (Build.VERSION.SDK_INT >= 25) {
            // On API 25 and later devices, as an analogy of Intent.FLAG_GRANT_READ_URI_PERMISSION,
            // you can specify InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION to give
            // a temporary read access to the recipient application without exporting your content
            // provider.
            flag = InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;

        } else {
            // On API 24 and prior devices, we cannot rely on
            // InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION. You as an IME author
            // need to decide what access control is needed (or not needed) for content URIs that
            // you are going to expose. This sample uses Context.grantUriPermission(), but you can
            // implement your own mechanism that satisfies your own requirements.
            flag = 0;
            try {
                // TODO: Use revokeUriPermission to revoke as needed.
                grantUriPermission(
                        editorInfo.packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception e){
                Log.e("bcd", "grantUriPermission failed packageName=" + editorInfo.packageName
                        + " contentUri=" + contentUri, e);
            }
        }

        final InputContentInfoCompat inputContentInfoCompat = new InputContentInfoCompat(
                contentUri,
                new ClipDescription(description, new String[]{mimeType}),
                null /* linkUrl */);
        InputConnectionCompat.commitContent(
                getCurrentInputConnection(), getCurrentInputEditorInfo(), inputContentInfoCompat,
                flag, null);
    }

    private boolean isCommitContentSupported(
            @Nullable EditorInfo editorInfo, @NonNull String mimeType) {
        if (editorInfo == null) {
            return false;
        }

        final InputConnection ic = getCurrentInputConnection();
        if (ic == null) {
            return false;
        }

        if (!validatePackageName(editorInfo)) {
            return false;
        }

        final String[] supportedMimeTypes = EditorInfoCompat.getContentMimeTypes(editorInfo);
        for (String supportedMimeType : supportedMimeTypes) {
            if (ClipDescription.compareMimeTypes(mimeType, supportedMimeType)) {
                return true;
            }
        }
        return false;
    }

    private boolean validatePackageName(@Nullable EditorInfo editorInfo) {
        if (editorInfo == null) {
            return false;
        }
        final String packageName = editorInfo.packageName;
        if (packageName == null) {
            return false;
        }

        // In Android L MR-1 and prior devices, EditorInfo.packageName is not a reliable identifier
        // of the target application because:
        //   1. the system does not verify it [1]
        //   2. InputMethodManager.startInputInner() had filled EditorInfo.packageName with
        //      view.getContext().getPackageName() [2]
        // [1]: https://android.googlesource.com/platform/frameworks/base/+/a0f3ad1b5aabe04d9eb1df8bad34124b826ab641
        // [2]: https://android.googlesource.com/platform/frameworks/base/+/02df328f0cd12f2af87ca96ecf5819c8a3470dc8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        }

        final InputBinding inputBinding = getCurrentInputBinding();
        if (inputBinding == null) {
            // Due to b.android.com/225029, it is possible that getCurrentInputBinding() returns
            // null even after onStartInputView() is called.
            // TODO: Come up with a way to work around this bug....
            Log.wtf("bcd", "inputBinding should not be null here. ");
            return false;
        }
        final int packageUid = inputBinding.getUid();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final AppOpsManager appOpsManager =
                    (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            try {
                appOpsManager.checkPackage(packageUid, packageName);
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        final PackageManager packageManager = getPackageManager();
        final String possiblePackageNames[] = packageManager.getPackagesForUid(packageUid);
        for (final String possiblePackageName : possiblePackageNames) {
            if (packageName.equals(possiblePackageName)) {
                return true;
            }
        }
        return false;
    }
}