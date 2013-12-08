/*
 * Copyright (C) 2013 Synthetos LLC. All Rights reserved.
 */
package tgfx.system;

import java.io.IOException;
import java.util.List;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.json.JSONException;
import org.json.JSONObject;
import tgfx.tinyg.responseCommand;

/**
 * The <code>Machine</code> interface defines a physical CNC machine
 * @author pfarrell, riley
 * Created on Dec 6, 2013 4:14:25 PM
 */
public interface Machine {

    void applyJsonStatusReport(responseCommand rc);

    //This is the main method to parser a JSON sys object
    void applyJsonSystemSetting(JSONObject js, String parent) throws IOException;

    /**
     * @return the a
     */
    Axis getA();

    /**
     * @return the ajoggingIncrement
     */
    SimpleIntegerProperty getAjoggingIncrement();

    List<Axis> getAllAxis();

    List<Axis> getAllLinearAxis();

    /**
     * @return the axis
     */
    List<Axis> getAxis();

    Axis getAxisByName(char c);

    Axis getAxisByName(String name);

    /**
     * @return the b
     */
    Axis getB();

    /**
     * @return the c
     */
    Axis getC();

    //    public void setCoordinateSystem(String cord) {
    //        setCoordinate_mode(Integer.valueOf(cord));
    //
    //    }
    SimpleStringProperty getCoordinateSystem();

    GcodeCoordinateSystem getCoordinateSystemByName(String name);

    GcodeCoordinateSystem getCoordinateSystemByNumberMnemonic(int number);

    GcodeCoordinateSystem getCoordinateSystemByTgNumber(int number);

    SimpleDoubleProperty getFirmwareBuild();

    double getFirmwareBuildValue();

    StringProperty getFirmwareVersion();

    /**
     * @return the gcm
     */
    GcodeCoordinateManager getGcm();

    /**
     * @return the gcodeCoordinateSystems
     */
    List<GcodeCoordinateSystem> getGcodeCoordinateSystems();

    /**
     * @return the gcodeUnitDivision
     */
    SimpleDoubleProperty getGcodeUnitDivision();

    StringProperty getGcodeUnitMode();

    int getGcodeUnitModeAsInt();

    Gcode_distance_mode getGcode_distance_mode();

    Gcode_path_control getGcode_path_control();

    /**
     * @return the gcode_select_coord_system
     */
    Gcode_coord_system getGcode_select_coord_system();

    Gcode_select_plane getGcode_select_plane();

    StringProperty getHardwareId();

    /**
     * @return the hardwarePlatform
     */
    SimpleDoubleProperty getHardwarePlatform();

    StringProperty getHardwareVersion();

    /**
     * @return the ignore_cr_lf_RX
     */
    Ignore_CR_LF_ON_RX getIgnore_cr_lf_RX();

    Ignore_CR_LF_ON_RX getIgnore_cr_lf_TX();

    double getJoggingIncrementByAxis(String _axisName);

    float getJunction_acceleration();

    String getLast_message();

    int getLineNumber();

    SimpleIntegerProperty getLineNumberSimple();

    /**
     * @return the longestTravelAxisValue
     */
    SimpleDoubleProperty getLongestTravelAxisValue();

    /**
     * @return the m_mode
     */
    SimpleStringProperty getM_mode();

    /**
     * @return the m_state
     */
    SimpleStringProperty getM_state();

    String getName();

    SimpleStringProperty getMachineState();

    float getMin_arc_segment();

    float getMin_line_segment();

    double getMin_segment_time();

    SimpleStringProperty getMotionMode();

    int getMotorAxis(Motor m);

    Motor getMotorByNumber(String m);

    Motor getMotorByNumber(int i);

    List<Motor> getMotors();

    int getNumberOfMotors();

    int getStatus_report_interval();

    switchNoNc getSwitchType();

    SimpleDoubleProperty getVelocity();

    Double getVelocityValue();

    /**
     * @return the x
     */
    Axis getX();

    /**
     * @return the xjoggingIncrement
     */
    SimpleIntegerProperty getXjoggingIncrement();

