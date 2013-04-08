/*This file allows to display the data sent by a node connected to 
the computer.
It is a 'much' simplified version of the Energest Demo code written 
by Frederik Osterlind - SICS Lab - Sweden.
The original code is in the energest demo folder, located in the 
examples/energest/src folder of the contiki-2.3 distribution */

/*DFS: Usage: java Junix /dev/ttyUSB0 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import javax.swing.Timer;
import java.awt.event.*;

public class Junix {
  public static final String SERIALDUMP_LINUX = "/home/wesley/Desktop/serialdump-linux";    //NOTE: This path must be changed to point to wherever the serialdump-linux file is on your pc!

  private Process serialDumpProcess;

  private String comPort;

  public Junix(String comPort) {
    this.comPort = comPort;
    System.out.println("Listening on COM port: " + comPort);

    /* Connect to COM using external serialdump application */
    String fullCommand;
    fullCommand = SERIALDUMP_LINUX + " " + "-b115200" + " " + comPort;
 
    try {
      String[] cmd = fullCommand.split(" ");
      serialDumpProcess = Runtime.getRuntime().exec(cmd);


      final BufferedReader input = new BufferedReader
		(new InputStreamReader(serialDumpProcess.getInputStream()));
      final BufferedReader err = new BufferedReader
		(new InputStreamReader(serialDumpProcess.getErrorStream()));

      /* Start thread listening on stdout */
      Thread readInput = new Thread(new Runnable() {
        public void run() {
          String line;
          try {
            while ((line = input.readLine()) != null) {
              parseIncomingLine(line);
            }
            input.close();
            System.out.println("Serialdump process shut down, exiting");
            System.exit(1);
          } catch (IOException e) {
            System.err.println("Exception when reading from serialdump");
            e.printStackTrace();
            System.exit(1);
          }
        }
      }, "read input stream thread");

 
      readInput.start();
 
    } catch (Exception e) {
      System.err.println("Exception when executing '" + fullCommand + "'");
      System.err.println("Exiting demo application");
      e.printStackTrace();
      System.exit(1);
    }
  }

  
  
  public void parseIncomingLine(String line) {
    if (line == null) {
      System.err.println("Parsing null line");
      return;
    }

    /* Split line into components 
    *  begin --- Handle the output here:
    */
    String[] components = line.split(" ");
 
    /* end --- Handle the output here */

    for (int i=0; i<components.length; i++) {
        System.out.print(components[i]+" ");
    }
    System.out.println();
 
   }


 

  public static void main(final String[] args) {
    if (args.length != 1) {
      System.err.println("Usage: java Demo COMPORT [TRACK_NODE_ID]");
      return;
    }

    final String comPort = args[0];
    new Junix(comPort);
   }
}
