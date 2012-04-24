/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import gnu.io.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    private boolean CANCELLED = false;
    private Boolean CLEAR_TO_TRANSMIT = true;
    //DEBUG
    public ByteArrayOutputStream bof = new ByteArrayOutputStream();
    public String debugFileBuffer = "";
    public byte[] debugBuffer = new byte[1024];
    public ArrayList<String> lastRes = new ArrayList();
    public double offsetPointer = 0;
    //DEBUG

    public synchronized void write(String str) throws Exception {
        this.output.write(str.getBytes());
        setClearToSend(false);
        setChanged();
//        notifyObservers("TEST");

    }

    public synchronized void priorityWrite(String str) throws Exception {
        //This is for sending control characters.. Pause Resume Reset.. etc
        //This does not check for the buffer being full it just sends
        //whatever your str is.  This SHOULD NOT be abused.  There are only
        //a few valid places where this is used.
        this.output.write(str.getBytes());
    }

    private SerialDriver() {
    }

    public static SerialDriver getInstance() {
        return SerialDriverHolder.INSTANCE;
    }

    private static class SerialDriverHolder {

        private static final SerialDriver INSTANCE = new SerialDriver();
    }

    public synchronized void disconnect() {

        if (serialPort != null) {
            //serialPort.removeEventListener();
            serialPort.close();
            setConnected(false); //Set our disconnected state
            


        }
    }

    public boolean isCANCELLED() {
        return CANCELLED;
    }

    public void setCANCELLED(boolean choice) {
        this.CANCELLED = choice;
    }

//    public boolean isPAUSED() {
//        return PAUSED;
//    }
//    public void setPAUSED(boolean PAUSED) {
//        this.PAUSED = PAUSED;
//    }
    public void setConnected(boolean c) {

        this.connectionState = c;

    }

    public String getDebugFileString() {
        return (debugFileBuffer);
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
                debugFileBuffer = debugFileBuffer + res;
                lastRes.add(res);
                if (res.contains("msg")) {
                    this.setClearToSend(true);
                } else if (res.contains("####")) {  //When TinyG is reset you will get this message
                    /**
                     * #### TinyG version 0.93 (build 334.01) "Fanny Pack" ####
                     * #### Zen Toolworks 7x12 Profile #### Type h for help
                     * tinyg[mm] ok>
                     */
                    //Machine was reset... Let the GUI know about the machine being reset.
                    this.priorityWrite(TinygDriver.CMD_GET_STATUS_REPORT);
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
//            if (l.equals("{")) {
//                System.out.println("l");
//            }
            if (l.startsWith("{\"") && l.endsWith("}}") && buf.equals("")) {  //The buf check makes sure
                //The serial event didn't not cut off at the perfect spot and send something like this:
                //"{"gc":{"gc":"F300.0","st":0,"msg":"OK"}}  
                //Which is missing the front part of that line "{"gc":
                MSG[1] = l;
                buf = "";  //Valid line clear the buffer
                getOKcheck(l);
                setChanged();
                notifyObservers(MSG);

            }else if (l.startsWith(TinygDriver.RESPONSE_FIRMWARE_BUILD) ||
                    l.startsWith(TinygDriver.RESPONSE_FIRMWARE_VERSION) && l.endsWith("}")) {
                //Firmware Build Value
                MSG[1] = l;
                buf = "";
                getOKcheck(l);
                setChanged();
                notifyObservers(MSG);
            } 
            
            else if (l.startsWith("{\"") && l.endsWith("}")) {
                //This is a input command
                //{"ee":"1"}
                buf = "";
                continue;
            } else if (l.startsWith("{\"")) {
                //System.out.println("!! GCODE LINE STARTS WITH { !!" + l);
                buf = l;


            } else if (l.endsWith("}}")) {
                //System.out.println("!! GCODE LINE ENDS WITH { !!" + l);
                buf = buf + l;
                if (buf.startsWith("{\"") && buf.endsWith("}}")) {
                    getOKcheck(buf);
                    buf = "";
                } else {
                    System.out.println("SERIAL DRIVER CODE: SHOULD NOT HIT THIS");
                    System.out.println(buf);
                }
            } else {
                //If we happen to get a single { as a line this code puts it into the buf var.
                //
                buf = l;
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
            setClearToSend(true);
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
