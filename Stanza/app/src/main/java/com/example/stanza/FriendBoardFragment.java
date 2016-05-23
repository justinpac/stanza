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
 * Created by Brianna on 4/22/2016.
 */
public class FriendBoardFragment extends Fragment
implements LoaderManager.LoaderCallbacks<Cursor>, CommInterface, AccountCommInterface
{
    private SwipeRefreshLayout swipeRefreshLayout;
    PoemRecyclerAdapter poemRecyclerAdapter;
    AlertDialog.Builder alert;
    AccountCommThread act;
    AccountCommInterface aci;
    String friendname;
    Vector<String> allFriends;

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
                //TODO: dialog box to type in username

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



                        //send to backend to check if the username exists
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
        }
        return super.onOptionsItemSelected(item);
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




    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = DBOpenHelper.CREATOR + " !='self'";
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
