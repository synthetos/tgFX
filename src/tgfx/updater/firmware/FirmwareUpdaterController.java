/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.updater.firmware;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import jfxtras.labs.dialogs.MonologFX;
import jfxtras.labs.dialogs.MonologFXBuilder;
import jfxtras.labs.dialogs.MonologFXButton;
import static jfxtras.labs.dialogs.MonologFXButton.Type.CANCEL;
import static jfxtras.labs.dialogs.MonologFXButton.Type.YES;
import jfxtras.labs.dialogs.MonologFXButtonBuilder;
import jssc.SerialPortException;
import tgfx.Main;
import tgfx.tinyg.*;
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
    private Button handleUpdateFirmware;
    private SimpleDoubleProperty _currentVersionString = new SimpleDoubleProperty();
//    private String tinygHexFileUrl = TinygDriver.getInstance().hardwarePlatform.getFirmwareUrl();
    //    private String tinygHexFileUrl = "https://raw.github.com/synthetos/TinyG/master/firmware/tinyg/default/tinyg.hex";
    private static String avrdudePath = new String();
    private static String avrconfigPath = new String();
    static HashMap<String, String> platformSetup = new HashMap<>();
//    private String currentFirmwareFile = "https://raw.github.com/synthetos/TinyG/master/version.current";
//    private String currentFirmwareFile = TinygDriver.getInstance().hardwarePlatform.getLatestVersionUrl();

    /**
     * Initializes the controller class.
     */
    @FXML
    public static void handleUpdateFirmware(ActionEvent event) {

        if (TinygDriver.getInstance().hardwarePlatform.isIsUpgradeable() ||  TinygDriver.getInstance().isTimedout()) {
            //This platform can be upgraded    

            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    File avc = new File("tools" + File.separator + "config" + File.separator + "avrdude.conf");
                    avrconfigPath = avc.getAbsolutePath().toString();
                    if (UtilityFunctions.getOperatingSystem().equals("mac")) {
                        File avd = new File("tools" + File.separator + "avrdude");
                        avrdudePath = avd.getAbsolutePath().toString();
                    } else {
                        File avd = new File("tools" + File.separator + "avrdude.exe");
                        avrdudePath = avd.getAbsolutePath().toString();
                    }

                    Main.print("Trying to enter bootloader mode");
                    Main.postConsoleMessage("Entering Bootloader mode.  tgFX will be un-responsive for then next 30 seconds.\n"
                            + "Your TinyG will start blinking rapidly while being programmed");
                    try {
                        enterBootloaderMode();
                    } catch (SerialPortException ex) {
                        Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                    }


                    //Download TinyG.hex
                    URL url;
                    try {
                        url = new URL(TinygDriver.getInstance().hardwarePlatform.getFirmwareUrl());
                        URLConnection urlConnection = url.openConnection();
                        Main.print("Opened Connection to Github");
                        Main.postConsoleMessage("Downloading tinyg.hex file from github.com");
                        InputStream input;
                        input = urlConnection.getInputStream();

                        try (OutputStream output = new FileOutputStream(new File("tinyg.hex"))) {
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
                        }
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    Runtime rt = Runtime.getRuntime();

                    try {
                        Main.print("Updating TinyG Now... Please Wait");
                        Process process = rt.exec(avrdudePath + " -p x192a3 -C " + avrconfigPath + " -c avr109 -b 115200 -P " + TinygDriver.getInstance().getPortName() + " -U flash:w:tinyg.hex");
                        InputStream is = process.getInputStream();
                        Main.postConsoleMessage("Attempting to update TinyG's firmware.");
                        process.waitFor();

                    } catch (IOException | InterruptedException ex) {
                        Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        TinygDriver.getInstance().priorityWrite(CommandManager.CMD_APPLY_RESET);
                        TinygDriver.getInstance().disconnect();

                    } catch (Exception ex) {
                        Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } else {
            Main.postConsoleMessage("Sorry your TinyG platform cannot be auto upgraded at this time.  Please see the TinyG wiki for manual upgrade instructions.");
        }
    }

//    public static double getCurrentBuildNumber() {
//        return (Double.valueOf(firmwareVersion.getText()));
//    }

    @FXML
    private void checkFirmwareUpdate(ActionEvent event) {
        Main.print("Checking current Firmware Version");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(TinygDriver.getInstance().hardwarePlatform.getLatestVersionUrl());
                    URLConnection urlConnection = url.openConnection();

                    //                    
                    //                Main.postConsoleMessage("Downloading tinyg.hex file from github.com");
                    InputStream input;
                    input = urlConnection.getInputStream();
                    byte[] buffer = new byte[4096];
                    Main.print("Checking end");
                    input.read(buffer);
                    String _currentVersionString = new String(buffer);
                    latestFirmwareBuild.setText(_currentVersionString);
                    Double currentVal;
                    if (TinygDriver.getInstance().machine.getFirmwareBuild() < Double.valueOf(_currentVersionString).doubleValue()) {
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
                                            handleUpdateFirmware(new ActionEvent());

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
                        Main.postConsoleMessage("Your " + TinygDriver.getInstance().hardwarePlatform.getPlatformName() + "'s firmware is up to date...\n");
                    }

//                    currentVal.valueOf(avrdudePath)
//                    
//                    
//                    
//                    FirmwareVersionLabel.setText(_currentVersionString);
//                    if(currentFirmwareVersionLabel.getAlignment())







                    //                    try (OutputStream output = new FileOutputStream(new File("tinyg.hex"))) {
                    //                        byte[] buffer = new byte[4096];
                    //                        int n = -1;
                    //                        while ((n = input.read(buffer)) != -1) {
                    //                            if (n > 0) {
                    //                                output.write(buffer, 0, n);
                    //                            }
                    //                        }
                    //                        output.close();
                    //                        Main.postConsoleMessage("Finished Downloading tinyg.hex");
                    //                
                    //
                } catch (MalformedURLException ex) {
                    Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                }


            }
        });

    }

    //https://github.com/synthetos/TinyG/blob/master/readme.md
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        NumberExpression ne = new SimpleDoubleProperty(_currentVersionString.doubleValue()).subtract(TinygDriver.getInstance().machine.getFirmwareBuild());
        hardwareId.textProperty().bind(TinygDriver.getInstance().machine.hardwareId); //Bind the tinyg hardware id to the tg driver value
        hwVersion.textProperty().bind(TinygDriver.getInstance().machine.hardwareVersion); //Bind the tinyg version  to the tg driver value
        firmwareVersion.textProperty().bind(TinygDriver.getInstance().machine.firmwareVersion);
        buildNumb.textProperty().bind(TinygDriver.getInstance().machine.firmwareBuild.asString());
//        BooleanExpression be = new SimpleDoubleProperty(TinygDriver.getInstance().m.getFirmwareVersion());
//        _currentVersionString.TinygDriver.getInstance().m.firmwareBuild);
    }

    protected static void enterBootloaderMode() throws SerialPortException {
        if (TinygDriver.getInstance().isConnected().get()) {
            //We need to disconnect from tinyg after issuing out boot command.
            try {
                TinygDriver.getInstance().priorityWrite(CommandManager.CMD_APPLY_BOOTLOADER_MODE); //Set our board into bootloader mode.
                Thread.sleep(1000);

            } catch (Exception ex) {
                Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
            }
            TinygDriver.getInstance().disconnect();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
