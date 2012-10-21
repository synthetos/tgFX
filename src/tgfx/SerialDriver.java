/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import tgfx.tinyg.TinygDriver;
import gnu.io.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author ril3y
 */
public class SerialDriver implements SerialPortEventListener {

    private final boolean DEBUG = true;
//    private final boolean DEBUG = false;
    private boolean connectionState = false;
    public String portArray[] = null; //Holder 
    public SerialPort serialPort;
    private String port;
    private String buf = "";
    private String flow = new String();
    private static final Object mutex = new Object(); 
    public InputStream input;
    public OutputStream output;
    private static boolean throttled = false;
    private boolean PAUSED = false;
    private boolean CANCELLED = false;
    //DEBUG
//    public ByteArrayOutputStream bof = new ByteArrayOutputStream();
    private static byte[] lineBuffer = new byte[1024];
    private static int lineIdx = 0;
    public String debugFileBuffer = "";
    public byte[] debugBuffer = new byte[1024];
    public ArrayList<String> lastRes = new ArrayList();
    public double offsetPointer = 0;
    //DEBUG

    public void write(String str) {
        try {
            synchronized (mutex) {
                while (throttled) {
                    mutex.wait();
                }
            }
            this.output.write(str.getBytes());
        } catch (Exception ex){
            Main.logger.error("Error in SerialDriver Write");   
        }
        
    }

    public  void priorityWrite(String str) throws Exception {
        this.output.write(str.getBytes());
    }
    
    public  void priorityWrite(Byte b) throws Exception {
        Main.logger.debug("[*] Priority Write Sent\n");
        this.output.write(b);
    }

    private SerialDriver() {
    }

    public static SerialDriver getInstance() {
        return SerialDriver.SerialDriverHolder.INSTANCE;
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

    public boolean setThrottled(boolean t) {
        synchronized (mutex) {
          if (t == throttled)
            return false;
          throttled = t;
            if (!throttled)
                mutex.notify();
        }
        return true;
    }
    public void setConnected(boolean c) {
        this.connectionState = c;
    }

    public String getDebugFileString() {
        return (debugFileBuffer);
    }

    public boolean isConnected() {
        return this.connectionState;
    }

    @Override
    public void serialEvent(SerialPortEvent oEvent) {
        byte[] inbuffer = new byte[1024];

        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                int cnt = input.read(inbuffer, 0, inbuffer.length);
                for (int i=0; i < cnt; i++) {
                    if (inbuffer[i] == 0x13) {
                        System.out.println("Got XOFF");
//                        setThrottled(true);
                    } else if (inbuffer[i] == 0x11) {
                        System.out.println("Got XON");
//                        setThrottled(false);
                    } else if (inbuffer[i] == '\n') {
                        String f = new String(lineBuffer, 0, lineIdx);
 //                       Main.logger.debug("full line |" + f + "|");                        
                        TinygDriver.getInstance().resParse.appendJsonQueue(f);
                        lineIdx = 0;
                    } else
                    lineBuffer[lineIdx++] = inbuffer[i];
                }

                //Flow.logger.debug("Serial Event Read In: <-- " + String.valueOf(chunk.length) + " Bytes...");
 //               TinygDriver.getInstance().appendResponseQueue(chunk);
            } catch (Exception ex) {
                System.out.println("Exception in Serial Event");
            }
        }
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
            CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(port);
            // Get the port's ownership
            serialPort = (SerialPort) portId.open("TG", TIME_OUT);
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
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN);
            serialPort.setInputBufferSize(15000);
            serialPort.setOutputBufferSize(254);

            Main.logger.debug("[+]Opened " + port + " successfully.");
            setConnected(true); //Register that this is connectionState.
//            TinygDriver.getInstance().setClearToSend(true);
            return true;

        } catch (PortInUseException ex) {
            Main.logger.error("[*] Port In Use Error: " + ex.getMessage());
            return false;
        } catch (NoSuchPortException ex) {
            Main.logger.error("[*] No Such Port Error: " + ex.getMessage());
            return false;
        } catch (Exception ex) {
            Main.logger.error("[*] " + ex.getMessage());
            return false;
        }

    }
}
