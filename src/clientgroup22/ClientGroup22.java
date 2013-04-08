/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clientgroup22;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author matthew
 */
public class ClientGroup22 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException, IOException {

        Connect();
        System.out.println("Hello0");
    }

    public static void Connect() {
        try {
            Socket sock = new Socket("197.85.191.195", 3000);

            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

        } catch (IOException e) {
            System.out.println(e);
        }

    }
}
