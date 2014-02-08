/*
 * Copyright Synthetos LLC
 * Rileyporter@gmail.com
 * www.synthetos.com
 */
package tgfx;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import jfxtras.labs.dialogs.MonologFXButton;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import java.util.MissingResourceException;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.util.StringConverter;
import jfxtras.labs.scene.control.gauge.Lcd;
import jfxtras.labs.scene.control.gauge.LcdBuilder;
import jfxtras.labs.scene.control.gauge.StyleModel;
import javafx.stage.Stage;

import jfxtras.labs.dialogs.MonologFX;
import jfxtras.labs.dialogs.MonologFXBuilder;
import static jfxtras.labs.dialogs.MonologFXButton.Type.NO;
import static jfxtras.labs.dialogs.MonologFXButton.Type.YES;
import jfxtras.labs.dialogs.MonologFXButtonBuilder;
import org.json.JSONException;

import tgfx.tinyg.TinygDriver;
import tgfx.system.StatusCode;
import tgfx.tinyg.CommandManager;
import tgfx.render.CNCMachine;
import tgfx.render.Draw2d;
import tgfx.ui.gcode.GcodeHistory;
import tgfx.ui.gcode.GcodeTabController;
import tgfx.ui.machinesettings.MachineSettingsController;
import tgfx.ui.tgfxsettings.TgfxSettingsController;
import tgfx.ui.tinygconfig.TinyGConfigController;
import tgfx.updater.firmware.FirmwareUpdaterController;
import tgfx.utility.QueueUsingTimer;
import tgfx.utility.QueuedTimerable;

/**
 * The
 * <code>Main</code> class is logically the "main" class of the application, but
 * due to how javaFX's framework is setup, it is not the first class executed on
 * application startup, rather that is TgFX, which is kicked off under the
 * control of the XML instructions.
 *
 * @see TgFX
 * @author riley
 */
public class Main extends Stage implements Initializable, Observer, QueuedTimerable<String> {

    private int oldRspLine = 0;
    private String CONNECTION_TIMEOUT = "{\"tgfx\": \"TinyG Connection Timeout\"}";
    int CONNECTION_TIMEOUT_VALUE = 5000;  //This is the amount of time in milliseconds that will go until we say the connection has timed out.
    public static String OS = System.getProperty("os.name").toLowerCase();
    private int delayValue = 150; //Time between config set'ers.
    private boolean buildChecked = false;  //this is checked apon initial connect.  Once this is set to true
    //if a buildVersion changed message comes in it will not refire onConnect2() again and again
    static final Logger logger = Logger.getLogger(Main.class);
    private TinygDriver tg = TinygDriver.getInstance();
    private String PROMPT = "tinyg>";
    private GcodeHistory gcodeCommandHistory = new GcodeHistory();
    
    //public final static String LOGLEVEL = "OFF";
    private QueueUsingTimer connectionTimer = new QueueUsingTimer(CONNECTION_TIMEOUT_VALUE, this, CONNECTION_TIMEOUT);
    public final static String LOGLEVEL = "OFF";
    @FXML
    private Circle cursor;
    @FXML
    private Button Connect;
    @FXML
    TextField input, listenerPort;
    @FXML
    private Label srMomo, srState, srBuild, srBuffer, srGcodeLine,
            srVer, srUnits, srCoord;
    @FXML
    StackPane cursorPoint;
    @FXML
    TextArea gcodesList;
    @FXML
    private static TextArea console;
    @FXML
    WebView html, makerCam;
    @FXML
    Text heightSize, widthSize;
    @FXML
    ChoiceBox serialPorts;
    //##########Config FXML##############//
    @FXML
    Group motor1Node;
    @FXML
    HBox bottom, xposhbox, gcodeWindowButtonBar, gcodePreviewHbox;
    @FXML
    HBox canvasHolder;
    @FXML
    VBox topvbox, positionsVbox, tester, consoleVBox;
    @FXML
    private TabPane topTabPane;

    

    public Main() {
        //Setup Logging for TinyG Driver
        if (LOGLEVEL.equals("INFO")) {
//            logger.setLevel(org.apache.log4j.Level.INFO);
        } else if (LOGLEVEL.equals("ERROR")) {
            logger.setLevel(org.apache.log4j.Level.ERROR);
        } else {
            logger.setLevel(org.apache.log4j.Level.OFF);
        }
    }

