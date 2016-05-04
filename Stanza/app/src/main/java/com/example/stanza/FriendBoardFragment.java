package com.example.stanza;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

/**
 * Created by Brianna on 4/22/2016.
 */
public class FriendBoardFragment extends Fragment
implements LoaderManager.LoaderCallbacks<Cursor>, CommInterface
{

    private NotesCursorAdapter cursorAdapter;
    public CommThread ct;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
       // ct = new CommThread(this,FriendBoardFragment.this);
       // ct.start();
        System.out.println("on create method");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        System.out.println("creating friends");
        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        cursorAdapter = new NotesCursorAdapter(getActivity(), null, 0);

        ListView list = (ListView) view.findViewById(R.id.listFriends);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ViewPoemActivity.class);
              //  intent.putExtra(NotesProvider2.CONTENT_ITEM_TYPE, id);

                Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + id);
                intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                startActivity(intent);
            }
        });


        getLoaderManager().initLoader(0, null, this);

        return view;
    }



    private void restartLoader() {
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
    public void onResume() {
        System.out.println("open friends; call run ");
        restartLoader();
        super.onResume();
    }

    @Override
    public void pushPoem(String poemTitle, String poemText) {
    }

    @Override
    public void pullPoem() {
        System.out.println("pull poem method");
    }

    @Override
    public void poemSaved(String output) {
    }

    @Override
    public void serverDisconnected() {
    }
}
