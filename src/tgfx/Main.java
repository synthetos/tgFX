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
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Rectangle2DBuilder;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import tgfx.external.SocketMonitor;
import tgfx.render.Draw2d;
import tgfx.system.Machine;
import tgfx.system.Motor;

/**
 *
 * @author ril3y
 */
public class Main implements Initializable, Observer {

    //private static final String CMD_GET_stateUS_REPORT = "{\"sr\":\"\"}\n";
//    public Machine m = new Machine();
    private JdomParser JDOM = new JdomParser(); //JSON Object Parser1
    private TinygDriver tg = TinygDriver.getInstance();
    //private SerialDriver ser = SerialDriver.getInstance();
    /**
     * FXML UI Components
     */
    @FXML
    private Button Con, Run, Connect, gcodeZero, btnClearScreen, btnRemoteListener, pauseResume;
    @FXML
    TextArea console;
    @FXML
    TextField input, listenerPort;
    @FXML
    private Label xAxisVal, yAxisVal, zAxisVal, aAxisVal, srMomo, srState, srVelo, srBuild,
            srVer, srUnits;
    @FXML
    TextArea gcodesList;
    @FXML
    WebView html;
    @FXML
    ChoiceBox serialPorts;
    //##########Config FXML##############//
    @FXML
    TextField motor1ConfigTravelPerRev,
            motor2ConfigTravelPerRev,
            motor3ConfigTravelPerRev,
            motor4ConfigTravelPerRev,
            motor1ConfigStepAngle,
            motor2ConfigStepAngle,
            motor3ConfigStepAngle,
            motor4ConfigStepAngle;
    @FXML
    ChoiceBox motor1ConfigMapAxis,
            motor2ConfigMapAxis,
            motor3ConfigMapAxis,
            motor4ConfigMapAxis,
            motor1ConfigMicroSteps,
            motor2ConfigMicroSteps,
            motor3ConfigMicroSteps,
            motor4ConfigMicroSteps,
            motor1ConfigPolarity,
            motor2ConfigPolarity,
            motor3ConfigPolarity,
            motor4ConfigPolarity,
            motor1ConfigPowerMode,
            motor2ConfigPowerMode,
            motor3ConfigPowerMode,
            motor4ConfigPowerMode;
    @FXML
    Group canvsGroup;  //Drawing Canvas
    @FXML
    Group motor1Node;
    @FXML
    HBox bottom;
    @FXML
    HBox canvas;
    @FXML
    VBox topvbox;
    /**
     * Drawing Code Vars
     *
     */
    double xPrevious = 0;
    double yPrevious = 0;
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

