package com.example.stanza;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/** A class for viewing poems without editing them.
 * @author Brianna, 4/22/2016.
 */
public class ViewPoemActivity extends AppCompatActivity{

    // state variables
    /**
     * TextViews which hold the poem's title and text, respectively.
     */
    private TextView title, text;
    /**
     * Strings which hold the values of the poem's text and title, as well as
     * <code>noteFilter</code>, which is used to pull the proper values
     * from the database.
     */
    String poemText, poemTitle, noteFilter;

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
        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);
       // long id = intent.getLongExtra(NotesProvider.CONTENT_ITEM_TYPE, -1);


        title = (TextView) findViewById(R.id.friend_poem_title);
        text = (TextView) findViewById(R.id.friend_poem_text);


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
        }catch(RuntimeException e){
            e.printStackTrace();
        }
        title.setText(poemTitle);
        title.requestFocus();
        text.setText(poemText);
        text.requestFocus();
    }


}
