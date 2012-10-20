/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.system;

/**
 *
 * @author ril3y
 */
public class Motor {
    
    private String CURRENT_MOTOR_JSON_OBJECT;
    private int id_number; //On TinyG the motor ports are 1-4
    private int ma;// map_to_axis
    private int mi; //Microsteps
    private float sa;
    private float tr;
    private boolean po;
    private boolean pm;

    
    
    /**
     *
     * What TinyG Motor Class Looks Like. 2/1/2012 [1ma] m1_map_to_axis 0 [0=X,
     * 1=Y...] [1sa] m1_step_angle 1.800 deg [1tr] m1_travel_per_revolution
     * 5.080 mm [1mi] m1_microsteps 8 [1,2,4,8] [1po] m1_polarity 1 [0,1] [1pm]
     * m1_power_management 1 [0,1]
     *
     */
    public Motor(int id) {
        id_number = id;
    }

    public String getCURRENT_MOTOR_JSON_OBJECT() {
        return CURRENT_MOTOR_JSON_OBJECT;
    }

    public void setCURRENT_MOTOR_JSON_OBJECT(String CURRENT_MOTOR_JSON_OBJECT) {
        this.CURRENT_MOTOR_JSON_OBJECT = CURRENT_MOTOR_JSON_OBJECT;
    }

    
    //Small wrappers to return int's vs bools
    public int isPolarityInt() {
        if (isPolarity() == true) {
            return (1);
        } else {
            return (0);
        }
    }
    //Small wrappers to return int's vs bools

    public int isPower_managementInt() {
        if (isPower_management() == true) {
            return (1);
        } else {
            return (0);
        }
    }

    public int getId_number() {
        return id_number;
    }

    public void setId_number(int id_number) {
        this.id_number = id_number;
    }

    public int getMapToAxis() {
        return ma;
    }

    public void setMapToAxis(int m) {
        ma = m;
    }

    public void setMicrosteps(int ms) {

        mi = ms;
    }

    public int getMicrosteps() {
        //This is really ugly looking but this is how it works with combo boxes or selection models.. ugh
        switch (mi) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 4:
                return 2;
            case 8:
                return 3;
            default:
                return 1;
        }
    }

    public boolean isPolarity() {
        return po;
    }

    public void setPolarity(boolean polarity) {
        this.po = polarity;
    }
    
    public void setPolarity(int polarity) {
        if(polarity == 0){
            this.po = false;
        }else{
            this.po = true;
        }
    }
    

    public boolean isPower_management() {
        return pm;
    }

    
    public void setPower_management(int power_management) {
        if(power_management == 0){
            this.pm = false;
        }else{
            this.pm = true;
        }
    }

    
    public void setPower_management(boolean power_management) {
        this.pm = power_management;
    }

    public float getStep_angle() {
        return sa;
    }

    public void setStep_angle(float step_angle) {
        this.sa = step_angle;
    }

    public float getTravel_per_revolution() {
        return tr;
    }

    public void setTravel_per_revolution(float travel_per_revolution) {
        this.tr = travel_per_revolution;
    }
}
