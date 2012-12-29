/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.tinyg;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.apache.log4j.Logger;
import tgfx.Command;
import tgfx.Main;
import tgfx.ResponseParser;
import tgfx.SerialDriver;
import tgfx.SerialWriter;
import tgfx.gcode.GcodeLine;
import tgfx.system.Axis;
import tgfx.system.Machine;
import tgfx.system.Motor;

/**
 * tgFX Driver Class Copyright Synthetos.com
 */
public class TinygDriver extends Observable {

    static final Logger logger = Logger.getLogger(TinygDriver.class);
    public Machine m = Machine.getInstance();
    public QueueReport qr = QueueReport.getInstance();

    private class StatusCode {

        int codeNumber;
        String codeName;
        String codeMessage;

        private StatusCode(int cn, String cnme, String cmessage) {
            codeName = cnme;
            codeMessage = cmessage;
            codeNumber = codeNumber;
        }

        private String getStatusCodeMessage() {
            return (codeMessage);
        }
    }
    public final StatusCode STATUS_CODE_OK = new StatusCode(0, "OK", "Ready For Input");
    public final StatusCode STATUS_CODE_NOOP = new StatusCode(3, "NOOP", "Non-operation");
    public final StatusCode STATUS_CODE_LOADING_EEPROM = new StatusCode(15, "LOADING_EEPROM", "Loading EEPROM settings");
    /**
     * Static commands for TinyG to get settings from the TinyG Driver Board
     */
    public SimpleStringProperty TEST = new SimpleStringProperty(this, "TEST", "Unknown");

    public SimpleStringProperty getTEST() {
        return TEST;
    }

