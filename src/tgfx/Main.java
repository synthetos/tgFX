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

import org.apache.log4j.BasicConfigurator;
import java.io.*;
import java.nio.charset.Charset;
import java.util.MissingResourceException;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebEngine;
import javafx.util.StringConverter;
import jfxtras.labs.scene.control.gauge.Gauge;
import jfxtras.labs.scene.control.gauge.Lcd;
import jfxtras.labs.scene.control.gauge.LcdBuilder;
import jfxtras.labs.scene.control.gauge.LcdDesign;
import jfxtras.labs.scene.control.gauge.StyleModel;
import jfxtras.labs.scene.control.gauge.StyleModelBuilder;
import jfxtras.labs.scene.control.window.Window;
import org.apache.log4j.Level;
import tgfx.gcode.GcodeLine;
import tgfx.system.Machine.Gcode_unit_modes;
import tgfx.system.StatusCode;
import tgfx.tinyg.CommandManager;

public class Main implements Initializable, Observer {

    
    private String buildDate;
    private int buildNumber;
    
    
    
    
    private boolean drawPreview = true;
    private boolean taskActive = false;
    static final Logger logger = Logger.getLogger(Main.class);
    private TinygDriver tg = TinygDriver.getInstance();
    public ObservableList data;
    private String PROMPT = "tinyg>";
    final static ResourceBundle rb = ResourceBundle.getBundle("version");   //Used to track build date and build number

    /*
     * LCD DRO PROFILE CREATION
     */
    private Lcd xLcd, yLcd, zLcd, aLcd; //DRO Lcds
    private StyleModel STYLE_MODEL_X = StyleModelBuilder.create()
            .lcdDesign(LcdDesign.BLACK)
            .lcdDecimals(3)
            .lcdValueFont(Gauge.LcdFont.LCD)
            .lcdUnitStringVisible(true)
            .build();
    private StyleModel STYLE_MODEL_Y = StyleModelBuilder.create()
            .lcdDesign(LcdDesign.BLACK)
            .lcdDecimals(3)
            .lcdValueFont(Gauge.LcdFont.LCD)
            .lcdUnitStringVisible(true)
            .build();
    private StyleModel STYLE_MODEL_Z = StyleModelBuilder.create()
            .lcdDesign(LcdDesign.BLACK)
            .lcdDecimals(3)
            .lcdValueFont(Gauge.LcdFont.LCD)
            .lcdUnitStringVisible(true)
            .build();
    private StyleModel STYLE_MODEL_A = StyleModelBuilder.create()
            .lcdDesign(LcdDesign.BLACK)
            .lcdDecimals(3)
            .lcdValueFont(Gauge.LcdFont.LCD)
            .lcdUnitStringVisible(true)
            .build();
    /**
     * JFXtras stuff
     */
    private Window w;
    private Pane gcodePane;
    /**
     * FXML UI Components
     */
    @FXML
    private Circle cursor;
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
    private Button Con, Run, Connect, gcodeZero, btnClearScreen, btnRemoteListener, pauseResume, btnTest;
    @FXML
    TextArea console;
    @FXML
    TextField input, listenerPort;
    @FXML
    private Label xAxisVal, yAxisVal, zAxisVal, aAxisVal, srMomo, srState, srVelo, srBuild,
            srVer, srUnits, srCoord, tgfxBuildNumber, tgfxBuildDate, tgfxVersion, tinygHardwareVersion, tinygIdNumber;
    @FXML
    StackPane cursorPoint;
    @FXML
    TextArea gcodesList;
    @FXML
    Label xposT, yposT;
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
    HBox bottom, xposhbox;
    @FXML
    HBox canvasHolder;
    @FXML
    VBox topvbox, positionsVbox;
    /**
     * Drawing Code Vars
     *
     */
    double xPrevious;
    double yPrevious;
    double magnification = 1;

