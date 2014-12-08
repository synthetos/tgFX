/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.ui.gcode;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import jfxtras.labs.scene.control.gauge.Lcd;
import org.apache.log4j.Logger;
import tgfx.Main;
import tgfx.render.CNCMachine;
import tgfx.render.Draw2d;
import tgfx.tinyg.CommandManager;
import tgfx.tinyg.TinygDriver;
import tgfx.ui.tgfxsettings.TgfxSettingsController;

/**
 * FXML Controller class
 *
 * @author rileyporter
 */
public class GcodeTabController implements Initializable {

    private byte[] BAD_BYTES = {(byte) 0x21, (byte) 0x18, (byte) 0x7e};
    private double scaleAmount;
    private int buildNumber;
    private String buildDate;
    private boolean taskActive = false;
    static final Logger logger = Logger.getLogger(GcodeTabController.class);
    public ObservableList data; //List to store the gcode file
    public static StackPane gcodePane = new StackPane(); //Holds CNCMachine  This needs to be before CNCMachine()
    private static CNCMachine cncMachine = new CNCMachine();
    private final EventHandler keyPress;
    private final EventHandler keyRelease;
    private String _axis = new String();
    public static SimpleBooleanProperty isSendingFile = new SimpleBooleanProperty(false);  //This tracks to see if we are sending a file to tinyg.  This allows us to NOT try to jog while sending files
    private boolean isKeyPressed = false;
    private double jogDial = 0;
    private double FEED_RATE_PERCENTAGE = .05;  //%5
    private double TRAVERSE_FEED_RATE = 1;  //%100
    private double NUDGE_FEED_RATE = .05;  //%5
    private static int totalGcodeLines = 0;
    private static Date timeStartDt;
    /*  ######################## FXML ELEMENTS ############################*/
    @FXML
    private static Text timeElapsedTxt;
    @FXML
    private static Text timeLeftTxt;
    @FXML
    private Lcd xLcd, yLcd, zLcd, aLcd, velLcd; //DRO Lcds
    @FXML
    StackPane machineWorkspace;
    @FXML
    private Pane previewPane;
    @FXML
    private TableColumn<GcodeLine, String> gcodeCol;
    @FXML
    private static TableView gcodeView;
    @FXML
    private Text xAxisLocation, yAxisLocation;
    @FXML
    private static Text gcodeStatusMessage;  //Cursor location on the cncMachine Canvas
    @FXML
    private static TextArea console;
    @FXML
    private Button Run, Connect, gcodeZero, btnClearScreen, pauseResume, btnTest, btnHandleInhibitAllAxis;
    @FXML
    private GridPane coordLocationGridPane;
    private float zScale = 0.1f;
    String cmd;
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    @FXML // fx:id="zMoveScale"
    private ChoiceBox<?> zMoveScale; // Value injected by FXMLLoader
    @FXML
    private HBox gcodeTabControllerHBox;

