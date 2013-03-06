/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.system;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import tgfx.tinyg.MnemonicManager;
import org.json.JSONObject;
import tgfx.tinyg.TinygDriver;
import org.apache.log4j.Logger;
import org.json.JSONException;
import tgfx.tinyg.responseCommand;

/**
 *
 * @author ril3y
 */
public final class Machine {

    //TG Specific
    //Machine EEPROM Values
    //binding
    public SimpleStringProperty m_state = new SimpleStringProperty();
    public SimpleStringProperty m_mode = new SimpleStringProperty();
    public SimpleDoubleProperty firmwareBuild = new SimpleDoubleProperty();
    public StringProperty firmwareVersion = new SimpleStringProperty();
    public StringProperty hardwareId = new SimpleStringProperty("na");
    public StringProperty hardwareVersion = new SimpleStringProperty("na");
    public SimpleDoubleProperty velocity = new SimpleDoubleProperty();
    private SimpleStringProperty gcodeUnitMode = new SimpleStringProperty("mm");
    public SimpleDoubleProperty gcodeUnitDivision = new SimpleDoubleProperty(1);
    private SimpleStringProperty gcodeDistanceMode = new SimpleStringProperty();
    private int switchType = 0; //0=normally closed 1 = normally open
    private int status_report_interval;
    public Gcode_unit_modes gcode_startup_units;
    public Gcode_select_plane gcode_select_plane;
    public Gcode_coord_system gcode_select_coord_system;
    public Gcode_path_control gcode_path_control;
    public Gcode_distance_mode gcode_distance_mode;
    private boolean enable_acceleration;
    private float junction_acceleration;
    private float min_line_segment;
    static final Logger logger = Logger.getLogger(TinygDriver.class);
    private float min_arc_segment;
    private double min_segment_time;
    public Ignore_CR_LF_ON_RX ignore_cr_lf_RX;
    private boolean enable_CR_on_TX;
    private boolean enable_echo;
    private boolean enable_xon_xoff;
    private boolean enable_hashcode;

    //Misc
    public SimpleIntegerProperty lineNumber = new SimpleIntegerProperty(0);
    private String last_message = new String("");
//    public static motion_modes motion_mode = new SimpleIntegerProperty();
    public static motion_modes motion_mode;
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
    public GcodeCoordinateManager gcm = new GcodeCoordinateManager();

    public static enum motion_modes {
//        [momo] motion_mode        - 0=traverse, 1=straight feed, 2=cw arc, 3=ccw arc

        traverse, feed, cw_arc, ccw_arc, cancel
    }

    public static enum coordinate_systems {

        g54, g55, g56, g57, g58, g59
    }

    public void setSwitchType(int swType){
        this.switchType = swType;
    }
    
    public int getSwitchType(){
        return(switchType);
    }
    
    public String getSwitchTypeAsString(){
        if(switchType == 0){
            return("Normally Open");
        }else{
            return("Normally Closed");
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
                this.gcode_distance_mode = Gcode_distance_mode.ABSOLUTE;
                break;
            case 1:
                this.gcode_distance_mode = Gcode_distance_mode.INCREMENTAL;
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
                this.gcode_select_plane = Gcode_select_plane.XY;
            case 1:
                this.gcode_select_plane = Gcode_select_plane.XZ;
            case 2:
                this.gcode_select_plane = Gcode_select_plane.YZ;
        }
    }

    public void setGcode_select_plane(Gcode_select_plane gcode_select_plane) {
        this.gcode_select_plane = gcode_select_plane;
    }
    private SimpleStringProperty coordinateSystem = new SimpleStringProperty();

    public StringProperty getHardwareId() {
        return hardwareId;
    }
    
    

    public void setHardwareId(String hwIdString) {
        hardwareId.set(hwIdString);
    }

    public StringProperty getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion.set(hardwareVersion);
    }
    
    
//    public static enum motion_modes {
////        [momo] motion_mode        - 0=traverse, 1=straight feed, 2=cw arc, 3=ccw arc
//
//        traverse, straight, cw_arc, ccw_arc, invalid
//    }
    public static enum machine_states {

        reset, cycle, stop, end, run, hold, homing, probe, jog
    }
    public static machine_states machine_state;

    public Ignore_CR_LF_ON_RX getIgnore_cr_lf_TX() {
        return ignore_cr_lf_RX;
    }

    public void setIgnore_cr_lf_TX(Ignore_CR_LF_ON_RX ignore_cr_lf_TX) {

        this.ignore_cr_lf_RX = ignore_cr_lf_TX;
    }

    public void setIgnore_cr_lf_RX(int ignore_cr_lf_RX) {
        //Send a int
        switch (ignore_cr_lf_RX) {
            case 0:
                this.ignore_cr_lf_RX = Ignore_CR_LF_ON_RX.OFF;
                break;
            case 1:
                this.ignore_cr_lf_RX = Ignore_CR_LF_ON_RX.CR;
                break;
            case 2:
                this.ignore_cr_lf_RX = Ignore_CR_LF_ON_RX.LF;
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
                this.gcode_path_control = Gcode_path_control.G61;
                break;
            case 1:
                this.gcode_path_control = Gcode_path_control.G61POINT1;
                break;
            case 2:
                this.gcode_path_control = Gcode_path_control.G64;
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
            gcodeUnitMode.setValue(Gcode_unit_modes.inches.toString());
            gcodeUnitDivision.set(25.4);  //mm to inches conversion   
        } else if (unitMode == 1) {
            gcodeUnitMode.setValue(Gcode_unit_modes.mm.toString());
            gcodeUnitDivision.set(1);
        }

    }
