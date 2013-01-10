/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.tinyg;

import java.util.ArrayList;
import tgfx.tinyg.TinygDriver;

/**
 *
 * @author ril3y
 */
public class MnemonicManager {
    //Group holder Mnemonics

    private static final ArrayList<String> AXIS_MNEMONICS = new ArrayList<>();
    private static final ArrayList<String> SYS_MNEMONICS = new ArrayList<>();
    private static final ArrayList<String> MOTOR_MNEMONICS = new ArrayList<>();
    private static final ArrayList<String> STATUS_MNEMONICS = new ArrayList<>(); //is this needed?
    //Group Mnemonics
    public static final String MNEMONIC_GROUP_SYSTEM = "sys";
    public static final String MNEMONIC_GROUP_EMERGENCY_SHUTDOWN = "er";
    public static final String MNEMONIC_GROUP_STATUS_REPORT = "sr";
    public static final String MNEMONIC_GROUP_HOME = "hom";
    public static final String MNEMONIC_GROUP_POS = "pos";
    public static final String MNEMONIC_GROUP_MOTOR_1 = "1";
    public static final String MNEMONIC_GROUP_MOTOR_2 = "2";
    public static final String MNEMONIC_GROUP_MOTOR_3 = "3";
    public static final String MNEMONIC_GROUP_MOTOR_4 = "4";
    public static final String MNEMONIC_GROUP_AXIS_X = "x";
    public static final String MNEMONIC_GROUP_AXIS_Y = "y";
    public static final String MNEMONIC_GROUP_AXIS_Z = "z";
    public static final String MNEMONIC_GROUP_AXIS_A = "a";
    public static final String MNEMONIC_GROUP_AXIS_B = "b";
    public static final String MNEMONIC_GROUP_AXIS_C = "c";
    //AXIS Mnemonics
    public static final String MNEMONIC_AXIS_AXIS_MODE = "am";
    public static final String MNEMONIC_AXIS_VELOCITY_MAXIMUM = "vm";
    public static final String MNEMONIC_AXIS_FEEDRATE_MAXIMUM = "fr";
    public static final String MNEMONIC_AXIS_TRAVEL_MAXIMUM = "tm";
    public static final String MNEMONIC_AXIS_JERK_MAXIMUM = "jm";
    public static final String MNEMONIC_AXIS_JUNCTION_DEVIATION = "jd";
    public static final String MNEMONIC_AXIS_MAX_SWITCH_MODE = "sx";
    public static final String MNEMONIC_AXIS_MIN_SWITCH_MODE = "sn";
    public static final String MNEMONIC_AXIS_SEARCH_VELOCITY = "sv";
    public static final String MNEMONIC_AXIS_LATCH_VELOCITY = "lv";
    public static final String MNEMONIC_AXIS_LATCH_BACKOFF = "lb";
    public static final String MNEMONIC_AXIS_ZERO_BACKOFF = "zb";
    public static final String MNEMONIC_AXIS_RADIUS = "ra";
    //MOTOR Mnemonics
    public static final String MNEMONIC_MOTOR_MAP_AXIS = "ma";
    public static final String MNEMONIC_MOTOR_STEP_ANGLE = "sa";
    public static final String MNEMONIC_MOTOR_TRAVEL_PER_REVOLUTION = "tr";
    public static final String MNEMONIC_MOTOR_MICROSTEPS = "mi";
    public static final String MNEMONIC_MOTOR_POLARITY = "po";
    public static final String MNEMONIC_MOTOR_POWER_MANAGEMENT = "pm";
    //Status Report
    public static final String MNEMONIC_STATUS_REPORT_LINE = "line";
    public static final String MNEMONIC_STATUS_REPORT_POSX = "posx";
    public static final String MNEMONIC_STATUS_REPORT_POSY = "posy";
    public static final String MNEMONIC_STATUS_REPORT_POSZ = "posz";
    public static final String MNEMONIC_STATUS_REPORT_POSA = "posa";
    public static final String MNEMONIC_STATUS_REPORT_VELOCITY = "vel";
    public static final String MNEMONIC_STATUS_REPORT_MOTION_MODE = "momo";
    public static final String MNEMONIC_STATUS_REPORT_STAT = "stat";
    //System MNEMONICS
    public static final String MNEMONIC_SYSTEM_FIRMWARE_BUILD = "fb";
    public static final String MNEMONIC_SYSTEM_FIRMWARE_VERSION = "fv";
    public static final String MNEMONIC_SYSTEM_GCODE_PLANE = "gpl";
    public static final String MNEMONIC_SYSTEM_GCODE_UNIT_MODE = "gun";
    public static final String MNEMONIC_SYSTEM_GCODE_COORDINATE_SYSTEM = "gco";
    public static final String MNEMONIC_SYSTEM_GCODE_PATH_CONTROL = "gpa";
    public static final String MNEMONIC_SYSTEM_GCODE_DISANCE_MODE = "gdi";
    public static final String MNEMONIC_SYSTEM_JUNCTION_ACCELERATION = "ja";
    public static final String MNEMONIC_SYSTEM_MIN_LINE_SEGMENT = "ml";
    public static final String MNEMONIC_SYSTEM_MIN_ARC_SEGMENT = "ma";
    public static final String MNEMONIC_SYSTEM_MIN_TIME_SEGMENT = "mt";
    public static final String MNEMONIC_SYSTEM_SWITCH_TYPE = "st";
    public static final String MNEMONIC_SYSTEM_IGNORE_CR = "ic";
    public static final String MNEMONIC_SYSTEM_ENABLE_ECHO = "ee";
    public static final String MNEMONIC_SYSTEM_ENABLE_XON = "ex";
    public static final String MNEMONIC_SYSTEM_QUEUE_REPORTS = "eq";
    public static final String MNEMONIC_SYSTEM_ENABLE_JSON_MODE = "ej";
    public static final String MNEMONIC_SYSTEM_JSON_VOBERSITY = "jv";
    public static final String MNEMONIC_SYSTEM_TEXT_VOBERSITY = "tv";
    public static final String MNEMONIC_SYSTEM_STATUS_REPORT_INTERVAL = "si";
    public static final String MNEMONIC_SYSTEM_BAUDRATE = "baud";