                try {
                    console.appendText("[+]Loading a gcode file.....\n");
                    FileChooser fc = new FileChooser();
                    fc.setTitle("Open GCode File");
                    fc.setInitialDirectory(new File("c:\\"));
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
    private void CancelFile(ActionEvent evt) throws Exception {
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
    void handleMotorQuery(ActionEvent evt) {
    }

    @FXML
    void handleMotorApply(ActionEvent evt) {
    }

    @FXML
    void handleRemoteListener(ActionEvent evt) {

        if (tg.isConnected()) {
            console.appendText("[+]Remote Monitor Listening for Connections....");
            Task SocketListner = this.initRemoteServer(listenerPort.getText());

            new Thread(SocketListner).start();
            btnRemoteListener.setDisable(true);
        } else {
            System.out.println("[!] Must be connected to TinyG First.");
            console.appendText("[!] Must be connected to TinyG First.");
        }

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

        canvsGroup.setScaleX(Draw2d.getMagnification());
        canvsGroup.setScaleY(Draw2d.getMagnification());
    }

    private void reDrawPreview() {
        for (Node n : canvsGroup.getChildren()) {
            Line nd = (Line) n;
            nd.setStrokeWidth(Draw2d.getStrokeWeight());
        }
//        console.appendText("[+]2d Preview Stroke Width: " + String.valueOf(Draw2d.getStrokeWeight()) + "\n");
    }

    @FXML
    private void zeroSystem(ActionEvent evt) {
        if (tg.isConnected() && tg.getClearToSend()) {
            try {
                tg.write(tg.CMD_ZERO_ALL_AXIS);
                //G92 does not invoke a status report... So we need to generate one to have
                //Our GUI update the coordinates to zero
                tg.write(tg.CMD_GET_STATUS_REPORT);
                //We need to set these to 0 so we do not draw a line from the last place we were to 0,0
                xPrevious = 0;
                yPrevious = 0;
            } catch (Exception ex) {
            }
        }
    }

    @FXML
    private void runFile(ActionEvent evt) {
        Task fileSend = fileSenderTask();
        new Thread(fileSend).start();

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
                while (tg.isConnected()) {


                    for (String l : gcodeProgramList) {
                        if (!tg.isConnected()) {
                            console.appendText("[!]Serial Port Disconnected.... Stopping file sending task...");
                            return false;
                            //break;
                        }

                        //###############CRITICAL SECTION#########################
                        //If this code is changed be very careful as there is much logic here
                        //to screw up.  This code makes it so that when you send a file and disconnect or
                        //press stop this filesending task dies.  
                        if (l.startsWith("(") || l.equals("")) {
                            //Skip these lines as they will not illicit a "OK" 
                            //From tinyg
                            continue;
                        } else {
                            line = new String("{\"gc\":\"" + l + "\"}" + "\n");

                            if (tg.getClearToSend() && !tg.isPAUSED() && !tg.isCANCELLED()) {
                                tg.write(line);
                            } else if (tg.isCANCELLED()) {
                                console.appendText("[!]Canceling the file sending task...\n");
                                return false;

                            } else if (tg.isPAUSED()) {

                                while (tg.isPAUSED()) {
                                    //Infinite Loop
                                    //Not ready yet
                                    Thread.sleep(1);
                                }
                                //This breaks out of the while loop and does some more checking to eliminate any race conditions
                                //That might have occured duing waiting for to unpause.
                                if (!tg.isConnected()) {
                                    console.appendText("[!]Serial Port Disconnected.... Stopping file sending task...");
                                    return false;
                                } else if (tg.isCANCELLED()) {
                                    console.appendText("[!]Canceling the file sending task...");
                                    return false;
                                }
                                tg.write(line);
                            } else {
                                while (!tg.getClearToSend()) {
                                    //Not ready yet
                                    Thread.sleep(10);
                                    //We have to check again while in the sleeping thread that sometime
                                    //during waiting for the clearbuffer the serialport has not been disconnected.
                                    //And cancel has not been called
                                    if (!tg.isConnected()) {
                                        console.appendText("[!]Serial Port Disconnected.... Stopping file sending task...");
                                        return false;
                                    } else if (tg.isCANCELLED()) {
                                        console.appendText("[!]Canceling the file sending task...");
                                        return false;
                                    }
                                }

                                //This looks like its not needed since the same check above in the while block.
                                //However I am pretty confident that this is.
                                if (!tg.isConnected()) {
                                    console.appendText("[!]Serial Port Disconnected.... Stopping file sending task...");
                                    return false;
                                }
                                //Finally write the line everything is Good to go.
                                tg.write(line);
                            }
                        }
                    }
                    console.appendText("[+] Sending File Complete\n");
                    return true;

                }
                return true;
            }
        };
    }
    //###############CRITICAL SECTION#########################

    @FXML
    private void FXreScanSerial(ActionEvent event) {
        this.reScanSerial();
    }

    private void reScanSerial() {
        serialPorts.getItems().clear();
        String portArray[] = null;
        portArray = tg.listSerialPorts();


        for (String p : portArray) {
            serialPorts.getItems().add(p);
        }
    }

