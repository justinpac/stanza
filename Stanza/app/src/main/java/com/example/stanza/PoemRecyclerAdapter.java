package com.example.stanza;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Justin on 5/15/2016.
 */
public class PoemRecyclerAdapter extends RecyclerView.Adapter<PoemRecyclerAdapter.PoemViewHolder>{

    Cursor poemCursor;
    Context context;
    OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String pid);
    }

    PoemRecyclerAdapter(Activity a, Cursor c) {
        context = a;
        poemCursor = c;
    }

    public class PoemViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView poemTitle;
        TextView poemBody;

        PoemViewHolder(View itemView){
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.poemCardView);
            poemTitle = (TextView) itemView.findViewById(R.id.poemTitle);
            poemBody = (TextView) itemView.findViewById(R.id.poemBody);
        }

        public void bind(final String pid, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    listener.onItemClick(pid);
                }
            });
        }
    }

    //Specify the layout each item of the Recycler uses.
    @Override
    public PoemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poem_card, parent, false);
        return new PoemViewHolder(view);
    }

    public void setOnItemClickListener(OnItemClickListener l){
        listener = l;
    }

    public Cursor swapCursor(Cursor cursor) {
        if (poemCursor == cursor) {
            return null;
        }
        Cursor oldCursor = poemCursor;
        poemCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    //Sets a listener for item clicks


    //Specify the contents of each item of the RecyclerView.
    @Override
    public void onBindViewHolder(PoemViewHolder poemViewHolder, int position) {
        poemCursor.moveToPosition(position);
        String pid = poemCursor.getString(poemCursor.getColumnIndex(DBOpenHelper.POEM_ID));
        String title = poemCursor.getString(poemCursor.getColumnIndex(DBOpenHelper.POEM_TITLE));
        String body = poemCursor.getString(poemCursor.getColumnIndex(DBOpenHelper.POEM_TEXT));


        //Break off the poem body with ellipsis at the third new line if it exists
        int pos  = ordinalIndexOf(body, '\n', 3);
        if (pos != -1) {
            body = body.substring(0, pos) + " ...";
        }

        poemViewHolder.poemTitle.setText(title);
        poemViewHolder.poemBody.setText(body);
        poemViewHolder.bind(pid, listener);
    }

    //get number of Poems in RecyclerView
    @Override
    public int getItemCount() {
        if (poemCursor == null) {
            return 0;
        }
        return poemCursor.getCount();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private static int ordinalIndexOf(String str, char c, int n) {
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos+1);
        return pos;
    }
}
