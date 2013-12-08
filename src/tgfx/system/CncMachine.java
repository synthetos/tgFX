/*
 * Copyright (C) 2013 Synthetos LLC. All Rights reserved.
 * see license.txt for terms.
 */

package tgfx.system;

import org.apache.log4j.Logger;
/**
 * The <code> CncMachine</code> class implements a concrete implementation of a Machine
 * @see AbstractMachine
 * @see Machine
 * @author pfarrell
 * Created on Dec 7, 2013 8:26:47 PM
 */
public class CncMachine extends AbstractMachine implements Machine {
    /** logger instance */
    private static final Logger aLog = Logger.getLogger(CncMachine.class);
    private String name = "cool CncMachineName";
    
    @Override
    public String getMachineName() {
        return name;
    }

    @Override
    public void setMachineName(String machineName) {
        name = machineName;
    }
}
