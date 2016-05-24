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
 * A class to manage your friends list
 */
public class ManageFriendsActivity extends AppCompatActivity
    implements AccountCommInterface, LoaderManager.LoaderCallbacks<Cursor>{

    // State variables
    /**
     * A cursorAdapter to handle user input/clicks in <code>ManageFriendsActivity</code>
     */
    private CursorAdapter cursorAdapter;
    /**
     * A alert that will be built and displayed to the user depending on their activities in <code>ManageFriendsActivity</code>
     */
    android.support.v7.app.AlertDialog.Builder alert;
    /**
     * An interface to interact with messaging and the comm threads in <code>ManageFriendsActivity</code>
     */
    AccountCommInterface aci;

    //methods
    /**
     * A method that initializes the activity when created
     * @param savedInstanceState a saved state when navigated away from
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_friends);

        String[] from = {DBOpen2.FRIEND};
        int[] to = {R.id.tvFriend};

        aci = this;
        cursorAdapter = new CursorAdapter(this, null, 0) {
            /**
             * Creates a new view when the user is navigating around the friend manager
             * @param context the context for the view
             * @param cursor the cursor, if needed to select options
             * @param parent the parent where this view is located
             * @return The inflated layout
             */
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.friend_list_item,
                        parent, false
                );
            }

            /**
             * a method to change the view when a new one is created
             * @param view the view we are working with
             * @param context the context where to display the view
             * @param cursor the cursor, if needed to select options
             */
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

    /**
     * A method to create an inflated menu
     * @param menu the menu that is being inflated
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_manage_friends, menu);
        return true;
    }

    /**
     * A handler for which option is selected by the user
     * @param item the item that is selected, located in a menu
     * @return the item selected, and therefore the action of the user
     */
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

    /**
     * A method to delete a friend from one's friend list
     * @param id A ID of the friend being deleted
     */
    private void deleteFriend(final long id){


        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    /**
                     * A handler for the user selecting an option for friend deletion
                     * @param dialog The dialogue being displayed to the user
                     * @param button The button clicked indicating the action to take
                     */
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

    /**
     * A method to request a new friend to be in a person's friend list
     */
    private void requestAFriend(){
        alert = new android.support.v7.app.AlertDialog.Builder(this);
        alert.setMessage("Enter your friend's username.");
        alert.setTitle("Add a Friend");
        final EditText editText = new EditText(this);
        alert.setView(editText);
        alert.setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
            /**
             * A click handler located in requestAFriend to get the User's actions
             * @param dialog the dialogue displayed to the user
             * @param which An integer indicating which friend is being added.
             */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String friendname = editText.getText().toString().trim();
                System.out.println("friendname is " + friendname);
                addFriend(friendname);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            /**
             * A click handler for the cancel button
             * @param dialog dialog displayed to the user
             * @param which which friend is selected that the user wants to add
             */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Canceled", Toast.LENGTH_SHORT ).show();
            }
        });

        alert.show();
    }

    /**
     * A method which adds a friend to a friendlist by username
     * @param username the username of the friend that the user wants to add
     */
    public void addFriend(String username){
        AccountCommThread act = new AccountCommThread(aci, ManageFriendsActivity.this);
        act.thisAccount(username, null, null);
        act.start();
    }

    /**
     * A method adding the friend to the user's friend list
     * @param friend The friend that is being added to the user's friend list
     */
    private void addToFriendList(String friend) {
        ContentValues values = new ContentValues();
        values.put(DBOpen2.FRIEND, friend);
        getContentResolver().insert(NotesProvider2.CONTENT_URI, values);
        restartLoader();
    }

    /**
     * A method which deletes all friends from a user's friend list
     */
    private void deleteAllFriends(){
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    /**
                     * A click handler for confirming to delete all friends
                     * @param dialog Message being displayed to the user
                     * @param button A button that the user interact with to confirm intention
                     */
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

    /**
     * A method to refresh the list of friends
     */
    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    /**
     * A method to create the list when first made
     * @param id an id to indicate friends?
     * @param args what args apply when creating the loader?
     * @return the friends list when first created
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, NotesProvider2.CONTENT_URI,
                null, null, null, null);
    }

    /**
     * when the loader is created, user can interact with it
     * @param loader the friends list
     * @param data the data being held?
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    /**
     * Creates the loader functionality when reset
     * @param loader the loader that is being reset
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    /**
     * A method that displays a message when server is disconnected
     */
    @Override public void onServerDisconnected() {}

    /**
     * A method that ensures the login is successful/authenticated
     */
    @Override public void onLoginSuccess() {}

    /**
     * A method that handles when login is not successful
     * @param error_message given if app login fails
     */
    @Override public void onLoginFailed(String error_message) {}

    /**
     * A method that indicates when signup is successful
     */
    @Override public void onSignupSuccess() {}

    /**
     * A method that indicates when signup fails
     * @param error_message given if app sign up fails (perhaps due to server issues)
     */
    @Override public void onSignupFailed(String error_message) {}

    /**
     * A method that adds a friend to a user's friend list
     * @param name the name of the friend
     */
    @Override
    public void onFriendSuccess(String name) {
        //add to friend list
        addToFriendList(name);
        Toast.makeText(getApplicationContext(), "Saved Friend", Toast.LENGTH_SHORT).show();
    }

    /**
     * A method that occurs when a friend cannot be added to a user's friend lsit
     * @param error_message Message that is displayed when adding a friend is unsuccessful
     */
    @Override
    public void onFriendFailure(String error_message) {
        //username did not exist; print error message
        System.out.println("Friend Does Not Exist");
        Toast.makeText(getApplicationContext(), error_message, Toast.LENGTH_LONG).show();
    }
}