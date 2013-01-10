/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.system;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.property.SimpleDoubleProperty;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import tgfx.tinyg.MnemonicManager;
import tgfx.tinyg.TinygDriver;
import tgfx.tinyg.responseCommand;

/**
 * [xmp] x_machine_position 0.000 mm [xwp] x_work_position 0.000 mm [xam]
 * x_axis_mode 1 [standard] [xfr] x_feedrate_maximum 2400.000 mm/min [xvm]
 * x_velocity_maximum 2400.000 mm/min [xtm] x_travel_maximum 400.000 mm [xjm]
 * x_jerk_maximum 100000000 mm/min^3 [xjd] x_junction_deviation 0.0500 mm [xsm]
 * x_switch_mode 1 [0,1] [xht] x_homing_travel 400.000 mm [xhs]
 * x_homing_search_velocity 2400.000 mm/min [xhl] x_homing_latch_velocity
 * 100.000 mm/min [xhz] x_homing_zero_offset 5.000 mm [xhw] x_homing_work_offset
 * 200.000 mm
 *
 */
public class Axis {

    public enum AXIS_TYPE {

        LINEAR, ROTATIONAL;
    }
    static final Logger logger = Logger.getLogger(TinygDriver.class);
    private String CURRENT_AXIS_JSON_OBJECT;
    private AXIS_TYPE axis_type;
    private float latch_velocity;
//    private float seek_rate_maximum;
    private double latch_backoff;
    private double zero_backoff;
    private double machine_position;
    private SimpleDoubleProperty work_position = new SimpleDoubleProperty();  //This has to be public if we are going to allow it to be updated.
    private AXIS_MODES axis_mode;
    private double radius;
    private double search_velocity;
    private double feed_rate_maximum;
    private double velocity_maximum;
    private double travel_maximum;
    private double jerk_maximum;
    private double junction_devation;
    private SWITCH_MODES max_switch_mode;
    private SWITCH_MODES min_switch_mode;
//    private float homing_travel;
//    private float homing_search_velocity;
//    private float homing_latch_velocity;
//    private float homing_zero_offset;
//    private float homing_work_offset;
    private String axis_name;
    private List<Motor> motors = new ArrayList<Motor>();

    public enum SWITCH_MODES {

        DISABLED,
        HOMING_ONLY,
        LIMIT_ONLY,
        HOMING_AND_LIMIT,
    }

    public String getCURRENT_AXIS_JSON_OBJECT() {
        return CURRENT_AXIS_JSON_OBJECT;
    }

    public void setCURRENT_AXIS_JSON_OBJECT(String CURRENT_AXIS_JSON_OBJECT) {
        this.CURRENT_AXIS_JSON_OBJECT = CURRENT_AXIS_JSON_OBJECT;
    }

    public enum AXIS_MODES {

        DISABLE,
        STANDARD,
        INHIBITED,
        RADIUS,
        SLAVE_X,
        SLAVE_Y,
        SLAVE_Z,
        SLAVE_XY,
        SLAVE_XZ,
        SLAVE_YZ,
        SLAVE_XYZ
    }

    public enum AXIS {

