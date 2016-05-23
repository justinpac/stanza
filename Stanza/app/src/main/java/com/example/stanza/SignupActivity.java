package com.example.stanza;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Brianna on 5/11/2016.
 */
public class SignupActivity extends AppCompatActivity
implements AccountCommInterface{

    private static final String TAG = "SignupActivity";

    EditText nameText;
    EditText emailText;
    EditText passwordText;
    Button signupButton;
    TextView loginLink;

    int minNameLength = 3;
    int minPasswordLength = 5;
    int maxPasswordLength = 13;

    AccountCommThread act;
   // ProgressDialog progressDialog;

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

    @Override
    public void onRestart(){
        super.onRestart();
        act = new AccountCommThread(this, SignupActivity.this);
        act.start();
    }

    public void signup(){
        if(!validate()){
            invalidAccount();;
            return;
        }

        System.out.println("valid fields");
        signupButton.setEnabled(false);

      /*  progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();
        */

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        System.out.println("add account to queue");
        act.thisAccount(name, email, password);
        //it will call either onSignupSuccess() or onSignupFailed()

        //currently we're just going to automatically call onSignupSuccess() here
     /*   new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onSignupSuccess();
                //onSignupFailed();
                progressDialog.dismiss();
            }
        }, 3000);*/

    }


    public void invalidAccount(){
        Toast.makeText(getBaseContext(), "Invalid account information", Toast.LENGTH_LONG).show();

        signupButton.setEnabled(true);
    }

    public boolean validate(){
        boolean valid = true;

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if(name.isEmpty() || name.length() < minNameLength){
            nameText.setError("At least " + minNameLength + " characters.");
            valid = false;
        }else{
            nameText.setError(null);
        }

        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailText.setError("Enter a valid email address.");
            valid = false;
        }else{
            emailText.setError(null);
        }

        if(password.isEmpty() || password.length() < minPasswordLength || password.length() > maxPasswordLength){
            passwordText.setError("Password must be between " + minPasswordLength + " and " + maxPasswordLength + " characters.");
            valid = false;
        }else{
            passwordText.setError(null);
        }

        return valid;
    }


    @Override
    public void onServerDisconnected() {
        Toast.makeText(getBaseContext(), "Server disconnected. Cannot create account.", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onLoginSuccess() {

    }

    @Override
    public void onLoginFailed(String error_message) {

    }

    public void onSignupSuccess(){
       // progressDialog.dismiss();
      //  Toast.makeText(getBaseContext(), "Account created.", Toast.LENGTH_SHORT).show();
        signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void onSignupFailed(String error_message) {
       // progressDialog.dismiss();
        signupButton.setEnabled(true);
        Toast.makeText(getBaseContext(), error_message, Toast.LENGTH_LONG).show();
        onRestart();

    }

    @Override
    public void onFriendSuccess(String name) {

    }

    @Override
    public void onFriendFailure(String error_message) {

    }
}