//

    public SimpleStringProperty getGcodeUnitMode() {
        return gcodeUnitMode;
    }

    public int getGcodeUnitModeAsInt() {
        if (gcodeUnitMode.get().equals(Gcode_unit_modes.mm.toString())) {
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
                gcodeUnitMode.set(Gcode_unit_modes.inches.toString());
                break;
            case (1):
                gcodeUnitMode.set(Gcode_unit_modes.mm.toString());
                break;
        }
    }

//    public void setGcodeUnits(Gcode_unit_modes gcode_units) {
//        this.gcode_unit_mode = gcode_units;
//    }
    public SimpleStringProperty getMotionMode() {
        return (m_mode);
    }

    public void setMotionMode(int mode) {

        if (mode == 0) {
            m_mode.set(motion_modes.traverse.toString());
        } else if (mode == 1) {
            m_mode.set(motion_modes.feed.toString());
        } else if (mode == 2) {
            m_mode.set(motion_modes.cw_arc.toString());
        } else if (mode == 3) {
            m_mode.set(motion_modes.ccw_arc.toString());
        } else {
            m_mode.set(motion_modes.cancel.toString());
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

    public double getFirmwareBuild() {
        return firmwareBuild.getValue();
    }

    public void setFirmwareBuild(double firmware_build) {
        this.firmwareBuild.set(firmware_build);

    }

    public StringProperty getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String fv) {
        this.firmwareVersion.setValue(fv);
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
        return this.m_state;
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
                m_state.set(machine_states.reset.toString());
                break;
            case 2:
                m_state.set(machine_states.cycle.toString());
                break;
            case 3:
                m_state.set(machine_states.stop.toString());
                break;
            case 4:
                m_state.set(machine_states.end.toString());
                break;
            case 5:
                m_state.set(machine_states.run.toString());
                break;
            case 6:
                m_state.set(machine_states.hold.toString());
                break;
            case 7:
                m_state.set(machine_states.homing.toString());
                break;
            case 8:
                m_state.set(machine_states.probe.toString());
                break;
            case 9:
                m_state.set(machine_states.jog.toString());
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

    public Double getVelocity() {
        return (velocity.get());
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

        


    }

    public GcodeCoordinateSystem getCoordinateSystemByName(String name) {
        for (GcodeCoordinateSystem _tmpGCS : gcodeCoordinateSystems) {
            if (_tmpGCS.getCoordinate().equals(name)) {
                return (_tmpGCS);
            }
        }
        return null;
    }

    public GcodeCoordinateSystem getCoordinateSystemByNumberMnemonic(int number) {
        for (GcodeCoordinateSystem _tmpGCS : gcodeCoordinateSystems) {
            if (_tmpGCS.getCoordinateNumberMnemonic() == number) {
                logger.info("Returned " + _tmpGCS.getCoordinate() + " coord system");
                return (_tmpGCS);
            }
        }
        return null;
    }

    public GcodeCoordinateSystem getCoordinateSystemByTgNumber(int number) {
        for (GcodeCoordinateSystem _tmpGCS : gcodeCoordinateSystems) {
            if (_tmpGCS.getCoordinateNumberByTgFormat() == number) {
                logger.info("Returned " + _tmpGCS.getCoordinate() + " coord system");
                return (_tmpGCS);
            }
        }
        return null;
    }

    public List<Axis> getAllAxis() {
        return axis;
    }

    public Axis getAxisByName(char c) {
        return (getAxisByName(String.valueOf(c)));
    }

    public Axis getAxisByName(String name) {
        for (Axis tmpAxis : axis) {
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
        for (Motor m : motors) {
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

//        Iterator ii = js.keySet().iterator();
//        try {
//            while (ii.hasNext()) {
//                String _key = ii.next().toString();
//                String _val = js.get(_key).toString();
//                final responseCommand rc = new responseCommand(parent, _key, _val);

        switch (rc.getSettingKey()) {
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_LINE):
                TinygDriver.getInstance().m.setLineNumber(Integer.valueOf(rc.getSettingValue()));
                setLineNumber(Integer.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_MOTION_MODE):
                TinygDriver.getInstance().m.setMotionMode(Integer.valueOf(rc.getSettingValue()));
                break;
            //Machine Position Cases
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_MACHINEPOSX):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingKey().charAt(3)).setMachinePosition(Double.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_MACHINEPOSY):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingKey().charAt(3)).setMachinePosition(Double.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_MACHINEPOSZ):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingKey().charAt(3)).setMachinePosition(Double.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_MACHINEPOSA):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingKey().charAt(3)).setMachinePosition(Double.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_WORKOFFSETX):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingKey().charAt(3)).setOffset(Double.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_WORKOFFSETY):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingKey().charAt(3)).setOffset(Double.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_WORKOFFSETZ):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingKey().charAt(3)).setOffset(Double.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_WORKOFFSETA):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingKey().charAt(3)).setOffset(Double.valueOf(rc.getSettingValue()));
                break;

            /*
             * INSERT HOMED HERE
             */

            case (MnemonicManager.MNEMONIC_STATUS_REPORT_STAT):
                TinygDriver.getInstance().m.setMachineState(Integer.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_UNIT):
                TinygDriver.getInstance().m.setGcodeUnits(Integer.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_COORDNIATE_MODE):
                TinygDriver.getInstance().m.gcm.setCurrentGcodeCoordinateSystem(Integer.valueOf(rc.getSettingValue()));
                break;
            case (MnemonicManager.MNEMONIC_STATUS_REPORT_VELOCITY):
                TinygDriver.getInstance().m.setVelocity(Double.valueOf(rc.getSettingValue()));
                break;
        }
    }

