package com.example.stanza;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.stanza.DBOpenHelper;

import java.io.InputStream;
import java.io.OutputStream;


public class EditPoemActivity extends AppCompatActivity
implements CommInterface{

    private String action;
    private EditText editorTitle;
    private EditText editor;
    private String noteFilter;
    private String oldText;
    private String oldTitle;

 //   private Button publish;
    private CommThread ct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        editorTitle = (EditText) findViewById(R.id.editText2);
        editor = (EditText) findViewById(R.id.editText);

       // publish = (Button) findViewById(R.id.publish_poem_button);
        ct = new CommThread(this, EditPoemActivity.this);
        ct.start();

        Intent intent = getIntent();
        long id = intent.getLongExtra(NotesProvider.CONTENT_ITEM_TYPE,-1);
               

        if (id == -1) {
            action = Intent.ACTION_INSERT;
            setTitle(getString(R.string.new_note));
        } else {
            action = Intent.ACTION_EDIT;
            //System.out.println("last path segment " + uri.getLastPathSegment());
            noteFilter = DBOpenHelper.POEM_ID + "=" + id; //uri.getLastPathSegment();

            Cursor cursor = getContentResolver().query(NotesProvider.CONTENT_URI,
                    DBOpenHelper.ALL_COLUMNS, noteFilter, null, null);
            cursor.moveToFirst();
            oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.POEM_TEXT));
            oldTitle = cursor.getString(cursor.getColumnIndex(DBOpenHelper.POEM_TITLE));
            editor.setText(oldText);
            editorTitle.setText(oldTitle);
            editor.requestFocus();
        }


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
                String title = editorTitle.getText().toString().trim() +"\001";
                String text = editor.getText().toString().trim() + "\001";
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
        System.out.println("notefilter " + noteFilter);
        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        //Toast.makeText(this, getString(R.string.note_updated), Toast.LENGTH_SHORT).show();



        setResult(RESULT_OK);
    }


    private void insertNote(String poemText, String poemTitle) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.POEM_TEXT, poemText);
        values.put(DBOpenHelper.POEM_TITLE,poemTitle);
        getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        setResult(RESULT_OK);
    }



    @Override
    public void onBackPressed() {
        finishEditing();
    }


    @Override
    public void pushPoem(String poemTitle, String poemText) {

        Poem poem = new Poem(poemTitle, poemText);
        ct.addPoem(poem);
    }

    @Override
    public void pullPoem(String poemTitle, String poemText) {

    }

    @Override
    public void poemSaved(String output) {
        Toast.makeText(getApplicationContext(), output, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void serverNotConnected() {
        Toast.makeText(getApplicationContext(), "Action failed. Server is not connected.", Toast.LENGTH_SHORT).show();
    }
}