    public void setTEST(String t) {
        this.TEST.set(t);
    }
    public static final String RESPONSE_FOOTER = "\"f\":[";
    public static final String RESPONSE_HEADER = "{\"r\":{\"gc\":";
    public static final String CMD_QUERY_COORDINATE_SYSTEM = "{\"coor\":\"\"}\n";
    public static final String CMD_QUERY_HARDWARE_BUILD_NUMBER = "{\"fb\":\"\"}\n";
    public static final String CMD_QUERY_HARDWARE_FIRMWARE_NUMBER = "{\"fv\":\"\"}\n";
    public static final String CMD_QUERY_OK_PROMPT = "{\"gc\":\"?\"}\n";
    public static final String CMD_QUERY_BUFFER = "{\"k\":\"\"}\n";
    public static final String CMD_QUERY_STATUS_REPORT = "{\"sr\":\"\"}\n";
    public static final String CMD_QUERY_AXIS_X = "{\"x\":null}\n";
    public static final String CMD_QUERY_AXIS_Y = "{\"y\":null}\n";
    public static final String CMD_QUERY_AXIS_Z = "{\"z\":null}\n";
    public static final String CMD_QUERY_AXIS_A = "{\"a\":null}\n";
    public static final String CMD_QUERY_AXIS_B = "{\"b\":null}\n";
    public static final String CMD_QUERY_AXIS_C = "{\"c\":null}\n";
    public static final String CMD_QUERY_MOTOR_1_SETTINGS = "{\"1\":null}\n";
    public static final String CMD_QUERY_MOTOR_2_SETTINGS = "{\"2\":null}\n";
    public static final String CMD_QUERY_MOTOR_3_SETTINGS = "{\"3\":null}\n";
    public static final String CMD_QUERY_MOTOR_4_SETTINGS = "{\"4\":null}\n";
    public static final String CMD_QUERY_MACHINE_SETTINGS = "{\"sys\":null}\n";
    public static final String CMD_APPLY_ZERO_ALL_AXIS = "{\"gc\":\"g92x0y0z0a0\"}\n";
    public static final String CMD_APPLY_DISABLE_LOCAL_ECHO = "{\"ee\":0}\n";
    public static final String CMD_APPLY_DISABLE_HASHCODE = "{\"eh\":0\"}\n";
    public static final String CMD_APPLY_DEFAULT_SETTINGS = "{\"defaults\":1}\n";
    public static final String CMD_APPLY_STATUS_UPDATE_INTERVAL = "{\"si\":200}\n";
    public static final String CMD_APPLY_PAUSE = "!\n";
    public static final String CMD_APPLY_RESUME = "~\n";
    public static final String CMD_APPLY_DISABLE_XON_XOFF = "{\"ex\":1}\n";
    public static final String CMD_ZERO_ALL_AXIS = "{\"gc\":G920g0x0y0z0}\n";
    public static final String RESPONSE_STATUS_REPORT = "{\"r\":{\"sr\":{\"";
    public static final String RESPONSE_QUEUE_REPORT = "{\"r\":{\"qr\":{";
    //public static final String RESPONSE_STATUS_REPORT = "{\"sr\":{";
    public static final String RESPONSE_BUFFER_STATUS = "{\"r\":{\"k\":{";
    public static final String RESPONSE_MACHINE_FIRMWARE_BUILD = "{\"r\":{\"sys\":{\"fb\":";
    public static final String RESPONSE_MACHINE_FIRMWARE_VERSION = "{\"r\":{\"sys\":{\"fv\"";
    public static final String RESPONSE_MACHINE_COORDINATE_SYSTEM = "{\"r\":{\"sys\":{\"gco\"";
    public static final String RESPONSE_MACHINE_SETTINGS = "{\"r\":{\"sys";
    public static final String RESPONSE_ACK = "{\"k\":";
    public static final String RESPONSE_MOTOR_1 = "{\"r\":{\"1";
    public static final String RESPONSE_MOTOR_2 = "{\"r\":{\"2";
    public static final String RESPONSE_MOTOR_3 = "{\"r\":{\"3";
    public static final String RESPONSE_MOTOR_4 = "{\"r\":{\"4";
    public static final String RESPONSE_MOTOR_5 = "{\"r\":{\"5";
    public static final String RESPONSE_MOTOR_6 = "{\"r\":{\"6";
    public static final String RESPONSE_AXIS_X = "{\"r\":{\"x";
    public static final String RESPONSE_AXIS_Y = "{\"r\":{\"y";
    public static final String RESPONSE_AXIS_Z = "{\"r\":{\"z";
    public static final String RESPONSE_AXIS_A = "{\"r\":{\"a";
    public static final String RESPONSE_AXIS_B = "{\"r\":{\"b";
    public static final String RESPONSE_AXIS_C = "{\"r\":{\"c";
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
    public ArrayList<String> connections = new ArrayList<>();
    private SerialDriver ser = SerialDriver.getInstance();
    public static ArrayBlockingQueue<String> jsonQueue = new ArrayBlockingQueue<>(10);
    public static ArrayBlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(30);
    public static ArrayBlockingQueue<GcodeLine[]> writerQueue = new ArrayBlockingQueue<>(50000);
    public ResponseParser resParse = new ResponseParser(jsonQueue); // Our
    public SerialWriter serialWriter = new SerialWriter(writerQueue);
    private boolean PAUSED = false;
    public static int MAX_BUFFER = 254;

    /**
     * Singleton Code for the Serial Port Object
     *
     * @return
     */
    public static TinygDriver getInstance() {
        return TinygDriverHolder.INSTANCE;
    }

    public void getAllMotorSettings() throws Exception {
        Platform.runLater(new Runnable() {
            public void run() {
                //With the sleeps in this method we wrap it in a runnable task
                try {
                    ser.write(CMD_QUERY_MOTOR_1_SETTINGS);
                    TinygDriver.logger.info("[+]Getting Motor 1 Settings");

                    ser.write(CMD_QUERY_MOTOR_2_SETTINGS);
                    TinygDriver.logger.info("[+]Getting Motor 2 Settings");

                    ser.write(CMD_QUERY_MOTOR_3_SETTINGS);
                    TinygDriver.logger.info("[+]Getting Motor 3 Settings");

                    ser.write(CMD_QUERY_MOTOR_4_SETTINGS);
                    TinygDriver.logger.info("[+]Getting Motor 4 Settings");

                } catch (Exception ex) {
                    TinygDriver.logger.error("[!]Exception in getAllMotorSettings()...");
                    TinygDriver.logger.error(ex.getMessage());
                }
            }
        });
    }

