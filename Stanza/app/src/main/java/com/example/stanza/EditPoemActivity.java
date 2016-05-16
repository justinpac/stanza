package com.example.stanza;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.util.JsonReader;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stanza.DBOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


public class EditPoemActivity extends AppCompatActivity
implements CommInterface{

    private String action;
    private EditText editorTitle;
    private EditText editor;
    private TextView rhymeList;
    private String noteFilter;
    private String oldText;
    private String oldTitle;
    private Toolbar toolbar;
    String[] spinnerList;
    public String apiWord;
    URL url;                        //api url
    Spinner rhymeSpinner;
    boolean firstSpinnerCall;
    String lookupWord;              //Word sent to api for lookup
    //String currentWord;             //Currently selected word
    Timer timerTest = new Timer();


 //   private Button publish;
    private CommThread ct;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        editorTitle = (EditText) findViewById(R.id.editText2);
        editor = (EditText) findViewById(R.id.editText);





        rhymeSpinner = (Spinner) findViewById(R.id.rhymeSpinner);
        spinnerList = new String[] { "rhymes" };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, spinnerList);
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        rhymeSpinner.setAdapter(spinnerAdapter);

        firstSpinnerCall = true;
        rhymeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstSpinnerCall) {
                    firstSpinnerCall = false;
                } else {
                    String selectedText = (String) parent.getItemAtPosition(position);
                    replaceText(selectedText);
                    //Log.v("item", (String) parent.getItemAtPosition(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /*editor.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                int startSelection = editor.getSelectionStart();
                int selectLength = 0;

                for (String currentWord : editor.getText().toString().split(" ")) {
                    selectLength = selectLength + currentWord.length() + 1;
                    if (selectLength > startSelection) {
                        Log.d("currentWord", currentWord);
                        if (currentWord != lookupWord) {
                            lookupWord = currentWord;
                            new rhymeTask().execute(); //Make api call!
                        }
                        break;
                    }

                }


                return false;
            }
        });*/



       // publish = (Button) findViewById(R.id.publish_poem_button);




        /* This section was changed to intent.getLongExtra(..), but it created a bug where the
        * old text and title were not being displayed. These subsequent changes fixed that bug. */
        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);
        if (uri == null) {
            action = Intent.ACTION_INSERT;
            setTitle(getString(R.string.new_note));
        } else {
            action = Intent.ACTION_EDIT;
            System.out.println("URI IS " + uri.toString());
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
    //AsyncTask to make api calls

    //AsnycTask to make call to Wordnik api
    class rhymeTask extends AsyncTask<String, Void, String> {
        String rhymeList;
        @Override
        protected String doInBackground(String... params) {
            try {
                String request = "http://api.wordnik.com:80/v4/word.json/" + lookupWord
                        + "/relatedWords?useCanonical=false&limitPerRelationshipType=10&api_key="
                        + "34b85c60a34e51f8ffa4a6f3bfe056794ea70f73f26e33123";

                url = new URL(request);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                rhymeList =  new Scanner(in, "UTF-8").useDelimiter("\\A").next();
                Log.d("myTag", rhymeList.getClass().getName());
                in.close();
                return rhymeList;



            }catch (Exception e) {
                e.printStackTrace();
            }
            return rhymeList;
        }
        @Override
        protected void onPostExecute(String result) {
            parseJson(rhymeList);
            for (int i = 0; i < spinnerList.length; i++){
                Log.d("spinnerList", spinnerList[i]);
            }

            ArrayAdapter<String> newSpinnerAdapter = new ArrayAdapter<String>
                    (getApplicationContext(), android.R.layout.simple_spinner_item, spinnerList);
            newSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            rhymeSpinner.setAdapter(newSpinnerAdapter);
            firstSpinnerCall = true;

        }

        protected String[] parseJson(String strJson) {
            String[] returnArray = new String[1];
            returnArray[0] = "No rhymes";
            try {
                JSONArray newJsonArray = new JSONArray(strJson);
                for (int i=0; i<newJsonArray.length(); i++) {
                    JSONObject wordData = newJsonArray.getJSONObject(i);
                    if ("rhyme".equals(wordData.getString("relationshipType"))) {
                        JSONArray interiorArr = wordData.getJSONArray("words");
                        returnArray = new String[interiorArr.length()];
                        for (int j=0; j<interiorArr.length(); j++) {
                            String testStr = interiorArr.getString(j);
                            returnArray[j] = testStr;
                        }
                    }
                }
                Log.d("JSONTag", newJsonArray.getClass().getName());
            } catch (JSONException e) {e.printStackTrace();}

            //set spinnerList up for suggestions
            int returnLen = returnArray.length + 1;
            String[] tempArray = new String[returnLen];
            tempArray[0] = "rhymes:";  //add title item to spinner
            for (int i = 0; i < returnArray.length; i++) {
                tempArray[i+1] = returnArray[i];
            }
            for (int i = 0; i < tempArray.length; i++) {
                tempArray[i] = tempArray[i].toLowerCase();
            }
            spinnerList = tempArray;
            return returnArray;
        }

    }

    //=======Timer handling for api calls!
    @Override
    public void onResume() {
        super.onResume();
        timerTest.schedule(new TimerTask() {
            @Override
            public void run() {
                int startSelection = editor.getSelectionStart();
                int selectLength = 0;
                String[] testforBlank = editor.getText().toString().split(" ");
                if (testforBlank[0] != "") {  //Test whether text field is blank
                    for (String currentWord : editor.getText().toString().split(" ")) {
                        if (currentWord != "") {
                            selectLength = selectLength + currentWord.length() + 1;
                            if (selectLength > startSelection) {
                                Log.d("currentWord", currentWord);
                                if (currentWord != lookupWord) {
                                    lookupWord = currentWord;
                                    new rhymeTask().execute(); //Make api call!
                                }
                                break;
                            }
                        }
                    }
                }

            }
        }, 0, 2000);
    }
    @Override
    public void onPause() {
        super.onPause();
        timerTest.cancel();
    }
    @Override
    public void onStop() {
        super.onStop();
        timerTest.cancel();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        timerTest.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (action.equals(Intent.ACTION_EDIT)) {
            getMenuInflater().inflate(R.menu.menu_editor, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    private void deleteNote() {
        getContentResolver().delete(NotesProvider.CONTENT_URI,
                noteFilter, null);
        Toast.makeText(this, getString(R.string.note_deleted),
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void finishEditing() {
        String newText = editor.getText().toString().trim();
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

    private void updateNote(String poemText, String poemTitle) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.POEM_TEXT, poemText);
        values.put(DBOpenHelper.POEM_TITLE,poemTitle);
        //System.out.println("notefilter " + noteFilter);
        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        //Toast.makeText(this, getString(R.string.note_updated), Toast.LENGTH_SHORT).show();



        setResult(RESULT_OK);
    }


    /*public void myClickHandler(View view) {
        String stringUrl1 = getString(R.string.url_chunk_1);
        String stringUrl2 = getString(R.string.url_chunk_1);
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //fetch data
        } else {
            //display error
        }
    }*/

    private void replaceText(String newText) {
        int start = Math.max(editor.getSelectionStart(), 0);
        int end = Math.max(editor.getSelectionEnd(), 0);
        editor.getText().replace(Math.min(start, end), Math.max(start, end),
                newText, 0, newText.length());
    }

    private void insertNote(String poemText, String poemTitle) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.POEM_TEXT, poemText);
        values.put(DBOpenHelper.POEM_TITLE, poemTitle);
        values.put(DBOpenHelper.CREATOR, "self");
        getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        setResult(RESULT_OK);
    }



    @Override
    public void onBackPressed() {
        finishEditing();
    }

    public void pushPoem(String poemTitle, String poemText) {

        Poem poem = new Poem(poemTitle, poemText);
        ct = new CommThread(this, EditPoemActivity.this);
        ct.start();
        ct.addPoem(poem);
        ct.interrupt();
    }


    @Override
    public void poemSaved(String output) {
        Toast.makeText(getApplicationContext(), output, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void serverDisconnected() {
        Toast.makeText(getApplication(), "Server not connected. Cannot save poems to server.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPullFinished() {

    }

}
