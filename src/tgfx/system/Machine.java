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
     *  [fc]  config_version             0.00
        [fv]  firmware_version           0.93
        [fv]  firmware_build           329.38
        [ln]  line_number       0
        [ms]  machine_state 0
        [vl]  velocity    0.000  mm/min
        [gi]  gcode_inches_mode         G21 [20,21]
        [gs]  gcode_select_plane        G17 [17,18,19]
        [gp]  gcode_path_control        G64.0 [61,61.1,64]
        [ga]  gcode_absolute_mode       G90 [90,91]
        [ea]  enable_acceleration         1 [0,1]
        [ja]  corner_acceleration    200000 mm
        [ml]  min_line_segment            0.080 mm
        [ma]  min_arc_segment             0.100 mm
        [mt]  min_segment_time        10000 uSec
        [ic]  ignore_CR (on RX)           0 [0,1]
        [il]  ignore_LF (on RX)           0 [0,1]
        [ec]  enable_CR (on TX)           0 [0,1]
        [ee]  enable_echo                 1 [0,1]
        [ex]  enable_xon_xoff             1 [0,1]
     */
    //TG Specific
    private float config_version;
    private float firmware_version;
    private float firmware_build;
    private int status_report_interval;
    private int line_number;
    private int machine_state;
    private float velocity;
    private enum unit_mode{ INCHES, MM };
    private enum selection_plane{G17, G18, G19};
    private boolean enable_acceleration;
    private int corner_acceleration;
    private float min_line_segment;
    private float min_arc_segment;
    private int min_segment_time;
    
    private boolean ignore_CR;
    private boolean ignore_LF;
    private boolean enable_CR;
    private boolean enable_echo;
    private boolean enable_xon_xoff;
    
    private String flow = "OK"; //Internal tracking for the msg OK
    
    
    
    private List<Motor> motors = new ArrayList<Motor>();
    private List<Axis> axis = new ArrayList<Axis>();

    public List<Motor> getMotors(){
        return(this.motors);
    }
    
    //TG Composer Specific
    private String machineName;

    public String getMachineName() {
        return machineName;
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

    public float getConfig_version() {
        return config_version;
    }

    public synchronized void setFlow(String f){
        this.flow = f;
    }
    
    public synchronized String getFlow(){
        return this.flow;
    }
    public void setConfig_version(float config_version) {
        this.config_version = config_version;
    }

    public int getCorner_acceleration() {
        return corner_acceleration;
    }

    public void setCorner_acceleration(int corner_acceleration) {
        this.corner_acceleration = corner_acceleration;
    }

    public boolean isEnable_CR() {
        return enable_CR;
    }

    public void setEnable_CR(boolean enable_CR) {
        this.enable_CR = enable_CR;
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

    public boolean isIgnore_CR() {
        return ignore_CR;
    }

    public void setIgnore_CR(boolean ignore_CR) {
        this.ignore_CR = ignore_CR;
    }

    public boolean isIgnore_LF() {
        return ignore_LF;
    }

    public void setIgnore_LF(boolean ignore_LF) {
        this.ignore_LF = ignore_LF;
    }

    public int getLine_number() {
        return line_number;
    }

    public void setLine_number(int line_number) {
        this.line_number = line_number;
    }

    public int getMachine_state() {
        return machine_state;
    }

    public void setMachine_state(int machine_state) {
        this.machine_state = machine_state;
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

    public int getMin_segment_time() {
        return min_segment_time;
    }

    public void setMin_segment_time(int min_segment_time) {
        this.min_segment_time = min_segment_time;
    }

    public float getVelocity() {
        return velocity;
    }

    public void setVelocity(float velocity) {
        this.velocity = velocity;
    }
    
    
    
    public Machine(){
        this.setFlow("OK");
        for(int i = 0; i < 3; i++){
            Motor m = new Motor(i);
            motors.add(m);
        }
            Axis x = new Axis(Axis.AXIS.X);
            Axis y = new Axis(Axis.AXIS.Y);
            Axis z = new Axis(Axis.AXIS.Z);
            Axis a = new Axis(Axis.AXIS.A);
            
            axis.add(x);
            axis.add(y);
            axis.add(z);
            axis.add(a);
    }

    
    public Axis getAxisByName(String name){
        for(Axis a : axis){
            if(a.getAxis_name() == name){
                return(a);
            }
        }
        return null;
    }
    
    public void parseStatusReport(String line){
        
    }
    
    public Motor getMotorByNumber(int i){
        for(Motor m : motors){
            if(m.getId_number() == i){
                return(m);
            }
        }
        return null;
    }
    
    public int getMotorAxis(Motor m){
        return m.getId_number();
    }
    
    public void setMotorAxis(int motorNumber, int x){
        Motor m = getMotorByNumber(motorNumber);
        m.setMapToAxis(x);
    }
            

}
