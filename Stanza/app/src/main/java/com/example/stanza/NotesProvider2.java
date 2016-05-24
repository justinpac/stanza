package com.example.stanza;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * This is the ContentProvider for the myFriends table in the friends database (the local database
 * that stores who the friends of this user are.
 */

public class NotesProvider2 extends ContentProvider{

    /**
     * The authority needed to access the database.
     */
    private static final String AUTHORITY = "com.example.stanza.notesprovider2";

    /**
     * The table within the database.
     */
    private static final String BASE_PATH = "myFriends";

    /**
     * The complete uri to access the myFriends table in the friends database.
     */
    public static Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );

    /**
     * An instance of SQLiteDatabase in order to access the myFriends table within the
     * friends database.
     */
    private SQLiteDatabase database;

    /**
     * Initialize the instance of SQLiteDatabase with the friends database in DBOpen2.
     * @return true when created.
     */
    @Override
    public boolean onCreate() {

        DBOpen2 helper = new DBOpen2(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    /**
     * Query the myFriends table and return all friends that exist in the local database.
     * @param uri The uri used to access the table in the database.
     * @param projection What columns we want returned in the query.
     * @param selection The selection clause.
     * @param selectionArgs The selection arguments.
     * @param sortOrder The order the query results should be returned in.
     * @return A cursor containing the records that satisfy the query
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        return database.query(DBOpen2.TABLE_FRIENDS, DBOpen2.ALL_COLUMNS,
                null, null, null, null, null);
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
     * Insert a friend record into the myFriends table.
     * @param uri The uri to access the myFriends table.
     * @param values The values (friend and _id) to be inserted into the table
     * @return The uri of the inserted friend.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = database.insert(DBOpen2.TABLE_FRIENDS,
                null, values);
        Uri result;

        result = Uri.parse(BASE_PATH + "/" + id);
        return result;

    }

    /**
     * Delete friend records from the myFriends table.
     * @param uri The uri to access the myFriends table
     * @param selection The selection clause of the query
     * @param selectionArgs The selection arguments of the query
     * @return The number of records deleted
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.delete(DBOpen2.TABLE_FRIENDS, selection, selectionArgs);
    }

    /**
     * Update the records within the myFriends table.
     * @param uri The uri to access the myFriends table
     * @param values The values being updated in the records.
     * @param selection The selection clause of the query.
     * @param selectionArgs The selection arguments of the query
     * @return the number of records updated
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return database.update(DBOpen2.TABLE_FRIENDS,
                values, selection, selectionArgs);
    }
}