//This is the main method to parser a JSON sys object
    public void applyJsonSystemSetting(JSONObject js, String parent) {
        logger.info("Applying JSON Object to System Group");
        Iterator ii = js.keySet().iterator();
        try {
            while (ii.hasNext()) {
                String _key = ii.next().toString();
                String _val = js.get(_key).toString();
                final responseCommand rc = new responseCommand(parent, _key, _val);

                switch (_key) {
                    case (MnemonicManager.MNEMONIC_SYSTEM_BAUDRATE):
                        //todo do this
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_ENABLE_ECHO):
                        TinygDriver.getInstance().m.setEnable_echo(Boolean.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_ENABLE_JSON_MODE):
                        //TinygDriver.getInstance().m(Float.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_ENABLE_XON):
                        TinygDriver.getInstance().m.setEnable_xon_xoff(Boolean.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_FIRMWARE_BUILD):
                        TinygDriver.getInstance().m.setFirmwareBuild(Double.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_FIRMWARE_VERSION):
                        TinygDriver.getInstance().m.setFirmwareVersion(rc.getSettingValue());
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_DEFAULT_GCODE_COORDINATE_SYSTEM):
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
//                        TinygDriver.getInstance().m.setCoordinateSystem(rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_DEFAULT_GCODE_DISTANCE_MODE):
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        TinygDriver.getInstance().m.setGcodeDistanceMode(rc.getSettingValue());
                        break;
                    case (MnemonicManager.MNEMONIC_SYSTEM_DEFAULT_GCODE_PATH_CONTROL):
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        TinygDriver.getInstance().m.setGcodePathControl(rc.getSettingValue());
                        break;
                    case (MnemonicManager.MNEMONIC_SYSTEM_DEFAULT_GCODE_PLANE):
                        //TinygDriver.getInstance().m.setGcodeSelectPlane(Float.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        TinygDriver.getInstance().m.setGcodeSelectPlane(rc.getSettingValue());
                        break;


                    case (MnemonicManager.MNEMONIC_SYSTEM_IGNORE_CR):
//                        TinygDriver.getInstance().m.setIgnore_cr_lf_RX(rc.getSettingValue());
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_JSON_VOBERSITY):
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_JUNCTION_ACCELERATION):
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;


                    case (MnemonicManager.MNEMONIC_SYSTEM_MIN_ARC_SEGMENT):
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_MIN_LINE_SEGMENT):
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_MIN_TIME_SEGMENT):
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_QUEUE_REPORTS):
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_STATUS_REPORT_INTERVAL):
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_SWITCH_TYPE):
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                       TinygDriver.getInstance().m.setSwitchType(Integer.valueOf(rc.getSettingValue()));
                       String[] message = new String[2];
                       message[0] = "MACHINE_UPDATE";
                       message[1] = null;
                       TinygDriver.getInstance().resParse.set_Changed();
                       TinygDriver.getInstance().resParse.notifyObservers(message);
                       
                       
                        break;

                    case (MnemonicManager.MNEMONIC_SYSTEM_TEXT_VOBERSITY):
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;
                    
                    case (MnemonicManager.MNEMONIC_SYSTEM_TINYG_ID_VERSION):
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        this.setHardwareId(rc.getSettingValue());
                        break;
                    
                    case (MnemonicManager.MNEMONIC_SYSTEM_HARDWARE_VERSION):
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        this.setHardwareVersion(rc.getSettingValue());
                        break;

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
            logger.error("Error in ApplyJsonSystemSetting in Machine:SYS group");
        }

    }
}