/**
 * This class is the entire backend. It receives requests from the client regarding either poems or accounts. 
 * Poem tasks involve saving poems to the backend database and sending friend poetry to the client. 
 * Account tasks involve creating accounts with unique usernames and emails, authenticating 
 * emails/passwords upon login, and verifying the existence of friend accounts when adding friends. 
 *
 **/

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Vector;
import java.util.StringTokenizer; 


public class Backend {
    /**
     * the maximum number of bytes in the buffer 
     **/
    static final int maxinBuff = 1000;

    /**
     * an instance of OutputStream to communicate with the clientr 
     **/
    public static OutputStream outStream = null;
    
    /**
     * A default Poem to use for acknowledging failed client requests 
     **/
    public static  Poem nack = null; 

    /**
     * Receives an incoming connection from a client and creates a worker thread for that client 
     * to perform the relevant tasks. 
     **/
  public static void main(String[] args) {
    try {
      int port = Integer.parseInt(args[0]);
      System.out.println("Initializing for network communication... ");
      ServerSocket servSock = new ServerSocket(port);
      
      Poem p = null;
      
      /* assert:  ServerSocket successfully created */

      System.out.println("Waiting for an incoming connection... ");

      while(true){

	  Socket inSock = servSock.accept();
	  Thread t = new Thread(new Worker(inSock));
	  t.start();   
      }
    }
    catch(IOException e){
    }
  }
}

/**
 * Is a separate thread for each backend request made by each client. Handles the saving and retrieving poems. 
 * Also handles creating and authenticating accounts, as well as adding friends. 
 **/

class Worker implements Runnable{
    /**
     * The socket used for the connection with the client. 
     **/
    Socket sock;

    /**
     * An instance of OutputStream for communication with the client. 
     **/
    OutputStream outStream = null;

    /**
     * An instance of InputStream for communication with the client. 
     **/
    InputStream inStream = null;

    /**
     * A instance of poem to receive requests from the client regarding pushing vs pulling poems  
     **/
    Poem p = null;

    /**
     * An instance of poem to receive the length of the poem that will be saved to the server. 
     **/
    Poem test = null;

    /**
     * An instance of poem to read in the actual poem to be saved to the server. 
     **/
    Poem read_in = null; 

    /**
     * An instance of poem to acknowledge successful requests made by the client. 
     **/
    Poem ack = null;

    /**
     * An instance of poem to acknowledge failed requests from the client. 
     **/
    Poem  nack = null;


    /**
     * An instance of account to receive a request from the client regarding creating an account vs
     * authenticating an account vs adding a friend.  
     **/
    Account code = null;

    /**
     * An instance of account to receive the account information from the client to be either created or verified that it exists.  
     **/
    Account account = null;


    /**
     * To connect to the postgres database. 
     **/
    String url = "jdbc:postgresql://shelob.cs.stolaf.edu:5432/mca_s16";

    /**
     * The schema within the postgres database. 
     **/
    String user = "mca_s16_poem";

    /**
     * The password used by the app to access the postgres database. 
     **/
    String password = "Javaphone";

    /**
     * Determines which task is being performed by the backend. 
     **/
    int task_code;

    /**
     * Receives the instruction from the client regarding whether the backend is currently handling
     * poems or accounts.  
     **/
    byte[] from_client_bytes = new byte[8192];

    /**
     * Signifies that the backend is working with poems and not accounts.  
     **/
    String POEMS = "POEMS";

    /**
     * Signifies that the backend is working with accounts and not poems.  
     **/
    String ACCOUNTS = "ACCOUNTS"; 

    /**
     * Create an instance of worker on the the given socket. 
     **/
    Worker(Socket s){sock = s;};


