/*
 * Copyright Synthetos LLC
 * Rileyporter@gmail.com
 * www.synthetos.com
 * 
 */
package tgfx;

import argo.jdom.JdomParser;
import java.io.*;
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
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import tgfx.external.SocketMonitor;
import tgfx.render.Draw2d;
import tgfx.system.Axis;
import tgfx.system.Machine;
import tgfx.system.Motor;
import org.apache.log4j.Logger;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;

import org.apache.log4j.BasicConfigurator;
import org.omg.PortableInterceptor.USER_EXCEPTION;

/**
 *
 * @author ril3y
 */
public class Main implements Initializable, Observer {

    static final Logger logger = Logger.getLogger(Main.class);
    //private static final String CMD_GET_stateUS_REPORT = "{\"sr\":\"\"}\n";
    //public Machine m = new Machine();
    private JdomParser JDOM = new JdomParser(); //JSON Object Parser1
    private TinygDriver tg = TinygDriver.getInstance();
    //private SerialDriver ser = SerialDriver.getInstance();
    /**
     * FXML UI Components
     */
    @FXML
    private TabPane motorTabPane, axisTabPane;
    @FXML
    private Pane previewPane;
    @FXML
    private Canvas drawingCanvas;
    GraphicsContext gp;
    @FXML
    private Button Con, Run, Connect, gcodeZero, btnClearScreen, btnRemoteListener, pauseResume;
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
            axisAzeroOffset, axisBzeroOffset, axisCzeroOffset, axisXzeroOffset, axisYzeroOffset, axisZzeroOffset,
            axisAmaxVelocity, axisBmaxVelocity, axisCmaxVelocity, axisXmaxVelocity, axisYmaxVelocity, axisZmaxVelocity,
            axisAmaxJerk, axisBmaxJerk, axisCmaxJerk, axisXmaxJerk, axisYmaxJerk, axisZmaxJerk,
            axisAradius, axisBradius, axisCradius, axisXradius, axisYradius, axisZradius,
            axisAlatchVelocity, axisBlatchVelocity, axisClatchVelocity, axisXlatchVelocity, axisYlatchVelocity, axisZlatchVelocity, externalConnections,
            materialThickness, gcodeLoaded;
    @FXML
    ChoiceBox motor1ConfigMapAxis, motor2ConfigMapAxis, motor3ConfigMapAxis, motor4ConfigMapAxis,
            motor1ConfigMicroSteps, motor2ConfigMicroSteps, motor3ConfigMicroSteps, motor4ConfigMicroSteps,
            motor1ConfigPolarity, motor2ConfigPolarity, motor3ConfigPolarity, motor4ConfigPolarity,
            motor1ConfigPowerMode, motor2ConfigPowerMode, motor3ConfigPowerMode, motor4ConfigPowerMode,
            axisAmode, axisBmode, axisCmode, axisXmode, axisYmode, axisZmode,
            axisAswitchMode, axisBswitchMode, axisCswitchMode, axisXswitchMode, axisYswitchMode, axisZswitchMode;
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
    private void handleOpenFile(ActionEvent event) {



        Platform.runLater(new Runnable() {

            public void run() {
                logger.debug("handleOpenFile");

                try {
                    console.appendText("[+]Loading a gcode file.....\n");
                    FileChooser fc = new FileChooser();
                    fc.setTitle("Open GCode File");
                    fc.setInitialDirectory(new File(System.getenv("HOME")));
//                    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gcode Files","*.gc"));
                    File f = fc.showOpenDialog(null);
                    FileInputStream fstream = new FileInputStream(f);
                    DataInputStream in = new DataInputStream((fstream));
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String strLine;

                    gcodesList.clear();
                    //Clear the list if there was a previous file loaded

                    while ((strLine = br.readLine()) != null) {

                        gcodesList.appendText(strLine + "\n");
//
//                        System.out.println(strLine);
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
        console.appendText("[!]Canceling File Sending Task...");
        tg.setCANCELLED(true);
    }

    @FXML
    private void handlePauseResumeAct(ActionEvent evt) throws Exception {
        if ("Pause".equals(pauseResume.getText())) {
            pauseResume.setText("Resume");
            tg.setPAUSED(true);

        } else {
            pauseResume.setText("Pause");
            tg.setPAUSED(false);
        }
    }

    @FXML
    void handleQueryMotor(ActionEvent evt) {
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

            //TODO:  Breakout Individual response messages vs having to call queryHardwareAllAxisSettings
            //something like if {"1po":1} then parse and update only the polarity setting
//            Thread.sleep(TinygDriver.CONFIG_DELAY);
//            tg.queryHardwareAllAxisSettings();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println("ERROR IN HANDLEAPPLYAXISSETTINGS");
        }

    }

    @FXML
    private void handleQueryAxisSettings(ActionEvent evt) throws Exception {
        String _axisSelected = axisTabPane.getSelectionModel().getSelectedItem().getText().toLowerCase();
        console.appendText("[+]Querying Axis: " + _axisSelected + "\n");
//        tg.queryHardwareAllAxisSettings();
        try {
            tg.queryHardwareSingleAxisSettings(_axisSelected);
            tg.write(TinygDriver.CMD_QUERY_OK_PROMPT);

        } catch (Exception ex) {
            System.out.println("[!]Error Querying Axis: " + _axisSelected);
        }
    }

    @FXML
    private void handleAxisEnter(final InputEvent event) throws Exception {
        //private void handleEnter(ActionEvent event) throws Exception {
        final KeyEvent keyEvent = (KeyEvent) event;
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            Axis _axis = Machine.getInstance().getAxisByName(axisTabPane.getSelectionModel().getSelectedItem().getText().toLowerCase());
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
                TinygDriver.getInstance().write(TinygDriver.CMD_APPLY_DEFAULT_SETTINGS);
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
        //

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
                tg.write(TinygDriver.CMD_APPLY_ZERO_ALL_AXIS);
                //G92 does not invoke a status report... So we need to generate one to have
                //Our GUI update the coordinates to zero
                tg.write(TinygDriver.CMD_QUERY_STATUS_REPORT);
                //We need to set these to 0 so we do not draw a line from the last place we were to 0,0
                xPrevious = 0;
                yPrevious = 0;
            } catch (Exception ex) {
            }
        }
    }

    @FXML
    private void handleRunFile(ActionEvent evt) {
        Task fileSend = fileSenderTask();
        Thread fsThread = new Thread(fileSend);
        fsThread.setName("FileSender");
        fsThread.start();

    }

    public Task fileSenderTask() {
        return new Task() {

            @Override
            protected Object call() throws Exception {
                //ObservableList<TextField> gcodeProgramList = gcodesList.getText();
                //gcodeProgramList = gcodesList.getItems();
                String[] gcodeProgramList = gcodesList.getText().split("\n");
                String line;
                tg.setCANCELLED(false);  //Clear this flag if was canceled in a previous job
                //TinygDriver.getInstance().setClearToSend(true);


                for (String l : gcodeProgramList) {
                    if (l.startsWith("(") || l.equals("")) {
                        continue;
                    }
//                    Thread.sleep(20);
                    line = "{\"gc\":\"" + l + "\"}" + "\n";
                    if (TinygDriver.getInstance().isPAUSED()) {
                        while (TinygDriver.getInstance().isPAUSED()) {
                            Thread.sleep(50);
                        }
                    }
                    tg.write(line);
                }

                logger.debug(tg.getBustedBufferCount() + " times buffer below threshold");

                return true;
            }
        };
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

            tg.write(TinygDriver.CMD_APPLY_DISABLE_HASHCODE);
            tg.write(TinygDriver.CMD_APPLY_DISABLE_LOCAL_ECHO);
            tg.getAllMotorSettings();
            tg.getAllAxisSettings();
            tg.write(TinygDriver.CMD_QUERY_STATUS_REPORT);  //If TinyG current positions are other than zero
            tg.write(TinygDriver.CMD_QUERY_MACHINE_SETTINGS);  //On command to rule them all.

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
                onConnectActions();
            }
        } else {
            tg.write(TinygDriver.CMD_QUERY_OK_PROMPT);
            tg.disconnect();
            if (!tg.isConnected()) {
                console.appendText("[+]Disconnected from " + tg.getPortName() + " Serial Port Successfully.\n");
                Connect.setText("Connect");
                onDisconnectActions();
            }

        }
    }

