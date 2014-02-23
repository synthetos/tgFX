/*
 * Copyright (C) 2014 Synthetos LLC. All Rights reserved.
 * http://www.synthetos.com
 */

package tgfx.render;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.apache.log4j.Logger;
import tgfx.tinyg.TinygDriver;
/**
 * The <code>MachineChangeListener</code> class implements a GUI change listener
 * based on code that used to live in the CNCMachine class's constructor
 * @see CNCMachine
 * @author pfarrell
 * Created on Feb 22, 2014 9:26:00 PM
 */
public class MachineChangeListener implements ChangeListener {
    /** logger instance */
    private static final Logger aLog = Logger.getLogger(MachineChangeListener.class);
    private CNCMachine theMachine;
    public MachineChangeListener(CNCMachine mach) {
        theMachine = mach;
    }
    @Override
    public void changed(ObservableValue ov, Object t, Object t1) {
        if (TinygDriver.getInstance().machine.getAxisByName("y").getMachinePosition() > theMachine.heightProperty().get()
              || TinygDriver.getInstance().machine.getAxisByName("x").getMachinePosition() > theMachine.widthProperty().get()) {
            theMachine.hideOrShowCursor(false);
        } else {
            theMachine.hideOrShowCursor(true);
        }
    }
}
