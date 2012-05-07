/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;
import java.util.List;
import tgfx.system.Machine;
import java.util.Observable;
import java.util.Observer;
import javafx.application.Platform;
import tgfx.system.Axis;


/**
 * tgFX Driver Class Copyright Synthetos.com
 */
public class TinygDriver extends Observable implements Observer {

//    private MachineTinyG mTG = MachineTinyG.getInstance();
    public Machine m = new Machine();
    private JdomParser JDOM = new JdomParser(); //JSON Object Parser
    /**
     * Static commands for TinyG to get settings from the TinyG Driver Board
     */
    public static final String CMD_GET_HARDWARE_BUILD_NUMBER = "{\"fb\":\"\"}\n";
    public static final String CMD_GET_HARDWARE_FIRMWARE_NUMBER = "{\"fv\":\"\"}\n";
    public static final String CMD_GET_OK_PROMPT = "{\"gc\":\"?\"}\n";
    public static final String CMD_GET_STATUS_REPORT = "{\"sr\":\"\"}\n";
    public static final String CMD_ZERO_ALL_AXIS = "{\"gc\":\"g92x0y0z0a0\"}\n";
    public static final String CMD_DISABLE_LOCAL_ECHO = "{\"ee\":0}\n";
    public static final String CMD_SET_STATUS_UPDATE_INTERVAL = "{\"si\":550}\n";
    private static final String CMD_GET_MACHINE_SETTINGS = "{\"sys\":null}\n";
    private static final String CMD_GET_X_AXIS = "{\"x\":null}\n";
    private static final String CMD_GET_Y_AXIS = "{\"y\":null}\n";
    private static final String CMD_GET_Z_AXIS = "{\"z\":null}\n";
    private static final String CMD_GET_A_AXIS = "{\"a\":null}\n";
    private static final String CMD_GET_B_AXIS = "{\"b\":null}\n";
    private static final String CMD_GET_C_AXIS = "{\"c\":null}\n";
    private static final String CMD_GET_MOTOR_1_SETTINGS = "{\"1\":null}\n";
    private static final String CMD_GET_MOTOR_2_SETTINGS = "{\"2\":null}\n";
    private static final String CMD_GET_MOTOR_3_SETTINGS = "{\"3\":null}\n";
    private static final String CMD_GET_MOTOR_4_SETTINGS = "{\"4\":null}\n";
    private static final String CMD_PAUSE = "!\n";
    private static final String CMD_RESUME = "~\n";
    private static final int CONFIG_DELAY = 50; //20 milliseconds to allow hardware configs to be set
    /**
     * TinyG Parsing Strings
     */
    private static final String RESPONSE_STATUS_REPORT = "{\"sr\":{";
    public static final String RESPONSE_FIRMWARE_BUILD = "{\"fb";
    public static final String RESPONSE_FIRMWARE_VERSION = "{\"fv";
    private SerialDriver ser = SerialDriver.getInstance();
    private String buf; //Buffer to store parital json lines
    private boolean PAUSED = false;
    /**
     * DEBUG VARS
     */
    public String lastMessage = "";

    /**
     * Singleton Code for the Serial Port Object
     *
     * @return
     */
    public static TinygDriver getInstance() {
        return TinygDriverHolder.INSTANCE;
    }

    private static class TinygDriverHolder {

        private static final TinygDriver INSTANCE = new TinygDriver();
    }
    //End Singleton

    @Override
    public void update(Observable o, Object o1) {
        String[] MSG = (String[]) o1;
        lastMessage = MSG[1];
        if (MSG[0] == "JSON") {
            parseJSON(MSG[1]);
        }
    }

    public boolean isCANCELLED() {
        return ser.isCANCELLED();
    }

    public void setCANCELLED(boolean choice) {
        ser.setCANCELLED(choice);
    }

    public boolean isPAUSED() {
        return PAUSED;
    }

    public void setPAUSED(boolean choice) throws Exception {
        if (choice) { //if set to pause
            ser.priorityWrite(CMD_PAUSE);
            PAUSED = choice;
        } else { //set to resume
            ser.priorityWrite(CMD_GET_OK_PROMPT);
            ser.priorityWrite(CMD_RESUME);
            ser.priorityWrite(CMD_GET_OK_PROMPT);
            PAUSED = false;
        }
    }

