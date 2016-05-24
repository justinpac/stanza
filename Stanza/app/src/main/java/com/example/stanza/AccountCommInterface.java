package com.example.stanza;

/** Interface for the sign-in page.
 */


public interface AccountCommInterface {
    /**
     * Method called if the device is disconnected from the server
     */
    void onServerDisconnected();
    /**
     * Method called on successful login
     */
    void onLoginSuccess();

    /**
     * Method called on unsuccessful login
     * @param error_message given if app login fails
     */
    void onLoginFailed(String error_message);
    /**
     * Method called if new user signup succeeds
     */
    void onSignupSuccess();

    /**
     * Method called if new user signup fails
     * @param error_message given if app sign up fails (perhaps due to server issues)
     */
    void onSignupFailed(String error_message);
    void onFriendSuccess(String name);
    void onFriendFailure(String error_message);
}
