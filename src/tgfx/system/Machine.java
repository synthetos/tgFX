/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.system;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ril3y
 */
public class Machine {

    /**
     * [fc] config_version 0.00 [fv] firmware_version 0.93 [fv] firmware_build
     * 329.38 [ln] line_number 0 [ms] machine_state 0 [vl] velocity 0.000 mm/min
     * [gi] gcode_inches_mode G21 [20,21] [gs] gcode_select_plane G17 [17,18,19]
     * [gp] gcode_path_control G64.0 [61,61.1,64] [ga] gcode_absolute_mode G90
     * [90,91] [ea] enable_acceleration 1 [0,1] [ja] corner_acceleration 200000
     * mm [ml] min_line_segment 0.080 mm [ma] min_arc_segment 0.100 mm [mt]
     * min_segment_time 10000 uSec [ic] ignore_CR (on RX) 0 [0,1] [il] ignore_LF
     * (on RX) 0 [0,1] [ec] enable_CR (on TX) 0 [0,1] [ee] enable_echo 1 [0,1]
     * [ex] enable_xon_xoff 1 [0,1]
     */
    //TG Specific
    //Machine EEPROM Values
    private String CURRENT_MACHINE_JSON_OBJECT;
    private float firmware_build;
    private float firmware_version;
    private int status_report_interval;
    public Gcode_unit_modes gcode_units;
    public Gcode_unit_modes gcode_startup_units;
    public Gcode_select_plane gcode_select_plane;
    public Gcode_coord_system gcode_select_coord_system;
    public Gcode_path_control gcode_path_control;
    public Gcode_distance_mode gcode_distance_mode;
    private boolean enable_acceleration;
    private float junction_acceleration;
    private float min_line_segment;
    private float min_arc_segment;
    private double min_segment_time;
    public Ignore_CR_LF_ON_RX ignore_cr_lf_RX;
    private boolean enable_CR_on_TX;
    private boolean enable_echo;
    private boolean enable_xon_xoff;
    private boolean enable_hashcode;
    //Misc
    private int line_number;
    public static motion_modes motion_mode;
    private float velocity;
    private List<Motor> motors = new ArrayList<>();
    private List<Axis> axis = new ArrayList<>();
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

    public static enum motion_modes {
//        [momo] motion_mode        - 0=traverse, 1=straight feed, 2=cw arc, 3=ccw arc

        traverse, straight, cw_arc, ccw_arc, invalid
    }

    public static enum coordinate_systems {

        g54, g55, g56, g57, g58, g59
    }

    public Gcode_select_plane getGcode_select_plane() {
        return gcode_select_plane;
    }
    

    public void setGcode_select_plane(int gsp) {
        switch(gsp){
            case 0:
                this.gcode_select_plane = gcode_select_plane.XY;
            case 1:
                this.gcode_select_plane = gcode_select_plane.XZ;
            case 2:
                this.gcode_select_plane = gcode_select_plane.YZ;
        }
    }
    
    public void setGcode_select_plane(Gcode_select_plane gcode_select_plane) {
        this.gcode_select_plane = gcode_select_plane;
    }

    public String getCURRENT_MACHINE_JSON_OBJECT() {
        return CURRENT_MACHINE_JSON_OBJECT;
    }

    public void setCURRENT_MACHINE_JSON_OBJECT(String CURRENT_MACHINE_JSON_OBJECT) {
        this.CURRENT_MACHINE_JSON_OBJECT = CURRENT_MACHINE_JSON_OBJECT;
    }
    
    public static coordinate_systems coordinate_system;

//    public static enum motion_modes {
////        [momo] motion_mode        - 0=traverse, 1=straight feed, 2=cw arc, 3=ccw arc
//
//        traverse, straight, cw_arc, ccw_arc, invalid
//    }
    public static enum machine_states {

        reset, nop, stop, end, run, hold, homing
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
        switch (ignore_cr_lf_RX){
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

        INCHES, //G21
        MM      //G20
    };

    public static enum Gcode_select_plane {
        //gpl

        XY, //G17
        XZ, //G18
        YZ  //G19
    }

    public static enum Gcode_distance_mode {
        //gdi

        G61,
        G61POINT1,
        G64
    }

    public static enum Gcode_path_control {
        //gpl

        ABSOLUTE, //G90
        INCREMENTAL   //91
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
    //TG Composer Specific
    private String machineName;

    public String getMachineName() {
        return machineName;
    }

    public void setUnits(int unitMode) {
        if (unitMode == 0) {
            gcode_units = gcode_units.INCHES;
        } else {
            gcode_units = gcode_units.MM;
        }
    }

    
    
    public Gcode_unit_modes getUnitMode() {
        return gcode_units;
    }

