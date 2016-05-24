package com.example.stanza;

import android.support.v4.widget.SwipeRefreshLayout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Brianna on 5/12/2016.
 */
public class AccountCommThread extends Thread
implements Runnable{

    private static final String createAccount = "CREATE_ACCOUNT";
    private static final String accessAccount = "ACCESS_ACCOUNT";
    private static final String verifyAccount = "VERIFY_ACCOUNT";

    int port = 28411;
    String host = "rns202-17.cs.stolaf.edu";
    InputStream inputStream = null;
    OutputStream outputStream = null;

    AccountCommInterface accountCommInterface;
    SignupActivity signupActivity;
    LoginActivity loginActivity;

    String task_id;
    String error_message;

    boolean done = false;
    boolean action = false;

    Queue<Account> accountQueue = new LinkedList<Account>();


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


    public synchronized void thisAccount(String u, String e, String p){
        Account account = new Account(u, e, p);
        accountQueue.add(account);
        done = false;
        this.notify();

        System.out.println("account in system");
    }

    public boolean verifyAccount(Account account){
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
                error_message = verification.username;
            }
        }
        catch (RuntimeException e){
            e.printStackTrace();
        }

        return false;
    }

    public boolean accountExists(Account account){
        Account code = new Account(createAccount, null, null);
        Account verification = null;
        Account ack = null;
        System.out.println("in account exists");
        try{
            code.send(outputStream);
            System.out.println("sent create account code");
            ack = new Account(inputStream);
            account.send(outputStream);
            verification = new Account(inputStream);

            if(verification.username.equals("ACCOUNT_IS_VALID")){
                System.out.println("Account is valid");
                error_message = null;
                return true;
            }
            else{
                System.out.println("Account not valid");
                error_message = verification.username;
                System.out.println(error_message);
            }

        }catch(RuntimeException e){
            e.printStackTrace();
        }
        return false;

    }


    public void run() {
        boolean valid;
        System.out.println("in run");

        try {
            Socket socket = new Socket(host, port);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            System.out.println("Set up socket");

            System.out.println("sockets set up");

            while(!done) {
              //  System.out.println("hi");


                while (!accountQueue.isEmpty()) {
                    String ACCOUNTS = "ACCOUNTS";
                    byte[] b = ACCOUNTS.getBytes();
                    outputStream.write(b);
                    Account account = accountQueue.remove();
                    System.out.println("action is true");
                    if (task_id.equals(createAccount)) {
                        System.out.println("create account");
                        valid = accountExists(account);
                        if (valid) {
                            signupActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountCommInterface.onSignupSuccess();
                                }
                            });
                            done = true;

                        } else {
                            //account already exists --username already used or email already used
                            signupActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountCommInterface.onSignupFailed(error_message);
                                }
                            });
                            done = true;

                        }
                    } else if (task_id.equals(accessAccount)) {
                        valid = verifyAccount(account);
                        if (valid) {
                            loginActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountCommInterface.onLoginSuccess();
                                }
                            });
                            done = true;

                        } else {
                            //account does not exist
                            loginActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountCommInterface.onLoginFailed(error_message);
                                }
                            });
                            done = true;

                        }
                    }
                }

                try {
                    synchronized (this) {
                        while (accountQueue.isEmpty() && !done)
                            wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
