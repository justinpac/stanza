package com.example.stanza;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class NotesProvider2 extends ContentProvider{

    private static final String AUTHORITY = "com.example.stanza.notesprovider2";
    //private static final String BASE_PATH = "notes";
    private static final String BASE_PATH = "friendsLocal";
    public static Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );

    // Constant to identify the requested operation
    private static final int FRIENDS = 1;   //get data
    private static final int FRIENDS_ID = 2;  //deals with only single record

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    public static final String CONTENT_ITEM_TYPE = "Poem";

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, FRIENDS);
        uriMatcher.addURI(AUTHORITY, BASE_PATH +  "/#", FRIENDS_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {

        FriendDPOpenHelper helper = new FriendDPOpenHelper(getContext());
        database = helper.getWritableDatabase();
       // CONTENT_URI = CONTENT_URI.buildUpon().appendQueryParameter("limit", "1000").build();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

       // System.out.println("pre selection adjust is " + selection);


       // System.out.println("post selection adjust is " + selection);


        return database.query(FriendDPOpenHelper.TABLE_FRIENDS, FriendDPOpenHelper.ALL_COLUMNS,
                        selection, null, null, null,
                        FriendDPOpenHelper.FRIEND_CREATED + " DESC");




    }


    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = database.insert(FriendDPOpenHelper.TABLE_FRIENDS,
                null, values);
        Uri result;

    //    System.out.println(id);

       // System.out.println(Uri.parse(BASE_PATH + "/" + id));
        result = Uri.parse(BASE_PATH + "/" + id);
      //  System.out.println("in insert " + result.getLastPathSegment());
        return result;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.delete(FriendDPOpenHelper.TABLE_FRIENDS, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //selection = "_id=" + uri.getLastPathSegment();
        //System.out.println("update selection is " + selection);
        return database.update(FriendDPOpenHelper.TABLE_FRIENDS,
                values, selection, selectionArgs);
    }
}