    public void getAllAxisSettings() throws Exception {
        Platform.runLater(new Runnable() {
            public void run() {
                try {
                    TinygDriver.logger.info("[+]Getting A AXIS Settings");
                    TinygDriver.getInstance().write(CMD_QUERY_AXIS_A);

                    TinygDriver.logger.info("[+]Getting B AXIS Settings");
                    TinygDriver.getInstance().write(CMD_QUERY_AXIS_B);

                    TinygDriver.logger.info("[+]Getting C AXIS Settings");
                    TinygDriver.getInstance().write(CMD_QUERY_AXIS_C);

                    TinygDriver.getInstance().write(CMD_QUERY_AXIS_X);
                    TinygDriver.logger.info("[+]Getting X AXIS Settings");

                    TinygDriver.getInstance().write(CMD_QUERY_AXIS_Y);
                    TinygDriver.logger.info("[+]Getting Y AXIS Settings");

                    TinygDriver.getInstance().write(CMD_QUERY_AXIS_Z);
                    TinygDriver.logger.info("[+]Getting Z AXIS Settings");

                } catch (Exception ex) {
                    logger.error("Error in getAllAxisSettings : TinygDriver.java");
                }
            }
        });
    }

    public void queryHardwareSingleAxisSettings(String _axis) {
        try {
            if (_axis.toLowerCase().equals("x")) {
                ser.write(CMD_QUERY_AXIS_X);
            } else if (_axis.toLowerCase().equals("y")) {
                ser.write(CMD_QUERY_AXIS_Y);
            } else if (_axis.toLowerCase().equals("z")) {
                ser.write(CMD_QUERY_AXIS_Z);
            } else if (_axis.toLowerCase().equals("a")) {
                ser.write(CMD_QUERY_AXIS_A);
            } else if (_axis.toLowerCase().equals("b")) {
                ser.write(CMD_QUERY_AXIS_B);
            } else if (_axis.toLowerCase().equals("c")) {
                ser.write(CMD_QUERY_AXIS_C);
            }
        } catch (Exception ex) {
            System.out.println("[!]Error in queryHardwareSingleMotorSettings() " + ex.getMessage());
        }
    }

    public void applyHardwareAxisSettings(Tab _tab) throws Exception {


        GridPane _gp = (GridPane) _tab.getContent();
        int size = _gp.getChildren().size();
        Axis _axis = this.m.getAxisByName(String.valueOf(_gp.getId().charAt(0)));
        int i;
        for (i = 0; i < size; i++) {
            if (_gp.getChildren().get(i).getClass().toString().contains("TextField")) {
                //This ia a TextField... Lets get the value and apply it if it needs to be applied.
                TextField tf = (TextField) _gp.getChildren().get(i);
                applyHardwareAxisSettings(_axis, tf);

            } else if (_gp.getChildren().get(i) instanceof ChoiceBox) {
                //This ia a ChoiceBox... Lets get the value and apply it if it needs to be applied.
                @SuppressWarnings("unchecked")
                ChoiceBox<Object> cb = (ChoiceBox<Object>) _gp.getChildren().get(i);
                if (cb.getId().contains("AxisMode")) {
                    int axisMode = cb.getSelectionModel().getSelectedIndex();
                    String configObj = String.format("{\"%s%s\":%s}\n", _axis.getAxis_name().toLowerCase(), MNEMONIC_AXIS_AXIS_MODE, axisMode);
                    this.write(configObj);
                    continue;
                } else if (cb.getId().contains("switchModeMax")) {
                    int switchMode = cb.getSelectionModel().getSelectedIndex();
                    String configObj = String.format("{\"%s%s\":%s}\n", _axis.getAxis_name().toLowerCase(), MNEMONIC_AXIS_MAX_SWITCH_MODE, switchMode);
                    this.write(configObj);
                } else if (cb.getId().contains("switchModeMin")) {
                    int switchMode = cb.getSelectionModel().getSelectedIndex();
                    String configObj = String.format("{\"%s%s\":%s}\n", _axis.getAxis_name().toLowerCase(), MNEMONIC_AXIS_MIN_SWITCH_MODE, switchMode);
                    this.write(configObj);
                }
            }
        }


        System.out.println("[+]Applying Axis Settings...");
    }

