/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.tinyg;

import java.util.ArrayList;

/**
 *
 * @author ril3y
 */
public class MnemonicManager {
    //Group holder Mnemonics

    private static final ArrayList<String> GROUP_MNEMONICS = new ArrayList<>();
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
    public static final String MNEMONIC_AXIS_JERK_HOMING = "jh";
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
    public static final String MNEMONIC_STATUS_REPORT_POSX = "posx";
    public static final String MNEMONIC_STATUS_REPORT_POSY = "posy";
    public static final String MNEMONIC_STATUS_REPORT_POSZ = "posz";
    public static final String MNEMONIC_STATUS_REPORT_POSA = "posa";
    //Homed Status
    public static final String MNEMONIC_STATUS_REPORT_HOMEDX = "homx";
    public static final String MNEMONIC_STATUS_REPORT_HOMEDY = "homy";
    public static final String MNEMONIC_STATUS_REPORT_HOMEDZ = "homz";
    public static final String MNEMONIC_STATUS_REPORT_HOMEDA = "homa";
    //Machine Positions
    public static final String MNEMONIC_STATUS_REPORT_MACHINEPOSX = "mpox"; //Machine Position
    public static final String MNEMONIC_STATUS_REPORT_MACHINEPOSY = "mpoy"; //Machine Position
    public static final String MNEMONIC_STATUS_REPORT_MACHINEPOSZ = "mpoz"; //Machine Position
    public static final String MNEMONIC_STATUS_REPORT_MACHINEPOSA = "mpoa"; //Machine Position
    //Offsets
    public static final String MNEMONIC_STATUS_REPORT_WORKOFFSETA = "ofsa";
    public static final String MNEMONIC_STATUS_REPORT_WORKOFFSETX = "ofsx";
    public static final String MNEMONIC_STATUS_REPORT_WORKOFFSETY = "ofsy";
    public static final String MNEMONIC_STATUS_REPORT_WORKOFFSETZ = "ofsz";
    //
    public static final String MNEMONIC_STATUS_REPORT_LINE = "line";
    public static final String MNEMONIC_STATUS_REPORT_VELOCITY = "vel";
    public static final String MNEMONIC_STATUS_REPORT_MOTION_MODE = "momo";
    public static final String MNEMONIC_STATUS_REPORT_STAT = "stat";
    public static final String MNEMONIC_STATUS_REPORT_UNIT = "unit";
    public static final String MNEMONIC_STATUS_REPORT_COORDNIATE_MODE = "coor";
    //System MNEMONICS
    public static final String MNEMONIC_SYSTEM_DEFAULT_GCODE_UNIT_MODE = "gun";
    public static final String MNEMONIC_SYSTEM_DEFAULT_GCODE_PLANE = "gpl";
    public static final String MNEMONIC_SYSTEM_DEFAULT_GCODE_COORDINATE_SYSTEM = "gco";
    public static final String MNEMONIC_SYSTEM_DEFAULT_GCODE_PATH_CONTROL = "gpa";
    public static final String MNEMONIC_SYSTEM_DEFAULT_GCODE_DISTANCE_MODE = "gdi";
    public static final String MNEMONIC_SYSTEM_FIRMWARE_BUILD = "fb";
    public static final String MNEMONIC_SYSTEM_SWITCH_TYPE = "st";
    public static final String MNEMONIC_SYSTEM_FIRMWARE_VERSION = "fv";
    public static final String MNEMONIC_SYSTEM_HARDWARD_PLATFORM = "hp";
    public static final String MNEMONIC_SYSTEM_HARDWARE_VERSION = "hv";
    public static final String MNEMONIC_SYSTEM_JUNCTION_ACCELERATION = "ja";
    public static final String MNEMONIC_SYSTEM_MIN_LINE_SEGMENT = "ml";
    public static final String MNEMONIC_SYSTEM_MIN_ARC_SEGMENT = "ma";
    public static final String MNEMONIC_SYSTEM_MIN_TIME_SEGMENT = "mt";
    public static final String MNEMONIC_SYSTEM_IGNORE_CR = "ic";
    public static final String MNEMONIC_SYSTEM_ENABLE_ECHO = "ee";
    public static final String MNEMONIC_SYSTEM_ENABLE_XON = "ex";
    public static final String MNEMONIC_SYSTEM_QUEUE_REPORTS = "eq";
    public static final String MNEMONIC_SYSTEM_ENABLE_JSON_MODE = "ej";
    public static final String MNEMONIC_SYSTEM_JSON_VOBERSITY = "jv";
    public static final String MNEMONIC_SYSTEM_TEXT_VOBERSITY = "tv";
    public static final String MNEMONIC_SYSTEM_STATUS_REPORT_INTERVAL = "si";
    public static final String MNEMONIC_SYSTEM_BAUDRATE = "baud";
//    public static final String MNEMONIC_SYSTEM_LAST_MESSAGE = "msg";
    public static final String MNEMONIC_SYSTEM_EXPAND_LF_TO_CRLF_ON_TX = "ec";
    public static final String MNEMONIC_SYSTEM_CHORDAL_TOLERANCE = "ct";
    public static final String MNEMONIC_SYSTEM_TINYG_ID_VERSION = "id";
    public static final String MNEMONIC_STATUS_REPORT_TINYG_DISTANCE_MODE = "dist";

