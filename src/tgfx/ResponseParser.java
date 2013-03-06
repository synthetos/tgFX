/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import org.json.*;
import java.util.Iterator;
import java.util.Observable;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import javafx.application.Platform;
import jfxtras.labs.dialogs.DialogFX;

import org.apache.log4j.Logger;
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
    private boolean TEXT_MODE = false;
    private String[] message = new String[2];
    private BlockingQueue responseQueue;
    boolean RUN = true;
    String buf = "";
    public ResponseFooter responseFooter = new ResponseFooter();  //our holder for ResponseFooter Data
    private static Logger logger = Logger.getLogger(ResponseParser.class);
    //These values are for mapping what n'Th element inthe json footer array maps to which values.
    private static final int FOOTER_ELEMENT_PROTOCOL_VERSION = 0;
    private static final int FOOTER_ELEMENT_STATUS_CODE = 1;
    private static final int FOOTER_ELEMENT_RX_RECVD = 2;
    private static final int FOOTER_ELEMENT_CHECKSUM = 3;
    private JSONArray footerValues;
    private String line;

    public boolean isTEXT_MODE() {
        return TEXT_MODE;
    }

    public void setTEXT_MODE(boolean TEXT_MODE) {
        this.TEXT_MODE = TEXT_MODE;
    }

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
        logger.setLevel(org.apache.log4j.Level.ERROR);
