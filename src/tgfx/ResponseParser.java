/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import java.util.ArrayList;
import org.json.*;
import java.util.Iterator;
import java.util.Observable;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import javafx.application.Platform;
import jfxtras.labs.dialogs.DialogFX;

import org.apache.log4j.Logger;
import tgfx.tinyg.CommandManager;
import tgfx.tinyg.MnemonicManager;
import static tgfx.tinyg.MnemonicManager.MNEMONIC_GROUP_AXIS_A;
import static tgfx.tinyg.MnemonicManager.MNEMONIC_GROUP_AXIS_B;
import static tgfx.tinyg.MnemonicManager.MNEMONIC_GROUP_AXIS_C;
import static tgfx.tinyg.MnemonicManager.MNEMONIC_GROUP_AXIS_X;
import static tgfx.tinyg.MnemonicManager.MNEMONIC_GROUP_AXIS_Y;
import static tgfx.tinyg.MnemonicManager.MNEMONIC_GROUP_AXIS_Z;
import static tgfx.tinyg.MnemonicManager.MNEMONIC_GROUP_MOTOR_1;
import static tgfx.tinyg.MnemonicManager.MNEMONIC_GROUP_MOTOR_2;
import static tgfx.tinyg.MnemonicManager.MNEMONIC_GROUP_MOTOR_3;
import static tgfx.tinyg.MnemonicManager.MNEMONIC_GROUP_MOTOR_4;
import static tgfx.tinyg.MnemonicManager.MNEMONIC_GROUP_SYSTEM;
import static tgfx.tinyg.MnemonicManager.MNEMONIC_GROUP_STATUS_REPORT;
import static tgfx.tinyg.MnemonicManager.MNEMONIC_GROUP_EMERGENCY_SHUTDOWN;
import tgfx.tinyg.TinygDriver;
import tgfx.tinyg.responseCommand;

/**
 *
 * @author ril3y
 */
public class ResponseParser extends Observable implements Runnable {

//    private BlockingQueue jsonQueue = new ArrayBlockingQueue(1024);
    private String[] message = new String[2];
    private BlockingQueue responseQueue;
    boolean RUN = true;
    String buf = "";
    private ResponseFooter responseFooter = new ResponseFooter();  //our holder for ResponseFooter Data
    private static Logger logger = Logger.getLogger(ResponseParser.class);
    //These values are for mapping what n'Th element inthe json footer array maps to which values.
    private static final int FOOTER_ELEMENT_PROTOCOL_VERSION = 0;
    private static final int FOOTER_ELEMENT_STATUS_CODE = 1;
    private static final int FOOTER_ELEMENT_RX_RECVD = 2;
    private static final int FOOTER_ELEMENT_CHECKSUM = 3;
    private JSONArray footerValues;

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
        logger.info("Response Parser Running");
        String line;

