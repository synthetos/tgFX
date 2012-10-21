/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import tgfx.Main;
import tgfx.tinyg.TinygDriver;
import tgfx.system.Axis;
import tgfx.system.StatusCode;

/**
 *
 * @author ril3y
 */
public class ResponseParser extends Observable implements Runnable {

//    private BlockingQueue jsonQueue = new ArrayBlockingQueue(1024);
    private BlockingQueue responseQueue;
    private JdomParser JDOM = new JdomParser(); //JSON Object Parser
    boolean RUN = true;
    String buf = "";
    private ResponseHeader responseHeader = new ResponseHeader();  //our holder for ResponseHeader Data
    private static Logger logger = Logger.getLogger(ResponseParser.class);

    public void appendJsonQueue(String jq) {
        try {
            this.responseQueue.put(jq);

        } catch (Exception ex) {
            logger.error("ERROR in appendJsonQueue", ex);
        }
    }

    public ResponseParser(BlockingQueue bq) {
        //Default constructor
        responseQueue = bq;

    }

    @Override
    public void run() {
        logger.info("Respone Parser Running");
        String line;

        while (RUN) {
            try {
                parseJSON((String) responseQueue.take());  //Take a line from the response queue when its ready and parse it.

            } catch (Exception ex) {
                logger.error("[!]Error in responseParser run()");
            }
        }
    }