    public void setConnected(boolean choice) {
        this.ser.setConnected(choice);
    }

    public void write(String msg) throws Exception {
        ser.write(msg);
    }

    public boolean getClearToSend() {
        return ser.getClearToSend();
    }

    public void priorityWrite(String msg) throws Exception {
        ser.priorityWrite(msg);
    }

    public String[] listSerialPorts() {
        //Get a listing current system serial ports
        String portArray[] = null;
        portArray = ser.listSerialPorts();
        return portArray;
    }

    public void initialize(String portName, int dataRate) {
        this.ser.initialize(portName, dataRate);
        ser.addObserver(this);
    }

    public void disconnect() {
        this.ser.disconnect();

    }

    public boolean isConnected() {
        return this.ser.isConnected();
    }

    public String getPortName() {
        //Return the serial port name that is connected.
        return ser.serialPort.getName();
    }

    @Override
    public synchronized void addObserver(Observer obsrvr) {
        super.addObserver(obsrvr);
    }

    @Override
    public void notifyObservers() {
        super.notifyObservers();
    }

    public List<Axis> getAllAxis() {
        return (m.getAllAxis());
    }

    public void getAllAxisSettings() throws Exception {
        try {
            //We toss these sleeps in to give the hardware (tg) time to
            //respond and have their values parsed.
            System.out.println("[+]Getting A AXIS Settings");
            this.ser.write(CMD_GET_A_AXIS);
            Thread.sleep(CONFIG_DELAY);

            System.out.println("[+]Getting B AXIS Settings");
            this.ser.write(CMD_GET_B_AXIS);
            Thread.sleep(CONFIG_DELAY);

            System.out.println("[+]Getting C AXIS Settings");
            this.ser.write(CMD_GET_C_AXIS);
            Thread.sleep(CONFIG_DELAY);

            this.ser.write(CMD_GET_X_AXIS);
            System.out.println("[+]Getting X AXIS Settings");
            Thread.sleep(CONFIG_DELAY);

            this.ser.write(CMD_GET_Y_AXIS);
            System.out.println("[+]Getting Y AXIS Settings");
            Thread.sleep(CONFIG_DELAY);

            System.out.println("[+]Getting Z AXIS Settings");
            this.ser.write(CMD_GET_Z_AXIS);
            Thread.sleep(CONFIG_DELAY);

        } catch (Exception ex) {
        }
    }

