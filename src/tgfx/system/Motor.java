/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.system;

import java.util.Iterator;
import org.json.JSONObject;
import org.apache.log4j.Logger;
import tgfx.tinyg.MnemonicManager;
import tgfx.tinyg.TinygDriver;
import tgfx.tinyg.responseCommand;

/**
 *
 * @author ril3y
 */
public class Motor {

    
    static final Logger logger = Logger.getLogger(TinygDriver.class);
    private String CURRENT_MOTOR_JSON_OBJECT;
    private int id_number; //On TinyG the motor ports are 1-4
    private int ma;// map_to_axis
    private int mi; //Microsteps
    private float sa; //step angle
    private float tr; //travel revolution
    private boolean po; //polarity
    private boolean pm; //power management

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
        if (polarity == 0) {
            this.po = false;
        } else {
            this.po = true;
        }
    }

    public boolean isPower_management() {
        return pm;
    }

    public void setPower_management(int power_management) {
        if (power_management == 0) {
            this.pm = false;
        } else {
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

    //This is the main method to parser a JSON Motor object
    public void applyJsonSystemSetting(JSONObject js, String parent) {
        logger.info("Applying JSON Object to " + parent + " Group");
        Iterator ii = js.keySet().iterator();
        try {
            while (ii.hasNext()) {
                String _key = ii.next().toString();
                String _val = js.get(_key).toString();
                responseCommand rc = new responseCommand(parent, _key, _val);

                switch (_key) {
                    case (MnemonicManager.MNEMONIC_MOTOR_MAP_AXIS):
                        TinygDriver.getInstance().getMachine().getMotorByNumber(Integer.valueOf(rc.getSettingParent())).setMapToAxis(Integer.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_MOTOR_MICROSTEPS):
                        TinygDriver.getInstance().getMachine().getMotorByNumber(Integer.valueOf(rc.getSettingParent())).setMicrosteps(Integer.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_MOTOR_POLARITY):
                        TinygDriver.getInstance().getMachine().getMotorByNumber(Integer.valueOf(rc.getSettingParent())).setPolarity(Integer.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_MOTOR_POWER_MANAGEMENT):
                        TinygDriver.getInstance().getMachine().getMotorByNumber(Integer.valueOf(rc.getSettingParent())).setPower_management(Integer.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_MOTOR_STEP_ANGLE):
                        TinygDriver.getInstance().getMachine().getMotorByNumber(Integer.valueOf(rc.getSettingParent())).setStep_angle(Float.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;

                    case (MnemonicManager.MNEMONIC_MOTOR_TRAVEL_PER_REVOLUTION):
                        TinygDriver.getInstance().getMachine().getMotorByNumber(Integer.valueOf(rc.getSettingParent())).setTravel_per_revolution(Float.valueOf(rc.getSettingValue()));
                        logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                        break;
                    default:
                        logger.info("Default Switch");


                }
            }

        } catch (Exception ex) {
            logger.error("Error in applyJsonSystemSetting in Motor");
        }

    }
}