    public void onDisconnectActions() {
        srBuild.setText("?");
        srVer.setText("?");
        srMomo.setText("?");
        srVelo.setText("?");
        srState.setText("?");


    }

    @FXML
    private void handleClearScreen(ActionEvent evt) {
        console.appendText("[+]Clearning Screen...\n");
        gp.clearRect(0, 0, gp.getCanvas().getWidth(), gp.getCanvas().getHeight());
//        canvsGroup.getChildren().clear();
    }

    private void handleTilda() {
        //                ==============HIDE CONSOLE CODE==============
        System.out.println("TILDA");
        if (topvbox.getChildren().contains(bottom)) {
            topvbox.getChildren().remove(bottom);

        } else {
            topvbox.getChildren().add(topvbox.getChildren().size() - 1, bottom);
        }
    }

    @FXML
    private void handleKeyInput(final InputEvent event) {
        if (event instanceof KeyEvent) {
            final KeyEvent keyEvent = (KeyEvent) event;
            if (keyEvent.getCode() == KeyCode.BACK_QUOTE) {

                handleTilda();

            } else if (keyEvent.getCode() == KeyCode.F10) {
                if (!tg.isConnected()) {
                    String msg = new String("[!]Getting Status Report Aborted... Serial Port Not Connected...");
                    console.appendText(msg);
                    return;
                } else {
                    String msg = new String("F10 Key Pressed - Getting Status Report\n");
                    console.appendText(msg);
                    System.out.println(msg);
                    try {
                        tg.requestStatusUpdate();
                    } catch (Exception ex) {
                        System.out.println("Error in getting status report");
                    }
                }
            } else if (keyEvent.getCode() == KeyCode.F5) {
                if (!tg.isConnected()) {
                    String msg = new String("[!]Getting Settings Aborted... Serial Port Not Connected...");
                    console.appendText(msg);
                    return;
                }
                String msg = new String("F5 Key Pressed - Getting Machine Settings\n");
                console.appendText(msg);
                System.out.println(msg);
                try {
//                    tg.requestStatusUpdate();
//                    tg.getMachineSettings();
                    tg.getMotorSettings(1);
                    tg.getMotorSettings(2);
                    tg.getMotorSettings(3);
                    tg.getMotorSettings(4);

                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }


            } else if (keyEvent.getCode() == KeyCode.F12) {
                System.out.println("Writing DEBUG file");
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter("debugTest.txt"));
                    DataOutputStream dos;
                    dos = new DataOutputStream(new FileOutputStream("debug.txt", true));
                    out.close();

                } catch (Exception e) {
                    System.out.println("Exception ");
                }
            } else if (keyEvent.getCode() == KeyCode.F1) {
                Draw2d.incrementSetStrokeWeight();
//                reDrawPreview();
                console.appendText("[+]Increasing Stroke Width: " + String.valueOf(Draw2d.getStrokeWeight()) + "\n");
            } else if (keyEvent.getCode() == KeyCode.F2) {
                Draw2d.decrementSetStrokeWeight();
//                reDrawPreview();
                console.appendText("[+]Decreasing Stroke Width: " + String.valueOf(Draw2d.getStrokeWeight()) + "\n");
            }
        }
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
                tg.write(TinygDriver.CMD_QUERY_OK_PROMPT);
            } else {
                if (command.contains("$")) {
                    if (command.contains("defaults")) {
                        //If someone resets TinyG's defaults we need to completely refresh the system.
                        console.appendText("[#]Restoring TinyG Defaults... Please Wait.....\n");
                        Thread.sleep(50); // Let the console update before we start to pause the UI thread.
                        tg.write(TinygDriver.CMD_APPLY_DEFAULT_SETTINGS);
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
                    tg.write(TinygDriver.CMD_QUERY_MACHINE_SETTINGS);
                    tg.getAllAxisSettings();
                    tg.getAllMotorSettings();


                    input.clear();
                    input.setPromptText(PROMPT);
                } else {
                    //Catch if a unit mode command was sent... Update the status bar if it was..
                    if (command.toLowerCase().contains("g20") || command.toLowerCase().contains("g21")) {
                        tg.write(command);
                        console.appendText(command);
                        input.clear();
                        input.setPromptText(PROMPT);
                        onConnectActions(); //Do this to get updated values in INCHES or MM
                        srUnits.setText(tg.getInstance().m.getUnitMode().toString());
                        console.appendText("[+]Unit Mode Change Detected... Units set to: " + tg.getInstance().m.getUnitMode().toString() + "\n");
                    } else if (command.toLowerCase().contains("g54") || command.toLowerCase().contains("g55") || command.toLowerCase().contains("g56")
                            || command.toLowerCase().contains("g57") || command.toLowerCase().contains("g58") || command.toLowerCase().contains("g59")) {
                        tg.write(command);
                        console.appendText(command);
                        input.clear();
                        input.setPromptText(PROMPT);
                        Thread.sleep(TinygDriver.CONFIG_DELAY);
                        tg.write(TinygDriver.CMD_QUERY_STATUS_REPORT); //the coord is a field that is enabled by default in status reports.
                        srUnits.setText(tg.getInstance().m.getCoordinateSystem().toString());
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

    public void drawLine(Machine.motion_modes moveType, float vel) {

        //Code to make mm's look the same size as inches
        double unitMagnication = 1;
//        if (tg.m.getUnitMode() == Machine.unit_modes.MM) {
//            unitMagnication = 50.8;
//        } else {
//            unitMagnication = 2;
//        }
        double newX = unitMagnication * (Double.valueOf(tg.m.getAxisByName("X").getWork_position()));// + magnification;
        double newY = unitMagnication * (Double.valueOf(tg.m.getAxisByName("Y").getWork_position()));// + magnification;

        Line l = new Line(xPrevious, yPrevious, newX, newY);
        l.setStroke(Draw2d.getLineColorFromVelocity(vel));

        if (moveType == Machine.motion_modes.traverse) {
            //G0 Move
            l.setStrokeWidth(Draw2d.getStrokeWeight() / 2);
            l.setStroke(Draw2d.TRAVERSE);
        }


        //CODE TO ONLY DRAW CUTTING MOVEMENTS
//        if (tg.m.getAxisByName("Z").getWork_position() > 0) {
//            l = null;
//        } else {
//            l.setStrokeWidth(Draw2d.getStrokeWeight());
//        }

//        l.setStrokeWidth(Draw2d.getStrokeWeight());



        if (xPrevious == -1 && yPrevious == -1) {
            //This is the initial move.
            //Move to the center of the canvas
            gp.moveTo(0, gp.getCanvas().getHeight());  //(0,0) = Bottom left of the canvas
            xPrevious = gp.getCanvas().getWidth()/2;
            yPrevious = gp.getCanvas().getHeight()/2;
        }

//        if (l != null) {
        gp.setLineWidth(1);
        gp.setStroke(Color.RED);
        gp.strokeLine(xPrevious, yPrevious, newX+gp.getCanvas().getWidth()/2, newY+gp.getCanvas().getHeight()/2);  //To correct our zero of being at the bottom left of the screen



        //Record our last points.
        xPrevious = newX+gp.getCanvas().getWidth()/2;
        yPrevious = newY+gp.getCanvas().getHeight()/2;
//        }
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
                        srBuild.setText(String.valueOf(tg.m.getFirmware_build()));
                        srVer.setText(String.valueOf(tg.m.getFirmware_version()));
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
                        srState.setText(tg.m.getMachineState().toString().toUpperCase());
                        srUnits.setText(tg.m.getUnitMode().toString());

                        srMomo.setText(tg.m.getMotionMode().toString().replace("_", " ").toUpperCase());
                        //Set the motion mode

                        //Parse the veloicity 
                        vel = tg.m.getVelocity();
                        srVelo.setText(String.valueOf(vel));

//                        drawLine(tg.m.getMotionMode(), vel);


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

            @Override
            public void run() {
                logger.info("updateGuiStatusReport Ran");
                float vel;
                //We are now back in the EventThread and can update the GUI
                try {
                    Machine m = TinygDriver.getInstance().m;
                    xAxisVal.setText(String.valueOf(new DecimalFormat("0.000").format(m.getAxisByName("X").getWork_position()))); //json.getNode("sr").getNode("posx").getText());
                    yAxisVal.setText(String.valueOf(new DecimalFormat("0.000").format(m.getAxisByName("Y").getWork_position())));
                    zAxisVal.setText(String.valueOf(new DecimalFormat("0.000").format(m.getAxisByName("Z").getWork_position())));
                    //zAxisVal2.setText(String.valueOf(m.getAxisByName("Z").getWork_position())); //Dedicated Z depth preview label
                    aAxisVal.setText(String.valueOf(new DecimalFormat("0.000").format(m.getAxisByName("A").getWork_position())));

//                    axisAjunctionDeviation.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getJunction_devation())));

                    //Update State... running stop homing etc.
                    srState.setText(m.getMachineState().toString().toUpperCase());
                    srUnits.setText(m.getUnitMode().toString());
                    srCoord.setText(m.getCoordinateSystem().toString());
                    srMomo.setText(m.getMotionMode().toString().replace("_", " ").toUpperCase());


                    //Parse the veloicity 
                    vel = m.getVelocity();
                    srVelo.setText(String.valueOf(vel));

                    drawLine(TinygDriver.getInstance().m.getMotionMode(), vel);
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
                    srBuild.setText(String.valueOf(m.getFirmware_build()));
                    srVer.setText(String.valueOf(m.getFirmware_version()));
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
        final String ROUTING_KEY = (String) arg;
//        We have to run the updates likes this.
//        https://forums.oracle.com/forums/thread.jspa?threadID=2298778&start=0 for more information
        Platform.runLater(new Runnable() {

            public void run() {
                // we are now back in the EventThread and can update the GUI
                if (ROUTING_KEY.equals("STATUS_REPORT")) {
                    updateGuiStatusReport(ROUTING_KEY);
                    //updateStatusReport(ROUTING_KEY);
                } else if (ROUTING_KEY.equals("CMD_GET_AXIS_SETTINGS")) {
                    updateGuiAxisSettings();
                } else if (ROUTING_KEY.equals("CMD_GET_MACHINE_SETTINGS")) {
                    updateGuiMachineSettings(ROUTING_KEY);
                } else if (ROUTING_KEY.contains("CMD_GET_MOTOR_SETTINGS")) {
                    updateGuiMotorSettings();
                } else if (ROUTING_KEY.equals("NETWORK_MESSAGE")) {  //unused
                    //updateExternal();
                } else if (ROUTING_KEY.equals("MACHINE_UPDATE")) {
                    updateGuiMachineSettings(ROUTING_KEY);
                } else {
                    System.out.println("[!]Invalid Routing Key: " + ROUTING_KEY);
                }
            }
        });
    }

    private void updateGuiMotorSettings() {
        //Update the GUI for config settings
        Platform.runLater(new Runnable() {

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
                    System.out.println("[!]Exception in updateGuiMotorSettings...");
                    System.out.println(ex.getMessage());
                }

            }
        });
    }

//    
//    @Override
//    public synchronized void update(Observable o, Object arg) {
//        final String ROUTING_KEY = (String) arg;
//
//        //We have to run the updates likes this.
//        //https://forums.oracle.com/forums/thread.jspa?threadID=2298778&start=0 for more information
//        Platform.runLater(new Runnable() {
//
//            public void run() {
//                // we are now back in the EventThread and can update the GUI
//                if (ROUTING_KEY.equals("PLAIN")) {
//                    console.appendText((String) ROUTING_KEY + "\n");
//
//                } else if (ROUTING_KEY.equals("BUILD_UPDATE")) {
//                    updateGuiState(ROUTING_KEY);
//                } else if (ROUTING_KEY.equals("STATUS_REPORT")) {
////                    console.setText((String) MSG[1] + "\n");
//                    updateGuiState(ROUTING_KEY);
//
//                } else if (ROUTING_KEY.contains("ERROR")) {
//                    console.appendText(ROUTING_KEY);
//                } else if (ROUTING_KEY.equals("CMD_GET_MACHINE_SETTINGS")) {
////                    System.out.println("UPDATE: MACHINE SETTINGS");
//                    updateGUIConfigState();
//                } else {
//                    console.appendText(ROUTING_KEY);
//                    System.out.println(ROUTING_KEY);
//                }
//
//            }
//        });
//    }
    private void updateGuiAxisSettings() {
        //Update the GUI for config settings
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                //We are now back in the EventThread and can update the GUI for the CMD SETTINGS
                //Right now this is how I am doing this.  However I think there can be a more optimized way
                //Perhaps by passing a routing message as to which motor was updated then not all have to be updated
                //every time one is.
                try {
                    for (Axis ax : tg.m.getAllAxis()) {
//                        Thread.sleep(10);;
                        switch (ax.getAxis_name().toLowerCase()) {
                            case "a":
                                axisAmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                                axisAmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                                axisAmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                                axisAjunctionDeviation.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getJunction_devation())));
                                axisAsearchVelocity.setText(String.valueOf(ax.getSearch_velocity()));
                                axisAzeroOffset.setText(String.valueOf(ax.getZero_backoff()));
                                axisAswitchMode.getSelectionModel().select(ax.getSwitch_mode().ordinal());
                                axisAmaxVelocity.setText(String.valueOf(ax.getVelocity_maximum()));
                                axisAmaxJerk.setText(new DecimalFormat("#.#####").format(ax.getJerk_maximum()));
//                                axisAmaxJerk.setText(String.valueOf(ax.getJerk_maximum()));
                                axisAradius.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getRadius())));
                                //axisAradius.setText(String.valueOf(ax.getRadius()));
                                axisAlatchVelocity.setText(String.valueOf(ax.getLatch_velocity()));
                                break;
                            case "b":
                                axisBmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                                axisBmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                                axisBmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                                axisBjunctionDeviation.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getJunction_devation())));
                                axisBsearchVelocity.setText(String.valueOf(ax.getSearch_velocity()));
                                axisBzeroOffset.setText(String.valueOf(ax.getZero_backoff()));
                                axisBswitchMode.getSelectionModel().select(ax.getSwitch_mode().ordinal());
                                axisBmaxVelocity.setText(String.valueOf(ax.getVelocity_maximum()));
                                axisBmaxJerk.setText(new DecimalFormat("#.#####").format(ax.getJerk_maximum()));
                                //                                axisBmaxJerk.setText(String.valueOf(ax.getJerk_maximum()));
                                //axisBradius.setText(String.valueOf(ax.getRadius()));
                                axisBradius.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getRadius())));
                                axisBlatchVelocity.setText(String.valueOf(ax.getLatch_velocity()));
                                break;
                            case "c":
                                axisCmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                                axisCmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                                axisCmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                                axisCjunctionDeviation.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getJunction_devation())));
                                axisCsearchVelocity.setText(String.valueOf(ax.getSearch_velocity()));
                                axisCzeroOffset.setText(String.valueOf(ax.getZero_backoff()));
                                axisCswitchMode.getSelectionModel().select(ax.getSwitch_mode().ordinal());
                                axisCmaxVelocity.setText(String.valueOf(ax.getVelocity_maximum()));
                                axisCmaxJerk.setText(new DecimalFormat("#.#####").format(ax.getJerk_maximum()));
