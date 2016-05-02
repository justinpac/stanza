package com.example.stanza;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

public class NotesProvider extends ContentProvider{

    private static final String AUTHORITY = "com.example.stanza.notesprovider";
    //private static final String BASE_PATH = "notes";
    private static final String BASE_PATH = "poemsLocal";
    public static Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );
    public static Uri CONTENT_URI2 =
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

        DBOpenHelper helper = new DBOpenHelper(getContext());
        database = helper.getWritableDatabase();
        CONTENT_URI = CONTENT_URI.buildUpon().appendQueryParameter("limit", "1000").build();
        CONTENT_URI2 = CONTENT_URI2.buildUpon().appendQueryParameter("limit", "10").build();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

       // System.out.println("pre selection adjust is " + selection);


       // System.out.println("post selection adjust is " + selection);


        String limit = uri.getQueryParameter("limit");

            if(limit.equals("10")) {
                limit = "LIMIT " + limit;

                return database.query(DBOpenHelper.TABLE_POEMS, DBOpenHelper.ALL_COLUMNS,
                        selection, null, null, null,
                        DBOpenHelper.POEM_CREATED + " DESC " + limit);
            }
            else{
                return database.query(DBOpenHelper.TABLE_POEMS, DBOpenHelper.ALL_COLUMNS,
                        selection, null, null, null,
                        DBOpenHelper.POEM_CREATED + " DESC");

            }


    }


    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = database.insert(DBOpenHelper.TABLE_POEMS,
                null, values);
        Uri result;

        System.out.println(id);

        System.out.println(Uri.parse(BASE_PATH + "/" + id));
        result = Uri.parse(BASE_PATH + "/" + id);
        System.out.println("in insert " + result.getLastPathSegment());
        return result;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.delete(DBOpenHelper.TABLE_POEMS, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //selection = "_id=" + uri.getLastPathSegment();
        System.out.println("update selection is " + selection);
        return database.update(DBOpenHelper.TABLE_POEMS,
                values, selection, selectionArgs);
    }
}
