/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.system;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import tgfx.Main;
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
public final class Axis {

    public enum AXIS_TYPE {

        LINEAR, ROTATIONAL;
    }
    static final Logger logger = Logger.getLogger(TinygDriver.class);
    private String CURRENT_AXIS_JSON_OBJECT;
    private AXIS_TYPE axis_type;
    private SimpleBooleanProperty homed = new SimpleBooleanProperty(false);
    private float latch_velocity;
//    private float seek_rate_maximum;
    private double latch_backoff;
    private double zero_backoff;
    private double machine_position;
    private SimpleDoubleProperty workPosition = new SimpleDoubleProperty();     //This has to be public if we are going to allow it to be updated.
    private SimpleDoubleProperty machinePosition = new SimpleDoubleProperty();  //This has to be public if we are going to allow it to be updated.
    private AXIS_MODES axis_mode;
    private double radius;
    private double searchVelocity;
    private double feedRateMaximum;
    private double velocityMaximum;
    private SimpleDoubleProperty travel_maximum = new SimpleDoubleProperty();
    private SimpleDoubleProperty travel_min = new SimpleDoubleProperty();
    private SimpleDoubleProperty offset = new SimpleDoubleProperty();
    private double jerkMaximum;
    private double jerkHomingMaximum;
    private double junction_devation;
    private SWITCH_MODES max_switch_mode = SWITCH_MODES.DISABLED;
    private SWITCH_MODES min_switch_mode = SWITCH_MODES.DISABLED;
    private List allAxis = new ArrayList();
//    private float homing_travel;
//    private float homing_search_velocity;
//    private float homing_latch_velocity;
//    private float homing_zero_offset;
//    private float homing_work_offset;
    private String axis_name;
    private List<Motor> motors = new ArrayList<>();

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
        RADIUS
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
    public void setHomed(boolean choice) {
        homed.set(choice);
    }

   

    public double getLatch_backoff() {
        return formatDoubleValue(latch_backoff, 3);
    }

    public double getRadius() {
        return formatDoubleValue(radius, 3);
    }

//    public void setRadius(int radius) {
//        this.radius = radius;
//    }
    public boolean setLatch_backoff(float latch_backoff) {
        this.latch_backoff = latch_backoff;
        return true;
    }

    public float getLatch_velocity() {
        return (float)formatDoubleValue(latch_velocity, 3);
    }

//    public float getSeek_rate_maximum() {
//        return seek_rate_maximum;
//    }
//
//    public void setSeek_rate_maximum(float seek_rate_maximum) {
//        this.seek_rate_maximum = seek_rate_maximum;
//    }
    public double getSearch_velocity() {
        return formatDoubleValue(searchVelocity, 3);
    }

    public boolean setSearch_velocity(double search_velocity) {
        this.searchVelocity = search_velocity;
        return true;
    }

    public boolean setLatch_velocity(float latch_velocity) {
        this.latch_velocity = latch_velocity;
        return true;
    }