//                                axisCmaxJerk.setText(String.valueOf(ax.getJerk_maximum()));
                                axisCradius.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getRadius())));
                                axisClatchVelocity.setText(String.valueOf(ax.getLatch_velocity()));
                                break;
                            case "x":
                                axisXradius.setText("NA");
                                axisXradius.setStyle("-fx-text-fill: red");
                                axisXradius.setDisable(true);
                                axisXradius.setEditable(false);
                                axisXmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                                axisXmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                                axisXmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                                axisXjunctionDeviation.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getJunction_devation())));
                                axisXsearchVelocity.setText(String.valueOf(ax.getSearch_velocity()));
                                axisXzeroOffset.setText(String.valueOf(ax.getZero_backoff()));
                                axisXswitchMode.getSelectionModel().select(ax.getSwitch_mode().ordinal());
                                axisXmaxVelocity.setText(String.valueOf(ax.getVelocity_maximum()));
                                axisXmaxJerk.setText(new DecimalFormat("#.#####").format(ax.getJerk_maximum()));
//                                axisXmaxJerk.setText(String.valueOf(ax.getJerk_maximum()));
                                //axisXradius.setText(String.valueOf(ax.getRadius()));
                                axisXlatchVelocity.setText(String.valueOf(ax.getLatch_velocity()));
                                break;
                            case "y":
                                axisYradius.setText("NA");
                                axisYradius.setStyle("-fx-text-fill: red");
                                axisYradius.setDisable(true);
                                axisYradius.setEditable(false);
                                axisYmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                                axisYmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                                axisYmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                                axisYjunctionDeviation.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getJunction_devation())));
                                axisYsearchVelocity.setText(String.valueOf(ax.getSearch_velocity()));
                                axisYzeroOffset.setText(String.valueOf(ax.getZero_backoff()));
                                axisYswitchMode.getSelectionModel().select(ax.getSwitch_mode().ordinal());
                                axisYmaxVelocity.setText(String.valueOf(ax.getVelocity_maximum()));
                                axisYmaxJerk.setText(new DecimalFormat("#.#####").format(ax.getJerk_maximum()));
