package com.example.stanza;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Brianna on 5/2/2016.
 */
public class FriendDPOpenHelper extends SQLiteOpenHelper {

    //Constants for db name and version
    private static final String DATABASE_NAME = "friends.db";
    private static final int DATABASE_VERSION = 11;

    //Constants for identifying table and columns
    public static final String TABLE_FRIENDS = "friendPoemsLocal";
    public static final String FRIEND_ID = "_id";
    public static final String FRIEND_TEXT = "friendText";
    public static final String FRIEND_TITLE = "friendTitle";
    public static final String FRIEND_CREATED = "friendCreated";


    public static final String[] ALL_COLUMNS =
            {FRIEND_ID, FRIEND_TITLE, FRIEND_TEXT};

    //SQL to create table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_FRIENDS + " (" +
                    FRIEND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FRIEND_TITLE + " TEXT, " +
                    FRIEND_TEXT + " TEXT, " +
                    FRIEND_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";


    public FriendDPOpenHelper(Context context) {
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