    /**
     * Pulls poems from the poems table and sends each of them to the client. Recieves the ResultSet from the 
     * pullFromDatabase() method and iterates over the Result Set to send each poem to the client. 
     **/
    public void pullPoems(int numFriends){
	Poem ack = null;
	String title, text, author, temp_friend; 

	byte [] friend_list = new byte [8192];
	String [] all_friends = new String[numFriends];  


	//if a person has no friends, they have no poems to pull 
	 if(numFriends == 0){
		Poem endPoems = new Poem("END_PULL", null, null);
		endPoems.send(outStream); 
		return;
	 }
	
	    
	try{
	    //extract the list of friends from the client and store them in an array 
	    inStream.read(friend_list);
	    String temp = new String(friend_list);
	    StringTokenizer st = new StringTokenizer(temp, "\001", false);
	   
	    for(int i=0; i<numFriends; i++){
		temp_friend = st.nextToken();
		all_friends[i] = temp_friend;
	    }


	    //query the database with the friends from the client 
	    ResultSet rs = pullFromDatabase(all_friends, numFriends);


	    //send each Poem from the Result Set to the client 
	    int numPoems = 0; 
	    while(rs.next()){
		//if there are no results, there is nothing to send back 
		if(rs.getRow() == 0){
		    Poem endPoems = new Poem("END_PULL", null, null);
		    endPoems.send(outStream);
		    return;
		}

		//format the poem to send to the client 
		title = rs.getString(2);
		text = rs.getString(3);
		author = rs.getString(5); 

		//send the poem (preceded by its byte length) 
		Poem p = new Poem(title, text, author);
		int poemLength = p.getBytes().length; 
		Poem logistics = new Poem("poem_length", String.valueOf(poemLength), null); 
		logistics.send(outStream); 
		ack = new Poem(inStream); 
		p.send(outStream);
		ack = new Poem(inStream);
		numPoems++; 
	    
	    }

	    if(numPoems < 10){
		Poem endPoems = new Poem("END_PULL", null, null);
		endPoems.send(outStream); 
	    }
	    	    
	    rs.close();
		 
	}
	catch(SQLException e){
	    System.out.println("SQL Exception");
	    System.out.println(e.getMessage());
	}
	catch(IOException e){
	    System.out.println(e.getMessage()); 
	}
	
    }

    

    /**
     * Saves a poem from the client to the poems table. And sends an acknowledgement to the client.  
     **/
    public void pushPoems(String title, String text, String author){
       	storeToDatabase(title, text, author);
       	ack = new Poem(title, "Saved to server", null); 
       	ack.send(outStream);
    }


    /**
     * This is the bulk of the backend. This method calls upon all the others in order to perform tasks such as
     * pulling and pushing poems, creating accounts, authenticating accounts, and adding friends. 
     **/

