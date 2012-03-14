/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import gnu.io.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author ril3y
 */
public class SerialDriver extends Observable implements SerialPortEventListener {

    
    private final Lock accessLock = new ReentrantLock();
    private final Condition canWrite = accessLock.newCondition();
    private final Condition canRead = accessLock.newCondition();
    private boolean connectionState = false;
    public String portArray[] = null; //Holder 
    public SerialPort serialPort;
    private String port;
    private String buf = "";
    public InputStream input;
    public OutputStream output;
    private boolean PAUSED = false;
    private Boolean CLEAR_TO_TRANSMIT = true;

    public synchronized void write(String str) throws Exception {
        setClearToSend(false);  //reset our flow flag "msg" for now in serialEvent 
        setChanged();
        this.output.write(str.getBytes());
    }

    public synchronized void priorityWrite(String str) throws Exception {
        //This is for sending control characters.. Pause Resume Reset.. etc
        this.output.write(str.getBytes());
    }

    private SerialDriver() {
    }

    //    public static asdf getInstance() {
//        return asdf.asdfHolder.INSTANCE;
//    }
    public static SerialDriver getInstance() {
        return SerialDriverHolder.INSTANCE;
    }

    private static class SerialDriverHolder {

        private static final SerialDriver INSTANCE = new SerialDriver();
    }

//    public void read() throws Exception {
//
//        int available = this.input.available();
//        if (available > 0) {
//            byte chunk[] = new byte[available];
//            this.input.read(chunk, 0, available);
//            buf = new String(chunk);
//            String[] lines = buf.split("\n");
//            for (String l : lines) {
//                if (l.contains("msg")) {
//                    this.setClearToSend(true);
//                }
//            }
//        }
//        Thread.sleep(100);
//    }
//            System.out.println("*****NOT CLEAR*****");
//        }else{
//            System.out.println("$$$$$$$$$$$$$RECURSION$$$$$$$$$$$$$");
//            this.write(str); //recurrsion...
//            Thread.sleep(100);
//        }
//        if (CLEAR_TO_TRANSMIT.get()) {
//            this.output.write(str.getBytes());
//            System.out.println("Writing...." + str + "\n");
//        } else {
//        }
//        this.output.write(str.getBytes());
//        if (this.input.available() > 0) {
//            int available = input.available();
//            int leftOffAtBuffer;
//            byte chunk[] = new byte[available];
//
//        }
//        input.read(chunk, 0, available);
//
//        String res = new String(chunk); //Convert the byte[] to a string
//        for (String line : res.split("\n")) {
//            if (line.contains("ok")) {
//                continue;
//
//            } else {
//                //This occurs when there the recv tinyg buffer is full. 
//                //It should wait here and continue to read until the buffer is 
//                //ready to recv again.
//                while (!res.contains("ok")) {
//
//                    leftOffAtBuffer = available;
//                    available = input.available();
//                    byte chunk2[] = new byte[available];
//
//                    input.read(chunk2, leftOffAtBuffer, available);
//                    res = res + new String(chunk);
//                }
//                String[] lines = res.split("\n");
//                for (String l : lines) {
//                    setChanged();
//                    notifyObservers(l + "\n");
//                }
//            }
//        }
//    }
//}
//
//            if (res.contains("ok")) {
//                String[] lines = res.split("\n");
//                for (String l : lines) {
//                    setChanged();
//                    notifyObservers(l + "\n");
//
//                }
//            } else {
//                //This occurs when there the recv tinyg buffer is full. 
//                //It should wait here and continue to read until the buffer is 
//                //ready to recv again.
//                while (!res.contains("ok")) {
//
//                    leftOffAtBuffer = available;
//                    available = input.available();
//                    byte chunk2[] = new byte[available];
//
//                    input.read(chunk2, leftOffAtBuffer, available);
//                    res = res + new String(chunk);
//                }
//                String[] lines = res.split("\n");
//                for (String l : lines) {
//                    setChanged();
//                    notifyObservers(l + "\n");
//                }
//            }
//        }
//
//        System.out.println("SERIALPORT: Writing " + str);
//        //Thread.sleep(CMD_DELAY);
//    }
    public void disconnect() {

        if (serialPort != null) {
//            serialPort.removeEventListener();
            serialPort.close();
            setConnected(false); //Set our disconnected state

        }
    }

    public boolean isPAUSED() {
        return PAUSED;
    }

    public void setPAUSED(boolean PAUSED) {
        this.PAUSED = PAUSED;
    }