    /**
     * @return the y
     */
    Axis getY();

    /**
     * @return the yjoggingIncrement
     */
    SimpleIntegerProperty getYjoggingIncrement();

    /**
     * @return the z
     */
    Axis getZ();

    /**
     * @return the zjoggingIncrement
     */
    SimpleIntegerProperty getZjoggingIncrement();

    boolean isEnable_CR_on_TX();

    boolean isEnable_acceleration();

    boolean isEnable_echo();

    boolean isEnable_hashcode();

    boolean isEnable_xon_xoff();

    /**
     * @param a the a to set
     */
    void setA(Axis a);

    /**
     * @param ajoggingIncrement the ajoggingIncrement to set
     */
    void setAjoggingIncrement(SimpleIntegerProperty ajoggingIncrement);

    /**
     * @param axis the axis to set
     */
    void setAxis(List<Axis> axis);

    /**
     * @param b the b to set
     */
    void setB(Axis b);

    /**
     * @param c the c to set
     */
    void setC(Axis c);

    /**
     * @param coordinateSystem the coordinateSystem to set
     */
    void setCoordinateSystem(SimpleStringProperty coordinateSystem);

    void setEnable_CR_on_TX(boolean enable_CR_on_TX);

    void setEnable_acceleration(boolean enable_acceleration);

    void setEnable_echo(boolean enable_echo);

    void setEnable_hashcode(boolean enable_hashcode);

    void setEnable_xon_xoff(boolean enable_xon_xoff);

    /**
     * @param firmwareBuild the firmwareBuild to set
     */
    void setFirmwareBuild(SimpleDoubleProperty firmwareBuild);

    void setFirmwareBuild(double firmware_build) throws IOException, JSONException;

    /**
     * @param firmwareVersion the firmwareVersion to set
     */
    void setFirmwareVersion(StringProperty firmwareVersion);

    void setFirmwareVersion(String fv);

    /**
     * @param gcm the gcm to set
     */
    void setGcm(GcodeCoordinateManager gcm);

    /**
     * @param gcodeCoordinateSystems the gcodeCoordinateSystems to set
     */
    void setGcodeCoordinateSystems(List<GcodeCoordinateSystem> gcodeCoordinateSystems);

    void setGcodeDistanceMode(String gdm);

    void setGcodeDistanceMode(int gdm);

    void setGcodePathControl(String gpc);

    void setGcodePathControl(int gpc);

    void setGcodeSelectPlane(String gsp);

    void setGcodeSelectPlane(int gsp);

    /**
     * @param gcodeUnitDivision the gcodeUnitDivision to set
     */
    void setGcodeUnitDivision(SimpleDoubleProperty gcodeUnitDivision);

    /**
     * @param gcodeUnitMode the gcodeUnitMode to set
     */
    void setGcodeUnitMode(StringProperty gcodeUnitMode);

    void setGcodeUnits(int unitMode);

    void setGcodeUnits(String gcu);

    /**
     * @param gcode_distance_mode the gcode_distance_mode to set
     */
    void setGcode_distance_mode(Gcode_distance_mode gcode_distance_mode);

    /**
     * @param gcode_path_control the gcode_path_control to set
     */
    void setGcode_path_control(Gcode_path_control gcode_path_control);

    /**
     * @param gcode_select_coord_system the gcode_select_coord_system to set
     */
    void setGcode_select_coord_system(Gcode_coord_system gcode_select_coord_system);

    void setGcode_select_plane(Gcode_select_plane gcode_select_plane);

    /**
     * @param hardwareId the hardwareId to set
     */
    void setHardwareId(StringProperty hardwareId);

    //    public SimpleDoubleProperty over(){
    //        return(gcodeUnitDivision.divide(2));
    //    }
    void setHardwareId(String hwIdString);

    /**
     * @param hardwarePlatform the hardwarePlatform to set
     */
    void setHardwarePlatform(SimpleDoubleProperty hardwarePlatform);

    /**
     * @param hardwareVersion the hardwareVersion to set
     */
    void setHardwareVersion(StringProperty hardwareVersion);