    @Override
    public void addToQueue(String t) {
        //This is used to add the connection timeout message to the "json" queue.
        TinygDriver.getInstance().appendJsonQueue(t);
    }

    @FXML
    private void FXreScanSerial(ActionEvent event) {
        this.reScanSerial();
    }

    private void reScanSerial() {
        serialPorts.getItems().clear();
        String portArray[];
        portArray = tg.listSerialPorts();
        serialPorts.getItems().addAll(Arrays.asList(portArray));
    }

    /**
     * These are the actions that need to be ran upon successful serial port
     * connection. If you have something that you want to "auto run" on connect.
     * This is the place to do so. This method is called in handleConnect.
     */
    private void onConnectActions() {
        try {

            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    try {
                        GcodeTabController.setGcodeTextTemp("Attempting to Connect to TinyG.");
                        TinygDriver.getInstance().serialWriter.notifyAck(); //If the serialWriter is in a wait state.. wake it up
                        TinygDriver.getInstance().write(CommandManager.CMD_APPLY_NOOP); //Just waking things up.
                        TinygDriver.getInstance().write(CommandManager.CMD_APPLY_NOOP);
                        TinygDriver.getInstance().write(CommandManager.CMD_APPLY_NOOP);

                        TinygDriver.getInstance().write(CommandManager.CMD_QUERY_HARDWARE_PLATFORM);
                        TinygDriver.getInstance().write(CommandManager.CMD_QUERY_HARDWARE_VERSION);
                        TinygDriver.getInstance().write(CommandManager.CMD_QUERY_HARDWARE_BUILD_NUMBER);
                        //Thread.sleep(delayValue);  //Should not need this for query operations
                        postConsoleMessage("Getting TinyG Firmware Build Version....");
                        connectionTimer.start();
                    } catch (Exception ex) {
                        logger.error("Error in OnConnectActions() " + ex.getMessage());
                    }
                }
            });



        } catch (Exception ex) {
            postConsoleMessage("[!]Error in onConnectActions: " + ex.getMessage());
            Main.print(ex.getMessage());
        }
    }

    private void onConnectActionsTwo() {
        try {
            buildChecked = true;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    try {

                        connectionTimer.disarm();

                        /*####################################
                         *Priority Write's Must Observe the delays or you will smash TinyG as it goes into a "disable interrupt mode" to write values to EEPROM 
                         #################################### */
                        tg.priorityWrite(CommandManager.CMD_APPLY_JSON_VOBERSITY);
                        Thread.sleep(delayValue);
                        tg.priorityWrite(CommandManager.CMD_APPLY_STATUS_UPDATE_INTERVAL);
                        Thread.sleep(delayValue);
                        tg.priorityWrite(CommandManager.CMD_APPLY_TEXT_VOBERSITY);
                        Thread.sleep(delayValue);
                        tg.priorityWrite(CommandManager.CMD_APPLY_FLOWCONTROL);
                        Thread.sleep(delayValue);
                        tg.priorityWrite(CommandManager.CMD_APPLY_STATUS_REPORT_FORMAT);
                        Thread.sleep(600); //Setting the status report takes some time!  Just leave this alone.  This is a hardware limit..
                        //writing to the eeprom (so many values) is troublesome :)  Like geese.. (this one is for alden)


                        /*####################################
                         *Query Code gets the regular write method
                         #################################### */
                        tg.cmdManager.queryAllMachineSettings();                    //SIXtH
                        Thread.sleep(delayValue);
                        tg.cmdManager.queryStatusReport();
                        Thread.sleep(delayValue);
                        tg.cmdManager.queryAllMotorSettings();
                        Thread.sleep(delayValue);
                        tg.cmdManager.queryAllHardwareAxisSettings();
                        Thread.sleep(delayValue);
                        tg.write(CommandManager.CMD_APPLY_TEXT_VOBERSITY);

                        tgfx.ui.gcode.GcodeTabController.setCNCMachineVisible(true); //Once we connected we should show the drawing enevlope. 
                        Main.postConsoleMessage("Showing CNC Machine Preview...");
                        GcodeTabController.setGcodeTextTemp("TinyG Connected.");

                    } catch (Exception ex) {
                        logger.error("Error in OnConnectActions() " + ex.getMessage());
                    }
                }
            });

        } catch (Exception ex) {
            postConsoleMessage("[!]Error in onConnectActions: " + ex.getMessage());
            Main.print(ex.getMessage());
        }
    }

    @FXML
    private void handleConnect(ActionEvent event) throws Exception {
        //Get a list of serial ports on the system.


        if (serialPorts.getSelectionModel().getSelectedItem() == (null)) {
            postConsoleMessage("[!]Error Connecting to Serial Port please select a valid port.\n");
            return;
        }
        if (Connect.getText().equals("Connect") && serialPorts.getSelectionModel().getSelectedItem() != (null)) {
            String serialPortSelected = serialPorts.getSelectionModel().getSelectedItem().toString();

            Main.print("[*]Attempting to Connect to TinyG.");

            if (!tg.initialize(serialPortSelected, 115200)) {  //This will be true if we connected when we tried to!
                //Our connect attempt failed. 
                postConsoleMessage("[!]There was an error connecting to " + serialPortSelected + " please verify that the port is not in use.");
                postConsoleMessage("If you are OSX you need to make sure you have /var/lock created. Open a console (terminal) and type in these 2 commands.\n"
                        + " sudo mkdir /var/lock\n "
                        + "sudo chmod a+rw /var/lock\n "
                        + "If that does not work make sure your port is not open with another application and restart tgFX.");
            }

            if (tg.isConnected().get()) {

                postConsoleMessage("[*]Connected to " + serialPortSelected + " Serial Port Successfully.\n");
                Connect.setText("Disconnect");

                /**
                 * *****************************
                 * OnConnect Actions Called Here *****************************
                 */
                onConnectActions();
            }
        } else {
            tg.disconnect();
            if (!tg.isConnected().get()) {
                postConsoleMessage("[+]Disconnected from " + tg.getPortName() + " Serial Port Successfully.\n");
                Connect.setText("Connect");
                onDisconnectActions();
            }

        }
    }

    public void onDisconnectActions() throws IOException, JSONException {
        TinygDriver.getInstance().machine.setFirmwareBuild(0.0);
        TinygDriver.getInstance().machine.firmwareBuild.set(0);
        TinygDriver.getInstance().machine.firmwareVersion.set("");
        TinygDriver.getInstance().machine.m_state.set("");
        TinygDriver.getInstance().machine.setLineNumber(0);
        TinygDriver.getInstance().machine.setMotionMode(0);
        Draw2d.setFirstDraw(true);
        tgfx.ui.gcode.GcodeTabController.setCNCMachineVisible(false);  //Once we disconnect we hide our gcode preview.

        TinygDriver.getInstance().serialWriter.resetBuffer();
        TinygDriver.getInstance().serialWriter.clearQueueBuffer();
        TinygDriver.getInstance().serialWriter.notifyAck();
        buildChecked = false; //Reset this so we can enable checking the build again on disconnect
        GcodeTabController.setGcodeTextTemp("TinyG Disconnected.");



        //TODO Need to have a way to pull this out of the gcodePane via a message
        //gcodePane.getChildren().remove(cncMachine);
    }

    @FXML
    private void handleKeyInput(final InputEvent event) {
    }

    public static void postConsoleMessage(String message) {
        //This allows us to send input to the console text area on the Gcode Tab.
        final String m = message;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                console.appendText(m + "\n");
            }
        });
    }

