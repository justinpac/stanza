package com.example.stanza;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

//adding to git

/**
 * A class for sending a poem over the network to the database
 */
public class Poem {
    // state variables
    /**
     * A delimiter for seperating the title from the body of the poem in <code>Poem</code>
     */
    static String fieldTerminator = "\001";
    /**
     * A string holding the title of the poem in <code>Poem</code>
     */
    String title;
    /**
     * A string for holding the body text of the poem in <code>Poem</code>
     */
    String text;
    String author;

    //constructors

    /**
     * A constructor for making a poem given the title and text
     * @param t1 the title of the poem
     * @param t2 the text of the poem
     */

    public Poem(String t1, String t2, String a){
        title = t1;
        text = t2;
        author = a;
    }


    /**
     * A constructor for contructing a poem given an inputstream. It is parsed in order to get the
     *      poem title and text
     * @param is the incoming poem title and text in a inpustream form
     */
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

    /**
     * A constructor for creating a poem via inputstream parsing and given the poem length
     * @param is the poem in an inputstream form, will be parsed
     * @param poemLength the length of the incoming poem
     */

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

    //methods

    /**
     * A method for sending a poem to the backend server
     * @param os The output stream used to send the poem, contains the poems title and text separated
     *           by delimiters
     */
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

    /**
     * A method for translating the poem title and text into a single string for debugging
     * @return the poem title and text in a single string, with debugging text
     */
    public String toString(){
        return "[title = " + title + ", text = " + text + ", author " + author + "]";
    }

    /**
     * A way to turn the poem into a byte array for conversion to a stream in preparation for seding
     *      to a backend.
     * @return the byte-ified string holding the poem's info.
     */
    public byte[] getBytes(){
        String temp = title + fieldTerminator + text + fieldTerminator + author + fieldTerminator;
        byte [] bytes = temp.getBytes();
        return bytes;
    }
}
