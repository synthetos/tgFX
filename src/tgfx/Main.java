/*
 * Copyright Synthetos LLC
 * Rileyporter@gmail.com
 * www.synthetos.com
 * 
 */
package tgfx;

import tgfx.tinyg.TinygDriver;
import java.io.BufferedReader; 
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;


import tgfx.external.SocketMonitor;
import tgfx.render.Draw2d;
import tgfx.system.Axis;
import tgfx.system.Machine;
import tgfx.system.Motor;
import org.apache.log4j.Logger;
import javafx.scene.canvas.GraphicsContext;

import org.apache.log4j.BasicConfigurator;
import java.io.*;
import java.nio.charset.Charset;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import jfxtras.labs.scene.control.BeanPathAdapter;
import tgfx.gcode.GcodeLine;
import tgfx.system.Machine.Gcode_unit_modes;
import tgfx.tinyg.CommandManager;

public class Main implements Initializable, Observer {

    private boolean drawPreview = true;
    private boolean taskActive = false;
    static final Logger logger = Logger.getLogger(Main.class);
//    private JdomParser JDOM = new JdomParser(); //JSON Object Parser1
    private TinygDriver tg = TinygDriver.getInstance();
    public ObservableList data;
//    BeanPathAdapter<Machine> tgPA = new BeanPathAdapter<>(tg.m);
    public BeanPathAdapter<TinygDriver> tgPA = new BeanPathAdapter<>(tg);
    //tgPA.bindBidirectional("role", cb.valueProperty(), TinygDriver.class);
    /**
     * FXML UI Components
     */
    @FXML
    private Button settingDrawBtn;
    @FXML
    private ListView configsListView;
    @FXML
    private TableColumn<GcodeLine, String> gcodeCol;
    @FXML
    private TableView gcodeView;
    @FXML
    private TabPane motorTabPane, axisTabPane;
    @FXML
    private Pane previewPane;
    @FXML
    private Group drawingCanvas;
    GraphicsContext gp;
    @FXML
    private Button Con, Run, Connect, gcodeZero, btnClearScreen, btnRemoteListener, pauseResume, btnTest;
    @FXML
    TextArea console;
    @FXML
    TextField input, listenerPort;
    @FXML
    private Label xAxisVal, yAxisVal, zAxisVal, aAxisVal, srMomo, srState, srVelo, srBuild,
            srVer, srUnits, srCoord;
    @FXML
    TextArea gcodesList;
    @FXML
    WebView html;
    @FXML
    ChoiceBox serialPorts;
    //##########Config FXML##############//
    @FXML
    TextField motor1ConfigTravelPerRev, motor2ConfigTravelPerRev, motor3ConfigTravelPerRev, motor4ConfigTravelPerRev,
            motor1ConfigStepAngle, motor2ConfigStepAngle, motor3ConfigStepAngle, motor4ConfigStepAngle,
            axisAmaxFeedRate, axisBmaxFeedRate, axisCmaxFeedRate, axisXmaxFeedRate, axisYmaxFeedRate, axisZmaxFeedRate,
            axisAmaxTravel, axisBmaxTravel, axisCmaxTravel, axisXmaxTravel, axisYmaxTravel, axisZmaxTravel,
            axisAjunctionDeviation, axisBjunctionDeviation, axisCjunctionDeviation, axisXjunctionDeviation, axisYjunctionDeviation, axisZjunctionDeviation,
            axisAsearchVelocity, axisBsearchVelocity, axisCsearchVelocity,
            axisXsearchVelocity, axisYsearchVelocity, axisZsearchVelocity,
            axisAzeroBackoff, axisBzeroBackoff, axisCzeroBackoff, axisXzeroBackoff, axisYzeroBackoff, axisZzeroBackoff,
            axisAmaxVelocity, axisBmaxVelocity, axisCmaxVelocity, axisXmaxVelocity, axisYmaxVelocity, axisZmaxVelocity,
            axisAmaxJerk, axisBmaxJerk, axisCmaxJerk, axisXmaxJerk, axisYmaxJerk, axisZmaxJerk,
            axisAradius, axisBradius, axisCradius, axisXradius, axisYradius, axisZradius,
            axisAlatchVelocity, axisBlatchVelocity, axisClatchVelocity, axisXlatchVelocity, axisYlatchVelocity, axisZlatchVelocity, externalConnections,
            materialThickness, gcodeLoaded, axisXlatchBackoff, axisYlatchBackoff, axisZlatchBackoff, axisAlatchBackoff, axisBlatchBackoff, axisClatchBackoff, MachineStatusInterval;
    @FXML
    ChoiceBox motor1ConfigMapAxis, motor2ConfigMapAxis, motor3ConfigMapAxis, motor4ConfigMapAxis,
            motor1ConfigMicroSteps, motor2ConfigMicroSteps, motor3ConfigMicroSteps, motor4ConfigMicroSteps,
            motor1ConfigPolarity, motor2ConfigPolarity, motor3ConfigPolarity, motor4ConfigPolarity,
            motor1ConfigPowerMode, motor2ConfigPowerMode, motor3ConfigPowerMode, motor4ConfigPowerMode,
            axisAmode, axisBmode, axisCmode, axisXmode, axisYmode, axisZmode,
            axisAswitchModeMin, axisAswitchModeMax, axisBswitchModeMin, axisBswitchModeMax, axisCswitchModeMin, axisCswitchModeMax, axisXswitchModeMin, axisXswitchModeMax, axisYswitchModeMin, axisYswitchModeMax,
            axisZswitchModeMin, axisZswitchModeMax, gcodePlane, movementMinLineSegment, movementTimeSegment, movementMinArcSegment, gcodeUnitMode, gcodeCoordSystem,
            gcodePathControl, gcodeDistanceMode;
    @FXML
    Group motor1Node;
    @FXML
    HBox bottom;
    @FXML
    HBox canvasHolder;
    @FXML
    VBox topvbox;
    /**
     * Drawing Code Vars
     *
     */
    double xPrevious = -1;
    double yPrevious = -1;
    double magnification = 1;

//    float x = 0;
//    float y = 0;
//    float z = 0;
//    float vel = 0;
//    String state = new String();
//    LineTo xl = new LineTo();
//    LineTo y1 = new LineTo();
//    LineTo z1 = new LineTo();
//    Path path = new Path();
    @FXML
    private void handleTogglePreview(ActionEvent event) {
        if (settingDrawBtn.getText().equals("ON")) {
            settingDrawBtn.setText("OFF");
            drawPreview = true;
        } else {
            settingDrawBtn.setText("ON");
            drawPreview = false;
        }
    }

    @FXML
    private void handleOpenFile(ActionEvent event) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                logger.debug("handleOpenFile");