//        logger.setLevel(org.apache.log4j.Level.INFO);

    }

    @Override
    public void run() {
        logger.info("Response Parser Running");


        while (RUN) {
            try {
                line = (String) responseQueue.take();
                if (line.equals("")) {  //Onreset TinyG kicks out a ""
                    continue;
                }
                if (line.startsWith("{")) {
                    if (isTEXT_MODE()) {
                        setTEXT_MODE(false);
                        //This checks to see if we WERE in textmode.  If we were we notify the user that we are not longer and update the system state.
                        setChanged();
                        message[0] = "TEXTMODE_REPORT";
                        message[1] = "[+]JSON Response Detected... Leaving Text mode..  Querying System State....\n";
                        notifyObservers(message);
                        try {
//                            TinygDriver.getInstance().cmdManager.queryAllMachineSettings();
//                            TinygDriver.getInstance().cmdManager.queryAllHardwareAxisSettings();
//                            TinygDriver.getInstance().cmdManager.queryAllMotorSettings();
                        } catch (Exception ex) {
                            logger.error("Error leaving Text mode and querying Motor, Machine and Axis Settings.");
                        }

                    }
                    parseJSON(line);  //Take a line from the response queue when its ready and parse it.
//                    System.out.println("GOT LINE: " + line);
                } else {
                    //Text Mode Response
                    if (!isTEXT_MODE()) {
                        //We are just entering text mode and need to alert the user. 
                        //This will fire the every time user is entering text mode.
                        setTEXT_MODE(true);
                        setChanged();
                        message[0] = "TEXTMODE_REPORT";
                        message[1] = "[+]User has entered text mode.  To exit type \"{\" and hit enter.\n";
                        notifyObservers(message);
                    }
                    setChanged();
                    message[0] = "TEXTMODE_REPORT";
                    message[1] = line + "\n";
                    notifyObservers(message);
                }
            } catch (InterruptedException | JSONException ex) {
                logger.error("[!]Error in responseParser run(): " + ex.getMessage());

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

//    public void applyStatusReport(JSONObject js) throws Exception {
//
//        logger.info("Applying JSON Object to System Group");
//        Iterator ii = js.keySet().iterator();
//        while (ii.hasNext()) {
//            String _key = ii.next().toString();
//            String _val = js.get(_key).toString();
//            String _parent;
//            _parent = (TinygDriver.getInstance().mneManager.lookupSingleGroup(_key)).getSettingParent();
//            final responseCommand rc = new responseCommand(_parent, _key, _val);
//            TinygDriver.getInstance().m.applyJsonStatusReport(rc);
//            setChanged();
//            String[] message = new String[2];
//            message[0] = "STATUS_REPORT";
//            message[1] = null;
//            notifyObservers(message);
//        }
//        //Set the status report values 
////            TinygDriver.getInstance().m.getAxisByName("X").setWork_position(js.getDouble("posx"));
////            TinygDriver.getInstance().m.getAxisByName("Y").setWork_position(js.getDouble("posy"));
////            TinygDriver.getInstance().m.getAxisByName("Z").setWork_position(js.getDouble("posz"));
////            TinygDriver.getInstance().m.getAxisByName("A").setWork_position(js.getDouble("posa"));
////            TinygDriver.getInstance().m.setMachineState(js.getInt("stat"));
////            TinygDriver.getInstance().m.setMotionMode(js.getInt("momo"));
////            TinygDriver.getInstance().m.setVelocity(js.getDouble("vel"));
//
//    }
    public void applySettingMasterGroup(JSONObject js, String pg) throws Exception {
        String parentGroup;
        if (pg.equals(MNEMONIC_GROUP_STATUS_REPORT)) {
            //This is a status report master object that came in through a response object.
            //meaning that the response was asked for like this {"sr":""} and returned like this.
            //{"r":{"sr":{"line":0,"posx":0.000,"posy":0.000,"posz":0.000,"posa":0.000,"vel":0.000,"unit":1,"momo":0,"stat":3},"f":[1,0,10,885]}}
            //Right now its parsed down to JUST the json object for the SR like so.
            //{"sr":{"line":0,"posx":0.000,"posy":0.000,"posz":0.000,"posa":0.000,"vel":0.000,"unit":1,"momo":0,"stat":3},"f":[1,0,10,885]}
            //so we can now just pass it to the applySettingStatusReport method.
            applySettingStatusReport(js);
        } else {
            if (js.keySet().size() > 1) {
                Iterator ii = js.keySet().iterator();
                //This is a special multi single value response object
                while (ii.hasNext()) {
                    String key = ii.next().toString();
                    if (key.equals("f")) {
                        parseFooter(js.getJSONArray("f"));  //This is very important.  We break out our response footer.. error codes.. bytes availble in hardware buffer etc.               
                    } else {
                        responseCommand rc = TinygDriver.getInstance().mneManager.lookupSingleGroupMaster(key, pg);
                        if (rc == null) { //This happens when a new mnemonic has been added to the tinyG firmware but not added to tgFX's MnemonicManger
                            //This is the error case
                            logger.error("Mnemonic Lookup Failed in applySettingsMasterGroup. \n\tMake sure there are not new elements added to TinyG and not to the MnemonicManager Class.\n\tMNEMONIC FAILED: " + key);
                        } else {
                            //This is the normal case
                            rc.setSettingValue(js.get(key).toString());
                            parentGroup = rc.getSettingParent();
                            _applySettings(rc.buildJsonObject(), rc.getSettingParent()); //we will supply the parent object name for each key pai
                        }
                    }
                }
            }
        }
    }

    public void applySettingStatusReport(JSONObject js) {
        /**
         * This breaks the mold a bit.. but its more efficient. This gets called
         * off the top of ParseJson if it has an "SR" in it. Sr's are called so
         * often that instead of walking the normal parsing tree.. this skips to
         * the end
         */
        String parentGroup;
        try {
            Iterator ii = js.keySet().iterator();
            //This is a special multi single value response object
            while (ii.hasNext()) {
                String key = ii.next().toString();

                responseCommand rc = new responseCommand(MNEMONIC_GROUP_SYSTEM, key.toString(), js.get(key).toString());
                TinygDriver.getInstance().m.applyJsonStatusReport(rc);
//                _applySettings(rc.buildJsonObject(), rc.getSettingParent()); //we will supply the parent object name for each key pair
            }
            setChanged();
            message[0] = "STATUS_REPORT";
            message[1] = null;
            notifyObservers(message);

        } catch (Exception ex) {
            logger.error("[!] Error in applySettingStatusReport(JsonOBject js) : " + ex.getMessage());
            logger.error("[!]js.tostring " + js.toString());
        }
    }
    
    public void set_Changed(){
        this.setChanged();
    }

    public void applySetting(JSONObject js) {
        String parentGroup;
        try {

            if (js.keySet().size() > 1) { //If there are more than one object in the json response
                Iterator ii = js.keySet().iterator();
                //This is a special multi single value response object
                while (ii.hasNext()) {
                    String key = ii.next().toString();
                    switch (key) {
                        case "f":
                            parseFooter(js.getJSONArray("f"));
                            //This is very important.  
                            //We break out our response footer.. error codes.. bytes availble in hardware buffer etc.
                            break;

                        case "msg":
                            message[0] = "TINYG_USER_MESSAGE";
                            message[1] = (String) js.get(key) + "\n";
                            logger.info("[+]TinyG Message Sent:  " + js.get(key) + "\n");
                            setChanged();
                            notifyObservers(message);
                            break;
                        case "rx":
                            TinygDriver.getInstance().serialWriter.setBuffer(js.getInt(key));
                            break;
                        default:
                            if (TinygDriver.getInstance().mneManager.isMasterGroupObject(key)) {
                                //                            logger.info("Group Status Report Detected: " + key);
                                applySettingMasterGroup(js.getJSONObject(key), key);
                                continue;
                            }
                            responseCommand rc = TinygDriver.getInstance().mneManager.lookupSingleGroup(key);
                            rc.setSettingValue(js.get(key).toString());
                            parentGroup = rc.getSettingParent();
                            _applySettings(rc.buildJsonObject(), rc.getSettingParent()); //we will supply the parent object name for each key pair
                            break;
                    }
                }
            } else {
                /* This else follows:
                 * Contains a single object in the JSON response
                 */
                if (js.keySet().contains("f")) {
                    /**
                     * This is a single response footer object: Like So:
                     * {"f":[1,0,5,3330]}
                     */
                    parseFooter(js.getJSONArray("f"));
                } else {
                    /**
                     * Contains a single object in the json response I am not
                     * sure this else is needed any longer.
                     */
                    _applySettings(js, js.keys().next().toString());
                }
            }
        } catch (Exception ex) {
            logger.error("[!] Error in applySetting(JsonOBject js) : " + ex.getMessage());
            logger.error("[!]JSON String Was: " + js.toString());
//            logger.error("Got Line: " + js);


        }
    }

//    private void _applySettings(JSONObject js, String pg) throws Exception {
//        String parentGroup = pg;
//        _applySettings(js, parentGroup);
//    }
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
                TinygDriver.getInstance().m.getAxisByName(MNEMONIC_GROUP_AXIS_X).applyJsonSystemSetting(js.getJSONObject(MNEMONIC_GROUP_AXIS_X), MNEMONIC_GROUP_AXIS_X);
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
                logger.info("HOME");
                break;
            case (MNEMONIC_GROUP_SYSTEM):
//                logger.info(MNEMONIC_GROUP_SYSTEM);
                TinygDriver.getInstance().m.applyJsonSystemSetting(js.getJSONObject(MNEMONIC_GROUP_SYSTEM), MNEMONIC_GROUP_SYSTEM);
//*
                /**
                 * UNCOMMENT THIS BELOW WHEN WE HAVE MACHINE SETTINGS THAT NEED
                 * TO UPDATE THE GU
                 */
//                setChanged();
                message[0] = "MACHINE_UPDATE";
                message[1] = null;
//                notifyObservers(message);
                break;
//            case (MNEMONIC_GROUP_STATUS_REPORT):
//                logger.info("Status Report");
//                TinygDriver.getInstance().m.applyJsonStatusReport(js.getJSONObject(MNEMONIC_GROUP_STATUS_REPORT), MNEMONIC_GROUP_STATUS_REPORT); //Send in the jsobject 
//                setChanged();
//                message[0] = "STATUS_REPORT";
//                message[1] = null;
//                notifyObservers(message);
//
//                break;
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
                                java.util.logging.Logger.getLogger(ResponseParser.class
                                        .getName()).log(Level.SEVERE, null, ex);
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

    private void parseFooter(JSONArray footerValues) {
        try {


            //Checking to see if we have a footer response
            //Status reports will not have a footer so this is for everything else

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
                logger.debug("Returned " + responseFooter.getRxRecvd() + " to buffer... Buffer was " + beforeBytesReturned + " is now " + afterBytesReturned);
                TinygDriver.getInstance().serialWriter.notifyAck();  //We let our serialWriter thread know we have added some space to the buffer.
                //Lets tell the UI the new size of the buffer
                message[0] = "BUFFER_UPDATE";
                message[1] = String.valueOf(afterBytesReturned);
                setChanged();
                notifyObservers(message);
            }
        } catch (Exception ex) {
            logger.error("Error parsing json footer");
        }
    }

    public synchronized void parseJSON(String line) throws JSONException {

        logger.info("Got Line: " + line + " from TinyG.");

        final JSONObject js = new JSONObject(line);

        if (js.has("r") || (js.has("sr"))) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        //These are the 2 types of responses we will get back.
                        switch (js.keys().next().toString()) {
                            case ("r"):
                                applySetting(js.getJSONObject("r"));
                                break;
                            case ("sr"):
                                applySettingStatusReport(js.getJSONObject("sr"));
                                break;
                        }

                    } catch (JSONException ex) {
                        java.util.logging.Logger.getLogger(ResponseParser.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

        } else if (js.has("er")) {
            applySetting(js);
        }

    }
}
