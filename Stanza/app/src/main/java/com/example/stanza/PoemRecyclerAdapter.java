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
 * An <code>Adapter</code> to provide binding from a data set of poems to card views that are
 * displayed within a <code>RecyclerView</code>.
 */
public class PoemRecyclerAdapter extends RecyclerView.Adapter<PoemRecyclerAdapter.PoemViewHolder> {

    /**
     * The <code>cursor</code> that will provide read-write access to database query of a set of poems.
     */
    Cursor poemCursor;

    /**
     * The <code>Context</code> indicating which fragment/activity we are interacting with.
     */
    Context context;

    /**
     * An instance of the interface for providing click functionality for each poem in a <code>RecyclerView</code>.
     */
    OnItemClickListener listener;

    /**
     * An interface for providing click functionality for each poem in a <code>RecyclerView</code>
     */
    public interface OnItemClickListener {
        /**
         * The callback that allows for implementation to respond to item clicks.
         * @param pid The id of the poem that was pressed.
         */
        void onItemClick(String pid);
    }

    /**
     * A constructor for <code>PoemRecyclerAdapter</code> using an <code>Activity</code> and a <code>Cursor</code>
     * @param a The <code>Activity</code> that the <code>RecyclerAdapter</code> resides in.
     * @param c The <code>Cursor</code> that holds the data set of poems to be displayed.
     */
    PoemRecyclerAdapter(Activity a, Cursor c) {
        context = a;
        poemCursor = c;
    }

    /**
     *  A class to describe an item view and metadata about its place within the <code>RecyclerView</code>.
     */
    public class PoemViewHolder extends RecyclerView.ViewHolder {
        /**
         * The layout defining what a poem card should look like.
         */
        CardView cardView;

        /**
         * The <code>TextView</code> for the poem title.
         */
        TextView poemTitle;

        /**
         * The <code>TextView</code> for the poem body.
         */
        TextView poemBody;

        /**
         * A constructor for PoemViewHolder that initializes the card content and set the
         * layout used to define the card view.
         * @param itemView
         */
        PoemViewHolder(View itemView){
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.poemCardView);
            poemTitle = (TextView) itemView.findViewById(R.id.poemTitle);
            poemBody = (TextView) itemView.findViewById(R.id.poemBody);
        }

        /**
         * This method invokes the <code>onItemClick(pid)</code> callback, passing in the
         * id of the poem that was clicked.
         * @param pid The id of the poem that was clicked.
         * @param listener The interface that allows a <code>RecyclerView</code> to respond to item clicks.
         */
        public void bind(final String pid, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    listener.onItemClick(pid);
                }
            });
        }
    }

    /**
     * Called when the <code>RecyclerView</code> needs a new <code>ViewHolder</code> of the given type to represent an item.
     * @param parent The <code>ViewGroup</code> into which the new <code>View</code> will be added after it is bound to an adapter position.
     * @param viewType The view type of the new <code>View</code>.
     * @return A new <code>ViewHolder</code> that holds a View of the given view type.
     */
    @Override
    public PoemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poem_card, parent, false);
        return new PoemViewHolder(view);
    }

    /**
     * initializer for the interface to provide click functionality.
     * @param l The interface that provides click functionality for each item in the <code>RecyclerView</code>.
     */
    public void setOnItemClickListener(OnItemClickListener l){
        listener = l;
    }

    /**
     * Changes the data set being displayed in the <code>RecyclerView</code>.
     * @param cursor The <code>Cursor</code> providing read-write access to the new data set to be displayed.
     * @return The previous data set that was being displayed.
     */
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

    /**
     * Called by RecyclerView to display the data at the specified position. This method updates
     * the contents of each poem card at the given position.
     * @param poemViewHolder The poem card to be updated.
     * @param position The position of the card within the poem data set.
     */
    @Override
    public void onBindViewHolder(PoemViewHolder poemViewHolder, int position) {
        poemCursor.moveToPosition(position);
        String pid = poemCursor.getString(poemCursor.getColumnIndex(DBOpenHelper.POEM_ID));
        String title = poemCursor.getString(poemCursor.getColumnIndex(DBOpenHelper.POEM_TITLE));
        String body = poemCursor.getString(poemCursor.getColumnIndex(DBOpenHelper.POEM_TEXT));


        //Break off the poem body with ellipsis at the third new line if it exists
        int pos  = ordinalIndexOf(body, '\n', 3);
        if (pos != -1) {
            body = body.substring(0, pos) + "\n...";
        }

        poemViewHolder.poemTitle.setText(title);
        poemViewHolder.poemBody.setText(body);
        poemViewHolder.bind(pid, listener);
    }

    /**
     * Get the number of items in the poem data set for the <code>RecyclerView</code>.
     * @return The number of items in the poem data set for the <code>RecyclerView</code>.
     */
    @Override
    public int getItemCount() {
        if (poemCursor == null) {
            return 0;
        }
        return poemCursor.getCount();
    }

    /**
     * Called by <code>RecyclerView</code> when it starts observing this <code>Adapter</code>.
     * @param recyclerView The <code>RecyclerView</code> instance which started observing this adapter.
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    /**
     * Get the index of the nth occurrence of a <code>char</code> within a <code>String</code>.
     * @param str The <code>String</code> to be searched.
     * @param c The <code>char</code> to find the occurrences of.
     * @param n The occurrence of the <code>char</code> you are looking for.
     * @return The index the index of the nth occurrence of the given <code>char</code> within the given <code>String</code>.
     */
    private static int ordinalIndexOf(String str, char c, int n) {
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos+1);
        return pos;
    }
}