                try {
                    console.appendText("[+]Loading a gcode file.....\n");
                    FileChooser fc = new FileChooser();
                    fc.setTitle("Open GCode File");

                    String HOME_DIR = System.getenv("HOME"); //Get Home DIR in OSX
                    if (HOME_DIR == null) {
                        HOME_DIR = System.getProperty("user.home");  //Get Home DIR in Windows
                    }

                    fc.setInitialDirectory(new File(HOME_DIR));  //This will find osx users home dir
                    File f = fc.showOpenDialog(null);
                    FileInputStream fstream = new FileInputStream(f);
                    DataInputStream in = new DataInputStream((fstream));
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String strLine;

                    data.removeAll(data);
                    //Clear the list if there was a previous file loaded

                    int _linenumber = 0;
                    while ((strLine = br.readLine()) != null) {

                        if (!strLine.equals("")) {
                            //Do not add empty lines to the list
//                            gcodesList.appendText(strLine + "\n");

                            if (!strLine.toUpperCase().startsWith("N")) {
                                strLine = "N" + String.valueOf(_linenumber) + " " + strLine;
                            }

                            data.add(new GcodeLine(strLine, _linenumber));
                            _linenumber++;
//                        System.out.println(strLine);
                        }
                    }
                    System.out.println("[*]File Loading Complete");

                } catch (FileNotFoundException ex) {
                    System.out.println("File Not Found.");
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleCancelFile(ActionEvent evt) throws Exception {
        console.appendText("[!]Canceling File Sending Task...\n");
//        tg.setCANCELLED(true);
        setTaskActive(false);
        Byte reset = 0x18;
//        tg.resetSpaceBuffer();
        tg.priorityWrite(reset); //This resets TinyG
        Thread.sleep(3000); //We need to sleep a bit until TinyG comes back
        tg.write(CommandManager.CMD_QUERY_STATUS_REPORT);  //This will reset our DRO readings
    }

    @FXML
    private void handlePauseResumeAct(ActionEvent evt) throws Exception {
        if ("Pause".equals(pauseResume.getText())) {
            pauseResume.setText("Resume");
            tg.priorityWrite("!\n");
//            tg.setPAUSED(true);

        } else {
            pauseResume.setText("Pause");
//            tg.setPAUSED(false);
            tg.priorityWrite("~\n");
        }
    }

    @FXML
    void handleTestButton(ActionEvent evt) throws Exception {
        logger.info("Test Button....");
        tg.write(CommandManager.CMD_QUERY_HARDWARE_BUILD_NUMBER);
//        TinygDriver.getInstance().priorityWrite((byte)0x18);
        TinygDriver.getInstance().write(CommandManager.CMD_APPLY_SYSTEM_MNEMONIC_SYSTEM_SWITCH_TYPE_NC);
        TinygDriver.getInstance().m.setFirmwareVersion("867543");
        TinygDriver.getInstance().m.setFirmwareBuild(0.01);
    }

    @FXML
    void handleMotorQuerySettings(ActionEvent evt) {
        System.out.println("[+]Querying Motor Config...");
        //Detect what motor tab is "active"...
        try {
//            updateGuiAxisSettings();
            if (motorTabPane.getSelectionModel().getSelectedItem().getText().equals("Motor 1")) {
                tg.queryHardwareSingleMotorSettings(1);
            } else if (motorTabPane.getSelectionModel().getSelectedItem().getText().equals("Motor 2")) {
                tg.queryHardwareSingleMotorSettings(2);
            } else if (motorTabPane.getSelectionModel().getSelectedItem().getText().equals("Motor 3")) {
                tg.queryHardwareSingleMotorSettings(3);
            } else if (motorTabPane.getSelectionModel().getSelectedItem().getText().equals("Motor 4")) {
                tg.queryHardwareSingleMotorSettings(4);
            }
        } catch (Exception ex) {
            System.out.println("[!]Error Querying Single Motor....");
        }
    }

    @FXML
    private void handleAxisApplySettings(ActionEvent evt) {
        console.appendText("[+]Applying Axis.......\n");
        try {

            tg.applyHardwareAxisSettings(axisTabPane.getSelectionModel().getSelectedItem());

            //TODO:  Breakout Individual response messages vs having to call queryAllHardwareAxisSettings
            //something like if {"1po":1} then parse and update only the polarity setting
//            Thread.sleep(TinygDriver.CONFIG_DELAY);
//            tg.queryAllHardwareAxisSettings();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println("ERROR IN HANDLEAPPLYAXISSETTINGS");
        }

    }

    @FXML
    private void handleAxisQuerySettings(ActionEvent evt) throws Exception {
        String _axisSelected = axisTabPane.getSelectionModel().getSelectedItem().getText().toLowerCase();
        console.appendText("[+]Querying Axis: " + _axisSelected + "\n");
//        tg.queryAllHardwareAxisSettings();
        try {
            tg.queryHardwareSingleAxisSettings(_axisSelected);
        } catch (Exception ex) {
            System.out.println("[!]Error Querying Axis: " + _axisSelected);
        }
    }

    @FXML
    private void handleAxisEnter(final InputEvent event) throws Exception {
        //private void handleEnter(ActionEvent event) throws Exception {
        final KeyEvent keyEvent = (KeyEvent) event;
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            Axis _axis = Machine.getInstance().getAxisByName(axisTabPane.getSelectionModel().getSelectedItem().getText().toLowerCase().substring(0, 1));
            if (event.getSource().toString().startsWith("TextField")) {
                //Object Returned is a TextField Object
                TextField tf = (TextField) event.getSource();
                tg.applyHardwareAxisSettings(_axis, tf);
                console.appendText("[+]Applying Axis.......\n");

            }
        }
    }

    @FXML
    void handleMotorApplySettings(ActionEvent evt) {
        console.appendText("[+]Applying Motor.......\n");
        try {
            tg.applyHardwareMotorSettings(motorTabPane.getSelectionModel().getSelectedItem());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println("ERROR IN HANDLEAPPLYAXISSETTINGS");
        }
    }

    private String checkConectedMessage() {
        if (TinygDriver.getInstance().isConnected()) {
            return ("true");
        } else {
            return ("[!]TinyG is Not Connected");
        }
    }

    @FXML
    void handleApplyDefaultSettings(ActionEvent evt) {
        try {
            if (checkConectedMessage().equals("true")) {
                TinygDriver.getInstance().write(CommandManager.CMD_APPLY_DEFAULT_SETTINGS);
            } else {
                Main.logger.error(checkConectedMessage());
                console.appendText(checkConectedMessage());
            }
        } catch (Exception ex) {
            Main.logger.error("[!]Error Applying Default Settings");
        }
    }

    @FXML
    void handleRemoteListener(ActionEvent evt) {
//        if (tg.isConnected()) {
//            console.appendText("[+]Remote Monitor Listening for Connections....");
//            Task SocketListner = this.initRemoteServer(listenerPort.getText());
//
//            new Thread(SocketListner).start();
//            btnRemoteListener.setDisable(true);
//        } else {
//            System.out.println("[!] Must be connected to TinyG First.");
//            console.appendText("[!] Must be connected to TinyG First.");
//        }
    }
//    
//    @FXML
//    void handleMotorQuery(ActionEvent evt){
//        
//    }

    @FXML
    void handleMouseScroll(ScrollEvent evt) {
        if (evt.getDeltaX() == 0 && evt.getDeltaY() == 40) {
            //Mouse Wheel Up
            Draw2d.setMagnification(true);
            console.appendText("[+]Zooming in " + String.valueOf(Draw2d.getMagnification()) + "\n");


        } else if (evt.getDeltaX() == 0 && evt.getDeltaY() == -40) {
            //Mouse Wheel Down
            Draw2d.setMagnification(false);
            console.appendText("[+]Zooming out " + String.valueOf(Draw2d.getMagnification()) + "\n");
        }

        drawingCanvas.setScaleX(Draw2d.getMagnification());
        drawingCanvas.setScaleY(Draw2d.getMagnification());


//        canvsGroup.setScaleX(Draw2d.getMagnification());
//        canvsGroup.setScaleY(Draw2d.getMagnification());
    }

//    private void reDrawPreview() {
//        for (Node n : canvsGroup.getChildren()) {
//            Line nd = (Line) n;
//            nd.setStrokeWidth(Draw2d.getStrokeWeight());
//        }
////        console.appendText("[+]2d Preview Stroke Width: " + String.valueOf(Draw2d.getStrokeWeight()) + "\n");
//    }
    @FXML
    private void zeroSystem(ActionEvent evt) {
        if (tg.isConnected()) {
            try {
                tg.write(CommandManager.CMD_APPLY_SYSTEM_ZERO_ALL_AXIS);
                //G92 does not invoke a status report... So we need to generate one to have
                //Our GUI update the coordinates to zero
                tg.write(CommandManager.CMD_QUERY_STATUS_REPORT);
                //We need to set these to 0 so we do not draw a line from the last place we were to 0,0
                xPrevious = 0;
                yPrevious = 0;
            } catch (Exception ex) {
            }
        }
    }

    @FXML
    private void handleRunFile(ActionEvent evt) {
        taskActive = true; //Set the thread condition to start
        Task fileSend = fileSenderTask();
        Thread fsThread = new Thread(fileSend);
        fsThread.setName("FileSender");
        fsThread.start();

    }

