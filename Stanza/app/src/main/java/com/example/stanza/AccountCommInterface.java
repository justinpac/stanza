package com.example.stanza;

/**
 * Created by Brianna on 5/12/2016.
 */
public interface AccountCommInterface {
    void onServerDisconnected();
    void onLoginSuccess();
    void onLoginFailed(String error_message);
    void onSignupSuccess();
    void onSignupFailed(String error_message);
}
