package com.example.stanza;

/**
 * Created by Brianna on 4/27/2016.
 */
public interface CommInterface {

    void pushPoem(String poemTitle, String poemText);
    void pullPoem(String poemTitle, String poemText);
    void poemSaved(String output);
}
