package com.example.stanza;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

/**
 * A class for retrieving the notes from our table in the database
 */
public class NotesProvider extends ContentProvider{
    // class variables
    /**
     * The program requesting the notes. Ensures correct permissions such that only this program
     *      will access the table in <code>NotesProvider</code>
     */
    private static final String AUTHORITY = "com.example.stanza.notesprovider";
    /**
     * The table we are looking at is the local database holding the poems on the device in <code>NotesProvider</code>
     */
    private static final String BASE_PATH = "poemsLocal";
    /**
     * Constructs the Uri using AUTHORITY and BASE_PATH in <code>NotesProvider</code>
     */
    public static Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );

    /**
     * A constant used to identify the requested operation in <code>NotesProvider</code>
     */
    private static final int NOTES = 1;   //get data
    /**
     * A constant used to identify the requested operation in <code>NotesProvider</code>
     */
    private static final int NOTES_ID = 2;  //deals with only single record

    /**
     * A constant used to match Uri's in <code>NotesProvider</code>
     */
    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * A constant used to identify the content type in <code>NotesProvider</code>
     */
    public static final String CONTENT_ITEM_TYPE = "Poem";

    /**
     * Updates uriMatcher with arguments in <code>NotesProvider</code>
     */
    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, NOTES);
        uriMatcher.addURI(AUTHORITY, BASE_PATH +  "/#", NOTES_ID);
    }

    /**
     * The database which we are using to store our table in <code>NotesProvider</code>
     */
    private SQLiteDatabase database;

    //methods

    /**
     *  Gets the context and hence correct table with our information
     * @return true
     */
    @Override
    public boolean onCreate() {

        DBOpenHelper helper = new DBOpenHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    /**
     * Allows for dynamic access to the database based on cursor position.
     * @param uri identifies the table.
     * @param projection The columns of the database we would like returned
     * @param selection The selection clause of the query
     * @param selectionArgs The selection arguments of the query
     * @param sortOrder How we want the return to be sorted
     * @return The database query results
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {


        if (sortOrder.equals("self")) {
            return database.query(DBOpenHelper.TABLE_POEMS, DBOpenHelper.ALL_COLUMNS,
                    selection, null, null, null, DBOpenHelper.POEM_CREATED + " DESC");
        } else {
            return database.query(DBOpenHelper.TABLE_POEMS, DBOpenHelper.ALL_COLUMNS,
                    selection, null, null, null, DBOpenHelper.POEM_CREATED + " ASC LIMIT 10");
        }
    }

    /**
     * A required method for making a notesProvider, does nothing functionally.
     * @param uri identifies the table
     * @return null
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }

    /**
     * What we want to insert into the database
     * @param uri identifies the table
     * @param values What is being inserted into the table
     * @return the resultant table identification after insertion
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = database.insert(DBOpenHelper.TABLE_POEMS,
                null, values);
        Uri result;
        result = Uri.parse(BASE_PATH + "/" + id);
        return result;

    }

    /**
     * Delete elements from the table
     * @param uri the table identifier
     * @param selection The selected element(s) to delete
     * @param selectionArgs Arguments wanted in the deletion process
     * @return The database with the deleted element(s)
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.delete(DBOpenHelper.TABLE_POEMS, selection, selectionArgs);
    }

    /**
     * Updates the table after some change
     * @param uri The table identifier
     * @param values The values to be updated into the table.
     * @param selection The selection to be updated.
     * @param selectionArgs Possible args that change how we update.
     * @return the database after the update operation.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return database.update(DBOpenHelper.TABLE_POEMS,
                values, selection, selectionArgs);
    }
}