    public void parseJSON(String line) {
        String axis;
        int motor;
        try {
            //Create our JSON Parsing Object
            JsonRootNode json = JDOM.parse(line);

            if (line.contains(RESPONSE_STATUS_REPORT)) {
                //Parse Status Report
                //"{"sr":{"line":0,"xpos":1.567,"ypos":0.548,"zpos":0.031,"apos":0.000,"vel":792.463,"unit":"mm","stat":"run"}}"
                m.getAxisByName("X").setWork_position(Float.parseFloat(json.getNode("sr").getNode("posx").getText()));
                m.getAxisByName("Y").setWork_position(Float.parseFloat(json.getNode("sr").getNode("posy").getText()));
                m.getAxisByName("Z").setWork_position(Float.parseFloat(json.getNode("sr").getNode("posz").getText()));
                m.getAxisByName("A").setWork_position(Float.parseFloat(json.getNode("sr").getNode("posa").getText()));

//                m.getAxisByName("B").setWork_position(Float.parseFloat(json.getNode("sr").getNode("posa").getText()));
//                m.getAxisByName("C").setWork_position(Float.parseFloat(json.getNode("sr").getNode("posa").getText()));

                //Parse state out of status report.
                m.setMachineState(Integer.valueOf(json.getNode("sr").getNode("stat").getText()));

                //Parse motion mode (momo) out of start report
                m.setMotionMode(Integer.parseInt(json.getNode("sr").getNode("momo").getText()));

                //Parse velocity out of status report
                m.setVelocity(Float.parseFloat(json.getNode("sr").getNode("vel").getText()));

                //Parse Unit Mode
                m.setUnits(Integer.parseInt(json.getNode("sr").getNode("unit").getText()));

                //m.getAxisByName("X").setWork_position(Float.parseFloat((json.getNode("sr").getNode("xpos").getText())));
                //m.getAxisByName("Y").setWork_position(Float.parseFloat((json.getNode("sr").getNode("ypos").getText())));
                //m.getAxisByName("Z").setWork_position(Float.parseFloat((json.getNode("sr").getNode("zpos").getText())));
                //this.A_AXIS.setWork_position(Float.parseFloat((json.getNode("sr").getNode("awp").getText())));
                setChanged();
                notifyObservers("STATUS_REPORT");

            } else if (line.startsWith(this.RESPONSE_FIRMWARE_BUILD)) {
                System.out.println("[#]Parsing Build Number...");
                m.setFirmware_build(Float.parseFloat(json.getNode("fb").getText()));
                setChanged();
                notifyObservers("BUILD_UPDATE");


            } else if (line.startsWith(this.RESPONSE_FIRMWARE_VERSION)) {
                System.out.println("[#]Parsing Version...");
                m.setFirmware_version(Float.parseFloat(json.getNode("fv").getText()));
                setChanged();
                notifyObservers("BUILD_UPDATE");
            } else if (line.startsWith("{\"sys\":")) {
                System.out.println("[#]Parsing Machine Settings JSON");
                //{"fv":0.930,"fb":330.190,"si":30,"gi":"21","gs":"17","gp":"64","ga":"90","ea":1,"ja":200000.000,"ml":0.080,"ma":0.100,"mt":10000.000,"ic":0,"il":0,"ec":0,"ee":0,"ex":1}
                m.setFirmware_version(Float.parseFloat(json.getNode("sys").getNode("fv").getText()));
                m.setFirmware_build(Float.parseFloat(json.getNode("sys").getNode("fb").getText()));
                m.setStatus_report_interval(Integer.parseInt((json.getNode("sys").getNode("si").getText())));
//                m.setEnable_acceleration(Boolean.parseBoolean((json.getNode("ea").getText())));
//                m.setCorner_acceleration(Integer.parseInt((json.getNode("ja").getText())));
                m.setMin_line_segment(Float.parseFloat((json.getNode("sys").getNode("ml").getText())));
                m.setMin_segment_time(Double.parseDouble(json.getNode("sys").getNode("mt").getText()));
                m.setMin_arc_segment(Float.parseFloat((json.getNode("sys").getNode("ma").getText())));
                m.setIgnore_CR(Boolean.parseBoolean((json.getNode("sys").getNode("ic").getText())));
                m.setIgnore_LF(Boolean.parseBoolean((json.getNode("sys").getNode("il").getText())));
                m.setEnable_CR(Boolean.parseBoolean((json.getNode("sys").getNode("ec").getText())));
                m.setEnable_echo(Boolean.parseBoolean((json.getNode("sys").getNode("ee").getText())));
                m.setEnable_xon_xoff(Boolean.parseBoolean((json.getNode("sys").getNode("ex").getText())));
                setChanged();
                notifyObservers("CMD_GET_MACHINE_SETTINGS");

            } /**
             * Start Checking for Motor Responses
             */
            else if (line.startsWith("{\"1\":") && !line.contains("null")) {
                motor = 1;
                parseJsonMotorSettings(line, motor);
            } else if (line.startsWith("{\"2\":") && !line.contains("null")) {
                motor = 2;
                parseJsonMotorSettings(line, motor);
            } else if (line.startsWith("{\"3\":") && !line.contains("null")) {
                motor = 3;
                parseJsonMotorSettings(line, motor);
            } else if (line.startsWith("{\"4\":") && !line.contains("null")) {
                motor = 4;
                parseJsonMotorSettings(line, motor);
            } else if (line.startsWith("{\"5\":") && !line.contains("null")) {
                motor = 5;
                parseJsonMotorSettings(line, motor);
            } else if (line.startsWith("{\"6\":") && !line.contains("null")) {
                motor = 6;
                parseJsonMotorSettings(line, motor);
            } /**
             * Start Checking for Axis Responses
             */
            else if (line.startsWith("{\"x\":") && !line.contains("null")) {
                axis = "x";
                parseJsonAxisSettings(line, axis);
            } else if (line.startsWith("{\"y\":") && !line.contains("null")) {
                axis = "y";
                parseJsonAxisSettings(line, axis);
            } else if (line.startsWith("{\"z\":") && !line.contains("null")) {
                axis = "z";
                parseJsonAxisSettings(line, axis);
            } else if (line.startsWith("{\"a\":") && !line.contains("null")) {
                axis = "a";
                parseJsonAxisSettings(line, axis);
            } else if (line.startsWith("{\"b\":") && !line.contains("null")) {
                axis = "b";
                parseJsonAxisSettings(line, axis);
            } else if (line.startsWith("{\"c\":") && !line.contains("null")) {
                axis = "c";
                parseJsonAxisSettings(line, axis);
            }





        } catch (argo.saj.InvalidSyntaxException ex) {
            //This will happen from time to time depending on the file that is being sent to TinyG
            //This is an issue mostly when the lines are very very small and there are many of them
            //and you are running at a high feedrate.
            System.out.println("[!]ParseJson Exception: " + ex.getMessage() + " LINE: " + line);
            setChanged();
            notifyObservers("[!] " + ex.getMessage() + "Line Was: " + line + "\n");

            //UGLY BUG FIX WORKAROUND FOR NOW
            //Code to fix a possible JSON TinyG Error
            if (line.contains("msg")) {
                try {

                    this.ser.setClearToSend(true);
                } catch (Exception ex1) {
                    System.out.println("EXCEPTION IN BUG FIX CODE TINYGDRIVER" + ex1.getMessage());
                }
            }
            //UGLY BUG FIX WORKAROUND FOR NOW



        } catch (argo.jdom.JsonNodeDoesNotMatchPathElementsException ex) {
            //Extra } for some reason
            System.out.println("[!]ParseJson Exception: " + ex.getMessage() + " LINE: " + line);
            setChanged();
            notifyObservers("[!] " + ex.getMessage() + "Line Was: " + line + "\n");

        } catch (Exception ex) {
            setChanged();
            notifyObservers("ERROR");
            System.out.println("Exception in TinygDriver");
            System.out.println(ex.getMessage());
        }
    }