    public void run(){
   	
	int poemLength = 0;
	Account ack2 = new Account("ack", null, null);
	Account verification;

	try{
	    inStream = sock.getInputStream();
	    outStream = sock.getOutputStream(); 

	    //READ IN STRING FROM CLIENT--this determines if we're working with poems or accounts 
	    inStream.read(from_client_bytes);
	    String from_client = new String(from_client_bytes);

	    
	    //denote whether we are working with poems or accounts 
	    from_client = from_client.trim();
	    if(from_client.equals(POEMS)){
		task_code = 1; //pull and push poems
	    }
	    else if(from_client.equals("ACCOUNTS")){
		task_code = 2; //create and authenticate accounts and add friends 
	    }


	    //working with poems 
	    if(task_code == 1){
		//pull and push poems 
		p = new Poem(inStream);

		try{
		    //if the poem text is a number, it denotes the length of the poem we want to save
		    //if there is no number, we are requesting poems from the backend 
		    poemLength = Integer.parseInt(p.text); 
		}
		catch(NumberFormatException e){System.out.println(e.getMessage()); }
	  
		String title = p.title; 
		String text = p.text;
		String author = p.author; 

		if(title.equals("pull_poems")){
		    //pull poems from the database 
		    int numFriends = Integer.parseInt(text); 
		    pullPoems(numFriends);
		}
		else{
		    //save a poem to the database 
		    p.send(outStream); 
		    Poem read_in = new Poem(inStream, poemLength); 
		    pushPoems(read_in.title, read_in.text, read_in.author); 
		}		
	    } 		
	    else{
		//creating and verifying accounts and adding friends 

		String result = null;
		code = new Account(inStream);

		ack2.send(outStream);
		
		String task = code.username;

		account = new Account(inStream);
		String username = account.username;
		String email = account.email;
		String passw = account.password;

		if(task.equals("CREATE_ACCOUNT")){
		    //check if account can be created (ie is username/email already taken?)
		    //send the result--either success or error message--to the client 
		    result = checkDatabase(username, email, passw);
		    if(result.equals("OK")){
			storeAccountToDatabase(username, email, passw);
			//assert--the account was stored to the database
			verification = new Account("ACCOUNT_IS_VALID", null, null);
			verification.send(outStream); 
		    }
		    else if(result.equals("INVALID_USERNAME")){
			verification = new Account(result, null, null);
			verification.send(outStream); 
		    }
		    else if(result.equals("INVALID_EMAIL")){
			verification = new Account(result, null, null);
			verification.send(outStream); 
		    }
		}
		else if(task.equals("ADD_FRIEND")){
		    //check if the username exists so that we can add the friend
		    //send the result to the client 
		    String resultFriend = addFriend(username);
		    if(resultFriend.equals("VALID")){
			verification = new Account("FRIEND_ACCEPTED", null, null);
			verification.send(outStream); 
			}
		    else if(resultFriend.equals("INVALID")){
			verification = new Account("Friend Does Not Exist", null, null); 
			verification.send(outStream); 
		    }
			
		}
		else{
		    //user is trying to login--is this a correct email/password combination?
		    String result2 = lookupAccount(email, passw);
		    if(result2.equals("VALID")){
			verification = new Account("ACCOUNT_IS_VALID", null, null);
			verification.send(outStream); 

		    }
		    else if(result2.equals("INVALID_EMAIL")){
			verification = new Account(result2, null, null);
			verification.send(outStream); 

		    }
		    else if(result2.equals("INVALID_PASSWORD")){
			verification = new Account(result2, null, null);
			verification.send(outStream); 

		    }

		    
		}		
		
	    }
	}catch(IOException e){
	    nack = new Poem(p.title, "Poem not saved.", null); 
		nack.send(outStream);
	    
		System.err.println("Receiver failed.");
		System.err.println(e.getMessage());
		System.exit(1);
		return; 
	}    
    }


    /**
     * Queries the database with the list of friends from the client. 
     * @param friends An array that holds the friends from the clients
     * @param numFriends The number of friends that need to be included in the query 
     * @return The ResultSet that contains the records that satisfy the query. 
     **/

    public ResultSet pullFromDatabase(String[] friends, int numFriends){
	ResourceBundle bundle;
	Connection con;
	Statement st;
	ResultSet rs; 

	int count = 0;
	String whereClause = "author  = '" + friends[0] + "'"; 
	
	try{
	    Class.forName("org.postgresql.Driver"); 
	    con = DriverManager.getConnection(url, user, password);
	    System.out.println("JDBC Connection Successful");   
	    st = con.createStatement();
	    st.executeUpdate("set search_path to poem");


	    //construct the where clause based on the friends from the client 
	    for(int i=1; i<numFriends; i++){
		whereClause = whereClause + " OR author = '" + friends[i] + "'"; 
	    }

	    //query the database 
	    String query = "SELECT * FROM poems WHERE " + whereClause + " ORDER BY poemCreated DESC LIMIT 10;"; 
	    rs = st.executeQuery(query);
	    return rs; 
	    
	}
	catch(NullPointerException e){System.out.println(e.getMessage()); }
	catch(MissingResourceException e){System.out.println(e.getMessage());}
	catch (ClassNotFoundException e){System.out.println("Class not found"); e.getMessage();}
	catch (SQLException e){
	    System.out.println("SQL Exception");
	    System.out.println(e.getMessage());}

	return null; 
    }


      
    /**
     * Saves a poem to the poems table. 
     * @param title The title of the poem
     * @param text The text of the poem
     * @param author The author of the poem 
     **/
    public void storeToDatabase(String title, String text, String author){
	ResourceBundle bundle;
	Connection con;
	Statement st;
	ResultSet rs;
	
	int count = 0;

	
	try{

	    Class.forName("org.postgresql.Driver"); 
	    con = DriverManager.getConnection(url, user, password);
	    System.out.println("JDBC Connection Successful");
	    
	    st = con.createStatement();
	    st.executeUpdate("set search_path to poem");

	   

	    text = text.replace("'","''");
	    text = text.replace(";", ",");
	    text = text.replace("\0", ""); 
	    title = title.replace("'","''");

	    rs = st.executeQuery("select * from accounts where account_email = '" + author + "';"); 
	    while(rs.next()){
		author = rs.getString(1); 
	    }
	    	    
	    st.executeUpdate("INSERT INTO poems (poemtitle, poemtext, author) VALUES('" +
	        title + "', '" + text + "', '" + author + "');");
	    System.out.println("Inserted into database");

	    st.close(); 
			   
	    
	}
	catch(NullPointerException e){System.out.println(e.getMessage()); }
	catch(MissingResourceException e){System.out.println(e.getMessage());}
	catch (ClassNotFoundException e){System.out.println("Class not found"); e.getMessage();}
	catch (SQLException e){
	    System.out.println("SQL Exception");
	    System.out.println(e.getMessage());}
    }





