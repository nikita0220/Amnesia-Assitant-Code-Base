package com.example.nikkiproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPerson extends AppCompatActivity {

    private static final int SELECT_PICTURE = 200;
    private ImageSwitcher IVPreviewImage;
    private TextView userMessageView = null;
    private Button savePersonButton = null;
    private DataManager dataManager = null;
    private Button addPicturesButton = null;
    private TextView successMessage = null;
    private LinearLayout formView = null;
    private EditText nameView = null;
    private EditText contactView = null;
    private ArrayList<String> imagesEncodedList = null;
    private String currentPersonId = "-1";
    private LinearLayout nextForm = null;

    private int MEMORIES_REQUEST = 300;
    private int ADD_PICTURES_REQUEST = 200;
    private String DIR_NAME = "Person";

    private ArrayList<String> imageList = null;
    private int imageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);
        savePersonButton = findViewById(R.id.save_person_new);
        nameView = findViewById(R.id.addperson_name);
        contactView = findViewById(R.id.addperson_contact);
        addPicturesButton = findViewById(R.id.add_pictures_new);
        formView = findViewById(R.id.add_person_form);
        successMessage = findViewById(R.id.add_person_success_message);
        nextForm = findViewById(R.id.add_person_next_form);

        addPicturesButton.setVisibility(View.GONE);
        successMessage.setVisibility(View.GONE);


        dataManager = new DataManager(getApplicationContext());
        dataManager.initializeDb();
    }

    private void goBack() {
        super.finish();
    }

    public void takePicture(View view) {
        imageChooser();
    }

    private void sendToServer() {
        String name = nameView.getText().toString();
        String contact = contactView.getText().toString();
        Log.v("NIKITA", "Here");
        ReqModel req = new ReqModel("name", name);
        API api = RetrofitClient.getInstance().getAPI();
        Call<ReqModel> add = api.addPerson(req);

        add.enqueue(new Callback<ReqModel>() {
            @Override
            public void onResponse(Call<ReqModel> call, Response<ReqModel> response) {
                if(response.isSuccessful()) {
                    Toast.makeText(AddPerson.this, "Added User", Toast.LENGTH_SHORT).show();
                    ReqModel res = response.body();
                    onAddPerson(res.getPersonId(), name, contact);
                }
            }

            @Override
            public void onFailure(Call<ReqModel> call, Throwable t) {
                Log.v("REST225", t.toString());
                Toast.makeText(AddPerson.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void savePerson(View view) {
        sendToServer();
    }

    public void addMemories (View view) {
        Intent i = new Intent();
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        i.setType("image/*");i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"),MEMORIES_REQUEST);
    }

    private void addMemoriesToStorage(ArrayList<Uri> uriList) throws FileNotFoundException {
        Log.v("NIKITA", String.valueOf(uriList.size()));
        String personFolderName = "Images-" + currentPersonId;
        File internalDir = new File(getApplicationContext().getFilesDir(), DIR_NAME);
        File userFolder = new File(internalDir, personFolderName);

        userFolder.mkdir();

        int memoryNumber = 0;

        for (Uri uriItem : uriList) {
            InputStream input = getApplicationContext().getContentResolver().openInputStream(uriItem);
            Bitmap bMap = BitmapFactory.decodeStream(input);
            File imageFile = new File(userFolder, "Image-" + String.valueOf(memoryNumber));
            Log.v("NIKITA-picked image", imageFile.getPath());

            try {
                FileOutputStream out = new FileOutputStream(imageFile);
                bMap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                memoryNumber += 1;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.v("NIKITA", String.valueOf(userFolder.exists()) + userFolder.getPath() +"---"+ String.valueOf(userFolder.listFiles().length));
        goBack();
    }

    public void onAddPerson(String personId, String name, String contact) {
        if (dataManager.duplicateName(name)) {
            Toast.makeText(this, "Duplicate name, change name to continue.", Toast.LENGTH_LONG).show();
            return;
        }
        Log.v("NIKITA", personId);
        if (!personId.equals("-1")) {
            Person newPerson = new Person(personId, name, contact);
            dataManager.addPerson(newPerson);
            addPicturesButton.setVisibility(View.VISIBLE);
            savePersonButton.setVisibility(View.GONE);
            formView.setVisibility(View.GONE);
            nextForm.setVisibility(View.VISIBLE);
            successMessage.setVisibility(View.VISIBLE);
            currentPersonId = personId;
        }
        else {
            Toast.makeText(this, "Error adding name", Toast.LENGTH_LONG).show();
            return;
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

    public void sendImageToServer(ArrayList<Uri> imageUriList) throws IOException {
        imageList = new ArrayList<>();
        if (imageUriList.size() == 0) {
            Log.v("NIKITA", "No image loaded");
            return;
        }
        for (Uri item : imageUriList) {
            Log.v("NIKITA", item.toString());
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), item);
            String temp_base64 = convertImageToString(bitmap);
            imageList.add(temp_base64);

            //sendImageToAzure(temp_base64);
        }
        Log.v("NIKITA", "Triggering Async Save");
        imageCount = imageList.size();
        sendImageToAzure(imageList.get(0), 0);
        //new sendImageAsync().execute(imageList);
    }

    private class sendImageAsync extends AsyncTask<ArrayList<String>, Void, Void> {

        @Override
        protected Void doInBackground(ArrayList<String>... params) {
            Log.v("NIKITA", "Starting to send to server");
            for (String image : params[0]) {
                //sendImageToAzure(image);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.v("NIKITA", "Uploaded all images");
            trainAzureModel();
        }
    }

    private void trainAzureModel() {
        Log.v("NIKITA", "Train azure");
        if (!isNetworkConnected()) {
            Toast.makeText(AddPerson.this, "Not connected to the internet", Toast.LENGTH_SHORT).show();
        }

        ReqModel req = new ReqModel("log", "Training");
        API api = RetrofitClient.getInstance().getAPI();
        Call<ReqModel> train = api.trainModel(req);

        train.enqueue(new Callback<ReqModel>() {
            @Override
            public void onResponse(Call<ReqModel> call, Response<ReqModel> response) {
                if(response.isSuccessful()) {
                    Log.v("NIKITA", "Training");
                    //goBack();
                }
            }
            @Override
            public void onFailure(Call<ReqModel> call, Throwable t) {
                Log.v("REST225", t.toString());
                Toast.makeText(AddPerson.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendImageToAzure(String image, int itemCount) {
        Log.v("NIKITA", "Send azure");
        if (!isNetworkConnected()) {
            Toast.makeText(AddPerson.this, "Not connected to the internet", Toast.LENGTH_SHORT).show();
        }
        if (image == null) {
            Toast.makeText(AddPerson.this, "No Image loaded", Toast.LENGTH_SHORT).show();
        }

        if (itemCount == imageCount) {
            trainAzureModel();
        }
        else {
            ReqModel req = new ReqModel("image", image, currentPersonId);
            API api = RetrofitClient.getInstance().getAPI();
            Call<ReqModel> add = api.addFace(req);

            add.enqueue(new Callback<ReqModel>() {
                @Override
                public void onResponse(Call<ReqModel> call, Response<ReqModel> response) {
                    if(response.isSuccessful()) {
                        Log.v("NIKITA", "Uploaded" + String.valueOf(itemCount));
                        sendImageToAzure(imageList.get(itemCount), itemCount+1);
                        //Toast.makeText(AddPerson.this, "Uploading", Toast.LENGTH_SHORT).show();
                        //ReqModel res = response.body();
                        //displayPerson(res.getResult());
                        //Log.v("REST226 - Result array", String.valueOf(res.getResult().get(1)) + ", " + String.valueOf(res.getResult().get(0)));
                    }
                }
                @Override
                public void onFailure(Call<ReqModel> call, Throwable t) {
                    Log.v("REST225", t.toString());
                    sendImageToAzure(imageList.get(itemCount), itemCount+1);
                    Toast.makeText(AddPerson.this, t.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

//        private void sendBitmapToServer(File image, int itemCount) {
//        if (!isNetworkConnected()) {
//            Toast.makeText(MainActivity.this, "Not connected to the internet", Toast.LENGTH_SHORT).show();
//        }
//
//        RequestBody reqBody = RequestBody.create(MediaType.parse("image/jpeg"), image);
//        MultipartBody.Part partImage = MultipartBody.Part.createFormData("file", "testImage", reqBody);
//        //Log.v("SDFF", "");
//        API api = RetrofitClient.getInstance().getAPI();
//        Call<TestResult> upload = api.uploadBitmap(partImage);
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

    public void imageChooser() {
        Intent i = new Intent();
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        i.setType("image/*");i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // this function is triggered when user
    // selects the image from the imageChooser
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String imageEncoded = null;
        try {
            if (resultCode == RESULT_OK
                    && null != data) {

                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                imagesEncodedList = new ArrayList<String>();
                if(data.getData()!=null){

                    Uri mImageUri=data.getData();
                    Cursor cursor = getContentResolver().query(mImageUri, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded  = cursor.getString(columnIndex);
                    cursor.close();

                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded  = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncoded);
                            cursor.close();
                        }

                        if (requestCode == MEMORIES_REQUEST) {
                            addMemoriesToStorage(mArrayUri);
                        }
                        else {
                            sendImageToServer(mArrayUri);
                        }
                        Log.v("NIKITA", "Selected Images" + mArrayUri.size());
                    }
                }
            } else {
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}