        X, Y, Z, A, B, C
    }

//    public Axis() {
//        axis_mode = AXIS_MODES.STANDARD;
////        latch_velocity = 0;
////        latch_backoff = 0;
////        machine_position = 0;
////        feed_rate_maximum = 800;
////        jerk_maximum = 0;
//        
//    }
    public void setAxisCommand(String cmd, String value) {
        //This is a blind commmand mode...  meaning that
        //if 2 strings (key, val) are passed to the axis class
        //Then we parse the command and set the value.
        switch (cmd) {
            case "am": {
                int val = Double.valueOf(value.substring(0)).intValue();  //Ugh this was a pain to figure out.
                String _axisMode = "UNKNOWN";
                switch (val) {
                    case 0: {
                        _axisMode = "DISABLED";
                        break;
                    }
                    case 1: {
                        _axisMode = "STANDARD";
                        break;
                    }
                    case 2: {
                        _axisMode = "INHIBITED";
                        break;
                    }
                    case 3: {
                        _axisMode = "RADIUS";
                        break;
                    }
                    case 4: {
                        _axisMode = "SLAVE_X";
                        break;
                    }
                    case 5: {
                        _axisMode = "SLAVE_Y";
                        break;
                    }
                    case 6: {
                        _axisMode = "SLAVE_Z";
                        break;
                    }
                    case 7: {
                        _axisMode = "SLAVE_XY";
                        break;
                    }
                    case 8: {
                        _axisMode = "SLAVE_XZ";
                        break;
                    }
                    case 9: {
                        _axisMode = "SLAVE_YZ";
                        break;
                    }
                    case 10: {
                        _axisMode = "SLAVE_XYZ";
                        break;
                    }
                    default: {
                        _axisMode = "DISABLED";
                        return;
                    }
                }
                this.setAxis_mode(val);
                System.out.println("\t[+]Set Axis: " + this.getAxis_name() + " Axis Mode to: " + _axisMode);
                return;
            }
            case "vm": {
                int val = (int) Double.parseDouble(value);
                this.setVelocity_maximum(val);
                System.out.println("\t[+]Set Axis: " + this.getAxis_name() + " Velocity Max to: " + this.getVelocity_maximum());
                return;
            }
            case "fr": {
                int val = (int) Double.parseDouble(value);
                this.setFeed_rate_maximum(val);
                System.out.println("\t[+]Set Axis: " + this.getFeed_rate_maximum() + " Feed Rate Max to: " + this.getFeed_rate_maximum());
                return;
            }
            case "tm": {
                int val = (int) Double.parseDouble(value);
                this.setTravel_maximum(val);
                System.out.println("\t[+]Set Axis: " + this.getAxis_name() + " Travel Max to: " + this.getTravel_maximum());
                return;
            }
            case "jm": {
                int val = (int) Double.parseDouble(value);
                this.setTravel_maximum(val);
                System.out.println("\t[+]Set Axis: " + this.getJerk_maximum() + " Jerk Max to: " + this.getJerk_maximum());
                return;
            }
            case "jd": {
                int val = (int) Double.parseDouble(value);
                this.setTravel_maximum(val);
                System.out.println("\t[+]Set Axis: " + this.getJunction_devation() + " Junction Deviation Max to: " + this.getJunction_devation());
                return;
            }
            case "sx": {
                int val = Double.valueOf(value.substring(0)).intValue();

                String _switchMode = "UNKNOWN";
                switch (val) {
                    case 0: {
                        _switchMode = "DISABLED";
                        break;
                    }
                    case 1: {
                        _switchMode = "HOMING ONLY";
                        break;
                    }
                    case 2: {
                        _switchMode = "HOMING AND LIMIT";
                        break;
                    }
                }
                this.setMaxSwitch_mode(val);
                System.out.println("\t[+]Set Axis: " + this.getAxis_name() + " Axis Mode to: " + _switchMode);
                return;
            }
            case "sn": {
                int val = Double.valueOf(value.substring(0)).intValue();

                String _switchMode = "UNKNOWN";
                switch (val) {
                    case 0: {
                        _switchMode = "DISABLED";
                        break;
                    }
                    case 1: {
                        _switchMode = "HOMING ONLY";
                        break;
                    }
                    case 2: {
                        _switchMode = "HOMING AND LIMIT";
                        break;
                    }
                }
                this.setMaxSwitch_mode(val);
                System.out.println("\t[+]Set Axis: " + this.getAxis_name() + " Axis Mode to: " + _switchMode);
                return;
            }

            case "sv": {
                int val = (int) Double.parseDouble(value);
                this.setSearch_velocity(val);
                System.out.println("\t[+]Set Axis: " + this.getAxis_name() + " Search Velocity to: " + this.getSearch_velocity());
                return;
            }
            case "lv": {
                int val = (int) Double.parseDouble(value);
                this.setLatch_velocity(val);
                System.out.println("\t[+]Set Axis: " + this.getAxis_name() + " Latch Velocity to: " + this.getLatch_velocity());
                return;
            }
            case "lb": {
                int val = (int) Double.parseDouble(value);
                this.setLatch_backoff(val);
                System.out.println("\t[+]Set Axis: " + this.getAxis_name() + " Latch Back Off to: " + this.getLatch_backoff());
                return;
            }
            case "zb": {
                int val = (int) Double.parseDouble(value);
                this.setZero_backoff(val);
                System.out.println("\t[+]Set Axis: " + this.getAxis_name() + " Zero Back Off to: " + this.getZero_backoff());
                return;
            }
            case "ra": {
                int val = (int) Double.parseDouble(value);
                this.setRadius(val);
                System.out.println("\t[+]Set Axis: " + this.getAxis_name() + " Radius to: " + this.getRadius());
                return;
            }
            default: {
                System.out.println("[!]Error... No such setting: " + value + " in Axis Settings...");
            }
        }
    }

