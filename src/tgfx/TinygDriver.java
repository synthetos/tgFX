/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import java.util.ArrayList;
import java.util.List;
import tgfx.system.Machine;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javafx.application.Platform;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import tgfx.system.Axis;
import org.apache.log4j.Logger;
import tgfx.system.Motor;

/**
 * tgFX Driver Class Copyright Synthetos.com
 */
public class TinygDriver extends Observable {

    static final Logger logger = Logger.getLogger(TinygDriver.class);
    // private MachineTinyG mTG = MachineTinyG.getInstance();
    public Machine m = Machine.getInstance();
    /**
     * Static commands for TinyG to get settings from the TinyG Driver Board
     */
    public static final String RESPONSE_HEADER ="{\"r\":{\"bd\":";
    public static final String CMD_QUERY_COORDINATE_SYSTEM = "{\"coor\":\"\"}\n";
    public static final String CMD_QUERY_HARDWARE_BUILD_NUMBER = "{\"fb\":\"\"}\n";
    public static final String CMD_QUERY_HARDWARE_FIRMWARE_NUMBER = "{\"fv\":\"\"}\n";
    public static final String CMD_QUERY_OK_PROMPT = "{\"gc\":\"?\"}\n";
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
    public static final String CMD_APPLY_STATUS_UPDATE_INTERVAL = "{\"si\":125}\n";
    public static final String CMD_APPLY_PAUSE = "!\n";
    public static final String CMD_APPLY_RESUME = "~\n";
    public static final String CMD_APPLY_DISABLE_XON_XOFF = "{\"ex\":1}\n";
    public static final String CMD_APPLY_RESTORE_DEFAULTS = "$defaults=1\n";
    public static final String CMD_ZERO_ALL_AXIS = "{\"gc\":G920g0x0y0z0}\n";
    public static final String RESPONSE_STATUS_REPORT = "{\"r\":{\"bd\":{\"sr\":{";
    //public static final String RESPONSE_STATUS_REPORT = "{\"sr\":{";
    public static final String RESPONSE_MACHINE_FIRMWARE_BUILD = "{\"fb";
    public static final String RESPONSE_MACHINE_FIRMWARE_VERSION = "{\"fv";
    public static final String RESPONSE_MACHINE_COORDINATE_SYSTEM = "{\"gco";
    public static final String RESPONSE_MACHINE_SETTINGS = "{\"sys";
    //AXIS Mnemonics
    public static final String MNEMONIC_AXIS_AXIS_MODE = "am";
    public static final String MNEMONIC_AXIS_VELOCITY_MAXIMUM = "vm";
    public static final String MNEMONIC_AXIS_FEEDRATE_MAXIMUM = "fr";
    public static final String MNEMONIC_AXIS_TRAVEL_MAXIMUM = "tm";
    public static final String MNEMONIC_AXIS_JERK_MAXIMUM = "jm";
    public static final String MNEMONIC_AXIS_JUNCTION_DEVIATION = "jd";
    public static final String MNEMONIC_AXIS_SWITCH_MODE = "sm";
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
    public static final int CONFIG_DELAY = 50; //50 milliseconds to allow hardware configs to be set
//      // hardware configs to be set
    public ArrayList<String> connections = new ArrayList<String>();
    /**
     * TinyG Parsing Strings
     */
    private SerialDriver ser = SerialDriver.getInstance();
    // Buffers for threads here
    public static ArrayBlockingQueue<String> jsonQueue = new ArrayBlockingQueue<String>(
            10);
    public static ArrayBlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(
            30);
    // public static BlockingQueue<String> jsonQueue = new
    // LinkedBlockingQueue<>();
    // public static BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();
    public QueueReader queueReader = new QueueReader(queue, jsonQueue);
    public ResponseParser resParse = new ResponseParser(jsonQueue); // Our
    // object
    // that
    // parses
    // all
    // responses
    // for the
    // TinyG
    // Hardware
    private String buf; // Buffer to store parital json lines
    private boolean PAUSED = false;
    private AtomicBoolean ClearToSend = new AtomicBoolean(true);
    public static int processedMsgs;
    private static final int maxbuffer = 100;
    public static final int ERROR = -maxbuffer - 1;
    private int freespace = maxbuffer;
    private ArrayBlockingQueue<String> ouputBuffer = new ArrayBlockingQueue<String>(
            maxbuffer);
    ReentrantLock lock = new ReentrantLock();
    private Condition clearToSend = lock.newCondition();

