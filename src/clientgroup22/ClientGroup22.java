/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clientgroup22;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author matthew
 */
public class ClientGroup22 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Socket s = Connect();
        Ping(s);
    }
    
    public static void DisplayMenu()
    {
        System.out.println("Hello!");
    }

    public static void Ping(Socket s) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);

            JSONObject j = new JSONObject();
            j.put("method", "ping");
            j.put("group_id", "22");

            out.write(j.toString());
            out.println();
            
            JSONObject returned = new JSONObject (in.readLine());   //Object to store returning string from server

            System.out.println("Reply from Server:" + returned.get("result") + " Time(ms):" + returned.getInt("elapsed"));
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static Socket Connect() {
        Socket sock = new Socket();
        try {
            sock = new Socket("197.85.191.195", 3000);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return sock;
    }
}
