/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.updater.firmware;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.NumberExpression;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import jfxtras.labs.dialogs.MonologFX;
import jfxtras.labs.dialogs.MonologFXBuilder;
import jfxtras.labs.dialogs.MonologFXButton;
import static jfxtras.labs.dialogs.MonologFXButton.Type.CANCEL;
import static jfxtras.labs.dialogs.MonologFXButton.Type.YES;
import jfxtras.labs.dialogs.MonologFXButtonBuilder;
import tgfx.Main;
import tgfx.tinyg.*;
import static tgfx.updater.firmware.FirmwareUpdaterController.enterBootloaderMode;
import static tgfx.updater.firmware.FirmwareUpdaterController.updateFileName;
import tgfx.utility.UtilityFunctions;

/**
 * FXML Controller class
 *
 * @author ril3y
 */
public class FirmwareUpdaterController implements Initializable {

    @FXML
    private static Label firmwareVersion;
    @FXML
    private Label hwVersion, buildNumb, hardwareId, latestFirmwareBuild;
    @FXML
    private Label currentFirmwareVersionLabel;
    @FXML
    private Button btnhandleUpdateFirmwareFromOnline;
    @FXML
    private Button btnhandleUpdateFirmwareFromFile;
    private SimpleDoubleProperty _currentVersionString = new SimpleDoubleProperty();
    private static String avrdudePath = new String();
    private static String avrconfigPath = new String();
    static HashMap<String, String> platformSetup = new HashMap<>();
    public static String updateFileName;
    private static SimpleBooleanProperty updaterButtonStateProperty = new SimpleBooleanProperty(false);

    private static boolean downloadUpdateFile() {
        //Lets put some MD5 checking in this also content checking
//        String updateFileName = new String();
        //Download TinyG.hex
        updateFileName = "tinyg.hex";
        URL url;
        try {
            url = new URL(TinygDriver.getInstance().machine.hardwarePlatform.getFirmwareUrl());
            URLConnection urlConnection = url.openConnection();
            Main.print("Opened Connection to Github");
            Main.postConsoleMessage("Downloading tinyg.hex file from github.com");
            InputStream input;
            input = urlConnection.getInputStream();
            //updateFileName = "tinyg.hex";
            try (OutputStream output = new FileOutputStream(new File(updateFileName))) {
                byte[] buffer = new byte[4096];
                int n = -1;
                while ((n = input.read(buffer)) != -1) {
                    if (n > 0) {
                        output.write(buffer, 0, n);
                    }
                }

                output.close();
                Main.postConsoleMessage("Finished Downloading tinyg.hex");
                Main.print("Finished Downloading tinyg.hex");
                return true;
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
            Main.postConsoleMessage("Error downloading the TinyG update from: " + TinygDriver.getInstance().machine.hardwarePlatform.getFirmwareUrl());
            Main.postConsoleMessage("Check your internetion connection and try again.  Firmware update aborted...");
            return false;
        } catch (IOException ex) {
            Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
            Main.postConsoleMessage("Error updating your TinyG.  IOERROR");
            return false;
        }
    }

    private static Task updateFirmware() {
        Task task;
//        final String updateFileName = getUpdateFile();
        Main.showConsole();
        task = new Task<Void>() {
            @Override
            public Void call() throws IOException, Exception {

                File avc = new File("tools" + File.separator + "config" + File.separator + "avrdude.conf");
                avrconfigPath = avc.getAbsolutePath();
                if (UtilityFunctions.getOperatingSystem().equals("mac")) {
                    File avd = new File("tools" + File.separator + "avrdude");
                    avrdudePath = avd.getAbsolutePath();
                } else {
                    File avd = new File("tools" + File.separator + "avrdude.exe");
                    avrdudePath = avd.getAbsolutePath();
                }

                if (enterBootloaderMode()) {
                    //we successfully sent "enterbootloader" most likely it worked.
                    Runtime rt = Runtime.getRuntime();
                    try {
                        String line;
                        String[] command = {avrdudePath,"-C"+avrconfigPath,"-c", "avr109", "-p","x192a3","-b" ,"115200","-P",TinygDriver.getInstance().getPortName(),"-U","flash:w:"+updateFileName};
                        ProcessBuilder pb = new ProcessBuilder(command);

                        pb.redirectErrorStream(true);
                        Process process = pb.start();

                        InputStream stdout = process.getInputStream();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));


                        OutputStream stream = process.getOutputStream();

                       
                        int b;
                        
                        while ((b = reader.read()) != -1){
                            Main.postConsoleMessage((char) b);
                        }

//                        while ((line = reader.readLine()) != null) {
//                            System.out.println("Stdout: " + line);
//                        }


                        Main.postConsoleMessage("Updating TinyG Now... Please Wait");

                        InputStream is = process.getInputStream();
                        Main.postConsoleMessage("Attempting to update TinyG's firmware.");
                        process.waitFor();
                        Thread.sleep(2000);//sleep a bit and let the firmware init
                        TinygDriver.getInstance().sendReconnectRequest();
                        toggleUpdateFirmwareButton(false);//re-enable the update firmware buttons now that we have completed

                        Main.postConsoleMessage("Firmware update complete.");
                        toggleUpdateFirmwareButton(false);

                    } catch (IOException | InterruptedException ex) {
                        Main.postConsoleMessage("ERROR");
                        toggleUpdateFirmwareButton(true);//re-enable the update firmware buttons now that we have failed to try :)
                    }
                }
                return null;
            }
        };
        return task;

    }

    private static void toggleUpdateFirmwareButton(boolean choice) {
        final boolean bChoice = choice;
        updaterButtonStateProperty.set(bChoice);
    }

