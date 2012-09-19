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
import tgfx.system.Axis;

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
    private ResponseHeader responseHeader =  new ResponseHeader();  //our holder for ResponseHeader Data
    
    public void appendJsonQueue(String jq) {
        try {
            this.responseQueue.put(jq);

        } catch (Exception ex) {
            Main.logger.error("ERROR in appendJsonQueue(): " + ex.getMessage());
        }
    }

    public ResponseParser(BlockingQueue bq) {
        //Default constructor
        responseQueue = bq;
        
    }

    @Override
    public void run() {
        Main.logger.info("Respone Parser Running");
        String line;
       
        while (RUN) {

            //DEBUGGING FOR NEW STUFF
                /*
             * set clear_to_send to true
             *
             *
             * while(!inputQueue.isEmpty()){
             *
             * if(CLEAR_TO_SEND.equals(true){ grab Line out of inputQueue() push
             * line to hardware aka wirte()
             *
             * //serial.read(until \n) Okrecieved = "ok" //Read hardware /*
             *
             *
             * if(hardware says ok in it){ then set clear_to_send == true }else
             * if(status_report){ push sr into messageQueue
             *
             * //
             *
             * //DEBUGGING FOR NEW STUFF
             *
             *
             */


            try {
                parseJSON((String) responseQueue.take());  //Take a line from the response queue when its ready and parse it.

            } catch (Exception ex) {
                Main.logger.error("[!]Error in responseParser run()");
            }
        }
    }