    public Task fileSenderTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                StringBuilder line = new StringBuilder();
                int numbGcodeLines = data.size();
                String tmp;
                for (int i = 0; i < numbGcodeLines; i++) {
                    GcodeLine _gcl = (GcodeLine) data.get(i);


                    if (isCancelled()) {
                        //Cancel Button was pushed
                        console.appendText("[!]File Sending Task Killed....\n");
                        break;

                    } else {
                        if (_gcl.getCodeLine().startsWith("(")) {
                            console.appendText("GCODE COMMENT:" + _gcl.getCodeLine() + "\n");
                            continue;
                        } else if (_gcl.getCodeLine().equals("")) {
                            //Blank Line.. Passing.. 
                            continue;
                        }
                        line.setLength(0);
                        line.append("{\"gc\":\"").append(_gcl.getCodeLine()).append("\"}\n");
                        while (TinygDriver.getInstance().isPAUSED()) {
                            Thread.sleep(50);
                        }
                        tg.write(line.toString());
                    }
                }
                return true;
            }
        };
    }

    public synchronized boolean isTaskActive() {
        return taskActive;
    }

    public synchronized void setTaskActive(boolean taskActive) {
        this.taskActive = taskActive;
    }

//
//                while (tg.ser.CONNECTED) {
//                    if (tg.ser.CLEAR_TO_TRANSMIT.get()) {
//                        for (String l : gcodeProgramList) {
//                            if (l.startsWith("(") || l.equals("")) {
//                                logger.debug("[#]Gcode line started with a ( or was blank");
//                                //Skip these lines as they will not illicit a "OK" 
//                                //From tinyg
//                                continue;
//                            } else {
//                                line = "{\"gc\":\"" + l + "\"}" + "\n";
//
////                                if (tg.getClearToSend() && !tg.isPAUSED() && !tg.isCANCELLED()) {
//                                    tg.write(line);
////                                    logger.debug("\t WROTE --> " + line);
//
////                                }
//                            }
//                        }
//                    }
//                }
//
//                    tg.write(TinygDriver.CMD_QUERY_OK_PROMPT);  //Lets make sure we are clear to send.
//
//                    for (String l : gcodeProgramList) {
//                        if (!tg.isConnected()) {
//                            console.appendText("[!]Serial Port Disconnected.... Stopping file sending task...");
//                            logger.info("[!]Serial Port Disconnected.... Stopping file sending task...");
//                            return false;
//                            //break;
//                        }
//
//                        //###############CRITICAL SECTION#########################
//                        //If this code is changed be very careful as there is much logic here
//                        //to screw up.  This code makes it so that when you send a file and disconnect or
//                        //press stop this filesending task dies.  
//                        if (l.startsWith("(") || l.equals("")) {
//                            logger.debug("[#]Gcode line started with a ( or was blank");
//                            //Skip these lines as they will not illicit a "OK" 
//                            //From tinyg
//                            continue;
//                        } else {
//                            line = "{\"gc\":\"" + l + "\"}" + "\n";
//
//                            if (tg.getClearToSend() && !tg.isPAUSED() && !tg.isCANCELLED()) {
//                                tg.write(line);
//                                logger.debug("\t WROTE --> " + line);
//
//                            } else if (tg.isCANCELLED()) {
//                                console.appendText("[!]Canceling the file sending task...\n");
//                                logger.info("[!]Canceling the file sending task...");
//                                return false;
//
//                            } else if (tg.isPAUSED()) {
//                                logger.info("[+]File Sender task is paused");
//                                console.appendText("[+]File Sender task is paused");
//                                while (tg.isPAUSED()) {
//                                    //Infinite Loop
//                                    //Not ready yet
//                                    Thread.sleep(10);
//                                }
//                                //This breaks out of the while loop and does some more checking to eliminate any race conditions
//                                //That might have occured duing waiting for to unpause.
//                                if (!tg.isConnected()) {
//                                    console.appendText("[!]Serial Port Disconnected.... Stopping file sending task...");
//                                    logger.info("[!]Serial Port Disconnected.... Stopping file sending task...");
//                                    return false;
//                                } else if (tg.isCANCELLED()) {
//                                    console.appendText("[!]Canceling the file sending task...");
//                                    logger.info("[!]Canceling the file sending task...");
//                                    return false;
//                                }
//                                tg.write(line);
//                                logger.debug("\t WROTE --> " + line);
//                            } else {
//                                while (!tg.getClearToSend()) {
//                                    //Not ready yet
////                                    Thread.sleep(10);
//                                    //We have to check again while in the sleeping thread that sometime
//                                    //during waiting for the clearbuffer the serialport has not been disconnected.
//                                    //And cancel has not been called
//                                    if (!tg.isConnected()) {
//                                        console.appendText("[!]Serial Port Disconnected.... Stopping file sending task...");
//                                        logger.info("[!]Serial Port Disconnected.... Stopping file sending task...");
//                                        return false;
//                                    } else if (tg.isCANCELLED()) {
//                                        console.appendText("[!]Canceling the file sending task...\n");
//                                        logger.info("[!]Canceling the file sending task...");
//                                        return false;
//                                    }
//                                }
//
//                                //This looks like its not needed since the same check above in the while block.
//                                //However I am pretty confident that this is.
//                                if (!tg.isConnected()) {
//                                    console.appendText("[!]Serial Port Disconnected.... Stopping file sending task...");
//                                    logger.info("[!]Serial Port Disconnected.... Stopping file sending task...");
//                                    return false;
//                                }
//                                //Finally write the line everything is Good to go.
//                                tg.write(line);
//                                logger.debug("\t WROTE --> " + line);
//
//                            }
//                        }
//                    }
//                    console.appendText("[+] Sending File Complete\n");
//                    return true;
//
//                }
    //###############CRITICAL SECTION#########################
    @FXML
    private void FXreScanSerial(ActionEvent event) {
        this.reScanSerial();
    }

    private void reScanSerial() {
        serialPorts.getItems().clear();
        String portArray[] = null;
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

//            
//            tg.write("{\"1ma\":1}");
//            tg.write("{\"gun\":null}");
//
//            tg.write("{\"xfr\":1500}");



            /*
             * Do CMD_QUERY_SYSTEM_SETTINGS FIRST
             */
            //FIRST
            tg.write(CommandManager.CMD_QUERY_SYSTEM_SETTINGS);  //On command to rule them all.
            //FIRST

            tg.cmdManager.queryAllMotorSettings();
            tg.cmdManager.queryAllHardwareAxisSettings();
            tg.write(CommandManager.CMD_QUERY_STATUS_REPORT);  //If TinyG current positions are other than zero

            /**
             * Draw the workspace area in the preview
             */
//            Float width = Float.valueOf(tg.m.getAxisByName("X").getTravel_maximum());
//            Float height = Float.valueOf(tg.m.getAxisByName("Y").getTravel_maximum());
//            Label hLabel = new Label("Table Width: " + width);
            //Rectangle rect1 = RectangleBuilder.create().strokeDashOffset(5).opacity(100).width(width).height(height).build();
            //canvsGroup.getChildren().add(rect1);
            //canvsGroup.getChildren().add(hLabel);
        } catch (Exception ex) {
            console.appendText("[!]Error: " + ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    private void handleConnect(ActionEvent event) throws Exception {
        //Get a list of serial ports on the system.

        if (serialPorts.getSelectionModel().getSelectedItem() == (null)) {
            console.appendText("[+]Error Connecting to Serial Port please select a valid port.\n");
            return;
        }
        if (Connect.getText().equals("Connect") && serialPorts.getSelectionModel().getSelectedItem() != (null)) {
            String serialPortSelected = serialPorts.getSelectionModel().getSelectedItem().toString();

            System.out.println("[+]Connecting...");
            tg.initialize(serialPortSelected, 115200);
            if (tg.isConnected()) {

                console.appendText("[+]Connected to " + serialPortSelected + " Serial Port Successfully.\n");
                Connect.setText("Disconnect");

                /**
                 * *****************************
                 * OnConnect Actions Called Here 
                 * *****************************
                 */
                onConnectActions();


            }
        } else {
            tg.write(CommandManager.CMD_QUERY_OK_PROMPT);
            tg.disconnect();
            if (!tg.isConnected()) {
                console.appendText("[+]Disconnected from " + tg.getPortName() + " Serial Port Successfully.\n");
                Connect.setText("Connect");
                onDisconnectActions();
            }

        }
    }

    public void onDisconnectActions() {
        TinygDriver.getInstance().m.setFirmwareBuild(0.0);
//        srVer.setText("?");
//        srMomo.setText("?");
//        TinygDriver.getInstance().m.setVelocity(0.0);
//        srState.setText("?");
//        tg.resetSpaceBuffer();

    }

    @FXML
    private void handleClearScreen(ActionEvent evt) {
        console.appendText("[+]Clearning Screen...\n");
        drawingCanvas.getChildren().clear();
    }

//    private void handleTilda() {
//        //                ==============HIDE CONSOLE CODE==============
//        System.out.println("TILDA");
//        if (topvbox.getChildren().contains(bottom)) {
//            topvbox.getChildren().remove(bottom);
//
//        } else {
//            topvbox.getChildren().add(topvbox.getChildren().size() - 1, bottom);
//        }
//    }
    @FXML
    private void handleKeyInput(final InputEvent event) {
//        if (event instanceof KeyEvent) {
//            final KeyEvent keyEvent = (KeyEvent) event;
//            if (keyEvent.getCode() == KeyCode.BACK_QUOTE) {
//
//                handleTilda();
//
//            } else if (keyEvent.getCode() == KeyCode.F10) {
//                if (!tg.isConnected()) {
//                    String msg = new String("[!]Getting Status Report Aborted... Serial Port Not Connected...");
//                    console.appendText(msg);
//                    return;
//                } else {
//                    String msg = new String("F10 Key Pressed - Getting Status Report\n");
//                    console.appendText(msg);
//                    System.out.println(msg);
//                    try {
//                        tg.requestStatusUpdate();
//                    } catch (Exception ex) {
//                        System.out.println("Error in getting status report");
//                    }
//                }
//            } else if (keyEvent.getCode() == KeyCode.F5) {
//                if (!tg.isConnected()) {
//                    String msg = new String("[!]Getting Settings Aborted... Serial Port Not Connected...");
//                    console.appendText(msg);
//                    return;
//                }
//                String msg = new String("F5 Key Pressed - Getting Machine Settings\n");
//                console.appendText(msg);
//                System.out.println(msg);
//                try {
////                    tg.requestStatusUpdate();
////                    tg.getMachineSettings();
//                    tg.getMotorSettings(1);
//                    tg.getMotorSettings(2);
//                    tg.getMotorSettings(3);
//                    tg.getMotorSettings(4);
//
//                } catch (Exception ex) {
//                    System.out.println(ex.getMessage());
//                }
//
//
//            } else if (keyEvent.getCode() == KeyCode.F12) {
//                System.out.println("Writing DEBUG file");
//                try {
//                    BufferedWriter out = new BufferedWriter(new FileWriter("debugTest.txt"));
//                    DataOutputStream dos;
//                    dos = new DataOutputStream(new FileOutputStream("debug.txt", true));
//                    out.close();
//
//                } catch (Exception e) {
//                    System.out.println("Exception ");
//                }
//            } else if (keyEvent.getCode() == KeyCode.F1) {
//                Draw2d.incrementSetStrokeWeight();
////                reDrawPreview();
//                console.appendText("[+]Increasing Stroke Width: " + String.valueOf(Draw2d.getStrokeWeight()) + "\n");
//            } else if (keyEvent.getCode() == KeyCode.F2) {
//                Draw2d.decrementSetStrokeWeight();
////                reDrawPreview();
//                console.appendText("[+]Decreasing Stroke Width: " + String.valueOf(Draw2d.getStrokeWeight()) + "\n");
//            }
//        }
    }

//    @FXML
//    private void gcodeProgramClicks(MouseEvent me) {
//        TextField tField = (TextField) gcodesList.getSelectionModel().getSelectedItem();
//        if (me.getButton() == me.getButton().SECONDARY) {
////            tg.write("{\"gc\":\"" + lbl.getText() + "\"}\n");
//            System.out.println("RIGHT CLICKED");
//
//
//        } else if (me.getButton() == me.getButton().PRIMARY && me.getClickCount() == 2) {
//            System.out.println("double clicked");
//            tField.setEditable(true);
//
//            //if (lbl.getParent().getStyleClass().contains("breakpoint")) {
////                lbl.getParent().getStyleClass().remove("breakpoint");
////                console.appendText("BREAKPOINT REMOVED: " + lbl.getText() + "\n");
////                System.out.println("BREAKPOINT REMOVED");
////            } else {
////
////                System.out.println("DOUBLE CLICKED");
////                lbl.getStyleClass().removeAll(null);
////                lbl.getParent().getStyleClass().add("breakpoint");
////                System.out.println("BREAKPOINT SET");
////                console.appendText("BREAKPOINT SET: " + lbl.getText() + "\n");
////            };
//        }
//    }
    @FXML
    private void handleSaveConfig(ActionEvent event) throws Exception {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {


                FileChooser fc = new FileChooser();
                fc.setInitialDirectory(new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "configs" + System.getProperty("file.separator")));
                fc.setTitle("Save Current TinyG Configuration");
                File f = fc.showOpenDialog(null);
                if (f.canWrite()) {
                }
            }
        });
    }

    @FXML
    private void handleImportConfig(ActionEvent event) throws Exception {
        //This function gets the config file selected and applys the settings onto tinyg.
        InputStream fis;
        BufferedReader br;
        String line;

        File selected_config = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "configs" + System.getProperty("file.separator") + configsListView.getSelectionModel().getSelectedItem());

        fis = new FileInputStream(selected_config);
        br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

        while ((line = br.readLine()) != null) {
            if (tg.isConnected()) {
                if (line.startsWith("NAME:")) {
                    //This is the name of the CONFIG lets not write this to TinyG 
                    console.appendText("[+]Loading " + line.split(":")[1] + " config into TinyG... Please Wait...");
                } else {
                    tg.write(line + "\n");    //Write the line to tinyG
                    Thread.sleep(100);      //Writing Values to eeprom can take a bit of time..
                    console.appendText("[+]Writing Config String: " + line + "\n");
                }
            }
        }
    }

    @FXML
    private void handleEnter(final InputEvent event) throws Exception {
        //private void handleEnter(ActionEvent event) throws Exception {
        final KeyEvent keyEvent = (KeyEvent) event;

        String PROMPT = "tinyg>";
        if (keyEvent.getCode() == KeyCode.ENTER) {
            System.out.println("Entered");
            if (!tg.isConnected()) {
                System.out.println("TinyG is not connected....\n");
                console.appendText("[!]TinyG is not connected....\n");
                input.setPromptText(PROMPT);
                return;
            }
            //TinyG is connected... Proceed with processing command.
            String command = (input.getText() + "\n");
            //This will send the command to get a OK prompt if the buffer is empty.
            if ("".equals(command)) {
                tg.write(CommandManager.CMD_QUERY_OK_PROMPT);
            } else {
                if (command.contains("$")) {
                    if (command.contains("defaults")) {
                        //If someone resets TinyG's defaults we need to completely refresh the system.
                        console.appendText("[#]Restoring TinyG Defaults... Please Wait.....\n");
                        Thread.sleep(50); // Let the console update before we start to pause the UI thread.
                        tg.write(CommandManager.CMD_APPLY_DEFAULT_SETTINGS);
                        Thread.sleep(4000);  //This takes a bit of time to run on TinyG.  We might want to 
                        //make this into a runnable or something of the sort.  For now we pause in the main event loop :(
                        onConnectActions();
                        console.appendText("[#]Restore Complete...\n");
                        input.clear();
                        input.setPromptText(PROMPT);
                        return;
                    } else if (!command.contains("=")) {
                        console.appendText("[!]Please use = in your $ command.\n");
                        console.appendText("[!]Example: $xsr=1200\n");
                        return;
                    }

                    //Command line setting detected

                    String cmd = command.split("=")[0].replace("$", ""); //Grabs the command... xfr in the example above...
                    String value = command.split("=")[1].replace("\n", "");  //Grabs the value... so 1200 in the example..
                    Scanner scanner = new Scanner(value);
                    if (scanner.hasNextDouble()) {
                        tg.write("{\"" + cmd + "\":" + value + "}\n");
                    } else {
                        tg.write("{\"" + cmd + "\":\"" + value + "\"}\n");
                    }

                    console.appendText("$Command: " + command + "\n");

                    //TODO: Clean this up to only refresh the axis or motor or machine group vs all of the settings
                    tg.write(CommandManager.CMD_QUERY_SYSTEM_SETTINGS);
                    tg.cmdManager.queryAllHardwareAxisSettings();
                    tg.cmdManager.queryAllMotorSettings();

                    input.clear();
                    input.setPromptText(PROMPT);
                } else {
                    //Catch if a unit mode command was sent... Update the status bar if it was..
                    if (command.toLowerCase().contains("g20") || command.toLowerCase().contains("g21")) {
//                        tg.write(command);
                        console.appendText(command);
                        input.clear();
                        input.setPromptText(PROMPT);
//                        onConnectActions(); //Do this to get updated values in INCHES or MM
                        switch(command){
                            case("g20\n"):
                                tg.priorityWrite("{\"gc\":\"g20\"}\n");
                                break;
                            case("g21\n"):
                                tg.priorityWrite("{\"gc\":\"g21\"}\n");
                                break;
                                
                        }
//                        srUnits.setText(tg.getInstance().m.getGcodeUnitMode().toString());
                        console.appendText("[+]Unit Mode Change Detected... Units set to: " + TinygDriver.getInstance().m.getGcodeUnitMode().toString() + "\n");
                    } else if (command.toLowerCase().contains("g54") || command.toLowerCase().contains("g55") || command.toLowerCase().contains("g56")
                            || command.toLowerCase().contains("g57") || command.toLowerCase().contains("g58") || command.toLowerCase().contains("g59")) {
                        tg.write(command);
                        console.appendText(command);
                        input.clear();
                        input.setPromptText(PROMPT);
                        tg.write(CommandManager.CMD_QUERY_STATUS_REPORT); //the coord is a field that is enabled by default in status reports.
//                        srUnits.setText(tg.getInstance().m.getCoordinateSystem().toString());
                        console.appendText("[+]Coordinate Mode Change Detected... Coordniate Mode set to: " + tg.getInstance().m.getCoordinateSystem().toString() + "\n");
                    } else {
                        //Execute whatever you placed in the input box
                        tg.write(command);
                        console.appendText(command);
                        input.clear();
                        input.setPromptText(PROMPT);
                    }
                }
            }

        }
    }

    private Task initRemoteServer(String port) {
        final String Port = port;
        return new Task() {
            @Override
            protected Object call() throws Exception {
                SocketMonitor sm = new SocketMonitor(Port);

                System.out.println("[+]Trying to start remote monitor.");
                return true;
            }
        };
    }

    public void drawLine(Machine.motion_modes moveType, double vel) {



        //Code to make mm's look the same size as inches
        double unitMagnication = 3;
        if (tg.m.getGcodeUnitMode().get().equals(Gcode_unit_modes.INCHES.toString()) ) {
            unitMagnication = unitMagnication * 10;
        }
        double newX = unitMagnication * (Double.valueOf(tg.m.getAxisByName("X").getWork_position().get()));// + magnification;
        double newY = unitMagnication * (Double.valueOf(tg.m.getAxisByName("Y").getWork_position().get()));// + magnification;

        Line l = new Line(xPrevious, yPrevious, newX, newY);
//        l.setStroke(Color.BLUE);
        l.setStroke(Draw2d.getLineColorFromVelocity(vel));

//        if (moveType == Machine.motion_modes.traverse) {
//            //G0 Move
//            l.setStrokeWidth(Draw2d.getStrokeWeight() / 2);
//            l.setStroke(Draw2d.TRAVERSE);
//        }


        //CODE TO ONLY DRAW CUTTING MOVEMENTS
//        if (tg.m.getAxisByName("Z").getWork_position() > 0) {
//            l = null;
//        } else {
//            l.setStrokeWidth(Draw2d.getStrokeWeight());
//        }

        l.setStrokeWidth(1);

        xPrevious = newX;
        yPrevious = newY;

        if (l != null) {
            drawingCanvas.getChildren().add(l);
        }


    }

    private void updateGuiState(String line) {
        final String l = line;
        if (l.contains("msg")) {
            //Pass this on by..
        } else if (line.equals("BUILD_UPDATE")) {
            Platform.runLater(new Runnable() {
                float vel;

                public void run() {
                    //We are now back in the EventThread and can update the GUI
                    try {
                        srBuild.setText(String.valueOf(tg.m.getFirmwareBuild()));
                        srVer.setText(String.valueOf(tg.m.getFirmwareVersion()));
                    } catch (Exception ex) {
                        System.out.println("[!]Exception in UpdateGUI State()");
                        System.out.println(ex.getMessage());
                    }

                }
            });

        } else if (line.equals("STATUS_REPORT")) {
            Platform.runLater(new Runnable() {
                float vel;

                public void run() {
                    //We are now back in the EventThread and can update the GUI
                    try {

                        xAxisVal.setText(String.valueOf(tg.m.getAxisByName("X").getWork_position())); //json.getNode("sr").getNode("posx").getText());
                        yAxisVal.setText(String.valueOf(tg.m.getAxisByName("Y").getWork_position()));
                        zAxisVal.setText(String.valueOf(tg.m.getAxisByName("Z").getWork_position()));
                        aAxisVal.setText(String.valueOf(tg.m.getAxisByName("A").getWork_position()));

                        //Update State... running stop homing etc.
//                        srState.setText(tg.m.getMachineState().toString().toUpperCase());
//                        srUnits.setText(tg.m.getGcodeUnitMode().toString());

//                        srMomo.setText(tg.m.getMotionMode().toString().replace("_", " ").toUpperCase());
                        //Set the motion mode

                        //Parse the veloicity 
//                        vel = tg.m.getVelocity();
//                        srVelo.setText(String.valueOf(vel));



                    } catch (Exception ex) {
                        System.out.println("[!]Error in UpdateGuiState -> STATUS_REPORT");
                        System.out.println(ex.getMessage());
                    }

                }
            });

        }
    }

    private void updateGUIConfigState() {
        //Update the GUI for config settings
        Platform.runLater(new Runnable() {
            float vel;

            public void run() {
                //We are now back in the EventThread and can update the GUI for the CMD SETTINGS
                //Right now this is how I am doing this.  However I think there can be a more optimized way
                //Perhaps by passing a routing message as to which motor was updated then not all have to be updated
                //every time one is.

                try {
                    for (Motor m : tg.m.getMotors()) {
                        if (m.getId_number() == 1) {

                            motor1ConfigMapAxis.getSelectionModel().select((tg.m.getMotorByNumber(1).getMapToAxis()));
                            motor1ConfigMicroSteps.getSelectionModel().select(tg.m.getMotorByNumber(1).getMicrosteps());
                            motor1ConfigPolarity.getSelectionModel().select(tg.m.getMotorByNumber(1).isPolarityInt());
                            motor1ConfigPowerMode.getSelectionModel().select(tg.m.getMotorByNumber(1).isPower_managementInt());
                            motor1ConfigStepAngle.setText(String.valueOf(tg.m.getMotorByNumber(1).getStep_angle()));
                            motor1ConfigTravelPerRev.setText(String.valueOf(tg.m.getMotorByNumber(1).getTravel_per_revolution()));
                        } else if (m.getId_number() == 2) {
                            motor2ConfigMapAxis.getSelectionModel().select(tg.m.getMotorByNumber(2).getMapToAxis());
                            motor2ConfigMicroSteps.getSelectionModel().select(tg.m.getMotorByNumber(2).getMicrosteps());
                            motor2ConfigPolarity.getSelectionModel().select(tg.m.getMotorByNumber(2).isPolarityInt());
                            motor2ConfigPowerMode.getSelectionModel().select(tg.m.getMotorByNumber(2).isPower_managementInt());
                            motor2ConfigStepAngle.setText(String.valueOf(tg.m.getMotorByNumber(2).getStep_angle()));
                            motor2ConfigTravelPerRev.setText(String.valueOf(tg.m.getMotorByNumber(2).getTravel_per_revolution()));
                        } else if (m.getId_number() == 3) {
                            motor3ConfigMapAxis.getSelectionModel().select(tg.m.getMotorByNumber(3).getMapToAxis());
                            motor3ConfigMicroSteps.getSelectionModel().select(tg.m.getMotorByNumber(3).getMicrosteps());
                            motor3ConfigPolarity.getSelectionModel().select(tg.m.getMotorByNumber(3).isPolarityInt());
                            motor3ConfigPowerMode.getSelectionModel().select(tg.m.getMotorByNumber(3).isPower_managementInt());
                            motor3ConfigStepAngle.setText(String.valueOf(tg.m.getMotorByNumber(3).getStep_angle()));
                            motor3ConfigTravelPerRev.setText(String.valueOf(tg.m.getMotorByNumber(3).getTravel_per_revolution()));
                        } else if (m.getId_number() == 4) {
                            motor4ConfigMapAxis.getSelectionModel().select(tg.m.getMotorByNumber(4).getMapToAxis());
                            motor4ConfigMicroSteps.getSelectionModel().select(tg.m.getMotorByNumber(4).getMicrosteps());
                            motor4ConfigPolarity.getSelectionModel().select(tg.m.getMotorByNumber(4).isPolarityInt());
                            motor4ConfigPowerMode.getSelectionModel().select(tg.m.getMotorByNumber(4).isPower_managementInt());
                            motor4ConfigStepAngle.setText(String.valueOf(tg.m.getMotorByNumber(4).getStep_angle()));
                            motor4ConfigTravelPerRev.setText(String.valueOf(tg.m.getMotorByNumber(4).getTravel_per_revolution()));
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("$$$$$$$$$$$$$EXCEPTION in CMD_SETTINGS_UPDATE$$$$$$$$$$$$$$");
                    System.out.println(ex.getMessage());
                }

            }
        });
    }

    private void updateGuiStatusReport(String line) {
        final String l = line;
        Platform.runLater(new Runnable() {
//            @Override
            public void run() {
                //logger.info("updateGuiStatusReport Ran");
//                String vel = String.valueOf(TinygDriver.getInstance().m.getVelocity();
                //We are now back in the EventThread and can update the GUI
                try {
//                    Machine m = TinygDriver.getInstance().m;
//                    xAxisVal.setText(String.valueOf(new DecimalFormat("0.000").format(m.getAxisByName("X").getWork_position()))); //json.getNode("sr").getNode("posx").getText());
//                    yAxisVal.setText(String.valueOf(new DecimalFormat("0.000").format(m.getAxisByName("Y").getWork_position())));
//                    zAxisVal.setText(String.valueOf(new DecimalFormat("0.000").format(m.getAxisByName("Z").getWork_position())));
//                    //zAxisVal2.setText(String.valueOf(m.getAxisByName("Z").getWork_position())); //Dedicated Z depth preview label
//                    aAxisVal.setText(String.valueOf(new DecimalFormat("0.000").format(m.getAxisByName("A").getWork_position())));
                    
//                    axisAjunctionDeviation.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getJunction_devation())));

                    //Update State... running stop homing etc.
//                    srState.setText(m.getMachineState().toString().toUpperCase());
//                    srUnits.setText(m.getGcodeUnitMode().toString());
//                    srCoord.setText(m.getCoordinateSystem().toString());
//                    srMomo.setText(m.getMotionMode().toString().replace("_", " ").toUpperCase());


                    //Parse the veloicity 
//                    vel = m.getVelocity();
//                    srVelo.setText(String.valueOf(vel));

                    //############################################################
                    /**
                     * This enables drawing on the GUI Uncomment it if you want
                     * it to draw
                     *
                     *
                     */
                    if (drawPreview) {
                        drawLine(Machine.motion_modes.traverse, new Float(12.32322));
                    }
                    //##############################################################

//                    drawLine(m.getMotionMode(), vel, zAxisVal.getText());
//                        renderZ();
                } catch (Exception ex) {
                    System.out.println("[!] Exception in UpdateGuiStatusReport...");
                    System.out.println(ex.getMessage());
                }
            }
        });

    }

    private void updateGuiMachineSettings(String line) {
        final String l = line;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //We are now back in the EventThread and can update the GUI
                try {
                    Machine m = TinygDriver.getInstance().m;
                    srBuild.setText(String.valueOf(m.getFirmwareBuild()));
                    srVer.setText(String.valueOf(m.getFirmwareVersion()));
                    gcodePlane.getSelectionModel().select(TinygDriver.getInstance().m.getGcode_select_plane().ordinal());
//                    gcodeUnitMode.getSelectionModel().select(TinygDriver.getInstance().m.getGcode_units().ordinal());
//                    gcodeCoordSystem.getSelectionModel().select(TinygDriver.getInstance().m.getCoordinateSystem().ordinal());
                    gcodePathControl.getSelectionModel().select(TinygDriver.getInstance().m.getGcode_distance_mode().ordinal());
                    gcodeDistanceMode.getSelectionModel().select(TinygDriver.getInstance().m.getGcode_distance_mode().ordinal());

                    if (m.getCoordinateSystem() != null) {
                        srCoord.setText(TinygDriver.getInstance().m.getCoordinateSystem().toString());
                    }
                } catch (Exception ex) {
                    System.out.println("[!] Exception in updateGuiMachineSettings");
                    System.out.println(ex.getMessage());
                }
            }
        });

    }

    @Override
    public synchronized void update(Observable o, Object arg) {
//
//        //We process status code messages here first.
//        if (arg.getClass().getCanonicalName().equals("tgfx.system.StatusCode")) {
//            //We got an error condition.. lets route it to where it goes!
//            StatusCode statuscode = (StatusCode) arg;
//            console.appendText("[->] TinyG Response: " + statuscode.getStatusType() + ":" + statuscode.getMessage() + "\n");
//        } else {
//            final String[] UPDATE_MESSAGE = (String[]) arg;
//            final String ROUTING_KEY = UPDATE_MESSAGE[0];
//            final String KEY_ARGUMENT = UPDATE_MESSAGE[1];
//
////        We have to run the updates likes this.
////        https://forums.oracle.com/forums/thread.jspa?threadID=2298778&start=0 for more information
//            Platform.runLater(new Runnable() {
//                public void run() {
//                    // we are now back in the EventThread and can update the GUI
////                    if (ROUTING_KEY.startsWith("[!]")) {
////                        String line = ROUTING_KEY.split("#")[1];
////                        String msg = ROUTING_KEY.split("#")[0];
////
////                        Main.logger.error("Invalid Routing Key: \n\tMessage: " + msg + "\n\tLine: " + line);
////                    } else 
//
//                    if (ROUTING_KEY.equals("STATUS_REPORT")) {
//                        updateGuiStatusReport(ROUTING_KEY);
//                        //updateStatusReport(ROUTING_KEY);
//                    } else if (ROUTING_KEY.equals("CMD_GET_AXIS_SETTINGS")) {
//                        updateGuiAxisSettings(KEY_ARGUMENT);
//                    } else if (ROUTING_KEY.equals("CMD_GET_MACHINE_SETTINGS")) {
//                        //updateGuiMachineSettings(ROUTING_KEY);
//                    } else if (ROUTING_KEY.contains("CMD_GET_MOTOR_SETTINGS")) {
//                        updateGuiMotorSettings(KEY_ARGUMENT);
//                    } else if (ROUTING_KEY.equals("NETWORK_MESSAGE")) {  //unused
//                        //updateExternal();
//                    } else if (ROUTING_KEY.equals("MACHINE_UPDATE")) {
//                        //updateGuiMachineSettings(ROUTING_KEY);
//                    } else {
//                        System.out.println("[!]Invalid Routing Key: " + ROUTING_KEY);
//                    }
//                }
//            });
//        }
//    }
//
//    private void updateGuiMotorSettings() {
//        //No motor was provided... Update them all.
//        updateGuiMotorSettings(null);
//    }
//
//    private void updateGuiMotorSettings(final String arg) {
//        //Update the GUI for config settings
//        Platform.runLater(new Runnable() {
//            String MOTOR_ARGUMENT = arg;
//
//            @Override
//            public void run() {
//                try {
//                    if (MOTOR_ARGUMENT == null) {
//                        //Update ALL motor's gui settings
//                        for (Motor m : tg.m.getMotors()) {
//                            _updateGuiMotorSettings(String.valueOf(m.getId_number()));
//                        }
//                    } else {
//                        //Update only ONE motor's gui settings
//                        _updateGuiMotorSettings(MOTOR_ARGUMENT);
//                    }
//                } catch (Exception ex) {
//                    System.out.println("[!]Exception in updateGuiMotorSettings...");
//                    System.out.println(ex.getMessage());
//                }
//            }
//        });
    }

    private void _updateGuiMotorSettings(String motor) {

        switch (TinygDriver.getInstance().m.getMotorByNumber(Integer.valueOf(motor)).getId_number()) {
            case 1:
                motor1ConfigMapAxis.getSelectionModel().select((tg.m.getMotorByNumber(1).getMapToAxis()));
                motor1ConfigMicroSteps.getSelectionModel().select(tg.m.getMotorByNumber(1).getMicrosteps());
                motor1ConfigPolarity.getSelectionModel().select(tg.m.getMotorByNumber(1).isPolarityInt());
                motor1ConfigPowerMode.getSelectionModel().select(tg.m.getMotorByNumber(1).isPower_managementInt());
                motor1ConfigStepAngle.setText(String.valueOf(tg.m.getMotorByNumber(1).getStep_angle()));
                motor1ConfigTravelPerRev.setText(String.valueOf(tg.m.getMotorByNumber(1).getTravel_per_revolution()));
                break;
            case 2:
                motor2ConfigMapAxis.getSelectionModel().select(tg.m.getMotorByNumber(2).getMapToAxis());
                motor2ConfigMicroSteps.getSelectionModel().select(tg.m.getMotorByNumber(2).getMicrosteps());
                motor2ConfigPolarity.getSelectionModel().select(tg.m.getMotorByNumber(2).isPolarityInt());
                motor2ConfigPowerMode.getSelectionModel().select(tg.m.getMotorByNumber(2).isPower_managementInt());
                motor2ConfigStepAngle.setText(String.valueOf(tg.m.getMotorByNumber(2).getStep_angle()));
                motor2ConfigTravelPerRev.setText(String.valueOf(tg.m.getMotorByNumber(2).getTravel_per_revolution()));
                break;
            case 3:
                motor3ConfigMapAxis.getSelectionModel().select(tg.m.getMotorByNumber(3).getMapToAxis());
                motor3ConfigMicroSteps.getSelectionModel().select(tg.m.getMotorByNumber(3).getMicrosteps());
                motor3ConfigPolarity.getSelectionModel().select(tg.m.getMotorByNumber(3).isPolarityInt());
                motor3ConfigPowerMode.getSelectionModel().select(tg.m.getMotorByNumber(3).isPower_managementInt());
                motor3ConfigStepAngle.setText(String.valueOf(tg.m.getMotorByNumber(3).getStep_angle()));
                motor3ConfigTravelPerRev.setText(String.valueOf(tg.m.getMotorByNumber(3).getTravel_per_revolution()));
                break;
            case 4:
                motor4ConfigMapAxis.getSelectionModel().select(tg.m.getMotorByNumber(4).getMapToAxis());
                motor4ConfigMicroSteps.getSelectionModel().select(tg.m.getMotorByNumber(4).getMicrosteps());
                motor4ConfigPolarity.getSelectionModel().select(tg.m.getMotorByNumber(4).isPolarityInt());
                motor4ConfigPowerMode.getSelectionModel().select(tg.m.getMotorByNumber(4).isPower_managementInt());
                motor4ConfigStepAngle.setText(String.valueOf(tg.m.getMotorByNumber(4).getStep_angle()));
                motor4ConfigTravelPerRev.setText(String.valueOf(tg.m.getMotorByNumber(4).getTravel_per_revolution()));
                break;
        }
    }

    private void _updateGuiAxisSettings(String axname) {
        Axis ax = TinygDriver.getInstance().m.getAxisByName(axname);
        _updateGuiAxisSettings(ax);
    }

    private void _updateGuiAxisSettings(Axis ax) {
        switch (ax.getAxis_name().toLowerCase()) {
            case "a":
                axisAmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                axisAmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                axisAmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                axisAjunctionDeviation.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getJunction_devation())));
                axisAmaxVelocity.setText(String.valueOf(ax.getVelocity_maximum()));
                axisAmaxJerk.setText(new DecimalFormat("#.#####").format(ax.getJerk_maximum()));
                axisAradius.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getRadius())));
                axisAsearchVelocity.setText(String.valueOf(ax.getSearch_velocity()));
                axisAzeroBackoff.setText(String.valueOf(ax.getZero_backoff()));
                //Rotational Do not have these.
