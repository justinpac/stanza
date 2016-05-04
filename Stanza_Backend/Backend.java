/* Example of network communication:  Receiver.java -- RAB 1/99 
   Requires one command line arg:  
     1.  port number to use (on this machine). */

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
   
    Poem ack = null; 
    Poem  nack = null;  

    Worker(Socket s){sock = s;};

    public void pullPoems(){
	Poem ack = null;
	String title, text; 

	System.out.println("pulling poems");
	    
	try{
	    ResultSet rs = pullFromDatabase();
		  
	    while(rs.next()){
		title = rs.getString(2);
		text = rs.getString(3); 
		//	System.out.println("title " + title);
		
		Poem p = new Poem(title, text);
		p.send(outStream);
		ack = new Poem(inStream);
		System.out.println(ack.title + " " + p.title); 
	    
	    }
	    rs.close();
		 
	}
	catch(SQLException e){
	    System.out.println("SQL Exception");
	    System.out.println(e.getMessage());
	}      
	
    }

    

    public void pushPoems(String title, String text){

	System.out.println("save to database"); 
       	storeToDatabase(title, text);
       	ack = new Poem(p.title, "Saved to server"); 
       	ack.send(outStream);
    }

    public void run(){  

	try{
	    inStream = sock.getInputStream();
	    outStream = sock.getOutputStream(); 
	    System.out.println("Successfully received the following: "); 

	    p = new Poem(inStream);
	  
	    String title = p.title; 
	    String text = p.text;

	    if(title.equals("pull_poems"))
		pullPoems();
	    else
		pushPoems(title, text); 
		
	    sock.close();

	}
	catch(IOException e){
	    nack = new Poem(p.title, "Poem not saved."); 
	    nack.send(outStream);
	    
	    System.err.println("Receiver failed.");
	    System.err.println(e.getMessage());
	    System.exit(1);
	    return; 
	}

    }

   

     public ResultSet pullFromDatabase(){
	ResourceBundle bundle;
	Connection con;
	Statement st;
	ResultSet rs;
	String url = "jdbc:postgresql://shelob.cs.stolaf.edu:5432/mca_s16";
	String user = "mca_s16_poem";
	String password = "Javaphone";
	int count = 0;
	
	try{
	    Class.forName("org.postgresql.Driver"); 
	    con = DriverManager.getConnection(url, user, password);
	    System.out.println("JDBC Connection Successful");   
	    st = con.createStatement();
	    st.executeUpdate("set search_path to poem");

	    rs = st.executeQuery("SELECT * FROM poems ORDER BY poemCreated DESC LIMIT 10;");
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




    


    public void storeToDatabase(String title, String text){
	ResourceBundle bundle;
	Connection con;
	Statement st;
	ResultSet rs;
	String url = "jdbc:postgresql://shelob.cs.stolaf.edu:5432/mca_s16";
	String user = "mca_s16_poem";
	String password = "Javaphone";
	int count = 0;

	
	try{

	    // bundle = ResourceBundle.getBundle("javaconfig");

	    //data to connect to database
	    //url = bundle.getString("jdbc.url") + bundle.getString("jdbc.dbname"); 
	    // user = bundle.getString("jdbc.user"); 
	    // password = bundle.getString("jdbc.password");


	    Class.forName("org.postgresql.Driver"); 
	    con = DriverManager.getConnection(url, user, password);
	    System.out.println("JDBC Connection Successful");
	    //assert--successful connection to the SQL database
	    
	    st = con.createStatement();
	    //assert--sucessful creation of a statement

	    st.executeUpdate("set search_path to poem");

	    // System.out.println(title);
	    // System.out.println(text);

	    text = text.replace("'","''");
	    text = text.replace(";", ",");
	    title = title.replace("'","''");

	    System.out.println("POEM TITLE IS\n" + title); 
	    System.out.println("POEM TEXT IS\n" + text); 

	    
	    st.executeUpdate("INSERT INTO poems (poemtitle, poemtext) VALUES('" +
	        title + "', '" + text + "');");
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

}