    /**
     * Checks in the account already exists in the accounts table. 
     * @param username The username of the account
     * @param email The email of the account
     * @param passw The password of the account
     * @return A string that denotes the state of the account--valid if the account does not yet exist and 
     * can be created, or an error message if either the username or email is already taken. 
     **/
    public String checkDatabase(String username, String email, String passw){
	ResourceBundle bundle;
	Connection con;
	Statement st;
	ResultSet rs;
	int count = 0; 
	String result = "OK"; 

	
	try{

	    Class.forName("org.postgresql.Driver"); 
	    con = DriverManager.getConnection(url, user, password);
	    System.out.println("JDBC Connection Successful");
	    //assert--successful connection to the SQL database
	    
	    st = con.createStatement();
	    //assert--sucessful creation of a statement

	    st.executeUpdate("set search_path to poem");

	    System.out.println("username: " + username);
	    System.out.println("email: " + email); 
	   

	    //we care if email is already being used or if username is already being used
	    rs = st.executeQuery("select * from accounts where account_name = '" + username + "' or account_email = '" + email + "';"); 
	    
	 
	    while(rs.next()){
		String name_result = rs.getString(1);
		String email_result = rs.getString(2);

		if(name_result.equals(username))
		   result = "INVALID_USERNAME";
		else if(email_result.equals(email))
		    result = "INVALID_EMAIL"; 
		   
	    }

	    st.close();
	    rs.close(); 

	    return result; 
			   
	    
	}
	catch(NullPointerException e){System.out.println(e.getMessage()); }
	catch(MissingResourceException e){System.out.println(e.getMessage());}
	catch (ClassNotFoundException e){System.out.println("Class not found"); e.getMessage();}
	catch (SQLException e){
	    System.out.println("SQL Exception");
	    System.out.println(e.getMessage());}

	return "CANCEL"; 
    }

    /**
     * Saves an account to the accounts table. 
     * @param username The username of the account
     * @param email The email of the account
     * @param passw The password of the account
     **/
public void storeAccountToDatabase(String username, String email, String passw){
	ResourceBundle bundle;
	Connection con;
	Statement st;
	ResultSet rs;
	int count = 0;

       	try{

	    Class.forName("org.postgresql.Driver"); 
	    con = DriverManager.getConnection(url, user, password);
	    System.out.println("JDBC Connection Successful");
	    //assert--successful connection to the SQL database
	    
	    st = con.createStatement();
	    //assert--sucessful creation of a statement

	    st.executeUpdate("set search_path to poem"); 

	    
	    st.executeUpdate("INSERT INTO accounts (account_name, account_email, account_password) VALUES('" + username + "', '" + email + "', '" + passw + "');");

	    System.out.println("Inserted account into database");

	    
	    
	    st.close(); 
			   
	    
	}
	catch(NullPointerException e){System.out.println(e.getMessage()); }
	catch(MissingResourceException e){System.out.println(e.getMessage());}
	catch (ClassNotFoundException e){System.out.println("Class not found"); e.getMessage();}
	catch (SQLException e){
	    System.out.println("SQL Exception");
	    System.out.println(e.getMessage());}
    }