    public double getLatch_backoff() {
        return latch_backoff;
    }

    public double getRadius() {
        return radius;
    }

//    public void setRadius(int radius) {
//        this.radius = radius;
//    }
    public boolean setLatch_backoff(float latch_backoff) {
        this.latch_backoff = latch_backoff;
        return true;
    }

    public float getLatch_velocity() {
        return latch_velocity;
    }

//    public float getSeek_rate_maximum() {
//        return seek_rate_maximum;
//    }
//
//    public void setSeek_rate_maximum(float seek_rate_maximum) {
//        this.seek_rate_maximum = seek_rate_maximum;
//    }
    public double getSearch_velocity() {
        return search_velocity;
    }

    public boolean setSearch_velocity(double search_velocity) {
        this.search_velocity = search_velocity;
        return true;
    }

    public boolean setLatch_velocity(float latch_velocity) {
        this.latch_velocity = latch_velocity;
        return true;
    }

    public double getZero_backoff() {
        return zero_backoff;
    }

    public boolean setZero_backoff(float zero_backoff) {
        this.zero_backoff = zero_backoff;
        return true;
    }

    public void setRadius(double r) {
        this.radius = r;
    }

    public void setAxisType(AXIS_TYPE at) {
        this.axis_type = at;
    }

    public AXIS_TYPE getAxisType() {
        return (this.axis_type);
    }

    public Axis(AXIS ax, AXIS_TYPE at, AXIS_MODES am) {

        this.axis_mode = am;

        if (ax == AXIS.X) {
            this.setAxis_name("X");
            this.setAxisType(at);

        } else if (ax == AXIS.Y) {
            this.setAxis_name("Y");
            this.setAxisType(at);

        } else if (ax == AXIS.Z) {
            this.setAxis_name("Z");
            this.setAxisType(at);

        } else if (ax == AXIS.A) {
            this.setAxis_name("A");
            this.setAxisType(at);

        } else if (ax == AXIS.B) {
            this.setAxis_name("B");
            this.setAxisType(at);

        } else if (ax == AXIS.C) {
            this.setAxis_name("C");
            this.setAxisType(at);
        } else {
            System.out.println("[!]Invalide Axis Name Specified.\n");
        }
    }

    public AXIS_MODES getAxis_mode() {
        return axis_mode;
    }

    public boolean setMotorCommand(String cmd, String value) {
        //Generic command parser when a single axis command has been given.
        //IE: $xsr=1200
        //cmd would be sr and value would be 1200
        switch (cmd) {
            case MnemonicManager.MNEMONIC_AXIS_AXIS_MODE: {
                int val = (int) Double.parseDouble(value);
                return (this.setAxis_mode(val));
            }
            case MnemonicManager.MNEMONIC_AXIS_VELOCITY_MAXIMUM:
                return (this.setVelocity_maximum(Float.valueOf(value)));
            case MnemonicManager.MNEMONIC_AXIS_FEEDRATE_MAXIMUM:
                return (this.setFeed_rate_maximum(Float.valueOf(value)));
            case MnemonicManager.MNEMONIC_AXIS_TRAVEL_MAXIMUM:
                return (this.setTravel_maximum(Float.valueOf(value)));
            case MnemonicManager.MNEMONIC_AXIS_JERK_MAXIMUM:
                return (this.setJerk_maximum(Double.valueOf(value)));
            case MnemonicManager.MNEMONIC_AXIS_JUNCTION_DEVIATION:
                return (this.setJunction_devation(Float.valueOf(value)));
            case "sn": {
                int val = (int) Double.parseDouble(value);
                return (this.setMaxSwitch_mode(val));
            }
            case MnemonicManager.MNEMONIC_AXIS_SEARCH_VELOCITY:
                return (this.setSearch_velocity(Double.parseDouble(value)));
            case MnemonicManager.MNEMONIC_AXIS_LATCH_VELOCITY:
                return (this.setLatch_velocity(Float.parseFloat(value)));
            case MnemonicManager.MNEMONIC_AXIS_LATCH_BACKOFF:
                return (this.setLatch_backoff(Float.parseFloat(value)));
            case MnemonicManager.MNEMONIC_AXIS_ZERO_BACKOFF:
                return (this.setZero_backoff(Float.parseFloat(value)));
            default:
                return false;
        }
    }

