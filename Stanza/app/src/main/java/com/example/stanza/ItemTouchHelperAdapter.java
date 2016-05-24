package com.example.stanza;

/**
 * An interface to allow a <code>RecyclerView</code> to respond to swipe and drag events.
 */
public interface ItemTouchHelperAdapter {
    /**
     * A callback to allow an activity/fragment to respond to drag events in a <code>RecyclerView</code>.
     * @param fromPosition The initial position of the item within the <code>RecyclerView</code>.
     * @param toPosition The final position of the item within the <code>RecyclerView</code>.
     */
    void onItemMove(int fromPosition, int toPosition);

    /**
     * A callback to allow an activity/fragment to respond to swipe events in a <code>RecyclerView</code>.
     * @param position The position of the item within the <code>RecyclerView</code>.
     */
    void onItemDismiss(int position);

}
