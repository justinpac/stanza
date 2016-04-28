package com.example.stanza;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

/**
 * Created by Brianna on 4/27/2016.
 */

//adding to git
public class Poem {
    static String fieldTerminator = "\001";
    String title;
    String text;

    public Poem(String t1, String t2){
        title = t1;
        text = t2;
    }

    public Poem(InputStream is){
        byte[] b = new byte[4096];
        try{
            is.read(b);
        }catch(IOException e){
            e.printStackTrace();
        }

        String temp = new String(b);
        StringTokenizer st = new StringTokenizer(temp, fieldTerminator, false);

        title = st.nextToken();
        text = st.nextToken();
    }


    public void send(OutputStream os){
        byte[] b = getBytes();
        try{
            os.write(b);
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public String toString(){
        return "[title = " + title + ", text = " + text + "]";
    }

    public byte[] getBytes(){
        String temp = title + fieldTerminator + text + fieldTerminator;
        return temp.getBytes();
    }
}
