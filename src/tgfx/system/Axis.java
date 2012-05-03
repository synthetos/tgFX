/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.system;

import java.util.ArrayList;
import java.util.List;

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
    private AXIS_TYPE axis_type;
    private float latch_velocity;
    private float latch_backoff;
    private float zero_backoff;
    private float machine_position;
    private float work_position;
    private AXIS_MODES axis_mode;
    private float feed_rate_maximum;
    private float velocity_maximum;
    private float travel_maximum;
    private double jerk_maximum;
    private float junction_devation;
    private int switch_mode;
//    private float homing_travel;
//    private float homing_search_velocity;
//    private float homing_latch_velocity;
//    private float homing_zero_offset;
//    private float homing_work_offset;
    private String axis_name;
    private List<Motor> motors = new ArrayList<Motor>();

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

    public Axis() {
        //
    }

    public float getLatch_backoff() {
        return latch_backoff;
    }

    public void setLatch_backoff(float latch_backoff) {
        this.latch_backoff = latch_backoff;
    }

    public float getLatch_velocity() {
        return latch_velocity;
    }

    public void setLatch_velocity(float latch_velocity) {
        this.latch_velocity = latch_velocity;
    }

    public float getZero_backoff() {
        return zero_backoff;
    }

    public void setZero_backoff(float zero_backoff) {
        this.zero_backoff = zero_backoff;
    }

    public void setRadius(float r) {
        this.setRadius(r);
    }

    public void setAxisType(AXIS_TYPE at) {
        this.axis_type = at;
    }

    public AXIS_TYPE getAxisType() {
        return (this.axis_type);
    }

    public Axis(AXIS ax, AXIS_TYPE at) {
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

    public void setAxis_mode(int axMode) {
        
        switch(axMode){
            case 0:
                this.axis_mode = AXIS_MODES.DISABLE;
                break;
            case 1:
                this.axis_mode = AXIS_MODES.STANDARD;
                break;
            case 2:
                this.axis_mode = AXIS_MODES.INHIBITED;
                break;
            case 3:
                this.axis_mode = AXIS_MODES.RADIUS;
                break;
            case 4:
                this.axis_mode = AXIS_MODES.SLAVE_X;
                break;
            case 5:
                this.axis_mode = AXIS_MODES.SLAVE_Y;
                break;
            case 6:
                this.axis_mode = AXIS_MODES.SLAVE_Z;
                break;
            case 7:
                this.axis_mode = AXIS_MODES.SLAVE_XY;
                break;
            case 8:
                this.axis_mode = AXIS_MODES.SLAVE_XZ;
                break;
            case 9:
                this.axis_mode = AXIS_MODES.SLAVE_YZ;
                break;
            case 10:
                this.axis_mode = AXIS_MODES.SLAVE_XYZ;
                break;
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

    public float getFeed_rate_maximum() {
        return feed_rate_maximum;
    }

    public void setFeed_rate_maximum(float feed_rate_maximum) {
        this.feed_rate_maximum = feed_rate_maximum;
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

    public void setJerk_maximum(double jerk_maximum) {
        this.jerk_maximum = jerk_maximum;
    }

    public float getJunction_devation() {
        return junction_devation;
    }

    public void setJunction_devation(float junction_devation) {
        this.junction_devation = junction_devation;
    }

    public float getMachine_position() {
        return machine_position;
    }

    public void setMachine_position(float machine_position) {
        this.machine_position = machine_position;
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

    public int getSwitch_mode() {
        return switch_mode;
    }

    public void setSwitch_mode(int switch_mode) {
        this.switch_mode = switch_mode;
    }

    public float getTravel_maximum() {
        return travel_maximum;
    }

    public void setTravel_maximum(float travel_maximum) {
        this.travel_maximum = travel_maximum;
    }

    public float getVelocity_maximum() {
        return velocity_maximum;
    }

    public void setVelocity_maximum(float velocity_maximum) {
        this.velocity_maximum = velocity_maximum;
    }

    public float getWork_position() {
        return work_position;
    }

    public void setWork_position(float work_position) {
        this.work_position = work_position;
    }
}