    void setHardwareVersion(String hardwareVersion);

    /**
     * @param ignore_cr_lf_RX the ignore_cr_lf_RX to set
     */
    void setIgnore_cr_lf_RX(Ignore_CR_LF_ON_RX ignore_cr_lf_RX);

    void setIgnore_cr_lf_RX(int ignore_cr_lf_RX);

    void setIgnore_cr_lf_TX(Ignore_CR_LF_ON_RX ignore_cr_lf_TX);

    void setJunction_acceleration(float junction_acceleration);

    void setLast_message(String last_message);

    /**
     * @param lineNumber the lineNumber to set
     */
    void setLineNumber(SimpleIntegerProperty lineNumber);

    void setLineNumber(int lineNumber);

    /**
     * @param longestTravelAxisValue the longestTravelAxisValue to set
     */
    void setLongestTravelAxisValue(SimpleDoubleProperty longestTravelAxisValue);

    /**
     * @param m_mode the m_mode to set
     */
    void setM_mode(SimpleStringProperty m_mode);

    /**
     * @param m_state the m_state to set
     */
    void setM_state(SimpleStringProperty m_state);

    void setMachineState(int state);

    void setMin_arc_segment(float min_arc_segment);

    void setMin_line_segment(float min_line_segment);

    void setMin_segment_time(double min_segment_time);

    void setMotionMode(int mode);

    void setMotorAxis(int motorNumber, int x);

    /**
     * @param motors the motors to set
     */
    void setMotors(List<Motor> motors);

    void setName(String machineName);

    void setStatus_report_interval(int status_report_interval);

    void setSwitchType(switchNoNc swType);
    void setSwitchType(int swType);
    

    /**
     * @param velocity the velocity to set
     */
    void setVelocity(SimpleDoubleProperty velocity);

    void setVelocity(double vel);

    /**
     * @param x the x to set
     */
    void setX(Axis x);

    /**
     * @param xjoggingIncrement the xjoggingIncrement to set
     */
    void setXjoggingIncrement(SimpleIntegerProperty xjoggingIncrement);

    /**
     * @param y the y to set
     */
    void setY(Axis y);

    /**
     * @param yjoggingIncrement the yjoggingIncrement to set
     */
    void setYjoggingIncrement(SimpleIntegerProperty yjoggingIncrement);

    /**
     * @param z the z to set
     */
    void setZ(Axis z);

    /**
     * @param zjoggingIncrement the zjoggingIncrement to set
     */
    void setZjoggingIncrement(SimpleIntegerProperty zjoggingIncrement);
    
    enum Ignore_CR_LF_ON_RX {
        OFF, //OFF means neither is ignored on RX
        CR, LF
    }

    enum Gcode_unit_modes {
        //gun
        inches, //G20
        mm      //G21
    };

    enum Gcode_select_plane {
        //gpl

        XY, //G17
        XZ, //G18
        YZ  //G19
    }

    enum Gcode_distance_mode {
        //gdi
        ABSOLUTE, //G90
        INCREMENTAL   //91
    }
    enum Gcode_path_control {
        //gpl
        G61,
        G61POINT1,
        G64
    }

    enum Gcode_coord_system {
        //gco
        G54, G55, G56, G57, G58, G59
    }

    enum selection_plane {
        G17, G18, G19
    };
   /**
    * Switch type Normally Open (no) or normally closed (nc)
    * this is switchType, but that name is used in too many places for me (pat) to change it without
    * understanding more of the code.
    */ 
    enum switchNoNc {    //0=normally closed 1 = normally open
         normallyClosed,
         normallyOpen;
         
        @Override
        public String toString() {
            String rval = (this == switchNoNc.normallyClosed) ? "Normally Closed" :  "Normally Open";
            return rval;
        }
        public static switchNoNc valueMatching(int val) {
            switchNoNc rval = normallyClosed;
            for (switchNoNc s : values()) {
                if (s.ordinal() == val) {
                    rval = s;
                    break;
                }
            }
            return rval;
        }
    }
}