    public double getZero_backoff() {
        return formatDoubleValue(zero_backoff, 3);
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
            Main.print("[!]Invalide Axis Name Specified.\n");
        }
    }

    public AXIS_MODES getAxis_mode() {
        return axis_mode;
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
        return formatDoubleValue(feedRateMaximum, 3);

    }

    public boolean setFeed_rate_maximum(float feed_rate_maximum) {
        this.feedRateMaximum = feed_rate_maximum;
        return true;
    }

    public double getJerkHomingMaximum() {
        return formatDoubleValue(jerkHomingMaximum, 3);
    }

    public void setJerkHomingMaximum(double jerkHomingMaximum) {
        this.jerkHomingMaximum = jerkHomingMaximum;
    }

    public double getJerkMaximum() {
        return formatDoubleValue(jerkMaximum, 3);
    }

    public boolean setJerkMaximum(double jerk_maximum) {
        this.jerkMaximum = jerk_maximum;
        return true;
    }

    public double getJunction_devation() {
        return formatDoubleValue(junction_devation, 6);
    }

    public boolean setJunctionDevation(float junction_devation) {
        this.junction_devation = junction_devation;
        return true;
    }

    public double getMachinePosition() {
        return machine_position;
    }

    public boolean setMachinePosition(float machine_position) {
        this.machine_position = machine_position;
        return true;
    }

    public SimpleDoubleProperty getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset.set(offset);
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

    public SWITCH_MODES getMaxSwitchMode() {
        return max_switch_mode;
    }

    public SWITCH_MODES getMinSwitchMode() {
        return min_switch_mode;
    }

    public Boolean setMaxSwitchMode(int _sw_mode) {

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

    

    private double formatDoubleValue(double val, int decimals) {
        //Utility Method to cleanly trim doubles for display in the UI
	BigDecimal bd = new BigDecimal(val);
	bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //Travel Max Code
    public SimpleDoubleProperty getTravelMaxSimple() {
        return (travel_maximum);
    }
    
    public double getTravel_maximum() {
        return formatDoubleValue(travel_maximum.getValue(), 3);
    }
    
    public void setTravel_min(double travel_min){
        this.travel_min.set(travel_min);
    }

    public boolean setTravel_maximum(float travel_maximum) {
        try {
            //Stub to always track the largest travel axis
            allAxis = TinygDriver.getInstance().machine.getAllLinearAxis();
            Iterator<Axis> iterator = allAxis.iterator();
            double _maxTravel = 0;
            Axis _ax;

            while (iterator.hasNext()) {
                _ax = (Axis) iterator.next();
                if (_ax.getTravel_maximum() > _maxTravel) {
                    //This is the largest travel max so far.. lets set it.
                    _maxTravel = _ax.getTravel_maximum();
                }
                TinygDriver.getInstance().machine.longestTravelAxisValue.set(_maxTravel); //We set this binding now to the largest value
            }
            this.travel_maximum.set(travel_maximum);
            return true;
        } catch (Exception ex) {
            //supress this error
            return false;
        }
    }

    //Travel Min Code
    public SimpleDoubleProperty getTravelMinimple() {
        return (travel_maximum);
    }
    
    public double getTravel_min() {
        return formatDoubleValue(travel_min.getValue(), 3);
    }


    
    public double getVelocityMaximum() {
        return formatDoubleValue(velocityMaximum, 3);
    }

    public boolean setVelocityMaximum(double velocity_maximum) {
        this.velocityMaximum = velocity_maximum;
        return true;
    }

    public SimpleDoubleProperty getWorkPosition() {
        return workPosition;
    }

    public void setWorkPosition(double workpos) {
        this.workPosition.set(workpos);
    }

    public void setMachinePosition(double workpos) {
        this.machinePosition.set(workpos);
    }

    public SimpleDoubleProperty getMachinePositionSimple() {
        return machinePosition;
    }

    public void applyJsonAxisSetting(responseCommand rc) {
        _applyJsonAxisSetting(rc);
    }

    //This is the main method to parser a JSON Axis object
    public void applyJsonAxisSetting(JSONObject js, String parent) {
//        logger.info("Applying JSON Object to " + parent + " Group");
        Iterator ii = js.keySet().iterator();
        try {
            while (ii.hasNext()) {
                String _key = ii.next().toString();
                String _val = js.get(_key).toString();
                responseCommand rc = new responseCommand(parent, _key, _val);
                _applyJsonAxisSetting(rc);
            }

        } catch (JSONException | NumberFormatException ex) {
            logger.error("Error in applyJsonSystemSetting in Axis");
        }

    }

    private void _applyJsonAxisSetting(responseCommand rc) {


        switch (rc.getSettingKey()) {
            case (MnemonicManager.MNEMONIC_AXIS_AXIS_MODE):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingParent()).setAxis_mode(Double.valueOf(rc.getSettingValue()).intValue());
//                logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (MnemonicManager.MNEMONIC_AXIS_FEEDRATE_MAXIMUM):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingParent()).setFeed_rate_maximum(Float.valueOf(rc.getSettingValue()));
//                logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (MnemonicManager.MNEMONIC_AXIS_JERK_MAXIMUM):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingParent()).setJerkMaximum(Float.valueOf(rc.getSettingValue()));
//                logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (MnemonicManager.MNEMONIC_AXIS_JUNCTION_DEVIATION):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingParent()).setJunctionDevation(Float.valueOf(rc.getSettingValue()));
//                logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (MnemonicManager.MNEMONIC_AXIS_LATCH_BACKOFF):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingParent()).setLatch_backoff(Float.valueOf(rc.getSettingValue()));
//                logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (MnemonicManager.MNEMONIC_AXIS_LATCH_VELOCITY):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingParent()).setLatch_velocity(Float.valueOf(rc.getSettingValue()));
//                logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (MnemonicManager.MNEMONIC_AXIS_MAX_SWITCH_MODE):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingParent()).setMaxSwitchMode(Integer.valueOf(rc.getSettingValue()));
//                logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (MnemonicManager.MNEMONIC_AXIS_MIN_SWITCH_MODE):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingParent()).setMinSwitch_mode(Integer.valueOf(rc.getSettingValue()));
//                logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (MnemonicManager.MNEMONIC_AXIS_RADIUS):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingParent()).setRadius(Float.valueOf(rc.getSettingValue()));
//                logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (MnemonicManager.MNEMONIC_AXIS_SEARCH_VELOCITY):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingParent()).setSearch_velocity(Float.valueOf(rc.getSettingValue()));
//                logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (MnemonicManager.MNEMONIC_AXIS_TRAVEL_MIN):
                 TinygDriver.getInstance().machine.getAxisByName(rc.getSettingParent()).setTravel_min(Float.valueOf(rc.getSettingValue()));
//                logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;
                
            case (MnemonicManager.MNEMONIC_AXIS_TRAVEL_MAXIMUM):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingParent()).setTravel_maximum(Float.valueOf(rc.getSettingValue()));
//                logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (MnemonicManager.MNEMONIC_AXIS_VELOCITY_MAXIMUM):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingParent()).setVelocityMaximum(Float.valueOf(rc.getSettingValue()));
//                logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (MnemonicManager.MNEMONIC_AXIS_ZERO_BACKOFF):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingParent()).setZero_backoff(Float.valueOf(rc.getSettingValue()));
//                logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;
            case (MnemonicManager.MNEMONIC_AXIS_JERK_HOMING):
                TinygDriver.getInstance().machine.getAxisByName(rc.getSettingParent()).setJerkHomingMaximum(Float.valueOf(rc.getSettingValue()));
//                logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;
            default:
                logger.info("Default Switch in Axis Class - _applyJsonSystemSetting" + rc.getSettingKey() + " : " + rc.getSettingValue());
        }
    }
}