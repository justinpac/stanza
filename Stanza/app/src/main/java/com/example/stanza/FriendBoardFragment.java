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
 * A class representing the Friends Board fragment, which contains a list of the user's friend's
 * poems. Appears under the "Friends Board" tab in the dashboard page.
 */
public class FriendBoardFragment extends Fragment
implements LoaderManager.LoaderCallbacks<Cursor>, CommInterface
{
    //class variables

    /**
     * The layout containing all other elements within the Friends Board fragment.
     * <code>SwipeRefreshLayout</code> allows the user to swipe down from the top of the screen
     * in order to refresh the poems in the Friends Board tab.
     */
    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     * An instance of <code>PoemRecyclerAdapter</code>, which provides binding from a set of poems
     * (in this case the user's friends' poems) to card views that are displayed in a
     * <code>RecyclerView</code>, which in this case is displayed within the Friends Board
     * Fragment.
     */
    PoemRecyclerAdapter poemRecyclerAdapter;

    //methods

    /**
     * Called to have the Friends Board fragment instantiate its user interface view.
     * @param inflater The <code>LayoutInflater</code> object that can be used to inflate any views in the fragment.
     * @param container The parent view that the Friends Board fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The <code>View</code> for the Friend Board Fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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

    /**
     * Called when <code>MainActivity</code> has been created and the Friends Board fragment's
     * view hierarchy has been instantiated.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
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

    /**
     * Starts a new or restarts an existing Loader in this manager, registers the callbacks implemented in this class,
     * and (if the fragment is currently started) starts loading it.
     */
    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    /**
     * Runs a <code>CommThread</code>, which retrieves the User's Friends' Poems
     * from the Backend server.
     */
    public void pullPoems(){
        CommThread ct = new CommThread(this,FriendBoardFragment.this);
        ct.start();
    }

    /**
     * Instantiate and return a new Loader for user's friends' poems in the database.
     * @param id The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return  A new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = DBOpenHelper.CREATOR + " LIKE 'friend'";
        String orderBy = "friends";
        return new CursorLoader(getActivity(), NotesProvider.CONTENT_URI,
                null, selection, null, orderBy);
    }

    /**
     * Called when a previously created loader has finished its load. Provides <code>PoemRecyclerAdapter</code>
     * with the data generated by the loader.
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        poemRecyclerAdapter.swapCursor(data);
    }


    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable.
     * Removes references to the Loader's data within <code>PoemRecyclerAdapter</code>.
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        poemRecyclerAdapter.swapCursor(null);
    }

    /**
     * Called when the Friends Board fragment is visible to the user and actively running.
     * Restarts the <code>Loader</code> for the user's friend's poems.
     */
    @Override
    public void onResume() {
        restartLoader();
        super.onResume();
    }

    /**
     * Called when a poem is saved to the remote server. The Friends Board does not allow you to save
     * poems to the server, so this callback is left blank here.
     * @param output The output message from <code>CommThread</code>.
     */
    @Override
    public void poemSaved(String output) {
    }

    /**
     * Called from CommThread if the server is disconnected. Informs the user that the server is not
     * connected, and so cannot retrieve friends' poems from the backend server.
     */
    @Override
    public void serverDisconnected() {
        Toast.makeText(getActivity(), R.string.cannot_retrieve_poems, Toast.LENGTH_SHORT).show();

    }

    /**
     * Called when the app has finished retrieving the user's friends' poems. Stops the refreshing
     * UI indicator and restarts the <code>Loader</code>.
     */
    @Override
    public void onPullFinished() {
        swipeRefreshLayout.setRefreshing(false);
        restartLoader();
    }
}
