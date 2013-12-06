/*
 * Copyright (c) 2013 Synthetos LLC
 * Rileyporter@gmail.com
 * www.synthetos.com
 */
package tgfx.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import tgfx.tinyg.MnemonicManager;
import tgfx.tinyg.TinygDriver;
import tgfx.tinyg.responseCommand;

/**
 *
 * @author ril3y
 */
public final class Machine {

    /**
     * @return the logger
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * @param aLogger the logger to set
     */
    public static void setLogger(Logger aLogger) {
        logger = aLogger;
    }

    /**
     * @return the motion_mode
     */
    public static motion_modes getMotion_mode() {
        return motion_mode;
    }

    /**
     * @param aMotion_mode the motion_mode to set
     */
    public static void setMotion_mode(motion_modes aMotion_mode) {
        motion_mode = aMotion_mode;
    }

    /**
     * @return the machine_state
     */
    public static machine_states getMachine_state() {
        return machine_state;
    }

    /**
     * @param aMachine_state the machine_state to set
     */
    public static void setMachine_state(machine_states aMachine_state) {
        machine_state = aMachine_state;
    }

    //TG Specific
    //Machine EEPROM Values
    //binding
    private SimpleDoubleProperty longestTravelAxisValue = new SimpleDoubleProperty();
    private SimpleIntegerProperty xjoggingIncrement = new SimpleIntegerProperty();
    private SimpleIntegerProperty yjoggingIncrement = new SimpleIntegerProperty();
    private SimpleIntegerProperty zjoggingIncrement = new SimpleIntegerProperty();
    private SimpleIntegerProperty ajoggingIncrement = new SimpleIntegerProperty();
    private SimpleStringProperty m_state = new SimpleStringProperty();
    private SimpleStringProperty m_mode = new SimpleStringProperty();
    private SimpleDoubleProperty firmwareBuild = new SimpleDoubleProperty();
    private StringProperty firmwareVersion = new SimpleStringProperty();
    private SimpleDoubleProperty hardwarePlatform = new SimpleDoubleProperty(0);
    private StringProperty hardwareId = new SimpleStringProperty("na");
    private StringProperty hardwareVersion = new SimpleStringProperty("na");
    private SimpleDoubleProperty velocity = new SimpleDoubleProperty();
    private StringProperty gcodeUnitMode = new SimpleStringProperty("mm");
    private SimpleDoubleProperty gcodeUnitDivision = new SimpleDoubleProperty(1);
//    private SimpleStringProperty gcodeDistanceMode = new SimpleStringProperty();
    private int switchType = 0; //0=normally closed 1 = normally open
    private int status_report_interval;
//    public Gcode_unit_modes gcode_startup_units;
    private Gcode_select_plane gcode_select_plane;
    private Gcode_coord_system gcode_select_coord_system;
    private Gcode_path_control gcode_path_control;
    private Gcode_distance_mode gcode_distance_mode = Gcode_distance_mode.ABSOLUTE;
    private boolean enable_acceleration;
    private float junction_acceleration;
    private float min_line_segment;
    private static Logger logger = Logger.getLogger(TinygDriver.class);
    private float min_arc_segment;
    private double min_segment_time;
    private Ignore_CR_LF_ON_RX ignore_cr_lf_RX;
    private boolean enable_CR_on_TX;
    private boolean enable_echo;
    private boolean enable_xon_xoff;
    private boolean enable_hashcode;
    //Misc
    private SimpleIntegerProperty lineNumber = new SimpleIntegerProperty(0);
    private String last_message = new String("");
//    public static motion_modes motion_mode = new SimpleIntegerProperty();
    private static motion_modes motion_mode;
    private List<Motor> motors = new ArrayList<>();
    private List<Axis> axis = new ArrayList<>();
    private List<GcodeCoordinateSystem> gcodeCoordinateSystems = new ArrayList<>();
    private Axis x = new Axis(Axis.AXIS.X, Axis.AXIS_TYPE.LINEAR, Axis.AXIS_MODES.STANDARD);
    private Axis y = new Axis(Axis.AXIS.Y, Axis.AXIS_TYPE.LINEAR, Axis.AXIS_MODES.STANDARD);
    private Axis z = new Axis(Axis.AXIS.Z, Axis.AXIS_TYPE.LINEAR, Axis.AXIS_MODES.STANDARD);
    private Axis a = new Axis(Axis.AXIS.A, Axis.AXIS_TYPE.ROTATIONAL, Axis.AXIS_MODES.STANDARD);
    private Axis b = new Axis(Axis.AXIS.B, Axis.AXIS_TYPE.ROTATIONAL, Axis.AXIS_MODES.STANDARD);
    private Axis c = new Axis(Axis.AXIS.C, Axis.AXIS_TYPE.ROTATIONAL, Axis.AXIS_MODES.STANDARD);
    private Motor Motor1 = new Motor(1);
    private Motor Motor2 = new Motor(2);
    private Motor Motor3 = new Motor(3);
    private Motor Motor4 = new Motor(4);
    private GcodeCoordinateManager gcm = new GcodeCoordinateManager();

