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
 * Created by Brianna on 5/11/2016.
 */
public class LoginActivity extends AppCompatActivity
implements AccountCommInterface{
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    EditText emailText;
    EditText passwordText;
    Button loginButton;
    TextView signupLink;

    int minPasswordLength = 5;
    int maxPasswordLength = 13;

    AccountCommThread act;

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


    public void login(){
        if(!validate()){
            invalidLoginAttempt();
            return;
        }

        loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        //TODO; Implement authentication here
        act.thisAccount(null, email, password);
        //will either call onLoginSuccess() or onLoginFailed();

        //currently we're still just calling onLoginSuccess() here
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onLoginSuccess();
                progressDialog.dismiss();
            }
        },3000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==REQUEST_SIGNUP){
            if(resultCode==RESULT_OK){
                //TODO: Implement successful signup logic here
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }

    public void invalidLoginAttempt(){
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    public boolean validate(){
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailText.setError("Enter a valid email address");
            valid = false;
        }else{
            emailText.setError(null);
        }

        if(password.isEmpty() || password.length() < minPasswordLength || password.length() > maxPasswordLength){
            passwordText.setError("Passwords must be between " + minPasswordLength + " and " + maxPasswordLength + " characters.");
            valid = false;
        }else{
            passwordText.setError(null);
        }

        return valid;
    }



    @Override
    public void onServerDisconnected() {
        Toast.makeText(getBaseContext(), "Server disconnected. Cannot login.", Toast.LENGTH_LONG).show();
    }

    public void onLoginSuccess(){
        loginButton.setEnabled(true);
        finish();
    }

    @Override
    public void onLoginFailed(String error_message) {
        Toast.makeText(getBaseContext(), error_message, Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    @Override
    public void onSignupSuccess() {

    }

    @Override
    public void onSignupFailed(String error_message) {

    }


}
