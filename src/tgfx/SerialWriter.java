/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import java.util.concurrent.BlockingQueue;

/**
 *
 * @author ril3y
 */
public class SerialWriter implements Runnable {

    private BlockingQueue queue;
    private boolean RUN = true;
    private String tmpCmd;
    private int buffer_available = 254;
    private SerialDriver ser = SerialDriver.getInstance();
    private static final Object mutex = new Object();
    private static boolean throttled = false;

    //   public Condition clearToSend = lock.newCondition();
    public SerialWriter(BlockingQueue q) {
        this.queue = q;
    }

    public boolean isRUN() {
        return RUN;
    }

    public void setRun(boolean RUN) {
        this.RUN = RUN;
    }

    public synchronized int getBufferValue() {
        return buffer_available;
    }

    public synchronized void addBytesReturnedToBuffer(int lenBytesReturned) {
        buffer_available = buffer_available + lenBytesReturned;
//        Main.logger.info("Returned " + lenBytesReturned + " to buffer.  Buffer is now at " + buffer_available + "\n");
    }

    public void addCommandToBuffer(String cmd) {
        this.queue.add(cmd);
    }

    public boolean setThrottled(boolean t) {
        
        synchronized (mutex) {
            if (t == throttled) {
                Main.logger.info("Throttled already set");
                return false;
            }
            Main.logger.info("Setting Throttled " + t);
            throttled = t;
//            if (!throttled) {
//                mutex.notify();
//            }
        }
        return true;
    }

    public void notifyAck() {
        //This is called by the response parser when an ack packet is recvd.  This
        //Will wake up the mutex that is sleeping in the write method of the serialWriter
        //(this) class.
        synchronized (mutex) {
            Main.logger.info("Notifying the SerialWriter we have recvd an ACK");
            mutex.notify();
        }
    }

    public void write(String str) {
        try {
            synchronized (mutex) {
                if (str.length() > getBufferValue()) {
                    setThrottled(true);
                } else {
                   buffer_available = getBufferValue() - str.length();
                }

                while (throttled) {
                    if (str.length() > getBufferValue()) {
                        Main.logger.info("Throttling: Line Length: " + str.length() + " is smaller than buffer length: " + buffer_available);
                        setThrottled(true);
                    } else {
                        setThrottled(false);
                        buffer_available = getBufferValue() - str.length();
                        break;
                    }
                    Main.logger.info("We are Throttled in the write method for SerialWriter");
                    //We wait here until the an ack comes in to the response parser
                    // frees up some buffer space.  Then we unlock the mutex and write the next line.
                    mutex.wait();
                    Main.logger.info("We are free from Throttled!");
                }
            }
            ser.write(str);
        } catch (Exception ex) {
            Main.logger.error("Error in SerialDriver Write");
        }
    }

    @Override
    public void run() {
        System.out.println("[+]Serial Writer Thread Running...");
        while (RUN == true) {
            try {
                tmpCmd = (String) queue.take();  //Grab the byte[] on the top of the stack (queue)

//                Main.logger.debug("Locking...");
//                Main.logger.debug("[+]Took msg from serialWriter queue");
//                lock.lock();
//                if (getPbaSize() <= 2 || getIncrementLinesSentBeforeUpdate() <= 24) {
                //Write the line to TinyG


                this.write(tmpCmd);

//                    incrementLinesSentBeforeUpdate();
//                    Main.logger.info("[+]PBA is: " + getPbaSize());
//                    lock.unlock();
//                    Main.logger.debug("Un-Locking...");
                //               } else {
                //                   while (getIncrementLinesSentBeforeUpdate() !=0 && getPbaSize() > 5) {
//                        System.out.println(getPbaSize());
                //Main.logger.debug("[+] Not Enough room in PBA or too many lines sent with a response... Waiting");
                //We use the size of 5 to let the buffer clear up a bit before we shove it back in.
//                        Main.logger.debug("Unlocking... Waiting for room in PBA... PBA is: " + getPbaSize());
//                        clearToSend.await();
//                        

//                    }
//                   ser.write(tmpCmd);
//                    incrementLinesSentBeforeUpdate();
//                    lock.unlock();
//                    Main.logger.debug("Un-Locking...");

                //               }
            } catch (Exception ex) {
                System.out.println("[!]Exception in SerialWriter Thread");


            }
        }
        System.out.println("[+]SerialWriter thread exiting...");
    }
}