    /**
     * @return the longestTravelAxisValue
     */
    public SimpleDoubleProperty getLongestTravelAxisValue() {
        return longestTravelAxisValue;
    }

    /**
     * @param longestTravelAxisValue the longestTravelAxisValue to set
     */
    public void setLongestTravelAxisValue(SimpleDoubleProperty longestTravelAxisValue) {
        this.longestTravelAxisValue = longestTravelAxisValue;
    }

    /**
     * @return the xjoggingIncrement
     */
    public SimpleIntegerProperty getXjoggingIncrement() {
        return xjoggingIncrement;
    }

    /**
     * @param xjoggingIncrement the xjoggingIncrement to set
     */
    public void setXjoggingIncrement(SimpleIntegerProperty xjoggingIncrement) {
        this.xjoggingIncrement = xjoggingIncrement;
    }

    /**
     * @return the yjoggingIncrement
     */
    public SimpleIntegerProperty getYjoggingIncrement() {
        return yjoggingIncrement;
    }

    /**
     * @param yjoggingIncrement the yjoggingIncrement to set
     */
    public void setYjoggingIncrement(SimpleIntegerProperty yjoggingIncrement) {
        this.yjoggingIncrement = yjoggingIncrement;
    }

    /**
     * @return the zjoggingIncrement
     */
    public SimpleIntegerProperty getZjoggingIncrement() {
        return zjoggingIncrement;
    }

    /**
     * @param zjoggingIncrement the zjoggingIncrement to set
     */
    public void setZjoggingIncrement(SimpleIntegerProperty zjoggingIncrement) {
        this.zjoggingIncrement = zjoggingIncrement;
    }

    /**
     * @return the ajoggingIncrement
     */
    public SimpleIntegerProperty getAjoggingIncrement() {
        return ajoggingIncrement;
    }

    /**
     * @param ajoggingIncrement the ajoggingIncrement to set
     */
    public void setAjoggingIncrement(SimpleIntegerProperty ajoggingIncrement) {
        this.ajoggingIncrement = ajoggingIncrement;
    }

    /**
     * @return the m_state
     */
    public SimpleStringProperty getM_state() {
        return m_state;
    }

    /**
     * @param m_state the m_state to set
     */
    public void setM_state(SimpleStringProperty m_state) {
        this.m_state = m_state;
    }

    /**
     * @return the m_mode
     */
    public SimpleStringProperty getM_mode() {
        return m_mode;
    }

    /**
     * @param m_mode the m_mode to set
     */
    public void setM_mode(SimpleStringProperty m_mode) {
        this.m_mode = m_mode;
    }

    /**
     * @param firmwareBuild the firmwareBuild to set
     */
    public void setFirmwareBuild(SimpleDoubleProperty firmwareBuild) {
        this.firmwareBuild = firmwareBuild;
    }

