package com.example.stanza;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Brianna on 4/27/2016.
 */
public class CommThread extends Thread
implements Runnable{

    Queue<Poem> poemQ = new LinkedList<Poem>();
    boolean done = false;
    CommInterface commInterface;
    EditPoemActivity editPoemActivity;
    InputStream inputStream;
    OutputStream outputStream;

    CommThread(CommInterface ci, EditPoemActivity epa){
        commInterface = ci;
        editPoemActivity = epa;
    }

    public synchronized void addPoem(Poem poem){
        if(poem == null)
            done = true;
        else
            poemQ.add(poem);
        this.notify();
    }

    void processOnePoem(){
        Poem poem = poemQ.remove();
        Poem receive = null;
        poem.send(outputStream);
        receive = new Poem(inputStream);

        final String output = receive.text + ": " + receive.title;
        editPoemActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                commInterface.poemSaved(output);
            }
        });

        System.out.println("CommThread: processing " + poem.title);

    }

    public void run(){
        String host = "rns202-13.cs.stolaf.edu";
        int port = 28414;

        try{
            Socket socket = new Socket(host, port);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        }catch (IOException e){
            e.printStackTrace();
        }

        while (!done){
            while(!poemQ.isEmpty())
                processOnePoem();
            try{
                synchronized (this){
                    while (poemQ.isEmpty() && !done)
                        wait();
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        System.out.println("CommThread terminating");
    }

}
