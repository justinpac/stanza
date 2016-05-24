package com.example.stanza;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.Toast;

import java.util.Vector;

/**
 * A class representing the Friends Board fragment, which contains a list of the user's friend's
 * poems. Appears under the "Friends Board" tab in the dashboard page.
 */
public class FriendBoardFragment extends Fragment
implements LoaderManager.LoaderCallbacks<Cursor>, CommInterface, AccountCommInterface
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
    AlertDialog.Builder alert;
    AccountCommThread act;
    AccountCommInterface aci;
    String friendname;
    Vector<String> allFriends;

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
        aci = this;

        allFriends = new Vector<String>();




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

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_friendboardfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_request_friend:

                alert = new AlertDialog.Builder(getContext());
                alert.setMessage("Enter your friend's username.");
                alert.setTitle("Add a Friend");
                final EditText editText = new EditText(getContext());
                alert.setView(editText);
                alert.setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String friendname = editText.getText().toString().trim();
                        System.out.println("friendname is " + friendname);
                        addFriend(friendname);



                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getContext(), "Canceled", Toast.LENGTH_SHORT ).show();
                    }
                });

                alert.show();
                break;
            case R.id.action_manage_friends:
                Intent intent = new Intent(getActivity(), ManageFriendsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
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
        //take each name from the friend database and put it in the friend vector
        //query that database and extract the friends


        Cursor cursor = getActivity().getContentResolver().query(NotesProvider2.CONTENT_URI, DBOpen2.ALL_COLUMNS
                , null, null, null);
        try {
            cursor.moveToFirst();
            do{
                String friend = cursor.getString(cursor.getColumnIndex(DBOpen2.FRIEND));
                allFriends.add(friend);
            }while(cursor.moveToNext());
        }catch(RuntimeException e){
            e.printStackTrace();
        }
        cursor.close();

        ct.addFriend(allFriends);
        ct.start();
    }

    public void addFriend(String username){
        AccountCommThread act = new AccountCommThread(aci, FriendBoardFragment.this);
        act.thisAccount(username, null, null);
        act.start();
    }

    private void addToFriendList(String friend) {
      /*  System.out.println("given friend is " + friend);
        allFriends.add(friend);
        for(int i=0; i<allFriends.size(); i++){
            System.out.println("friend: " + allFriends.elementAt(i));
        }
        */
        ContentValues values = new ContentValues();
        values.put(DBOpen2.FRIEND, friend);
        getActivity().getContentResolver().insert(NotesProvider2.CONTENT_URI, values);
    }

    /**
     * Instantiate and return a new Loader for user's friends' poems in the database.
     * @param id The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return  A new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = DBOpenHelper.CREATOR + " !='self'";
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

    @Override public void onServerDisconnected() {}
    @Override public void onLoginSuccess() {}
    @Override public void onLoginFailed(String error_message) {}
    @Override public void onSignupSuccess() {}
    @Override public void onSignupFailed(String error_message) {}

    @Override
    public void onFriendSuccess(String name) {
        //add to friend list
        addToFriendList(name);
        Toast.makeText(getActivity(), "Saved Friend", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFriendFailure(String error_message) {
        //username did not exist; print error message
        System.out.println("Friend Does Not Exist");
        Toast.makeText(getActivity(), error_message, Toast.LENGTH_LONG).show();
    }
}