//    @FXML
//    private void gcodeProgramClicks(MouseEvent me) {
//        TextField tField = (TextField) gcodesList.getSelectionModel().getSelectedItem();
//        if (me.getButton() == me.getButton().SECONDARY) {
////            tg.write("{\"gc\":\"" + lbl.getText() + "\"}\n");
//            Main.print("RIGHT CLICKED");
//
//
//        } else if (me.getButton() == me.getButton().PRIMARY && me.getClickCount() == 2) {
//            Main.print("double clicked");
//            tField.setEditable(true);
//
//            //if (lbl.getParent().getStyleClass().contains("breakpoint")) {
////                lbl.getParent().getStyleClass().remove("breakpoint");
////                tgfx.Main.postConsoleMessage("BREAKPOINT REMOVED: " + lbl.getText() + "\n");
////                Main.print("BREAKPOINT REMOVED");
////            } else {
////
////                Main.print("DOUBLE CLICKED");
////                lbl.getStyleClass().removeAll(null);
////                lbl.getParent().getStyleClass().add("breakpoint");
////                Main.print("BREAKPOINT SET");
////                tgfx.Main.postConsoleMessage("BREAKPOINT SET: " + lbl.getText() + "\n");
////            };
//        }
//    }
    @FXML
    private void handleGuiRefresh() throws Exception {
        //Refreshed all gui settings from TinyG Responses.
        if (tg.isConnected().get()) {

            postConsoleMessage("[+]System GUI Refresh Requested....");
            tg.cmdManager.queryAllHardwareAxisSettings();
            tg.cmdManager.queryAllMachineSettings();
            tg.cmdManager.queryAllMotorSettings();
        } else {
            postConsoleMessage("[!]TinyG Not Connected.. Ignoring System GUI Refresh Request....");
        }
    }

    @FXML
    private void handleKeyPress(final InputEvent event) throws Exception {
        //private void handleEnter(ActionEvent event) throws Exception {
        final KeyEvent keyEvent = (KeyEvent) event;
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            String command = (input.getText() + "\n");
            logger.info("Entered Command: " + command);
            if (!tg.isConnected().get()) {
                logger.error("TinyG is not connected....\n");
                postConsoleMessage("[!]TinyG is not connected....\n");
                input.setPromptText(PROMPT);
                return;
            }
            //TinyG is connected... Proceed with processing command.
            //This will send the command to get a OK prompt if the buffer is empty.
            //"{\""+ command.split("=")[0].replace("$", "") + "\":" + command.split("=")[1].trim() + "}\n"
            if ("".equals(command)) {
                tg.write(CommandManager.CMD_QUERY_OK_PROMPT);
            }

            tg.write(command);
            postConsoleMessage(command.replace("\n", ""));
            gcodeCommandHistory.addCommandToHistory(command);  //Add this command to the history
            input.clear();
            input.setPromptText(PROMPT);
        } else if (keyEvent.getCode().equals(KeyCode.UP)) {
            input.setText(gcodeCommandHistory.getNextHistoryCommand());
//            input.positionCaret(input.lengthProperty().get());
        } else if (keyEvent.getCode().equals(KeyCode.DOWN)) {
            input.setText(gcodeCommandHistory.getPreviousHistoryCommand());
//            input.positionCaret(input.lengthProperty().get());
        }
    }