//                axisAsearchVelocity.setDisable(true);
//                axisAlatchVelocity.setDisable(true);
//                axisAlatchBackoff.setDisable(true);
                axisAswitchModeMax.getSelectionModel().select(ax.getMaxSwitch_mode().ordinal());
                axisAswitchModeMin.getSelectionModel().select(ax.getMinSwitch_mode().ordinal());

                axisAmaxVelocity.setText(String.valueOf(ax.getVelocity_maximum()));
                axisAlatchBackoff.setText(String.valueOf(ax.getLatch_backoff()));
                axisAlatchVelocity.setText(String.valueOf(ax.getLatch_velocity()));

//                axisAswitchModeMax.setDisable(true);
//                axisAswitchModeMin.setDisable(true);
//                axisAzeroBackoff.setDisable(true);

                break;
            case "b":
                axisBmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                axisBmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                axisBmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                axisBjunctionDeviation.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getJunction_devation())));
                axisBmaxVelocity.setText(String.valueOf(ax.getVelocity_maximum()));
                axisBmaxJerk.setText(new DecimalFormat("#.#####").format(ax.getJerk_maximum()));
                axisBradius.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getRadius())));
                //Rotational Do not have these.
                axisBsearchVelocity.setDisable(true);
                axisBlatchVelocity.setDisable(true);
                axisBlatchBackoff.setDisable(true);
                axisBswitchModeMax.setDisable(true);
                axisBswitchModeMin.setDisable(true);
                axisBzeroBackoff.setDisable(true);

                break;
            case "c":
                axisCmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                axisCmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                axisCmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                axisCjunctionDeviation.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getJunction_devation())));
                axisCmaxVelocity.setText(String.valueOf(ax.getVelocity_maximum()));
                axisCmaxJerk.setText(new DecimalFormat("#.#####").format(ax.getJerk_maximum()));
                axisCradius.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getRadius())));

                //Rotational Do not have these.
                axisCsearchVelocity.setDisable(true);
                axisClatchVelocity.setDisable(true);
                axisClatchBackoff.setDisable(true);
                axisCswitchModeMax.setDisable(true);
                axisCswitchModeMin.setDisable(true);
                axisCzeroBackoff.setDisable(true);
                break;
            case "x":
