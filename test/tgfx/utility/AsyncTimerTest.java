/*
 * Copyright (C) 2014 Synthetos LLC. All Rights reserved.
 * http://www.synthetos.com
 */
package tgfx.utility;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * junit tets for AsyncTimer
 * @author pfarrell
 */
public class AsyncTimerTest {
    
    public AsyncTimerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of run method, of class AsyncTimer.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        TimableTester tt = new TimableTester();
        AsyncTimer instance = new AsyncTimer(5000, tt);
        instance.start();
        long start = System.currentTimeMillis();
        double sum = 0.0;
        
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
            // do something that takes come computing
            sum += Math.pow(sum, i ) + 3.1415926;
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(TimerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        long stop = System.currentTimeMillis();
        System.out.printf(" delta = %d kicked: %b\nSum: %f\n", stop-start, tt.getTimeSemaphore().get(), sum);
        
    }
    class TimableTester implements Timeable {
        private AtomicBoolean timerKicked = new AtomicBoolean(false);  
        @Override
        public AtomicBoolean getTimeSemaphore() {
            return timerKicked;
        }
    }
}