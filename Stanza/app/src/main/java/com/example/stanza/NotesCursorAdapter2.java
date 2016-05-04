package com.example.stanza;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class NotesCursorAdapter2 extends CursorAdapter {
    public NotesCursorAdapter2(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(
                R.layout.note_list_item, parent, false
        );
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String noteTitle = cursor.getString(
                cursor.getColumnIndex(FriendDPOpenHelper.FRIEND_TITLE));

        if (noteTitle == null) {noteTitle = context.getString(R.string.unnamed_poem);}

        int pos = noteTitle.indexOf(10);
        if (pos != -1) {
            noteTitle = noteTitle.substring(0, pos) + " ...";
        }

        TextView tv = (TextView) view.findViewById(R.id.tvNote);
        tv.setText(noteTitle);

    }
}
