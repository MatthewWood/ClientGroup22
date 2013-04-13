/*
 * Client for Networking practical. Allows users to interact with the Server, 
 * and alter or view the data frmo the database.
 * @author Matthew Wood
 * @author Ezrom Chijoriga
 * @author Wesley Robinson
 * Networking Prac 1
 * 12 April 2013
 * Version 1.4.1
 */

package clientgroup22;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
//JSON imports
import org.json.JSONArray;
import org.json.JSONObject;
//chart imports
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import java.util.ArrayList;
import java.util.Scanner;
import org.json.JSONException;

public class ClientGroup22 {


    static final String host = "197.85.191.195"; //nightmare@cs.uct.ac.za - alternative host
    static int group_id; //group ID of the user

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        /*menu system*/
        System.out.println("Welcome to group 22's client");
        System.out.println("Authors Matthew Wood, Wesley Robinson, and Ezrom Chijoriga");
        System.out.println("==========================================================");

        Socket s = Connect(); //connect to server

        System.out.println("Server connection established on: " + host);

        String input;
        Scanner sc = new Scanner(System.in);
        //getting user group ID
        System.out.print("Please enter your group ID: ");
        input = sc.nextLine();
        if (isInteger(input)) {
            group_id = Integer.parseInt(input);
        } else {
            System.exit(0);
        }

        while (true) { //print out menu
            System.out.println();
            System.out.println("1: Ping server");
            System.out.println("2: Upload data");
            System.out.println("3: Query specific data");
            System.out.println("4: Get data statistics");
            System.out.println("5: Graph data statistics");
            System.out.println("6: Get data summary");
            System.out.println("7: Query the logs");
            System.out.println("8: Exit");
            System.out.print("Please select a choice: ");

            input = sc.nextLine();

            System.out.println();

            if (isInteger(input) && Integer.parseInt(input) != 8) { //case statement to decide which functions to call, based on user input
                switch (Integer.parseInt(input)) {
                    case 1:
                        Ping(s);
                        break;
                    case 2:
                        UploadData(s);
                        break;
                    case 3: //query specific data
                        System.out.println("Display data:");
                        System.out.println("1: Graph data");
                        System.out.println("2: Raw data");

                        char choice2 = sc.nextLine().charAt(0);

                        switch (choice2) {
                            case '1':
                                GraphQueryResults(QueryData(s));
                                break;
                            case '2':
                                System.out.println(QueryData(s).toString());
//                                JSONArray result = QueryData(s).getJSONArray("result");
//                                for (int i = 0 ; i < result.length() ; i++) {
//                                    System.out.println(result.get(i).toString());
//                                }
                                break;
                            default:
                                System.out.println("Invalid selection.");
                                break;
                        }

                        break;
                    case 4: //query statistc data
                        System.out.println("Please enter which types of reading you want to query (\"light\", \"temperature\", \"humidity\"):");
                        String type = sc.nextLine();
                        System.out.println("Please enter which types of statistic you want to query(\"count\", \"average\", \"min\", \"max\", \"stddev\", \"mode\", \"median\"):");
                        String agg = sc.nextLine();
                        System.out.println(agg + " of " + type + ": " + AggregatedData(s, agg, type, 1).get("result"));
                        break;
                    case 5:
                        GraphStatData(s);
                        break;
                    case 6:
                        GetDataSumm(s); //gets the data summary of all data
                        break;
                    case 7:
                        QueryLogs(s);
                        break;
                }
            } else if (isInteger(input) && Integer.parseInt(input) == 8) { //exits
                break;
            } else {
                System.out.println("Not a valid input!");
            }
        }

