package com.example.stanza;

/**
 * A class that will help interact with the CommThread
 */
public interface CommInterface {
    // methods

    /**
     * Displays a message when Pull is finished
     */
    void onPullFinished();

    /**
     * Displays a message when the poem is saved to backend
     * @param output the title of the poem that will be displayed when saved
     */
    void poemSaved(String output);

    /**
     * Displays a message when the server is inaccessible
     */
    void serverDisconnected();
}