//    private Task initRemoteServer(String port) {
//        final String Port = port;
//        return new Task() {
//            @Override
//            protected Object call() throws Exception {
//                SocketMonitor sm = new SocketMonitor(Port);
//
//                Main.print("[+]Trying to start remote monitor.");
//                return true;
//            }
//        };
//    }
//    private void updateGuiMachineSettings(String line) {
//        final String l = line;
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                //We are now back in the EventThread and can update the GUI
//                try {
//                    Machine m = TinygDriver.getInstance().m;
////                    srBuild.setText(String.valueOf(m.getFirmwareBuild()));
////                    srVer.setText(String.valueOf(m.getFirmwareVersion()));
////                    gcodePlane.getSelectionModel().select(TinygDriver.getInstance().m.getGcode_select_plane().ordinal());
////                    gcodeUnitMode.getSelectionModel().select(TinygDriver.getInstance().m.getGcodeUnitMode());
////                    gcodeCoordSystem.getSelectionModel().select(TinygDriver.getInstance().m.getCoordinateSystem().ordinal());
////                    gcodePathControl.getSelectionModel().select(TinygDriver.getInstance().m.getGcode_distance_mode().ordinal());
////                    gcodeDistanceMode.getSelectionModel().select(TinygDriver.getInstance().m.getGcode_distance_mode().ordinal());
//
////                    if (m.getCoordinateSystem() != null) {
////                        srCoord.setText(TinygDriver.getInstance().m.getCoordinateSystem().toString());
////                    }
//                } catch (Exception ex) {
//                    Main.print("[!] Exception in updateGuiMachineSettings");
//                    Main.print(ex.getMessage());
//                }
//            }
//        });
//
//    }
    

    @Override
    public synchronized void update(Observable o, Object arg) {
        //We process status code messages here first.
        if (arg.getClass().getCanonicalName().equals("tgfx.system.StatusCode")) {
            //We got an error condition.. lets route it to where it goes!
            StatusCode statuscode = (StatusCode) arg;
            postConsoleMessage("[->] TinyG Response: " + statuscode.getStatusType() + ":" + statuscode.getMessage() + "\n");
        } else {
            try {
                final String[] UPDATE_MESSAGE = (String[]) arg;
                final String ROUTING_KEY = UPDATE_MESSAGE[0];
                final String KEY_ARGUMENT = UPDATE_MESSAGE[1];

                /**
                 * This is our update routing switch From here we update
                 * different parts of the GUI that is not bound to properties.
                 */
                switch (ROUTING_KEY) {
                    case ("STATUS_REPORT"):
                        doStatusReport();
                        break;
                    case ("CMD_GET_AXIS_SETTINGS"):
                        TinyGConfigController.updateGuiAxisSettings(KEY_ARGUMENT);
                        break;
                    case ("CMD_GET_MACHINE_SETTINGS"):
                        //updateGuiMachineSettings(ROUTING_KEY);
                        break;
                    case ("CMD_GET_MOTOR_SETTINGS"):
                        TinyGConfigController.updateGuiMotorSettings(KEY_ARGUMENT);
                        break;
                    case ("NETWORK_MESSAGE"):
                        //updateExternal();
                        break;
                    case ("MACHINE_UPDATE"):
                        MachineSettingsController.updateGuiMachineSettings();

                        break;
                    case ("TEXTMODE_REPORT"):
                        postConsoleMessage(KEY_ARGUMENT);
                        break;
                    case ("BUFFER_UPDATE"):
                        srBuffer.setText(KEY_ARGUMENT);
                        break;
                    case ("UPDATE_LINE_NUMBER"):
                        srGcodeLine.setText(KEY_ARGUMENT);
                        break;
                    case ("BUILD_OK"):
                        doBuildOK();
                        break;

                    case ("TINYG_USER_MESSAGE"):
                        doTinyGUserMessage(KEY_ARGUMENT);
                        break;

                    case ("TINYG_CONNECTION_TIMEOUT"):  //This fires if your tinyg is not responding to tgFX in a timely manner.
                        doTinyGConnectionTimeout();
                        break;

                    case ("BUILD_ERROR"):
                        doBuildError(KEY_ARGUMENT);
                        break;

                    default:
                        Main.print("[!]Invalid Routing Key: " + ROUTING_KEY);
                }
            } catch (IOException | JSONException ex) {
                java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        }
    }

    private void doTinyGConnectionTimeout() {
        Main.postConsoleMessage("ERROR! - tgFX timed out while attempting to connect to TinyG.  \nVerify the port you selected and that power is applied to your TinyG.");
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Connect.setText("Connect"); //set the text back to "connect" since we are disconnected
                MonologFXButton btnYes = MonologFXButtonBuilder.create()
                        .defaultButton(true)
                        .icon("/testmonologfx/dialog_apply.png")
                        .type(MonologFXButton.Type.YES)
                        .build();

                MonologFXButton btnNo = MonologFXButtonBuilder.create()
                        .cancelButton(true)
                        .icon("/testmonologfx/dialog_cancel.png")
                        .type(MonologFXButton.Type.NO)
                        .build();

                MonologFX mono = MonologFXBuilder.create()
                        .titleText("TinyG Connection Timeout")
                        .message("tgFX timed out while trying to connect to your TinyG.\nYour TinyG might have a too old firmware version or\n"
                        + "You might have selected the wrong port?"
                        + "Click ok to attempt to auto upgrade your TinyG. \nA Internet Connection is Required."
                        + "\nClicking No will allow you to select a different serial port to try again.")
                        .button(btnYes)
                        .button(btnNo)
                        .type(MonologFX.Type.ERROR)
                        .build();

                MonologFXButton.Type retval = mono.showDialog();


                switch (retval) {
                    case YES:
                        logger.info("Clicked Yes");


                        Platform.runLater(
                                new Runnable() {
                            @Override
                            public void run() {
                                FirmwareUpdaterController.handleUpdateFirmware(null);
                                tg.disconnect();
                                Main.postConsoleMessage("Firmware Updated: Click Connect.");
                            }
                        });
                        break;
                        
                    case NO:
                        logger.info("Clicked No");
                        TinygDriver.getInstance().disconnect(); //free up the serial port to be able to try another one.
                        break;
                }
            }
        });
    }

    private Lcd buildSingleDRO(Lcd tmpLcd, StyleModel sm, String title, String units) {
        tmpLcd = LcdBuilder.create()
                .styleModel(sm)
                .threshold(30)
                .title(title)
                .unit(units)
                .build();
        tmpLcd.setPrefSize(200, 70);
        return tmpLcd;

    }

    private void doTinyGUserMessage(String KEY_ARGUMENT) throws JSONException, IOException {
        if (KEY_ARGUMENT.trim().equals("SYSTEM READY")) {
            //The board has been reset and is ready to re-init our internal tgFX models
            onDisconnectActions();
            CNCMachine.resetDrawingCoords();
            //onConnectActions();  WE ARE DISABLING THIS FOR NOW.  THIS SHOULD KICK OF A RE-QUERY OF THE TINYG ON RESET.  
            //HOWEVER IT IS MAKING OnConnectionActions run 2x.  Need to fix this.
        }
    }

    private void doBuildOK() {
        //TinyG's build version is up to date to run tgfx.
        if (!buildChecked && tg.isConnected().get()) {
            //we do this once on connect, disconnect will reset this flag
            onConnectActionsTwo();
        }
    }

    private void doStatusReport() {
        tgfx.ui.gcode.GcodeTabController.drawCanvasUpdate();
        int rspLine = TinygDriver.getInstance().machine.getLineNumber();

        // Scroll Gcode view to stay in synch with TinyG acks during file send
        if (rspLine != oldRspLine && GcodeTabController.isSendingFile.get()) {
            GcodeTabController.updateProgress(rspLine);
            // Check for gaps in TinyG acks - Note comments are not acked
            if (rspLine != oldRspLine + 1) {
                int gap = oldRspLine + 1;
                //if (gap != 1)
                //postConsoleMessage("NO RESPONSE FOR N" + gap  );  //mikeh says not to put this in... so we won't.
            }
            oldRspLine = rspLine;
        }
    }

    private void doBuildError(String KEY_VALUE) {
        //This is the code to manage the build error window and checking system.
        logger.error("Your TinyG firmware is too old.  System is exiting.");
        console.appendText("Your TinyG firmware is too old.  Please update your TinyG Firmware.\n");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                MonologFXButton btnYes = MonologFXButtonBuilder.create()
                        .defaultButton(true)
                        .icon("/testmonologfx/dialog_apply.png")
                        .type(MonologFXButton.Type.YES)
                        .build();

                MonologFXButton btnNo = MonologFXButtonBuilder.create()
                        .cancelButton(true)
                        .icon("/testmonologfx/dialog_cancel.png")
                        .type(MonologFXButton.Type.NO)
                        .build();

                MonologFX mono = MonologFXBuilder.create()
                        .titleText("TinyG Firware Build Outdated...")
                        .message("Your TinyG firmware is too old to be used with tgFX. \nYour build version: " + tg.machine.getFirmwareBuild() + "\n"
                        + "Minmal Needed Version: " + tg.hardwarePlatform.getMinimalBuildVersion().toString() + "\n\n"
                        + "Click ok to attempt to auto upgrade your TinyG. \nA Internet Connection is Required."
                        + "\nClicking No will exit tgFX.")
                        .button(btnYes)
                        .button(btnNo)
                        .type(MonologFX.Type.ERROR)
                        .build();

                MonologFXButton.Type retval = mono.showDialog();


                switch (retval) {
                    case YES:
                        logger.info("Clicked Yes");

                        WebView firwareUpdate = new WebView();
                        final WebEngine webEngFirmware = firwareUpdate.getEngine();
                        Stage stage = new Stage();
                        stage.setTitle("TinyG Firmware Update Guide");
                        Scene s = new Scene(firwareUpdate, 1280, 800);

                        stage.setScene(s);
                        stage.show();

                        Platform.runLater(
                                new Runnable() {
                            @Override
                            public void run() {
                                webEngFirmware.load("https://github.com/synthetos/TinyG/wiki/TinyG-Boot-Loader#wiki-updating");
                                tg.disconnect();
                                Connect.setText("Connect");
                            }
                        });
                        break;
                    case NO:
                        logger.info("Clicked No");
                        tg.disconnect();
                        System.exit(0);
                        break;
                }
            }
        });

    }

    

    public static void print(String msg) {
        if (TgfxSettingsController.settingDebugBtn.isSelected()) {
            System.out.println(msg);
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("[+]tgFX is starting....");

        /*####################################
         *MISC INIT CODE 
         #################################### */

        tg.resParse.addObserver(this);  //Add the tinygdriver to this observer
        tg.addObserver(this);
        this.reScanSerial();            //Populate our serial ports
        final Logger logger = Logger.getLogger(Main.class);
        final Logger resParserLogger = Logger.getLogger(ResponseParser.class);
        final Logger serialDriverLogger = Logger.getLogger(SerialDriver.class);

        GcodeTabController.setGcodeText(
                "TinyG Disconnected.");

        //This disables the UI if we are not connected.
        consoleVBox.disableProperty()
                .bind(TinygDriver.getInstance().connectionStatus.not());
        topTabPane.disableProperty()
                .bind(TinygDriver.getInstance().connectionStatus.not());

        /*######################################
         * THREAD INITS
         ######################################*/

        Thread serialWriterThread = new Thread(tg.serialWriter);

        serialWriterThread.setName(
                "SerialWriter");
        serialWriterThread.setDaemon(
                true);
        serialWriterThread.start();
        Thread threadResponseParser = new Thread(tg.resParse);

        threadResponseParser.setDaemon(
                true);
        threadResponseParser.setName(
                "ResponseParser");
        threadResponseParser.start();


        /*######################################
         * LOGGER CONFIG
         ######################################*/
        BasicConfigurator.configure();
        //        logger.setLevel(Level.ERROR);
        //        logger.setLevel(Level.INFO);
    /*#######################################################
         * String Converters
         * #####################################################*/
        StringConverter sc = new StringConverter<Number>() {
            @Override
            public String toString(Number n) {
                return String.valueOf(n.floatValue());
            }

            @Override
            public Number fromString(String s) {
                return Integer.valueOf(s);
            }
        };

        /*#######################################################
         * BINDINGS
         * #####################################################*/
        srMomo.textProperty()
                .bind(TinygDriver.getInstance().machine.getMotionMode());
        srVer.textProperty()
                .bind(TinygDriver.getInstance().machine.firmwareVersion);
        srBuild.textProperty()
                .bindBidirectional(TinygDriver.getInstance().machine.firmwareBuild, sc);
        srState.textProperty()
                .bind(TinygDriver.getInstance().machine.m_state);
        srCoord.textProperty()
                .bind(TinygDriver.getInstance().machine.getCoordinateSystem());
        srUnits.textProperty()
                .bind(TinygDriver.getInstance().machine.getGcodeUnitMode());
        srCoord.textProperty()
                .bind(TinygDriver.getInstance().machine.gcm.getCurrentGcodeCoordinateSystemName());
        srGcodeLine.textProperty()
                .bind(TinygDriver.getInstance().machine.getLineNumberSimple().asString());


        /*##########################
         * SOCKET NETWORK INIT CODE
         ##########################/*
         //SocketMonitor sm;
         //Thread remoteListener = new Thread(initRemoteServer("8888"));
         //remoteListener.setName("Remote Listener Thread");
         //remoteListener.start();

        
         /*######################################
         * WEB PAGE SUPPORT
         ######################################*/
//        final WebEngine webEngine = html.getEngine();
//        final WebEngine webEngine2 = makerCam.getEngine();
//
//        Platform.runLater(
//                new Runnable() {
//            @Override
//            public void run() {
//                webEngine.load("https://github.com/synthetos/TinyG/wiki");
//                webEngine2.load("http://simplegcoder.com/js_editor/");
//            }
//        });


        /*############################################
         * BUILD VERSIONING CODE
         ############################################*/
//        buildNumber = Integer.valueOf(getBuildInfo("BUILD").replace(",", ""));
//        buildDate = getBuildInfo("DATE");
//
//        //Set our build / versions in the tgFX settings tab.
//        tgfxBuildDate.setText(buildDate);

        //This is the input text box change listener.
        //Made the blinky line at the end of the box so you can edit the value quickly
//        input.textProperty().addListener(new ChangeListener<String>() {
//               @Override
//               public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
//                    Main.print(input.getCaretPosition());
//                    Platform.runLater(new Runnable() {
//                         @Override
//                         public void run() {
//                              
//                         }
//                    });
//
//               }
//          });
    }
}
