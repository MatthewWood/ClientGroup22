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
import Read_Serial.Junix;
import java.util.ArrayList;

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
        GetDataSumm(s);
        


        ClientGroup22 client = new ClientGroup22();
        
        

    }
    
    public ClientGroup22() {
    }
    
    public static void DisplayMenu() {
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
            
            JSONObject returned = new JSONObject(in.readLine()); //Object to store returning string from server

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
    
    public static void GetDataSumm(Socket s) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            
            JSONObject j = new JSONObject();
            j.put("method", "data_summary");
            j.put("group_id", "22");
            
            out.write(j.toString());
            out.println();
            
            JSONObject returned = new JSONObject(in.readLine()); //Object to store returning string from server

            JSONArray dataArr = returned.getJSONArray("result"); //array of JSONobjects
            
            System.out.println("============");

            for (int i = 0; i < dataArr.length(); i++) {
                JSONObject temp = new JSONObject();
                temp = dataArr.getJSONObject(i);
                System.out.println("Type: " + temp.get("type") + "\nType ID: " + temp.get("type_id") + "\nMean: " + temp.get("mean")
                        + "\nMin: " + temp.get("min") + "\nMax: " + temp.get("max") + "\nStandard dev: " + temp.get("stddev") + "\n");
                System.out.println("============");
                
            }

            //System.out.println("Reply from Server:" + returned.get("result") + " Time(ms):" + returned.getInt("elapsed"));

        } catch (IOException e) {
            System.out.println(e);
        }
    }
}