//                                axisYmaxJerk.setText(String.valueOf(ax.getJerk_maximum()));
                                // axisYradius.setText(String.valueOf(ax.getRadius()));
                                axisYlatchVelocity.setText(String.valueOf(ax.getLatch_velocity()));
                                break;
                            case "z":
                                axisZradius.setText("NA");
                                axisZradius.setStyle("-fx-text-fill: red");
                                axisZradius.setDisable(true);
                                axisZradius.setEditable(false);
                                axisZmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                                axisZmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                                axisZmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                                axisZjunctionDeviation.setText(String.valueOf(new DecimalFormat("#.#####").format(ax.getJunction_devation())));
                                axisZsearchVelocity.setText(String.valueOf(ax.getSearch_velocity()));
                                axisZzeroOffset.setText(String.valueOf(ax.getZero_backoff()));
                                axisZswitchMode.getSelectionModel().select(ax.getSwitch_mode().ordinal());
                                axisZmaxVelocity.setText(String.valueOf(ax.getVelocity_maximum()));
                                axisZmaxJerk.setText(new DecimalFormat("#.#####").format(ax.getJerk_maximum()));
//                                axisZmaxJerk.setText(String.valueOf(ax.getJerk_maximum()));
                                //axisZradius.setText(String.valueOf(ax.getRadius()));
                                axisZlatchVelocity.setText(String.valueOf(ax.getLatch_velocity()));
                                break;
                        }
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



        BasicConfigurator.configure();
        SocketMonitor sm;

//        WebEngine webEngine = html.getEngine();
//        webEngine.load("http://www.synthetos.com/wiki/index.php?title=Projects:TinyG");


        logger.info("[+]tgFX is starting....");
        //Make our canvas as big as the hbox that holds it is.
//        drawingCanvas.setWidth(canvasHolder.getWidth());
       // drawingCanvas.setHeight(canvasHolder.getMaxHeight());
        
        drawingCanvas.setWidth(727);
        drawingCanvas.setHeight(515);
        
        double HEIGHT = drawingCanvas.getHeight();
        double WIDTH = drawingCanvas.getWidth();
        
        gp = drawingCanvas.getGraphicsContext2D();
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
        
        TinygDriver.getInstance().queueReader.setRun(true);
        Thread reader = new Thread(TinygDriver.getInstance().queueReader);
        reader.setName("QueueReader");
        reader.setDaemon(true);
        reader.setPriority(Thread.MIN_PRIORITY);
        reader.start();  //start the queueReader thread.


        Thread threadResponseParser = new Thread(tg.resParse);
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
