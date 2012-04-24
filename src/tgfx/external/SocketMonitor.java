/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.external;

import tgfx.SerialDriver;
import gnu.io.SerialPort;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import tgfx.TinygDriver;

/**
 *
 * @author ril3y
 */
public class SocketMonitor {

    private SerialDriver ser = SerialDriver.getInstance();
    private final int LISTENER_PORT = 4444;
    private ServerSocket server;
    private int clientCount = 0;

    public SocketMonitor() {
        this.initServer();
        this.handleConnections();

    }

    int countClientConnections() {
        return (clientCount);
    }

    boolean initServer() {
        try {
            server = new ServerSocket(LISTENER_PORT);
            return (true);
        } catch (IOException e) {
            System.out.println("Could not listen on port: 4444");
            return (false);
        }
    }

    public void handleConnections() {
        System.out.println("[+]Remote Monitor Listening for Connections....");

        while (ser.isConnected()) {
            try {
                final Socket socket = server.accept();
                new ConnectionHandler(socket);

            } catch (IOException ex) {
                System.out.println("[!]Error: " + ex.getMessage());

            }
        }
        System.out.println("[!]Socket Monitor Terminated...");

    }

    public SocketMonitor(ServerSocket server) {
        this.server = server;
    }
}
/*
 * New Class Here
 */

class ConnectionHandler implements Runnable, Observer {

    public Socket socket;

    @Override
    public void update(Observable o, Object arg) {

        String[] MSG = (String[]) arg;

        if (MSG[0] == "JSON") {
            final String line = MSG[1];
//             System.out.println("UPDATE: "+MSG[1]);
            try {
                this.write(MSG[1] + "\n");
            } catch (Exception ex) {
                //
                System.out.println(ex.getMessage());
            }

        }


//        System.out.println("Got and UPDATE in ConnectionHandler");

    }

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
        
        SerialDriver ser = SerialDriver.getInstance();
        ser.addObserver(this);
        Thread t = new Thread(this);
        t.start();
    }

    private void write(String l) throws Exception {
        //Method for writing to the socket
        socket.getOutputStream().write(l.getBytes());
    }

    public void run() {
        try {
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));


//            System.out.println("GOT: " + stdIn.readLine());
            try {
                this.write("[+]Connected to tgFX\n");
            } catch (Exception ex) {
            }
            TinygDriver tg = TinygDriver.getInstance();
            String line = "";
            SerialDriver ser = SerialDriver.getInstance();
            while (ser.isConnected()) {
                try {
                    line = stdIn.readLine() + "\n";
                    this.write("Writing: " + line);
                    tg.write(line);
                    Thread.sleep(100);
                } catch (Exception ex) {
                }



            }
            System.out.println("[+]Closing Remote Listener Socket");
            socket.close();

//            while (true) {
//                try {
//
//                    String l = new String("::HEARTBEAT::\n");
//                    if (ser.isConnected()) {
//                        this.write(l);
////                        ser.write("{\"sys\":null}\n");
//                    }else{
//                        this.write("[+]Serial Port Not Connected\n");
//                    }
//                    
//
//                    Thread.sleep(50);
//                } catch (Exception ex) {
//                    System.out.println(ex.getMessage());
//                }
//
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}