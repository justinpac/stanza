package com.example.stanza;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

/**
 * A class to allow a user to create an account for the Stanza app.
 */

public class SignupActivity extends AppCompatActivity
implements AccountCommInterface{

    //state variables

    /**
     * The text field into which the user will enter a username in this instance of <code>SignupActivity</code>
     */
    EditText nameText;

    /**
     * The text field into which the user will enter an email in this instance of <code>SignupActivity</code>
     */
    EditText emailText;

    /**
     * The text field into which the user will enter a password in this instance of <code>SignupActivity</code>
     */
    EditText passwordText;

    /**
     * A button that will when pressed will initiate the process of verifying this is a valid account
     * and creating said account in this instance of <code>SignupActivity</code>
     */
    Button signupButton;

    /**
     * This links to the LoginActivity and when pressed will take the user to the LoginActivity
     */
    TextView loginLink;

    /**
     * The minimum character length for a username
     */
    int minNameLength = 3;

    /**
     * The minimum character length for a password
     */
    int minPasswordLength = 5;

    /**
     * The maximum character length for a password
     */
    int maxPasswordLength = 13;

    /**
     * An instance of AccountCommThread. Used to verify that this is a valid account the user is
     * trying to create and to store the account information in the backend server.
     */
    AccountCommThread act;


    //methods

    /**
     * Upon creation of the activity, all view fields (for username, email, and password) are
     * initiated and onClickListeners for the signup button and login link are set up. An instance
     * of AccountCommThread is initiated for communication with the backend.
     * @param savedInstanceState a reference to a Bundle object passed into the Activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameText = (EditText) findViewById(R.id.input_name);
        emailText = (EditText) findViewById(R.id.input_email);
        passwordText = (EditText) findViewById(R.id.input_password);
        signupButton = (Button) findViewById(R.id.btn_signup);
        loginLink = (TextView) findViewById(R.id.link_login);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        act = new AccountCommThread(this, SignupActivity.this);
        act.start();
    }

    /**
     * Upon restarting the activity, an instance of AccountCommThread will be initialized.
     */
    @Override
    public void onRestart(){
        super.onRestart();
        act = new AccountCommThread(this, SignupActivity.this);
        act.start();
    }

    /**
     * Called when the signup button is pressed. First checks if the account information is valid.
     * If it is, then an AccountCommThread is initiated to check whether the account information is
     * already being used. If it is not, then the account information will be saved to the backend.
     */
    public void signup(){
        if(!validate()){
            invalidAccount();;
            return;
        }

        signupButton.setEnabled(false);

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        act.thisAccount(name, email, password);
    }

    /**
     * Called if the account information is invalid. Displays a message to the user to this effect.
     */
    public void invalidAccount(){
        Toast.makeText(getBaseContext(), "Invalid account information", Toast.LENGTH_LONG).show();
        signupButton.setEnabled(true);
    }

    /**
     * Ensure that the information (username, email, and password) are all valid entries. Specifically,
     * that the username and password meet the minimum/maximum character requirements and that the email
     * is actually an email. Set an error message if any of these conditions are not satisfied
     * @return True/false depending on whether the account information entered is valid.
     */
    public boolean validate(){
        boolean valid = true;

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        //username must exist and be a minimum number of characters
        if(name.isEmpty() || name.length() < minNameLength){
            nameText.setError("At least " + minNameLength + " characters.");
            valid = false;
        }else{
            nameText.setError(null);
        }

        //email must exist and follow email address patterns
        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailText.setError("Enter a valid email address.");
            valid = false;
        }else{
            emailText.setError(null);
        }

        //password must exist and meet min/max character limits
        if(password.isEmpty() || password.length() < minPasswordLength || password.length() > maxPasswordLength){
            passwordText.setError("Password must be between " + minPasswordLength + " and " + maxPasswordLength + " characters.");
            valid = false;
        }else{
            passwordText.setError(null);
        }

        return valid;
    }

    /**
     * Called from an instance of AccountCommThread if connection to the backend is unsuccessful.
     */
    @Override
    public void onServerDisconnected() {
        Toast.makeText(getBaseContext(), "Server disconnected. Cannot create account.", Toast.LENGTH_LONG).show();
    }

    /**
     * Called from an instance of AccountCommThread if sign-up is sucessful. Upon success, the user
     * will be logged into the app, and his/her account information would have been stored in the
     * accounts table in the backend.
     */
    public void onSignupSuccess(){
        signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    /**
     * Called from an instance of AccountCommThread if sign-up fails. This will occur if either
     * the username or email are already used and stored in the accounts table in the backend.
     *
     * @param error_message A message describing what the sign-up error was
     */
    @Override
    public void onSignupFailed(String error_message) {
        signupButton.setEnabled(true);
        Toast.makeText(getBaseContext(), error_message, Toast.LENGTH_LONG).show();
        onRestart();
    }

    //these interface methods are not implemented in SignupActivity
    @Override public void onLoginSuccess() {}
    @Override public void onLoginFailed(String error_message) {}
    @Override public void onFriendSuccess(String name) {}
    @Override public void onFriendFailure(String error_message) {}
}
