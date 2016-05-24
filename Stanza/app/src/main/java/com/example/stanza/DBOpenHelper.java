package com.example.stanza;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * A class for creating a database and storing necessary info.
 */
public class DBOpenHelper extends SQLiteOpenHelper{
    // class variables
    /**
     * Name of our SQL database.
     */
    private static final String DATABASE_NAME = "poems.db";
    /**
     * current iteration of our database.
     */
    private static final int DATABASE_VERSION = 14;

    //Constants for identifying table and columns
    /**
     * The local table, this is filled by poems held on the device.
     */
    public static final String TABLE_POEMS = "poemsLocal";
    /**
     * A primary key for unique records. No two records will have the same id.
     */
    public static final String POEM_ID = "_id";
    /**
     * The
     */
    public static final String POEM_TEXT = "poemText";
    /**
     * The date of the poem's creation.
     */
    public static final String POEM_CREATED = "poemCreated";
    /**
     * The title of the poem.
     */
    public static final String POEM_TITLE = "poemTitle";
    /**
     * The user that created the poem. Either self (created by user) or friend (created by friend)
     */
    public static final String CREATOR = "creator"; //'self' = created by user, 'friend' = created by friend
    /**
     * holds all columns that will be present in the database as seen above.
     */
    public static final String[] ALL_COLUMNS =
            {POEM_ID, POEM_TITLE, POEM_TEXT, CREATOR, POEM_CREATED};

    /**
     * SQL to create table, creates the table in the database.
     */
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_POEMS + " (" +
                    POEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    POEM_TITLE + " TEXT, " +
                    POEM_TEXT + " TEXT, " +
                    CREATOR + " TEXT, " +
                    POEM_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";
    // constructor
    /** Creates the SQL database????
     * @param context  The context passed to the table
     */
    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //methods
    /** Creates the SQL table in the database
     * @param db The database which will contain our table
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    /**
     * When upgrading our database by incrementing @param DATABASE_VERSION,
     *      this will delete the current table and create a new one.
     * @param db The database that contains our table
     * @param oldVersion An integer indicating the database version of the old database
     * @param newVersion An integer indicating the database version of the current database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POEMS);
        onCreate(db);
    }
}
