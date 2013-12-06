/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import tgfx.tinyg.TinygDriver;
import tgfx.ui.gcode.GcodeTabController;

/**
 *
 * @author ril3y
 */
public class SerialWriter implements Runnable {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SerialWriter.class);
    private BlockingQueue queue;
    private boolean RUN = true;
    private boolean cleared  = false;
    private String tmpCmd;
    private int BUFFER_SIZE = 180;
    public AtomicInteger buffer_available = new AtomicInteger(BUFFER_SIZE);
    private SerialDriver ser = SerialDriver.getInstance();
    private static final Object mutex = new Object();
    private static boolean throttled = false;

    //   public Condition clearToSend = lock.newCondition();
    public SerialWriter(BlockingQueue q) {
        this.queue = q;
        
        //Setup Logging for SerialWriter
        if(Main.LOGLEVEL.equals("INFO")){
            logger.setLevel(org.apache.log4j.Level.INFO);
        }else if( Main.LOGLEVEL.equals("ERROR")){
            logger.setLevel(org.apache.log4j.Level.ERROR);
        }else{
            logger.setLevel(org.apache.log4j.Level.OFF);
        }
    }

    public void resetBuffer() {
        //Called onDisconnectActions
        buffer_available.set(BUFFER_SIZE);
        notifyAck();  
    }

 
   public void clearQueueBuffer() {
        queue.clear();
        this.cleared = true; // We set this to tell teh mutex with waiting for an ack to send a line that it should not send a line.. we were asked to be cleared.
        try {
            //This is done in resetBuffer is this needed?
            buffer_available.set(BUFFER_SIZE);
            this.setThrottled(false);
            this.notifyAck();
            
            
          
        } catch (Exception ex) {
            Logger.getLogger(SerialWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isRUN() {
        return RUN;
    }

    public void setRun(boolean RUN) {
        this.RUN = RUN;
    }

    public synchronized int getBufferValue() {
        return buffer_available.get();
    }

    public synchronized void setBuffer(int val) {
        buffer_available.set(val);
        logger.info("Got a BUFFER Response.. reset it to: " + val);
    }

    public synchronized void addBytesReturnedToBuffer(int lenBytesReturned) {
        buffer_available.set(getBufferValue() + lenBytesReturned);
        logger.info("Returned " + lenBytesReturned + " to buffer.  Buffer is now at " + buffer_available + "\n");
    }

    public void addCommandToBuffer(String cmd) {
        this.queue.add(cmd);
    }

    public boolean setThrottled(boolean t) {

        synchronized (mutex) {
            if (t == throttled) {
                logger.debug("Throttled already set");
                return false;
            }
            logger.debug("Setting Throttled " + t);
            throttled = t;
        }
        return true;
    }

    public void notifyAck() {
        //This is called by the response parser when an ack packet is recvd.  This
        //Will wake up the mutex that is sleeping in the write method of the serialWriter
        //(this) class.
        synchronized (mutex) {
            logger.debug("Notifying the SerialWriter we have recvd an ACK");
            mutex.notify();
        }
    }

    private void sendUiMessage(String str) {
        //Used to send messages to the console on the GUI
        String gcodeComment = "";
        int startComment = str.indexOf("(");
        int endComment = str.indexOf(")");
        for (int i = startComment; i <= endComment; i++) {
            gcodeComment += str.charAt(i);
        }
        Main.postConsoleMessage(" Gcode Comment << " + gcodeComment);
    }

    public void write(String str) {
        try {
            synchronized (mutex) {
                if (str.length() > getBufferValue()) {
                    setThrottled(true);
                } else {
                    this.setBuffer(getBufferValue() - str.length());
                }

                while (throttled) {
                    if (str.length() > getBufferValue()) {
                        logger.debug("Throttling: Line Length: " + str.length() + " is smaller than buffer length: " + buffer_available);
                        setThrottled(true);
                    } else {
                        setThrottled(false);
                        buffer_available.set(getBufferValue() - str.length());
                        break;
                    }
                    logger.debug("We are Throttled in the write method for SerialWriter");
                    //We wait here until the an ack comes in to the response parser
                    // frees up some buffer space.  Then we unlock the mutex and write the next line.
                    mutex.wait();
                    if(cleared){
                       //clear out the line we were waiting to send.. we were asked to clear our buffer
                        //includeing this line that is waiting to be sent.
                        cleared = false;  //Reset this flag now...
                        return;
                    }
                    logger.debug("We are free from Throttled!");
                }
            }
            if (str.contains("(")) {
                //Gcode Comment Push it back to the UI
                sendUiMessage(str);
            }

            ser.write(str);
            if(!Main.LOGLEVEL.equals("OFF")){
                Main.print("+" + str);
            }
            
            
        } catch (Exception ex) {
            logger.error("Error in SerialDriver Write");
        }
    }

    @Override
    public void run() {
        Main.print("[+]Serial Writer Thread Running...");
        while (RUN) {
            try {
                tmpCmd = (String) queue.take();  //Grab the line
                if(tmpCmd.equals("**FILEDONE**")){
                    //Our end of file sending token has been detected.
                    //We will not enable jogging by setting isSendingFile to false
                    GcodeTabController.setIsFileSending(false);
                }else if(tmpCmd.startsWith("Comment:")){
                    //Display current gcode comment
                    GcodeTabController.setGcodeTextTemp("Comment: " + tmpCmd);
                    continue;
                }
                this.write(tmpCmd);
            } catch (Exception ex) {
                Main.print("[!]Exception in SerialWriter Thread");
            }
        }
        Main.print("[+]SerialWriter thread exiting...");
    }
}