    /**
     * @param firmwareVersion the firmwareVersion to set
     */
    public void setFirmwareVersion(StringProperty firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    /**
     * @return the hardwarePlatform
     */
    public SimpleDoubleProperty getHardwarePlatform() {
        return hardwarePlatform;
    }

    /**
     * @param hardwarePlatform the hardwarePlatform to set
     */
    public void setHardwarePlatform(SimpleDoubleProperty hardwarePlatform) {
        this.hardwarePlatform = hardwarePlatform;
    }

    /**
     * @param hardwareId the hardwareId to set
     */
    public void setHardwareId(StringProperty hardwareId) {
        this.hardwareId = hardwareId;
    }

    /**
     * @param hardwareVersion the hardwareVersion to set
     */
    public void setHardwareVersion(StringProperty hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    /**
     * @param velocity the velocity to set
     */
    public void setVelocity(SimpleDoubleProperty velocity) {
        this.velocity = velocity;
    }

    /**
     * @param gcodeUnitMode the gcodeUnitMode to set
     */
    public void setGcodeUnitMode(StringProperty gcodeUnitMode) {
        this.gcodeUnitMode = gcodeUnitMode;
    }

    /**
     * @return the gcodeUnitDivision
     */
    public SimpleDoubleProperty getGcodeUnitDivision() {
        return gcodeUnitDivision;
    }

    /**
     * @param gcodeUnitDivision the gcodeUnitDivision to set
     */
    public void setGcodeUnitDivision(SimpleDoubleProperty gcodeUnitDivision) {
        this.gcodeUnitDivision = gcodeUnitDivision;
    }

    /**
     * @return the gcode_select_coord_system
     */
    public Gcode_coord_system getGcode_select_coord_system() {
        return gcode_select_coord_system;
    }

    /**
     * @param gcode_select_coord_system the gcode_select_coord_system to set
     */
    public void setGcode_select_coord_system(Gcode_coord_system gcode_select_coord_system) {
        this.gcode_select_coord_system = gcode_select_coord_system;
    }

    /**
     * @param gcode_path_control the gcode_path_control to set
     */
    public void setGcode_path_control(Gcode_path_control gcode_path_control) {
        this.gcode_path_control = gcode_path_control;
    }

    /**
     * @param gcode_distance_mode the gcode_distance_mode to set
     */
    public void setGcode_distance_mode(Gcode_distance_mode gcode_distance_mode) {
        this.gcode_distance_mode = gcode_distance_mode;
    }

    /**
     * @return the ignore_cr_lf_RX
     */
    public Ignore_CR_LF_ON_RX getIgnore_cr_lf_RX() {
        return ignore_cr_lf_RX;
    }

    /**
     * @param ignore_cr_lf_RX the ignore_cr_lf_RX to set
     */
    public void setIgnore_cr_lf_RX(Ignore_CR_LF_ON_RX ignore_cr_lf_RX) {
        this.ignore_cr_lf_RX = ignore_cr_lf_RX;
    }

    /**
     * @param lineNumber the lineNumber to set
     */
    public void setLineNumber(SimpleIntegerProperty lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * @param motors the motors to set
     */
    public void setMotors(List<Motor> motors) {
        this.motors = motors;
    }

    /**
     * @return the axis
     */
    public List<Axis> getAxis() {
        return axis;
    }

    /**
     * @param axis the axis to set
     */
    public void setAxis(List<Axis> axis) {
        this.axis = axis;
    }

    /**
     * @return the gcodeCoordinateSystems
     */
    public List<GcodeCoordinateSystem> getGcodeCoordinateSystems() {
        return gcodeCoordinateSystems;
    }

    /**
     * @param gcodeCoordinateSystems the gcodeCoordinateSystems to set
     */
    public void setGcodeCoordinateSystems(List<GcodeCoordinateSystem> gcodeCoordinateSystems) {
        this.gcodeCoordinateSystems = gcodeCoordinateSystems;
    }

    /**
     * @return the x
     */
    public Axis getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(Axis x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public Axis getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(Axis y) {
        this.y = y;
    }

    /**
     * @return the z
     */
    public Axis getZ() {
        return z;
    }

    /**
     * @param z the z to set
     */
    public void setZ(Axis z) {
        this.z = z;
    }

    /**
     * @return the a
     */
    public Axis getA() {
        return a;
    }

    /**
     * @param a the a to set
     */
    public void setA(Axis a) {
        this.a = a;
    }

    /**
     * @return the b
     */
    public Axis getB() {
        return b;
    }

    /**
     * @param b the b to set
     */
    public void setB(Axis b) {
        this.b = b;
    }

    /**
     * @return the c
     */
    public Axis getC() {
        return c;
    }

    /**
     * @param c the c to set
     */
    public void setC(Axis c) {
        this.c = c;
    }

    /**
     * @return the Motor1
     */
    public Motor getMotor1() {
        return Motor1;
    }

    /**
     * @param Motor1 the Motor1 to set
     */
    public void setMotor1(Motor Motor1) {
        this.Motor1 = Motor1;
    }

    /**
     * @return the Motor2
     */
    public Motor getMotor2() {
        return Motor2;
    }

    /**
     * @param Motor2 the Motor2 to set
     */
    public void setMotor2(Motor Motor2) {
        this.Motor2 = Motor2;
    }

    /**
     * @return the Motor3
     */
    public Motor getMotor3() {
        return Motor3;
    }

    /**
     * @param Motor3 the Motor3 to set
     */
    public void setMotor3(Motor Motor3) {
        this.Motor3 = Motor3;
    }

    /**
     * @return the Motor4
     */
    public Motor getMotor4() {
        return Motor4;
    }

    /**
     * @param Motor4 the Motor4 to set
     */
    public void setMotor4(Motor Motor4) {
        this.Motor4 = Motor4;
    }

    /**
     * @return the gcm
     */
    public GcodeCoordinateManager getGcm() {
        return gcm;
    }

    /**
     * @param gcm the gcm to set
     */
    public void setGcm(GcodeCoordinateManager gcm) {
        this.gcm = gcm;
    }

    /**
     * @param coordinateSystem the coordinateSystem to set
     */
    public void setCoordinateSystem(SimpleStringProperty coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }

    public static enum motion_modes {
//        [momo] motion_mode        - 0=traverse, 1=straight feed, 2=cw arc, 3=ccw arc

        traverse, feed, cw_arc, ccw_arc, cancel
    }

    public static enum coordinate_systems {

        g54, g55, g56, g57, g58, g59
    }

    public void setSwitchType(int swType) {
        this.switchType = swType;
    }

    public int getSwitchType() {
        return (switchType);
    }

    public String getSwitchTypeAsString() {
        if (getSwitchType() == 0) {
            return ("Normally Open");
        } else {
            return ("Normally Closed");
        }
    }

    public Gcode_select_plane getGcode_select_plane() {
        return gcode_select_plane;
    }

    public Gcode_distance_mode getGcode_distance_mode() {
        return gcode_distance_mode;
    }

    public void setGcodeDistanceMode(String gdm) {
        setGcodeDistanceMode(Integer.valueOf(gdm));
    }

    public void setGcodeDistanceMode(int gdm) {

        switch (gdm) {
            case 0:
                this.setGcode_distance_mode(Gcode_distance_mode.ABSOLUTE);
                break;
            case 1:
                this.setGcode_distance_mode(Gcode_distance_mode.INCREMENTAL);
        }

    }

    public void setGcodeSelectPlane(String gsp) {
        setGcodeSelectPlane(Integer.valueOf(gsp));
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public void setGcodeSelectPlane(int gsp) {
        switch (gsp) {
            case 0:
                this.setGcode_select_plane(Gcode_select_plane.XY);
            case 1:
                this.setGcode_select_plane(Gcode_select_plane.XZ);
            case 2:
                this.setGcode_select_plane(Gcode_select_plane.YZ);
        }
    }

    public void setGcode_select_plane(Gcode_select_plane gcode_select_plane) {
        this.gcode_select_plane = gcode_select_plane;
    }
    private SimpleStringProperty coordinateSystem = new SimpleStringProperty();

    public StringProperty getHardwareId() {
        return hardwareId;
    }

//    public SimpleDoubleProperty over(){
//        return(gcodeUnitDivision.divide(2));
//    }
    public void setHardwareId(String hwIdString) {
        getHardwareId().set(hwIdString);
    }

    public StringProperty getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        int h = Integer.valueOf(hardwareVersion);
//        this.hardwareVersion.set(hardwareVersion);
        TinygDriver.getInstance().hardwarePlatformManager.setHardwarePlatformByVersionNumber(h);
    }

//    public static enum motion_modes {
////        [momo] motion_mode        - 0=traverse, 1=straight feed, 2=cw arc, 3=ccw arc
//
//        traverse, straight, cw_arc, ccw_arc, invalid
//    }
    public static enum machine_states {

        reset, cycle, stop, end, run, hold, homing, probe, jog
    }
    private static machine_states machine_state;

    public Ignore_CR_LF_ON_RX getIgnore_cr_lf_TX() {
        return getIgnore_cr_lf_RX();
    }

    public void setIgnore_cr_lf_TX(Ignore_CR_LF_ON_RX ignore_cr_lf_TX) {

        this.setIgnore_cr_lf_RX(ignore_cr_lf_TX);
    }

    public void setIgnore_cr_lf_RX(int ignore_cr_lf_RX) {
        //Send a int
        switch (ignore_cr_lf_RX) {
            case 0:
                this.setIgnore_cr_lf_RX(Ignore_CR_LF_ON_RX.OFF);
                break;
            case 1:
                this.setIgnore_cr_lf_RX(Ignore_CR_LF_ON_RX.CR);
                break;
            case 2:
                this.setIgnore_cr_lf_RX(Ignore_CR_LF_ON_RX.LF);
                break;
        }
    }

    public static enum Ignore_CR_LF_ON_RX {
        //OFF means neither is ignored on RX

        OFF, CR, LF
    }

    public static enum Gcode_unit_modes {
        //gun

        inches, //G20
        mm      //G21
    };

    public static enum Gcode_select_plane {
        //gpl

        XY, //G17
        XZ, //G18
        YZ  //G19
    }

    public static enum Gcode_distance_mode {
        //gdi

        ABSOLUTE, //G90
        INCREMENTAL   //91
    }

    public Gcode_path_control getGcode_path_control() {
        return gcode_path_control;
    }

    public void setGcodePathControl(String gpc) {
        setGcodePathControl(Integer.valueOf(gpc));
    }

    public void setGcodePathControl(int gpc) {
        switch (gpc) {
            case 0:
                this.setGcode_path_control(Gcode_path_control.G61);
                break;
            case 1:
                this.setGcode_path_control(Gcode_path_control.G61POINT1);
                break;
            case 2:
                this.setGcode_path_control(Gcode_path_control.G64);
                break;
        }
    }

    public static enum Gcode_path_control {
        //gpl

        G61,
        G61POINT1,
        G64
    }

    public static enum Gcode_coord_system {
        //gco

        G54, G55, G56, G57, G58, G59
    }

    private enum selection_plane {

        G17, G18, G19
    };

    public boolean isEnable_CR_on_TX() {
        return enable_CR_on_TX;
    }

    public void setEnable_CR_on_TX(boolean enable_CR_on_TX) {
        this.enable_CR_on_TX = enable_CR_on_TX;
    }

    public boolean isEnable_hashcode() {
        return enable_hashcode;
    }

    public void setEnable_hashcode(boolean enable_hashcode) {
        this.enable_hashcode = enable_hashcode;
    }

    public float getJunction_acceleration() {
        return junction_acceleration;
    }

    public void setJunction_acceleration(float junction_acceleration) {
        this.junction_acceleration = junction_acceleration;
    }

    public List<Motor> getMotors() {
        return (this.motors);
    }

    public int getNumberOfMotors() {
        //return how many numbers are in the system
        return (this.getMotors().size());
    }
    //TG Composer Specific
    private String machineName;

    public String getMachineName() {
        return machineName;
    }

    public void setGcodeUnits(int unitMode) {
        if (unitMode == 0) {
            getGcodeUnitMode().setValue("inches");
            getGcodeUnitDivision().set(25.4);  //mm to inches conversion   


        } else if (unitMode == 1) {
            getGcodeUnitMode().setValue("mm");
            getGcodeUnitDivision().set(1.0);

        }

    }
//

    public StringProperty getGcodeUnitMode() {
        return gcodeUnitMode;
    }

    public int getGcodeUnitModeAsInt() {
        if (getGcodeUnitMode().get().equals(Gcode_unit_modes.mm.toString())) {
            return (1);
        } else {
            return (0);
        }
    }
//

    public void setGcodeUnits(String gcu) {
        int _tmpgcu = Integer.valueOf(gcu);

        switch (_tmpgcu) {
            case (0):
                getGcodeUnitMode().set(Gcode_unit_modes.inches.toString());
                break;
            case (1):
                getGcodeUnitMode().set(Gcode_unit_modes.mm.toString());
                break;
        }
    }

//    public void setGcodeUnits(Gcode_unit_modes gcode_units) {
//        this.gcode_unit_mode = gcode_units;
//    }
    public SimpleStringProperty getMotionMode() {
        return (getM_mode());
    }

    public void setMotionMode(int mode) {

        if (mode == 0) {
            getM_mode().set(motion_modes.traverse.toString());
        } else if (mode == 1) {
            getM_mode().set(motion_modes.feed.toString());
        } else if (mode == 2) {
            getM_mode().set(motion_modes.cw_arc.toString());
        } else if (mode == 3) {
            getM_mode().set(motion_modes.ccw_arc.toString());
        } else {
            getM_mode().set(motion_modes.cancel.toString());
        }
    }
//

    public int getStatus_report_interval() {
        return status_report_interval;
    }

    public void setStatus_report_interval(int status_report_interval) {
        this.status_report_interval = status_report_interval;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public boolean isEnable_acceleration() {
        return enable_acceleration;
    }

    public void setEnable_acceleration(boolean enable_acceleration) {
        this.enable_acceleration = enable_acceleration;
    }

    public boolean isEnable_echo() {
        return enable_echo;
    }

    public void setEnable_echo(boolean enable_echo) {
        this.enable_echo = enable_echo;
    }

    public boolean isEnable_xon_xoff() {
        return enable_xon_xoff;
    }

    public void setEnable_xon_xoff(boolean enable_xon_xoff) {
        this.enable_xon_xoff = enable_xon_xoff;
    }

    public double getFirmwareBuildValue() {
        return firmwareBuild.getValue();
    }
    public SimpleDoubleProperty getFirmwareBuild() {
        return firmwareBuild;
    }
    public void setFirmwareBuild(double firmware_build) throws IOException, JSONException {

        this.firmwareBuild.set(firmware_build);
        TinygDriver.getInstance().notifyBuildChanged();
    }

    public StringProperty getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String fv) {
        firmwareVersion.setValue(fv);
    }

    public int getLineNumber() {
        return lineNumber.get();
    }

    public SimpleIntegerProperty getLineNumberSimple() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber.set(lineNumber);
    }

    public SimpleStringProperty getMachineState() {
        return this.getM_state();
    }

//    public void setCoordinateSystem(String cord) {
//        setCoordinate_mode(Integer.valueOf(cord));
//
//    }
    public SimpleStringProperty getCoordinateSystem() {
        return (this.coordinateSystem);
    }

    //    public void setCoordinate_mode(double m) {
    //        int c = (int) (m); //Convert this to a int
    //        setCoordinate_mode(c);
    //    }
    //    public int getCoordinateSystemOrd() {
    //        coordinate_systems[] cs = coordinate_systems.values();
    //        return 1;
    //    }
    //
    //    public void setCoordinate_mode(int c) {
    //        switch (c) {
    //            case 1:
    //                coordinateSystem.set(coordinate_systems.g54.toString());
    //                break;
    //            case 2:
    //                coordinateSystem.set(coordinate_systems.g55.toString());
    //                break;
    //            case 3:
    //                coordinateSystem.set(coordinate_systems.g56.toString());
    //                break;
    //            case 4:
    //                coordinateSystem.set(coordinate_systems.g57.toString());
    //                break;
    //            case 5:
    //                coordinateSystem.set(coordinate_systems.g58.toString());
    //                break;
    //            case 6:
    //                coordinateSystem.set(coordinate_systems.g59.toString());
    //                break;
    //            default:
    //                coordinateSystem.set(coordinate_systems.g54.toString());
    //                break;
    //        }
    //    }
    public void setMachineState(int state) {

        switch (state) {
            case 1:
                getM_state().set(machine_states.reset.toString());
                break;
            case 2:
                getM_state().set(machine_states.cycle.toString());
                break;
            case 3:
                getM_state().set(machine_states.stop.toString());
                break;
            case 4:
                getM_state().set(machine_states.end.toString());
                break;
            case 5:
                getM_state().set(machine_states.run.toString());
                break;
            case 6:
                getM_state().set(machine_states.hold.toString());
                break;
            case 7:
                getM_state().set(machine_states.homing.toString());
                break;
            case 8:
                getM_state().set(machine_states.probe.toString());
                break;
            case 9:
                getM_state().set(machine_states.jog.toString());
                break;
        }
    }

    public float getMin_arc_segment() {
        return min_arc_segment;
    }

    public void setMin_arc_segment(float min_arc_segment) {
        this.min_arc_segment = min_arc_segment;
    }

    public float getMin_line_segment() {
        return min_line_segment;
    }

    public void setMin_line_segment(float min_line_segment) {
        this.min_line_segment = min_line_segment;
    }

    public double getMin_segment_time() {
        return min_segment_time;
    }

    public void setMin_segment_time(double min_segment_time) {
        this.min_segment_time = min_segment_time;
    }

    public Double getVelocityValue() {
        return (velocity.get());
    }
    public SimpleDoubleProperty getVelocity() {
        return velocity;
    }

    public void setVelocity(double vel) {
        velocity.set(vel);
    }

    public static Machine getInstance() {
        return MachineHolder.INSTANCE;
    }

    private static class MachineHolder {

        private static final Machine INSTANCE = new Machine();
    }

    public Machine() {

//        this.firmwareVersion

        //Initially set gcode units to mm
//        setGcodeUnits(Gcode_unit_modes.MM.toString());
        motors.add(Motor1);
        motors.add(Motor2);
        motors.add(Motor3);
        motors.add(Motor4);


        axis.add(x);
        axis.add(y);
        axis.add(z);
        axis.add(a);
        axis.add(b);
        axis.add(c);


        setMotionMode(0);
        xjoggingIncrement.bind(getAxisByName("X").getTravelMaxSimple());
        yjoggingIncrement.bind(getAxisByName("Y").getTravelMaxSimple());
        zjoggingIncrement.bind(getAxisByName("Z").getTravelMaxSimple());

    }

    public double getJoggingIncrementByAxis(String _axisName) {
        return getAxisByName(_axisName).getTravelMaxSimple().get();
    }

    public GcodeCoordinateSystem getCoordinateSystemByName(String name) {
        for (GcodeCoordinateSystem _tmpGCS : getGcodeCoordinateSystems()) {
            if (_tmpGCS.getCoordinate().equals(name)) {
                return (_tmpGCS);
            }
        }
        return null;
    }

    public GcodeCoordinateSystem getCoordinateSystemByNumberMnemonic(int number) {
        for (GcodeCoordinateSystem _tmpGCS : getGcodeCoordinateSystems()) {
            if (_tmpGCS.getCoordinateNumberMnemonic() == number) {
                getLogger().info("Returned " + _tmpGCS.getCoordinate() + " coord system");
                return (_tmpGCS);
            }
        }
        return null;
    }

    public GcodeCoordinateSystem getCoordinateSystemByTgNumber(int number) {
        for (GcodeCoordinateSystem _tmpGCS : getGcodeCoordinateSystems()) {
            if (_tmpGCS.getCoordinateNumberByTgFormat() == number) {
                getLogger().info("Returned " + _tmpGCS.getCoordinate() + " coord system");
                return (_tmpGCS);
            }
        }
        return null;
    }

    public List<Axis> getAllAxis() {
        return getAxis();
    }

    public List getAllLinearAxis() {

        List _allAxis = getAllAxis();
        List _retAxisList = new ArrayList();


        Axis _ax;

        for (int i = 0; i < _allAxis.size(); i++) {
            Axis a = (Axis) _allAxis.get(i);
            if (a.getAxisType().equals(Axis.AXIS_TYPE.LINEAR)) {
                _retAxisList.add(a);
            }

        }
        return _retAxisList;

    }

    public Axis getAxisByName(char c) {
        return (getAxisByName(String.valueOf(c)));
    }

    public Axis getAxisByName(String name) {
        for (Axis tmpAxis : getAxis()) {
            if (tmpAxis.getAxis_name().equals(name.toUpperCase())) {
                return (tmpAxis);
            }
        }
        return null;
    }

    public Motor getMotorByNumber(String m) {
        //Little stub method to allow calling getMotorByNumber with String arg.
        return getMotorByNumber(Integer.valueOf(m));
    }

    public Motor getMotorByNumber(int i) {
        for (Motor m : getMotors()) {
            if (m.getId_number() == i) {
                return (m);
            }
        }
        return null;
    }

    public int getMotorAxis(Motor m) {
        return m.getId_number();
    }

    public void setMotorAxis(int motorNumber, int x) {
        Motor m = getMotorByNumber(motorNumber);
        m.setMapToAxis(x);
    }

    public void applyJsonStatusReport(responseCommand rc) {


        switch (rc.getSettingKey()) {
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_LINE):
                TinygDriver.getInstance().machine.setLineNumber(Integer.valueOf(rc.getSettingValue()));
                setLineNumber(Integer.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_MOTION_MODE):
                TinygDriver.getInstance().machine.setMotionMode(Integer.valueOf(rc.getSettingValue()));
                break;
            //Machine Position Cases
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_MACHINEPOSX):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingKey().charAt(3)).setMachinePosition(Double.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_MACHINEPOSY):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingKey().charAt(3)).setMachinePosition(Double.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_MACHINEPOSZ):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingKey().charAt(3)).setMachinePosition(Double.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_MACHINEPOSA):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingKey().charAt(3)).setMachinePosition(Double.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_WORKOFFSETX):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingKey().charAt(3)).setOffset(Double.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_WORKOFFSETY):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingKey().charAt(3)).setOffset(Double.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_WORKOFFSETZ):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingKey().charAt(3)).setOffset(Double.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_WORKOFFSETA):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingKey().charAt(3)).setOffset(Double.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_TINYG_DISTANCE_MODE):
                TinygDriver.getInstance().machine.setGcodeDistanceMode(rc.getSettingValue());
                break;

            /*
             * INSERT HOMED HERE
             */

            case (MnemonicManager.MNEMONIC_STATUS_REPORT_STAT):
                TinygDriver.getInstance().machine.setMachineState(Integer.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_UNIT):
                TinygDriver.getInstance().machine.setGcodeUnits(Integer.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_COORDNIATE_MODE):
                TinygDriver.getInstance().machine.getGcm().setCurrentGcodeCoordinateSystem(Integer.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_VELOCITY):
                TinygDriver.getInstance().machine.setVelocity(Double.valueOf(rc.getSettingValue()));
                break;
        }
    }

