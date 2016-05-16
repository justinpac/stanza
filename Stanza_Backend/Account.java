

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

/**
 * Created by Brianna on 5/12/2016.
 */
public class Account {

    static String fieldTerminator = "\001";
    String username;
    String email;
    String password;

    public Account(String u, String e, String p) {
        username = u;
        email = e;
        password = p;
    }

    public Account(InputStream is) {
        byte[] b = new byte[8192];
        try {
            is.read(b);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String temp = new String(b);
        StringTokenizer st = new StringTokenizer(temp, fieldTerminator, false);
        username = st.nextToken();
        email = st.nextToken();
        password = st.nextToken();
    }

    public void send(OutputStream os){
        byte[] b = getBytes();
        try {
            os.write(b);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String toString(){
        return "[username = " + username + ", email = " + email + ", password" + password + "]";
    }

    public byte[] getBytes(){
        String temp = username + fieldTerminator + email + fieldTerminator + password + fieldTerminator;
        byte [] bytes = temp.getBytes();
        return bytes;
    }
}

