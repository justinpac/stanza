package com.example.stanza;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper{

    //Constants for db name and version
    private static final String DATABASE_NAME = "poems.db";
    private static final int DATABASE_VERSION = 12;

    //Constants for identifying table and columns
    public static final String TABLE_POEMS = "poemsLocal";
    public static final String POEM_ID = "_id";
    public static final String POEM_TEXT = "poemText";
    public static final String POEM_CREATED = "poemCreated";
    public static final String POEM_TITLE = "poemTitle";
    public static final String CREATOR = "creator";

    public static final String[] ALL_COLUMNS =
            {POEM_ID, POEM_TITLE, POEM_TEXT, CREATOR, POEM_CREATED};

    //SQL to create table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_POEMS + " (" +
                    POEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    POEM_TITLE + " TEXT, " +
                    POEM_TEXT + " TEXT, " +
                    CREATOR + " TEXT, " +
                    POEM_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POEMS);
        onCreate(db);
    }
}
