/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import tgfx.tinyg.TinygDriver;
import gnu.io.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;


/**
 *
 * @author ril3y
 */
public class SerialDriver implements SerialPortEventListener {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SerialWriter.class);
    private boolean connectionState = false;
    public String portArray[] = null; 
    public SerialPort serialPort;
    private String port;
    public InputStream input;
    public OutputStream output;
    private boolean CANCELLED = false;
    private static byte[] lineBuffer = new byte[1024];
    private static int lineIdx = 0;
    public String debugFileBuffer = "";
    public byte[] debugBuffer = new byte[1024];
    public ArrayList<String> lastRes = new ArrayList();
    public double offsetPointer = 0;
 
    
  

    public void write(String str) {
        try {
            this.output.write(str.getBytes());
            logger.info("Wrote Line: " + str);
        } catch (Exception ex) {
            logger.error("Error in SerialDriver Write");
            logger.error("\t" + ex.getMessage());
        }


    }

    public void priorityWrite(String str) throws Exception {
        this.output.write(str.getBytes());
    }

    public void priorityWrite(Byte b) throws Exception {
        logger.debug("[*] Priority Write Sent\n");
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
                for (int i = 0; i < cnt; i++) {
                    if(inbuffer[i] == 0x11 || inbuffer[i] == 0x13){  //We have to filter our XON or XOFF charaters from JSON
                        continue;
                    }
                    if ( inbuffer[i] == 0xA) { // inbuffer[i] is a \n
                        String f = new String(lineBuffer, 0, lineIdx);
                        if(!f.equals("")){ //Do not add "" to the jsonQueue..
                            TinygDriver.getInstance().resParse.appendJsonQueue(f);
                        }
                        lineIdx = 0;
                    } else {
                        lineBuffer[lineIdx++] = inbuffer[i];
                    }
                }

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

            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
            serialPort.setRTS(true);
            
            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);

            logger.debug("[+]Opened " + port + " successfully.");
            setConnected(true); //Register that this is connectionState.
            return true;

        } catch (PortInUseException ex) {
            logger.error("[*] Port In Use Error: " + ex.getMessage());
            return false;
        } catch (NoSuchPortException ex) {
            logger.error("[*] No Such Port Error: " + ex.getMessage());
            return false;
        } catch (Exception ex) {
            logger.error("[*] " + ex.getMessage());
            return false;
        }

    }
}
