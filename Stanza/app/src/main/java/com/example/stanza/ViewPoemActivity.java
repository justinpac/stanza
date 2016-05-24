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

    private TextView title, text, author;
    String poemText, poemTitle, poemAuthor, noteFilter;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);
       // long id = intent.getLongExtra(NotesProvider.CONTENT_ITEM_TYPE, -1);


        title = (TextView) findViewById(R.id.friend_poem_title);
        text = (TextView) findViewById(R.id.friend_poem_text);
        author = (TextView) findViewById(R.id.friend_poem_author);


        String path = uri.toString();
        String idStr = path.substring(path.lastIndexOf('/') + 1);
        noteFilter = DBOpenHelper.POEM_ID + "=" + idStr;

        String sortOrder = "friend";

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
