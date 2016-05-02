package com.example.stanza;

//import android.app.LoaderManager;
//import android.app.Fragment;
//import android.app.LoaderManager;
//import android.content.CursorLoader;
//import android.app.LoaderManager;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
//import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
//import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

import at.markushi.ui.CircleButton;

/**
 * Created by Justin on 4/26/2016.
 */


public class UserPoemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EDITOR_REQUEST_CODE = 1001;
    private NotesCursorAdapter cursorAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_poem_fragment, container, false);

        cursorAdapter = new NotesCursorAdapter(getActivity(), null, 0);

        ListView list = (ListView) view.findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), EditorActivity.class);
                Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + id);
                intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });


        FloatingActionButton circleButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        circleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditorActivity.class);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });

        getLoaderManager().initLoader(0, null, this);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_userpoemfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_create_sample:
                insertSampleData();
                break;
            case R.id.action_delete_all:
                deleteAllNotes();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), NotesProvider.CONTENT_URI,
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            restartLoader();
        }
    }

    private void insertSampleData() {
        insertNote("Simple note");
        insertNote("Multi-line\nnote");
        insertNote("Very long note with a lot of text that exceeds the width of the screen");
        restartLoader();
    }
    private void deleteAllNotes() {

        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            //Insert Data management code here
                            getActivity().getContentResolver().delete(
                                    NotesProvider.CONTENT_URI, null, null
                            );
                            restartLoader();

                            Toast.makeText(getActivity(),
                                    getString(R.string.all_deleted),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }
    private void insertNote(String noteText) {
        ContentValues values;
        values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        Uri noteUri = getActivity().getContentResolver().insert(NotesProvider.CONTENT_URI,
                values);
        Log.d("MainActivity", "Inserted note " + noteUri.getLastPathSegment());
    }
}