    /**
     * Checks if the account being used to login to the app is a valid account. 
     * @param email The email being used to login 
     * @param passw The password being used to login. 
     * @return A string denoting either a successful login or an error message describing why the login failed. 
     **/

    public String lookupAccount(String email, String passw){
	ResourceBundle bundle;
	Connection con;
	Statement st;
	ResultSet rs;

	int count = 0;

	String result = "INVALID_EMAIL"; 

	
	try{

	    Class.forName("org.postgresql.Driver"); 
	    con = DriverManager.getConnection(url, user, password);
	    System.out.println("JDBC Connection Successful");
	    //assert--successful connection to the SQL database
	    
	    st = con.createStatement();
	    //assert--sucessful creation of a statement

	    st.executeUpdate("set search_path to poem");

	   

	    //we care if email is already being used or if username is already being used
	    System.out.println("email: " + email);
	    System.out.println("passw: " + passw); 

	    rs = st.executeQuery("select * from accounts where account_email = '" + email + "' OR account_password = '" + passw + "';"); 
	    
	    
	     while(rs.next()){
		String name_result = rs.getString(1);
		String email_result = rs.getString(2);
		String password_result = rs.getString(3);

		System.out.println("in rs"); 

		System.out.println("email is " + email_result);
		System.out.println("password is " + password_result); 

		if(email_result.equals(email) && password_result.equals(passw)){
		    result = "VALID";
		    break; 
		}
		else if(email_result.equals(email))
		    result = "INVALID_PASSWORD";
		else
		    result = "INVALID_EMAIL"; 
		   
	    }

	    st.close();
	    rs.close(); 



	    return result; 
			   
	    
	}
	catch(NullPointerException e){System.out.println(e.getMessage()); }
	catch(MissingResourceException e){System.out.println(e.getMessage());}
	catch (ClassNotFoundException e){System.out.println("Class not found"); e.getMessage();}
	catch (SQLException e){
	    System.out.println("SQL Exception");
	    System.out.println(e.getMessage());}

	return "CANCEL"; 
    }


    /**
     * Checks if the friend that a user wants to add is actually a user of the Stanza app and thus in the accounts table. 
     * @param username The user that someone wants to add as a friend. 
     * @return String denoting whether or not the user exists and can be added as a friend or doesn't exist. 
     **/
    public String addFriend(String username){

       	ResourceBundle bundle;
	Connection con;
	Statement st;
	ResultSet rs;

	int count = 0;

	String result = "CANCEL"; 

	
	try{

	    Class.forName("org.postgresql.Driver"); 
	    con = DriverManager.getConnection(url, user, password);
	    System.out.println("JDBC Connection Successful");
	    //assert--successful connection to the SQL database
	    
	    st = con.createStatement();
	    //assert--sucessful creation of a statement

	    st.executeUpdate("set search_path to poem");	   

	    rs = st.executeQuery("select * from accounts;"); 
	    
	    
	     while(rs.next()){
		String name_result = rs.getString(1);



		if(name_result.equals(username)){
		    result = "VALID";
		    break; 
		}
		else
		    result = "INVALID"; 
       			   
	    }

	    st.close();
	    rs.close(); 

	    return result; 
			   
	    
	}
	catch(NullPointerException e){System.out.println(e.getMessage()); }
	catch(MissingResourceException e){System.out.println(e.getMessage());}
	catch (ClassNotFoundException e){System.out.println("Class not found"); e.getMessage();}
	catch (SQLException e){
	    System.out.println("SQL Exception");
	    System.out.println(e.getMessage());}

	return "INVALID"; 
    
    }


}