    public void applyHardwareMotorSettings(Motor _motor, TextField tf) throws Exception {
        if (tf.getId().contains("StepAngle")) {
            if (_motor.getStep_angle() != Float.valueOf(tf.getText())) {
                this.write("{\"" + _motor.getId_number() + MNEMONIC_MOTOR_STEP_ANGLE + "\":" + tf.getText() + "}\n");
            }
        } else if (tf.getId().contains("TravelPer")) {
            if (_motor.getStep_angle() != Float.valueOf(tf.getText())) {
                this.write("{\"" + _motor.getId_number() + MNEMONIC_MOTOR_TRAVEL_PER_REVOLUTION + "\":" + tf.getText() + "}\n");
            }
        }
    }

    public void applyHardwareAxisSettings(Axis _axis, TextField tf) throws Exception {
        /**
         * Apply Axis Settings to TinyG from GUI
         */
        if (tf.getId().contains("maxVelocity")) {
            if (_axis.getVelocity_maximum() != Double.valueOf(tf.getText())) {
                //We check to see if the value passed was already set in TinyG 
                //To avoid un-needed EEPROM Writes.
                this.write("{\"" + _axis.getAxis_name().toLowerCase() + MNEMONIC_AXIS_VELOCITY_MAXIMUM + "\":" + tf.getText() + "}\n");
            }
        } else if (tf.getId().contains("maxFeed")) {
            if (_axis.getFeed_rate_maximum() != Double.valueOf(tf.getText())) {
                //We check to see if the value passed was already set in TinyG 
                //To avoid un-needed EEPROM Writes.
                this.write("{\"" + _axis.getAxis_name().toLowerCase() + MNEMONIC_AXIS_FEEDRATE_MAXIMUM + "\":" + tf.getText() + "}\n");
            }
        } else if (tf.getId().contains("maxTravel")) {
            if (_axis.getTravel_maximum() != Double.valueOf(tf.getText())) {
                //We check to see if the value passed was already set in TinyG 
                //To avoid un-needed EEPROM Writes.
                this.write("{\"" + _axis.getAxis_name().toLowerCase() + MNEMONIC_AXIS_TRAVEL_MAXIMUM + "\":" + tf.getText() + "}\n");
            }
        } else if (tf.getId().contains("maxJerk")) {
            if (_axis.getJerk_maximum() != Double.valueOf(tf.getText())) {
                //We check to see if the value passed was already set in TinyG 
                //To avoid un-needed EEPROM Writes.
                this.write("{\"" + _axis.getAxis_name().toLowerCase() + MNEMONIC_AXIS_JERK_MAXIMUM + "\":" + tf.getText() + "}\n");
            }
        } else if (tf.getId().contains("junctionDeviation")) {
            if (Double.valueOf(_axis.getJunction_devation()).floatValue() != Double.valueOf(tf.getText())) {
                //We check to see if the value passed was already set in TinyG 
                //To avoid un-needed EEPROM Writes.
                this.write("{\"" + _axis.getAxis_name().toLowerCase() + MNEMONIC_AXIS_JUNCTION_DEVIATION + "\":" + tf.getText() + "}\n");
            }
        } else if (tf.getId().contains("radius")) {
            if (_axis.getAxisType().equals(Axis.AXIS_TYPE.ROTATIONAL)) {
                //Check to see if its a ROTATIONAL AXIS... 
                if (_axis.getRadius() != Double.valueOf(tf.getText())) {
                    //We check to see if the value passed was already set in TinyG 
                    //To avoid un-needed EEPROM Writes.
                    this.write("{\"" + _axis.getAxis_name().toLowerCase() + MNEMONIC_AXIS_RADIUS + "\":" + tf.getText() + "}\n");
                }
            }
        } else if (tf.getId().contains("searchVelocity")) {
            if (_axis.getSearch_velocity() != Double.valueOf(tf.getText())) {
                //We check to see if the value passed was already set in TinyG 
                //To avoid un-needed EEPROM Writes.
                this.write("{\"" + _axis.getAxis_name().toLowerCase() + MNEMONIC_AXIS_SEARCH_VELOCITY + "\":" + tf.getText() + "}\n");
            }
        } else if (tf.getId().contains("latchVelocity")) {
            if (_axis.getLatch_velocity() != Double.valueOf(tf.getText())) {
                //We check to see if the value passed was already set in TinyG 
                //To avoid un-needed EEPROM Writes.
                this.write("{\"" + _axis.getAxis_name().toLowerCase() + MNEMONIC_AXIS_LATCH_VELOCITY + "\":" + tf.getText() + "}\n");
            }
        } else if (tf.getId().contains("latchBackoff")) {
            if (_axis.getLatch_backoff() != Double.valueOf(tf.getText())) {
                //We check to see if the value passed was already set in TinyG 
                //To avoid un-needed EEPROM Writes.
                this.write("{\"" + _axis.getAxis_name().toLowerCase() + MNEMONIC_AXIS_LATCH_BACKOFF + "\":" + tf.getText() + "}\n");
            }
        } else if (tf.getId().contains("zeroBackoff")) {
            if (_axis.getZero_backoff() != Double.valueOf(tf.getText())) {
                //We check to see if the value passed was already set in TinyG 
                //To avoid un-needed EEPROM Writes.
                this.write("{\"" + _axis.getAxis_name().toLowerCase() + MNEMONIC_AXIS_ZERO_BACKOFF + "\":" + tf.getText() + "}\n");
            }
        }
        System.out.println("[+]Applying " + _axis.getAxis_name() + " settings");
    }

