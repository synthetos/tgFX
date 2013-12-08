/*
 * Copyright (C) 2013 Synthetos LLC. All Rights reserved.
 * see license.txt for terms.
 */

package tgfx.system;

import java.util.List;
import org.apache.log4j.Logger;

/**
 * The <code> CncMachine</code> class unique features of a <code>Machine</code>.
 * It is typically created in the MachineFactory
 * @see MachineFactory
 * @see AbstractMachine
 * @see Machine
 * @author pfarrell
 * Created on Dec 7, 2013 7:50:23 PM
 */
public class CncMachine extends AbstractMachine implements Machine {
    /** logger instance */
    private static final Logger aLog = Logger.getLogger(CncMachine.class);
    private String name = "cool CncMachineName";
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String machineName) {
        name = machineName;
    }
    
    @Override
final void initialize() {
    List<Axis> axis = getAxis();
    axis.add(getX());
    axis.add(getY());
    axis.add(getZ());
    axis.add(getA());
    axis.add(getB());
    axis.add(getC());

    setMotionMode(0);
    getXjoggingIncrement().bind(getX().getTravelMaxSimple());
    getYjoggingIncrement().bind(getY().getTravelMaxSimple());
    getZjoggingIncrement().bind(getZ().getTravelMaxSimple());
}

}