    /**
     * These are the actions that need to be ran upon successful serial port
     * connection. If you have something that you want to "auto run" on connect.
     * This is the place to do so. This method is called in handleConnect.
     */
    private void onConnectActions() {
        try {
            //DISABLE LOCAL ECHO!! THIS IS A MUST OR NOTHING WORKS
            tg.write(tg.CMD_GET_OK_PROMPT);
            tg.write(tg.CMD_DISABLE_LOCAL_ECHO);
            tg.write(tg.CMD_GET_OK_PROMPT);  //This is required as "status reports" do not return an "OK" msg
//            //DISABLE LOCAL ECHO!! THIS IS A MUST OR NOTHING WORKS

//            tg.write(tg.CMD_SET_STATUS_UPDATE_INTERVAL); //Set to every X ms
            tg.write(tg.CMD_GET_OK_PROMPT);  //This is required as "status reports" do not return an "OK" msg
            //this will poll for the new values and update the GUI

            //Updates the Config GUI from settings currently applied on the TinyG board
            tg.getAllMotorSettings();
            tg.getAllAxisSettings();

            tg.write(tg.CMD_GET_OK_PROMPT);  //This is required as "status reports" do not return an "OK" msg
            tg.write(tg.CMD_GET_STATUS_REPORT);  //If TinyG current positions are other than zero
            tg.write(tg.CMD_GET_OK_PROMPT);  //This is required as "status reports" do not return an "OK" msg

            tg.write(tg.CMD_GET_HARDWARE_BUILD_NUMBER);  //If TinyG current positions are other than zero
            tg.write(tg.CMD_GET_OK_PROMPT);  //This is required as "status reports" do not return an "OK" msg
            
            tg.write(tg.CMD_GET_HARDWARE_FIRMWARE_NUMBER);  //If TinyG current positions are other than zero
            tg.write(tg.CMD_GET_OK_PROMPT);  //This is required as "status reports" do not return an "OK" msg

            /**
             * Draw the workspace area in the preview
             */
            Float width = Float.valueOf(tg.m.getAxisByName("X").getTravel_maximum());
            Float height = Float.valueOf(tg.m.getAxisByName("Y").getTravel_maximum());
            Label hLabel = new Label("Table Width: " + width);

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
            tg.write(tg.CMD_GET_OK_PROMPT);
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
        canvsGroup.getChildren().clear();
    }

    private void handleTilda() {
        //                ==============HIDE CONSOLE CODE==============
        System.out.println("TILDA");
        if (topvbox.getChildren().contains(bottom)) {
            topvbox.getChildren().remove(bottom);

        } else {
            topvbox.getChildren().add(topvbox.getChildren().size() - 1, bottom);
        }
//        String cmd = input.getText();
//        cmd = cmd.replace('`', ' ');  //Remove the tilda from the input box
//        input.setText(cmd);
// 
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
                    out.write(SerialDriver.getInstance().getDebugFileString());
                    out.close();
                } catch (IOException e) {
                    System.out.println("Exception ");
                }
            } else if (keyEvent.getCode() == KeyCode.F1) {
                Draw2d.incrementSetStrokeWeight();
                reDrawPreview();
                console.appendText("[+]Increasing Stroke Width: " + String.valueOf(Draw2d.getStrokeWeight()) + "\n");
            } else if (keyEvent.getCode() == KeyCode.F2) {
                Draw2d.decrementSetStrokeWeight();
                reDrawPreview();
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
            if (tg.isConnected()) {

                String command = (input.getText() + "\n");
                //This will send the command to get a OK prompt if the buffer is empty.
                if (command == "") {
                    tg.write(TinygDriver.CMD_GET_OK_PROMPT);
                } else {
                    //Execute whatever you placed in the input box
                    tg.write(command);
                    console.appendText(command);
                    input.clear();
                    input.setPromptText(PROMPT);
                }

            } else {
                System.out.println("TinyG is not connected....\n");
                console.appendText("[!]TinyG is not connected....\n");
                input.setPromptText(PROMPT);

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

        l.setStrokeWidth(Draw2d.getStrokeWeight());

        xPrevious = newX;
        yPrevious = newY;

        if (l != null) {
            canvsGroup.getChildren().add(l);
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
                        srBuild.setText(String.valueOf(tg.m.getFirmware_build()));
                        srVer.setText(String.valueOf(tg.m.getFirmware_version()));
                    } catch (Exception ex) {
                        System.out.println("$$$$$$$$$$$$$EXCEPTION$$$$$$$$$$$$$$");
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

                        drawLine(tg.m.getMotionMode(), vel);


                    } catch (Exception ex) {
                        System.out.println("$$$$$$$$$$$$$EXCEPTION$$$$$$$$$$$$$$");
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

    /**
     * Observer Methods
     *
     * @param o
     * @param arg
     */
    @Override
    public synchronized void update(Observable o, Object arg) {
        final String ROUTING_KEY = (String) arg;

        //We have to run the updates likes this.
        //https://forums.oracle.com/forums/thread.jspa?threadID=2298778&start=0 for more information
        Platform.runLater(new Runnable() {

            public void run() {
                // we are now back in the EventThread and can update the GUI
                if (ROUTING_KEY.equals("PLAIN")) {
                    console.appendText((String) ROUTING_KEY + "\n");

                } else if (ROUTING_KEY.equals("BUILD_UPDATE")) {
                    updateGuiState(ROUTING_KEY);
                } else if (ROUTING_KEY.equals("STATUS_REPORT")) {
//                    console.setText((String) MSG[1] + "\n");
                    updateGuiState(ROUTING_KEY);

                } else if (ROUTING_KEY.contains("ERROR")) {
                    console.appendText(ROUTING_KEY);
                } else if (ROUTING_KEY.equals("CMD_GET_MACHINE_SETTINGS")) {
//                    System.out.println("UPDATE: MACHINE SETTINGS");
//                    updateGUIConfigState();
                } else {
                    console.appendText(ROUTING_KEY);
                    System.out.println(ROUTING_KEY);
                }

            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SocketMonitor sm;

        WebEngine webEngine = html.getEngine();
        webEngine.load("http://www.synthetos.com/wiki/index.php?title=Projects:TinyG");




        tg.addObserver(this);
        this.reScanSerial();//Populate our serial ports


    }
}