    private synchronized void parseJsonAxisSettings(String line, String axis) throws InvalidSyntaxException {
        /**
         * When an axis is queried by tg this is the method that parses the
         * response and populates the machine model with the axis values.
         * {"a":{"am":1,"fr":36000.000,"vm":36000.000,"tm":-1.000,"jm":100000000.000,"jd":0.050,"ra":10.000,"sm":0,"sv":36000.000,"lv":3600.000,"lb":0.000,"zb":0.000}}
         */
        JsonRootNode json = JDOM.parse(line);
        Axis ax = m.getAxisByName(axis.toUpperCase());

        //m.getMotorByNumber(motor).setMapToAxis(Integer.valueOf((json.getNode(strMotor).getNode("ma").getText())));
        ax.setAxis_mode(Integer.valueOf((json.getNode(axis).getNode("am").getText())));
        ax.setFeed_rate_maximum(Float.valueOf((json.getNode(axis).getNode("fr").getText())));
        ax.setVelocity_maximum(Float.valueOf((json.getNode(axis).getNode("vm").getText())));
        ax.setTravel_maximum(Float.valueOf((json.getNode(axis).getNode("tm").getText())));
        ax.setJerk_maximum(Double.valueOf((json.getNode(axis).getNode("jm").getText())));

        //This is a bug fix.  This was messed up in firmware builds < 338.05
        //This will go away eventually
        if (ax.getAxis_name().equals("B")) {
            //This is not correct.  This should not be "cd" but "jd"
            ax.setJunction_devation(Float.valueOf((json.getNode(axis).getNode("cd").getText())));
        } else {
            //This is the correct syntax
            ax.setJunction_devation(Float.valueOf((json.getNode(axis).getNode("jd").getText())));
        }


        ax.setSwitch_mode(Integer.valueOf((json.getNode(axis).getNode("sm").getText())));
        ax.setVelocity_maximum(Float.valueOf((json.getNode(axis).getNode("sv").getText())));

        //This is a bug fix.  This was messed up in firmware builds < 338.05
        //This will go away eventually
        if (ax.getAxis_name().equals("C")) {
            ax.setLatch_velocity(Float.valueOf((json.getNode(axis).getNode("ls").getText())));
        } else {
            //This is the correct syntax
            ax.setLatch_velocity(Float.valueOf((json.getNode(axis).getNode("lv").getText())));
        }

        ax.setLatch_backoff(Float.valueOf((json.getNode(axis).getNode("lb").getText())));
        ax.setZero_backoff(Float.valueOf((json.getNode(axis).getNode("zb").getText())));
//        if (ax.getAxisType() == Axis.AXIS_TYPE.ROTATIONAL) {
//            ax = (RotationalAxis) ax;
//            RotationalAxis.AX.setRadius(Float.valueOf((json.getNode(axis).getNode("ra").getText())));
//        }

        if (ax.getAxisType().equals(Axis.AXIS_TYPE.ROTATIONAL)) {
            ax.setRadius(Float.valueOf((json.getNode(axis).getNode("ra").getText())));
        }

    }

