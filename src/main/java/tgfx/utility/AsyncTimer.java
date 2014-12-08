/*
 * Copyright (C) 2014 Synthetos LLC. All Rights reserved.
 * http://www.synthetos.com
 */

package tgfx.utility;

import org.apache.log4j.Logger;
/**
 * The <code>AsyncTimer</code> class implements a timer that will fire off asynchronously
 * and set the callback's semaphore variable.
 * @see Timeable
 * @see AtomicBoolean
 * @author pfarrell
 * Created on Jan 24, 2014 4:23:40 PM
 */
public class AsyncTimer extends Thread {
    /** logger instance */
    private static final Logger aLog = Logger.getLogger(AsyncTimer.class);
    private final Timeable callback;
    private final long naptime;
    private final boolean more;
    
   /**
    * construct an AsyncTimer
    * @param nap milliseconds to nap
    * @param cb a Timeable for callback access to semaphore
    */ 
    public AsyncTimer(long nap, Timeable cb) {
        this(nap, cb, false);
    }
   /**
    * construct an AsyncTimer
    * @param nap milliseconds to nap
    * @param cb a Timeable for callback access to semaphore
    * @param moreflag if true, loop after each nap, if false, once only code
    */ 
    public AsyncTimer(long nap, Timeable cb, boolean moreflag) {
        callback = cb;        
        naptime = nap;
        more = moreflag;
    }
    @Override
    public void run() {
        do {
            try {
                Thread.sleep(naptime);
                callback.getTimeSemaphore().set(true);
            } catch (InterruptedException ex) {
                aLog.error("sleep interupted", ex);
            }
            if (more) {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException ex) {
                    aLog.fatal("more sleep interupted", ex);
                }
            }
        } while (more);
    }
}
