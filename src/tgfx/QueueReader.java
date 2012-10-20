/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import java.util.concurrent.BlockingQueue;

import java.util.Observable;
import java.util.concurrent.BlockingQueue;
import tgfx.tinyg.TinygDriver;

/**
 *
 * @author ril3y
 */
public class QueueReader extends Observable implements Runnable {

    private BlockingQueue queue;
    private BlockingQueue jsonQueue;
    private int val = 0;
    private boolean RUN = true;
    private byte[] tmp;

//    responseParser parser = new responseParser();
    public QueueReader(BlockingQueue q, BlockingQueue jq) {
        this.queue = q;
        this.jsonQueue = jq;
    }

    public boolean isRUN() {
        return RUN;
    }

    public void setRun(boolean RUN) {
        this.RUN = RUN;
    }
    private static final String commandReturn = "{\"r\":{\"bd\":{\"";

    public void emptyQueue(){
        //This will empty the queue
        //This is called when the tinyG board is reset.
        queue.removeAll(queue);
        jsonQueue.removeAll(jsonQueue);
    }
    
    @Override
    public void run() {
        //String lineBuffer = ""; //This is a buffer to store half json lines when the serial driver sent a line without ending with a /n
        String json = "";
        System.out.println("[+]Queue Reader Thread Running...");
        //json = lineBuffer;
        while (RUN == true) {

            try {
                //System.out.println("\t[QUEUE-READER]: #Elements: " + queue.size());
                tmp = (byte[]) queue.take();  //Grab the byte[] on the top of the stack (queue)
                if (tmp.length > 0) {

                    for (int i = 0; i < tmp.length; i++) {

                        if (tmp[i] == 10) { //New Line Decimal 
                            if (json.equals("")) {
                                continue;
                            } else {
                                TinygDriver.getInstance().resParse.appendJsonQueue(json);
                                json = ""; //
                            }
                        } else {
                            json = json + (String.valueOf((char) tmp[i]));
                        }
//                                //Flow.logger.debug("\t QueueReader... {sr} Processed... " + json);
//                                //Flow.logger.debug("\tResponse Queue Buffer Size: " + String.valueOf(queue.size()));
//
//                                if (json.length() > commandReturn.length()) {
//                                    int differences = 0;
//                                    for (int spot = 0; spot < commandReturn.length(); ++spot) {
//                                        if (json.charAt(spot) != commandReturn.charAt(spot)) {
//                                            ++differences;
//                                        }
//                                    }
//                                    if (differences <= 2) {
//
//                                        int commandSize = TinygDriver.getInstance().commandComplete(json);
////                                        TinygDriver.getInstance().resParse.appendJsonQueue(json);
//                                        //Flow.logger.debug("\t QueueReader Processed this JSON: " + json);
//                                        Main.logger.debug("Popped " + commandSize + " approximately " + TinygDriver.getInstance().approximateFreespace() + " bytes free " + differences + " differences from " + commandReturn);
////                                    	                                	}
//                                    } else {
//                                        Main.logger.debug(json + "[[" + differences + "]]");
//                                    }
//                                }
//                                TinygDriver.getInstance().resParse.appendJsonQueue(json);
//                                json = "";
//
//                            }
//                        } else {
//                            json = json + (String.valueOf((char) tmp[i]));
//
//                        }
//                    }


                    }
                }
            } catch (Exception ex) {
                System.out.println("EXP");
            }
        }
        System.out.println("[+]QueueReader thread exiting...");
    }
}