    public MnemonicManager() {
        //When new settings are added we need to add them to the this class.
        //Axis
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_AXIS_MODE);
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_FEEDRATE_MAXIMUM);
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_JERK_MAXIMUM);
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_JUNCTION_DEVIATION);
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_LATCH_BACKOFF);
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_LATCH_VELOCITY);
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_MAX_SWITCH_MODE);
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_MIN_SWITCH_MODE);
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_RADIUS);
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_SEARCH_VELOCITY);
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_TRAVEL_MAXIMUM);
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_VELOCITY_MAXIMUM);
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_ZERO_BACKOFF);
        //Motor
        MOTOR_MNEMONICS.add(MNEMONIC_MOTOR_MAP_AXIS);
        MOTOR_MNEMONICS.add(MNEMONIC_MOTOR_MICROSTEPS);
        MOTOR_MNEMONICS.add(MNEMONIC_MOTOR_POLARITY);
        MOTOR_MNEMONICS.add(MNEMONIC_MOTOR_POWER_MANAGEMENT);
        MOTOR_MNEMONICS.add(MNEMONIC_MOTOR_STEP_ANGLE);
        MOTOR_MNEMONICS.add(MNEMONIC_MOTOR_TRAVEL_PER_REVOLUTION);
        //SYS
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_BAUDRATE);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_ENABLE_ECHO);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_ENABLE_JSON_MODE);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_ENABLE_XON);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_FIRMWARE_BUILD);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_FIRMWARE_VERSION);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_GCODE_COORDINATE_SYSTEM);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_GCODE_DISANCE_MODE);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_GCODE_PATH_CONTROL);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_GCODE_PATH_CONTROL);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_GCODE_PLANE);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_GCODE_UNIT_MODE);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_IGNORE_CR);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_JSON_VOBERSITY);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_JUNCTION_ACCELERATION);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_MIN_ARC_SEGMENT);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_MIN_LINE_SEGMENT);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_MIN_TIME_SEGMENT);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_QUEUE_REPORTS);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_STATUS_REPORT_INTERVAL);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_SWITCH_TYPE);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_TEXT_VOBERSITY);




    }

    public responseCommand lookupSingleGroup(String strToLookup) {
        //This will iterate all group mnemoics to see if the single group object
        // belongs in which group.

        responseCommand rc = new responseCommand();


    
        
        if (AXIS_MNEMONICS.contains(strToLookup.substring(1))) {
            rc.setSettingParent(String.valueOf(strToLookup.charAt(0)));
            rc.setSettingKey(strToLookup.substring(1));
            return (rc);
        } else if (MOTOR_MNEMONICS.contains(strToLookup.substring(1))) {
            rc.setSettingParent(String.valueOf(strToLookup.charAt(0)));
            rc.setSettingKey(strToLookup.substring(1));
            return (rc);
        }else if(SYS_MNEMONICS.contains(strToLookup)){
            rc.setSettingParent(MNEMONIC_GROUP_SYSTEM);
            rc.setSettingKey(strToLookup);
            return (rc);
        }
//        for (String s : AXIS_MNEMONICS) {
//            if (s.equals(strToLookup)) {
//                rc.setSettingParent(s);
//                rc.setSettingKey(s.substring(1));
//                return (rc);
//            }
//        }
//        for (Integer i = 1; i != TinygDriver.getInstance().m.getNumberOfMotors(); i++) { //iterate the motors
//            for (String s : MOTOR_MNEMONICS) {
//                if ((String.valueOf(i) + s).equals(strToLookup)) {
//                    rc.setSettingParent(i.toString());
//                    rc.setSettingKey(s.substring(1));
//                    return (rc); //return the motor number as the group
//                }
//            }
//        }
//
//        for (String s : SYS_MNEMONICS) {
//            if (s.equals(strToLookup)) {
//                rc.setSettingParent(s.toString());
//                rc.setSettingKey(s.substring(1));
//                return (rc);
//            }
//        }
        return null;
    }
}
