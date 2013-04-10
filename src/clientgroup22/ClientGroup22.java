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
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author matthew
 */
public class ClientGroup22 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {

        Socket s = Connect();

        Ping(s);

//        AggregatedData(s);


//        GetDataSumm(s);

//        UploadData(s);

//        QueryData(s);

//        QueryLogs(s);

//        ClientGroup22 client = new ClientGroup22();

    }

    public ClientGroup22() {
    }

    public static void DisplayMenu() {
        System.out.println("Hello!");
    }

    public static void QueryLogs(Socket s) {
        JSONObject query = new JSONObject();
        JSONObject params = new JSONObject();
        JSONArray group_ids = new JSONArray();

        /*Group ids*/
        System.out.println("Please enter in the groups to query by their group ids separated by a space (Enter to confirm):");
//        String[] group_idArr = ((new Scanner(System.in)).nextLine()).split(" ");
        String[] group_idArr = {"22"};
        for (String i : group_idArr) {
            if (isInteger(i)) {
                group_ids.put(Integer.parseInt(i));
            } else {
                System.out.println(i + " is not an integer, ignoring.");
            }
        }
        params.put("group_ids", group_ids);

        /*Time from*/
        System.out.println("Please enter the time from which you want readings (yyyy-mm-dd hh:mm:ss):");
//        params.put("time_from", new Scanner(System.in).nextLine());
        params.put("time_from", "2013-01-01 01:01:01.01");

        /*Time to*/
        System.out.println("Please enter the time to which you want readings (yyyy-mm-dd hh:mm:ss):");
//        params.put("time_to", new Scanner(System.in).nextLine());
        params.put("time_to", "2013-04-09 23:55:01.01");

        /*Limit number of logs*/
        System.out.println("Please enter the limit of the number of logs you want returned:");
//        params.put("limit", new Scanner(System.in).nextLine());
        params.put("limit", 30);

        /*Construct query JSONObject*/
        query.put("method", "query_logs");
        query.put("group_id", 22);
        query.put("params", params);

        System.out.println(query.toString());

        JSONObject reply = new JSONObject(GetReplyFromServer(query, s));
        System.out.println(reply.toString());
    }

    public static void Ping(Socket s) {
        JSONObject query = new JSONObject();
        query.put("method", "ping");
        query.put("group_id", "22");
        
        JSONObject reply = new JSONObject (GetReplyFromServer(query, s));
        
        System.out.println("Reply from Server:" + reply.get("result") + " Time(ms):" + reply.getInt("elapsed"));
    }

    public static void UploadData(Socket s) throws FileNotFoundException, IOException {
        JSONObject query = new JSONObject();
        JSONObject params = new JSONObject();

        ArrayList<String> dataArr = ReadFromFile("MoteDump.txt");
        JSONObject reading;
        JSONArray readings = new JSONArray();
        String type = "";
        String value = "";
        long time = 0;
        for (int i = 0; i < dataArr.size(); i++) {
            reading = new JSONObject();

            type = dataArr.get(i).split(" ")[0];
            value = dataArr.get(i).split(" ")[1];
            time = Long.parseLong(dataArr.get(i).split(" ")[2]);

            reading.put("type", type);
            reading.put("value", value);
            reading.put("time", time);

            readings.put(reading);
        }

        params.put("readings", readings);

        query.put("group_id", "22");
        query.put("params", params);
        query.put("method", "new_readings");

//        System.out.println(finalSend.toString());

        String reply = GetReplyFromServer(query, s);

        System.out.println("Reply from Server: " + reply);
    }

    public static JSONObject QueryData(Socket s) {
        JSONObject query = new JSONObject();
        JSONObject params = new JSONObject();
        JSONObject returned = new JSONObject();
//        JSONObject time_from = new JSONObject();  //Probably don't need...
//        JSONObject time_to = new JSONObject();
        JSONArray group_ids = new JSONArray();
        JSONArray types = new JSONArray();

        /*Group ids*/
        System.out.println("Please enter in the groups to query by their group ids separated by a space (Enter to confirm):");
//        String[] group_idArr = ((new Scanner(System.in)).nextLine()).split(" ");
        String[] group_idArr = {"22"};
        for (String i : group_idArr) {
            if (isInteger(i)) {
                group_ids.put(Integer.parseInt(i));
            } else {
                System.out.println(i + " is not an integer, ignoring.");
            }
        }
        params.put("group_ids", group_ids);

        /*Time from*/
        System.out.println("Please enter the time from which you want readings (yyyy-mm-dd hh:mm:ss):");
//        params.put("time_from", new Scanner(System.in).nextLine());
        params.put("time_from", "2013-01-01 01:01:01");

        /*Time to*/
        System.out.println("Please enter the time to which you want readings (yyyy-mm-dd hh:mm:ss):");
//        params.put("time_to", new Scanner(System.in).nextLine());
        params.put("time_to", "2013-04-09 23:55:01");

        /*Types*/
        System.out.println("Please enter which types of readings you want to query, separated by a space (\"light\", \"temperature\", \"humidity\"):");
//        String[] typesArr = ((new Scanner(System.in)).nextLine()).split(" ");
        String[] typesArr = {"light", "temperature"};
        for (String i : typesArr) {
            types.put(i);
        }
        params.put("types", types);

        query.put("method", "query_readings");
        query.put("group_id", 22);
        query.put("params", params);

        JSONObject reply = new JSONObject (GetReplyFromServer(query, s));

        return reply;
    }

    public static JSONObject AggregatedData(Socket s) {
        JSONObject query = new JSONObject();
        JSONObject params = new JSONObject();
        JSONObject returned = new JSONObject();

        /*Group id if given*/
        System.out.println("Please enter in the group to query by their group ID. Enter 0 for all data. (Enter to confirm):");
        Scanner scan = new Scanner(System.in);
        String input = scan.nextLine();
        if (isInteger(input)) {
            if (Integer.parseInt(input) == 0) {
                //do nothing
            } else {
                params.put("group_id", Integer.parseInt(input));
//          params.put("group_id", 1); //hardcoded version
            }
        } else {
            System.out.println("Not an integer, assuming 0");
        }

        //test
        params.put("aggregation", "mean");
        System.out.println("mean put");
        params.put("type", "temperature");
        System.out.println("temp put");

        /*Time from*/
        System.out.println("Please enter the time from which you want readings (yyyy-mm-dd hh:mm:ss):");
//        params.put("time_from", new Scanner(System.in).nextLine());
        params.put("time_from", "2013-01-01 01:01:01");
//
        /*Time to*/
        System.out.println("Please enter the time to which you want readings (yyyy-mm-dd hh:mm:ss):");
//        params.put("time_to", new Scanner(System.in).nextLine());
        params.put("time_to", "2013-04-10 23:55:01");

        /*Aggregation*/
        System.out.println("Please enter which types of statistic you want to query (\"light\", \"temperature\", \"humidity\"):");
        params.put("types", new Scanner(System.in).nextLine());

        /*Types*/
        System.out.println("Please enter which types of reading you want to query(\"count\", \"average\", \"min\", \"max\", \"stddev\", \"mode\", \"median\"):");
        params.put("types", new Scanner(System.in).nextLine());

        query.put("method", "aggregate");
        query.put("group_id", 22);
        query.put("params", params);

        returned = new JSONObject(GetReplyFromServer(query, s));

        return returned;
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

            for (int i = 0; i < dataArr.length(); i++) {
                if (i == 0) {
                    System.out.println("============");
                }
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

    public static ArrayList<String> ReadFromFile(String fileName) throws FileNotFoundException {    //Works! Don't change!
        ArrayList<String> temp = new ArrayList<String>();
        File file = new File(fileName);
        Scanner sc = new Scanner(file);

        while (sc.hasNext()) {
            String reading = "";
            reading += sc.next() + " ";
            sc.next();
            reading += sc.next() + " ";
            reading += sc.next();
            temp.add(reading);
        }
        return temp;
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

    public static String GetReplyFromServer(JSONObject query, Socket s) {
        String reply = "";
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);

            System.out.println("Querying server...");
            out.write(query.toString());
            out.println();

            reply = in.readLine();
        } catch (IOException e) {
            System.out.println(e);
        }
        return reply;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    public static void DisplayQueryResults(JSONObject j) {  //TODO format the information stored in the JSONObject returned by the server
        System.out.println(j.toString());
    }
}
