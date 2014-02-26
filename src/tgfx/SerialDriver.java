/*
 * Copyright (C) 2013-2014 Synthetos LLC. All Rights reserved.
 * http://www.synthetos.com
 */
package tgfx;

import tgfx.tinyg.TinygDriver;
//import gnu.io.*;
import jssc.SerialPort;
import jssc.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import tgfx.utility.UtilityFunctions;

/**
 *
 * @author ril3y
 */
public class SerialDriver implements SerialPortEventListener {

    private static Logger logger = Logger.getLogger(SerialWriter.class);
    private boolean connectionState = false;
    public String portArray[] = null;
    public SerialPort serialPort;
    public InputStream input;
    public OutputStream output;
    private boolean CANCELLED = false;
    private static byte[] lineBuffer = new byte[1024];
    private static int lineIdx = 0;
    public String debugFileBuffer = "";
    public byte[] debugBuffer = new byte[1024];
    public ArrayList<String> lastRes = new ArrayList();
    public double offsetPointer = 0;

    /**
     * private constructor since this is a singleton
     */
    private SerialDriver() {
    }

    public static SerialDriver getInstance() {
        return SerialDriver.SerialDriverHolder.INSTANCE;
    }

    public void write(String str) {
        try {
            serialPort.writeBytes(str.getBytes());
            //this.output.write(str.getBytes());
            logger.info("Wrote Line: " + str);
        } catch (Exception ex) {
            logger.error("Error in SerialDriver Write");
            logger.error("\t" + ex.getMessage());
        }
    }

    public void priorityWrite(String str) throws Exception {
        serialPort.writeBytes(str.getBytes());
        //this.output.write(str.getBytes());
    }

    public void priorityWrite(Byte b) throws Exception {
        logger.debug("[*] Priority Write Sent\n");
        serialPort.writeByte(b);
        //this.output.write(b);
    }

    public synchronized void disconnect() throws SerialPortException {
        if (serialPort != null && serialPort.isOpened()) {
            serialPort.closePort();
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
    public void serialEvent(SerialPortEvent event) {
        byte[] inbuffer = new byte[1024];
        int bytesToRead;
        byte[] tmpBuffer = null;

        bytesToRead = event.getEventValue();
        //tmpBuffer = serialPort.readBytes(bytesToRead);

        if (event.isRXCHAR()) {
            try {
                //            int bytesToRead = input.read(inbuffer, 0, inbuffer.length);
                tmpBuffer = serialPort.readBytes(bytesToRead, 10);
            } catch (    SerialPortException | SerialPortTimeoutException ex) {
                java.util.logging.Logger.getLogger(SerialDriver.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            for (int i = 0; i < bytesToRead; i++) {
                if (tmpBuffer[i] == 0x11 || tmpBuffer[i] == 0x13) {  //We have to filter our XON or XOFF charaters from JSON
                    continue;
                }
                if (tmpBuffer[i] == 0xA) { // inbuffer[i] is a \n
                    String f = new String(lineBuffer, 0, lineIdx);
                    if (!f.equals("")) { //Do not add "" to the jsonQueue..
                        TinygDriver.getInstance().appendJsonQueue(f);
                    }
                    lineIdx = 0;
                } else {
                    lineBuffer[lineIdx++] = tmpBuffer[i];
                }
            }

        }
    }





public static String[] listSerialPorts() {
        String[] ports = jssc.SerialPortList.getPortNames();
        ArrayList portList = new ArrayList();

        for (String port : ports) {
//            CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
            SerialPort _tmpPort = new SerialPort(port);
            if (!_tmpPort.getPortName().contains("Bluetooth")) {

            }

//            if (UtilityFunctions.getOperatingSystem().equals("mac")) {
//                if (_tmpPort.getPortName().contains("tty")) {
//                    continue; //We want to remove the the duplicate tty's and just provide the "cu" ports in the drop down.
//                }
//            }

            portList.add(_tmpPort.getPortName());  //Go ahead and add the ports that made it though the logic above
        }

        String portArray[] = (String[]) portList.toArray(new String[0]);
        return portArray;
    }

    public boolean initialize(String port, int DATA_RATE) throws SerialPortException {

        int TIME_OUT = 2000;

        if (isConnected()) {
            String returnMsg = "[*] Port Already Connected.\n";
            logger.info(returnMsg);
            return (true);
        }

//            CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(port);
        // Get the port's ownership
//            serialPort = (SerialPort) portId("TG", TIME_OUT);
        // set port parameters
        serialPort = new SerialPort(port);
        serialPort.openPort();
        serialPort.setParams(DATA_RATE,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        // open the streams
        //input = serialPort.getInputBufferBytesCount;
        //output = serialPort.getOutputStream();
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
        serialPort.setRTS(true);

        // add event listeners
        serialPort.addEventListener(this);
        //            serialPort.addEventListener(this);notifyOnDataAvailable(true);
        
        logger.debug("[+]Opened " + port + " successfully.");
        setConnected(true); //Register that this is connectionState.

        return true;

//        } catch (PortInUseException ex) {
//            logger.error("[*] Port In Use Error: " + ex.getMessage());
//            return false;
//        } catch (NoSuchPortException ex) {
//            logger.error("[*] No Such Port Error: " + ex.getMessage());
//            return false;
//        } catch (Exception ex) {
//            logger.error("[*] " + ex.getMessage());
//            return false;
//        }
    

}

    /**
     * usual IBM-approved singleton helper class.
     */
    private static class SerialDriverHolder {

    private static final SerialDriver INSTANCE = new SerialDriver();
}
}