    /**
     * Initializes the controller class.
     */
    public GcodeTabController() {
        logger.setLevel(org.apache.log4j.Level.ERROR);
        logger.info("Gcode Controller Loaded");
        cncMachine.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                yAxisLocation.setText(cncMachine.getNormalizedYasString(me.getY()));
                xAxisLocation.setText(cncMachine.getNormalizedXasString(me.getX()));




            }
        });


        //JOGGING NEEDS TO BE BROKEN INTO A NEW CLASS
        //JOGGING NEEDS TO BE BROKEN INTO A NEW CLASS
        //JOGGING NEEDS TO BE BROKEN INTO A NEW CLASS
        //JOGGING NEEDS TO BE BROKEN INTO A NEW CLASS
        //JOGGING NEEDS TO BE BROKEN INTO A NEW CLASS
        //JOGGING NEEDS TO BE BROKEN INTO A NEW CLASS
        //JOGGING NEEDS TO BE BROKEN INTO A NEW CLASS

        keyPress = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (isSendingFile.get() == false) {  //If we are sending a file.. Do NOT jog right now
//                Main.postConsoleMessage("KEY PRESSED: " + keyEvent.getCode().toString());

                    //Do the jogging.
                    _axis = " "; // Initialize to no valid axis set

                    if (!isKeyPressed) { //If this event has already sent a jog in need to pass this over.
                        KeyCode _kc = keyEvent.getCode();
                        if (_kc.equals(KeyCode.SHIFT)) {
                            return;   //This is going to toss out our initial SHIFT press for the z axis key combination.
                        }

                        if (keyEvent.isShiftDown()) {
                            //Alt is down so we make this into a Z movement
                            FEED_RATE_PERCENTAGE = TRAVERSE_FEED_RATE;
                        } else {
                            FEED_RATE_PERCENTAGE = NUDGE_FEED_RATE;
                        }

                        //Y Axis Jogging Movement
                        if (_kc.equals(KeyCode.UP) || _kc.equals(KeyCode.DOWN)) {
                            //This is and Y Axis Jog action
                            _axis = "Y"; //Set the axis for this jog movment
                            if (keyEvent.getCode().equals(KeyCode.UP)) {
                                jogDial = TinygDriver.getInstance().machine.getJoggingIncrementByAxis(_axis);
                            } else if (keyEvent.getCode().equals(KeyCode.DOWN)) {
                                jogDial = (-1 * TinygDriver.getInstance().machine.getJoggingIncrementByAxis(_axis)); //Invert this value by multiplying by -1
                            }

                            //X Axis Jogging Movement
                        } else if (_kc.equals(KeyCode.RIGHT) || _kc.equals(KeyCode.LEFT)) {
                            //This is a X Axis Jog Action
                            _axis = "X"; //Set the axis for this jog movment
                            if (keyEvent.getCode().equals(KeyCode.LEFT)) {
                                jogDial = (-1 * TinygDriver.getInstance().machine.getJoggingIncrementByAxis(_axis));


                            } else if (keyEvent.getCode().equals(KeyCode.RIGHT)) {
                                jogDial = TinygDriver.getInstance().machine.getJoggingIncrementByAxis(_axis); //Invert this value by multiplying by -1
                            }

                            //Z Axis Jogging Movement
                        } else if (_kc.equals(KeyCode.MINUS) || (_kc.equals(KeyCode.EQUALS))) {
                            _axis = "Z";
                            if (keyEvent.getCode().equals(KeyCode.MINUS)) {
                                jogDial = (-1 * TinygDriver.getInstance().machine.getJoggingIncrementByAxis(_axis));
                            } else if (keyEvent.getCode().equals(KeyCode.EQUALS)) {
                                jogDial = TinygDriver.getInstance().machine.getJoggingIncrementByAxis(_axis); //Invert this value by multiplying by -1
                            }
                        }


                        try {
                            if (_axis.equals("X") || _axis.equals("Y") || _axis.equals("Z")) {
                                // valid key pressed
                                CommandManager.setIncrementalMovementMode();
                                TinygDriver.getInstance().write("{\"GC\":\"G1F" + (TinygDriver.getInstance().machine.getAxisByName(_axis).getFeed_rate_maximum() * FEED_RATE_PERCENTAGE) + _axis + jogDial + "\"}\n");
//                                TinygDriver.getInstance().write("{\"GC\":\"G0" + _axis + jogDial + "\"}\n");
                                isKeyPressed = true;
                            }

                        } catch (Exception ex) {
                            java.util.logging.Logger.getLogger(CNCMachine.class.getName()).log(Level.SEVERE, null, ex);
                        }


                    }

                } //end if isSendingFile
                else {
                    //We are sending a file... We need to post a messages
                    setGcodeTextTemp("Jogging Disabled... Sending File.");
                }
            }
        };

        keyRelease = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