    public MnemonicManager() {
        //When new settings are added we need to add them to the this class.
        //Axis
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_AXIS_MODE);
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_FEEDRATE_MAXIMUM);
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_JERK_MAXIMUM);
        AXIS_MNEMONICS.add(MNEMONIC_AXIS_JERK_HOMING);
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
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_DEFAULT_GCODE_COORDINATE_SYSTEM);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_DEFAULT_GCODE_DISTANCE_MODE);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_DEFAULT_GCODE_PATH_CONTROL);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_DEFAULT_GCODE_PATH_CONTROL);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_DEFAULT_GCODE_PLANE);

        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_BAUDRATE);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_ENABLE_ECHO);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_ENABLE_JSON_MODE);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_ENABLE_XON);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_FIRMWARE_BUILD);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_FIRMWARE_VERSION);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_HARDWARD_PLATFORM);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_HARDWARE_VERSION);
//        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_GCODE_UNIT_MODE);
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

        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_EXPAND_LF_TO_CRLF_ON_TX);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_DEFAULT_GCODE_UNIT_MODE);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_CHORDAL_TOLERANCE);
        SYS_MNEMONICS.add(MNEMONIC_SYSTEM_TINYG_ID_VERSION);

        //Status Report
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_LINE);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_MOTION_MODE);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_POSA);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_POSX);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_POSY);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_POSZ);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_UNIT);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_VELOCITY);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_STAT);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_COORDNIATE_MODE);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_MACHINEPOSA);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_MACHINEPOSX);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_MACHINEPOSY);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_MACHINEPOSZ);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_TINYG_DISTANCE_MODE);
        //Homed Group
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_HOMEDX);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_HOMEDY);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_HOMEDZ);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_HOMEDA);

        //offsets
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_WORKOFFSETX);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_WORKOFFSETY);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_WORKOFFSETZ);
        STATUS_MNEMONICS.add(MNEMONIC_STATUS_REPORT_WORKOFFSETA);

        //Master Group
        GROUP_MNEMONICS.add(MNEMONIC_GROUP_AXIS_A);
        GROUP_MNEMONICS.add(MNEMONIC_GROUP_AXIS_B);
        GROUP_MNEMONICS.add(MNEMONIC_GROUP_AXIS_C);
        GROUP_MNEMONICS.add(MNEMONIC_GROUP_AXIS_X);
        GROUP_MNEMONICS.add(MNEMONIC_GROUP_AXIS_Y);
        GROUP_MNEMONICS.add(MNEMONIC_GROUP_AXIS_Z);
        GROUP_MNEMONICS.add(MNEMONIC_GROUP_EMERGENCY_SHUTDOWN);
        GROUP_MNEMONICS.add(MNEMONIC_GROUP_HOME);
        GROUP_MNEMONICS.add(MNEMONIC_GROUP_MOTOR_1);
        GROUP_MNEMONICS.add(MNEMONIC_GROUP_MOTOR_2);
        GROUP_MNEMONICS.add(MNEMONIC_GROUP_MOTOR_3);
        GROUP_MNEMONICS.add(MNEMONIC_GROUP_MOTOR_4);
        GROUP_MNEMONICS.add(MNEMONIC_GROUP_POS);
        GROUP_MNEMONICS.add(MNEMONIC_GROUP_STATUS_REPORT);
        GROUP_MNEMONICS.add(MNEMONIC_GROUP_SYSTEM);


    }

    public boolean isMasterGroupObject(String strToLookup) {
        if (GROUP_MNEMONICS.contains(strToLookup)) {
            return true;
        }

        return false;
    }

    public responseCommand lookupSingleGroupMaster(String strToLookup, String parentGroup) {
        //This will iterate all group mnemoics to see if the single group object
        // belongs in which group.

        responseCommand rc = new responseCommand(parentGroup, null, null);

        if (AXIS_MNEMONICS.contains(strToLookup)) {
            rc.setSettingKey(strToLookup);
            return (rc);
        } else if (MOTOR_MNEMONICS.contains(strToLookup)) {
            rc.setSettingKey(strToLookup);
            return (rc);
        } else if (SYS_MNEMONICS.contains(strToLookup)) {
            rc.setSettingKey(strToLookup);
            return (rc);
        } else if (STATUS_MNEMONICS.contains(strToLookup)) {
            rc.setSettingKey(strToLookup);
            return (rc);
        }
        return null;
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
        } else if (SYS_MNEMONICS.contains(strToLookup)) {
            rc.setSettingParent(MNEMONIC_GROUP_SYSTEM);
            rc.setSettingKey(strToLookup);
            return (rc);
        } else if (STATUS_MNEMONICS.contains(strToLookup)) {
            rc.setSettingParent(MNEMONIC_GROUP_STATUS_REPORT);
            rc.setSettingKey(strToLookup);
            return (rc);
        }
        return null;
    }
}