    private synchronized void parseJsonMotorSettings(String line, int motor) throws InvalidSyntaxException {
        //{"1":{"ma":0,"sa":1.800,"tr":1.250,"mi":8,"po":0,"pm":1}}
        JsonRootNode json = JDOM.parse(line);
        String strMotor = String.valueOf(motor);


        m.getMotorByNumber(motor).setMapToAxis(Integer.valueOf((json.getNode(strMotor).getNode("ma").getText())));
        m.getMotorByNumber(motor).setStep_angle(Float.valueOf(json.getNode(strMotor).getNode("sa").getText()));
        m.getMotorByNumber(motor).setTravel_per_revolution(Float.valueOf(json.getNode(strMotor).getNode("tr").getText()));
        m.getMotorByNumber(motor).setPolarity(Boolean.valueOf((json.getNode(strMotor).getNode("mi").getText())));
        m.getMotorByNumber(motor).setPower_management(Boolean.valueOf(json.getNode(strMotor).getNode("po").getText()));
        m.getMotorByNumber(motor).setMicrosteps(Integer.valueOf(json.getNode(strMotor).getNode("pm").getText()));


        setChanged();
        notifyObservers("CMD_GET_MACHINE_SETTINGS");
    }

    public void requestStatusUpdate() {
        try {
            ser.write(CMD_GET_STATUS_REPORT);
            setChanged();
            notifyObservers("HEARTBEAT");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void getMachineSettings() {
        try {
            ser.write(CMD_GET_MACHINE_SETTINGS);
            setChanged();
            notifyObservers("CMD_GET_MACHINE_SETTINGS");
        } catch (Exception e) {
            System.out.println("ERROR Writing to Serial Port in getMachineSettings");
        }
    }

    public void getAllMotorSettings() throws Exception {
        Platform.runLater(new Runnable() {

            float vel;

            public void run() {
                //With the sleeps in this method we wrap it in a runnable task
                try {
                    ser.write(CMD_GET_MOTOR_1_SETTINGS);
//                    Thread.sleep(100);
                    ser.write(CMD_GET_MOTOR_2_SETTINGS);
//                    Thread.sleep(100);
                    ser.write(CMD_GET_MOTOR_3_SETTINGS);
//                    Thread.sleep(100);
                    ser.write(CMD_GET_MOTOR_4_SETTINGS);

                } catch (Exception ex) {
                    System.out.println("$$$$$$$$$$$$$EXCEPTION IN GetAllMotorSettings() $$$$$$$$$$$$$$");
                    System.out.println(ex.getMessage());
                }

            }
        });
    }

    public void getMotorSettings(int motorNumber) {
        try {
            if (motorNumber == 1) {
                ser.write(CMD_GET_MOTOR_1_SETTINGS);
            } else if (motorNumber == 2) {
                ser.write(CMD_GET_MOTOR_2_SETTINGS);
            } else if (motorNumber == 3) {
                ser.write(CMD_GET_MOTOR_3_SETTINGS);
            } else if (motorNumber == 4) {
                ser.write(CMD_GET_MOTOR_4_SETTINGS);
            } else {
                System.out.println("Invalid Motor Number.. Please try again..");
                setChanged();
//                notifyObservers("{\"error\":\"Invalid Motor Number\"}");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public synchronized boolean hasChanged() {
        return super.hasChanged();
    }

    @Override
    protected synchronized void setChanged() {
        super.setChanged();
    }
}
