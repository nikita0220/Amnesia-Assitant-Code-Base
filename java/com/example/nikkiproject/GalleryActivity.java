package com.example.nikkiproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {

    private ArrayList<Bitmap> imageList = null;
    private String personId = null;
    private String DIR_NAME = "Person";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Intent intent = getIntent();
        personId = intent.getExtras().getString("personId");
        imageList = getImageData();
        initializeUi();
    }

    private ArrayList<Bitmap> getImageData() {
        ArrayList<Bitmap> result = new ArrayList<>();
        String personFolderName = "Images-" + personId;
        File internalDir = new File(getApplicationContext().getFilesDir(), DIR_NAME);
        File personFolder = new File(internalDir, personFolderName);

        if (!personFolder.exists()) {
            Log.v("NIKITA", "No memories saved");
            //super.finish();
        }
        else {
            File[] imageFiles = personFolder.listFiles();
            for (int imageIndex = 0; imageIndex < imageFiles.length; imageIndex ++) {
                Bitmap bMap = BitmapFactory.decodeFile(imageFiles[imageIndex].getPath());
                result.add(bMap);
            }
        }
        return result;
    }

    public void initializeUi() {
        GridLayoutManager galleryLayout = new GridLayoutManager(GalleryActivity.this, 4);
        RecyclerView galleryView = findViewById(R.id.gallery_recycler);
        galleryView.setLayoutManager(galleryLayout);

        GalleryRecyclerAdapter galleryAdapter = new GalleryRecyclerAdapter(this, imageList);
        galleryView.setAdapter(galleryAdapter);
    }
}