    public void getMotorSettings(int motorNumber) {
        try {
            if (motorNumber == 1) {
                ser.write(CMD_QUERY_MOTOR_1_SETTINGS);
            } else if (motorNumber == 2) {
                ser.write(CMD_QUERY_MOTOR_2_SETTINGS);
            } else if (motorNumber == 3) {
                ser.write(CMD_QUERY_MOTOR_3_SETTINGS);
            } else if (motorNumber == 4) {
                ser.write(CMD_QUERY_MOTOR_4_SETTINGS);
            } else {
                TinygDriver.logger.error("Invalid Motor Number.. Please try again..");
            }
        } catch (Exception ex) {
            TinygDriver.logger.error(ex.getMessage());
        }
    }

    public void applyHardwareMotorSettings(Tab _tab) throws Exception {
        /**
         * Apply Motor Settings to TinyG from GUI
         */
        Tab selectedTab = _tab.getTabPane().getSelectionModel().getSelectedItem();
        int _motorNumber = Integer.valueOf(selectedTab.getText().split(" ")[1].toString());
        Motor _motor = this.m.getMotorByNumber(_motorNumber);

        GridPane _gp = (GridPane) _tab.getContent();
        int size = _gp.getChildren().size();
        int i;
        //Iterate though each gridpane child... Picking out text fields and choice boxes
        for (i = 0; i < size; i++) {

            if (_gp.getChildren().get(i).toString().contains("TextField")) {
                TextField tf = (TextField) _gp.getChildren().get(i);
                try {
                    applyHardwareMotorSettings(_motor, tf);
                } catch (Exception _ex) {
                    System.out.println("[!]Exception in applyHardwareMotorSettings(Tab _tab)");
                }
            } else if (_gp.getChildren().get(i) instanceof ChoiceBox) {
                @SuppressWarnings("unchecked")
                ChoiceBox<Object> _cb = (ChoiceBox<Object>) _gp.getChildren().get(i);
                if (_cb.getId().contains("MapAxis")) {
                    int mapAxis;
                    switch (_cb.getSelectionModel().getSelectedItem().toString()) {
                        case "X":
                            mapAxis = 0;
                            break;
                        case "Y":
                            mapAxis = 1;
                            break;
                        case "Z":
                            mapAxis = 2;
                            break;
                        case "A":
                            mapAxis = 3;
                            break;
                        case "B":
                            mapAxis = 4;
                            break;
                        case "C":
                            mapAxis = 5;
                            break;
                        default:
                            mapAxis = 0;  //Defaults to map to X
                    }
                    String configObj = String.format("{\"%s\":{\"%s\":%s}}\n", _motorNumber, MNEMONIC_MOTOR_MAP_AXIS, mapAxis);
                    this.write(configObj);

                } else if (_cb.getId().contains("MicroStepping")) {
                    //This is the MapAxis Choice Box... Lets apply that
                    int microSteps;
                    switch (_cb.getSelectionModel().getSelectedIndex()) {
                        case 0:
                            microSteps = 1;
                            break;
                        case 1:
                            microSteps = 2;
                            break;
                        case 2:
                            microSteps = 4;
                            break;
                        case 3:
                            microSteps = 8;
                            break;
                        default:
                            microSteps = 1;
                    }
                    String configObj = String.format("{\"%s%s\":%s}\n", _motorNumber, MNEMONIC_MOTOR_MICROSTEPS, microSteps);
                    this.write(configObj);

                } else if (_cb.getId().contains("Polarity")) {
                    String configObj = String.format("{\"%s%s\":%s}\n", _motorNumber, MNEMONIC_MOTOR_POLARITY, _cb.getSelectionModel().getSelectedIndex());
                    this.write(configObj);

                } else if (_cb.getId().contains("PowerMode")) {
                    String configObj = String.format("{\"%s%s\":%s}\n", _motorNumber, MNEMONIC_MOTOR_POWER_MANAGEMENT, _cb.getSelectionModel().getSelectedIndex());
                    this.write(configObj);
                }
            }
        }
    }