//                axisXradius.setText("NA");
//                axisXradius.setStyle("-fx-text-fill: red");
//                axisXradius.setDisable(true);
//                axisXradius.setEditable(false);
                axisXmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                axisXmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                axisXmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                axisXjunctionDeviation.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getJunction_devation())));
                axisXsearchVelocity.setText(String.valueOf(ax.getSearch_velocity()));
                axisXzeroBackoff.setText(String.valueOf(ax.getZero_backoff()));
                axisXswitchModeMax.getSelectionModel().select(ax.getMaxSwitch_mode().ordinal());
                axisXswitchModeMin.getSelectionModel().select(ax.getMinSwitch_mode().ordinal());
                axisXmaxJerk.setText(new DecimalFormat("#.#####").format(ax.getJerk_maximum()));

                axisXmaxVelocity.setText(String.valueOf(ax.getVelocity_maximum()));
                axisXlatchBackoff.setText(String.valueOf(ax.getLatch_backoff()));
                axisXlatchVelocity.setText(String.valueOf(ax.getLatch_velocity()));
                break;
            case "y":
//                axisYradius.setText("NA");
//                axisYradius.setStyle("-fx-text-fill: red");
//                axisYradius.setDisable(true);
//                axisYradius.setEditable(false);
                axisYmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                axisYmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                axisYmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                axisYjunctionDeviation.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getJunction_devation())));
                axisYsearchVelocity.setText(String.valueOf(ax.getSearch_velocity()));
                axisYzeroBackoff.setText(String.valueOf(ax.getZero_backoff()));
                axisYswitchModeMax.getSelectionModel().select(ax.getMaxSwitch_mode().ordinal());
                axisYswitchModeMin.getSelectionModel().select(ax.getMinSwitch_mode().ordinal());
                axisYmaxVelocity.setText(String.valueOf(ax.getVelocity_maximum()));
                axisYmaxJerk.setText(new DecimalFormat("#.#####").format(ax.getJerk_maximum()));