    public synchronized void parseJSON(String line) {
        String axis;
        String[] statusResponse;
        int motor;
        try {
            //Create our JSON Parsing Object
            JsonRootNode json = JDOM.parse(line);
            responseHeader.parseResponseHeader(json);

//            logger.debug("Response Header Line Number: " + responseHeader.getLineNumber());

            switch (ResponseHeader.getStatusCode()) {
                case -1:
                    logger.info("[!]Error Parsing JSON line: " + line);
                    return;
                case 40:
                    logger.info("[!]" + responseHeader.getStatusMessage() + " ignoring rest of JSON message..");
                    return;
                case 60:
                    logger.info("[!]Zero Length Line");
                    setChanged();
                    StatusCode sc = new StatusCode(60, "Zero Length Line", line, "Illegal Gcode");
                    notifyObservers(sc);
                default:

            }
//            if (line.contains("bd\":{\"\":\"\"}")) {
//                //Move on
//            } else

            if (line.contains("SYSTEM READY")) {
                //This will be moved to the switch statement above when we assign "SYSTEM READY" a status code.
                setChanged();
                StatusCode sc = new StatusCode(0, "System Ready", "", "TinyG Message");
                notifyObservers(sc);
            } /**
             * ###################CRITICAL#########################*
             */
            else if (line.startsWith(TinygDriver.RESPONSE_QUEUE_REPORT)) {
                //TinyG Input Planning Queue Reporting... Queue Reports are for flow control
                //LOCK THE DAMN SERIALWRITER!
//                TinygDriver.getInstance().serialWriter.lock.lock();
                try {
//                    logger.debug("Locking...");
                    TinygDriver.getInstance().qr.updateQueue(json, line);
                    logger.info("GOT A QR UPDATE qr: PBA:" + TinygDriver.getInstance().qr.getPba() + " Line: " + TinygDriver.getInstance().qr.getLineIndex());
                    if (TinygDriver.getInstance().qr.getPba() <= 2) {
                        if (TinygDriver.getInstance().serialWriter.setThrottled(true))
                          logger.info("Throttled for PBA");
                    } else {
                        if (TinygDriver.getInstance().serialWriter.setThrottled(false))
                            logger.info("Unthrottled for PBA");                        
                    }
//                    TinygDriver.getInstance().serialWriter.resetLinesSentBeforeUpdate();
//                    TinygDriver.getInstance().serialWriter.clearToSend.signal();
                } finally {
//                    TinygDriver.getInstance().serialWriter.lock.unlock();
                }
                /**
                 * ###################CRITICAL#########################*
                 */
            } else if (line.contains(TinygDriver.RESPONSE_STATUS_REPORT)) {
                //Parse Status Report
                //"{"sr":{"line":0,"xpos":1.567,"ypos":0.548,"zpos":0.031,"apos":0.000,"vel":792.463,"unit":"mm","stat":"run"}}"
                TinygDriver.getInstance().m.getAxisByName("X").setWork_position(Float.parseFloat(json.getNode("r").getNode("bd").getNode("sr").getNode("posx").getText()));
                TinygDriver.getInstance().m.getAxisByName("Y").setWork_position(Float.parseFloat(json.getNode("r").getNode("bd").getNode("sr").getNode("posy").getText()));
                TinygDriver.getInstance().m.getAxisByName("Z").setWork_position(Float.parseFloat(json.getNode("r").getNode("bd").getNode("sr").getNode("posz").getText()));
                TinygDriver.getInstance().m.getAxisByName("A").setWork_position(Float.parseFloat(json.getNode("r").getNode("bd").getNode("sr").getNode("posa").getText()));
                TinygDriver.getInstance().m.setMachineState(Integer.valueOf(json.getNode("r").getNode("bd").getNode("sr").getNode("stat").getText()));
                TinygDriver.getInstance().m.setMotionMode(Integer.parseInt(json.getNode("r").getNode("bd").getNode("sr").getNode("momo").getText()));
                TinygDriver.getInstance().m.setVelocity(Float.parseFloat(json.getNode("r").getNode("bd").getNode("sr").getNode("vel").getText()));
                TinygDriver.getInstance().m.setUnits(Integer.parseInt(json.getNode("r").getNode("bd").getNode("sr").getNode("unit").getText()));
                TinygDriver.getInstance().m.setCoordinate_mode(Integer.parseInt(json.getNode("r").getNode("bd").getNode("sr").getNode("coor").getText()));
                //m.getAxisByName("X").setWork_position(Float.parseFloat((json.getNode("r").getNode("bd").getNode("sr").getNode("xpos").getText())));
                //m.getAxisByName("Y").setWork_position(Float.parseFloat((json.getNode("r").getNode("bd").getNode("sr").getNode("ypos").getText())));
                //m.getAxisByName("Z").setWork_position(Float.parseFloat((json.getNode("r").getNode("bd").getNode("sr").getNode("zpos").getText())));
                //this.A_AXIS.setWork_position(Float.parseFloat((json.getNode("r").getNode("bd").getNode("sr").getNode("awp").getText())));
                setChanged();
                notifyObservers("STATUS_REPORT");

            } else if (line.startsWith(TinygDriver.RESPONSE_MACHINE_SETTINGS)) {
                logger.info("[#]Parsing Machine Settings JSON");
                //{"fv":0.930,"fb":330.190,"si":30,"gi":"21","gs":"17","gp":"64","ga":"90","ea":1,"ja":200000.000,"ml":0.080,"ma":0.100,"mt":10000.000,"ic":0,"il":0,"ec":0,"ee":0,"ex":1}
                TinygDriver.getInstance().m.setFirmware_version(Float.parseFloat(json.getNode("r").getNode("bd").getNode("sys").getNode("fv").getText()));
                TinygDriver.getInstance().m.setFirmware_build(Float.parseFloat(json.getNode("r").getNode("bd").getNode("sys").getNode("fb").getText()));
                TinygDriver.getInstance().m.setStatus_report_interval(Integer.parseInt(json.getNode("r").getNode("bd").getNode("sys").getNode("si").getText()));
                TinygDriver.getInstance().m.setEnable_acceleration(Boolean.parseBoolean(json.getNode("r").getNode("bd").getNode("sys").getNode("ex").getText()));
                TinygDriver.getInstance().m.setJunction_acceleration(Float.parseFloat((json.getNode("r").getNode("bd").getNode("sys").getNode("ml").getText())));
                TinygDriver.getInstance().m.setMin_segment_time(Double.parseDouble(json.getNode("r").getNode("bd").getNode("sys").getNode("mt").getText()));
                TinygDriver.getInstance().m.setMin_arc_segment(Float.parseFloat((json.getNode("r").getNode("bd").getNode("sys").getNode("ma").getText())));
                TinygDriver.getInstance().m.setIgnore_cr_lf_RX(Integer.parseInt(json.getNode("r").getNode("bd").getNode("sys").getNode("ic").getText()));  //Check this.
                TinygDriver.getInstance().m.setEnable_CR_on_TX(Boolean.parseBoolean((json.getNode("r").getNode("bd").getNode("sys").getNode("ec").getText())));
                TinygDriver.getInstance().m.setEnable_echo(Boolean.parseBoolean((json.getNode("r").getNode("bd").getNode("sys").getNode("ee").getText())));
                TinygDriver.getInstance().m.setEnable_xon_xoff(Boolean.parseBoolean((json.getNode("r").getNode("bd").getNode("sys").getNode("ex").getText())));
                setChanged();
                notifyObservers("CMD_GET_MACHINE_SETTINGS");

            } /**
             * Start Checking for Motor Responses
             */
            else if (line.startsWith(TinygDriver.RESPONSE_MOTOR_1) && !line.contains("null")) {
                motor = 1;
                parseJsonMotorSettings(line, motor);
            } else if (line.startsWith(TinygDriver.RESPONSE_MOTOR_2) && !line.contains("null")) {
                motor = 2;
                parseJsonMotorSettings(line, motor);
            } else if (line.startsWith(TinygDriver.RESPONSE_MOTOR_3) && !line.contains("null")) {
                motor = 3;
                parseJsonMotorSettings(line, motor);
            } else if (line.startsWith(TinygDriver.RESPONSE_MOTOR_4) && !line.contains("null")) {
                motor = 4;
                parseJsonMotorSettings(line, motor);
            } else if (line.startsWith(TinygDriver.RESPONSE_MOTOR_5) && !line.contains("null")) {
                motor = 5;
                parseJsonMotorSettings(line, motor);
            } else if (line.startsWith(TinygDriver.RESPONSE_MOTOR_6) && !line.contains("null")) {
                motor = 6;
                parseJsonMotorSettings(line, motor);
            } /**
             * Start Checking for Axis Responses
             */
            else if (line.startsWith(TinygDriver.RESPONSE_AXIS_X) && !line.contains("null")) {
                axis = "x";
                parseJsonAxisSettings(line, axis);
            } else if (line.startsWith(TinygDriver.RESPONSE_AXIS_Y) && !line.contains("null")) {
                axis = "y";
                parseJsonAxisSettings(line, axis);
            } else if (line.startsWith(TinygDriver.RESPONSE_AXIS_Z) && !line.contains("null")) {
                axis = "z";
                parseJsonAxisSettings(line, axis);
            } else if (line.startsWith(TinygDriver.RESPONSE_AXIS_A) && !line.contains("null")) {
                axis = "a";
                parseJsonAxisSettings(line, axis);
            } else if (line.startsWith(TinygDriver.RESPONSE_AXIS_B) && !line.contains("null")) {
                axis = "b";
                parseJsonAxisSettings(line, axis);
            } else if (line.startsWith(TinygDriver.RESPONSE_AXIS_C) && !line.contains("null")) {
                axis = "c";
                parseJsonAxisSettings(line, axis);
            }





        } catch (argo.saj.InvalidSyntaxException ex) {
            //This will happen from time to time depending on the file that is being sent to TinyG
            //This is an issue mostly when the lines are very very small and there are many of them
            //and you are running at a high feedrate.
            logger.error("[!]ParseJson Exception: " + ex.getMessage() + " LINE: " + line);
//            setChanged();
//            notifyObservers("[!] #" + ex.getMessage() +"#"+ line + "\n");

            //UGLY BUG FIX WORKAROUND FOR NOW
            //Code to fix a possible JSON TinyG Error
//            if (line.contains("msg")) {
//                try {
//
//                    TinygDriver.getInstance().ser.setClearToSend(true);
//                } catch (Exception ex1) {
//                    logger.error("EXCEPTION IN BUG FIX CODE TINYGDRIVER" + ex1.getMessage());
//                }
//            }
            //UGLY BUG FIX WORKAROUND FOR NOW



        } catch (argo.jdom.JsonNodeDoesNotMatchPathElementsException ex) {
            //Extra } for some reason
            logger.error("[!]ParseJson Exception: " + ex.getMessage() + " LINE: " + line);
            setChanged();
            notifyObservers("[!]ParserJson Exception #" + ex.getMessage() + "#" + line + "\n");

        } catch (Exception ex) {
            setChanged();
            notifyObservers("ERROR");
            logger.error("Exception in TinygDriver", ex);
        }
    }

