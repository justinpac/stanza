package com.example.stanza;

/**
 * Created by Brianna on 4/27/2016.
 */

//adding to git
public interface CommInterface {

    void pushPoem(String poemTitle, String poemText);
    void pullPoem();
    void onPullFinished();
    void poemSaved(String output);
    void serverDisconnected();
}
