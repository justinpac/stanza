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
 */
public class AccountCommThread extends Thread
        implements Runnable{

    //class variables
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


    //state variables

    /**
     * The port that is used to connect to the backend server
     */
    int port = 28414;

    /**
     * The host name of the backend server.
     */
    String host = "rns202-13.cs.stolaf.edu";

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

    /**
     * An instance of FriendBoardFragment to perform backend tasks in conjunction
     * with LoginActivity.
     */
    FriendBoardFragment friendBoardFragment;

    /**
     * An instance of ManageFriendsActivity to perform backend tasks in conjunction
     * with LoginActivity.
     */
    ManageFriendsActivity manageFriendsActivity;

    /**
     * Code that designates which task is being performed by the AccountCommThread
     */
    String task_id;

    /**
     * Identifies the error message if the username/email already existed or it was not a valid
     * email/password combination used in LoginActivity.
     */
    String error_message;

    /**
     * Determines when the thread should stop running.
     */
    boolean done = false;

    /**
     * Holds the account that is being created, being authenticated, or being added as a friend.
     */
    Queue<Account> accountQueue = new LinkedList<Account>();


    //constructors

    /**
     * Creates an instance of AccountCommThread to enable communication between the backend server
     * and SignupActivity.
     * @param aci An instance of AccountCommInterface
     * @param sa An instance of SignupActivity
     */
    AccountCommThread(AccountCommInterface aci, SignupActivity sa){
        accountCommInterface = aci;
        signupActivity = sa;
        task_id = createAccount;
    }

    /**
     * Creates an instance of AccountCommThread to enable communication between the backend server
     * and LoginActivity.
     * @param aci An instance of AccountCommInterface.
     * @param la An instance of LoginActivity.
     */
    AccountCommThread(AccountCommInterface aci, LoginActivity la){
        accountCommInterface = aci;
        loginActivity = la;
        task_id = accessAccount;
    }

    /**
     * Creates an instance of AccountCommThread to enable communication between the backend server
     * and FriendBoardFragment.
     * @param aci An instance of AccountCommInterface.
     * @param fb An instance of FriendBoardFragment.
     */
    AccountCommThread(AccountCommInterface aci, FriendBoardFragment fb){
        accountCommInterface = aci;
        friendBoardFragment = fb;
        task_id = addFriend;
    }

    /**
     * Creates an instance of AccountCommThread to enable communication between the backend server
     * and ManagerFriendsAcivity.
     * @param aci An instance of AccountCommInterface
     * @param mfa An instance of ManageFriendsActivity
     */
    AccountCommThread(AccountCommInterface aci, ManageFriendsActivity mfa){
        accountCommInterface = aci;
        manageFriendsActivity = mfa;
        task_id = addFriendManage;
    }



    //methods

    /**
     * Adds an account to the account queue. Notifies the thread that it is time to work.
     * @param u The username.
     * @param e The email.
     * @param p The password.
     */
    public synchronized void thisAccount(String u, String e, String p){
        Account account = new Account(u, e, p);
        accountQueue.add(account);
        done = false;
        this.notify();

        System.out.println("account in system");
    }

    /**
     * Sends the account information to the backend server and receives verification of whether
     * or nothis account exists. Called as part of the login process.
     * @param account The account that is being verified. Specifically, does this email/password
     *                combination exist.
     * @return True/false depending on whether or not the account exists.
     */
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

    /**
     * Sends account information to the backend server (containing the username of the friend)
     * and receives verification of whether or not the friend's account exists.
     * @param account An instance of Account containing the desired friend's username.
     * @return True/false depending on whether the friend's account exists.
     */
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

    /**
     * Sends an account to the backend and receives verification of whether or not the account
     * already exists. Used as part of the signup process.
     * @param account An instance of account containing the account information for that account
     *                which the user is trying to create.
     * @return True if the user can create the account with that username/email or false if the
     * account is invalid (the username or email has already been taken).
     */
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

    /**
     * Takes care of all communication with the backend when there is an account in the accounts
     * queue. Utitlizes the task_id that has been defined in the constructors to determine which
     * task is currently being handled--creating an account, authenticating an account, or
     * adding a friend. Sends a message to the backend telling it which task is being performed.
     * Then sends the account information to the backend. Finally, it receives a true/false
     * validation regarding the success of the task and informs the proper activity.
     */
    public void run() {
        boolean valid;

        try {
            Socket socket = new Socket(host, port);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            while(!done) {
                //do work when there is an account in the queue
                while (!accountQueue.isEmpty()) {
                    //tell the backend we're working with accounts and not poems
                    String ACCOUNTS = "ACCOUNTS";
                    byte[] b = ACCOUNTS.getBytes();
                    outputStream.write(b);
                    Account account = accountQueue.remove();

                    //CREATING AN ACCOUNT
                    if (task_id.equals(createAccount)) {
                        valid = accountExists(account);
                        //if successful, inform SignupActivity and allow the user into the app
                        if (valid) {
                            signupActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountCommInterface.onSignupSuccess();
                                }
                            });
                            done = true;
                        }
                        //if not successful, inform SignupActivity what the error was
                        else {
                            signupActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountCommInterface.onSignupFailed(error_message);
                                }
                            });
                            done = true;
                        }
                    }

                    //AUTHENTICATING AN ACCOUNT
                    else if (task_id.equals(accessAccount)) {
                        valid = verifyAccount(account);
                        //if successful, inform LoginActivity and let the user into the app
                        if (valid) {
                            loginActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountCommInterface.onLoginSuccess();
                                }
                            });
                            done = true;
                        }
                        //if not successful, inform LoginActivity what the error was
                        else {
                            loginActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountCommInterface.onLoginFailed(error_message);
                                }
                            });
                            done = true;
                        }
                    }

                    //ADDING A FRIEND
                    else if(task_id.equals(addFriend)){
                        valid = addFriend(account);
                        //if successful, inform FriendBoardFragment so the friend can be added to the local database
                        if(valid){
                            final String name = account.username;
                            friendBoardFragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountCommInterface.onFriendSuccess(name);
                                }
                            });
                            done = true;
                        }
                        //if not successful, inform FriendBoardFragment that friend doesn't exist
                        else{
                            friendBoardFragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountCommInterface.onFriendFailure(error_message);
                                }
                            });
                            done = true;
                        }
                    }

                    //ADDING A FRIEND VIA THE FRIEND_MANAGER_ACTIVITY
                    else if(task_id.equals(addFriendManage)){
                        valid = addFriend(account);
                        //if successful, inform FriendManagerActivity so that friend can be added to local database
                        if(valid){
                            final String name = account.username;
                            manageFriendsActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accountCommInterface.onFriendSuccess(name);
                                }
                            });
                            done = true;
                        }
                        //if not successful, inform FriendManager that friend does not exist
                        else{
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

                //if there is not yet an account added to the queue, wait for the account
                try {
                    synchronized (this) {
                        while (accountQueue.isEmpty() && !done)
                            wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //display server disconnected messages on UI thread if the server is disconnected
        catch (ConnectException e) {
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
