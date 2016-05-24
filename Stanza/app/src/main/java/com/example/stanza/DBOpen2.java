package com.example.stanza;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * A class for friendslist management with the database
 */
public class DBOpen2 extends SQLiteOpenHelper{

    //class poems
    //Constants for db name and version
    /**
     * a String holding the name of the database in <code>DBOpen2</code>
     */
    private static final String DATABASE_NAME = "friends.db";
    /**
     * a integer holding the current iteration of the database in <code>DBOpen2</code>
     */
    private static final int DATABASE_VERSION = 20;

    //Constants for identifying table and columns
    /**
     * A String holding the name of the table for friends in <code>DBOpen2</code>
     */
    public static final String TABLE_FRIENDS = "myFriends";
    /**
     * A string marking the column for friend in the table in <code>DBOpen2</code>
     */
    public static final String FRIEND = "friend";
    /**
     * A string for the column FRIEND_ID in the friend table in <code>DBOpen2</code>
     */
    public static final String FRIEND_ID = "_id";

    /**
     * A String holding all of the columns of the table in <code>DBOpen2</code>
     */
    public static final String[] ALL_COLUMNS = {FRIEND_ID, FRIEND};

    //SQL to create table
    /**
     * A SQL statement held in a string to create a SQL table in <code>DBOpen2</code>
     */
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_FRIENDS + " (" +
                    FRIEND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FRIEND + " TEXT" + " UNIQUE" +
                    ")";

    // constructors

    /**
     * a constructor that creates the class with the context
     * @param context the context for the database
     */
    public DBOpen2(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     *A method that creates the table in the database
     * @param db the database in which we create the table
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    /**
     * A method that deletes the old table and creates a new one if we upgrade the database
     * @param db the database holding the table
     * @param oldVersion the current, possibly old version of the database
     * @param newVersion the current version of the database, if newer the table is recreated
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        onCreate(db);
    }
}
