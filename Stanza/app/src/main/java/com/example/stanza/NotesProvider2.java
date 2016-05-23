package com.example.stanza;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class NotesProvider2 extends ContentProvider{

    private static final String AUTHORITY = "com.example.stanza.notesprovider2";
    private static final String BASE_PATH = "friends";
    public static Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );

    // Constant to identify the requested operation
    private static final int NOTES = 1;   //get data
    private static final int NOTES_ID = 2;  //deals with only single record

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    public static final String CONTENT_ITEM_TYPE = "Poem";

   static {
       uriMatcher.addURI(AUTHORITY, BASE_PATH, NOTES);
       uriMatcher.addURI(AUTHORITY, BASE_PATH +  "/#", NOTES_ID);
   }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {

        DBOpen2 helper = new DBOpen2(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        return database.query(DBOpen2.TABLE_FRIENDS, DBOpen2.ALL_COLUMNS,
                selection, null, null, null, null);
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = database.insert(DBOpen2.TABLE_FRIENDS,
                null, values);
        Uri result;

        result = Uri.parse(BASE_PATH + "/" + id);
        return result;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.delete(DBOpen2.TABLE_FRIENDS, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return database.update(DBOpen2.TABLE_FRIENDS,
                values, selection, selectionArgs);
    }
}
