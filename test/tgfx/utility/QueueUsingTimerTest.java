/*
 * Copyright (C) 2014 Synthetos LLC. All Rights reserved.
 * http://www.synthetos.com
 */
package tgfx.utility;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Junit test for QueueUsingTimer
 * @author pfarrell
 */
public class QueueUsingTimerTest implements QueuedTimerable<String> {
    static ArrayBlockingQueue<String> theQueue;
    private static final String SPECIAL_ENTRY = "**TIMER**";
    public QueueUsingTimerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        theQueue = new ArrayBlockingQueue<>(100);
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
     * Test of run method, of class QueueUsingTimer.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        
        QueueUsingTimer<String> instance = new QueueUsingTimer<>(5000, this, SPECIAL_ENTRY);
        instance.start();
        long start = System.currentTimeMillis();
        double sum = 0.0;
        
        for (int i = 0; i < 1000*1000; i++) {
            if ( !theQueue.isEmpty()) {
                try {
                    String top = theQueue.take();
                    if (top.equals(SPECIAL_ENTRY)) {
                        System.out.println("timer kicked off " + i);
                        break;
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(QueueUsingTimerTest.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                }
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
        System.out.printf(" delta = %d Sum: %f\n", stop-start, sum);
        
    }

    @Override
    public void addToQueue(String t) {
        theQueue.add(t);
    }
    
    
}