package com.example.stanza;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by Brianna on 4/22/2016.
 */
public class FriendBoardActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor>
{

    private NotesCursorAdapter cursorAdapter;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        Intent intent = getIntent();

        cursorAdapter = new NotesCursorAdapter(this, null, 0);

        ListView list = (ListView) findViewById(R.id.listFriends);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(FriendBoardActivity.this, ViewPoemActivity.class);
                intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, id);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(0, null, this);

    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this, NotesProvider.CONTENT_URI2,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

}
