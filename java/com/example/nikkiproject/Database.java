package com.example.nikkiproject;

import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

public class Database {
    public static final String FILE_NAME = "userData_test.db";
    public static final String PERSON_ID = "person_id";
    public static final String NAME = "name";
    public static final String CONTACT = "contact";
    public static final String DATE_TIME = "datetime";

    public static class PersonTable {
        public static final String TABLE_NAME = "person_test";

        static public void create(SQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + PERSON_ID + " TEXT PRIMARY KEY, " + NAME + " TEXT NOT NULL, " + CONTACT + " TEXT NOT NULL, " + DATE_TIME + " TEXT NOT NULL)");
            //database.execSQL("CREATE INDEX IF NOT EXISTS " + PERSON_ID + " ON " + NAME + " ( " + BARCODE + " );");
        }

        public class Keys {
            public static final String PERSON_ID = TABLE_NAME + '.' + Database.PERSON_ID;
            public static final String NAME = TABLE_NAME + '.' + Database.NAME;
            public static final String CONTACT = TABLE_NAME + '.' + Database.CONTACT;
            public static final String DATE_TIME = TABLE_NAME + '.' + Database.DATE_TIME;
        }
    }

}