    public void queryHardwareSingleMotorSettings(int motorNumber) {
        try {
            if (motorNumber == 1) {
                ser.write(CMD_QUERY_MOTOR_1_SETTINGS);
            } else if (motorNumber == 2) {
                ser.write(CMD_QUERY_MOTOR_2_SETTINGS);
            } else if (motorNumber == 3) {
                ser.write(CMD_QUERY_MOTOR_3_SETTINGS);
            } else if (motorNumber == 4) {
                ser.write(CMD_QUERY_MOTOR_4_SETTINGS);
            } else {
                System.out.println("Invalid Motor Number.. Please try again..");
                setChanged();
            }
        } catch (Exception ex) {
            System.out.println("[!]Error in queryHardwareSingleMotorSettings() " + ex.getMessage());
        }
    }

    private TinygDriver() {
    }

    private static class TinygDriverHolder {

        private static final TinygDriver INSTANCE = new TinygDriver();
    }

    @Override
    public synchronized void addObserver(Observer obsrvr) {
        super.addObserver(obsrvr);
    }

    @Override
    public void notifyObservers() {
        super.notifyObservers();
    }

    public void appendJsonQueue(String line) {
        // This adds full normalized json objects to our jsonQueue.
        TinygDriver.jsonQueue.add(line);
    }

    public synchronized void appendResponseQueue(byte[] queue) {
        // Add byte arrays to the buffer queue from tinyG's responses.
        try {
            TinygDriver.queue.put((byte[]) queue);
        } catch (Exception e) {
            System.out.println("ERROR n shit");
        }
    }

    public boolean isPAUSED() {
        return PAUSED;
    }

    public void setPAUSED(boolean choice) throws Exception {
        if (choice) { // if set to pause
            ser.priorityWrite(CMD_APPLY_PAUSE);
            PAUSED = choice;
        } else { // set to resume
            ser.priorityWrite(CMD_QUERY_OK_PROMPT);
            ser.priorityWrite(CMD_APPLY_RESUME);
            ser.priorityWrite(CMD_QUERY_OK_PROMPT);
            PAUSED = false;
        }
    }

    /**
     * Connection Methods
     */
    public void setConnected(boolean choice) {
        this.ser.setConnected(choice);
    }

    public void initialize(String portName, int dataRate) {
        this.ser.initialize(portName, dataRate);
    }

    public void disconnect() {
        this.ser.disconnect();
    }

    public boolean isConnected() {
        return this.ser.isConnected();
    }

