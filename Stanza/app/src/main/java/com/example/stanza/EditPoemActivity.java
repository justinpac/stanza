package com.example.stanza;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/** A class for editing personal poems, with added rhyming functionality
 * from the Wordnik api.
 */

public class EditPoemActivity extends AppCompatActivity
implements CommInterface{

    // state variables

    /**
     * String which holds the intent for the poem:
     * <code>ACTION_INSERT</code> for a newly created poem, and
     * <code>ACTION_EDIT</code> for editing an existing poem.
     */
    private String action;
    /**
     * EditText field which contains the poem's title.
     */
    private EditText editorTitle;
    /**
     * EditText field which contains the poem's text.
     */
    private EditText editor;
    /**
     * Used to properly identify the particular poem when pulling
     * its title and text from the local database.
     */
    private String noteFilter;
    /**
     * Used to set <code>editor's</code> text to the previously
     * saved poem text when the editor is started.
     */
    private String oldText;
    /**
     * Used to set <code>editorTitle's</code> text to the previously
     * saved poem title when the editor is started.
     */
    private String oldTitle;
    /**
     * Holds the list of rhyme suggestions for the current word
     * in <code>editor</code>.
     */
    String[] rhymeList;
    /**
     * Specifies the URL for the api call (in this case, to Wordnik,
     * but that could be changed).
     */
    URL url;                             //api url
    /**
     * The word most recently sent to Wordnik for the api call.
     */
    String lookupWord = "";              //Word sent to api for lookup
    /**
     * A timer which makes a Wordnik api call once per second.
     * This api call is done on an AsyncTask, so as to not slow the
     * UI thread.
     */
    Timer rhymeTime = new Timer();
    /**
     * The scrollable view which contains the list of suggested rhymes
     * for the current word in <code>editor</code>.
     */
    HorizontalScrollView rhymeView;
    /**
     * The internal view of <code>rhymeView</code>, which contains
     * the list of suggested rhymes for the selected word.
     */
    LinearLayout rhymeBar;

    /**
     * Holds the username of the current user, to associate with any
     * poem they publish to the backend server.
     */
    String user;
    /**
     * Creates a <code>CommThread</code>, for communication with the
     * backend server. Allows for pushing a poem to the server.
     */


    private CommThread ct;

    /** Called on the creation of an <code>EditPoemActivity</code>.
     * Initializes <code>EditText</code> fields for the poem's title and
     * text and fills them with the values stored in the local database,
     * then initializes the <code>HorizontalScrollView</code> and
     * <code>LinearLayout</code> which will contain rhyme suggestions.
     *
     * @param savedInstanceState allows us to call Android's onCreate
     *                           function in addition to ours.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        /**
         * Initializes the <code>EditText</code> which holds the poem's
         * title.
         */
        editorTitle = (EditText) findViewById(R.id.editText2);
        /**
         * Initializes the <code>EditText</code> which holds the poem's
         * text.
         */
        editor = (EditText) findViewById(R.id.editText);
        /**
         * Grabs the username of the user, to associate with any poem
         * they publish.
         */
        user = LoginActivity.user;

        /**
         * Initializes the <code>HorizontalScrollView</code> which
         * holds the <code>LinearLayout</code> containing rhyme suggestions.
         */
        rhymeView = (HorizontalScrollView) findViewById(R.id.rhymeView);
        /**
         * Initializes the <code>LinearLayout</code> which contains rhyme
         * suggestions.
         */
        rhymeBar = (LinearLayout)findViewById(R.id.rhymeLayout);

        /* This section was changed to intent.getLongExtra(..), but it created a bug where the
        * old text and title were not being displayed. These subsequent changes fixed that bug. */
        /**
         * Contains the intent that started this activity.
         */
        Intent intent = getIntent();
        /**
         * Tries to find the uri for the current poem. If it exists,
         * loads in the poem title and text using <code>action</code>.
         * If it does not, creates default values for poem title and text,
         * also using <code>action</code>.
         */
        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);
        if (uri == null) {
            action = Intent.ACTION_INSERT;
            setTitle(getString(R.string.new_note));
        } else {
            action = Intent.ACTION_EDIT;
            String path = uri.toString();
            String idStr = path.substring(path.lastIndexOf('/') + 1);
            noteFilter = DBOpenHelper.POEM_ID + "=" + idStr;

            String orderBy = "self";
            Cursor cursor = getContentResolver().query(uri, DBOpenHelper.ALL_COLUMNS
                    , noteFilter, null, orderBy);
            cursor.moveToFirst();
            oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.POEM_TEXT));
            oldTitle = cursor.getString(cursor.getColumnIndex(DBOpenHelper.POEM_TITLE));
            editor.setText(oldText);
            editorTitle.setText(oldTitle);
            editor.requestFocus();
        }


    }

    //AsnycTask to make call to Wordnik api

    /**
     * AsyncTask which makes a call to the Wordnik api, parses a received
     * json, then fills <code>rhymeBar</code> with the resulting suggestions.
     */
    class rhymeTask extends AsyncTask<String, Void, String> {
        /**
         * Holds the value for the json list resulting from the api call.
         */
        String jsonList;

        /** Overrides Android's doInBackground for this async task.
         * Makes an api call using <code>lookupWord</code> and reads json data
         * into <code>jsonList</code>.
         *
         * @param params Parameters passed by the caller of this task.
         * @return The populated json string from the api call.
         */
        @Override
        protected String doInBackground(String... params) {
            try {
                String request = "http://api.wordnik.com:80/v4/word.json/" + lookupWord
                        + "/relatedWords?useCanonical=false&limitPerRelationshipType=10&api_key="
                        + "34b85c60a34e51f8ffa4a6f3bfe056794ea70f73f26e33123";

                /**
                 * url for the api call
                 */
                url = new URL(request);
                /**
                 * Opens a connection to read input, using <code>in</code>.
                 */
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                /**
                 * Reads json data from Wordnik.
                 */
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                jsonList =  new Scanner(in, "UTF-8").useDelimiter("\\A").next();
                in.close();
                return jsonList;
            }catch (Exception e) {
                e.printStackTrace();
            }
            return jsonList;
        }

        /**
         * Called when <code>doInBackground</code> returns. Parses the json
         * from the api, then populates <code>rhymeBar</code> with its results.
         * @param result Value returned from <code>doInBackground</code>, namely,
         *               <code>jsonList</code>.
         */
        @Override
        protected void onPostExecute(String result) {
            parseJson(jsonList);
            refillRhymeBar();
        }

        /**
         * Parses the json object from the api call, extracting the list of
         * words under the "rhyme" relationship for <code>lookupWord</code>
         * and stores them in <code>rhymeList</code>.
         * @param strJson The json string to be parsed.
         */
        protected void parseJson(String strJson) {
            /**
             * Holds the list of rhyme suggestions contained in
             * <code>strJson</code>. Originally holds the expression "No rhymes",
             * in case <code>strJson</code> is empty.
             */
            String[] returnArray = new String[1];
            returnArray[0] = "No rhymes";
            try {
                if (strJson != null) {

                    JSONArray newJsonArray = new JSONArray(strJson);
                    for (int i = 0; i < newJsonArray.length(); i++) {
                        JSONObject wordData = newJsonArray.getJSONObject(i);
                        if ("rhyme".equals(wordData.getString("relationshipType"))) {
                            JSONArray interiorArr = wordData.getJSONArray("words");
                            returnArray = new String[interiorArr.length()];
                            for (int j = 0; j < interiorArr.length(); j++) {
                                String testStr = interiorArr.getString(j);
                                returnArray[j] = testStr;
                            }
                        }
                    }
                    Log.d("JSONTag", newJsonArray.getClass().getName());
                }
            } catch (JSONException e) {e.printStackTrace();}

            //set rhymeList up to contain the rhyming list
            rhymeList = returnArray;
        }

    }

    //=======Timer handling for api calls!

    /**
     * Called whenever the app resumes. Re-schedules the <code>TimerTask</code>
     * which checks every second to see whether the selected word has changed,
     * and if so, creates a new <code>rhymeTask()</code>, getting rhyme suggestions
     * for the new word.
     */
    @Override
    public void onResume() {
        super.onResume();
        rhymeTime.schedule(new TimerTask() {
            @Override
            public void run() {
                int startSelection = editor.getSelectionStart();
                int selectLength = 0;

                for (String currentWord : editor.getText().toString().split("\\s")) {
                    if (currentWord != null) {
                        selectLength = selectLength + currentWord.length() + 1;
                        if (selectLength > startSelection) {
                            currentWord = currentWord.replaceAll("[\",.;!?(){}\\<>%]", "");
                            //Log.d("currentWord", currentWord);
                            if (currentWord.intern() != lookupWord.intern()) {
                                lookupWord = currentWord;

                                refillRhymeBar();
                                new rhymeTask().execute(); //Make api call!
                            }
                            break;
                        }
                    }
                }
            }
        }, 0, 1000);
    }

    /**
     * If the activity is paused, cancels <code>rhymeTime</code>.
     */
    @Override
    public void onPause() {
        super.onPause();
        rhymeTime.cancel();
    }
    /**
     * If the activity is stopped, cancels <code>rhymeTime</code>.
     */
    @Override
    public void onStop() {
        super.onStop();
        rhymeTime.cancel();
    }
    /**
     * If the activity is destroyed, cancels <code>rhymeTime</code>.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        rhymeTime.cancel();
    }

    /**
     * Creates the options bar with the <code>home</code>,
     * <code>delete</code> and <code>publish_poem_to_server</code>
     * buttons. Only creates this bar for edited poems; does not create
     * this bar for new poems.
     * @param menu The options menu to be created.
     *
     * @return <code>true</code>, every time this is called.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (action.equals(Intent.ACTION_EDIT)) {
            getMenuInflater().inflate(R.menu.menu_editor, menu);
        }
        return true;
    }

    /**
     * Depending on which menu item was selected, take the appropriate
     * action. If <code>home</code>, go home. If <code>delete</code>, do
     * delete confirmation and then delete the poem. If
     * <code>publish_poem_to_server</code>, publish the poem to the server.
     * @param item The options item that was selected.
     * @return <code>true</code>, every time this is called.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /**
         * The id of the selected <code>MenuItem</code>. Used to determine
         * appropriate action.
         */
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finishEditing();
                break;
            case R.id.action_delete:
                deleteNote();
                break;
            case R.id.action_publish_poem_to_server:
                String title = editorTitle.getText().toString().trim() + '\001';
                String text = editor.getText().toString().trim() + '\001';
                pushPoem(title,text);
                break;
        }

        return true;
    }

    /**
     * Called if the delete menu item is selected. Deletes the current
     * poem, removing it from the local database.
     */
    private void deleteNote() {
        getContentResolver().delete(NotesProvider.CONTENT_URI,
                noteFilter, null);
        Toast.makeText(this, getString(R.string.note_deleted),
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    /**
     * Called when the home menu icon is selected, or when the back
     * button is pressed. If <code>editor</code> and <code>editorTitle</code>
     * are blank, the poem is not saved. If only <code>editorTitle</code> is
     * blank, it is saved with value "Untitled". If only
     * <code>editorText</code> is blank, it is saved with value " ".
     */
    private void finishEditing() {
        /**
         * Gets saved text, if any, from <code>editor</code>.
         */
        String newText = editor.getText().toString().trim();
        /**
         * Gets saved text, if any, from <code>editorTitle</code>.
         */
        String newTitle = editorTitle.getText().toString().trim();

        switch (action) {
            case Intent.ACTION_INSERT:
                if (newText.length() == 0 && newTitle.length() == 0) {
                    setResult(RESULT_CANCELED);
                }
                else if(newText.length() == 0){
                    insertNote(" ",newTitle);
                }
                else if(newTitle.length() == 0){
                    insertNote(newText, "Untitled");
                }
                else{
                    insertNote(newText, newTitle);
                }
                break;
            case Intent.ACTION_EDIT:
                if (newText.length() == 0 && newTitle.length() == 0) {
                    deleteNote();
                } else {
                    if(newTitle.length() == 0) newTitle = "Untitled";
                    updateNote(newText, newTitle);
                }
                break;
        }
        finish();
    }

    /**
     * Saves new values for <code>editor</code> and <code>editorTitle</code>
     * to the local database.
     * @param poemText Contains value that will be saved as
     *                 <code>POEM_TEXT</code> in the local database.
     * @param poemTitle Contains value that will be saved as
     *                  <code>POEM_TITLE</code> in the local database.
     */
    private void updateNote(String poemText, String poemTitle) {
        /**
         * Holds the content values of <code>POEM_TEXT</code> and
         * <code>POEM_TITLE</code>.
         */
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.POEM_TEXT, poemText);
        values.put(DBOpenHelper.POEM_TITLE,poemTitle);
        //System.out.println("notefilter " + noteFilter);
        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        //Toast.makeText(this, getString(R.string.note_updated), Toast.LENGTH_SHORT).show();



        setResult(RESULT_OK);
    }

    /**
     * Fills <code>rhymeBar</code> with the values in <code>rhymeList</code>.
     */
    private void refillRhymeBar() {
        //First, clear all existing TextViews...
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                rhymeBar.removeAllViews();
            }
        });
        //Then, add new TextViews to the bar, if appropriate
        if (rhymeList != null){    //Check whether rhymeList has been initialized
            for (int i = 0; i < rhymeList.length; i++) {
                final TextView tv = new TextView(getApplicationContext());
                tv.setText(rhymeList[i]);
                Log.d("SETTEXT", tv.getText().toString());
                assert rhymeBar != null;
                tv.setTextColor(Color.BLACK);
                tv.setPadding(20, 0, 0, 20);
                final int finalI = i;
                tv.setOnClickListener(new  View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String selectedText = rhymeList[finalI];
                        replaceText(selectedText);
                    }
                });
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rhymeBar.addView(tv);
                    }
                });

            }
        }

    }

    /**
     * Replaces the word the cursor is in with a word from
     * <code>rhymeBar</code>.
     * @param newText The text that will replace the currently selected
     *                word. Taken from <code>rhymeBar</code>.
     */
    private void replaceText(String newText) {
        /**
         * Will contain the return value from <code>checkPunctuation()</code>.
         */
        String punctuation;
        /**
         * Holds the entire text of <code>editor</code>, for manipulation.
         */
        String s = editor.getText().toString();
        /**
         * Holds the initial location of the cursor.
         */
        int selectionStart = editor.getSelectionStart();
        /**
         * Holds the portion of <code>s</code> before <code>selectionStart</code>.
         */
        String beforeCursor = s.substring(0, selectionStart);
        /**
         * Holds the portion of <code>s</code> after <code>selectionStart</code>.
         */
        String afterCursor = s.substring(selectionStart, s.length());

        /**
         * Will hold the final string value, with the proper word replaced by
         * <code>newText</code>. Uses "garbage" characters to handle the
         * cases where <code>selectionStart</code> was at the beginning or
         * the end of the word to be replaced.
         */
        String finalString = beforeCursor + "garbage" + afterCursor;
        /**
         * Offsets <code>selectionStart</code> to place it inside those "garbage"
         * characters.
         */
        int selectionStart1 = selectionStart + 3;
        beforeCursor = finalString.substring(0, selectionStart1);
        afterCursor = finalString.substring(selectionStart1, finalString.length());

        /**
         * Holds all words in <code>beforeCursor</code>, split by whitespace.
         */
        String[] beforeWords = beforeCursor.split("\\s");
        /**
         * Holds all words in <code>afterCursor</code>, split by whitespace.
         */
        String[] afterWords = afterCursor.split("\\s");

        int arrayLength = beforeWords.length;
        punctuation = checkPunctuation(beforeWords[arrayLength - 1]);
        if (punctuation == "")
            punctuation = checkPunctuation(afterWords[0]);
        int removeStringLength = beforeWords[arrayLength - 1].length();
        beforeCursor = beforeCursor.substring(0, selectionStart1 - removeStringLength);

        removeStringLength = afterWords[0].length();
        afterCursor = afterCursor.substring(removeStringLength, afterCursor.length());
        finalString = beforeCursor + newText + punctuation + afterCursor;

        editor.setText(finalString);
        try {
            editor.setSelection(selectionStart);
        } catch(Exception e) {
            editor.setSelection(finalString.length());
        }
    }

    /**
     * Checks whether or not a word segment contains punctuation, and if
     * so, preserves it.
     * @param checkText The word segment to be checked.
     * @return The value of any punctuation found.
     */
    private String checkPunctuation(String checkText) {
        /**
         * Used to check for the existence of punctuation in the word
         * segment.
         */
        int indexVal;
        indexVal = checkText.indexOf(".");
        if (indexVal != -1) return ".";

        indexVal = checkText.indexOf(",");
        if (indexVal != -1) return ",";

        indexVal = checkText.indexOf("!");
        if (indexVal != -1) return "!";

        indexVal = checkText.indexOf("?");
        if (indexVal != -1) return "?";

        return "";
    }

    /**
     * Inserts the poem title and text into the local database.
     * @param poemText The value to be inserted into the local database under
     *                 <code>POEM_TEXT</code>.
     * @param poemTitle The value to be inserted into the local database under
     *                 <code>POEM_TITLE</code>.
     */
    private void insertNote(String poemText, String poemTitle) {
        /**
         * Holds the values for the poem's text, title and creator, to be stored
         * under <code>POEM_TEXT</code>, <code>POEM_TITLE</code> and
         * <code>POEM_CREATOR</code>.
         */
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.POEM_TEXT, poemText);
        values.put(DBOpenHelper.POEM_TITLE, poemTitle);
        values.put(DBOpenHelper.CREATOR, "self");
        getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        setResult(RESULT_OK);
    }

    /**
     * Called when the device's back button is pressed. Saves any changes to
     * the poem's text or title, or deletes it if both are empty.
     */
    @Override
    public void onBackPressed() {
        finishEditing();
    }

    /**
     * Pushes a poem to the backend database, to be shared with friends.
     * Starts and interrupts a <code>CommThread</code> to do this.
     * @param poemTitle The value to be inserted into the local database under
     *                 <code>POEM_TEXT</code>.
     * @param poemText The value to be inserted into the local database under
     *                 <code>POEM_TITLE</code>.
     */
    public void pushPoem(String poemTitle, String poemText) {
        /**
         * Initializes a poem object with <code>poemTitle</code> and
         * <code>poemText</code>, to be sent to the backend database.
         */
        Poem poem = new Poem(poemTitle, poemText, user);
        /**
         * Starts a <code>CommThread</code> to send <code>poem</code>
         * to the backend database.
         */
        ct = new CommThread(this, EditPoemActivity.this);
        ct.start();
        ct.addPoem(poem);
        ct.interrupt();
    }

    /**
     * Displays the proper message if a poem is saved to the database.
     * @param output Gives this output if poem is saved.
     */
    @Override
    public void poemSaved(String output) {
        Toast.makeText(getApplicationContext(), output, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays the shown message if the server is disconnected.
     */
    @Override
    public void serverDisconnected() {
        Toast.makeText(getApplication(), "Server not connected. Cannot save poems to server.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when Pull is Finished. Does nothing.
     */
    @Override
    public void onPullFinished() {

    }

}
