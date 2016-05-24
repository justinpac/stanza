package com.example.stanza;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/** A class for viewing poems without editing them.
 */
public class ViewPoemActivity extends AppCompatActivity{

    // state variables
    /**
     * TextViews which hold the poem's title and text, respectively.
     */
    private TextView title, text, author;
    /**
     * Strings which hold the values of the poem's text and title, as well as
     * <code>noteFilter</code>, which is used to pull the proper values
     * from the database.
     */
    String poemText, poemTitle, poemAuthor, noteFilter;

    /**
     * Called when the view is created. Populates the <code>title</code> and
     * <code>text</code> <code>TextViews</code> with the proper poem's values,
     * pulled from the local database.
     * @param savedInstanceState Used to run Android's onCreate in addition
     *                           to ours.
     */
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        /**
         * Holds the intent that started this activity.
         */
        Intent intent = getIntent();
        /**
         * Sets up the uri for this activity to access the local database.
         */
        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);
       // long id = intent.getLongExtra(NotesProvider.CONTENT_ITEM_TYPE, -1);


        title = (TextView) findViewById(R.id.friend_poem_title);
        text = (TextView) findViewById(R.id.friend_poem_text);
        author = (TextView) findViewById(R.id.friend_poem_author);

        /**
         * Holds the string value of the uri
         */
        String path = uri.toString();
        String idStr = path.substring(path.lastIndexOf('/') + 1);
        noteFilter = DBOpenHelper.POEM_ID + "=" + idStr;

        /**
         * Sorts the existing poems by "friend"; only shows friend poems
         */
        String sortOrder = "friend";

        /**
         * Queries the local database for the proper poem, matched by id
         */
        Cursor cursor = getContentResolver().query(uri, DBOpenHelper.ALL_COLUMNS
                , noteFilter, null, sortOrder);
        try {
            cursor.moveToFirst();
            poemText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.POEM_TEXT));
            poemTitle = cursor.getString(cursor.getColumnIndex(DBOpenHelper.POEM_TITLE));
            poemAuthor = cursor.getString(cursor.getColumnIndex(DBOpenHelper.CREATOR));
        }catch(RuntimeException e){
            e.printStackTrace();
        }
        title.setText(poemTitle);
        title.requestFocus();
        text.setText(poemText);
        text.requestFocus();
        author.setText(poemAuthor);
        author.requestFocus();
    }


}
