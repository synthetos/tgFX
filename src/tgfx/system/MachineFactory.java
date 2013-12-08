/*
 * Copyright (C) 2013 Synthetos LLC. All Rights reserved.
 */

package tgfx.system;

import java.util.concurrent.locks.ReadWriteLock;
/**
 * The <code> MachineFactory</code> class creates Machines for us. It completely
 * hides the fact that the Machine is really one instance.
 * @author pfarrell
 * Created on Dec 6, 2013 4:23:11 PM
 */
public class MachineFactory {
    private static Machine theMachine;
    private static ReadWriteLock lock;
/**
 * gets a machine.
 * @return a nice machine
 */    
    public static Machine getMachine() {
        Machine rval = null;
        lock.writeLock().lock();
        try {
            if (theMachine == null) {
                theMachine = new CncMachine();
            }
            rval = theMachine;
        } finally {
            lock.writeLock().unlock();
        }
        return rval;
    }
}
