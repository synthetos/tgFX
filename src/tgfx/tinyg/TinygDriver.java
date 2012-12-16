/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.tinyg;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import javafx.application.Platform;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.apache.log4j.Logger;
import tgfx.Command;
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
    
    public static final String RESPONSE_FOOTER = "\"f\":[";
    public static final String RESPONSE_HEADER = "{\"b\":{\"gc\":";
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
    public static final String RESPONSE_STATUS_REPORT = "{\"b\":{\"sr\":{\"";
    public static final String RESPONSE_QUEUE_REPORT = "{\"b\":{\"qr\":{";
    //public static final String RESPONSE_STATUS_REPORT = "{\"sr\":{";
    public static final String RESPONSE_BUFFER_STATUS = "{\"b\":{\"k\":{";
    public static final String RESPONSE_MACHINE_FIRMWARE_BUILD = "{\"b\":{\"sys\":{\"fb\":";
    public static final String RESPONSE_MACHINE_FIRMWARE_VERSION = "{\"b\":{\"sys\":{\"fv\"";
    public static final String RESPONSE_MACHINE_COORDINATE_SYSTEM = "{\"b\":{\"sys\":{\"gco\"";
    public static final String RESPONSE_MACHINE_SETTINGS = "{\"b\":{\"sys";
    public static final String RESPONSE_ACK = "{\"k\":";
    public static final String RESPONSE_MOTOR_1 = "{\"b\":{\"1";
    public static final String RESPONSE_MOTOR_2 = "{\"b\":{\"2";
    public static final String RESPONSE_MOTOR_3 = "{\"b\":{\"3";
    public static final String RESPONSE_MOTOR_4 = "{\"b\":{\"4";
    public static final String RESPONSE_MOTOR_5 = "{\"b\":{\"5";
    public static final String RESPONSE_MOTOR_6 = "{\"b\":{\"6";
    public static final String RESPONSE_AXIS_X = "{\"b\":{\"x";
    public static final String RESPONSE_AXIS_Y = "{\"b\":{\"y";
    public static final String RESPONSE_AXIS_Z = "{\"b\":{\"z";
    public static final String RESPONSE_AXIS_A = "{\"b\":{\"a";
    public static final String RESPONSE_AXIS_B = "{\"b\":{\"b";
    public static final String RESPONSE_AXIS_C = "{\"b\":{\"c";
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
                } else if (cb.getId().contains("switchMode")) {
                    int switchMode = cb.getSelectionModel().getSelectedIndex();
                    String configObj = String.format("{\"%s%s\":%s}\n", _axis.getAxis_name().toLowerCase(), MNEMONIC_AXIS_MAX_SWITCH_MODE, switchMode);
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
}
