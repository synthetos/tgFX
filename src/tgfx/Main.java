/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import tgfx.external.SocketMonitor;
import tgfx.render.Draw2d;
import tgfx.system.Machine;

/**
 *
 * @author ril3y
 */
public class Main implements Initializable, Observer {

    private static final String CMD_GET_stateUS_REPORT = "{\"sr\":\"\"}\n";
//    public Machine m = new Machine();
    private JdomParser JDOM = new JdomParser(); //JSON Object Parser1
    private TinygDriver tg = TinygDriver.getInstance();
    //private SerialDriver ser = SerialDriver.getInstance();
    /**
     * FXML UI Components
     */
    @FXML
    private Button Con, Run, Connect, gcodeZero, btnClearScreen;
    @FXML
    TextArea console;
    @FXML
    TextField input;
    @FXML
    private Label label, xAxisVal, yAxisVal, zAxisVal, aAxisVal, srMomo, srState, srVelo;
    @FXML
    ListView gcodesList;
    @FXML
    ChoiceBox serialPorts;
    @FXML
    Button pauseResume;
    @FXML
    Group canvsGroup;  //Drawing Canvas
    @FXML
    VBox bottomConsoleHBox;
    @FXML
    HBox canvas;
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

                    FileChooser fc = new FileChooser();
                    fc.setTitle("Open GCode File");
                    fc.setInitialDirectory(new File("c:\\"));
                    File f = fc.showOpenDialog(null);
                    FileInputStream fstream = new FileInputStream(f);
                    DataInputStream in = new DataInputStream((fstream));
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String strLine;

                    gcodesList.getItems().clear();
                    //Clear the list if there was a previous file loaded

