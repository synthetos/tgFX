/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import java.io.*;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;

import tgfx.external.SocketMonitor;
import tgfx.system.Machine;

/**
 *
 * @author ril3y
 */
public class Main implements Initializable, Observer {

    private static final String CMD_GET_STATUS_REPORT = "{\"sr\":\"\"}\n";
    public Machine m = new Machine();
    private JdomParser JDOM = new JdomParser(); //JSON Object Parser
    private SerialDriver ser = SerialDriver.getInstance();
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
    private Label label, xAxisVal, yAxisVal, zAxisVal, aAxisVal, srMomo, srStat;
    @FXML
    ListView gcodesList;
    @FXML
    ChoiceBox serialPorts;
    @FXML
    Button pauseResume;
    @FXML
    Region canvas;  //Drawing Canvas
    @FXML
    Path path;
    /**
     * Drawing Code Vars
     *
     */
    float x = 0;
    float y = 0;
    float z = 0;
    float vel = 0;
    String stat = new String();
    LineTo xl = new LineTo();
    LineTo y1 = new LineTo();
    LineTo z1 = new LineTo();
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

                    boolean flag = true;
                    while ((strLine = br.readLine()) != null) {

                        final Label tmpLbl = new Label(strLine);
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
    private void pauseResumeAct(ActionEvent evt) throws Exception {
        if ("Pause".equals(pauseResume.getText())) {
            pauseResume.setText("Resume");
            ser.setPAUSED(true);
            ser.priorityWrite("!\n");
        } else {
            pauseResume.setText("Pause");
            ser.priorityWrite("~\n");
            ser.setPAUSED(false);

        }
    }

    @FXML
    private void zeroSystem(ActionEvent evt) {
        if (ser.isConnected() && ser.getClearToSend()) {
            try {
                ser.write("{\"gc\":\"g92x0y0z0a0\"}\n");
                ser.write(CMD_GET_STATUS_REPORT);
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
                ObservableList<Label> gcodeProgramList = gcodesList.getItems();
                gcodeProgramList = gcodesList.getItems();
                String line;

                for (Label l : gcodeProgramList) {

                    if (l.getText().startsWith("(")) {
                        continue;
                    } else {
                        line = new String("{\"gc\":\"" + l.getText() + "\"}" + "\n");

                        if (ser.getClearToSend() && !ser.isPAUSED()) {
                            if (ser.isConnected()) {
                                ser.write(line);
                            } else {
                                console.appendText("[!]Serial Port is not Connected!\n");

                            }


                        } else if (ser.isPAUSED()) {

                            while (ser.isPAUSED()) {
                                //Infinite Loop
                            }
                            ser.write(line);
                        } else {

                            int count = 0;
                            while (!ser.getClearToSend()) {
                                //Not ready yet
                                Thread.sleep(1);
                            }
                            ser.write(line);
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
        portArray = ser.listSerialPorts();


        for (String p : portArray) {
            serialPorts.getItems().add(p);
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
            ser.initialize(serialPortSelected, 115200);
            if (ser.isConnected()) {

                console.appendText("[+]Connected to " + serialPortSelected + " Serial Port Successfully.\n");

                //DISABLE LOCAL ECHO!! THIS IS A MUST OR NOTHING WORKS
                ser.write("{\"ee\":0}\n");
                //DISABLE LOCAL ECHO!! THIS IS A MUST OR NOTHING WORKS

                ser.write(CMD_GET_STATUS_REPORT);
                Connect.setText("Disconnect");
            }
        } else {
            ser.disconnect();
            if (!ser.isConnected()) {
                console.appendText("[+]Disconnected from " + ser.serialPort.getName() + " Serial Port Successfully.\n");
                Connect.setText("Connect");
            }

        }
    }

    @FXML
    private void clearScreen(ActionEvent evt) {
        console.appendText("[+]Clearning Screen...\n");
        path.getElements().clear();
        MoveTo mt = new MoveTo(400, 400);
        path.getElements().add(mt);

    }

    @FXML
    private void handleEnter(ActionEvent event) throws Exception {
        ser.write(input.getText() + "\n");
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

    public void drawLine(int moveType, float vel) {

        xl.setX(Float.parseFloat(xAxisVal.getText()) + 400);
        y1.setY(Float.parseFloat(yAxisVal.getText()) + 400);
        LineTo tmpL = new LineTo();

        if (moveType == 0) {
            //G0 Move
        } else {
            //G1 Move
        }
        tmpL.setX(xl.getX());
        tmpL.setY(y1.getY());
        path.getElements().add(tmpL);

    }

    private void parseJson(String line) {
        final String l = line;
        if (l.contains("msg")) {
            //Pass this on by..
        } else {
            Platform.runLater(new Runnable() {

                String momo;
                String stat;

                public void run() {
                    //We are now back in the EventThread and can update the GUI
                    try {
                        JsonRootNode json = JDOM.parse(l);

                        xAxisVal.setText(json.getNode("sr").getNode("posx").getText());
                        yAxisVal.setText(json.getNode("sr").getNode("posy").getText());
                        zAxisVal.setText(json.getNode("sr").getNode("posz").getText());
                        aAxisVal.setText(json.getNode("sr").getNode("posa").getText());


                        //Set running state
                        stat = json.getNode("sr").getNode("stat").getText();
                        //[stat] machine_state      - 0=reset, 2=stop, 3=end, 4=run, 5=hold, 6=homing 

                        if (stat.equals("0")) {
                            srStat.setText("RESET");
                        } else if (stat.equals("2")) {
                            srStat.setText("STOP");
                        } else if (stat.equals("3")) {
                            srStat.setText("END");
                        } else if (stat.equals("4")) {
                            srStat.setText("RUN");
                        } else if (stat.equals("5")) {
                            srStat.setText("HOLD");
                        } else if (stat.equals("6")) {
                            srStat.setText("HOMING");
                        }


                        momo = json.getNode("sr").getNode("momo").getText();
                        //Set the motion mode
                        if (momo.equals("0")) {
                            srMomo.setText("TRAVERSE");
                        } else if (momo.equals("1")) {
                            srMomo.setText("STRAIGHT");
                        } else if (momo.equals("2")) {
                            srMomo.setText("CW ARC");
                        } else {
                            srMomo.setText("CCW ARC");
                        }


                        //Parse the veloicity 


                        drawLine(Integer.parseInt(momo), Float.parseFloat(json.getNode("sr").getNode("posa").getText()));


                    } catch (argo.saj.InvalidSyntaxException ex) {
                        System.out.println("[!]ParseJson Exception: " + ex.getMessage());
                    } catch (argo.jdom.JsonNodeDoesNotMatchPathElementsException ex) {
                        System.out.println("[!]ParseJson Exception: " + ex.getMessage());
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
        final String[] MSG = (String[]) arg;

        //We have to run the updates likes this.
        //https://forums.oracle.com/forums/thread.jspa?threadID=2298778&start=0 for more information
        Platform.runLater(new Runnable() {

            public void run() {
                // we are now back in the EventThread and can update the GUI
                if (MSG[0] == "PLAIN") {
                    console.appendText((String) MSG[1] + "\n");

                } else if (MSG[0] == "JSON") {
//                    console.setText((String) MSG[1] + "\n");
                    parseJson(MSG[1]);
                }

            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SocketMonitor sm;


        Task SocketListner = this.initRemoteServer();
//        new Thread(SocketListner).start();

        ser.addObserver(this);
        this.reScanSerial();//Populate our serial ports

        //Move to middle of canvas
        MoveTo mt = new MoveTo(400, 400);


        path.getElements().add(mt);





    }
}