//                Main.postConsoleMessage("Stopping Jog Action: " + keyEvent.getCode().toString());
                if (isSendingFile.get() == false) {


                    try {
                        setGcodeText("");
                        if (isKeyPressed) {  //We should find out of TinyG's distance mode is set to G90 before just firing this off.

                            CommandManager.stopJogMovement();
                            if (TinygDriver.getInstance().machine.getGcode_distance_mode().equals(TinygDriver.getInstance().machine.gcode_distance_mode.INCREMENTAL)) {
                                //We are in incremental mode we now will enter ABSOLUTE mode
                                CommandManager.setAbsoluteMovementMode();
                            } //re-enable absolute mode
                            isKeyPressed = false; //reset the press flag
                        }
                    } catch (Exception ex) {
                        java.util.logging.Logger.getLogger(CNCMachine.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

            }
        };



        cncMachine.setOnKeyPressed(keyPress);
        cncMachine.setOnKeyReleased(keyRelease);

    }

    public static void setGcodeTextTemp(String _text) {
        gcodeStatusMessage.setText(_text);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(3000), gcodeStatusMessage);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.play();
//        gcodeStatusMessage.setText(""); //clear it out

    }

    public static void setGcodeText(String _text) {
        gcodeStatusMessage.setText(_text);
        gcodeStatusMessage.setVisible(true);
//        FadeTransition fadeTransition  = new FadeTransition(Duration.millis(1000), gcodeStatusMessage);
//                fadeTransition.setFromValue(0.0);
//                fadeTransition.setToValue(1.0);
//                fadeTransition.play();
    }

    public static void hideGcodeText() {
//        gcodeStatusMessage.setVisible(false);
//        FadeTransition fadeTransition  = new FadeTransition(Duration.millis(500), gcodeStatusMessage);
//                fadeTransition.setFromValue(1.0);
//                fadeTransition.setToValue(0.0);
//                fadeTransition.play();
    }

    public static void drawCanvasUpdate() {
        if (TgfxSettingsController.isDrawPreview()) {
            cncMachine.drawLine(TinygDriver.getInstance().machine.getMotionMode().get(), TinygDriver.getInstance().machine.getVelocity());
        }
    }

    private void drawTable() {
        //TODO  We need to make this a message to subscribe to.
        if (!gcodePane.getChildren().contains(cncMachine)) {
            gcodePane.getChildren().add(cncMachine); // Add the cnc machine to the gcode pane
        }
    }

    @FXML
    private void handleHomeXYZ(ActionEvent evt) {
        if (TinygDriver.getInstance().isConnected().get()) {
            try {
                TinygDriver.getInstance().write(CommandManager.CMD_APPLY_SYSTEM_HOME_XYZ_AXES);
            } catch (Exception ex) {
                logger.error("Erroring HomingXYZ Command");
            }
        }
    }

    @FXML
    private void handleHomeAxisClick(ActionEvent evt) {
        MenuItem m = (MenuItem) evt.getSource();
        String _axis = String.valueOf(m.getId().charAt(0));
        if (TinygDriver.getInstance().isConnected().get()) {
            try {
                switch (_axis) {
                    case "x":
                        TinygDriver.getInstance().write(CommandManager.CMD_APPLY_HOME_X_AXIS);
                        break;
                    case "y":
                        TinygDriver.getInstance().write(CommandManager.CMD_APPLY_HOME_Y_AXIS);
                        break;
                    case "z":
                        TinygDriver.getInstance().write(CommandManager.CMD_APPLY_HOME_Z_AXIS);
                        break;
                    case "a":
                        TinygDriver.getInstance().write(CommandManager.CMD_APPLY_HOME_A_AXIS);
                        break;
                }
            } catch (Exception ex) {
                logger.error("Exception in handleHomeAxisClick for Axis: " + _axis + " " + ex.getMessage());
            }
        }
        tgfx.Main.postConsoleMessage("[+]Homing " + _axis.toUpperCase() + " Axis...\n");
    }

    @FXML
    private void handleZeroAxisClick(ActionEvent evt) {
        MenuItem m = (MenuItem) evt.getSource();
        String _axis = String.valueOf(m.getId().charAt(0));
        if (TinygDriver.getInstance().isConnected().get()) {
            Draw2d.setFirstDraw(true);  //We set this so we do not draw lines for the previous position to the new zero.
            try {
                switch (_axis) {
                    case "x":
                        TinygDriver.getInstance().write(CommandManager.CMD_APPLY_ZERO_X_AXIS);
                        break;
                    case "y":
                        TinygDriver.getInstance().write(CommandManager.CMD_APPLY_ZERO_Y_AXIS);
                        break;
                    case "z":
                        TinygDriver.getInstance().write(CommandManager.CMD_APPLY_ZERO_Z_AXIS);
                        break;
                    case "a":
                        TinygDriver.getInstance().write(CommandManager.CMD_APPLY_ZERO_A_AXIS);
                        break;
                }
            } catch (Exception ex) {
                logger.error("Exception in handleZeroAxisClick for Axis: " + _axis + " " + ex.getMessage());
            }
        }
        tgfx.Main.postConsoleMessage("[+]Zeroed " + _axis.toUpperCase() + " Axis...\n");

    }

    @FXML
    private void handleDroMouseClick(MouseEvent me) {
        if (me.isSecondaryButtonDown()) { //Check to see if its a Right Click
            String t;
            String _axis;
            Lcd l;
            l = (Lcd) me.getSource();
            t = String.valueOf(l.idProperty().get().charAt(0));
        }
    }

    public static void setCNCMachineVisible(boolean t) {
        cncMachine.setVisible(t);

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /* add support for zmove
         * 
         */
//        assert zMoveScale != null : "fx:id=\"zMoveScale\" was not injected: check your FXML file 'Position.fxml'.";
//
//        // Set up ChoiceBox selection handler
//        zMoveScale.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number result) {
//                switch ((int) result) {
//                    case 0:
//                        zScale = 10.0f;
//                        break;
//                    case 1:
//                        zScale = 1.0f;
//                        break;
//                    case 2:
//                        zScale = 0.1f;
//                        break;
//                }
//            }
//        });

        timeStartDt = new Date();

        setCNCMachineVisible(false);  //We default to NOT display the CNC machine pane.  Once the serial port is connected we will show this.
        //This adds our CNC Machine (2d preview) to our display window
        if (!gcodePane.getChildren().contains(cncMachine)) {
            gcodePane.getChildren().add(cncMachine); // Add the cnc machine to the gcode pane
        }

        coordLocationGridPane.visibleProperty().bind(cncMachine.visibleProperty());  //This shows the coords when the cncMachine is visible.

        xLcd.valueProperty().bind(TinygDriver.getInstance().machine.getAxisByName("x").getMachinePositionSimple().subtract(TinygDriver.getInstance().machine.getAxisByName("x").getOffset()).divide(TinygDriver.getInstance().machine.gcodeUnitDivision));
        yLcd.valueProperty().bind(TinygDriver.getInstance().machine.getAxisByName("y").getMachinePositionSimple().subtract(TinygDriver.getInstance().machine.getAxisByName("y").getOffset()).divide(TinygDriver.getInstance().machine.gcodeUnitDivision));
        zLcd.valueProperty().bind(TinygDriver.getInstance().machine.getAxisByName("z").getMachinePositionSimple().subtract(TinygDriver.getInstance().machine.getAxisByName("z").getOffset()).divide(TinygDriver.getInstance().machine.gcodeUnitDivision));
        aLcd.valueProperty().bind(TinygDriver.getInstance().machine.getAxisByName("a").getMachinePositionSimple().subtract(TinygDriver.getInstance().machine.getAxisByName("a").getOffset()));
        velLcd.valueProperty().bind(TinygDriver.getInstance().machine.velocity);


        /*######################################
         * BINDINGS CODE
         ######################################*/
        gcodeTabControllerHBox.disableProperty().bind(TinygDriver.getInstance().connectionStatus.not());




        /*######################################
         * CHANGE LISTENERS
         ######################################*/


        xLcd.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldValue, Object newValue) {
                double tmp = TinygDriver.getInstance().machine.getAxisByName("y").getWorkPosition().doubleValue() + 5;
            }
        });


        yLcd.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldValue, Object newValue) {
                double tmp = TinygDriver.getInstance().machine.getAxisByName("y").getWorkPosition().doubleValue() + 5;
            }
        });

        TinygDriver.getInstance().machine.getGcodeUnitMode().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldValue, Object newValue) {
                String tmp = TinygDriver.getInstance().machine.getGcodeUnitMode().get();

//                gcodeUnitMode.getSelectionModel().select(TinygDriver.getInstance().m.getGcodeUnitModeAsInt());
                if (TinygDriver.getInstance().machine.getGcodeUnitModeAsInt() == 0) {
                    //A bug in the jfxtras does not allow for units to be updated.. we hide them if they are not mm
                    xLcd.lcdUnitVisibleProperty().setValue(false);
                    yLcd.lcdUnitVisibleProperty().setValue(false);
                    zLcd.lcdUnitVisibleProperty().setValue(false);
                    aLcd.lcdUnitVisibleProperty().setValue(false);
                    velLcd.lcdUnitVisibleProperty().setValue(false);
                } else {
                    xLcd.lcdUnitVisibleProperty().setValue(true);
                    yLcd.lcdUnitVisibleProperty().setValue(true);
                    zLcd.lcdUnitVisibleProperty().setValue(true);
                    aLcd.lcdUnitVisibleProperty().setValue(true);
                    velLcd.lcdUnitVisibleProperty().setValue(true);
                }
                tgfx.Main.postConsoleMessage("[+]Gcode Unit Mode Changed to: " + tmp + "\n");

                try {
                    TinygDriver.getInstance().serialWriter.setThrottled(true);
                    TinygDriver.getInstance().priorityWrite(CommandManager.CMD_QUERY_MOTOR_1_SETTINGS);
                    TinygDriver.getInstance().priorityWrite(CommandManager.CMD_QUERY_MOTOR_2_SETTINGS);
                    TinygDriver.getInstance().priorityWrite(CommandManager.CMD_QUERY_MOTOR_3_SETTINGS);
                    TinygDriver.getInstance().priorityWrite(CommandManager.CMD_QUERY_MOTOR_4_SETTINGS);

                    TinygDriver.getInstance().priorityWrite(CommandManager.CMD_QUERY_AXIS_X);
                    TinygDriver.getInstance().priorityWrite(CommandManager.CMD_QUERY_AXIS_Y);
                    TinygDriver.getInstance().priorityWrite(CommandManager.CMD_QUERY_AXIS_Z);
                    TinygDriver.getInstance().priorityWrite(CommandManager.CMD_QUERY_AXIS_A);
                    TinygDriver.getInstance().priorityWrite(CommandManager.CMD_QUERY_AXIS_B);
                    TinygDriver.getInstance().priorityWrite(CommandManager.CMD_QUERY_AXIS_C);
                    Thread.sleep(400);
                    TinygDriver.getInstance().serialWriter.setThrottled(false);
                } catch (Exception ex) {
                    logger.error("Error querying tg model state on gcode unit change.  Main.java binding section.");
                }
            }
        });

        cncMachine.heightProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal,
                    Object newVal) {
                logger.info("cncHeightChanged: " + cncMachine.getHeight());
//                Main.print(cncHeightString 
            }
        });
        cncMachine.maxWidthProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldValue, Object newValue) {
                handleMaxWithChange();
            }
        });
        cncMachine.maxHeightProperty()
                .addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldValue, Object newValue) {
                handleMaxHeightChange();
            }
        });





        /*######################################
         * GCODE FILE CODE
         ######################################*/
        data = FXCollections.observableArrayList();

        gcodeCol.setCellValueFactory(
                new PropertyValueFactory<GcodeLine, String>("codeLine"));
        GcodeLine n = new GcodeLine("Click open to load..", 0);

        gcodeView.getItems()
                .setAll(data);
        data.add(n);

        gcodeView.setItems(data);

        gcodeView.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton().equals(me.getButton().PRIMARY)) {
                    if (me.getClickCount() == 2) {
                        GcodeLine gcl = (GcodeLine) gcodeView.getSelectionModel().getSelectedItem();
                        if (TinygDriver.getInstance().isConnected().get()) {
                            logger.info("Double Clicked gcodeView " + gcl.getCodeLine());
                            try {
                                TinygDriver.getInstance().write(gcl.getGcodeLineJsonified());
                                tgfx.Main.postConsoleMessage(gcl.getGcodeLineJsonified());
                            } catch (Exception ex) {
                                java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                            }
                        } else {
                            logger.info("TinyG Not Connected not sending: " + gcl.getGcodeLineJsonified());
                            tgfx.Main.postConsoleMessage("TinyG Not Connected not sending: " + gcl.getGcodeLineJsonified());
                        }

                    }
                }
            }
        });
    }

    private Lcd getLcdByAxisName(String _axis) {
        switch (_axis) {
            case ("x"):
                return (xLcd);
            case ("y"):
                return (yLcd);
            case ("z"):
                return (zLcd);
            case ("a"):
                return (aLcd);
            case ("vel"):
                return (velLcd);
        }
        return (null);
    }

    @FXML
    private void handleZeroSystem(ActionEvent evt) {
        cncMachine.zeroSystem();
    }

    @FXML
    private void handlePauseResumeAct(ActionEvent evt) throws Exception {
        if ("Pause".equals(pauseResume.getText())) {
            pauseResume.setText("Resume");
            TinygDriver.getInstance().priorityWrite(CommandManager.CMD_APPLY_PAUSE);

        } else {
            pauseResume.setText("Pause");
            TinygDriver.getInstance().priorityWrite(CommandManager.CMD_APPLY_RESUME);
        }
    }

    @FXML
    private void handleClearScreen(ActionEvent evt) {
        tgfx.Main.postConsoleMessage("[+]Clearing Screen...\n");
        cncMachine.clearScreen();
        Draw2d.setFirstDraw(true);  //clear this so our first line added draws correctly
    }

    @FXML
    private void handleReset(ActionEvent evt) throws Exception {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    TinygDriver.getInstance().serialWriter.clearQueueBuffer();
                    TinygDriver.getInstance().priorityWrite(CommandManager.CMD_APPLY_RESET); //This sends the 0x18 byte



                    //We disable everything while waiting for theboard to reset
//                    topAnchorPane.setDisable(true);
//                    topTabPane.setDisable(true);

//                    Thread.sleep(8000);
//                    onConnectActions();
                    tgfx.Main.postConsoleMessage("[!]Resetting TinyG....\n.");
                    TinygDriver.getInstance().serialWriter.notifyAck();
                    TinygDriver.getInstance().serialWriter.clearQueueBuffer();
                    cncMachine.clearScreen();
                    isSendingFile.set(false); //We set this to false to allow us to jog again

                } catch (Exception ex) {
                    logger.error("handleReset " + ex.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleStop(ActionEvent evt) throws Exception {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {

                    logger.info("[!]Stopping Job Clearing Serial Queue...\n");
                    CommandManager.stopTinyGMovement();
                    isSendingFile.set(false); //We set this to false to allow us to jog again


                } catch (Exception ex) {
                    logger.error("handleStop " + ex.getMessage());
                }
            }
        });
    }
    static int test = 1;

    @FXML
    static void handleTestButton(ActionEvent evt) throws Exception {
        //logger.info("Test Button....");

        updateProgress(test);
        test += 5;

        //tgfx.Main.postConsoleMessage("Test!");
        //timeElapsedTxt.setText("hello");

//        Iterator ii = null;
//        Line l;
//        cncMachine.getChildren().iterator();
//        while(ii.hasNext()){
//            l = (Line) ii.next();
//            
//        }
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
                        tgfx.Main.postConsoleMessage("[!]File Sending Task Killed....\n");
                        break;
                    } else {
                        if (_gcl.getCodeLine().equals("")) {
                            //Blank Line.. Passing.. 
                            continue;
                        }

                        if (_gcl.getCodeLine().toLowerCase().contains("(")) {
                            TinygDriver.getInstance().write("**COMMENT**" + _gcl.getCodeLine());
//                            tgfx.Main.postConsoleMessage("GCODE COMMENT:" + _gcl.getCodeLine());
                            continue;

                        }

                        line.setLength(0);
                        line.append("{\"gc\":\"").append(_gcl.getCodeLine()).append("\"}\n");
                        TinygDriver.getInstance().write(line.toString());

                    }
                }
                TinygDriver.getInstance().write("**FILEDONE**");
                return true;
            }
        };
    }

    public static void setIsFileSending(boolean flag) {
        isSendingFile.set(flag);
    }

    @FXML
    private void handleRunFile(ActionEvent evt) {
        if (!isSendingFile.get()) {
            isSendingFile.set(true); //disables jogging while file is running
            taskActive = true; //Set the thread condition to start
            Task fileSend = fileSenderTask();
            Thread fsThread = new Thread(fileSend);
            fsThread.setName("FileSender");
            timeStartDt = new Date();
//            updateProgress(1);
            fsThread.start();
        }
    }

    public synchronized boolean isTaskActive() {
        return taskActive;
    }

    public synchronized void setTaskActive(boolean boolTask) {
        taskActive = boolTask;
    }

    @FXML
    private void handleOpenFile(ActionEvent event) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
