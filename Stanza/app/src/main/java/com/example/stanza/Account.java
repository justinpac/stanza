package com.example.stanza;

import android.renderscript.ScriptGroup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

/**
 * This class authenticates the user
 */
public class Account {
    // class variables
    /**
     * A delimiter to separate username, email, and password in <code>Account</code>
     */
    static String fieldTerminator = "\001";
    /**
     * A string containing the username of the account in <code>Account</code>
     */
    String username;
    /**
     * A string containing the email of the account in <code>Account</code>
     */
    String email;
    /**
     * A string containing the password of the Account in <code>Account</code>
     */
    String password;

    // constructors

    /**
     * Creates the account with username, email, and password
     * @param u AKA username
     * @param e AKA email
     * @param p AKA password
     */
    public Account(String u, String e, String p) {
        username = u;
        email = e;
        password = p;
    }

    /**
     * Parses the account fields (username, email, password) using an inputstream
     * @param is contains username, email, and password. Is parsed and assigned.
     */
    public Account(InputStream is) {
        byte[] b = new byte[8192];
        try {
            is.read(b);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String temp = new String(b);
        StringTokenizer st = new StringTokenizer(temp, fieldTerminator, false);
        username = st.nextToken();
        email = st.nextToken();
        password = st.nextToken();
    }

    //methods

    /**
     * Sends the account information to the backend to authenticate
     * @param os an outputstream containing login info
     */
    public void send(OutputStream os){
        byte[] b = getBytes();
        try {
            os.write(b);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Sets the state variables to a string for sending out over a network
     * @return a string with the username, email, and password information
     */
    public String toString(){
        return "[username = " + username + ", email = " + email + ", password" + password + "]";
    }

    /**
     * turns our string into bytes for sending over a network
     * @return returns the byte-ified string.
     */
    public byte[] getBytes(){
        String temp = username + fieldTerminator + email + fieldTerminator + password + fieldTerminator;
        byte [] bytes = temp.getBytes();
        return bytes;
    }
}
