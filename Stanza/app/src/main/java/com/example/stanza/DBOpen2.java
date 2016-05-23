package com.example.stanza;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpen2 extends SQLiteOpenHelper{

    //Constants for db name and version
    private static final String DATABASE_NAME = "friends.db";
    private static final int DATABASE_VERSION = 14;

    //Constants for identifying table and columns
    public static final String TABLE_FRIENDS = "myFriends";
    public static final String FRIEND = "friend";

    public static final String[] ALL_COLUMNS = {FRIEND};

    //SQL to create table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_FRIENDS + " (" + FRIEND + " TEXT)";

    public DBOpen2(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        onCreate(db);
    }
}
