package com.example.stanza;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Brianna on 5/24/2016.
 */
public class ManageFriendsActivity extends AppCompatActivity
    implements AccountCommInterface, LoaderManager.LoaderCallbacks<Cursor>{

    private CursorAdapter cursorAdapter;
    android.support.v7.app.AlertDialog.Builder alert;
    AccountCommInterface aci;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_friends);

        String[] from = {DBOpen2.FRIEND};
        int[] to = {R.id.tvFriend};

        aci = this;
        cursorAdapter = new CursorAdapter(this, null, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.friend_list_item,
                        parent, false
                );
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                String friend = cursor.getString(
                        cursor.getColumnIndex(DBOpen2.FRIEND));

                int pos = friend.indexOf(10);
                if(pos != -1){
                    friend = friend.substring(0, pos) + "...";
            }

                TextView tv = (TextView) view.findViewById(R.id.tvFriend);
                tv.setText(friend);

            }
        };

        ListView list = (ListView) findViewById(R.id.friend_list);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                deleteFriend(id);
            }
        });

        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_manage_friends, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch(id){
            case R.id.action_request_friend2:
                requestAFriend();
                break;
            case R.id.action_delete_all_friends:
                deleteAllFriends();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteFriend(final long id){


        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            String noteFilter = DBOpen2.FRIEND_ID + "=" + id;

                            getContentResolver().delete(NotesProvider2.CONTENT_URI,
                                    noteFilter, null);
                            Toast.makeText(ManageFriendsActivity.this, R.string.delete_friend,
                                    Toast.LENGTH_SHORT).show();
                            restartLoader();

                            Toast.makeText(ManageFriendsActivity.this,
                                    "Delete All Friends",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.would_you_like_to_delete_friend);
        builder.setPositiveButton(R.string.delete_friend, dialogClickListener);
        builder.setNegativeButton(getString(android.R.string.no), dialogClickListener);
        builder.show();

    }

    private void requestAFriend(){
        alert = new android.support.v7.app.AlertDialog.Builder(this);
        alert.setMessage("Enter your friend's username.");
        alert.setTitle("Add a Friend");
        final EditText editText = new EditText(this);
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
                Toast.makeText(getApplicationContext(), "Canceled", Toast.LENGTH_SHORT ).show();
            }
        });

        alert.show();
    }

    public void addFriend(String username){
        AccountCommThread act = new AccountCommThread(aci, ManageFriendsActivity.this);
        act.thisAccount(username, null, null);
        act.start();
    }

    private void addToFriendList(String friend) {
        ContentValues values = new ContentValues();
        values.put(DBOpen2.FRIEND, friend);
        getContentResolver().insert(NotesProvider2.CONTENT_URI, values);
        restartLoader();
    }
    private void deleteAllFriends(){
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            //Insert Data management code here
                            getContentResolver().delete(
                                    NotesProvider2.CONTENT_URI, null, null
                            );
                            restartLoader();

                            Toast.makeText(ManageFriendsActivity.this,
                                    "Delete All Friends",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
        }


    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, NotesProvider2.CONTENT_URI,
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

    @Override public void onServerDisconnected() {}
    @Override public void onLoginSuccess() {}
    @Override public void onLoginFailed(String error_message) {}
    @Override public void onSignupSuccess() {}
    @Override public void onSignupFailed(String error_message) {}

    @Override
    public void onFriendSuccess(String name) {
        //add to friend list
        addToFriendList(name);
        Toast.makeText(getApplicationContext(), "Saved Friend", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFriendFailure(String error_message) {
        //username did not exist; print error message
        System.out.println("Friend Does Not Exist");
        Toast.makeText(getApplicationContext(), error_message, Toast.LENGTH_LONG).show();
    }
}




