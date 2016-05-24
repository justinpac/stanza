package com.example.stanza;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * A class to define the interaction between ItemTouchHelper and the application. This controls
 * which touch behaviors per each <code>ViewHolder</code> within a <code>RecyclerView</code> and also receive callbacks when the
 * user performs these actions.
 */
public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    /**
     * An interface to allow a <code>RecyclerView</code> to respond to swipe and drag events.
     */
    private final ItemTouchHelperAdapter mAdapter;

    /**
     * A constructor for initializing <code>ItemTouchHelperCallback</code> using an <code></code>
     * @param adapter An interface to allow a <code>RecyclerView</code> to respond to swipe and drag events.
     */
    public ItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
    }

    /**
     * Determines if the user can drag items in the <code>ReyclerView</code> after a long press.
     * @return True if drag functionality is enabled, false otherwise.
     */
    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    /**
     * Determines if the user can swipe items in the <code>RecyclerView</code>.
     * @return True is swipe functionality is enabled, false otherwise.
     */
    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    /**
     * Specifies which directions of drags and swipes are supported.
     * @param recyclerView The <code>RecyclerView</code> to which <code>ItemTouchHelper</code> is attached.
     * @param viewHolder The <code>ViewHolder</code> for which the movement information is necessary.
     * @return Flags specifying which movements are allowed on this <code>ViewHolder</code>.
     */
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int swipeFlags = ItemTouchHelper.RIGHT; //ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    /**
     * Called when <code>ItemTouchHelper</code> wants to move the dragged item from its old position to the new position.
     * @param recyclerView The <code>RecyclerView</code> to which <code>ItemTouchHelper</code> is attached to.
     * @param source The <code>ViewHolder</code> which is being dragged by the user.
     * @param target The <code>ViewHolder</code> over which the currently active item is being dragged.
     * @return if the <code>ViewHolder</code> has been moved to the adapter position of target.
     */
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        mAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    /**
     * Called when a <code>ViewHolder</code> within a <code>RecyclerView</code> is swiped by the user.
     * @param viewHolder The ViewHolder which has been swiped by the user.
     * @param i The direction to which the ViewHolder is swiped.
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }
}