//    private String[] getStatusMessage(JsonRootNode json) {
//        /**
//         * This function parses all return status codes and messages before
//         * anything else
//         */
//        
//        ResponseHeader responseHeader =  new ResponseHeader(json);
//        
//        try {
//            
//            
//            String[] ret = {responseHeader, statusCode};
//            return (ret);
//        } catch (Exception ex) {
//            String[] ret = {"JSON Invalid", "-1"};
//            return (ret);
//        }
//    }

    public synchronized void parseJSON(String line) {
        String axis;
        String[] statusResponse;
        int motor;
        try {
            //Create our JSON Parsing Object
            JsonRootNode json = JDOM.parse(line);
            responseHeader.parseResponseHeader(json);
            TinygDriver.getInstance().commandComplete(responseHeader);

            //This is a way to catch status codes that we do not want to parse out the rest of the message
            //40 is an Unrecognized Command
            //-1 is an error in parsing the json
            
            switch (responseHeader.getStatusCode()) {
                case -1:
                    Main.logger.info("[!]Error Parsing JSON line: " + line);
                    return;
                case 40:
                    Main.logger.info("[!]" + responseHeader.getStatusMessage() + " ignoring rest of JSON message..");
                    return;
                default:
                    
            }

            if (line.contains(TinygDriver.RESPONSE_STATUS_REPORT)) {
                //Parse Status Report
                //"{"sr":{"line":0,"xpos":1.567,"ypos":0.548,"zpos":0.031,"apos":0.000,"vel":792.463,"unit":"mm","stat":"run"}}"
                TinygDriver.getInstance().m.getAxisByName("X").setWork_position(Float.parseFloat(json.getNode("r").getNode("bd").getNode("sr").getNode("posx").getText()));
                TinygDriver.getInstance().m.getAxisByName("Y").setWork_position(Float.parseFloat(json.getNode("r").getNode("bd").getNode("sr").getNode("posy").getText()));
                TinygDriver.getInstance().m.getAxisByName("Z").setWork_position(Float.parseFloat(json.getNode("r").getNode("bd").getNode("sr").getNode("posz").getText()));
                TinygDriver.getInstance().m.getAxisByName("A").setWork_position(Float.parseFloat(json.getNode("r").getNode("bd").getNode("sr").getNode("posa").getText()));

//               TinygDriver.getInstance().m.getAxisByName("B").setWork_position(Float.parseFloat(json.getNode("r").getNode("bd").getNode("sr").getNode("posa").getText()));
//               TinygDriver.getInstance().m.getAxisByName("C").setWork_position(Float.parseFloat(json.getNode("r").getNode("bd").getNode("sr").getNode("posa").getText()));

                //Parse state out of status report.
                TinygDriver.getInstance().m.setMachineState(Integer.valueOf(json.getNode("r").getNode("bd").getNode("sr").getNode("stat").getText()));

                //Parse motion mode (momo) out of start report
                TinygDriver.getInstance().m.setMotionMode(Integer.parseInt(json.getNode("r").getNode("bd").getNode("sr").getNode("momo").getText()));

                //Parse velocity out of status report
                TinygDriver.getInstance().m.setVelocity(Float.parseFloat(json.getNode("r").getNode("bd").getNode("sr").getNode("vel").getText()));

                //Parse Unit Mode
                TinygDriver.getInstance().m.setUnits(Integer.parseInt(json.getNode("r").getNode("bd").getNode("sr").getNode("unit").getText()));
                TinygDriver.getInstance().m.setCoordinate_mode(Integer.parseInt(json.getNode("r").getNode("bd").getNode("sr").getNode("coor").getText()));


                //m.getAxisByName("X").setWork_position(Float.parseFloat((json.getNode("r").getNode("bd").getNode("sr").getNode("xpos").getText())));
                //m.getAxisByName("Y").setWork_position(Float.parseFloat((json.getNode("r").getNode("bd").getNode("sr").getNode("ypos").getText())));
                //m.getAxisByName("Z").setWork_position(Float.parseFloat((json.getNode("r").getNode("bd").getNode("sr").getNode("zpos").getText())));
                //this.A_AXIS.setWork_position(Float.parseFloat((json.getNode("r").getNode("bd").getNode("sr").getNode("awp").getText())));
                setChanged();
                notifyObservers("STATUS_REPORT");

            } else if (line.startsWith(TinygDriver.RESPONSE_MACHINE_FIRMWARE_BUILD)) {
                Main.logger.info("[#]Parsing Machine Settings....");
                TinygDriver.getInstance().m.setFirmware_build(Float.parseFloat(json.getNode("r").getNode("bd").getNode("fb").getText()));
                setChanged();
                notifyObservers("MACHINE_UPDATE");
            } else if (line.startsWith(TinygDriver.RESPONSE_MACHINE_FIRMWARE_BUILD)) {
                Main.logger.info("[#]Parsing Build Number...");
                TinygDriver.getInstance().m.setFirmware_build(Float.parseFloat(json.getNode("r").getNode("bd").getNode("fb").getText()));
                setChanged();
                notifyObservers("MACHINE_UPDATE");


            } else if (line.startsWith(TinygDriver.RESPONSE_MACHINE_FIRMWARE_VERSION)) {
                Main.logger.info("[#]Parsing Version...");
                TinygDriver.getInstance().m.setFirmware_version(Float.parseFloat(json.getNode("r").getNode("bd").getNode("fv").getText()));
                setChanged();
                notifyObservers("MACHINE_UPDATE");


            } else if (line.startsWith(TinygDriver.RESPONSE_MACHINE_SETTINGS)) {
                Main.logger.info("[#]Parsing Machine Settings JSON");
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
            Main.logger.error("[!]ParseJson Exception: " + ex.getMessage() + " LINE: " + line);
            setChanged();
            notifyObservers("[!] " + ex.getMessage() + "Line Was: " + line + "\n");

            //UGLY BUG FIX WORKAROUND FOR NOW
            //Code to fix a possible JSON TinyG Error
//            if (line.contains("msg")) {
//                try {
//
//                    TinygDriver.getInstance().ser.setClearToSend(true);
//                } catch (Exception ex1) {
//                    Main.logger.error("EXCEPTION IN BUG FIX CODE TINYGDRIVER" + ex1.getMessage());
//                }
//            }
            //UGLY BUG FIX WORKAROUND FOR NOW



        } catch (argo.jdom.JsonNodeDoesNotMatchPathElementsException ex) {
            //Extra } for some reason
            Main.logger.error("[!]ParseJson Exception: " + ex.getMessage() + " LINE: " + line);
            setChanged();
            notifyObservers("[!] " + ex.getMessage() + "Line Was: " + line + "\n");

        } catch (Exception ex) {
            setChanged();
            notifyObservers("ERROR");
            Main.logger.error("Exception in TinygDriver");
            Main.logger.error(ex.getMessage());
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
        ax.setAxis_mode(Integer.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_AXIS_MODE).getText())));
        ax.setFeed_rate_maximum(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_FEEDRATE_MAXIMUM).getText())));
        ax.setVelocity_maximum(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_VELOCITY_MAXIMUM).getText())));
        ax.setTravel_maximum(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_TRAVEL_MAXIMUM).getText())));
        ax.setJerk_maximum(Double.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_JERK_MAXIMUM).getText())));

        //This is a bug fix.  This was messed up in firmware builds < 338.05
        //This will go away eventually
        //        if (ax.getAxis_name().equals("B")) {
        //            //This is not correct.  This should not be "cd" but "jd"
        //            ax.setJunction_devation(Float.valueOf((json.getNode(axis).getNode("cd").getText())));
        //        } else {
        //            //This is the correct syntax
        ax.setJunction_devation(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_JUNCTION_DEVIATION).getText())));
        //        }
        Boolean setSwitch_mode = ax.setSwitch_mode(Integer.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_SWITCH_MODE).getText())));
        Boolean setSearch_velocity = ax.setSearch_velocity(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_SEARCH_VELOCITY).getText())));

        //This is a bug fix.  This was messed up in firmware builds < 338.05
        //This will go away eventually
        //        if (ax.getAxis_name().equals("C")) {
        //            ax.setLatch_velocity(Float.valueOf((json.getNode(axis).getNode("ls").getText())));
        //        } else {
        //This is the correct syntax
        ax.setLatch_velocity(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_LATCH_VELOCITY).getText())));
        //        } 

        ax.setLatch_backoff(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_LATCH_BACKOFF).getText())));
        ax.setZero_backoff(Float.valueOf((json.getNode("r").getNode("bd").getNode(axis).getNode(TinygDriver.MNEMONIC_AXIS_ZERO_BACKOFF).getText())));
        //        if (ax.getAxisType() == Axis.AXIS_TYPE.ROTATIONAL) {
        //            ax = (RotationalAxis) ax;
        //            RotationalAxis.AX.setRadius(Float.valueOf((json.getNode(axis).getNode("ra").getText())));
        //        }

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


        TinygDriver.getInstance().m.getMotorByNumber(motor).setMapToAxis(Integer.valueOf((json.getNode("r").getNode("bd").getNode(strMotor).getNode(TinygDriver.MNEMONIC_MOTOR_MAP_AXIS).getText())));
        TinygDriver.getInstance().m.getMotorByNumber(motor).setStep_angle(Float.valueOf(json.getNode("r").getNode("bd").getNode(strMotor).getNode("sa").getText()));
        TinygDriver.getInstance().m.getMotorByNumber(motor).setTravel_per_revolution(Float.valueOf(json.getNode("r").getNode("bd").getNode(strMotor).getNode("tr").getText()));
        TinygDriver.getInstance().m.getMotorByNumber(motor).setPolarity(Integer.valueOf((json.getNode("r").getNode("bd").getNode(strMotor).getNode("po").getText())));
        TinygDriver.getInstance().m.getMotorByNumber(motor).setPower_management(Integer.valueOf((json.getNode("r").getNode("bd").getNode(strMotor).getNode("pm").getText())));
        TinygDriver.getInstance().m.getMotorByNumber(motor).setMicrosteps(Integer.valueOf(json.getNode("r").getNode("bd").getNode(strMotor).getNode("mi").getText()));

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
//        for (int i = 0; i < chunkLength - 1; i++) {
//            if (chunk[i] != 10 && i == chunkLength - 1) {
//                linebuffer = (json + String.valueOf((char) chunk[i]));
//
//            } else if (chunk[i] == 10) { //10 is a new line character
//                normalizeResponseLine(json);
//                Main.logger.info("LINE: == " + json);
//                json = "";
//            } else {
//                json = json + String.valueOf((char) chunk[i]);
//            }
//        }
//        //This was an invalid json object 
//        normalizeResponseLine(json);
//    }

    }

    void normalizeResponseLine(String line) throws Exception {
        //This function takes the line from the responseLine and forms it into a valid json object

        /**
         * Build JSON Lines
         */
        //This code strings together lines that do not start with valid json objects
//        Integer hashCode, calculatedHashCode;
//        if (line.contains("msg")) {
//            try {
//                TinygDriver.getInstance().setClearToSend();
//            } catch (Exception ex) {
//                Main.logger.error("[!]Error Setting Clear to Send in normalizeResponseLine()");
//            }
//        }  
//        
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







//            TinygDriver.getInstance().ser.setClearToSend(true); //These commands to no illicit a response with a "msg" in it.
            //We manually set it to clear to send.






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
//    private void getOKcheck(String l) throws Exception {
//        if (l.startsWith("{\"gc\":{\"gc\":")) {
//            //This is our "OK" buffer message.  If we get inside the code then we got a response
//            //From TinyG and we are good to push more data into TinyG.
//            TinygDriver.getInstance().ser.setClearToSend(true);  //Set the clear to send flag to True.
//            //DEBUG
////            setChanged();
////            notifyObservers("[+]Clear to Send Recvd.\n");
//            //DEBUG
//        } else {
////            setChanged();
////            notifyObservers(l + "\n");
//        }
//
//    }
}