    /**
     * Singleton Code for the Serial Port Object
     *
     * @return
     */
    public static TinygDriver getInstance() {
        return TinygDriverHolder.INSTANCE;
    }

    public int getFreeSpace() {
        lock.lock();
        int ret = freespace;
        lock.unlock();
        return ret;
    }

    public int commandComplete(String jsonReturn) throws InterruptedException {
        //logger.debug("taking");
        //int completedSize = this.ouputBuffer.take();
        String completedCommand = this.ouputBuffer.take();
        int completedSize = completedCommand.length();
        if (jsonReturn.contains(completedCommand.substring(0, completedSize - 2))) {
            logger.debug("[" + jsonReturn + "] contains [" + completedCommand.substring(0, completedSize - 2) + "]");
        } else {
            logger.error("[" + jsonReturn + "] DOES NOT CONTAIN [" + completedCommand.substring(0, completedSize - 2) + "]");
        }
        //logger.debug("locking");
        lock.lock();
        boolean wakeUpCall = freespace <= 0;
        //logger.debug("adding " + completedSize + " to freespace " + freespace);
        freespace += completedSize;
        logger.debug("checking freespace " + freespace + " >= 0");
        if (wakeUpCall && freespace >= 0) {
            //logger.debug("signaling sender");
            clearToSend.signal();
        }
        //logger.debug("unlocking");
        lock.unlock();
        //logger.debug("returning " + completedSize);
        return completedSize;
    }

    public void getAllMotorSettings() throws Exception {
        Platform.runLater(new Runnable() {

            float vel;

            public void run() {
                //With the sleeps in this method we wrap it in a runnable task
                try {
                    ser.write(CMD_QUERY_OK_PROMPT);
                    Thread.sleep(CONFIG_DELAY);
                    ser.write(CMD_QUERY_MOTOR_1_SETTINGS);
                    ser.write(TinygDriver.CMD_QUERY_OK_PROMPT);
                    Main.logger.info("[+]Getting Motor 1 Settings");

                    Thread.sleep(CONFIG_DELAY);
                    ser.write(CMD_QUERY_MOTOR_2_SETTINGS);
                    Main.logger.info("[+]Getting Motor 2 Settings");
                    ser.write(TinygDriver.CMD_QUERY_OK_PROMPT);

                    Thread.sleep(CONFIG_DELAY);
                    ser.write(CMD_QUERY_MOTOR_3_SETTINGS);
                    Main.logger.info("[+]Getting Motor 3 Settings");
                    ser.write(TinygDriver.CMD_QUERY_OK_PROMPT);

                    Thread.sleep(CONFIG_DELAY);
                    ser.write(CMD_QUERY_MOTOR_4_SETTINGS);
                    Main.logger.info("[+]Getting Motor 4 Settings");
                    ser.write(CMD_QUERY_OK_PROMPT);

                } catch (Exception ex) {
                    Main.logger.error("[!]Exception in getAllMotorSettings()...");
                    Main.logger.error(ex.getMessage());
                }

            }
        });
    }

    public void requestStatusUpdate() {
        try {
            ser.write(CMD_QUERY_STATUS_REPORT);
            setChanged();
            notifyObservers("HEARTBEAT");
        } catch (Exception e) {
            Main.logger.error(e.getMessage());
        }
    }