        while (RUN) {
            try {
                parseJSON((String) responseQueue.take());  //Take a line from the response queue when its ready and parse it.

            } catch (InterruptedException | JSONException ex) {
                logger.error("[!]Error in responseParser run()");
            }
        }
    }

    private boolean isJsonObject(JSONObject js, String strVal) throws Exception {

        if (js.get(strVal).getClass().toString().contains("JSONObject")) {
            return true;
        } else {
            return false;
        }
    }

    public void applyStatusReport(JSONObject js) {
        try {
            //Set the status report values 
            TinygDriver.getInstance().m.getAxisByName("X").setWork_position(js.getDouble("posx"));
            TinygDriver.getInstance().m.getAxisByName("Y").setWork_position(js.getDouble("posy"));
            TinygDriver.getInstance().m.getAxisByName("Z").setWork_position(js.getDouble("posz"));
            TinygDriver.getInstance().m.getAxisByName("A").setWork_position(js.getDouble("posa"));
            TinygDriver.getInstance().m.setMachineState(js.getInt("stat"));
            TinygDriver.getInstance().m.setMotionMode(js.getInt("momo"));
            TinygDriver.getInstance().m.setVelocity(js.getDouble("vel"));


            setChanged();

            String[] message = new String[2];
            message[0] = "STATUS_REPORT";
            message[1] = null;
            notifyObservers(message);

        } catch (Exception ex) {
            logger.error("Error in ApplyStatusReport");

        }
    }

    public void applySetting(JSONObject js) {
        String parentGroup;
        try {
            if (js.has("gc") | js.has("qr")) {
                //this is a gcode line echo not a valid response... return now.
                return;
            }

            Iterator ii = js.keySet().iterator();
            if (js.keySet().size() > 1) {
                //This is a special multi single value response object
                while (ii.hasNext()) {
                    String key = ii.next().toString();
                    responseCommand rc = TinygDriver.getInstance().mneManager.lookupSingleGroup(key);
                    rc.setSettingValue(js.get(key).toString());
                    parentGroup = rc.getSettingParent();
                    _applySettings(rc.buildJsonObject(), rc.getSettingParent()); //we will supply the parent object name for each key pair

                }
            } else {
                _applySettings(js, ii); //this is a standard single group response object
            }



        } catch (Exception ex) {
            System.out.println("Apply Settings Exception.");
        }
    }

    private void _applySettings(JSONObject js, Iterator ii) throws Exception {
        String parentGroup = (String) ii.next();
        _applySettings(js, parentGroup);
    }

    private void _applySettings(JSONObject js, String pg) throws Exception {

        switch (pg) {
            case (MNEMONIC_GROUP_MOTOR_1):
                TinygDriver.getInstance().m.getMotorByNumber(MNEMONIC_GROUP_MOTOR_1)
                        .applyJsonSystemSetting(js.getJSONObject(MNEMONIC_GROUP_MOTOR_1), MNEMONIC_GROUP_MOTOR_1);
                setChanged();
                message[0] = "CMD_GET_MOTOR_SETTINGS";
                message[1] = MNEMONIC_GROUP_MOTOR_1;
                notifyObservers(message);
                break;
            case (MNEMONIC_GROUP_MOTOR_2):
                TinygDriver.getInstance().m.getMotorByNumber(MNEMONIC_GROUP_MOTOR_2)
                        .applyJsonSystemSetting(js.getJSONObject(MNEMONIC_GROUP_MOTOR_2), MNEMONIC_GROUP_MOTOR_2);
                setChanged();
                message[0] = "CMD_GET_MOTOR_SETTINGS";
                message[1] = MNEMONIC_GROUP_MOTOR_2;
                notifyObservers(message);
                break;
            case (MNEMONIC_GROUP_MOTOR_3):
                TinygDriver.getInstance().m.getMotorByNumber(MNEMONIC_GROUP_MOTOR_3)
                        .applyJsonSystemSetting(js.getJSONObject(MNEMONIC_GROUP_MOTOR_3), MNEMONIC_GROUP_MOTOR_3);
                setChanged();
                message[0] = "CMD_GET_MOTOR_SETTINGS";
                message[1] = MNEMONIC_GROUP_MOTOR_3;
                notifyObservers(message);
                break;

            case (MNEMONIC_GROUP_MOTOR_4):
                TinygDriver.getInstance().m.getMotorByNumber(MNEMONIC_GROUP_MOTOR_4)
                        .applyJsonSystemSetting(js.getJSONObject(MNEMONIC_GROUP_MOTOR_4), MNEMONIC_GROUP_MOTOR_4);
                setChanged();
                message[0] = "CMD_GET_MOTOR_SETTINGS";
                message[1] = MNEMONIC_GROUP_MOTOR_4;
                notifyObservers(message);
                break;

            case (MNEMONIC_GROUP_AXIS_X):
                TinygDriver.getInstance().m.getAxisByName(MNEMONIC_GROUP_AXIS_X)
                        .applyJsonSystemSetting(js.getJSONObject(MNEMONIC_GROUP_AXIS_X), MNEMONIC_GROUP_AXIS_X);
                setChanged();
                message[0] = "CMD_GET_AXIS_SETTINGS";
                message[1] = MNEMONIC_GROUP_AXIS_X;
                notifyObservers(message);
                break;

            case (MNEMONIC_GROUP_AXIS_Y):
                TinygDriver.getInstance().m.getAxisByName(MNEMONIC_GROUP_AXIS_Y)
                        .applyJsonSystemSetting(js.getJSONObject(MNEMONIC_GROUP_AXIS_Y), MNEMONIC_GROUP_AXIS_Y);
                setChanged();
                message[0] = "CMD_GET_AXIS_SETTINGS";
                message[1] = MNEMONIC_GROUP_AXIS_Y;
                notifyObservers(message);
                break;

            case (MNEMONIC_GROUP_AXIS_Z):
                TinygDriver.getInstance().m.getAxisByName(MNEMONIC_GROUP_AXIS_Z)
                        .applyJsonSystemSetting(js.getJSONObject(MNEMONIC_GROUP_AXIS_Z), MNEMONIC_GROUP_AXIS_Z);
                setChanged();
                message[0] = "CMD_GET_AXIS_SETTINGS";
                message[1] = MNEMONIC_GROUP_AXIS_Z;
                notifyObservers(message);
                break;

            case (MNEMONIC_GROUP_AXIS_A):
                TinygDriver.getInstance().m.getAxisByName(MNEMONIC_GROUP_AXIS_A)
                        .applyJsonSystemSetting(js.getJSONObject(MNEMONIC_GROUP_AXIS_A), MNEMONIC_GROUP_AXIS_A);
                setChanged();
                message[0] = "CMD_GET_AXIS_SETTINGS";
                message[1] = MNEMONIC_GROUP_AXIS_A;
                notifyObservers(message);
                break;
            case (MNEMONIC_GROUP_AXIS_B):
                TinygDriver.getInstance().m.getAxisByName(MNEMONIC_GROUP_AXIS_B)
                        .applyJsonSystemSetting(js.getJSONObject(MNEMONIC_GROUP_AXIS_B), MNEMONIC_GROUP_AXIS_B);
                setChanged();
                message[0] = "CMD_GET_AXIS_SETTINGS";
                message[1] = MNEMONIC_GROUP_AXIS_B;
                notifyObservers(message);
                break;

            case (MNEMONIC_GROUP_AXIS_C):
                TinygDriver.getInstance().m.getAxisByName(MNEMONIC_GROUP_AXIS_C)
                        .applyJsonSystemSetting(js.getJSONObject(MNEMONIC_GROUP_AXIS_C), MNEMONIC_GROUP_AXIS_C);
                setChanged();
                message[0] = "CMD_GET_AXIS_SETTINGS";
                message[1] = MNEMONIC_GROUP_AXIS_C;
                notifyObservers(message);
                break;

            case ("hom"):
                System.out.println("HOME");
                break;
            case (MNEMONIC_GROUP_SYSTEM):
                System.out.println(MNEMONIC_GROUP_SYSTEM);
                TinygDriver.getInstance().m.applyJsonSystemSetting(js.getJSONObject(MNEMONIC_GROUP_SYSTEM), MNEMONIC_GROUP_SYSTEM);

                setChanged();
                message[0] = "CMD_GET_MACHINE_SETTINGS";
                message[1] = null;
                notifyObservers(message);
                break;
            case (MNEMONIC_GROUP_STATUS_REPORT):
                System.out.println("Status Report");
                applyStatusReport(js.getJSONObject(MNEMONIC_GROUP_STATUS_REPORT)); //Send in the jsobject 
                break;
            case (MNEMONIC_GROUP_EMERGENCY_SHUTDOWN):
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        DialogFX dialog = new DialogFX(DialogFX.Type.ERROR);
                        dialog.setTitleText("Error Occured");
                        dialog.setMessage("You have triggered a limit switch.  TinyG is now in DISABLED mode. \n"
                                + "Manually back your machine off of its limit switches.\n  Once done, if you would like to re-enable TinyG click yes.");
                        int choice = dialog.showDialog();
                        if (choice == 0) {
                            logger.info("Clicked Yes");
                            try {
                                TinygDriver.getInstance().priorityWrite((byte) 0x18);
                            } catch (Exception ex) {
                                java.util.logging.Logger.getLogger(ResponseParser.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else if (choice == 1) {
                            logger.info("Clicked No");
                        }


                    }
                });

            default:

                //This is for single settings xfr, 1tr etc...
                //This is pretty ugly but it gets the key and the value. For single values.
                responseCommand rc = TinygDriver.getInstance().mneManager.lookupSingleGroup(pg);

//                  String _parent = String.valueOf(parentGroup.charAt(0));
                String newJs;
//                  String _key = parentGroup; //I changed this to deal with the fb mnemonic.. not sure if this works all over.
                rc.setSettingValue(String.valueOf(js.get(js.keys().next().toString())));
                logger.info("Single Key Value: " + rc.getSettingParent() + rc.getSettingKey() + rc.getSettingValue());
                this.applySetting(rc.buildJsonObject()); //We pass the new json object we created from the string above
            }


    }

    public void applySettings(String newJsObjString) {
        //When a single key value pair is sent without the group object
        //We use this method to create a new json object
        try {
            JSONObject newJs = new JSONObject(newJsObjString);
            applySetting(newJs);
        } catch (Exception ex) {
            logger.error("Invalid Attempt to create newJs object");
        }
    }

    private void parseFooter(JSONObject js) {
        try {


            //Checking to see if we have a footer response
            //Status reports will not have a footer so this is for everything else
            footerValues = js.getJSONArray("f");
            responseFooter.setProtocolVersion(footerValues.getInt(FOOTER_ELEMENT_PROTOCOL_VERSION));
            responseFooter.setStatusCode(footerValues.getInt(FOOTER_ELEMENT_STATUS_CODE));
            responseFooter.setRxRecvd(footerValues.getInt(FOOTER_ELEMENT_RX_RECVD));
            responseFooter.setCheckSum(footerValues.getInt(FOOTER_ELEMENT_STATUS_CODE));
            //Out footer object is not populated

            int beforeBytesReturned = TinygDriver.getInstance().serialWriter.getBufferValue();
            //Make sure we do not add bytes to a already full buffer
            if (beforeBytesReturned != TinygDriver.MAX_BUFFER) {
                TinygDriver.getInstance().serialWriter.addBytesReturnedToBuffer(responseFooter.getRxRecvd());
                int afterBytesReturned = TinygDriver.getInstance().serialWriter.getBufferValue();
                logger.info("Returned " + responseFooter.getRxRecvd() + " to buffer... Buffer was " + beforeBytesReturned + " is now " + afterBytesReturned);
                TinygDriver.getInstance().serialWriter.notifyAck();  //We let our serialWriter thread know we have added some space to the buffer.
            }
        } catch (Exception ex) {
            logger.error("Error parsing json footer");
        }
    }

    public synchronized void parseJSON(String line) throws JSONException {
        String axis;
        String[] statusResponse;
        int motor;
        logger.info("Got Line: " + line + " from TinyG.");
        final JSONObject js = new JSONObject(line);
        if (js.has("f")) {
            parseFooter(js);  //This is very important.  We break out our response footer.. error codes.. bytes availble in hardware buffer etc.
        }

        if (js.has("r")) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        applySetting(js.getJSONObject("r"));
                    } catch (JSONException ex) {
                        java.util.logging.Logger.getLogger(ResponseParser.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

        } else if (js.has("er")) {
            applySetting(js);
        }

    }
}
