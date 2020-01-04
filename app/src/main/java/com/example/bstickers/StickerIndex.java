package com.example.bstickers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.StickerBuilder;

import java.util.ArrayList;
import java.util.List;

public class StickerIndex extends JobIntentService {

    private static final int UNIQUE_JOB_ID = 420;

    private static List<StickerPack> stickerPacks;

    public static void init() {
        try {
            stickerPacks = new MetaLoader().execute().get();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't load initial sticker data!");
        }
    }

    public static void indexSticker(Context context, int index) {
        Intent intent = new Intent();
        intent.putExtra("index", index);
        enqueueWork(context, StickerIndex.class, UNIQUE_JOB_ID, intent);
    }

    public static void removeSticker(final Context context, int index) {
        final StickerPack pack = stickerPacks.get(index);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    final ArrayList<String> indexUrls = new ArrayList<>();
                    for (Sticker s : pack.stickers()) {
                        indexUrls.add(s.getIndexUrl());
                    }
                    indexUrls.add(pack.getIndexUrl());

                    Task<Void> task = FirebaseAppIndex.getInstance().remove(indexUrls.toArray(new String[indexUrls.size()]));
                    task.addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void v) {
                            Toast.makeText(context, "Removed " + indexUrls.toString()  + ", it may take a few moments for the index to be cleared", Toast.LENGTH_LONG).show();
                        }
                    });
                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Uh oh something went wrong: " + e, Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("This will remove " + pack.name() + ". Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
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

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        final Context context = getApplicationContext();
        final int index = intent.getIntExtra("index", 0);
        final StickerPack pack = stickerPacks.get(index);

        List<Indexable> indexables = new ArrayList<>();
        indexables.add(pack.getPackBuilder().build());
        for (StickerBuilder sb : pack.getStickerBuilders()) {
            indexables.add(sb.build());
        }

        Task<Void> task = FirebaseAppIndex.getInstance().update(indexables.toArray(new Indexable[indexables.size()]));

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void v) {
                Toast.makeText(context, "Yay " + pack.name() + "! Please give the index some time to adjust.", Toast.LENGTH_LONG)
                        .show();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Uh oh something went wrong: " + e, Toast.LENGTH_LONG)
                        .show();
            }
        });

    }
}