    public Main() {
        this.gcodePane = new Pane();
        double xPrevious = gcodePane.getWidth()/2;
        double yPrevious = gcodePane.getHeight()/2;
    }

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
//        Byte reset = 0x18;
//        tg.resetSpaceBuffer();
//        tg.priorityWrite(reset); //This resets TinyG
//        Thread.sleep(3000); //We need to sleep a bit until TinyG comes back
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
        tg.write(CommandManager.CMD_QUERY_SYSTEM_SETTINGS);
//        tg.write(CommandManager.CMD_QUERY_HARDWARE_BUILD_NUMBER);
//        TinygDriver.getInstance().priorityWrite((byte)0x18);
//        TinygDriver.getInstance().write(CommandManager.CMD_APPLY_SYSTEM_MNEMONIC_SYSTEM_SWITCH_TYPE_NC);
//        TinygDriver.getInstance().m.setFirmwareVersion("867543");
//        TinygDriver.getInstance().m.setFirmwareBuild(0.01);
    }

    @FXML
    void handleMotorQuerySettings(ActionEvent evt) {
        System.out.println("[+]Querying Motor Config...");
        //Detect what motor tab is "active"...
        try {
            //            updateGuiAxisSettings();
            switch (motorTabPane.getSelectionModel().getSelectedItem().getText()) {
                case "Motor 1":
                    tg.queryHardwareSingleMotorSettings(1);
                    break;
                case "Motor 2":
                    tg.queryHardwareSingleMotorSettings(2);
                    break;
                case "Motor 3":
                    tg.queryHardwareSingleMotorSettings(3);
                    break;
                case "Motor 4":
                    tg.queryHardwareSingleMotorSettings(4);
                    break;
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
        try {
            tg.queryHardwareSingleAxisSettings(_axisSelected.charAt(0));
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
                try {
                    console.appendText("[+]Applying Axis.......\n");
                    tg.applyHardwareAxisSettings(_axis, tf);
                } catch (NumberFormatException ex) {
                    console.appendText("[!]Invalid Setting Entered.. Ignoring.");
                    logger.error(ex.getMessage());
                    tg.queryHardwareSingleAxisSettings(_axis.getAxis_name()); //This will reset the input that was bad to the current settings
                }


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

        previewPane.setScaleX(Draw2d.getMagnification());
        previewPane.setScaleY(Draw2d.getMagnification());


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
    private void handleZeroSystem(ActionEvent evt) {
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
                int gcodeCharLength = data.size();
                String tmp;
                for (int i = 0; i < gcodeCharLength; i++) {
                    GcodeLine _gcl = (GcodeLine) data.get(i);


                    if (isTaskActive() == false) {
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

//            
//            tg.write("{\"1ma\":1}");
//            tg.write("{\"gun\":null}");
//
//            tg.write("{\"xfr\":1500}");
//            tg.write(CommandManager.CMD_APPLY_STATUS_REPORT_FORMAT);
            tg.write(CommandManager.CMD_DEFAULT_ENABLE_JSON);
            tg.cmdManager.queryStatusReport(); //If TinyG current positions are other than zero
            
            tg.write(CommandManager.CMD_APPLY_JSON_VOBERSITY);
            tg.write(CommandManager.CMD_APPLY_TEXT_VOBERSITY); 



            /*
             * Do CMD_QUERY_SYSTEM_SETTINGS FIRST
             */
            //FIRST
            tg.cmdManager.queryAllMachineSettings();  //One command to rule them all.
            //FIRST

            tg.cmdManager.queryAllMotorSettings();
            tg.cmdManager.queryAllHardwareAxisSettings();


//            Circle c1 = new Circle();
//            c1.setRadius(50d);
//            

//            c1.fillProperty().setValue(new Color(0,255,0,100));
//            drawingCanvas.getChildren().add(c1);

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
                 * OnConnect Actions Called Here *****************************
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
        gcodePane.getChildren().clear();
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
    private void handleGuiRefresh() throws Exception {
        //Refreshed all gui settings from TinyG Responses.
        if (tg.isConnected()) {

            console.appendText("[+]System GUI Refresh Requested....");
            tg.cmdManager.queryAllHardwareAxisSettings();
            tg.cmdManager.queryAllMachineSettings();
            tg.cmdManager.queryAllMotorSettings();
        } else {
            console.appendText("[!]TinyG Not Connected.. Ignoring System GUI Refresh Request....");
        }
    }

    @FXML
    private void handleEnter(final InputEvent event) throws Exception {
        //private void handleEnter(ActionEvent event) throws Exception {
        final KeyEvent keyEvent = (KeyEvent) event;


        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            String command = (input.getText() + "\n");
            Main.logger.info("Entered Command: " + command);
            if (!tg.isConnected()) {
                Main.logger.error("TinyG is not connected....\n");
                console.appendText("[!]TinyG is not connected....\n");
                input.setPromptText(PROMPT);
                return;
            }
            //TinyG is connected... Proceed with processing command.
            //This will send the command to get a OK prompt if the buffer is empty.
            if ("".equals(command)) {
                tg.write(CommandManager.CMD_QUERY_OK_PROMPT);
            }
            tg.write(command);
            console.appendText(command);
            input.clear();
            input.setPromptText(PROMPT);
        } else if (keyEvent.getCode().equals(KeyCode.F5)) {
            console.appendText("[+]System GUI State Requested....");
            tg.cmdManager.queryAllHardwareAxisSettings();
            tg.cmdManager.queryAllMachineSettings();
            tg.cmdManager.queryAllMotorSettings();
        }
    }
//        }
//    }

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

    public void drawLine(String moveType, double vel) {



        //Code to make mm's look the same size as inches
        double unitMagnication = 1;
        if (tg.m.getGcodeUnitMode().get().equals(Gcode_unit_modes.INCHES.toString())) {
            unitMagnication =  5;  //INCHES
        }else{
            unitMagnication = 2; //MM
        }
//        double newX = unitMagnication * (Double.valueOf(tg.m.getAxisByName("X").getWork_position().get()) + 80);// + magnification;
//        double newY = unitMagnication * (Double.valueOf(tg.m.getAxisByName("Y").getWork_position().get()) + 80);// + magnification;
        
        double newX = (Double.valueOf(tg.m.getAxisByName("x").getWork_position().get())) + (gcodePane.getWidth()/2);// + magnification;
        double newY = (gcodePane.getHeight() - (Double.valueOf(tg.m.getAxisByName("y").getWork_position().get()))) - (gcodePane.getHeight()/2);// + magnification;
//        System.out.println(gcodePane.getHeight() - tg.m.getAxisByName("y").getWork_position().get());
        Line l = new Line(xPrevious, yPrevious, newX, newY);
//        l.setStroke(Color.BLUE);
        

        if (tg.m.getMotionMode().get().equals("traverse")) {
            //G0 Move
//            l.setStrokeWidth(Draw2d.getStrokeWeight() / 2);
            l.setStrokeDashOffset(5);
            l.setStroke(Draw2d.TRAVERSE);
        }else{
            l.setStroke(Draw2d.getLineColorFromVelocity(vel));
        }


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
            gcodePane.getChildren().add(l);
        }


    }

    private void updateGUIConfigState() {
        //Update the GUI for config settings
        Platform.runLater(new Runnable() {
            float vel;

            @Override
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

    private void drawCanvasUpdate(String line) {
        final String l = line;
        if (drawPreview) {
            drawLine(tg.m.getMotionMode().get(), tg.m.getVelocity());
        }
    }

    private void updateGuiMachineSettings(String line) {
        final String l = line;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //We are now back in the EventThread and can update the GUI
                try {
                    Machine m = TinygDriver.getInstance().m;
//                    srBuild.setText(String.valueOf(m.getFirmwareBuild()));
//                    srVer.setText(String.valueOf(m.getFirmwareVersion()));
//                    gcodePlane.getSelectionModel().select(TinygDriver.getInstance().m.getGcode_select_plane().ordinal());
//                    gcodeUnitMode.getSelectionModel().select(TinygDriver.getInstance().m.getGcodeUnitMode());
//                    gcodeCoordSystem.getSelectionModel().select(TinygDriver.getInstance().m.getCoordinateSystem().ordinal());
//                    gcodePathControl.getSelectionModel().select(TinygDriver.getInstance().m.getGcode_distance_mode().ordinal());
//                    gcodeDistanceMode.getSelectionModel().select(TinygDriver.getInstance().m.getGcode_distance_mode().ordinal());

//                    if (m.getCoordinateSystem() != null) {
//                        srCoord.setText(TinygDriver.getInstance().m.getCoordinateSystem().toString());
//                    }
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
        if (arg.getClass().getCanonicalName().equals("tgfx.system.StatusCode")) {
            //We got an error condition.. lets route it to where it goes!
            StatusCode statuscode = (StatusCode) arg;
            console.appendText("[->] TinyG Response: " + statuscode.getStatusType() + ":" + statuscode.getMessage() + "\n");
        } else {
            final String[] UPDATE_MESSAGE = (String[]) arg;
            final String ROUTING_KEY = UPDATE_MESSAGE[0];
            final String KEY_ARGUMENT = UPDATE_MESSAGE[1];

//            if (ROUTING_KEY.startsWith("[!]")) {
//                String line = ROUTING_KEY.split("#")[1];
//                String msg = ROUTING_KEY.split("#")[0];
//                
//                Main.logger.error("Invalid Routing Key: \n\tMessage: " + msg + "\n\tLine: " + line);
//            } else 
            if (ROUTING_KEY.equals("STATUS_REPORT")) {
                drawCanvasUpdate(ROUTING_KEY);
            } else if (ROUTING_KEY.equals("CMD_GET_AXIS_SETTINGS")) {
                updateGuiAxisSettings(KEY_ARGUMENT);
            } else if (ROUTING_KEY.equals("CMD_GET_MACHINE_SETTINGS")) {
                //updateGuiMachineSettings(ROUTING_KEY);
            } else if (ROUTING_KEY.contains("CMD_GET_MOTOR_SETTINGS")) {
                updateGuiMotorSettings(KEY_ARGUMENT);
            } else if (ROUTING_KEY.equals("NETWORK_MESSAGE")) {  //unused
                //updateExternal();
            } else if (ROUTING_KEY.equals("MACHINE_UPDATE")) {
                updateGuiMachineSettings(ROUTING_KEY);
            } else if (ROUTING_KEY.equals("TEXTMODE_REPORT")) {
                console.appendText(KEY_ARGUMENT);
            } else {
                System.out.println("[!]Invalid Routing Key: " + ROUTING_KEY);
            }
        }
    }
//

    private void updateGuiMotorSettings() {
        //No motor was provided... Update them all.
        updateGuiMotorSettings(null);
    }

    private void updateGuiMotorSettings(final String arg) {
        //Update the GUI for config settings
        Platform.runLater(new Runnable() {
            String MOTOR_ARGUMENT = arg;

            @Override
            public void run() {
                try {
                    if (MOTOR_ARGUMENT == null) {
                        //Update ALL motor's gui settings
                        for (Motor m : tg.m.getMotors()) {
                            _updateGuiMotorSettings(String.valueOf(m.getId_number()));
                        }
                    } else {
                        //Update only ONE motor's gui settings
                        _updateGuiMotorSettings(MOTOR_ARGUMENT);
                    }
                } catch (Exception ex) {
                    System.out.println("[!]Exception in updateGuiMotorSettings...");
                    System.out.println(ex.getMessage());
                }
            }
        });
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

    public static final String getBuildInfo(String propToken) {
        String msg = "";
        try {
            msg = rb.getString(propToken);
        } catch (MissingResourceException e) {
           logger.error("Error Getting Build Info Token ".concat(propToken).concat(" not in Propertyfile!"));
        }
        return msg;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        
        buildNumber = Integer.valueOf(getBuildInfo("BUILD"));
        buildDate = getBuildInfo("DATE");
        
        //Set our build / versions in the tgFX settings tab.
        tgfxBuildDate.setText(buildDate);
        tgfxBuildNumber.setText(getBuildInfo("BUILD"));
        tgfxVersion.setText(".9");

//  

        w = new Window("Gcode Preview");
        // set the window position to 10,10 (coordinates inside canvas)
        w.setLayoutX(10);
        w.setLayoutY(10);
        w.setStyle("-fx-background-color:black");
        // define the initial window size
        w.setPrefSize(700, 450);
        w.setTooltip(new Tooltip("This GUI is draggable and can be resized \nby clicking the bottom right corner and dragging."));

        // either to the left
//        w.getLeftIcons().add(new CloseIcon(w));

        // .. or to the right
//        w.getRightIcons().add(new MinimizeIcon(w));
//        ScrollBar vGcodePreviewScrollBar = new ScrollBar();
//        ScrollBar hGcodePreviewScrollBar = new ScrollBar();
//        vGcodePreviewScrollBar.setOrientation(Orientation.VERTICAL);
//        hGcodePreviewScrollBar.setOrientation(Orientation.HORIZONTAL);
//        
//        
//        hGcodePreviewScrollBar.valueProperty().addListener(new ChangeListener<Number>() {
//            public void changed(ObservableValue<? extends Number> ov,
//                Number old_val, Number new_val) {
//                System.out.println(new_val);
//                    gcodePane.setLayoutY(-new_val.doubleValue());
//            }
//        });
        
//   
//        gcodePane.getChildren().add(vGcodePreviewScrollBar);
//        gcodePane.getChildren().add(hGcodePreviewScrollBar);
//       
//        gcodePane.setTranslateX(gcodePane.getWidth());
        w.getContentPane().getChildren().add(gcodePane);
        // add some content
//        w.getContentPane().getChildren().add(new Label("Content"));
        previewPane.getChildren().add(w);



//        xtgPA.bindBidirectional("firmwareBuild", srBuild.textProperty());
//        tgPA.bindBidirectional("firmwareBuild", srBuild.textProperty());
        logger.setLevel(Level.ERROR);

        xLcd = buildSingleDRO(xLcd, STYLE_MODEL_X, "X Axis Position", tg.m.getGcodeUnitMode().get());
        yLcd = buildSingleDRO(yLcd, STYLE_MODEL_Y, "Y Axis Position", tg.m.getGcodeUnitMode().get());
        zLcd = buildSingleDRO(zLcd, STYLE_MODEL_Z, "Z Axis Position", tg.m.getGcodeUnitMode().get());
        aLcd = buildSingleDRO(aLcd, STYLE_MODEL_A, "A Axis Position", "°");

        StackPane droStackPane = new StackPane();
        droStackPane.getChildren().addAll(xLcd, yLcd, zLcd, aLcd);

//        
        positionsVbox.getChildren().add(xLcd);
        positionsVbox.getChildren().add(yLcd);
        positionsVbox.getChildren().add(zLcd);
        positionsVbox.getChildren().add(aLcd);


        xLcd.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldValue, Object newValue) {
                double tmp = TinygDriver.getInstance().m.getAxisByName("y").getWork_position().doubleValue() + 5;
                xposT.setText(String.valueOf(tmp));
                cursorPoint.setLayoutY(tmp);
            }
        });


        yLcd.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldValue, Object newValue) {
                double tmp = TinygDriver.getInstance().m.getAxisByName("y").getWork_position().doubleValue() + 5;
                yposT.setText(String.valueOf(tmp));
                cursorPoint.setLayoutY(tmp);
                //cursor.setLayoutY(canvasHolder.getHeight() - tg.m.getAxisByName("y").getWork_position().doubleValue());  
            }
        });


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
        srBuild.textProperty().bindBidirectional(tg.m.firmwareBuild, sc);
        srState.textProperty().bind(tg.m.m_state);
        srCoord.textProperty().bind(tg.m.getCoordinateSystem());
        srUnits.textProperty().bind(tg.m.getGcodeUnitMode());

        //Bind our Units to each axis
        tg.m.getGcodeUnitMode().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldValue, Object newValue) {
                String tmp = TinygDriver.getInstance().m.getGcodeUnitMode().get();
//                System.out.println("Gcode Units Changed to: " + tmp);
                xLcd.setUnit(tmp);
                yLcd.setUnit(tmp);
                zLcd.setUnit(tmp);
                gcodeUnitMode.getSelectionModel().select(tg.m.getGcodeUnitModeAsInt());
                console.appendText("[+]Gcode Unit Mode Changed to: " + tmp + "\n");
                try {
                    tg.cmdManager.queryAllMotorSettings();
                    tg.cmdManager.queryAllHardwareAxisSettings();
                } catch (Exception ex) {
                    logger.error("Error querying tg model state on gcode unit change.  Main.java binding section.");
                }
            }
        });

//        yLcd.lcdUnitProperty().bind(tg.m.getGcodeUnitMode());
//        zLcd.lcdUnitProperty().bind(tg.m.getGcodeUnitMode());
//        aLcd.lcdUnitProperty().bind(tg.m.getCoordinateSystem());  Always degress

        xLcd.valueProperty().bind(TinygDriver.getInstance().m.getAxisByName("x").getWork_position());
        yLcd.valueProperty().bind(TinygDriver.getInstance().m.getAxisByName("y").getWork_position());
        zLcd.valueProperty().bind(TinygDriver.getInstance().m.getAxisByName("z").getWork_position());
        aLcd.valueProperty().bind(TinygDriver.getInstance().m.getAxisByName("a").getWork_position());

        //cursor
//        cursorPoint.layoutXProperty().bind(tg.m.getAxisByName("x").getWork_position());
//        cursorPoint.layoutYProperty().bind(tg.m.getAxisByName("y").getWork_position());


        BasicConfigurator.configure();
        SocketMonitor sm;

        WebEngine webEngine = html.getEngine();
        webEngine.load("https://github.com/synthetos/TinyG/wiki");


        logger.info("[+]tgFX is starting....");


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

//        Thread remoteListener = new Thread(initRemoteServer("8888"));
//        remoteListener.setName("Remote Listener Thread");
//        remoteListener.start();

        tg.resParse.addObserver(this);
        this.reScanSerial();//Populate our serial ports


    }
}
