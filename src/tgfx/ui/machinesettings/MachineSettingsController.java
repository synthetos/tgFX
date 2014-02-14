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

    private final DecimalFormat decimalFormat = new DecimalFormat("################################.############################");
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
     *
     * @param url
     * @param rb
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
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                files = listOfFile.getName();
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

    private int getConfigElementLength(JSONObject js) throws JSONException {
        //This method is used to get the total number of json objects to write for loading config progress bar to work
        String topLevelParent;
        topLevelParent = (String) js.names().get(0);
        //JSONObject tmp = js.get(topLevelParent);
        int jsonKeyLength = js.getJSONObject(topLevelParent).length();
        return jsonKeyLength;
    }

    @FXML
    private void handleLoadConfig(ActionEvent event) throws Exception {

//        Task task = new Task<Void>() {
//            @Override
//            public Void call() {
//                int max = 100000000;
//                for (int i = 1; i <= max; i++) {
//                    updateProgress(i, max);
//                }
//                return null;
//            }
//        };
        //This function gets the config file selected and applys the settings onto tinyg.
        InputStream fis;

        BufferedReader br;

        final File selected_config = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "configs" + System.getProperty("file.separator") + configsListView.getSelectionModel().getSelectedItem());
        fis = new FileInputStream(selected_config);

        br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

        //Loop though the file to get the total elements we need to write
        //for the progress bar to function
        String line;

        int elementCount = 1;

        while ((line = br.readLine()) != null) {
            if (line.startsWith("{\"name")) {
            } else {
                JSONObject js = new JSONObject(line);
                elementCount = elementCount + getConfigElementLength(js);
            }
        }

        final int maxElements = elementCount;
        logger.info("Max Elements is: " + String.valueOf(maxElements));
        Task task = null;
        task = new Task<Void>() {
            @Override
            public Void call() throws IOException, JSONException, Exception {
                String line = new String();
                int count = 1;
                final BufferedReader br2;
                InputStream fis2;

                fis2 = new FileInputStream(selected_config);
                br2 = new BufferedReader(new InputStreamReader(fis2, Charset.forName("UTF-8")));

                while ((line = br2.readLine()) != null) {
                    if (line.startsWith("{\"name")) {
                        //This is the name of the CONFIG lets not write this to TinyG 
                        tgfx.Main.postConsoleMessage("[+]Loading " + line.split(":")[1] + " config into TinyG... Please Wait...");
                    } else {
                        JSONObject js = new JSONObject(line);

                        String topLevelParent = new String();
                        topLevelParent = (String) js.names().get(0);
                        Iterator it = js.getJSONObject(topLevelParent).keys();

                        while (it.hasNext()) {
                            String k = (String) it.next();
                            Double value = (Double) js.getJSONObject(topLevelParent).getDouble(k);
                            System.out.println("This is the value " + k + " " + decimalFormat.format(value));
                            //value = Double.valueOf(decimalFormatjunctionDeviation.format(value));

                            String singleJsonSetting = "{\"" + topLevelParent + k + "\":" + value + "}\n";
                            TinygDriver.getInstance().write(singleJsonSetting);
                            count = count + 1;
                            Thread.sleep(400);
                            updateProgress(count, maxElements);
                        }

                        TinygDriver.getInstance().write(line + "\n");    //Write the line to tinyG
                        //Thread.sleep(200);      //Writing Values to eeprom can take a bit of time..
                        tgfx.Main.postConsoleMessage("[+]Writing Config String: " + line + "\n");
                        

                        logger.info("Count is: " + String.valueOf(count) + "of " + String.valueOf(maxElements));
                    }
                }
                return null;
            }
        };
        if (TinygDriver.getInstance().isConnected().get()) {
            configProgress.progressProperty().bind(task.progressProperty());
            new Thread(task).start();
        }

    }

//        while ((line = br.readLine()) != null) {
//            if (TinygDriver.getInstance().isConnected().get()) {
//                if (line.startsWith("{\"name")) {
//                    //This is the name of the CONFIG lets not write this to TinyG 
//                    tgfx.Main.postConsoleMessage("[+]Loading " + line.split(":")[1] + " config into TinyG... Please Wait...");
//                } else {
//
//
//
//                    JSONObject js = new JSONObject(line);
//
//
//                    writeConfigValue(js);
////                    TinygDriver.getInstance().write(line + "\n");    //Write the line to tinyG
////                    Thread.sleep(200);      //Writing Values to eeprom can take a bit of time..
//
//                    //tgfx.Main.postConsoleMessage("[+]Writing Config String: " + line + "\n");
//                }
//            }
//        }
//    }
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