    /**
     * All Methods involving writing to TinyG.. This messages will call the
     * SerialDriver write methods from here.
     */
    public synchronized void write(String msg) throws Exception {
        TinygDriver.getInstance().serialWriter.addCommandToBuffer(msg);
    }

    public void priorityWrite(Byte b) throws Exception {
        this.ser.priorityWrite(b);
    }

    public void priorityWrite(String msg) throws Exception {
        if (!msg.contains("\n")) {
            msg = msg + "\n";
        }
        ser.write(msg);
    }

    /**
     * Utility Methods
     *
     * @return
     */
    public String[] listSerialPorts() {
        // Get a listing current system serial ports
        String portArray[] = null;
        portArray = SerialDriver.listSerialPorts();
        return portArray;
    }

    public String getPortName() {
        // Return the serial port name that is connected.
        return ser.serialPort.getName();
    }

    public List<Axis> getInternalAllAxis() {
        return (Machine.getInstance().getAllAxis());
    }

    public void queryHardwareAllAxisSettings() throws Exception {
        try {

            System.out.println("[+]Getting A AXIS Settings");
            this.ser.write(CMD_QUERY_AXIS_A);

            System.out.println("[+]Getting B AXIS Settings");
            this.ser.write(CMD_QUERY_AXIS_B);

            System.out.println("[+]Getting C AXIS Settings");
            this.ser.write(CMD_QUERY_AXIS_C);

            this.ser.write(CMD_QUERY_AXIS_X);
            System.out.println("[+]Getting X AXIS Settings");

            this.ser.write(CMD_QUERY_AXIS_Y);
            System.out.println("[+]Getting Y AXIS Settings");

            this.ser.write(CMD_QUERY_AXIS_Z);
            System.out.println("[+]Getting Z AXIS Settings");

        } catch (Exception ex) {
            System.out.println("[!]Error in queryHardwareAxisSettings()");
        }
    }

