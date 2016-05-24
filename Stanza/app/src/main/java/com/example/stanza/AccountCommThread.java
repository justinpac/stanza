package com.example.stanza;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This class is used to communicate with the backend server regarding accounts.
 * Namely, it is used in conjunction with the LoginActivity, SignupActivity, and
 * FriendBoardFragment to create accounts, authenticate accounts, and add friends.
 *
 * @author Brianna
 */
public class AccountCommThread extends Thread
implements Runnable{

    //state variables
    /**
     * Code to signify the task is creating an account.
     */
    private static final String createAccount = "CREATE_ACCOUNT";

    /**
     * Code to signify the task is accessing an account.
     */
    private static final String accessAccount = "ACCESS_ACCOUNT";

    /**
     * Code to signify the task is verifying the account.
     */
    private static final String verifyAccount = "VERIFY_ACCOUNT";

    /**
     * Code to signify the task is adding a friend.
     */
    private static final String addFriend = "ADD_FRIEND";

    /**
     * Code to signify the task is adding a friend via the FriendManagerActivity.
     */
    private static final String addFriendManage = "ADD_FRIEND_MANAGER";


    //class variables

    /**
     * The port that is used to connect to the backend server
     */
    int port = 28414;

    /**
     * The host name of the backend server.
     */
    String host = "rns202-7.cs.stolaf.edu";

    /**
     * An instance of an InputStream to communicate with the backend server.
     */
    InputStream inputStream = null;

    /**
     * An instance of an OutputStream to communicate with the backend server.
     */
    OutputStream outputStream = null;

    /**
     * An instance of AccountCommInterface to communicate with the UI activities.
     */
    AccountCommInterface accountCommInterface;

    /**
     * An instance of Signup Activity to perform backend tasks in conjunction
     * with SignupActivity.
     */
    SignupActivity signupActivity;

    /**
     * An instance of Login Activity to perform backend tasks in conjunction
     * with LoginActivity.
     */
    LoginActivity loginActivity;
    FriendBoardFragment friendBoardFragment;
    ManageFriendsActivity manageFriendsActivity;

    String task_id;
    String error_message;

    boolean done = false;

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

    AccountCommThread(AccountCommInterface aci, FriendBoardFragment fb){
        accountCommInterface = aci;
        friendBoardFragment = fb;
        task_id = addFriend;
    }
    AccountCommThread(AccountCommInterface aci, ManageFriendsActivity mfa){
        accountCommInterface = aci;
        manageFriendsActivity = mfa;
        task_id = addFriendManage;
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

    public boolean addFriend(Account account){
        Account code = new Account(addFriend, null, null);
        Account verification = null;
        Account ack = null;
        try{
            code.send(outputStream);
            ack = new Account(inputStream);
            account.send(outputStream);
            verification = new Account(inputStream) ;
            if(verification.username.equals("FRIEND_ACCEPTED")){
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
                    else if(task_id.equals(addFriend)){
                        valid = addFriend(account);
                        if(valid){
                            final String name = account.username;
                            //friend exists; add friend to friend list
                            friendBoardFragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountCommInterface.onFriendSuccess(name);
                                }
                            });
                            done = true;
                        }
                        else{
                            //friend doesn't exist; error message
                            friendBoardFragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountCommInterface.onFriendFailure(error_message);
                                }
                            });
                            done = true;
                        }
                    }else if(task_id.equals(addFriendManage)){
                        valid = addFriend(account);
                        if(valid){
                            final String name = account.username;
                            //friend exists; add friend to friend list
                            manageFriendsActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountCommInterface.onFriendSuccess(name);
                                }
                            });
                            done = true;
                        }
                        else{
                            //friend doesn't exist; error message
                            manageFriendsActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountCommInterface.onFriendFailure(error_message);
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
