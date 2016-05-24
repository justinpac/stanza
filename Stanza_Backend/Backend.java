/**

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
  static final int maxinBuff = 1000;
    
    public static OutputStream outStream = null;
    public static  Poem nack = null; 

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
      

class Worker implements Runnable{
    Socket sock;
    OutputStream outStream = null;
    InputStream inStream = null;
    Poem p = null;
    Poem test = null;
    Poem read_in = null; 
   
    Poem ack = null; 
    Poem  nack = null;

    Account code = null;
    Account account = null;

    String url = "jdbc:postgresql://shelob.cs.stolaf.edu:5432/mca_s16";
    String user = "mca_s16_poem";
    String password = "Javaphone";

    int task_code;
    byte[] from_client_bytes = new byte[8192]; 
    String POEMS = "POEMS";
    String ACCOUNTS = "ACCOUNTS"; 

    Worker(Socket s){sock = s;};


  
    public void pullPoems(int numFriends){
	Poem ack = null;
	String title, text, author, temp_friend; 

	System.out.println("pulling poems");
	byte [] friend_list = new byte [8192];
	String [] all_friends = new String[numFriends];  

	System.out.println("num friends is " + numFriends); 

	 if(numFriends == 0){
		Poem endPoems = new Poem("END_PULL", null, null);
		endPoems.send(outStream); 
		return;
	 }
	
	    
	try{
	    inStream.read(friend_list);
	    String temp = new String(friend_list);
	    StringTokenizer st = new StringTokenizer(temp, "\001", false);

	   
	    
	    for(int i=0; i<numFriends; i++){
		temp_friend = st.nextToken();
		all_friends[i] = temp_friend;
		System.out.println("current friend is " + temp_friend); 
	    }


	    
	    ResultSet rs = pullFromDatabase(all_friends, numFriends);

	    // System.out.println("num peoms is " + rs.getRow());

	    
	    
	    

	    

	    int numPoems = 0; 
	    while(rs.next()){
		System.out.println("row number " + rs.getRow()); 

		if(rs.getRow() == 0){
		    System.out.println("in here"); 
		    Poem endPoems = new Poem("END_PULL", null, null);
		    endPoems.send(outStream);
		    System.out.println("sent end"); 
		    return;
		}
		
		title = rs.getString(2);
		text = rs.getString(3);
		author = rs.getString(5); 
		//	System.out.println("title " + title);
		
		Poem p = new Poem(title, text, author);
		int poemLength = p.getBytes().length; 
		Poem logistics = new Poem("poem_length", String.valueOf(poemLength), null); 
		logistics.send(outStream); 
		ack = new Poem(inStream); 
		p.send(outStream);
		ack = new Poem(inStream);
		System.out.println(ack.title + " " + p.title);
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

    

    public void pushPoems(String title, String text, String author){

	System.out.println("save to database"); 
       	storeToDatabase(title, text, author);
	
       	ack = new Poem(title, "Saved to server", null); 
       	ack.send(outStream);
    }

    

    public void run(){
   	
	int poemLength = 0;
	Account ack2 = new Account("ack", null, null);
	Account verification;



	try{
	    inStream = sock.getInputStream();
	    outStream = sock.getOutputStream(); 
	    System.out.println("Successfully received the following: ");
	    System.out.println("in run again");

	    //READ IN STRING FROM CLIENT
	    inStream.read(from_client_bytes);
	    String from_client = new String(from_client_bytes);

	    System.out.println(from_client); 
	    
	    from_client = from_client.trim();
	    if(from_client.equals(POEMS)){
		task_code = 1; //pull and push poems
		System.out.println("in task code poems"); 
	    }
	    else if(from_client.equals("ACCOUNTS")){
		task_code = 2; //create and authenticate accounts
		System.out.println("in task code accounts"); 
	    }


	    
	    if(task_code == 1){
		//pull and push poems 
		p = new Poem(inStream);
		System.out.println("Reading poem"); 

		try{
		    poemLength = Integer.parseInt(p.text); 
		}
		catch(NumberFormatException e){System.out.println(e.getMessage()); }
	  
		String title = p.title; 
		String text = p.text;
		String author = p.author; 
		System.out.println(title);
		System.out.println(text); 

		if(title.equals("pull_poems")){
		    int numFriends = Integer.parseInt(text); 
		    pullPoems(numFriends);
		    System.out.println("finished pulling"); 
		}
		else{
		    System.out.println("in push poem");
		    System.out.println("p title " + p.title);
		    p.send(outStream); 
		    Poem read_in = new Poem(inStream, poemLength); 

		    pushPoems(read_in.title, read_in.text, read_in.author); 
		}		
	    } 		
	    else{
		//creating and verifying accounts

		String result = null;
		code = new Account(inStream);
		System.out.println("in accounts");

		ack2.send(outStream);
		
		System.out.println("sent ack"); 
		String task = code.username;
		System.out.println("task code is " + task); 

		account = new Account(inStream);
		System.out.println("after read in actual account"); 
		String username = account.username;
		String email = account.email;
		String passw = account.password;

		System.out.println("user: " + username);
		System.out.println("email: " + email);
		System.out.println("password: " + passw); 


		System.out.println("pre if statement"); 
		if(task.equals("CREATE_ACCOUNT")){
		    //check if account can be created (ie is username/email already taken?)

		    System.out.println("in create account");
		    result = checkDatabase(username, email, passw);
		    if(result.equals("OK")){
			System.out.println("can make this account"); 
			storeAccountToDatabase(username, email, passw);
			//assert--the account was stored to the database
			verification = new Account("ACCOUNT_IS_VALID", null, null);
			verification.send(outStream); 
		    }
		    else if(result.equals("INVALID_USERNAME")){
			System.out.println("user name taken");
			verification = new Account(result, null, null);
			verification.send(outStream); 
		    }
		    else if(result.equals("INVALID_EMAIL")){
			System.out.println("email taken");
			verification = new Account(result, null, null);
			verification.send(outStream); 
		    }
		}
		else if(task.equals("ADD_FRIEND")){
		    String resultFriend = addFriend(username);
		    if(resultFriend.equals("VALID")){
			System.out.println("friend exists");
			verification = new Account("FRIEND_ACCEPTED", null, null);
			verification.send(outStream); 
			}
		    else if(resultFriend.equals("INVALID")){
			System.out.println("Friend Does Not Exist");
			verification = new Account("Friend Does Not Exist", null, null); 
			verification.send(outStream); 
		    }
			
		}
		else{
		    //user is trying to login--is this a correct email/password combination?
		    System.out.println("in authenticate account");
		    String result2 = lookupAccount(email, passw);

		    System.out.println(result2); 

		    System.out.println("pre if statement"); 
		    if(result2.equals("VALID")){
			System.out.println("valid account"); 
			verification = new Account("ACCOUNT_IS_VALID", null, null);
			verification.send(outStream); 

		    }
		    else if(result2.equals("INVALID_EMAIL")){
			System.out.println("invalid email"); 
			verification = new Account(result2, null, null);
			verification.send(outStream); 

		    }
		    else if(result2.equals("INVALID_PASSWORD")){
			System.out.println("invalid password"); 
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

	    
	    for(int i=1; i<numFriends; i++){
		whereClause = whereClause + " OR author = '" + friends[i] + "'"; 
	    }

	    System.out.println("where clause: " + whereClause);
	    String query = "SELECT * FROM poems WHERE " + whereClause + " ORDER BY poemCreated DESC LIMIT 10;"; 

	    System.out.println("query: " + query); 
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
	    //assert--successful connection to the SQL database
	    
	    st = con.createStatement();
	    
	    //assert--sucessful creation of a statement

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


    public String addFriend(String username){

	//check if friend exists in the database

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

		//	System.out.println("in rs"); 


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