    public boolean setAxis_mode(int axMode) {

        switch (axMode) {
            case 0:
                this.axis_mode = AXIS_MODES.DISABLE;
                return true;
            case 1:
                this.axis_mode = AXIS_MODES.STANDARD;
                return true;
            case 2:
                this.axis_mode = AXIS_MODES.INHIBITED;
                return true;
            case 3:
                this.axis_mode = AXIS_MODES.RADIUS;
                return true;
            case 4:
                this.axis_mode = AXIS_MODES.SLAVE_X;
                return true;
            case 5:
                this.axis_mode = AXIS_MODES.SLAVE_Y;
                return true;
            case 6:
                this.axis_mode = AXIS_MODES.SLAVE_Z;
                return true;
            case 7:
                this.axis_mode = AXIS_MODES.SLAVE_XY;
                return true;
            case 8:
                this.axis_mode = AXIS_MODES.SLAVE_XZ;
                return true;
            case 9:
                this.axis_mode = AXIS_MODES.SLAVE_YZ;
                return true;
            case 10:
                this.axis_mode = AXIS_MODES.SLAVE_XYZ;
                return true;
            default:
                return false;
        }
//        if (axMode == 0) {
//            this.axis_mode =AXIS_MODES.DISABLE;
//        }else if( axMode == 1){
//            this.axis_mode = AXIS_MODES.
//        }
    }

    public String getAxis_name() {
        return axis_name;
    }

    public void setAxis_name(String axis_name) {
        this.axis_name = axis_name;
    }

    public double getFeed_rate_maximum() {
        return feed_rate_maximum;
    }

    public boolean setFeed_rate_maximum(float feed_rate_maximum) {
        this.feed_rate_maximum = feed_rate_maximum;
        return true;
    }

//    public float getHoming_latch_velocity() {
//        return homing_latch_velocity;
//    }
//
//    public void setHoming_latch_velocity(float homing_latch_velocity) {
//        this.homing_latch_velocity = homing_latch_velocity;
//    }
//
//    public float getHoming_search_velocity() {
//        return homing_search_velocity;
//    }
//
//    public void setHoming_search_velocity(float homing_search_velocity) {
//        this.homing_search_velocity = homing_search_velocity;
//    }
//
//    public float getHoming_travel() {
//        return homing_travel;
//    }
//
//    public void setHoming_travel(float homing_travel) {
//        this.homing_travel = homing_travel;
//    }
//    public float getHoming_work_offset() {
//        return homing_work_offset;
//    }
//
//    public void setHoming_work_offset(float homing_work_offset) {
//        this.homing_work_offset = homing_work_offset;
//    }
//
//    public float getHoming_zero_offset() {
//        return homing_zero_offset;
//    }
//
//    public void setHoming_zero_offset(float homing_zero_offset) {
//        this.homing_zero_offset = homing_zero_offset;
//    }
    public double getJerk_maximum() {
        return jerk_maximum;
    }

    public boolean setJerk_maximum(double jerk_maximum) {
        this.jerk_maximum = jerk_maximum;
        return true;
    }

    public double getJunction_devation() {
        return junction_devation;
    }

    public boolean setJunction_devation(float junction_devation) {
        this.junction_devation = junction_devation;
        return true;
    }

    public double getMachine_position() {
        return machine_position;
    }

