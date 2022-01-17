package com.example.nikkiproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public class MainActivity extends AppCompatActivity {

    private Menu optionsMenu;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageCheck = null;
    private File outputDir = null;
    private String tempImagePath = null;
    private DataManager dataManager = null;
    private LinearLayout displayPersonView = null;
    private TextView welcomeTextView = null;
    private TextView personNameView = null;
    private TextView personContactView = null;
    private String personId = null;
    private TextView errorMessage = null;

    private GalleryRecyclerAdapter galleryAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        personNameView = findViewById(R.id.main_display_name);
        personContactView = findViewById(R.id.main_display_relationship);
        displayPersonView = findViewById(R.id.main_display_person);
        displayPersonView.setVisibility(View.GONE);
        welcomeTextView = findViewById(R.id.startView);
        errorMessage = findViewById(R.id.main_user_message_error);
        errorMessage.setVisibility(View.GONE);


        dataManager = new DataManager(getApplicationContext());
        if (getPermissions()) {
            dataManager.initializeDb();
        }

//        dataManager.addPerson(new Person("bbdbd866-f98d-404e-aa41-1f9190543178", "Man", "xxx-xxx-xxxx"));
//        dataManager.addPerson(new Person("1e71600a-50dd-4845-a253-bba32909be6d", "Woman", "000-000-0000"));
//        dataManager.addPerson(new Person("831f5406-8da8-42b8-ba6f-9f3029745b65", "Child", "999-999-9999"));
//
    }


    public void takePicture(View view) {
        dispatchTakePictureIntent();
    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("Nikki", "worked");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            //imageCheck.setImageBitmap(imageBitmap);
            outputDir = new File(Environment.getExternalStorageDirectory(), "FaceAI");
            outputDir.mkdir();
            File imageFile = new File(outputDir.getAbsoluteFile(), "Temp-Face-File.jpg");
            String path = storeImage(imageBitmap);
            //loadImageFromStorage(path);
            String image64 = convertImageToString(imageBitmap);
            sendImage(image64);
        }
        else if (requestCode == 200) {
            String imageEncoded = null;
            ArrayList<String> imagesEncodedList = null;
            try {
                // When an Image is picked
                if (resultCode == RESULT_OK
                        && null != data) {
                    // Get the Image from data

                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    imagesEncodedList = new ArrayList<String>();
                    if (data.getData() != null) {

                        Uri mImageUri = data.getData();

                        // Get the cursor
                        Cursor cursor = getContentResolver().query(mImageUri,
                                filePathColumn, null, null, null);
                        // Move to first row
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        imageEncoded = cursor.getString(columnIndex);
                        cursor.close();

                    } else {
                        if (data.getClipData() != null) {
                            ClipData mClipData = data.getClipData();
                            ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                            for (int i = 0; i < mClipData.getItemCount(); i++) {

                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                mArrayUri.add(uri);
                                // Get the cursor
                                Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                                // Move to first row
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                imageEncoded = cursor.getString(columnIndex);
                                imagesEncodedList.add(imageEncoded);
                                cursor.close();
                            }
                            //displayInGallery(mArrayUri);
                            //trainAzureModel();
                            Log.v("NIKITA", "Selected Images main" + mArrayUri.size());
                        }
                    }
                } else {
                    Toast.makeText(this, "You haven't picked Image",
                            Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    public static String convertImageToString(Bitmap imageBitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(imageBitmap != null) {
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
            byte[] byteArray = stream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        }else{
            return null;
        }
    }

    private void sendImage(String image) {
        if (!isNetworkConnected()) {
            Toast.makeText(MainActivity.this, "Not connected to the internet", Toast.LENGTH_SHORT).show();
        }
        if (image == null) {
            Toast.makeText(MainActivity.this, "No Image loaded", Toast.LENGTH_SHORT).show();
        }

        ReqModel req = new ReqModel("image", image);

        API api = RetrofitClient.getInstance().getAPI();
        Call<ReqModel> upload = api.uploadImage(req);

        upload.enqueue(new Callback<ReqModel>() {
            @Override
            public void onResponse(Call<ReqModel> call, Response<ReqModel> response) {
                if(response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Checking for faces", Toast.LENGTH_SHORT).show();

                    ReqModel res = response.body();
                    if (res.getResult().size() > 0) {
                        Log.v("NIKITA", res.getResult().get(0).toString());
                    } else {
                        Log.v("NIKITA", "No person");
                    }
                    displayPerson(res.getResult());
                    //Log.v("REST226 - Result array", String.valueOf(res.getResult().get(1)) + ", " + String.valueOf(res.getResult().get(0)));
                }
            }

            @Override
            public void onFailure(Call<ReqModel> call, Throwable t) {
                Log.v("REST225", t.toString());
                Toast.makeText(MainActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
                displayPerson(new ArrayList<String>());
            }
        });
    }

    private void displayPerson(ArrayList<String> people) {
        welcomeTextView.setVisibility(View.GONE);
        String output = "";
        if (people.size() == 0) {
            displayPersonView.setVisibility(View.GONE);
            errorMessage.setVisibility(View.VISIBLE);

        } else {
            Log.v("NIKITA", people.get(0));
            Person data = dataManager.getPerson(people.get(0));

            if (data == null) {
                output = "No person detected from existing records, Go to ADMIN to add new person";
                errorMessage.setVisibility(View.VISIBLE);
                displayPersonView.setVisibility(View.GONE);
            }
            else {
                output = "Person detected: Name: " + data.getName() + " --- Contact no. : " + data.getRelationship();
                personId = data.getId();
                welcomeTextView.setVisibility(View.GONE);
                personNameView.setText(data.getName());
                personContactView.setText(data.getRelationship());
                errorMessage.setVisibility(View.GONE);
                displayPersonView.setVisibility(View.VISIBLE);
            }
        }
    }

//    private void sendImageToServer() {
//        if (!isNetworkConnected()) {
//            Toast.makeText(MainActivity.this, "Not connected to the internet", Toast.LENGTH_SHORT).show();
//        }
//        File imageFile = loadImageFromStorage(tempImagePath);
//
//        if (imageFile == null) {
//            Toast.makeText(MainActivity.this, "No Image loaded", Toast.LENGTH_SHORT).show();
//        }
//        RequestBody reqBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
//        MultipartBody.Part partImage = MultipartBody.Part.createFormData("file", "testImage", reqBody);
//        Log.v("SDFF", imageFile.getName().toString());
//        API api = RetrofitClient.getInstance().getAPI();
//        Call<TestResult> upload = api.uploadImage(partImage);
//        Log.v("REST", String.valueOf(upload));
//        upload.enqueue(new Callback<TestResult>() {
//            @Override
//            public void onResponse(Call<TestResult> call, Response<TestResult> response) {
//                if(response.isSuccessful()) {
//                    Toast.makeText(MainActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
//                    Log.v("REST22", new Gson().toJson(response.body()));
//                    TestResult res = response.body();
//                    String jsonRes = new Gson().toJson(response.body());
//
//                    String result = res.getResult();
//                    Log.v("REST22", result);
//                    //Intent main = new Intent(MainActivity.this, MainActivity.class);
//                    //main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    //startActivity(main);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<TestResult> call, Throwable t) {
//                Log.v("REST22", t.toString());
//                Toast.makeText(MainActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private File loadImageFromStorage(String path)
    {
        File f = null;

        try {
            f=new File(path, "profile.jpeg");

            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            imageCheck.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return f;
    }

    private String storeImage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpeg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            Log.v("Save", String.valueOf(mypath));
            Log.v("Save", String.valueOf(directory.getAbsolutePath()));
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        tempImagePath = directory.getAbsolutePath();
        return directory.getAbsolutePath();
    }

//    private void storeImage(Bitmap image, File imageFile) {
//        if (imageFile == null) {
//            Log.d("Save",
//                    "Error creating media file, check storage permissions: ");// e.getMessage());
//            return;
//        }
//        try {
//            //FileOutputStream fos = new FileOutputStream(imageFile);
//            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
//            fos.close();
//        } catch (FileNotFoundException e) {
//            Log.d("Save", "File not found: " + e.getMessage());
//        } catch (IOException e) {
//            Log.d("Save", "Error accessing file: " + e.getMessage());
//        }
//    }

//    private void saveFaceImage(Bitmap imageBitmap) {
//        outputDir = new File(Environment.getExternalStorageDirectory(), "FaceAI");
//        outputDir.mkdir();
//
//        //File filesDir = getFilesDir();
//        Log.v("snap", String.valueOf(outputDir.getAbsoluteFile()));
//        File imageFile = new File(outputDir.getAbsoluteFile(), "Temp-Face-File.jpg");
//
//        OutputStream os;
//        try {
//            asyncSaveSignature(getApplicationContext(), imageBitmap);
////            os = new FileOutputStream(imageFile);
////            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
////            os.flush();
////            os.close();
//        } catch (Exception e) {
//            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
//        }
//    }
//
//    private static void asyncSaveSignature(Context context, final Bitmap bitmap) {
//
//        final WeakReference<Context> contextWeakReference = new WeakReference<>(context.getApplicationContext());
//        context = null;
//        final File file = new File("Test-Face.jpg");
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                File tempSignature;
//
//                try {
//                    tempSignature = File.createTempFile("signature", ".png", file.getParentFile());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return;
//                }
//            }
//        });
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_admin:
                Intent adminIntent = new Intent(this, AdminActivity.class);
                startActivity(adminIntent);
                Log.v("Nikki", "admin worked");
                return true;

            case R.id.action_reset:
                Log.v("Nikki", "reset worked");
                new AlertDialog.Builder(MainActivity.this)
                        .setCancelable(false)
                        .setTitle("Remove Item")
                        .setMessage("Do you want to reset app?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                resetAll();
                            }
                        }).create().show();
                return true;

            case R.id.action_people:
                Intent peopleIntent = new Intent(this, PeopleActivity.class);
                startActivity(peopleIntent);
                return true;
        }
        return true;
    }

    private void resetAll() {
        ReqModel req = new ReqModel("reset", "all");
        API api = RetrofitClient.getInstance().getAPI();
        Call<ReqModel> reset = api.reset(req);
        reset.enqueue(new Callback<ReqModel>() {
            @Override
            public void onResponse(Call<ReqModel> call, Response<ReqModel> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Checking for faces", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReqModel> call, Throwable t) {
                Log.v("REST225", t.toString());
                Toast.makeText(MainActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void makeToast(final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean getPermissions() {
        boolean ans = false;
        Log.v("NIKITA", String.valueOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE));
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            makeToast("Write external storage permission is required for this");
            //ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.v("NIKITA", "Given");
        if (permissions.length != 0 && grantResults.length != 0) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    if (requestCode == 1) {
                        Log.v("NIKITA", "Persmission");
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    public void openGallery(View view) {
        int SELECT_PICTURE = 200;
        Intent i = new Intent(this, GalleryActivity.class);
        i.putExtra("personId", personId);
        startActivity(i);
    }

    // this function is triggered when user
    // selects the image from the imageChooser
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        if (requestCode == 200) {
//
//        }
//            else {
//
//            }
//
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }
}