    public void applyResponseCommand(responseCommand rc) {
        char _ax;
        switch (rc.getSettingKey()) {
            case (TinygDriver.MNEMONIC_AXIS_AXIS_MODE):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingKey()).setAxis_mode(Integer.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_AXIS_FEEDRATE_MAXIMUM):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setFeed_rate_maximum(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_AXIS_JERK_MAXIMUM):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setJerk_maximum(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_AXIS_JUNCTION_DEVIATION):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setJunction_devation(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_AXIS_LATCH_BACKOFF):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setLatch_backoff(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_AXIS_LATCH_VELOCITY):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setLatch_velocity(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_AXIS_MAX_SWITCH_MODE):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setMaxSwitch_mode(Integer.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_AXIS_MIN_SWITCH_MODE):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setMinSwitch_mode(Integer.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_AXIS_RADIUS):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setRadius(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_AXIS_SEARCH_VELOCITY):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setSearch_velocity(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_AXIS_TRAVEL_MAXIMUM):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setTravel_maximum(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_AXIS_VELOCITY_MAXIMUM):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setVelocity_maximum(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_AXIS_ZERO_BACKOFF):
                TinygDriver.getInstance().m.getAxisByName(rc.getSettingParent()).setZero_backoff(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_MOTOR_MAP_AXIS):
                TinygDriver.getInstance().m.getMotorByNumber(Integer.valueOf(rc.getSettingParent())).setMapToAxis(Integer.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_MOTOR_MICROSTEPS):
                TinygDriver.getInstance().m.getMotorByNumber(Integer.valueOf(rc.getSettingParent())).setMicrosteps(Integer.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_MOTOR_POLARITY):
                TinygDriver.getInstance().m.getMotorByNumber(Integer.valueOf(rc.getSettingParent())).setPolarity(Integer.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_MOTOR_POWER_MANAGEMENT):
                TinygDriver.getInstance().m.getMotorByNumber(Integer.valueOf(rc.getSettingParent())).setPower_management(Integer.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_MOTOR_STEP_ANGLE):
                TinygDriver.getInstance().m.getMotorByNumber(Integer.valueOf(rc.getSettingParent())).setStep_angle(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_MOTOR_TRAVEL_PER_REVOLUTION):
                TinygDriver.getInstance().m.getMotorByNumber(Integer.valueOf(rc.getSettingParent())).setTravel_per_revolution(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_STATUS_REPORT_LINE):
                TinygDriver.getInstance().m.setLine_number(Integer.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_STATUS_REPORT_MOTION_MODE):
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                TinygDriver.getInstance().m.setMotionMode(Integer.valueOf(rc.getSettingValue()));
                break;

            case (TinygDriver.MNEMONIC_STATUS_REPORT_POSA):
                _ax =  rc.getSettingKey().charAt(rc.getSettingKey().length()-1);
                TinygDriver.getInstance().m.getAxisByName(String.valueOf(_ax)).setWork_position(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                
                break;

            case (TinygDriver.MNEMONIC_STATUS_REPORT_POSX):
                _ax =  rc.getSettingKey().charAt(rc.getSettingKey().length()-1);
                TinygDriver.getInstance().m.getAxisByName(String.valueOf(_ax)).setWork_position(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_STATUS_REPORT_POSY):
                _ax =  rc.getSettingKey().charAt(rc.getSettingKey().length()-1);
                TinygDriver.getInstance().m.getAxisByName(String.valueOf(_ax)).setWork_position(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_STATUS_REPORT_POSZ):
                _ax =  rc.getSettingKey().charAt(rc.getSettingKey().length()-1);
                TinygDriver.getInstance().m.getAxisByName(String.valueOf(_ax)).setWork_position(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_STATUS_REPORT_STAT):
                //TinygDriver.getInstance()(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_STATUS_REPORT_VELOCITY):
                TinygDriver.getInstance().m.setVelocity(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_SYSTEM_BAUDRATE):
                //TinygDriver.getInstance().m.s(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_SYSTEM_ENABLE_ECHO):
                TinygDriver.getInstance().m.setEnable_echo(Boolean.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_SYSTEM_ENABLE_JSON_MODE):
                //TinygDriver.getInstance().m(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_SYSTEM_ENABLE_XON):
                TinygDriver.getInstance().m.setEnable_xon_xoff(Boolean.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_SYSTEM_FIRMWARE_BUILD):
                TinygDriver.getInstance().m.setFirmware_build(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_SYSTEM_FIRMWARE_VERSION):
                TinygDriver.getInstance().m.setFirmware_version(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_SYSTEM_GCODE_COORDINATE_SYSTEM):
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;
            case (TinygDriver.MNEMONIC_SYSTEM_GCODE_DISANCE_MODE):
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;
            case (TinygDriver.MNEMONIC_SYSTEM_GCODE_PATH_CONTROL):
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;
            case (TinygDriver.MNEMONIC_SYSTEM_GCODE_PLANE):
                //TinygDriver.getInstance().m.setGcode_select_plane(Float.valueOf(rc.getSettingValue()));
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;
            case (TinygDriver.MNEMONIC_SYSTEM_GCODE_UNIT_MODE):
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_SYSTEM_IGNORE_CR):
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_SYSTEM_JSON_VOBERSITY):
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_SYSTEM_JUNCTION_ACCELERATION):
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;


//            case (TinygDriver.MNEMONIC_SYSTEM_MIN_ARC_SEGMENT):
//            TinygDriver.logger.info("[APPLIED: "+ rc.getSettingParent() + " "+ rc.getSettingKey() + ": "+ rc.getSettingValue());
//break;
            case (TinygDriver.MNEMONIC_SYSTEM_MIN_LINE_SEGMENT):
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_SYSTEM_MIN_TIME_SEGMENT):
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_SYSTEM_QUEUE_REPORTS):
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_SYSTEM_STATUS_REPORT_INTERVAL):
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_SYSTEM_SWITCH_TYPE):
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            case (TinygDriver.MNEMONIC_SYSTEM_TEXT_VOBERSITY):
                TinygDriver.logger.info("[APPLIED:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;

            default:
                TinygDriver.logger.error("[ERROR] in ApplyResponseCommand:  Command Was:" + rc.getSettingParent() + " " + rc.getSettingKey() + ":" + rc.getSettingValue());
                break;
        }


    }
}