        s.close(); //closes connection to server
        System.out.println("Server connection closed");

    }

    //Default constructor
    public ClientGroup22() {
    }

    /**
     * When the user wishes to query the data based on specific filters - allows graph output.
     * @param Socket s the socket that is to be connected through
     */
    public static void QueryLogs(Socket s) {
        JSONObject query; //sent to server
        JSONObject reply; //recieved from server

        Scanner sc = new Scanner(System.in);
        //submenu
        System.out.println("Query logs:");
        System.out.println("1: Get last 20 logs");
        System.out.println("2: Get specific logs");
        System.out.print("Please make a selection: ");

        char choice = sc.nextLine().charAt(0);

        switch (choice) {
            case '1':
                reply = LastLogLines(s);
                JSONArray lines1 = reply.getJSONArray("result");
                for (int i = 0; i < lines1.length(); i++) { //printing out reply from server
                    System.out.println(lines1.get(i).toString());
                }
                break;
            case '2':
                query = GetLogQueryInfo();
                reply = new JSONObject(GetReplyFromServer(query, s));
                System.out.println(reply.toString());
                JSONArray lines2 = reply.getJSONObject("result").getJSONArray("lines");
                JSONObject line = new JSONObject();
                for (int i = 0; i < lines2.length(); i++) { //printing out reply from server
                    line = lines2.optJSONObject(i);
                    System.out.println("Action " + line.get("action") + " run by group " + line.get("group_id") + " at time " + line.get("time"));
                }
                break;
        }


    }
    
    /** 
     * Gets the information the user would like to view from the logs, then sends query to server.
     * @return JSONObject containing the reply from the server
     */
    private static JSONObject GetLogQueryInfo() {
        JSONObject query = new JSONObject(); //sent to server
        JSONObject params = new JSONObject(); //included in what is sent to server
        JSONArray group_ids = new JSONArray(); //included in what is sent to server

        /*Group ids*/
        System.out.println("Please enter in the groups to query by their group ids separated by a space (Enter to confirm):");
        String[] group_idArr = ((new Scanner(System.in)).nextLine()).split(" "); //array of the input given
//        String[] group_idArr = {"22"}; //hardcoded
        for (String i : group_idArr) {
            if (isInteger(i)) { //check if a valid input
                group_ids.put(Integer.parseInt(i)); //pass it through
            } else {
                System.out.println(i + " is not an integer, ignoring.");
            }
        }
        params.put("group_ids", group_ids); //place into parameters JSONObj

        /*Time from*/
        System.out.println("Please enter the time from which you want readings (yyyy-mm-dd hh:mm:ss):");
//        params.put("time_from", new Scanner(System.in).nextLine());
        params.put("time_from", "2013-01-01 01:01:01");

        /*Time to*/
        System.out.println("Please enter the time to which you want readings (yyyy-mm-dd hh:mm:ss):");
//        params.put("time_to", new Scanner(System.in).nextLine());
        params.put("time_to", "2014-01-01 00:00:00");

        /*Limit number of logs*/
        System.out.println("Please enter the limit of the number of logs you want returned:");
//        params.put("limit", new Scanner(System.in).nextLine());
        params.put("limit", 30);

        /*Construct query JSONObject thats sent to the server*/
        query.put("method", "query_logs");
        query.put("group_id", group_id);
        query.put("params", params);

        return query;
    }
    
    /**
     * Special case, when the user only wishes to see the last 20 log lines on the server.
     * @param Socket s the socket that is to be connected through
     * @return JSONObject containing the reply from the server
     */
    public static JSONObject LastLogLines(Socket s) {
        JSONObject query = new JSONObject();

        /*Construct query JSONObject sent to server*/
        query.put("method", "last_log_lines");
        query.put("group_id", group_id);

        JSONObject reply = new JSONObject(GetReplyFromServer(query, s)); //reply from server
        return reply;
    }
    
    /**
     * Pings the server to test connection, printing out server response (if any) and response time in ms.
     * @param Socket s the socket that is to be connected through
     */
    public static void Ping(Socket s) {
        JSONObject query = new JSONObject(); //sent to server
        query.put("method", "ping");
        query.put("group_id", group_id);

        JSONObject reply = new JSONObject(GetReplyFromServer(query, s)); //reply from server

        System.out.println("Reply from Server:" + reply.get("result") + " Time(ms):" + reply.getInt("elapsed")); //print out server reply
    }
    
    /**
     * Allows user to upload sensor data via a text file, or directly typing them in.
     * @param Socket s the socket that is to be connected through
     * @throws IOException 
     */
    public static void UploadData(Socket s) throws IOException {
        JSONObject query = new JSONObject(); //sent to server
        JSONObject params = new JSONObject(); //included in query
        ArrayList<String> dataArr = new ArrayList<String>();

        System.out.println("How would you like to enter the readings?"); //different upload methods
        System.out.println("1: From a file");
        System.out.println("2: Enter readings");
        Scanner sc = new Scanner(System.in);

        char choice = sc.next().toUpperCase().charAt(0);

        switch (choice) {
            case '1':
                dataArr = ReadFromFile(); //from a text file
                break;

            case '2': //manual input
                System.out.println("Please enter the readings you would like to submit, prefixing each subsequent reading by a # (eg: Temp 34.93 1365350128000 #Humidity 43 1365350126542)");
                String input = ((new Scanner(System.in)).nextLine());
                String[] readingsArr = input.split("#");
                String[] temp = new String[3];
                //        String[] group_idArr = {"101"};
                for (String i : readingsArr) {
//                    System.out.println(i);
                    if (isReading(i)) { //test if it's a valid reading format
                        dataArr.add(i);
                    } else {
                        System.out.println(i + " is not a valid reading, ignoring.");
                    }

                }
                break;

            default:
                System.out.println("ERROR: Invalid selection.");
                return;
        }

        /*extracting data from input in order to place into the correct format and send to server*/
        JSONObject reading; //user reading
        JSONArray readings = new JSONArray(); //list of reading from the user for manual input
        String type = "";
        String value = "";
        long time = 0;
        for (int i = 0; i < dataArr.size(); i++) {
            reading = new JSONObject();

            type = dataArr.get(i).split(" ")[0]; //type
            value = dataArr.get(i).split(" ")[1]; //value
            time = Long.parseLong(dataArr.get(i).split(" ")[2]); //time

            if (type.equals("Temp")) { //extracting the type
                type = "temperature";
            } else if (type.equals("Light")) {
                type = "light";
            } else if (type.equals("Humidity")) {
                type = "humidity";
            }

            if (type.equals("light") || type.equals("temperature") || type.equals("humidity")) {
                reading.put("type", type); //placing type into the JSONArray
                reading.put("value", value);
                reading.put("time", time); 

                readings.put(reading); //placing array into object to be placed into params
            }
        }

        params.put("readings", readings);

        query.put("group_id", group_id); //creating query sent to server
        query.put("params", params);
        query.put("method", "new_readings");

//        System.out.println(finalSend.toString());

        JSONObject reply = new JSONObject(GetReplyFromServer(query, s)); //get reply from server

        try {
            System.out.println("Reply from Server:\nERROR: " + reply.get("error"));
        } catch (JSONException e) {
            System.out.println("Reply from Server:\nElapsed: " + reply.get("elapsed") + "\nResult: " + reply.get("result"));
        }
    }

    /**
     * Allows user to query data from the database directly
     * @param Socket s the socket that is to be connected through
     * @return JSONObject containing reply from the server
     */
    public static JSONObject QueryData(Socket s) {
        JSONObject query = new JSONObject(); //sent to server
        JSONObject params = new JSONObject(); //placed into object sent to server
        JSONObject returned = new JSONObject(); //from server
        JSONArray group_ids = new JSONArray(); //all of the required data from specified group ID's
        JSONArray types = new JSONArray();

        String input = "";

        /*Group ids*/
        System.out.println("Please enter in the groups to query by their group ids separated by a space. Leave blank for all (Enter to confirm):");
        input = (new Scanner(System.in)).nextLine();
        if (input.length() > 0) { //if no groups specified, gets all the data based on other filters
            String[] group_idArr = input.split(" ");
            for (String i : group_idArr) {
                if (isInteger(i)) { //checking valid integers in the array
                    group_ids.put(Integer.parseInt(i));
                } else {
                    System.out.println(i + " is not an integer, ignoring.");
                }
            }
            params.put("group_ids", group_ids);
        }

        /*Time from*/
        System.out.println("Please enter the time from which you want readings. Leave blank for all (yyyy-mm-dd hh:mm:ss):");
        input = (new Scanner(System.in)).nextLine();
        if (input.length() > 0) { //if no groups specified, gets all the data based on other filters
            params.put("time_from", input);
        }

        /*Time to*/
        System.out.println("Please enter the time to which you want readings. Leave blank for all (yyyy-mm-dd hh:mm:ss):");
        input = (new Scanner(System.in)).nextLine();
        if (input.length() > 0) {
            params.put("time_to", input);
        }

        /*Types*/
        System.out.println("Please enter which types of readings you want to query, separated by a space. Leave blank for all (\"light\", \"temperature\", \"humidity\"):");
        input = (new Scanner(System.in)).nextLine();
        if (input.length() > 0) {
            String[] typesArr = input.split(" ");
            for (String i : typesArr) {
                types.put(i); //placing the types requested into JSONArray
            }
            params.put("types", types);
        }
        //creating JSONObject to be sent to the server
        query.put("method", "query_readings"); 
        query.put("group_id", group_id);
        query.put("params", params);

        JSONObject reply = new JSONObject(GetReplyFromServer(query, s)); //reply from server

        return reply;
    }

    /**
     * Allows user to return data in it's aggregated form from the database. Includes Min, Max, Mean and Standard deviation 
     * @param Socket s the socket that is to be connected through
     * @param String aggregation the type of data summary required
     * @param String readingType the type of reading to be aggregated
     * @param int id 
     * @return JSONObject containing reply from the server
     */
    public static JSONObject AggregatedData(Socket s, String aggregation, String readingType, int id) {
        JSONObject query = new JSONObject();
        JSONObject params = new JSONObject();
        JSONObject returned = new JSONObject();

        if (id == 1) {
            /*Group id if given*/
            while (true) {
                System.out.println("Please enter in the group to query by their group ID. Enter 0 for all data. (Enter to confirm):");
                Scanner scan = new Scanner(System.in);
                String groupNo = scan.nextLine();
                if (isInteger(groupNo)) {
                    if (Integer.parseInt(groupNo) == 0) {
                        break;
                    } else {
                        params.put("group_id", Integer.parseInt(groupNo));
                        break;
                    }
                } else {
                    System.out.println("Not an valid integer, assuming 0");
                }
            }

            params.put("type", readingType);

            params.put("aggregation", aggregation);

            /*Time from*/
            System.out.println("Please enter the time from which you want readings (yyyy-mm-dd hh:mm:ss):");
            params.put("time_from", new Scanner(System.in).nextLine());
            //params.put("time_from", "2013-01-01 01:01:01");

            /*Time to*/
            System.out.println("Please enter the time to which you want readings (yyyy-mm-dd hh:mm:ss):");
            params.put("time_to", new Scanner(System.in).nextLine());
            //params.put("time_to", "2013-04-10 23:55:01");

        } else {
            params.put("type", readingType);
            params.put("aggregation", aggregation);
        }

        query.put("method", "aggregate");
        query.put("group_id", group_id);
        query.put("params", params);

        returned = new JSONObject(GetReplyFromServer(query, s));

        return returned;
    }

    /**
     * Gets a summary of all the data in the server, including information such as mean, min, max, standard deviation. Grouped by reading type.
     * @param Socket s the socket that is to be connected through
     */
    public static void GetDataSumm(Socket s) {
        JSONObject query = new JSONObject();
        query.put("method", "data_summary");
        query.put("group_id", group_id);

        JSONObject returned = new JSONObject(GetReplyFromServer(query, s));


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
    }

    /**
     * Helper method which reads mote data from a file, which is given as input by the user
     * @return An ArrayList<String> containing each reading as a separate element
     * @throws FileNotFoundException 
     */
    public static ArrayList<String> ReadFromFile() throws FileNotFoundException {    //Works! Don't change!
        Scanner sc1 = new Scanner(System.in);
        System.out.println("Please enter the name of the text file containing new readings (eg MoteDump.txt)");
        String filename = sc1.nextLine();
//        sc1.close();
        ArrayList<String> temp = new ArrayList<String>();
        File file = new File(filename);
        try {
            Scanner sc2 = new Scanner(file);

            while (sc2.hasNext()) {
                String reading = "";
                reading += sc2.next() + " ";
                sc2.next();
                reading += sc2.next() + " ";
                reading += sc2.next();
                if (isReading(reading)) {
                    temp.add(reading);
                } else {
                    System.out.println(reading + " is not a valid reading, ignoring...");
                }
            }
//            sc2.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found. Please try again.");
        }
        return temp;
    }

    /**
     * Helper method to connect to the server through the socket.
     * @return The Socket which has been connected to
     */
    public static Socket Connect() {
        Socket sock = new Socket();
        try {
            sock = new Socket(host, 3000);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return sock;
    }

    /**
     * Helper method to query the database and return relevant information from it.
     * @param JSONObject query the JSONObject to be sent as a query to the server
     * @param Socket s the socket that is to be connected through
     * @return String of the reply from the server
     */
    public static String GetReplyFromServer(JSONObject query, Socket s) {
        String reply = "";
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);

            System.out.println("\nQuerying server...\n");
            out.write(query.toString());
            out.println();

            reply = in.readLine();
        } catch (IOException e) {
            System.out.println(e);
        }
        return reply;
    }

    /**
     * Helper method that checks if a String contains only an integer
     * @param String s the String to be tested
     * @return boolean of whether (true) the string is an int or not (false)
     */
    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    /**
     * Helper method that checks if a String contains only a double
     * @param String s the String to be tested
     * @return boolean of whether (true) the string is a double or not (false)
     */
    private static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    /**
     * Helper method that checks if a String contains only a long
     * @param String s the String to be tested
     * @return boolean of whether (true) the string is a long or not (false)
     */
    private static boolean isLong(String s) {
        try {
            Long.parseLong(s);
        } catch (NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    /**
     * Helper method that checks if a String is a timestamp
     * @param String s the String to be tested
     * @return boolean of whether (true) the string is a timestamp or not (false)
     */
    public static boolean isTimeStamp(String inputString) {
        SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        try {
            format.parse(inputString);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    /**
     * Helper method that checks if a String is a valid reading to be added to the database
     * @param String s the String to be tested
     * @return boolean of whether (true) the string is a valid reading or not (false)
     */
    private static boolean isReading(String i) {
        System.out.println("Checking reading: " + i);
        String[] parts = i.split(" ");
        if (parts.length == 3) {
            if (parts[0].equals("Temp") || parts[0].equals("temperature") || parts[0].equals("Light") || parts[0].equals("light") || parts[0].equals("humidity") || parts[0].equals("Humidity")) {
                if (isInteger(parts[1]) || isDouble(parts[1])) {
                    if (isTimeStamp(parts[2]) || isLong(parts[2])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Helper method which uses the JFreeChart graphing library to graph results of queries
     * @param JSONObject j the JSONObject containing all the information to be graphed
     */
    public static void GraphQueryResults(JSONObject j) { 
        JSONArray results = j.getJSONArray("result");

        System.out.println(results.toString());

        JSONArray light = new JSONArray();
        JSONArray temperature = new JSONArray();
        JSONArray humidity = new JSONArray();

        JSONObject temp = new JSONObject();

        for (int i = 0; i < results.length(); i++) {
            temp = results.getJSONObject(i);
            if (temp.get("type").equals("light")) {
                light.put(temp);
            } else if (temp.get("type").equals("temperature")) {
                temperature.put(temp);
            } else if (temp.get("type").equals("humidity")) {
                humidity.put(temp);
            } else {
            }
        }

        /*Creating the light query graph*/
        if (light.length() > 0) {

            int lightValue;

            XYSeriesCollection lightdataset = new XYSeriesCollection();
            XYSeries lightdata = new XYSeries("Light Query");

            for (int i = 0; i < light.length(); i++) {
                try {
                    lightValue = (int) light.getJSONObject(i).get("value");
                    lightdata.add((i + 1), lightValue);
                } catch (ClassCastException e) {
                    double tempDouble = (double) temperature.getJSONObject(i).get("value");
                    lightdata.add((i + 1), tempDouble);
                }
            }

            lightdataset.addSeries(lightdata);

            JFreeChart lightchart = ChartFactory.createScatterPlot(
                    "Query results", // chart title
                    "Time", // x axis label
                    "Value", // y axis label
                    lightdataset, // data
                    PlotOrientation.VERTICAL,
                    true, // include legend
                    true, // tooltips
                    false // urls
                    );
            XYPlot lightplot = (XYPlot) lightchart.getPlot();
            XYLineAndShapeRenderer lightrenderer = new XYLineAndShapeRenderer();
            lightrenderer.setSeriesLinesVisible(0, true);
            lightplot.setRenderer(lightrenderer);

            ChartPanel lightchartPanel = new ChartPanel(lightchart);
            lightchartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
            ApplicationFrame lightframe = new ApplicationFrame("Queried Data");
            lightframe.setContentPane(lightchartPanel);
            lightframe.pack();
            lightframe.setVisible(true);
        }

        /*Creating the temperature query graph*/
        if (temperature.length() > 0) {

            int temperatureValue;

            XYSeriesCollection temperaturedataset = new XYSeriesCollection();
            XYSeries temperaturedata = new XYSeries("Temperature Query");

            for (int i = 0; i < temperature.length(); i++) {
                try {
                    temperatureValue = (int) temperature.getJSONObject(i).get("value");
                    temperaturedata.add((i + 1), temperatureValue);
                } catch (ClassCastException e) {
                    double tempDouble = (double) temperature.getJSONObject(i).get("value");
                    temperaturedata.add((i + 1), tempDouble);
                }
                //time = (String)temperature.getJSONObject(i).get("time");
            }

            temperaturedataset.addSeries(temperaturedata);

            JFreeChart temperaturechart = ChartFactory.createScatterPlot(
                    "Query results", // chart title
                    "Time", // x axis label
                    "Value", // y axis label
                    temperaturedataset, // data
                    PlotOrientation.VERTICAL,
                    true, // include legend
                    true, // tooltips
                    false // urls
                    );
            XYPlot temperatureplot = (XYPlot) temperaturechart.getPlot();
            XYLineAndShapeRenderer temperaturerenderer = new XYLineAndShapeRenderer();
            temperaturerenderer.setSeriesLinesVisible(0, true);
            temperatureplot.setRenderer(temperaturerenderer);

            ChartPanel temperaturechartPanel = new ChartPanel(temperaturechart);
            temperaturechartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
            ApplicationFrame temperatureframe = new ApplicationFrame("Queried Data");
            temperatureframe.setContentPane(temperaturechartPanel);
            temperatureframe.pack();
            temperatureframe.setVisible(true);
        }

        /*Creating the humidity query graph*/
        if (humidity.length() > 0) {

            int humidityValue;

            XYSeriesCollection humiditydataset = new XYSeriesCollection();
            XYSeries humiditydata = new XYSeries("Humidity Query");

            for (int i = 0; i < humidity.length(); i++) {
                try {
                    humidityValue = (int) humidity.getJSONObject(i).get("value");
                    humiditydata.add((i + 1), humidityValue);
                } catch (ClassCastException e) {
                    double tempDouble = (double) humidity.getJSONObject(i).get("value");
                    humiditydata.add((i + 1), tempDouble);
                }
                //time = (String)humidity.getJSONObject(i).get("time");
            }

            humiditydataset.addSeries(humiditydata);

            JFreeChart humiditychart = ChartFactory.createScatterPlot(
                    "Query results", // chart title
                    "Time", // x axis label
                    "Value", // y axis label
                    humiditydataset, // data
                    PlotOrientation.VERTICAL,
                    true, // include legend
                    true, // tooltips
                    false // urls
                    );
            XYPlot humidityplot = (XYPlot) humiditychart.getPlot();
            XYLineAndShapeRenderer humidityrenderer = new XYLineAndShapeRenderer();
            humidityrenderer.setSeriesLinesVisible(0, true);
            humidityplot.setRenderer(humidityrenderer);

            ChartPanel humiditychartPanel = new ChartPanel(humiditychart);
            humiditychartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
            ApplicationFrame humidityframe = new ApplicationFrame("Queried Data");
            humidityframe.setContentPane(humiditychartPanel);
            humidityframe.pack();
            humidityframe.setVisible(true);

        }
    }

    /**
     * Uses the JFreeChart graphing library to graph results of stat queries
     * @param Socket s the socket that is to be connected through
     */
    public static void GraphStatData(Socket s) {

        /*Chart display*/
        double lightMean = (double) AggregatedData(s, "mean", "light", 0).get("result");
        int lightMin = (int) AggregatedData(s, "min", "light", 0).get("result");
        int lightMax = (int) AggregatedData(s, "max", "light", 0).get("result");
        double lightStddev = (double) AggregatedData(s, "stddev", "light", 0).get("result");

        double temperatureMean = (double) AggregatedData(s, "mean", "temperature", 0).get("result");
        int temperatureMin = (int) AggregatedData(s, "min", "temperature", 0).get("result");
        double temperatureMax = (double) AggregatedData(s, "max", "temperature", 0).get("result");
        double temperatureStddev = (double) AggregatedData(s, "stddev", "temperature", 0).get("result");

        double humidityMean = (double) AggregatedData(s, "mean", "humidity", 0).get("result");
        int humidityMin = (int) AggregatedData(s, "min", "humidity", 0).get("result");
        int humidityMax = (int) AggregatedData(s, "max", "humidity", 0).get("result");
        double humidityStddev = (double) AggregatedData(s, "stddev", "humidity", 0).get("result");

        /*Light Chart*/
        DefaultCategoryDataset lightds = new DefaultCategoryDataset();
        lightds.addValue(lightMean, "Mean", "");
        lightds.addValue(lightMin, "Min", "");
        lightds.addValue(lightMax, "Max", "");
        lightds.addValue(lightStddev, "Standard Deviation", "");

        JFreeChart lightbc = ChartFactory.createBarChart("Light Statistics", "Key", "Value", lightds, PlotOrientation.VERTICAL, true, false, false);

        CategoryPlot lightmainPlot = lightbc.getCategoryPlot();

        NumberAxis lightmainAxis = (NumberAxis) lightmainPlot.getRangeAxis();;
        lightmainAxis.setLowerBound(0);
        lightmainAxis.setUpperBound(500);

        ChartFrame lightcf = new ChartFrame("Data", lightbc);
        lightcf.setSize(800, 600);
        lightcf.setVisible(true);

        /*temperature Chart*/
        DefaultCategoryDataset tempds = new DefaultCategoryDataset();
        tempds.addValue(temperatureMean, "Mean", "");
        tempds.addValue(temperatureMin, "Min", "");
        tempds.addValue(temperatureMax, "Max", "");
        tempds.addValue(temperatureStddev, "Standard Deviation", "");

        JFreeChart tempbc = ChartFactory.createBarChart("Temperature Statistics", "Key", "Value", tempds, PlotOrientation.VERTICAL, true, false, false);

        CategoryPlot tempmainPlot = tempbc.getCategoryPlot();

        NumberAxis tempmainAxis = (NumberAxis) tempmainPlot.getRangeAxis();;
        tempmainAxis.setLowerBound(0);
        tempmainAxis.setUpperBound(50);

        ChartFrame tempcf = new ChartFrame("Data", tempbc);
        tempcf.setSize(800, 600);
        tempcf.setVisible(true);

        /*humidity Chart*/
        DefaultCategoryDataset humidityds = new DefaultCategoryDataset();
        humidityds.addValue(humidityMean, "Mean", "");
        humidityds.addValue(humidityMin, "Min", "");
        humidityds.addValue(humidityMax, "Max", "");
        humidityds.addValue(humidityStddev, "Standard Deviation", "");

        JFreeChart humiditybc = ChartFactory.createBarChart("Humidity Statistics", "Key", "Value", humidityds, PlotOrientation.VERTICAL, true, false, false);

        CategoryPlot humiditymainPlot = humiditybc.getCategoryPlot();

        NumberAxis humiditymainAxis = (NumberAxis) humiditymainPlot.getRangeAxis();;
        humiditymainAxis.setLowerBound(0);
        humiditymainAxis.setUpperBound(50);

        ChartFrame humiditycf = new ChartFrame("Data", humiditybc);
        humiditycf.setSize(800, 600);
        humiditycf.setVisible(true);
    }
}
