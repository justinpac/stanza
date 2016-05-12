package com.example.stanza;

import android.support.v4.widget.SwipeRefreshLayout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Created by Brianna on 5/12/2016.
 */
public class AccountCommThread extends Thread
implements Runnable{

    private static final String createAccount = "CREATE_ACCOUNT";
    private static final String accessAccount = "ACCESS_ACCOUNT";
    private static final String verifyAccount = "VERIFY_ACCOUNT";

    int port = 28414;
    String host = "rns202-13.cs.stolaf.edu";
    InputStream inputStream = null;
    OutputStream outputStream = null;
    Account account;

    AccountCommInterface accountCommInterface;
    SignupActivity signupActivity;
    LoginActivity loginActivity;

    String task_id;
    String error_message;

    AccountCommThread(AccountCommInterface aci, SignupActivity sa){
        accountCommInterface = aci;
        signupActivity = sa;
        task_id = createAccount;
    }

    AccountCommThread(AccountCommInterface aci, LoginActivity la){
        accountCommInterface = aci;
        loginActivity = la;
        task_id = accessAccount;
    }


    public void getAccount(String u, String e, String p){
        account = new Account(u, e, p);
    }

    public boolean verifyAccount(){
        Account code = new Account(verifyAccount, null, null);
        Account verification = null;
        Account ack = null;
        try{
            code.send(outputStream);
            ack = new Account(inputStream);
            account.send(outputStream);
            verification = new Account(inputStream) ;
            if(verification.username.equals("ACCOUNT_IS_VALID")){
                error_message = null;
                return true;
            }
            else{
                error_message = verification.email;
            }
        }
        catch (RuntimeException e){
            e.printStackTrace();
        }

        return false;
    }

    public boolean accountExists(){
        Account code = new Account(createAccount, null, null);
        Account verification = null;
        Account ack = null;
        try{
            code.send(outputStream);
            ack = new Account(inputStream);
            account.send(outputStream);
            verification = new Account(inputStream);

            if(verification.username.equals("ACCOUNT_IS_VALID")){
                error_message = null;
                return true;
            }
            else{
                error_message = verification.email;
            }

        }catch(RuntimeException e){
            e.printStackTrace();
        }
        return false;

    }


    public void run() {
        boolean valid;

        try {
            Socket socket = new Socket(host, port);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            System.out.println("Set up socket");

            System.out.println("sockets set up");

            if(task_id.equals(createAccount)){
                valid = accountExists();
                if(valid){
                    signupActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            accountCommInterface.onSignupSuccess();
                        }
                    });

                }
                else{
                    //account already exists --username already used or email already used
                    signupActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            accountCommInterface.onSignupFailed(error_message);
                        }
                    });

                }
            }
            else if(task_id.equals(accessAccount)){
                valid = verifyAccount();
                if(valid){
                    loginActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            accountCommInterface.onLoginSuccess();
                        }
                    });

                }
                else{
                    //account does not exist
                    loginActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            accountCommInterface.onLoginFailed(error_message);
                        }
                    });

                }
            }
        } catch (ConnectException e) {
            e.printStackTrace();
            if (task_id.equals(createAccount)) {
                signupActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        accountCommInterface.onServerDisconnected();
                    }
                });
            } else if (task_id.equals(accessAccount)) {
                loginActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        accountCommInterface.onServerDisconnected();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }



}