//This is the main method to parser a JSON sys object
    public void applyJsonSystemSetting(JSONObject js, String parent) throws IOException {
        getLogger().info("Applying JSON Object to System Group");
        Iterator ii = js.keySet().iterator();
        try {
            while (ii.hasNext()) {
                String _key = ii.next().toString();
                String _val = js.get(_key).toString();
                final responseCommand rc = new responseCommand(parent, _key, _val);

                switch (_key) {

                    case (MnemonicManager.MNEMONIC_SYSTEM_BAUDRATE):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;
                    case (MnemonicManager.MNEMONIC_SYSTEM_HARDWARD_PLATFORM):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
//                        TinygDriver.getInstance().hardwarePlatform.setHardwarePlatformVersion(Integer.valueOf(rc.getSettingValue()));
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_HARDWARE_VERSION):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        TinygDriver.getInstance().hardwarePlatformManager.setHardwarePlatformByVersionNumber(Integer.valueOf(rc.getSettingValue()));
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_ENABLE_ECHO):
                        TinygDriver.getInstance().machine.setEnable_echo(Boolean.valueOf(rc.getSettingValue()));
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_ENABLE_JSON_MODE):
                        //TinygDriver.getInstance().m(Float.valueOf(rc.getSettingValue()));
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_ENABLE_XON):
                        TinygDriver.getInstance().machine.setEnable_xon_xoff(Boolean.valueOf(rc.getSettingValue()));
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_FIRMWARE_BUILD):
                        TinygDriver.getInstance().machine.setFirmwareBuild(Double.valueOf(rc.getSettingValue()));
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_FIRMWARE_VERSION):
                        TinygDriver.getInstance().machine.setFirmwareVersion(rc.getSettingValue());
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_DEFAULT_GCODE_COORDINATE_SYSTEM):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
//                        TinygDriver.getInstance().m.setCoordinateSystem(rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_DEFAULT_GCODE_DISTANCE_MODE):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        TinygDriver.getInstance().machine.setGcodeDistanceMode(rc.getSettingValue());
                        break;
                    case (MnemonicManager.MNEMONIC_SYSTEM_DEFAULT_GCODE_PATH_CONTROL):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        TinygDriver.getInstance().machine.setGcodePathControl(rc.getSettingValue());
                        break;
                    case (MnemonicManager.MNEMONIC_SYSTEM_DEFAULT_GCODE_PLANE):
                        //TinygDriver.getInstance().m.setGcodeSelectPlane(Float.valueOf(rc.getSettingValue()));
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        TinygDriver.getInstance().machine.setGcodeSelectPlane(rc.getSettingValue());
                        break;


                    case (MnemonicManager.MNEMONIC_SYSTEM_IGNORE_CR):