    public void getAllAxisSettings() throws Exception {
        Platform.runLater(new Runnable() {

            public void run() {
                try {
                    //We toss these sleeps in to give the hardware (tg) time to
                    //respond and have their values parsed.

                    Main.logger.info("[+]Getting A AXIS Settings");
                    Thread.sleep(CONFIG_DELAY);
                    TinygDriver.getInstance().ser.write(CMD_QUERY_OK_PROMPT);
                    TinygDriver.getInstance().write(CMD_QUERY_AXIS_A);
                    ser.write(TinygDriver.CMD_QUERY_OK_PROMPT);
                    Thread.sleep(CONFIG_DELAY);

                    TinygDriver.getInstance().write(CMD_QUERY_OK_PROMPT);
                    Main.logger.info("[+]Getting B AXIS Settings");
                    TinygDriver.getInstance().write(CMD_QUERY_AXIS_B);
                    ser.write(TinygDriver.CMD_QUERY_OK_PROMPT);
                    Thread.sleep(CONFIG_DELAY);

                    TinygDriver.getInstance().write(CMD_QUERY_OK_PROMPT);
                    Main.logger.info("[+]Getting C AXIS Settings");
                    TinygDriver.getInstance().write(CMD_QUERY_AXIS_C);
                    ser.write(TinygDriver.CMD_QUERY_OK_PROMPT);
                    Thread.sleep(CONFIG_DELAY);

                    TinygDriver.getInstance().write(CMD_QUERY_OK_PROMPT);
                    TinygDriver.getInstance().write(CMD_QUERY_AXIS_X);
                    Main.logger.info("[+]Getting X AXIS Settings");
                    ser.write(TinygDriver.CMD_QUERY_OK_PROMPT);
                    Thread.sleep(CONFIG_DELAY);

                    TinygDriver.getInstance().write(CMD_QUERY_OK_PROMPT);
                    TinygDriver.getInstance().write(CMD_QUERY_AXIS_Y);
                    Main.logger.info("[+]Getting Y AXIS Settings");
                    ser.write(TinygDriver.CMD_QUERY_OK_PROMPT);
                    Thread.sleep(CONFIG_DELAY);

                    TinygDriver.getInstance().write(CMD_QUERY_OK_PROMPT);
                    Main.logger.info("[+]Getting Z AXIS Settings");
                    TinygDriver.getInstance().write(CMD_QUERY_AXIS_Z);
                    ser.write(TinygDriver.CMD_QUERY_OK_PROMPT);
                    Thread.sleep(CONFIG_DELAY);

                } catch (Exception ex) {
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

            } else if (_gp.getChildren().get(i).getClass().toString().contains("ChoiceBox")) {
                //This ia a ChoiceBox... Lets get the value and apply it if it needs to be applied.
                ChoiceBox cb = (ChoiceBox) _gp.getChildren().get(i);
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
        Thread.sleep(100);


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
                Main.logger.error("Invalid Motor Number.. Please try again..");
                setChanged();
//                notifyObservers("{\"error\":\"Invalid Motor Number\"}");
            }
        } catch (Exception ex) {
            Main.logger.error(ex.getMessage());
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

        for (i = 0; i < size; i++) {
            System.out.println("TEST");
            if (_gp.getChildren().get(i).toString().contains("TextField")) {
                TextField tf = (TextField) _gp.getChildren().get(i);
                try {
                    applyHardwareMotorSettings(_motor, tf);
                } catch (Exception _ex) {
                    System.out.println("[!]Exception in applyHardwareMotorSettings(Tab _tab)");
                }
            } else if (_gp.getChildren().get(i).toString().contains("ChoiceBox")) {
                ChoiceBox _cb = (ChoiceBox) _gp.getChildren().get(i);
                if (_cb.getId().contains("MapAxis")) {
                    int axisMap;
                    switch (_cb.getSelectionModel().getSelectedItem().toString()) {
                        case "X":
                            axisMap = 0;
                            break;
                        case "Y":
                            axisMap = 1;
                            break;
                        case "Z":
                            axisMap = 2;
                            break;
                        case "A":
                            axisMap = 3;
                            break;
                        case "B":
                            axisMap = 4;
                            break;
                        case "C":
                            axisMap = 5;
                            break;
                        default:
                            axisMap = 0;  //Defaults to map to X
                    }
                    String configObj = String.format("{\"%s%s\":%s}\n", _motorNumber, MNEMONIC_AXIS_AXIS_MODE, axisMap);
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

                //_cb.getSelectionModel().getSelectedItem()
                System.out.println("Test");

            }
        }
//        Motor _motor = this.m.getAxisByName(String.valueOf(_gp.getId().charAt(0)));
//        int i;
//        for (i = 0; i < size; i++) {
//            if (_gp.getChildren().get(i).getClass().toString().contains("TextField")) {
//                //This ia a TextField... Lets get the value and apply it if it needs to be applied.
//                TextField tf = (TextField) _gp.getChildren().get(i);
//                applyHardwareAxisSettings(_axis, tf);
//
//            } else if (_gp.getChildren().get(i).getClass().toString().contains("ChoiceBox")) {
//                //This ia a ChoiceBox... Lets get the value and apply it if it needs to be applied.
//                ChoiceBox cb = (ChoiceBox) _gp.getChildren().get(i);
//            }
//        }
//    
//        



    }

    public void applyHardwareMachineSettings() {
        /**
         * Apply Machine Settings to TinyG from GUI
         */
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
//            this.write(TinygDriver.CMD_QUERY_OK_PROMPT);
        } catch (Exception ex) {
            System.out.println("[!]Error in queryHardwareSingleMotorSettings() " + ex.getMessage());
        }
    }

    private int waitForSpace(String command) {
        lock.lock();
        this.ouputBuffer.add(command);
        try {
            freespace -= command.length();
            while (freespace < 0) {
                clearToSend.await();
            }
            return freespace;
        } catch (InterruptedException intex) {
            intex.printStackTrace();
            return ERROR;
        } finally {
            lock.unlock();
        }
    }

    public int approximateFreespace() {
        return freespace;
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

    // End Singleton
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
        // queueReader.setRun(false); //Kill the reader thread.
    }

    public boolean isConnected() {
        return this.ser.isConnected();
    }

    /**
     * All Methods involving writing to TinyG.. This messages will call the
     * SerialDriver write methods from here.
     */
    public void write(String msg) throws Exception {
        // if (msg.contains("gc")) {
        // TinygDriver.getInstance().setClearToSend(false);

        //Flow.logger.debug("waiting for space");
        int spaceAvailable = waitForSpace(msg);
        Main.logger.debug("wrote " + msg.length() + " byte message, " + spaceAvailable + " bytes available in hardware buffer");
        ser.write(msg);

        // } else {
        // ser.write(msg);
        // ser.write(TinygDriver.CMD_QUERY_OK_PROMPT);
        // Thread.sleep(10);
        // }
    }

    public synchronized void setClearToSend(boolean choice) throws Exception {
        ClearToSend.set(choice);
    }

    public synchronized boolean getClearToSend() {
        return ClearToSend.get();
    }

    public void priorityWrite(String msg) throws Exception {
        ser.priorityWrite(msg);
    }

    /**
     * Utility Methods
     *
     * @return
     */
    public String[] listSerialPorts() {
        // Get a listing current system serial ports
        String portArray[] = null;
        portArray = ser.listSerialPorts();
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
            // We toss these sleeps in to give the hardware (tg) time to
            // respond and have their values parsed.
            this.write(CMD_QUERY_OK_PROMPT);

            System.out.println("[+]Getting A AXIS Settings");
            this.ser.write(CMD_QUERY_AXIS_A);
            Thread.sleep(50);

            System.out.println("[+]Getting B AXIS Settings");
            this.ser.write(CMD_QUERY_AXIS_B);
            Thread.sleep(50);

            System.out.println("[+]Getting C AXIS Settings");
            this.ser.write(CMD_QUERY_AXIS_C);
            Thread.sleep(50);

            this.ser.write(CMD_QUERY_AXIS_X);
            System.out.println("[+]Getting X AXIS Settings");
            Thread.sleep(50);

            this.ser.write(CMD_QUERY_AXIS_Y);
            System.out.println("[+]Getting Y AXIS Settings");
            Thread.sleep(50);

            System.out.println("[+]Getting Z AXIS Settings");
            this.ser.write(CMD_QUERY_AXIS_Z);
            Thread.sleep(50);

        } catch (Exception ex) {
            System.out.println("[!]Error in queryHardwareAxisSettings()");
        }
    }

    // public void requestStatusUpdate() {
    // try {
    // ser.write(CMD_QUERY_STATUS_REPORT);
    // setChanged();
    // notifyObservers("HEARTBEAT");
    // } catch (Exception e) {
    // System.out.println(e.getMessage());
    // }
    // }
    public void queryHardwareMachineSettings() {
        try {
            ser.write(CMD_QUERY_MACHINE_SETTINGS);
            // ser.write(CMD_QUERY_OK_PROMPT); //This is required as
            // "status reports" do not return an "OK" msg

            ser.write(CMD_QUERY_HARDWARE_BUILD_NUMBER); // If TinyG current
            // positions are other
            // than zero
            // ser.write(CMD_QUERY_OK_PROMPT); //This is required as
            // "status reports" do not return an "OK" msg

            ser.write(CMD_QUERY_HARDWARE_FIRMWARE_NUMBER); // If TinyG current
            // positions are
            // other than zero
            // ser.write(CMD_QUERY_OK_PROMPT); //This is required as
            // "status reports" do not return an "OK" msg

            ser.write(CMD_QUERY_COORDINATE_SYSTEM);
            // ser.write(CMD_QUERY_OK_PROMPT); //This is required as
            // "status reports" do not return an "OK" msg
            // setChanged();
            // notifyObservers("CMD_GET_MACHINE_SETTINGS");
        } catch (Exception e) {
            System.out.println("ERROR Writing to Serial Port in getMachineSettings");
        }
    }
}