    public boolean setMachine_position(float machine_position) {
        this.machine_position = machine_position;
        return true;
    }

    public List<Motor> getMotors() {
        return motors;
    }

    public boolean addMotor(Motor motor) {
        if (!motors.contains(motor)) {
            motors.add(motor);
            return true;
        }
        return false;
    }

    public void setMotors(List<Motor> motors) {
        this.motors = motors;
    }

    public SWITCH_MODES getMaxSwitch_mode() {
        return max_switch_mode;
    }

    public SWITCH_MODES getMinSwitch_mode() {
        return min_switch_mode;
    }

    public Boolean setMaxSwitch_mode(int _sw_mode) {

        switch (_sw_mode) {
            case 0:
                max_switch_mode = SWITCH_MODES.DISABLED;
                return true;

            case 1:
                max_switch_mode = SWITCH_MODES.HOMING_ONLY;
                return true;
            case 2:
                max_switch_mode = SWITCH_MODES.LIMIT_ONLY;
                return true;
            case 3:
                max_switch_mode = SWITCH_MODES.HOMING_AND_LIMIT;
                return true;
        }
        return false;
    }

    public Boolean setMinSwitch_mode(int _sw_mode) {

        switch (_sw_mode) {
            case 0:
                min_switch_mode = SWITCH_MODES.DISABLED;
                return true;

            case 1:
                min_switch_mode = SWITCH_MODES.HOMING_ONLY;
                return true;
            case 2:
                min_switch_mode = SWITCH_MODES.LIMIT_ONLY;
                return true;
            case 3:
                min_switch_mode = SWITCH_MODES.HOMING_AND_LIMIT;
                return true;
        }
        return false;
    }

    public double getTravel_maximum() {
        return travel_maximum;
    }

    public boolean setTravel_maximum(float travel_maximum) {
        this.travel_maximum = travel_maximum;
        return true;
    }

    public double getVelocity_maximum() {
        return velocity_maximum;
    }

    public boolean setVelocity_maximum(double velocity_maximum) {
        this.velocity_maximum = velocity_maximum;
        return true;
    }

    public SimpleDoubleProperty getWork_position() {
        return work_position;
    }

    public void setWork_position(double workpos) {
        this.work_position.set(workpos);
    }

    //This is the main method to parser a JSON Axis object
    public void applyJsonSystemSetting(JSONObject js, String parent) {
        logger.info("Applying JSON Object to " + parent + " Group");
        Iterator ii = js.keySet().iterator();
        try {
            while (ii.hasNext()) {
                String _key = ii.next().toString();
                String _val = js.get(_key).toString();
                responseCommand rc = new responseCommand(parent, _key, _val);

                switch (_key) {
                    case (MnemonicManager.MNEMONIC_AXIS_AXIS_MODE):
                        TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setAxis_mode(Integer.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_AXIS_FEEDRATE_MAXIMUM):
                        TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setFeed_rate_maximum(Float.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_AXIS_JERK_MAXIMUM):
                        TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setJerk_maximum(Float.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_AXIS_JUNCTION_DEVIATION):
                        TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setJunction_devation(Float.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_AXIS_LATCH_BACKOFF):
                        TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setLatch_backoff(Float.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_AXIS_LATCH_VELOCITY):
                        TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setLatch_velocity(Float.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_AXIS_MAX_SWITCH_MODE):
                        TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setMaxSwitch_mode(Integer.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_AXIS_MIN_SWITCH_MODE):
                        TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setMinSwitch_mode(Integer.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_AXIS_RADIUS):
                        TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setRadius(Float.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_AXIS_SEARCH_VELOCITY):
                        TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setSearch_velocity(Float.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_AXIS_TRAVEL_MAXIMUM):
                        TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setTravel_maximum(Float.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_AXIS_VELOCITY_MAXIMUM):
                        TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setVelocity_maximum(Float.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_AXIS_ZERO_BACKOFF):
                        TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setZero_backoff(Float.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;
                    default:
                        logger.info("Default Switch");
                }
            }

        } catch (JSONException | NumberFormatException ex) {
            logger.error("Error in ApplyJsonSetting in Machine:SYS group");
        }

    }
}