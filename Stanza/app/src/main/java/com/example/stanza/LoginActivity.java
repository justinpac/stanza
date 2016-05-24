package com.example.stanza;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

/**
 * A class to allow the user to login to the Stanza ap.
 */
public class LoginActivity extends AppCompatActivity
implements AccountCommInterface{

    //class variables

    /**
     * A code designating that the LoginActivity can start the SignupActivity for a result.
     */
    private static final int REQUEST_SIGNUP = 0;

    /**
     * The email used to login to the app. Will be used to identify the author of poetry submitted
     * for publication to the backend.
     */
    static String user;

    //state variables

    /**
     * Field to enter the email used to login to the app.
     */
    EditText emailText;

    /**
     * Field to enter the password used to login to the app.
     */
    EditText passwordText;

    /**
     * Button that when pressed will send the account information to the server, and upon verification
     * of the account, will allow the user to login to the app.
     */
    Button loginButton;

    /**
     * Denotes a link to the SignupActivit if the user has not yet created an account.
     */
    TextView signupLink;

    /**
     * The minimum character length for a password.
     */
    int minPasswordLength = 5;

    /**
     * The maximum character length for a password.
     */
    int maxPasswordLength = 13;

    /**
     * An instance of AccountCommThread for communication with the backend in order to authenticate
     * the Stanza account.
     */
    AccountCommThread act;


    //methods

    /**
     * Upon creation of the activity, all view fields (for email and password) are
     * initiated and onClickListeners for the login button and signup link are set up. An instance
     * of AccountCommThread is initiated for communication with the backend.
     * @param savedInstanceState a reference to a Bundle object passed into the Activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_login);

        emailText = (EditText) findViewById(R.id.input_email);
        passwordText = (EditText) findViewById(R.id.input_password);
        loginButton = (Button) findViewById(R.id.btn_login);
        signupLink = (TextView) findViewById(R.id.link_signup);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });

        act = new AccountCommThread(this, LoginActivity.this);
        act.start();
    }

    /**
     * Upon restarting the activity, an instance of AccountCommThread will be initialized.
     */
    @Override
    public void onRestart(){
        super.onRestart();
        act = new AccountCommThread(this, LoginActivity.this);
        act.start();
    }

    /**
     * Called when the login button is pressed. First checks if the account information is valid.
     * If it is, then an AccountCommThread is initiated to check whether the account information is
     * a valid email/password combination. If it is, the user will be logged into the app. If it is
     * not, then invalidLoginAttempt() will be called.
     */
    public void login(){
        if(!validate()){
            invalidLoginAttempt();
            return;
        }

        loginButton.setEnabled(false);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        act.thisAccount(null, email, password);

        //BELOW LETS YOU PAST THE LOGIN SCREEN WITH ANY EMAIL + PASSWORD
        //REMOVE THIS BEFORE TURNING IN

        //currently we're still just calling onLoginSuccess() here
/*        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onLoginSuccess();
               // progressDialog.dismiss();
            }
        },1000);*/
    }

    /**
     * Called upon successful completion of SignupActivity. If creating an account was successful,
     * the user will be automatically logged into the app.
     *
     * @param requestCode The activity code sent to SignupActivity.
     * @param resultCode The result code received from SignupActivity.
     * @param data The intent used to start the SignupActivity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==REQUEST_SIGNUP){
            if(resultCode==RESULT_OK){
                this.finish();
            }
        }
    }

    /**
     * Override the back button in this activity such that this activity is the only one visible to
     * the user prior to a successful login.
     */
    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }

    /**
     * Called if the login information is invalid. Displays a message to the user to this effect.
     */
    public void invalidLoginAttempt(){
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    /**
     * Ensure that the information (email, and password) are valid entries. Specifically,
     * that the password meet the minimum/maximum character requirements and that the email
     * is actually an email. Set an error message if any of these conditions are not satisfied
     * @return True/false depending on whether the account information entered is valid.
     */
    public boolean validate(){
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        //email must exist and follow proper email patterns
        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailText.setError("Enter a valid email address");
            valid = false;
        }else{
            emailText.setError(null);
        }

        //password must exist and obey min/max character limits
        if(password.isEmpty() || password.length() < minPasswordLength || password.length() > maxPasswordLength){
            passwordText.setError("Passwords must be between " + minPasswordLength + " and " + maxPasswordLength + " characters.");
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
        Toast.makeText(getBaseContext(), "Server disconnected. Cannot login.", Toast.LENGTH_LONG).show();
    }

    /**
     * Called from an instance of AccountCommThread if login is successful. The email used to login
     * will be saved as the user for use in publishing poems, and the user will be allowed into the
     * app.
     */
    public void onLoginSuccess(){
        loginButton.setEnabled(true);
        user = emailText.getText().toString();  ;
        finish();
    }

    /**
     * Called from an instance of AccountCommThread if login fails. This will occur if the user has
     * entered an invalid email/password combination. Will display an error message describing the
     * issue.
     *
     * @param error_message A message describing what the login error is.
     */
    @Override
    public void onLoginFailed(String error_message) {
        Toast.makeText(getBaseContext(), error_message, Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
        onRestart();
    }

    //these interface methods are not implemented in LoginActivity
    @Override public void onSignupSuccess() {}
    @Override public void onSignupFailed(String error_message) {}
    @Override public void onFriendSuccess(String name) {}
    @Override public void onFriendFailure(String error_message) {}
}
