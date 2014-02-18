/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.ui.machinesettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import tgfx.Main;
import tgfx.tinyg.CommandManager;
import tgfx.tinyg.TinygDriver;

import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import org.json.JSONException;

/**
 * FXML Controller class
 *
 * @author rileyporter
 */
public class MachineSettingsController implements Initializable {

    private DecimalFormat decimalFormat = new DecimalFormat("################################.############################");
    private static final Logger logger = Logger.getLogger(MachineSettingsController.class);
    @FXML
    private ListView configsListView;
    @FXML
    private static ChoiceBox machineSwitchType, machineUnitMode;
    @FXML
    private ProgressBar configProgress;

    public static void updateGuiMachineSettings() {
        machineUnitMode.getSelectionModel().select(TinygDriver.getInstance().machine.getGcodeUnitModeAsInt());
        machineSwitchType.getSelectionModel().select(TinygDriver.getInstance().machine.getSwitchType());
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        populateConfigFiles();          //Populate all Config Files

    }

    private void populateConfigFiles() {

        String path = "configs";

        String files;
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                files = listOfFiles[i].getName();
                if (files.endsWith(".config") || files.endsWith(".json")) {
                    configsListView.getItems().add(files);
                }
            }
        }
    }

    @FXML
    private void handleSaveCurrentSettings(ActionEvent event) throws Exception {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                FileChooser fc = new FileChooser();
                fc.setInitialDirectory(new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "configs" + System.getProperty("file.separator")));
                fc.setTitle("Save Current TinyG Configuration");
                File f = fc.showSaveDialog(null);
                if (f.canWrite()) {
                }
            }
        });
    }

    @FXML
    private void handleImportConfig(ActionEvent event) throws Exception {
    }

    private void writeConfigValue(JSONObject j) throws Exception {

        String topLevelParent = new String();
        topLevelParent = (String) j.names().get(0);
        Iterator it = j.getJSONObject(topLevelParent).keys();

        while (it.hasNext()) {
            String k = (String) it.next();
            Double value = (Double) j.getJSONObject(topLevelParent).getDouble(k);
            System.out.println("This is the value " + k + " " + decimalFormat.format(value));
            //value = Double.valueOf(decimalFormatjunctionDeviation.format(value));

            String singleJsonSetting = new String("{\"" + topLevelParent + k + "\":" + value + "}\n");
            TinygDriver.getInstance().write(singleJsonSetting);
            Thread.sleep(400);

        }

    }

    private int getElementCount(JSONObject j) throws JSONException {
        //We are getting a count of all the values we need to send from the config file.
        if (j.has("name")) { //We do not want the name of the config to count as stuff to write.
            return 0;
        } else {
            String topLevelParent = new String();
            topLevelParent = (String) j.names().get(0);
            return j.getJSONObject(topLevelParent).length();
        }
    }

    @FXML
    private void handleLoadConfig(ActionEvent event) throws Exception {
        //This function gets the config file selected and applys the settings onto tinyg.
//        if(configsListView.getSelectionModel().getSelectedIndex() < -1){
//            
//        }
        InputStream fis, fis2;
        final BufferedReader br, br2;

        //Why are we reading the file 2x?  It is to get the count of elemnts we need to write.. then writing each line... so we just do it 2x.
        File selected_config = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "configs" + System.getProperty("file.separator") + configsListView.getSelectionModel().getSelectedItem());
        fis = new FileInputStream(selected_config);
        fis2 = new FileInputStream(selected_config);
        br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
        br2 = new BufferedReader(new InputStreamReader(fis2, Charset.forName("UTF-8")));

//        
        Task task;
        task = new Task<Void>() {
            @Override
            public Void call() throws IOException, Exception {
                String line;
                int maxElements = 0;
                int currentElement = 0;
                String filename = new String();

                while ((line = br2.readLine()) != null) {
                    JSONObject j = new JSONObject(line);
                    maxElements = maxElements + getElementCount(j);

                }

                while ((line = br.readLine()) != null) {
                    if (TinygDriver.getInstance().isConnected().get()) {
                        if (line.startsWith("{\"name")) {
                            //This is the name of the CONFIG lets not write this to TinyG 
                            filename = line.split(":")[1];
                            tgfx.Main.postConsoleMessage("[+]Loading " + filename + " config into TinyG... Please Wait...");
                        } else {

                            JSONObject j = new JSONObject(line);

                            String topLevelParent;
                            topLevelParent = (String) j.names().get(0);
                            Iterator it = j.getJSONObject(topLevelParent).keys();

                            while (it.hasNext()) {
                                String k = (String) it.next();
                                Double value = (Double) j.getJSONObject(topLevelParent).getDouble(k);
                                System.out.println("This is the value " + k + " " + decimalFormat.format(value));
                                Main.postConsoleMessage("Applied: " + k + ":" + decimalFormat.format(value));
                                //value = Double.valueOf(decimalFormatjunctionDeviation.format(value));

                                String singleJsonSetting = "{\"" + topLevelParent + k + "\":" + value + "}\n";
                                TinygDriver.getInstance().write(singleJsonSetting);
                                updateProgress(currentElement, maxElements);
                                Thread.sleep(400); //Writing Values to eeprom can take a bit of time..
                                currentElement++;
                            }
                        }
                    }
                }
                updateProgress(0, 0); //reset the progress bar
                Main.postConsoleMessage("Finished Loading " + filename + ".");
                return null;
            }
        };

        configProgress.progressProperty().bind(task.progressProperty());
        new Thread(task).start();

    }

    @FXML
    private void handleApplyMachineSettings() {
        try {
            TinygDriver.getInstance().cmdManager.applyMachineSwitchMode(machineSwitchType.getSelectionModel().getSelectedIndex());
            TinygDriver.getInstance().cmdManager.applyMachineUnitMode(machineUnitMode.getSelectionModel().getSelectedIndex());
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleQueryMachineSettings() {
        try {
            TinygDriver.getInstance().cmdManager.queryMachineSwitchMode();
            TinygDriver.getInstance().cmdManager.queryAllMachineSettings();
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    @FXML
    void handleApplyDefaultSettings(ActionEvent evt) {
        try {
            if (checkConectedMessage().equals("true")) {
                TinygDriver.getInstance().write(CommandManager.CMD_APPLY_DEFAULT_SETTINGS);
            } else {
                logger.error(checkConectedMessage());
                tgfx.Main.postConsoleMessage(checkConectedMessage());
            }
        } catch (Exception ex) {
            logger.error("[!]Error Applying Default Settings");
        }
    }

    private String checkConectedMessage() {
        if (TinygDriver.getInstance().isConnected().get()) {
            return ("true");
        } else {
            return ("[!]TinyG is Not Connected");
        }
    }
}
