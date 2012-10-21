/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import java.util.Observable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import tgfx.tinyg.TinygDriver;

/**
 *
 * @author ril3y
 */
public class SerialWriter implements Runnable {

 //   public ReentrantLock lock = new ReentrantLock();
    private BlockingQueue queue;
    private int val = 0;
    private boolean RUN = true;
    private String tmpCmd;
    private int pba = 24;
    private int lines_sent_before_update = 0;
    private SerialDriver ser = SerialDriver.getInstance();
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

    public void resetLinesSentBeforeUpdate() {
        lines_sent_before_update = 0;
    }

    public int getIncrementLinesSentBeforeUpdate() {
        return (lines_sent_before_update);
    }

    public void incrementLinesSentBeforeUpdate() {
        lines_sent_before_update = lines_sent_before_update + 1;
    }

    public void addCommandToBuffer(String cmd) {
        this.queue.add(cmd);
    }

    public void setPbaSize(int sze) {
        pba = sze;
    }

    public int getPbaSize() {
        return (pba);
    }

    public float getFeeBufferPercentage() {
        float p = (pba / 24) * 100;
        return (p);
    }

    public void emptyPBA() {
        //This will empty the queue
        //This is called when the tinyG board is reset.
        queue.removeAll(queue);
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
                    ser.write(tmpCmd);
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