    public void setMotionMode(int mode) {
//        machine_state = machine_state.reset;
        if (mode == 0) {
            motion_mode = motion_mode.traverse;
        } else if (mode == 1) {
            motion_mode = motion_mode.straight;
        } else if (mode == 2) {
            motion_mode = motion_mode.cw_arc;
        } else if (mode == 3) {
            motion_mode = motion_mode.ccw_arc;
        } else {
            motion_mode = motion_mode.invalid;
        }
    }

    public motion_modes getMotionMode() {
        return motion_mode;
    }

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

    public float getFirmware_build() {
        return firmware_build;
    }

    public void setFirmware_build(float firmware_build) {
        this.firmware_build = firmware_build;
    }

    public float getFirmware_version() {
        return firmware_version;
    }

    public void setFirmware_version(float firmware_version) {
        this.firmware_version = firmware_version;
    }

    public int getLine_number() {
        return line_number;
    }

    public void setLine_number(int line_number) {
        this.line_number = line_number;
    }

    public machine_states getMachineState() {
        return this.machine_state;
    }

    public coordinate_systems getCoordinateSystem() {
        return (this.coordinate_system);
    }

    public void setCoordinate_mode(double m) {
        int c = (int) (m); //Convert this to a int
        setCoordinate_mode(c);
    }

    public void setCoordinate_mode(int c) {
        switch (c) {
            case 1:
                Machine.coordinate_system = coordinate_systems.g54;
                break;
            case 2:
                Machine.coordinate_system = coordinate_systems.g55;
                break;
            case 3:
                Machine.coordinate_system = coordinate_systems.g56;
                break;
            case 4:
                Machine.coordinate_system = coordinate_systems.g57;
                break;
            case 5:
                Machine.coordinate_system = coordinate_systems.g58;
                break;
            case 6:
                Machine.coordinate_system = coordinate_systems.g59;
                break;
            default:
                Machine.coordinate_system = coordinate_systems.g54;
                break;
        }
    }

    public void setMachineState(int state) {
        if (state == 0) {
            machine_state = machine_state.reset;
        } else if (state == 1) {
            machine_state = machine_state.nop;
        } else if (state == 2) {
            machine_state = machine_state.stop;
        } else if (state == 3) {
            machine_state = machine_state.end;
        } else if (state == 4) {
            machine_state = machine_state.run;
        } else if (state == 5) {
            machine_state = machine_state.hold;
        } else if (state == 6) {
            machine_state = machine_state.homing;
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

    public float getVelocity() {
        return velocity;
    }

    public void setVelocity(float vel) {
        velocity = vel;
    }

    public static Machine getInstance() {
        return MachineHolder.INSTANCE;
    }

    private static class MachineHolder {

        private static final Machine INSTANCE = new Machine();
    }

    public Machine() {

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

    }
//        this.setFlow("OK");
//        for (int i = 1; i < 8; i++) {
//            Motor m = new Motor(i);
//            motors.add(m);
//        }
    //DO NOT TRY TO SET XYZ to anything BUT LINEAR
    //This is a hardware limitation.
//        Axis x = new Axis(Axis.AXIS.X, Axis.AXIS_TYPE.LINEAR);
//        Axis y = new Axis(Axis.AXIS.Y, Axis.AXIS_TYPE.LINEAR);
//        Axis z = new Axis(Axis.AXIS.Z, Axis.AXIS_TYPE.LINEAR);
//        Axis a = new Axis(Axis.AXIS.A, Axis.AXIS_TYPE.ROTATIONAL);
//        Axis b = new Axis(Axis.AXIS.B, Axis.AXIS_TYPE.ROTATIONAL);
//        Axis c = new Axis(Axis.AXIS.C, Axis.AXIS_TYPE.ROTATIONAL);

    //This just initially sets 1 motor to 1 axis at start time
    //The profile in tgFX will load / changes these vaules.
    //Also on connect to your board it should update these values to 
    //Your correct board setting.  Currently 4/5/12 it does not.
//        for (Motor m : motors) {
//
//            if (m.getId_number() == 1) {
//                x.addMotor(m);
//            }
//            if (m.getId_number() == 2) {
//                y.addMotor(m);
//            }
//            if (m.getId_number() == 3) {
//                z.addMotor(m);
//            }
//            if (m.getId_number() == 4) {
//                a.addMotor(m);
//            }
//            if (m.getId_number() == 5) {
//                b.addMotor(m);
//            }
//            if (m.getId_number() == 6) {
//                c.addMotor(m);
//            }
//        }
//        axis.add(x);
//        axis.add(y);
//        axis.add(z);
//        axis.add(a);
//        axis.add(b);
//        axis.add(c);
//    }
    public List<Axis> getAllAxis() {
        return axis;
    }

    public Axis getAxisByName(String name) {
        for (Axis tmpAxis : axis) {
            if (tmpAxis.getAxis_name().equals(name.toUpperCase())) {
                return (tmpAxis);
            }
        }
        return null;
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
}