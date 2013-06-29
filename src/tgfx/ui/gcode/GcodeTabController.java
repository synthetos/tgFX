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
import java.util.ResourceBundle;
import javafx.application.Platform;
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
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
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

    private double scaleAmount;
    private int buildNumber;
    private String buildDate;
    private boolean taskActive = false;
    static final Logger logger = Logger.getLogger(GcodeTabController.class);
    public ObservableList data; //List to store the gcode file
    public static StackPane gcodePane = new StackPane(); //Holds CNCMachine  This needs to be before CNCMachine()
    private static CNCMachine cncMachine = new CNCMachine();
    /*  ######################## FXML ELEMENTS ############################*/
    @FXML
    private Lcd xLcd, yLcd, zLcd, aLcd, velLcd; //DRO Lcds
    @FXML
    StackPane machineWorkspace;
    @FXML
    private Pane previewPane;
    @FXML
    private TableColumn<GcodeLine, String> gcodeCol;
    @FXML
    private TableView gcodeView;
    @FXML
    private static TextArea console;
    @FXML
    private Button Run, Connect, gcodeZero, btnClearScreen, pauseResume, btnTest, btnHandleInhibitAllAxis;

    /**
     * Initializes the controller class.
     */
    public GcodeTabController() {
        logger.info("Gcode Controller Loaded");

    }

    public static void drawCanvasUpdate() {
        if (TgfxSettingsController.isDrawPreview()) {
            cncMachine.drawLine(TinygDriver.getInstance().m.getMotionMode().get(), TinygDriver.getInstance().m.getVelocity());
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
        if (TinygDriver.getInstance().isConnected()) {
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
        if (TinygDriver.getInstance().isConnected()) {
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
        if (TinygDriver.getInstance().isConnected()) {
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


        setCNCMachineVisible(false);  //We default to NOT display the CNC machine pane.  Once the serial port is connected we will show this.
        //This adds our CNC Machine (2d preview) to our display window
        if (!gcodePane.getChildren().contains(cncMachine)) {
            gcodePane.getChildren().add(cncMachine); // Add the cnc machine to the gcode pane
        }

        xLcd.valueProperty().bind(TinygDriver.getInstance().m.getAxisByName("x").getMachinePositionSimple().subtract(TinygDriver.getInstance().m.getAxisByName("x").getOffset()).divide(TinygDriver.getInstance().m.gcodeUnitDivision));
        yLcd.valueProperty().bind(TinygDriver.getInstance().m.getAxisByName("y").getMachinePositionSimple().subtract(TinygDriver.getInstance().m.getAxisByName("y").getOffset()).divide(TinygDriver.getInstance().m.gcodeUnitDivision));
        zLcd.valueProperty().bind(TinygDriver.getInstance().m.getAxisByName("z").getMachinePositionSimple().subtract(TinygDriver.getInstance().m.getAxisByName("z").getOffset()).divide(TinygDriver.getInstance().m.gcodeUnitDivision));
        aLcd.valueProperty().bind(TinygDriver.getInstance().m.getAxisByName("a").getMachinePositionSimple().subtract(TinygDriver.getInstance().m.getAxisByName("a").getOffset()));
        velLcd.valueProperty().bind(TinygDriver.getInstance().m.velocity);


        /*######################################
         * CHANGE LISTENERS
         ######################################*/


        xLcd.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldValue, Object newValue) {
                double tmp = TinygDriver.getInstance().m.getAxisByName("y").getWorkPosition().doubleValue() + 5;
            }
        });


        yLcd.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldValue, Object newValue) {
                double tmp = TinygDriver.getInstance().m.getAxisByName("y").getWorkPosition().doubleValue() + 5;
            }
        });

        TinygDriver.getInstance().m.getGcodeUnitMode().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldValue, Object newValue) {
                String tmp = TinygDriver.getInstance().m.getGcodeUnitMode().get();

//                gcodeUnitMode.getSelectionModel().select(TinygDriver.getInstance().m.getGcodeUnitModeAsInt());
                if (TinygDriver.getInstance().m.getGcodeUnitModeAsInt() == 0) {
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
                System.out.println("cncHeightChanged: " + cncMachine.getHeight());
//                System.out.println(cncHeightString 
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
                        if (TinygDriver.getInstance().isConnected()) {
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
        tgfx.Main.postConsoleMessage("[+]Clearning Screen...\n");
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
                    TinygDriver.getInstance().priorityWrite(CommandManager.CMD_APPLY_PAUSE);

                    TinygDriver.getInstance().serialWriter.clearQueueBuffer();
                    TinygDriver.getInstance().priorityWrite(CommandManager.CMD_APPLY_QUEUE_FLUSH);
//                    TinygDriver.getInstance().priorityWrite(CommandManager.CMD_APPLY_RESUME);
                    tgfx.Main.postConsoleMessage("[!]Stopping Job Clearing Serial Queue...\n");



                } catch (Exception ex) {
                    logger.error("handleStop " + ex.getMessage());
                }
            }
        });
    }

    @FXML
    void handleTestButton(ActionEvent evt) throws Exception {
        logger.info("Test Button....");
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
                            tgfx.Main.postConsoleMessage("GCODE COMMENT:" + _gcl.getCodeLine());

                        }

                        line.setLength(0);
                        line.append("{\"gc\":\"").append(_gcl.getCodeLine()).append("\"}\n");
                        TinygDriver.getInstance().write(line.toString());
                    }
                }
                return true;
            }
        };
    }
    
    

    @FXML
    private void handleRunFile(ActionEvent evt) {
        taskActive = true; //Set the thread condition to start
        Task fileSend = fileSenderTask();
        Thread fsThread = new Thread(fileSend);
        fsThread.setName("FileSender");
        fsThread.start();

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
                logger.debug("handleOpenFile");

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
                            data.add(new GcodeLine(strLine, _linenumber));
                            _linenumber++;
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

    /*######################################
     * EVENT LISTENERS CODE
     ######################################*/
    public void handleMaxHeightChange() {
        if (gcodePane.getWidth() - TinygDriver.getInstance().m.getAxisByName("x").getTravelMaxSimple().get() < gcodePane.getHeight() - TinygDriver.getInstance().m.getAxisByName("y").getTravelMaxSimple().get()) {
            //X is longer use this code
            if (TinygDriver.getInstance().m.getGcodeUnitModeAsInt() == 0) {  //INCHES
                scaleAmount = ((gcodePane.heightProperty().get() / (TinygDriver.getInstance().m.getAxisByName("y").getTravelMaxSimple().get() * 25.4))) * .80;  //%80 of the scale;
            } else { //MM
                scaleAmount = ((gcodePane.heightProperty().get() / TinygDriver.getInstance().m.getAxisByName("y").getTravelMaxSimple().get())) * .80;  //%80 of the scale;
            }
        } else {
            //Y is longer use this code
            if (TinygDriver.getInstance().m.getGcodeUnitModeAsInt() == 0) {  //INCHES
                scaleAmount = ((gcodePane.heightProperty().get() / (TinygDriver.getInstance().m.getAxisByName("y").getTravelMaxSimple().get() * 25.4))) * .80;  //%80 of the scale;
            } else { //MM
                scaleAmount = ((gcodePane.heightProperty().get() / TinygDriver.getInstance().m.getAxisByName("y").getTravelMaxSimple().get())) * .80;  //%80 of the scale;
            }
//                    scaleAmount = ((gcodePane.heightProperty().get() / TinygDriver.getInstance().m.getAxisByName("y").getTravelMaxSimple().get())) * .80;  //%80 of the scale;
        }
        cncMachine.autoScaleWorkTravelSpace(scaleAmount);
        //        widthSize.textProperty().bind( Bindings.format("%s",  cncMachine.widthProperty().divide(TinygDriver.getInstance().m.gcodeUnitDivision).asString().concat(TinygDriver.getInstance().m.getGcodeUnitMode())    ));  //.asString().concat(TinygDriver.getInstance().m.getGcodeUnitMode().get()));

//        heightSize.setText(decimalFormat.format(TinygDriver.getInstance().m.getAxisByName("y").getTravel_maximum()) + " " + TinygDriver.getInstance().m.getGcodeUnitMode().getValue());


    }

    public void handleMaxWithChange() {
        //This is for the change listener to call for Max Width Change on the CNC Machine
        if (gcodePane.getWidth() - TinygDriver.getInstance().m.getAxisByName("x").getTravelMaxSimple().get() < gcodePane.getHeight() - TinygDriver.getInstance().m.getAxisByName("y").getTravelMaxSimple().get()) {
            //X is longer use this code
            if (TinygDriver.getInstance().m.getGcodeUnitModeAsInt() == 0) {  //INCHES
                scaleAmount = ((gcodePane.heightProperty().get() / (TinygDriver.getInstance().m.getAxisByName("y").getTravelMaxSimple().get() * 25.4))) * .80;  //%80 of the scale;
            } else { //MM
                scaleAmount = ((gcodePane.heightProperty().get() / TinygDriver.getInstance().m.getAxisByName("y").getTravelMaxSimple().get())) * .80;  //%80 of the scale;
            }
        } else {
            //Y is longer use this code
            if (TinygDriver.getInstance().m.getGcodeUnitModeAsInt() == 0) {  //INCHES
                scaleAmount = ((gcodePane.heightProperty().get() / (TinygDriver.getInstance().m.getAxisByName("y").getTravelMaxSimple().get() * 25.4))) * .80;  //%80 of the scale;
            } else { //MM
                scaleAmount = ((gcodePane.heightProperty().get() / TinygDriver.getInstance().m.getAxisByName("y").getTravelMaxSimple().get())) * .80;  //%80 of the scale;
            }
        }
        cncMachine.autoScaleWorkTravelSpace(scaleAmount);
//        widthSize.setText(decimalFormat.format(TinygDriver.getInstance().m.getAxisByName("x").getTravel_maximum()) + " " + TinygDriver.getInstance().m.getGcodeUnitMode().getValue());

    }
}