                    while ((strLine = br.readLine()) != null) {
                        final TextField tmpLbl = new TextField(strLine);
                        tmpLbl.setEditable(false);
                        Tooltip tp = new Tooltip();
                        tp.setText("To edit a value double click the cell.\nClick enter to apply the new value.");
                        tmpLbl.setTooltip(tp);


                        tmpLbl.setOnMouseClicked(new EventHandler<MouseEvent>() {

                            public void handle(MouseEvent me) {
                                if (me.getButton() == me.getButton().PRIMARY && me.getClickCount() == 2) {
                                    String tmpValue = tmpLbl.getText();
                                    tmpLbl.setEditable(true);


                                }
                            }
                        });

                        tmpLbl.setOnKeyPressed(new EventHandler<KeyEvent>() {

                            public void handle(KeyEvent kevt) {
                                if (kevt.getCode() == KeyCode.ENTER) {
                                    tmpLbl.setEditable(false);
                                    System.out.println("[+]Applied new gcode value");
                                }
//                                else if(kevt.getCode()== KeyCode.ESCAPE){
//                                    tmpLbl.setText(tmpValue);
//                                }
                            }
                        });


                        //final Label tmpLbl = new Label(strLine);
                        gcodesList.getItems().add(tmpLbl);

                        System.out.println(strLine);
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
                tg.write("{\"gc\":\"g92x0y0z0a0\"}\n");
                tg.write(CMD_GET_stateUS_REPORT);
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
                ObservableList<TextField> gcodeProgramList = gcodesList.getItems();
                gcodeProgramList = gcodesList.getItems();
                String line;

                for (TextField tf : gcodeProgramList) {

                    if (tf.getText().startsWith("(") || tf.getText().equals("")) {
                        continue;
                    } else {
                        line = new String("{\"gc\":\"" + tf.getText() + "\"}" + "\n");

                        if (tg.getClearToSend() && !tg.isPAUSED()) {
                            if (tg.isConnected()) {
                                tg.write(line);
                            } else {
                                console.appendText("[!]Serial Port is not Connected!\n");
                            }


                        } else if (tg.isPAUSED()) {

                            while (tg.isPAUSED()) {
                                //Infinite Loop
                            }
                            tg.write(line);
                        } else {

                            int count = 0;
                            while (!tg.getClearToSend()) {
                                //Not ready yet
                                Thread.sleep(1);
                            }
                            tg.write(line);
                        }
                    }
                }
                console.appendText("[+] Sending File Complete\n");
                return true;

            }
        };
    }

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
            tg.write(tg.CMD_DISABLE_LOCAL_ECHO);
            //DISABLE LOCAL ECHO!! THIS IS A MUST OR NOTHING WORKS
            tg.write("{\"ex\":0}\n");
            tg.write(tg.CMD_SET_STATUS_UPDATE_INTERVAL); //Set to every 50ms
            tg.write(tg.CMD_GET_STATUS_REPORT);  //If TinyG current positions are other than zero
            //this will poll for the new values and update the GUI
            tg.write(tg.CMD_GET_OK_PROMPT);  //This is required as "status reports" do not return an "OK" msg

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
            tg.disconnect();
            if (!tg.isConnected()) {
                console.appendText("[+]Disconnected from " + tg.getPortName() + " Serial Port Successfully.\n");
                Connect.setText("Connect");
            }

        }
    }

    @FXML
    private void clearScreen(ActionEvent evt) {
        console.appendText("[+]Clearning Screen...\n");
        canvsGroup.getChildren().clear();



//        path.getElements().clear();
//        MoveTo mt = new MoveTo(400, 400);
//        path.getElements().add(mt);

    }

    private void handleTilda() {
        //                ==============HIDE CONSOLE CODE==============
        System.out.println("TILDA");

        if (bottomConsoleHBox.isVisible()) {
            bottomConsoleHBox.setVisible(false);


        } else {
            bottomConsoleHBox.setVisible(true);
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

            } else if (keyEvent.getCode() == KeyCode.F5) {
                String msg = new String("F5 Key Pressed - Getting Machine Settings\n");
                console.appendText(msg);
                System.out.println(msg);
                try {
//                    tg.requestStatusUpdate();
//                    tg.getMachineSettings();
                    tg.getMotorSettings(1);
                    tg.getMotorSettings(2);
                    tg.getMotorSettings(3);
//                    tg.getMotorSettings(3);

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

    @FXML
    private void gcodeProgramClicks(MouseEvent me) {
        TextField tField = (TextField) gcodesList.getSelectionModel().getSelectedItem();
        if (me.getButton() == me.getButton().SECONDARY) {
//            tg.write("{\"gc\":\"" + lbl.getText() + "\"}\n");
            System.out.println("RIGHT CLICKED");


        } else if (me.getButton() == me.getButton().PRIMARY && me.getClickCount() == 2) {
            System.out.println("double clicked");
            tField.setEditable(true);

            //if (lbl.getParent().getStyleClass().contains("breakpoint")) {
//                lbl.getParent().getStyleClass().remove("breakpoint");
//                console.appendText("BREAKPOINT REMOVED: " + lbl.getText() + "\n");
//                System.out.println("BREAKPOINT REMOVED");
//            } else {
//
//                System.out.println("DOUBLE CLICKED");
//                lbl.getStyleClass().removeAll(null);
//                lbl.getParent().getStyleClass().add("breakpoint");
//                System.out.println("BREAKPOINT SET");
//                console.appendText("BREAKPOINT SET: " + lbl.getText() + "\n");
//            };
        }
    }

    @FXML
    private void handleEnter(ActionEvent event) throws Exception {
        tg.write(input.getText() + "\n");
        System.out.println("Entered");
    }

    private Task initRemoteServer() {
        return new Task() {

            @Override
            protected Object call() throws Exception {
                SocketMonitor sm = new SocketMonitor();
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
        
        if (tg.m.getAxisByName("Z").getWork_position() > 0) {
            l = null;
        } else {
            l.setStrokeWidth(Draw2d.getStrokeWeight());
        }
        xPrevious = newX;
        yPrevious = newY;

        if(l != null){
            canvsGroup.getChildren().add(l);
        }

    }

    private void updateGuiState(String line) {
        final String l = line;
        if (l.contains("msg")) {
            //Pass this on by..
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
                if (ROUTING_KEY == "PLAIN") {
                    console.appendText((String) ROUTING_KEY + "\n");

                } else if (ROUTING_KEY == "STATUS_REPORT") {
//                    console.setText((String) MSG[1] + "\n");
                    updateGuiState(ROUTING_KEY);
                } else if (ROUTING_KEY.contains("ERROR")) {
                    console.appendText(ROUTING_KEY);
                }

            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SocketMonitor sm;


        Task SocketListner = this.initRemoteServer();
//        new Thread(SocketListner).start();

        tg.addObserver(this);
        this.reScanSerial();//Populate our serial ports

        //Move to middle of canvas
//        MoveTo mt = new MoveTo(400, 400);


//        path.getElements().add(mt);








    }
}
