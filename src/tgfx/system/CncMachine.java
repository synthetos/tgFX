/*
 * Copyright (C) 2013 Synthetos LLC. All Rights reserved.
 */

package tgfx.system;

import org.apache.log4j.Logger;
/**
 * The <code> CncMachine</code> class unique features of a <code>Machine</code>.
 * It is typically created in the MachineFactory
 * @see MachineFactory
 * @author pfarrell
 * Created on Dec 7, 2013 7:50:23 PM
 */
public class CncMachine extends AbstractMachine implements Machine {
    /** logger instance */
    private static final Logger aLog = Logger.getLogger(CncMachine.class);

    @Override
    public String getName() {
        return ("CoolMachineName, Not supported yet.");
    }
}