//                logger.debug("handleOpenFile");

                try {
                    tgfx.Main.postConsoleMessage("[+]Loading a gcode file.....\n");
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
                            //gcodesList.appendText(strLine + "\n");
                            if (!strLine.toUpperCase().startsWith("N")) {
                                strLine = "N" + String.valueOf(_linenumber) + " " + strLine;
                            }
                            if (normalizeGcodeLine(strLine)) {
                                data.add(new GcodeLine(strLine, _linenumber));
                                _linenumber++;
                            } else {
                                Main.postConsoleMessage("ERROR: Your gcode file contains an invalid character.. Either !,% or ~. Remove this character and try again.");
                                Main.postConsoleMessage("  Line " + _linenumber);
                                data.clear(); //Remove all other previous entered lines
                                break;
                            }

                        }
                    }
                    totalGcodeLines = _linenumber;
//                    logger.info("File Loading Complete");
                } catch (FileNotFoundException ex) {
                    logger.error("File Not Found.");
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                }
            }
        });
    }

    private boolean normalizeGcodeLine(String gcl) {
        byte[] tmpLine = gcl.getBytes();
        //0x21 = !
        //0x18 = Ctrl-X
        //0x7e = ~
        //0x25 = %
        //These are considered bad bytes in gcode files.  These will trigger tinyg to throw interrupts

        for (int i = 0; i < tmpLine.length; i++) {
        }

        for (int i = 0; i < BAD_BYTES.length; i++) {
            for (int j = 0; j < gcl.length(); j++) {
                if (gcl.charAt(j) == BAD_BYTES[i]) {
                    //Bad Byte Found
                    logger.error("Bad Byte Char Detected: " + BAD_BYTES[i]);
                    return false;
                }
            }
        }
        return true;
    }

    /*######################################
     * EVENT LISTENERS CODE
     ######################################*/
    public void handleMaxHeightChange() {
        if (gcodePane.getWidth() - TinygDriver.getInstance().machine.getAxisByName("x").getTravelMaxSimple().get() < gcodePane.getHeight() - TinygDriver.getInstance().machine.getAxisByName("y").getTravelMaxSimple().get()) {
            //X is longer use this code
            if (TinygDriver.getInstance().machine.getGcodeUnitModeAsInt() == 0) {  //INCHES
                scaleAmount = ((gcodePane.heightProperty().get() / (TinygDriver.getInstance().machine.getAxisByName("y").getTravelMaxSimple().get() * 25.4))) * .80;  //%80 of the scale;
            } else { //MM
                scaleAmount = ((gcodePane.heightProperty().get() / TinygDriver.getInstance().machine.getAxisByName("y").getTravelMaxSimple().get())) * .80;  //%80 of the scale;
            }
        } else {
            //Y is longer use this code
            if (TinygDriver.getInstance().machine.getGcodeUnitModeAsInt() == 0) {  //INCHES
                scaleAmount = ((gcodePane.heightProperty().get() / (TinygDriver.getInstance().machine.getAxisByName("y").getTravelMaxSimple().get() * 25.4))) * .80;  //%80 of the scale;
            } else { //MM
                scaleAmount = ((gcodePane.heightProperty().get() / TinygDriver.getInstance().machine.getAxisByName("y").getTravelMaxSimple().get())) * .80;  //%80 of the scale;
            }
//                    scaleAmount = ((gcodePane.heightProperty().get() / TinygDriver.getInstance().m.getAxisByName("y").getTravelMaxSimple().get())) * .80;  //%80 of the scale;
        }
        cncMachine.autoScaleWorkTravelSpace(scaleAmount);
        //        widthSize.textProperty().bind( Bindings.format("%s",  cncMachine.widthProperty().divide(TinygDriver.getInstance().m.gcodeUnitDivision).asString().concat(TinygDriver.getInstance().m.getGcodeUnitMode())    ));  //.asString().concat(TinygDriver.getInstance().m.getGcodeUnitMode().get()));

//        heightSize.setText(decimalFormat.format(TinygDriver.getInstance().m.getAxisByName("y").getTravel_maximum()) + " " + TinygDriver.getInstance().m.getGcodeUnitMode().getValue());


    }

    public void handleMaxWithChange() {
        //This is for the change listener to call for Max Width Change on the CNC Machine
        if (gcodePane.getWidth() - TinygDriver.getInstance().machine.getAxisByName("x").getTravelMaxSimple().get() < gcodePane.getHeight() - TinygDriver.getInstance().machine.getAxisByName("y").getTravelMaxSimple().get()) {
            //X is longer use this code
            if (TinygDriver.getInstance().machine.getGcodeUnitModeAsInt() == 0) {  //INCHES
                scaleAmount = ((gcodePane.heightProperty().get() / (TinygDriver.getInstance().machine.getAxisByName("y").getTravelMaxSimple().get() * 25.4))) * .80;  //%80 of the scale;
            } else { //MM
                scaleAmount = ((gcodePane.heightProperty().get() / TinygDriver.getInstance().machine.getAxisByName("y").getTravelMaxSimple().get())) * .80;  //%80 of the scale;
            }
        } else {
            //Y is longer use this code
            if (TinygDriver.getInstance().machine.getGcodeUnitModeAsInt() == 0) {  //INCHES
                scaleAmount = ((gcodePane.heightProperty().get() / (TinygDriver.getInstance().machine.getAxisByName("y").getTravelMaxSimple().get() * 25.4))) * .80;  //%80 of the scale;
            } else { //MM
                scaleAmount = ((gcodePane.heightProperty().get() / TinygDriver.getInstance().machine.getAxisByName("y").getTravelMaxSimple().get())) * .80;  //%80 of the scale;
            }
        }
        cncMachine.autoScaleWorkTravelSpace(scaleAmount);
//        widthSize.setText(decimalFormat.format(TinygDriver.getInstance().m.getAxisByName("x").getTravel_maximum()) + " " + TinygDriver.getInstance().m.getGcodeUnitMode().getValue());

    }

    // Scroll Gcode table view to specified line, show elapsed and remaining time
    public static void updateProgress(int lineNum) {

        if (isSendingFile.get() && lineNum > 0) {
//            gcodeView.scrollTo(lineNum);

            // Show elapsed and remaining time
            Date currentTimeDt = new Date();  // Get current time
            long elapsed = (currentTimeDt.getTime() - timeStartDt.getTime());
            float rate = elapsed / lineNum;
            long remain = (long) ((totalGcodeLines - lineNum) * rate);  // remaining lines * secs per line

            timeElapsedTxt.setText(String.format("%02d:%02d", elapsed / 60000, (elapsed / 1000) % 60));
            timeLeftTxt.setText(String.format("%02d:%02d", remain / 60000, (remain / 1000) % 60));
        }
    }
}