//    @FXML
//    public static void handleUpdateFirmwareFromFile(ActionEvent event) {
//        
//        final FileChooser fc = new FileChooser();
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                tgfx.Main.postConsoleMessage("[+]Loading a gcode file.....\n");
//                fc.showOpenDialog(null);
//                
//                fc.setTitle("Open GCode File");
//                File f = new File(fc.getInitialFileName());
//            }
//        });  
//        
//    }
    @FXML
    private void handleUpdateFirmwareFromFile(ActionEvent event) {
        checkForBlankHardwareProfile();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tgfx.Main.postConsoleMessage("[+]Loading a gcode file.....\n");
                FileChooser fc = new FileChooser();
                fc.setTitle("Open GCode File");

                String HOME_DIR = System.getenv("HOME"); //Get Home DIR in OSX
                if (HOME_DIR == null) {
                    HOME_DIR = System.getProperty("user.home");  //Get Home DIR in Windows
                }

                fc.setInitialDirectory(new File(HOME_DIR));  //This will find osx users home dir
                File f = fc.showOpenDialog(null);
                updateFileName = f.getAbsolutePath();
                handleUpdateFirmware();
            }
        });
    }

    private static void checkForBlankHardwareProfile() {
        if (TinygDriver.getInstance().machine.hardwarePlatform.getHardwarePlatformVersion() == -1) {
            //This code checks to see if a hardware platform has been applied.
            //if the hpv is -1 then it has not.  So we guess that the board is a v8 TinyG.
            TinygDriver.getInstance().hardwarePlatformManager.setPlatformByName("TinyG");
        }
    }

    public static void handleUpdateFirmware() {

        if (TinygDriver.getInstance().isTimedout() || TinygDriver.getInstance().machine.hardwarePlatform.isIsUpgradeable()) {
            //This platform can be upgraded  

            toggleUpdateFirmwareButton(true);
            Task task = updateFirmware();
            new Thread(task).start();

        } else {
            Main.postConsoleMessage("Sorry your TinyG platform cannot be auto upgraded at this time.  Please see the TinyG wiki for manual upgrade instructions.");
        }
    }

    @FXML
    public void handleUpdateFirmwareFromOnline(ActionEvent event) {
        checkForBlankHardwareProfile();

        if (downloadUpdateFile()) { //we downloaded the file successfully
            handleUpdateFirmware();
        }

    }

    @FXML
    private void checkFirmwareUpdate(ActionEvent event) {
        Main.print("Checking current Firmware Version");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if(TinygDriver.getInstance().machine.hardwarePlatform.getLatestVersionUrl() == null){
                        Main.postConsoleMessage("Error updating TinyG Firmware, try updating though the update file method instead");
                    }
                    URL url = new URL(TinygDriver.getInstance().machine.hardwarePlatform.getLatestVersionUrl());
                    URLConnection urlConnection = url.openConnection();

                    InputStream input;
                    input = urlConnection.getInputStream();
                    byte[] buffer = new byte[4096];
                    Main.print("Checking end");
                    input.read(buffer);
                    String _currentVersionString = new String(buffer);
                    latestFirmwareBuild.setText(_currentVersionString);
                    Double currentVal;
                    if (TinygDriver.getInstance().machine.getFirmwareBuild() < Double.parseDouble(_currentVersionString)) {
                        //We need to update your firmware
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Main.postConsoleMessage("TinyG Firmware Update Available.");

                                MonologFXButton btnYes = MonologFXButtonBuilder.create()
                                        .defaultButton(true)
                                        .icon("/testmonologfx/dialog_apply.png")
                                        .type(MonologFXButton.Type.YES)
                                        .build();

                                MonologFXButton btnNo = MonologFXButtonBuilder.create()
                                        .cancelButton(true)
                                        .icon("/testmonologfx/dialog_cancel.png")
                                        .type(MonologFXButton.Type.CANCEL)
                                        .build();

                                MonologFX mono = MonologFXBuilder.create()
                                        .titleText("Firmware Update Available")
                                        .message("There is a firmware update available for your TinyG Hardware. \n"
                                        + "\n Click Yes to start your firmware update.")
                                        .button(btnYes)
                                        .button(btnNo)
                                        .type(MonologFX.Type.ERROR)
                                        .build();

                                MonologFXButton.Type retval = mono.showDialog();

                                switch (retval) {
                                    case YES:
//                                logger.info("Clicked Yes");

                                        try {
                                            Main.postConsoleMessage("This is going to take about 30 seconds.... Please Wait... Watch the flashies....");
                                            handleUpdateFirmware();

                                        } catch (Exception ex) {
                                            Main.postConsoleMessage("Error in updating firmware.");
                                        }
                                        break;
                                    case CANCEL:
//                                logger.info("Clicked No");
                                        Main.postConsoleMessage("TinyG firmware update cancelled.");
                                        break;
                                }
                            }
                        });

                    } else {
                        Main.postConsoleMessage("Your " + TinygDriver.getInstance().machine.hardwarePlatform.getPlatformName() + "'s firmware is up to date...\n");
                    }

                } catch (MalformedURLException ex) {
                    Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /**
         * Initializes the controller class.
         */
        //We bind these values to allow us to disable the buttons when the updating thread is running
        btnhandleUpdateFirmwareFromFile.disableProperty().bind(updaterButtonStateProperty);
        btnhandleUpdateFirmwareFromOnline.disableProperty().bind(updaterButtonStateProperty);

        NumberExpression ne = new SimpleDoubleProperty(_currentVersionString.doubleValue()).subtract(TinygDriver.getInstance().machine.getFirmwareBuild());
        hardwareId.textProperty().bind(TinygDriver.getInstance().machine.hardwareId); //Bind the tinyg hardware id to the tg driver value
        //hwVersion.textProperty().bind(TinygDriver.getInstance().machine.hardwareVersion); //Bind the tinyg version  to the tg driver value
        hwVersion.textProperty().bind(TinygDriver.getInstance().machine.hardwareVersion); //Bind the tinyg version  to the tg driver value
        firmwareVersion.textProperty().bind(TinygDriver.getInstance().machine.firmwareVersion);
        buildNumb.textProperty().bind(TinygDriver.getInstance().machine.firmwareBuild.asString());

    }

    protected static boolean enterBootloaderMode() throws InterruptedException {

        Main.print("Trying to enter bootloader mode");
        Main.postConsoleMessage("Entering Bootloader mode.  tgFX will be un-responsive for then next 30 seconds.\n"
                + "Your TinyG will start blinking rapidly while being programmed");
        if (TinygDriver.getInstance().isConnected().get()) {
            //We need to disconnect from tinyg after issuing out boot command.
            try {
                TinygDriver.getInstance().priorityWrite(CommandManager.CMD_APPLY_BOOTLOADER_MODE); //Set our board into bootloader mode.
                Thread.sleep(1000);
            } catch (Exception ex) {
                Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            TinygDriver.getInstance().sendDisconnectRequest();
            Thread.sleep(500);
        } else {
            return false; //TinyG is not connected
        }
        return true;

    }
}
