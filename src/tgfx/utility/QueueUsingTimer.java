/*
 * Copyright (C) 2014 Synthetos LLC. All Rights reserved.
 * http://www.synthetos.com
 */
package tgfx.utility;

import org.apache.log4j.Logger;

/**
 * The
 * <code>QueueUsingTimer</code> class is a class that sets a timer and when the
 * timer expires, adds an entry to a queue
 *
 * @see AsyncTimer
 * @author pfarrell Created on Jan 27, 2014 12:36:51 AM
 */
public class QueueUsingTimer<T> extends Thread {

    /**
     * logger instance
     */
    private static final Logger aLog = Logger.getLogger(QueueUsingTimer.class);
    private final QueuedTimerable<T> callback;
    private final long naptime;
    private boolean report_timeout = true;
    private final T makeEntryOf;

    /**
     * construct an QueueUsingTimer
     *
     * @param nap milliseconds to nap
     * @param cb a QueuedTimerable for callback access to semaphore
     */
    public QueueUsingTimer(long nap, QueuedTimerable<T> cb, T entry) {
        callback = cb;
        naptime = nap;
        makeEntryOf = entry;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(naptime);
            if (report_timeout) {
                callback.addToQueue(makeEntryOf);
            }
        } catch (InterruptedException ex) {
            aLog.error("sleep interupted", ex);
        }
    }

    public void disarm() {
        report_timeout = false;
    }
}
