package Read_Serial;

/*This file allows to display the data sent by a node connected to 
 the computer.
 It is a 'much' simplified version of the Energest Demo code written 
 by Frederik Osterlind - SICS Lab - Sweden.
 The original code is in the energest demo folder, located in the 
 examples/energest/src folder of the contiki-2.3 distribution */

/*DFS: Usage: java Junix /dev/ttyUSB0 */
//import clientgroup22.ClientGroup22;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import javax.swing.Timer;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Junix {

    public static final String SERIALDUMP_LINUX = "/home/wesley/Desktop/serialdump-linux";
    private static Process serialDumpProcess;
    private static String comPort = "/dev/ttyUSB1";
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static ArrayList<String> dataReadArr = new ArrayList<String>();
    private static ArrayList<String> moteDataArr = new ArrayList<String>();
//    private static final String motePort = "/dev/ttyUSB0";
    private static File dumpFile = new File("MoteDump.txt");

    public static void main(String[] args) {
        Junix j = new Junix();
        j.getDataFromMote(comPort, TimeUnit.MINUTES, 1);
    }

    public Junix() {
    }

    public void getDataFromMote(String comPort, TimeUnit unit, int duration) {
        moteDataArr = Junix.readMote(unit, duration);
        System.out.println("Data read.");
        System.out.println("Writing to file...");

        try {
            dumpFile.setWritable(true);
            BufferedWriter bw = new BufferedWriter(new FileWriter(dumpFile));
            for (int i = 0; i < moteDataArr.size(); i++) {
                bw.write(moteDataArr.get(i));
                bw.newLine();
            }
            bw.close();
        } catch (IOException ex) {
            System.out.println("ERROR DUMPING TO FILE");
            //Logger.getLogger(ClientGroup22.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Done, exiting.");
        System.exit(1); // for now anyway
    }

    public static ArrayList<String> readMote(TimeUnit unit, int period) {
        System.out.println("Listening on COM port: " + comPort);

        /* Connect to COM using external serialdump application */
        String fullCommand;
        fullCommand = SERIALDUMP_LINUX + " " + "-b115200" + " " + comPort;

        try {
            String[] cmd = fullCommand.split(" ");
            serialDumpProcess = Runtime.getRuntime().exec(cmd);

            final BufferedReader input = new BufferedReader(new InputStreamReader(serialDumpProcess.getInputStream()));
            final BufferedReader err = new BufferedReader(new InputStreamReader(serialDumpProcess.getErrorStream()));

            /* Start thread listening on stdout */
            Thread readInput = new Thread(new Runnable() {
                public void run() {
                    String line;
                    try {
                        while ((line = input.readLine()) != null) {
                            parseIncomingLine(line);
                        }
                        input.close();
                        System.out.println("Serialdump process shut down,read complete");
                    } catch (IOException e) {
                        System.out.println("Exception when reading from serialdump");
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }, "read input stream thread");


            readInput.start();
            switch (unit) {
                case SECONDS:
                    TimeUnit.SECONDS.sleep(period);
                    break;
                case MINUTES:
                    TimeUnit.MINUTES.sleep(period);
                    break;
                case HOURS:
                    TimeUnit.HOURS.sleep(period);
                    break;
                case DAYS:
                    TimeUnit.DAYS.sleep(period);
                    break;
            }

            return dataReadArr;// return the data read

        } catch (Exception e) {
            System.out.println("Exception when executing '" + fullCommand + "' when trying to read device");
            System.out.println("Exiting demo application");
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    public static void parseIncomingLine(String line) {
        if (line == null) {
            System.out.println("Parsing null line");
            return;
        }
        //completeRead = completeRead + line + " at time: " + df.format(new Date()) + "\n";
        //completeRead = completeRead + line + " " + System.currentTimeMillis() + "\n";
        String fullRead = line + " " + System.currentTimeMillis();
        dataReadArr.add(fullRead);
    }
}