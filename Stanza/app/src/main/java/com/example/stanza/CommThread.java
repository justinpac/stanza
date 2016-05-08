package com.example.stanza;

import android.content.ContentValues;
import android.os.NetworkOnMainThreadException;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Created by Brianna on 4/27/2016.
 */
public class CommThread extends Thread
implements Runnable{
    Queue<Poem> poemQ2 = new LinkedList<Poem>();

    boolean done = false;
    boolean done2 = false;
    CommInterface commInterface;
    EditPoemActivity editPoemActivity;
    FriendBoardFragment friendBoardFragment;
    InputStream inputStream;
    OutputStream outputStream;

    int port = 28414;

    Queue<Poem> poemQ = new LinkedList<Poem>();
    int task_id = 0;


    CommThread(CommInterface ci, EditPoemActivity epa){
        commInterface = ci;
        editPoemActivity = epa;
        task_id = 1;
    }


    CommThread(CommInterface ci, FriendBoardFragment fba) {
        commInterface = ci;
        friendBoardFragment = fba;
        task_id = 2;
    }


    public synchronized void addPoem(Poem poem){
        System.out.println("add poem");
        if(poem == null)
            done = true;
        else
            poemQ.add(poem);
        this.notify();
    }

    void processOnePoem(){
        System.out.println("process poem");
        Poem poem = poemQ.remove();
        Poem receive = null;
        try {
            poem.send(outputStream);
            receive = new Poem(inputStream);
            final String output = receive.text + ": " + receive.title;
            System.out.println("add poem");

            editPoemActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    commInterface.poemSaved(output);
                }
            });

        }catch (RuntimeException e){
            editPoemActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    commInterface.serverDisconnected();
                }
            });
        }

        System.out.println("CommThread: processing " + poem.title);

    }

    void processOnePoem2(){
        System.out.println("process poem from backend method");
        Poem receive = null;
        Poem ack = new Poem("received_poem", "");

        try {
                Poem request = new Poem("pull_poems", "");
                request.send(outputStream);
                System.out.println("sent: " + request.title);

                for (int i = 0; i < 10; i++) {
                    System.out.println("gettnig new poem");
                    receive = new Poem(inputStream);
                    poemQ2.add(receive);
                    ack.send(outputStream);
                    System.out.println("receive poem number " + (i+1) + ": " + receive.title);
                }
        }
            catch(RuntimeException e){
                e.printStackTrace();
            }
        finally{return;}
    }



    public synchronized void storeToLocalDatabase(Poem poem) {

        if(poem==null)
            done2 = true;
        else{
            String text = poem.text;
            String title = poem.title;
            System.out.println("Store to database: " + title);
            ContentValues values = new ContentValues();
            values.put(DBOpenHelper.POEM_TEXT, text);
            values.put(DBOpenHelper.POEM_TITLE, title);
            values.put(DBOpenHelper.CREATOR, "friend");

            friendBoardFragment.getContext().getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        }
    }

    public void run() {
        String host = "rns203-8.cs.stolaf.edu";
        System.out.println("task id is " + task_id);

        try {
            Socket socket = new Socket(host, port);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            System.out.println("Set up socket");

            System.out.println("sockets set up");


            if (task_id == 1) {//do stuff for editor activity
                while (!done) {
                    System.out.println("process poem");
                    while (!poemQ.isEmpty())
                        processOnePoem();
                    try {
                        synchronized (this) {
                            while (poemQ.isEmpty() && !done)
                                wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("CommThread terminating");
            } else if (task_id == 2) { //do stuff for friendboard
                System.out.println("about to save poems");
                String filter = DBOpenHelper.CREATOR + "='friend'";
                friendBoardFragment.getActivity().getContentResolver().delete(NotesProvider.CONTENT_URI, filter, null);
                while (!done2) {
                    processOnePoem2();
                    try {
                        synchronized (this) {
                            while (!poemQ2.isEmpty()) {
                                Poem poem = null;
                                poem = poemQ2.remove();
                                storeToLocalDatabase(poem);
                            }
                            done2 = true;
                            friendBoardFragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    commInterface.onPullFinished();
                                }
                            });
                            while (poemQ2.isEmpty() && !done2)
                                wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (ConnectException e) {
            e.printStackTrace();
            if (task_id == 1) {
                editPoemActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        commInterface.serverDisconnected();
                    }
                });
            } else if (task_id == 2) {
                friendBoardFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        commInterface.serverDisconnected();
                        SwipeRefreshLayout swipeRefreshLayout =
                                (SwipeRefreshLayout) friendBoardFragment.getActivity().findViewById(R.id.friendSwipeLayout);
                        swipeRefreshLayout.setRefreshing(false);
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
