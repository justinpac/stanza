package com.example.stanza;

import android.content.ContentValues;
import android.support.v4.widget.SwipeRefreshLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

/**
 * This class is used to communicate with the backend server regarding poems.
 * Namely, it is used in conjunction with the EditPoemActivity and the FriendBoardFragement to
 * save poems to the bckend server as well as pull friend poems from the backend server.
 */
public class CommThread extends Thread
        implements Runnable{

    //state variables

    /**
     * The host name of the backend server.
     */
    String host = "rns202-13.cs.stolaf.edu";

    /**
     * The port tha is used to connec to the backend server.
     */
    int port = 28414;

    /**
     * Holds the poem that is to be saved to the backend server.
     */
    Queue<Poem> poemQ = new LinkedList<Poem>();

    /**
     * Holds the poems received from the backend server.
     */
    Queue<Poem> poemQ2 = new LinkedList<Poem>();

    /**
     * Holds the friends of the user that will be used to query the backend database for
     * friend poetry.
     */
    Queue<String> friendQ = new LinkedList<String>();

    /**
     * Determines when the thread should stop running.
     */
    boolean done = false;

    /**
     * Determines when the thread should stop running.
     */
    boolean done2 = false;

    /**
     * An instance of CommInterface to communicate with UI activities.
     */
    CommInterface commInterface;

    /**
     * An instance of EditPoemActivity to perform backend tasks in conjunction with
     * EditPoemActivity.
     */
    EditPoemActivity editPoemActivity;

    /**
     * An instance of FriendBoardFragment to perform backend tasks in conjunction with
     * FriendBoardFragment.
     */
    FriendBoardFragment friendBoardFragment;

    /**
     * An instance of InputStream to communicate with the backend server.
     */
    InputStream inputStream;

    /**
     * An instance of OutputStream to communicate with the backend server.
     */
    OutputStream outputStream;

    /**
     * Code that designates which task is being performed by CommThread. Assigned within the
     * constructor.
     *
     * 1: working with EditPoemActivity to save a poem.
     * 2: working with FriendBoardFragment to retrieve poems from the backend.
     */
    int task_id = 0;

    /**
     * The number of friends in the local friends database. To inform the backend how many
     * friends it needs to incorporate into its query.
     */
    int numFriends = 0;



    //constructors

    /**
     * Creates an instance of CommThread in order to enable communication between the backend
     * and EditPoemActivity. SEts task_id to 1 (save poem to backend).
     * @param ci An instance of CommInterface.
     * @param epa An instance of EditPoemActivity.
     */
    CommThread(CommInterface ci, EditPoemActivity epa){
        commInterface = ci;
        editPoemActivity = epa;
        task_id = 1;
    }


    /**
     * Creates an instance of CommThread in order to enable communication between the backend
     * and FriendBoardFragment.  Sets task_id to 2 (retrieve poems from the backend).
     * @param ci An instance of CommInterface.
     * @param fba An instnace of FriendBoardFragment.
     */
    CommThread(CommInterface ci, FriendBoardFragment fba) {
        commInterface = ci;
        friendBoardFragment = fba;
        task_id = 2;
    }


    /**
     * Adds a poem to poemQ (those poems to be saved to the backend). Notifies the thread that
     * it is time to work.
     * @param poem The poem to be saved to the backend.
     */
    public synchronized void addPoem(Poem poem){
        System.out.println("add poem");
        if(poem == null)
            done = true;
        else
            poemQ.add(poem);
        this.notify();
    }

    /**
     * Adds all friends to the friendQ in order to retrieve poems written by these friends
     * from the the backend database. Notifies the thread that it is time to work.
     * @param friends A vector containing all friends in the local database.
     */
    public synchronized void addFriend(Vector<String> friends){
        System.out.println("sending friends to backend");
        for(int i=0; i<friends.size(); i++){
            friendQ.add(friends.elementAt(i));
        }
        numFriends = friends.size();
        this.notify();
    }


    /**
     * Part of the save poem to the backend server process. Takes the poem from the poemQ and
     * sends it to the backend. Receives confirmation that the poem has been saved to the
     * database.
     */
    void processOnePoem(){
        Poem poem = poemQ.remove();
        Poem receive = null;
        try {
            System.out.println(poem.getBytes().length);

            //tell the backend how long the poem it will be receiving is
            Poem logistics = new Poem("poem_length", String.valueOf(poem.getBytes().length), null);
            logistics.send(outputStream);
            receive = new Poem(inputStream);
            poem.send(outputStream);

            //receive confirmation that the poem was saved to the backend server
            receive = new Poem(inputStream);
            final String output = receive.text + ": " + receive.title;

            //inform the UI that the poem was successfully saved
            editPoemActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    commInterface.poemSaved(output);
                }
            });

        }
        //print server disconnected message on UI if the server is disconnected
        catch (RuntimeException e){
            editPoemActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    commInterface.serverDisconnected();
                }
            });
        }
    }

    /**
     * Part of the retrieve poems from the backend server process. Sends a request to the server
     * to retrieve poems. Then sends the list of friends to the backend server to be used in the
     * database query. Receives poems from the backend and stores them in poemQ2 (retrieved poems).
     * Finishes after it receives 10 poems or a message from the server that there are no more
     * poems available.
     */
    void processOnePoem2(){
        Poem receive = null;
        Poem logistics = null;
        Poem ack = new Poem("received_poem", "", null);

        try {
            //send request to retrieve poems
            Poem request = new Poem("pull_poems", Integer.toString(numFriends), null);
            request.send(outputStream);
            //send list of friends
            sendFriendList();

            //process the poems received from the backend
            for (int i = 0; i < 10; i++) {
                logistics = new Poem(inputStream);
                ack.send(outputStream);
                int poemLength = Integer.parseInt((logistics.text));
                receive = new Poem(inputStream, poemLength);
                if(receive.title.equals("END_PULL")) {
                    System.out.println("end pull");
                    break;
                }
                poemQ2.add(receive);
                ack.send(outputStream);
            }
        }
        catch(RuntimeException e){
            e.printStackTrace();
        }
        finally{return;}
    }


    /**
     * Send the list of friends with which to query the backend database to the backend.
     */
    public void sendFriendList(){
        String all_friends = "";
        while(!friendQ.isEmpty()){
            String friend = friendQ.remove();
            all_friends = all_friends + friend + "\001";
        }
        byte [] friend_bytes = all_friends.getBytes();
        try {
            outputStream.write(friend_bytes);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Take each poem that has been received from the backend and store it in the local database.
     * @param poem A poem from poemQ2 that has been received from the backend.
     */
    public synchronized void storeToLocalDatabase(Poem poem) {
        if(poem==null)
            done2 = true;
        else{
            String text = poem.text;
            String title = poem.title;
            String author = poem.author;
            ContentValues values = new ContentValues();
            values.put(DBOpenHelper.POEM_TEXT, text);
            values.put(DBOpenHelper.POEM_TITLE, title);
            values.put(DBOpenHelper.CREATOR, author);
            friendBoardFragment.getContext().getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        }
    }

    /**
     * Take care of all communication with the backend when there is a poem in poemQ to be saved
     * or friends in friendsQ to use in retrieving poetry. Utilizes the task_id, which was defined
     * in the constructors in order to determine which task is currently being handled--saving a
     * poem or retrieving friend poetry. Sends a message to the backend telling it which task is
     * being performed. Then sends or receives poetry information to/from the backend. Finally,
     * it receives a true/false verification of the success of the task and informs the UI.
     */
    public void run() {

        try {
            Socket socket = new Socket(host, port);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            //inform the backend we're working with poems and not accounts
            String POEMS = "POEMS";
            byte[] b = POEMS.getBytes();
            outputStream.write(b);

            //SAVING POEM TO BACKEND
            if (task_id == 1) {
                while (!done) {
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
            }
            //RETRIEVE POEMS FROM THE BACKEND
            else if (task_id == 2) {
                //remove from the local database all friend poems that have been previously retrieved
                String filter = DBOpenHelper.CREATOR + "!='self'";
                friendBoardFragment.getActivity().getContentResolver().delete(NotesProvider.CONTENT_URI, filter, null);
                while (!done2) {
                    processOnePoem2();
                    try {
                        //as long as there are poems in poemQ2 that have been retrieved from the backend
                        //and not yet saved, save them to the local database as friend poems
                        synchronized (this) {
                            while (!poemQ2.isEmpty()) {
                                Poem poem = null;
                                poem = poemQ2.remove();
                                storeToLocalDatabase(poem);
                            }
                            done2 = true;
                            //show the retrieved poems in the FriendBoardFragment
                            friendBoardFragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    commInterface.onPullFinished();
                                }
                            });
                            //as long as the task is yet complete, wait
                            while (poemQ2.isEmpty() && !done2)
                                wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //display server disconnected messages when the server is disconnected
        catch (ConnectException e) {
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