    public void setConnected(boolean c) {

        this.connectionState = c;

    }

    public boolean isConnected() {
        return this.connectionState;
    }

    public synchronized boolean getClearToSend() {
        return (this.CLEAR_TO_TRANSMIT);
    }

    public synchronized void setClearToSend(boolean c) throws Exception {
        this.CLEAR_TO_TRANSMIT = c;

//        Thread.sleep(10);
    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {

        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                int available = input.available();   //Get the size of data in the input buffer
                byte chunk[] = new byte[available];  //Setup byte array to store the data.

                input.read(chunk, 0, available);  //Read the data into the byte array
                String res = new String(chunk);   //Convert the byte[] to a string
                if (res.contains("msg")) {
                    this.setClearToSend(true);
                }

                //Spilt the data into lines
                buildString(res);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

        }
    }

    void buildString(String res) throws Exception {

        String[] MSG = new String[2];

        /**
         * Process $ input commands at the console
         */
        if (res.contains("tinyg[")) {
            String ROUTING_TAG = "PLAIN";
            MSG[0] = ROUTING_TAG;

            //This occurs if someone types in a $ value at the console
            String[] lines = res.split("\n");
            for (String l : lines) {
                MSG[1] = l;
                setChanged();
                notifyObservers(MSG);
            }
            return;
        }



        /**
         * Build JSON Lines
         */
        MSG[0] = "JSON";
        String[] lines = res.split("\n");
        //This code strings together lines that do not start with valid json objects
        for (String l : lines) {
            if (l.startsWith("{") && l.endsWith("}")) {
                MSG[1] = l;
                buf = "";  //Valid line clear the buffer
                getOKcheck(l);
                setChanged();
                notifyObservers(MSG);

            } else if (l.startsWith("{")) {
                //System.out.println("!! GCODE LINE STARTS WITH { !!" + l);
                buf = l;


            } else if (l.endsWith("}")) {
                //System.out.println("!! GCODE LINE ENDS WITH { !!" + l);
                buf = buf + l;
                if (buf.startsWith("{") && buf.endsWith("}")) {
                    getOKcheck(buf);
                    buf = "";
                } else {
                    System.out.println(buf);
                }
            }
        }
    }

    private void getOKcheck(String l) throws Exception {
        if (l.startsWith("{\"gc\":{\"gc\":")) {
            //This is our "OK" buffer message.  If we get inside the code then we got a response
            //From TinyG and we are good to push more data into TinyG.
            setClearToSend(true);  //Set the clear to send flag to True.
            //DEBUG
//            setChanged();
//            notifyObservers("[+]Clear to Send Recvd.\n");
            //DEBUG
        } else {
//            setChanged();
//            notifyObservers(l + "\n");
        }

    }

    @Override
    public synchronized void addObserver(Observer o) {
        super.addObserver(o);
    }

    @Override
    public void notifyObservers() {
        super.notifyObservers();
    }

    @Override
    protected synchronized void setChanged() {
        super.setChanged();
    }

    public static String[] listSerialPorts() {
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        ArrayList portList = new ArrayList();
        String portArray[] = null;
        while (ports.hasMoreElements()) {
            CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
            if (port.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                portList.add(port.getName());
            }
        }
        portArray = (String[]) portList.toArray(new String[0]);
        return portArray;
    }

    public boolean initialize(String port, int DATA_RATE) {
        int TIME_OUT = 2000;
        this.port = port;

        if (isConnected()) {
            String returnMsg = "[*] Port Already Connected.\n";
            return (true);
        }

        try {
            CommPortIdentifier portId =
                    CommPortIdentifier.getPortIdentifier(port);

            // Get the port's ownership
            serialPort =
                    (SerialPort) portId.open("TG", TIME_OUT);
            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // open the streams
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
//            serialPort.setFlowControlMode(serialPort.FLOWCONTROL_XONXOFF_IN);
//            serialPort.setInputBufferSize(64);
//            serialPort.setOutputBufferSize(64);

            System.out.println("[+]Opened " + port + " successfully.");
            setConnected(true); //Register that this is connectionState.

            return true;
        } catch (PortInUseException ex) {
            System.out.println("[*] Port In Use Error: " + ex.getMessage());
            return false;
        } catch (NoSuchPortException ex) {
            System.out.println("[*] No Such Port Error: " + ex.getMessage());
            return false;
        } catch (Exception ex) {
            System.out.println("[*] " + ex.getMessage());
            return false;
        }

    }
}