//                        TinygDriver.getInstance().m.setIgnore_cr_lf_RX(rc.getSettingValue());
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_JSON_VOBERSITY):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_JUNCTION_ACCELERATION):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;


                    case (MnemonicManager.MNEMONIC_SYSTEM_MIN_ARC_SEGMENT):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_MIN_LINE_SEGMENT):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_MIN_TIME_SEGMENT):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_QUEUE_REPORTS):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_STATUS_REPORT_INTERVAL):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_SWITCH_TYPE):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        TinygDriver.getInstance().machine.setSwitchType(Integer.valueOf(rc.getSettingValue()));
                        String[] message = new String[2];
                        message[0] = "MACHINE_UPDATE";
                        message[1] = null;
                        TinygDriver.getInstance().resParse.set_Changed();
                        TinygDriver.getInstance().resParse.notifyObservers(message);


                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_TEXT_VOBERSITY):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_TINYG_ID_VERSION):
                        getLogger().info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        this.setHardwareId(rc.getSettingValue());
                        break;



//                    case (MnemonicManager.MNEMONIC_SYSTEM_LAST_MESSAGE):
//                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
//                        break;


//                    case (MnemonicManager.MNEMONIC_SYSTEM_LAST_MESSAGE):
//                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
//                        break;

                }

                //Here we are going to have the resParser notifiy the GUI observer that it has changed 
                //This is so we get updates to the GUI when the system changes.
//                String[] message = new String[2];
//                message[0] = "MACHINE_UPDATE";
//                message[1] = null;
//                TinygDriver.getInstance().resParse.hasChanged();
//                TinygDriver.getInstance().resParse.notifyObservers(message);
            }

        } catch (JSONException | NumberFormatException ex) {
            getLogger().error("Error in ApplyJsonSystemSetting in Machine:SYS group");
        }

    }
}