    private synchronized void parseJsonAxisSettings(String line, String axis) throws InvalidSyntaxException {
        /**
         * When an axis is queried by tg this is the method that parses the
         * response and populates the machine model with the axis values.
         * {"a":{"am":1,"fr":36000.000,"vm":36000.000,"tm":-1.000,"jm":100000000.000,"jd":0.050,"ra":10.000,"sm":0,"sv":36000.000,"lv":3600.000,"lb":0.000,"zb":0.000}}
         */
        JsonRootNode json = JDOM.parse(line);
        Axis ax = TinygDriver.getInstance().m.getAxisByName(axis.toUpperCase());

        //m.getMotorByNumber(motor).setMapToAxis(Integer.valueOf((json.getNode(strMotor).getNode("ma").getText())));
        ax.setAxis_mode(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_AXIS_MODE).getText())).intValue());
        ax.setFeed_rate_maximum(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_FEEDRATE_MAXIMUM).getText())));
        ax.setVelocity_maximum(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_VELOCITY_MAXIMUM).getText())));
        ax.setTravel_maximum(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_TRAVEL_MAXIMUM).getText())));
        ax.setJerk_maximum(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_JERK_MAXIMUM).getText())).intValue());
        ax.setJunction_devation(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_JUNCTION_DEVIATION).getText())));
        Boolean setSwitch_mode = ax.setSwitch_mode(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_SWITCH_MODE).getText())).intValue());
        Boolean setSearch_velocity = ax.setSearch_velocity(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_SEARCH_VELOCITY).getText())).intValue());
        ax.setLatch_velocity(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_LATCH_VELOCITY).getText())));
        ax.setLatch_backoff(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_LATCH_BACKOFF).getText())));
        ax.setZero_backoff(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_ZERO_BACKOFF).getText())));

        if (ax.getAxisType().equals(Axis.AXIS_TYPE.ROTATIONAL)) {
            ax.setRadius(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_RADIUS).getText())));
        }
        setChanged();
        notifyObservers("CMD_GET_AXIS_SETTINGS");

    }

    private synchronized void parseJsonMotorSettings(String line, int motor) throws InvalidSyntaxException {
        //{"1":{"ma":0,"sa":1.800,"tr":1.250,"mi":8,"po":0,"pm":1}}
        JsonRootNode json = JDOM.parse(line);
        String strMotor = String.valueOf(motor);
        TinygDriver.getInstance().m.getMotorByNumber(motor).setCURRENT_MOTOR_JSON_OBJECT(line.split("bd\":")[1].split(",\"sc")[0]); //Get us or current json line. This isfor
        //saving settings in the UI
        try {



            TinygDriver.getInstance().m.getMotorByNumber(motor).setMapToAxis(Integer.valueOf((json.getNode("r").getNode("bd").getNode(strMotor).getNode(TinygDriver.MNEMONIC_MOTOR_MAP_AXIS).getText())));
            TinygDriver.getInstance().m.getMotorByNumber(motor).setStep_angle(Float.valueOf(json.getNode("r").getNode("bd").getNode(strMotor).getNode("sa").getText()));
            TinygDriver.getInstance().m.getMotorByNumber(motor).setTravel_per_revolution(Float.valueOf(json.getNode("r").getNode("bd").getNode(strMotor).getNode("tr").getText()));
            TinygDriver.getInstance().m.getMotorByNumber(motor).setPolarity(Integer.valueOf((json.getNode("r").getNode("bd").getNode(strMotor).getNode("po").getText())));
            TinygDriver.getInstance().m.getMotorByNumber(motor).setPower_management(Integer.valueOf((json.getNode("r").getNode("bd").getNode(strMotor).getNode("pm").getText())));
            TinygDriver.getInstance().m.getMotorByNumber(motor).setMicrosteps(Integer.valueOf(json.getNode("r").getNode("bd").getNode(strMotor).getNode("mi").getText()));
        } catch (java.lang.NumberFormatException ex) {
            TinygDriver.getInstance().m.getMotorByNumber(motor).setMapToAxis(Float.valueOf(json.getNode("r").getNode("bd").getNode(strMotor).getNode(TinygDriver.MNEMONIC_MOTOR_MAP_AXIS).getText()).intValue());
            TinygDriver.getInstance().m.getMotorByNumber(motor).setStep_angle(Float.valueOf(json.getNode("r").getNode("bd").getNode(strMotor).getNode("sa").getText()));
            TinygDriver.getInstance().m.getMotorByNumber(motor).setTravel_per_revolution(Float.valueOf(json.getNode("r").getNode("bd").getNode(strMotor).getNode("tr").getText()));
            TinygDriver.getInstance().m.getMotorByNumber(motor).setPolarity(Float.valueOf((json.getNode("r").getNode("bd").getNode(strMotor).getNode("po").getText())).intValue());
            TinygDriver.getInstance().m.getMotorByNumber(motor).setPower_management(Float.valueOf((json.getNode("r").getNode("bd").getNode(strMotor).getNode("pm").getText())).intValue());
            TinygDriver.getInstance().m.getMotorByNumber(motor).setMicrosteps(Float.valueOf(json.getNode("r").getNode("bd").getNode(strMotor).getNode("mi").getText()).intValue());
        }
        //TODO: Add support for new switch modes all 4 of them.


        setChanged();
        notifyObservers("CMD_GET_MOTOR_SETTINGS");
    }

    void parseResponseLine(byte[] chunk) throws Exception {
        String json = "";
        String[] lines;

        int chunkLength = chunk.length;
        lines = (new String(chunk)).split("\n");   //Convert our byte array to a string[] that split on "\n"

        for (String linebuffer : lines) {
            normalizeResponseLine(linebuffer);
        }
    }

    void normalizeResponseLine(String line) throws Exception {

        if (line.startsWith("{\"") && line.endsWith("}}") && buf.equals("")) {  //The buf check makes sure
            //The serial event didn't not cut off at the perfect spot and send something like this:
            //"{"gc":{"gc":"F300.0","st":0,"msg":"OK"}}  
            //Which is missing the front part of that line "{"gc":
            buf = "";  //Valid line clear the buffer
            parseJSON(line);
        } else if (line.startsWith("{\"") && line.endsWith("}")) {
            //This is a input command
            //{"ee":"1"}
            buf = "";
            parseJSON(line);

        } else if (line.startsWith("{\"")) {
            //System.out.println("!! GCODE LINE STARTS WITH { !!" + l);
            buf = line;

        } else if (line.endsWith("}}")) {
            //System.out.println("!! GCODE LINE ENDS WITH { !!" + l);
            buf = buf + line;
            if (buf.startsWith("{\"") && buf.endsWith("}}")) {
                parseJSON(buf);
                buf = "";
            } else {
                System.out.println("SERIAL DRIVER CODE: SHOULD NOT HIT THIS");
                System.out.println(buf);
            }
        } else {
            //If we happen to get a single { as a line this code puts it into the buf var.
            //
            buf = line;
        }
    }
}
