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
public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    EditText nameText;
    EditText emailText;
    EditText passwordText;
    Button signupButton;
    TextView loginLink;

    int minNameLength = 3;
    int minPasswordLength = 5;
    int maxPasswordLength = 13;

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
    }

    public void signup(){
        if(!validate()){
            onSignupFailed();
            return;
        }

        signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        //TODO: implement signup logic here

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onSignupSuccess();
                //onSignupFailed();
                progressDialog.dismiss();
            }
        }, 3000);
    }

    public void onSignupSuccess(){
        signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed(){
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

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
}
