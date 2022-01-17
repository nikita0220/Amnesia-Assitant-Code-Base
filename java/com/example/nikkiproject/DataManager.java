package com.example.nikkiproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataManager {

    private static final String DATE_FORMAT = "yyyy/MM/dd kk:mm:ss";
    private File internalDir = null;
    private File databaseFile = null;
    private String DIR_NAME = "Person";
    private SQLiteDatabase mDatabase = null;
    private Context context = null;
    private File outputFile = null;
    private File outputDir = null;

    private String resultPersonId = null;

    public DataManager(Context context) {
        this.context = context;
    }

    public void initializeDb() {
        internalDir = new File(context.getFilesDir(), DIR_NAME);
        if (!internalDir.exists()) {
            internalDir.mkdir();
        }
        //databaseFile = new File(internalDir, Database.FILE_NAME);
        //databaseFile.setWritable(true);

        //outputDir = new File(Environment.getExternalStorageDirectory(), "Person_External");
        //outputDir.mkdirs();
        databaseFile = new File(internalDir, "test.db");

        File check = new File(internalDir, Database.FILE_NAME);
        if (check == null) {
            Log.v("NIKITA", "File not found");
        }
        else {
            Log.v("NIKITA", "file found" + String.valueOf(context.getFilesDir()));
        }
        mDatabase = SQLiteDatabase.openOrCreateDatabase(databaseFile, null);
        Database.PersonTable.create(mDatabase);
    }

    public boolean deletePerson(String personId) {
        Log.v("NIKITA-data", personId);
        boolean value = mDatabase.delete(Database.PersonTable.TABLE_NAME, "PERSON_ID = ?", new String[]{personId}) > 0;
        return value;
    }

    public boolean duplicateName(String name) {
        Cursor c = getItemCursor();
        if (c != null && c.getCount() > 0) {
            int nameIndex = c.getColumnIndex(Database.NAME);
            do {
                if (c.getString(nameIndex).equals(name)) {
                    return true;
                }
            } while (c.moveToNext());
        } else {
            return false;
        }
        return false;
    }


//    public String addPersonToAzure(String name) {
////        if (!isNetworkConnected()) {
////            Toast.makeText(MainActivity.this, "Not connected to the internet", Toast.LENGTH_SHORT).show();
//        if (name == null) {
//            resultPersonId = "-1";
//        }
//        Log.v("NIKITA", "data manager");
//
//        ReqModel req = new ReqModel("name", name);
//        API api = RetrofitClient.getInstance().getAPI();
//        Call<ReqModel> add = api.addPerson(req);
//
//        add.enqueue(new Callback<ReqModel>() {
//            @Override
//            public void onResponse(Call<ReqModel> call, Response<ReqModel> response) {
//                if(response.isSuccessful()) {
//                    resultPersonId = "-1";
//                    Log.v("NIKITA", "Came here");
//                    ReqModel res = response.body();
//                    resultPersonId = res.getPersonId();
//                    Log.v("NIKITA", "---" + String.valueOf(resultPersonId) + String.valueOf(response.body()));
//                    //Log.v("REST226 - Result array", String.valueOf(res.getResult().get(1)) + ", " + String.valueOf(res.getResult().get(0)));
//                }
//                else {
//                    Log.v("NIKITA", "Server Error");
//                    resultPersonId = "-1";
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ReqModel> call, Throwable t) {
//                Log.v("NIKITA", t.toString());
//                resultPersonId = "-1";
//            }
//        });
//        Log.v("NIKITA - here", resultPersonId);
//        return resultPersonId;
//    }

    public long addPerson(Person newPerson) {
        ContentValues person = new ContentValues();
        person.put(Database.PERSON_ID, newPerson.getId());
        person.put(Database.NAME, newPerson.getName());
        person.put(Database.CONTACT, newPerson.getRelationship());
        person.put(Database.DATE_TIME, String.valueOf(formatDate(System.currentTimeMillis())));

        long rowID = mDatabase.insert(Database.PersonTable.TABLE_NAME, null, person);
        return rowID;
    }

    public Person getPerson(String personId) {
        Person res = null;
            Cursor c = getItemCursor();
            if (c != null && c.getCount() > 0) {
                int index = c.getColumnIndex(Database.PERSON_ID);
                int nameIndex = c.getColumnIndex(Database.NAME);
                int contactIndex = c.getColumnIndex(Database.CONTACT);

                do {
                    if(personId.equals(c.getString(index))) {
                        res = new Person(personId, c.getString(nameIndex), c.getString(contactIndex));
                    }
                }while (c.moveToNext());

            } else {
                Log.v("NIKITA", "No person stored yet.");
                return null;
            }
            return res;
    }

    public ArrayList<Person> getAll() {
        ArrayList<Person> result = new ArrayList<Person>();
        Cursor c = getItemCursor();
        if (c != null && c.getCount() > 0) {
            int idIndex = c.getColumnIndex(Database.PERSON_ID);
            int nameIndex = c.getColumnIndex(Database.NAME);
            int contactIndex = c.getColumnIndex(Database.CONTACT);

            do {
                Log.v("NIKITA-data",  c.getString(idIndex) + " -- " + c.getString(nameIndex));
                Person temp = new Person(c.getString(idIndex), c.getString(nameIndex), c.getString(contactIndex));
                result.add(temp);
            } while(c.moveToNext());
        } else {
            Log.v("NIKITA", "No person stored.");
        }
        return result;
    }


    public Cursor getItemCursor() {
        Cursor c = mDatabase.rawQuery("SELECT * FROM " + Database.PersonTable.TABLE_NAME, null);
        c.moveToFirst();
        return c;
    }

    private CharSequence formatDate(long millis) {
        return DateFormat.format(DATE_FORMAT, millis).toString();
    }
}
