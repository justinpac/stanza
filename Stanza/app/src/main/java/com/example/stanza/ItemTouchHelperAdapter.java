package com.example.stanza;

/**
 * Created by Justin on 5/22/2016.
 */
public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}
