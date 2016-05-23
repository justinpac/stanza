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
    String author;

    public Poem(String t1, String t2, String a){
        title = t1;
        text = t2;
        author = a;
    }

    public Poem(InputStream is){
        byte[] b = new byte[8192];
        try{
            is.read(b);
        }catch(IOException e){
            e.printStackTrace();
        }

        String temp = new String(b);
        StringTokenizer st = new StringTokenizer(temp, fieldTerminator, false);

       // System.out.println("IN CONSTRUCTOR\n" + temp);
        title = st.nextToken();
     //   System.out.println("in constructor title is " + title);
        text = st.nextToken();
        author = st.nextToken();
    }

    public Poem(InputStream is, int poemLength){
        byte[] b = new byte[8192];
        int bytes_read = 0;
        int bytes_not_yet_read = poemLength;
        int total_bytes = 0;
        int max_bytes = 8192;
        String temp = "";
        String partial_temp = "";

        try{
            while(bytes_not_yet_read != 0){
                bytes_read = is.read(b);
                bytes_not_yet_read -= bytes_read;
                total_bytes += bytes_read;
                partial_temp = new String(b);
                temp = temp + partial_temp;
            }
          //  is.read(b);
        }catch(IOException e){
            e.printStackTrace();
        }

       // String temp = new String(b);
        StringTokenizer st = new StringTokenizer(temp, fieldTerminator, false);

        title = st.nextToken();
        text = st.nextToken();
        author = st.nextToken();
    }


    public void send(OutputStream os){
        byte[] b = getBytes();
        int bytes_to_send = b.length;
        int bytes_written = 0;
        int max_bytes = 8192;
        int sending_length = 0;

        try{
            while(bytes_to_send != 0){
                if(bytes_to_send < max_bytes) sending_length = bytes_to_send;
                else sending_length = max_bytes;

                os.write(b, bytes_written, sending_length);
                bytes_to_send -= sending_length;
                bytes_written += sending_length;
            }
        //    os.write(b);
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public String toString(){
        return "[title = " + title + ", text = " + text + ", author " + author + "]";
    }

    public byte[] getBytes(){
        String temp = title + fieldTerminator + text + fieldTerminator + author + fieldTerminator;
        byte [] bytes = temp.getBytes();
        return bytes;
    }
}
