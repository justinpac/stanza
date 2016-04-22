package com.example.stanza;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Brianna on 4/22/2016.
 */
public class ViewPoemActivity extends AppCompatActivity{

    private TextView title, text;
    String poemText, poemTitle, noteFilter;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);


        title = (TextView) findViewById(R.id.friend_poem_title);
        text = (TextView) findViewById(R.id.friend_poem_text);

        noteFilter = DBOpenHelper.POEM_ID + "=" + uri.getLastPathSegment();


        Cursor cursor = getContentResolver().query(uri, DBOpenHelper.ALL_COLUMNS
                , noteFilter, null, null);
        cursor.moveToFirst();
        poemText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.POEM_TEXT));
        poemTitle = cursor.getString(cursor.getColumnIndex(DBOpenHelper.POEM_TITLE));

        title.setText(poemTitle);
        title.requestFocus();
        text.setText(poemText);
        text.requestFocus();
    }


}
