/* Example of network communication:  Receiver.java -- RAB 1/99 
   Requires one command line arg:  
     1.  port number to use (on this machine). */

import java.io.*;
import java.net.*;

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
      // Socket inSock = servSock.accept();

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
   
    Poem ack = new Message("ACK","Message received");
    Poem  nack = new Message("NACK", "Transmission failed"); 

    Worker(Socket s){sock = s;};

    public void run(){

	try{
	    inStream = sock.getInputStream();
	    outStream = sock.getOutputStream(); 
	    System.out.println("Successfully received the following: "); 

	 do{
	  p = new Poem(inStream);
	  System.out.println(m.toString());
	  ack.send(outStream);
	 
    
	 }while(! p.type.equals("EOT"));
	 sock.close();
	 return;

	}catch(IOException e){
	    nack.send(outStream);
	    System.err.println("Receiver failed.");
	    System.err.println(e.getMessage());
	    System.exit(1);
	    return; 
	}

    }

}
