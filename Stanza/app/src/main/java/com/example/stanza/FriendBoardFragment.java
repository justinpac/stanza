package com.example.stanza;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.Toast;

/**
 * Created by Brianna on 4/22/2016.
 */
public class FriendBoardFragment extends Fragment
implements LoaderManager.LoaderCallbacks<Cursor>, CommInterface
{
    private SwipeRefreshLayout swipeRefreshLayout;
    PoemRecyclerAdapter poemRecyclerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        System.out.println("creating friends");
        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        RecyclerView friendPoemRecycler = (RecyclerView) view.findViewById(R.id.friendPoemRecycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        friendPoemRecycler.setLayoutManager(linearLayoutManager);


        poemRecyclerAdapter = new PoemRecyclerAdapter(getActivity(), null);
        poemRecyclerAdapter.setOnItemClickListener(new PoemRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String pid) {
                Intent intent = new Intent(getActivity(), ViewPoemActivity.class);

                Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + pid);
                intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                startActivity(intent);
            }
        });
        friendPoemRecycler.setAdapter(poemRecyclerAdapter);

        getLoaderManager().initLoader(0, null, this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.friendSwipeLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary,R.color.fab_color);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullPoems();
                //new Handler().post(new CommThread(this,FriendBoardFragment.this));
            }
        });
        super.onActivityCreated(savedInstanceState);
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    public void pullPoems(){
        CommThread ct = new CommThread(this,FriendBoardFragment.this);
        ct.start();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = DBOpenHelper.CREATOR + " LIKE 'friend'";
        String orderBy = "friends";
        return new CursorLoader(getActivity(), NotesProvider.CONTENT_URI,
                null, selection, null, orderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        poemRecyclerAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        poemRecyclerAdapter.swapCursor(null);
    }


    @Override
    public void onResume() {
        System.out.println("open friends; call run ");
        restartLoader();
        super.onResume();
    }

    @Override
    public void poemSaved(String output) {
    }

    @Override
    public void serverDisconnected() {
        Toast.makeText(getActivity(), "Server not connected. Cannot retrieve poems from server.", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPullFinished() {
        swipeRefreshLayout.setRefreshing(false);
        restartLoader();
    }
}
