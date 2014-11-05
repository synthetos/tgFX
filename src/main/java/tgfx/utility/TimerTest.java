/*
 * Copyright (C) 2013 Synthetos LLC. All Rights reserved.
 * http://www.synthetos.com
 */
package tgfx.utility;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The <class>TimerTest</code> class is a simple test driver for AsyncTimer
 * It should be a JUnit test, but that server is down right now.
 * @author pfarrell
 */
public class TimerTest implements Timeable {

private AtomicBoolean timerKicked = new AtomicBoolean(false);    
    
public TimerTest() {
    AsyncTimer at = new AsyncTimer(5000, this);
    at.start();
}

    @Override
    public AtomicBoolean getTimeSemaphore() {
        return timerKicked;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TimerTest tt = new TimerTest();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000*1000; i++) {
            if (tt.timerKicked.get()) {
                System.out.println("timer kicked off " + i);
                break;
            }
            if (i % 50 == 1) {
                System.out.print(".");
            }
            if (i % 1000 == 1) {
                System.out.println(i);
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(TimerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        long stop = System.currentTimeMillis();
        System.out.printf(" delta = %d kicked: %b\n", stop-start, tt.getTimeSemaphore().get());
    }
}