//                                axisYmaxJerk.setText(String.valueOf(ax.getJerk_maximum()));
                // axisYradius.setText(String.valueOf(ax.getRadius()));
                axisYlatchVelocity.setText(String.valueOf(ax.getLatch_velocity()));
                axisYlatchBackoff.setText(String.valueOf(ax.getLatch_backoff()));
                break;
            case "z":
//                axisZradius.setText("NA");
//                axisZradius.setStyle("-fx-text-fill: red");
//                axisZradius.setDisable(true);
//                axisZradius.setEditable(false);
                axisZmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                axisZmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                axisZmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                axisZjunctionDeviation.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getJunction_devation())));
                axisZsearchVelocity.setText(String.valueOf(ax.getSearch_velocity()));
                axisZzeroBackoff.setText(String.valueOf(ax.getZero_backoff()));
                axisZswitchModeMin.getSelectionModel().select(ax.getMaxSwitch_mode().ordinal());
                axisZswitchModeMax.getSelectionModel().select(ax.getMinSwitch_mode().ordinal());
                axisZmaxVelocity.setText(String.valueOf(ax.getVelocity_maximum()));
                axisZmaxJerk.setText(new DecimalFormat("#.#####").format(ax.getJerk_maximum()));
//                                axisZmaxJerk.setText(String.valueOf(ax.getJerk_maximum()));
                //axisZradius.setText(String.valueOf(ax.getRadius()));
                axisZlatchVelocity.setText(String.valueOf(ax.getLatch_velocity()));
                axisZlatchBackoff.setText(String.valueOf(ax.getLatch_backoff()));
                break;
        }
    }

    private void updateGuiAxisSettings(Axis ax) {
        updateGuiAxisSettings(ax);
    }

    private void updateGuiAxisSettings(String axname) {
        //Update the GUI for Axis Config Settings
        final String AXIS_NAME = axname;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //We are now back in the EventThread and can update the GUI for the CMD SETTINGS
                //Right now this is how I am doing this.  However I think there can be a more optimized way
                //Perhaps by passing a routing message as to which motor was updated then not all have to be updated
                //every time one is.
                try {
                    if (AXIS_NAME == null) {
                        //Axis was not provied as a sting argument.. so we update all of them
                        for (Axis ax : tg.m.getAllAxis()) {
                            _updateGuiAxisSettings(ax);
                        }
                    } else {
                        //We were provided with a specific axis to update.  Update it.
                        _updateGuiAxisSettings(AXIS_NAME);
                    }
                } catch (Exception ex) {
                    System.out.println("[!]EXCEPTION in updateGuiAxisSettings");
                    System.out.println("LINE: ");
                    System.out.println(ex.getMessage());
                }

            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

//        xtgPA.bindBidirectional("firmwareBuild", srBuild.textProperty());
//        tgPA.bindBidirectional("firmwareBuild", srBuild.textProperty());


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


        /*
         * WE CREATE OUR BINDINGS HERE TO BIND OUR INTERNAL GUI (JAVAFX 2) MODEL
         * TO OUR TINYG INTERNAL MODEL... READ EMBEDDED SYSTEM MODEL
         */
        
        srMomo.textProperty().bind(tg.m.getMotionMode());
        srVer.textProperty().bind(tg.m.firmwareVersion);
        srVelo.textProperty().bindBidirectional(tg.m.velocity, sc);
        srBuild.textProperty().bindBidirectional(tg.m.firmwareBuild,sc);
        srState.textProperty().bind(tg.m.m_state);
        srCoord.textProperty().bind(tg.m.getCoordinateSystem());
        srUnits.textProperty().bind(tg.m.getGcodeUnitMode());
        xAxisVal.textProperty().bindBidirectional(TinygDriver.getInstance().m.getAxisByName("x").getWork_position(),sc);
        yAxisVal.textProperty().bindBidirectional(TinygDriver.getInstance().m.getAxisByName("y").getWork_position(),sc);
        zAxisVal.textProperty().bindBidirectional(TinygDriver.getInstance().m.getAxisByName("z").getWork_position(),sc);
        aAxisVal.textProperty().bindBidirectional(TinygDriver.getInstance().m.getAxisByName("a").getWork_position(),sc);
        
//        srMomo.textProperty().bindBidirectional(tg.m.);
        
        

        BasicConfigurator.configure();
        SocketMonitor sm;

//        WebEngine webEngine = html.getEngine();
//        webEngine.load("http://www.synthetos.com/wiki/index.php?title=Projects:TinyG");


        logger.info("[+]tgFX is starting....");
        //Make our canvas as big as the hbox that holds it is.
//        drawingCanvas.setWidth(canvasHolder.getWidth());
        // drawingCanvas.setHeight(canvasHolder.getMaxHeight());


        //drawingCanvas.setWidth(727);
        //drawingCanvas.setHeight(515);

//        double HEIGHT = drawingCanvas.getHeight();
//        double WIDTH = drawingCanvas.getWidth();

        //gp = drawingCanvas.getGraphicsContext2D();
//        gp.setFill(Color.RED);
//        gp.getCanvas().setWidth(100);
//        gp.getCanvas().toFront();
//        gp.setLineWidth(2);
//        gp.setStroke(Color.AZURE);
//        gp.strokeLine(0,0,100,100);
//        gp.setStroke(Color.PINK);
//        gp.strokeLine(100,100,0,100);
//        
//        gp.setStroke(Color.CYAN);
//        gp.strokeLine(0,HEIGHT, WIDTH,0);
//        gp.setStroke(Color.BLANCHEDALMOND);
//        gp.strokeLine(gp.getCanvas().getWidth()/2,gp.getCanvas().getHeight()/2,0,0);
////
//        gp.setStroke(Color.ORANGE);
//        
//        gp.strokeLine(0, 0, 10, gp.getCanvas().getHeight()-5);
//        gp.setStroke(Color.BLUE);
//        gp.strokeLine(0,0,gp.getCanvas().getHeight()-5,10);
//        
//        
//        gp.setFill(Color.BLUE);
//        
//        gp.rect(10, 5, 10, 5);

        //Gcode Mapping
        data = FXCollections.observableArrayList();
        gcodeCol.setCellValueFactory(new PropertyValueFactory<GcodeLine, String>("codeLine"));
        GcodeLine n = new GcodeLine("Click open to load..", 0);
        gcodeView.getItems().setAll(data);
        data.add(n);
        gcodeView.setItems(data);

        Thread serialWriterThread = new Thread(tg.serialWriter);
        serialWriterThread.setName("SerialWriter");
        serialWriterThread.setDaemon(true);
        serialWriterThread.start();

        Thread threadResponseParser = new Thread(tg.resParse);
        threadResponseParser.setDaemon(true);
        threadResponseParser.setName("ResponseParser");
        threadResponseParser.start();
//
//        Thread remoteListener = new Thread(initRemoteServer("8888"));
//        remoteListener.setName("Remote Listener Thread");
//        remoteListener.start();

        tg.resParse.addObserver(this);
        this.reScanSerial();//Populate our serial ports


    }
}
