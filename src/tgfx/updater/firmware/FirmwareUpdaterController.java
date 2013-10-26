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
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.NumberExpression;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import tgfx.Main;
import tgfx.tinyg.*;
import tgfx.ui.machinesettings.MachineSettingsController;

/**
 * FXML Controller class
 *
 * @author ril3y
 */
public class FirmwareUpdaterController implements Initializable {

    @FXML
    private Label currentFirmwareVersionLabel;
    
    @FXML 
    private Button handleUpdateFirmware;
    
    private SimpleDoubleProperty _currentVersionString = new SimpleDoubleProperty();
    private String tinygHexFileUrl = "https://raw.github.com/synthetos/TinyG/master/firmware/tinyg/default/tinyg.hex";
    private String avrdudePath = new String();
    private String avrconfigPath = new String();
    static HashMap<String, String> platformSetup = new HashMap<>();
    private String currentFirmwareFile = "https://raw.github.com/synthetos/TinyG/master/version.current";
    

    /**
     * Initializes the controller class.
     */
    @FXML
    private void handleUpdateFirmware(ActionEvent event) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                File avc = new File("tools" + File.separator + "config" + File.separator + "avrdude.conf");
                avrconfigPath = avc.getAbsolutePath().toString();
                if (Main.getOperatingSystem().equals("mac")) {
                    File avd = new File("tools" + File.separator + "avrdude");
                    avrdudePath = avd.getAbsolutePath().toString();
                } else {
                    File avd = new File("tools" + File.separator + "avrdude.exe");
                    avrdudePath = avd.getAbsolutePath().toString();
                }

                System.out.println("Trying to enter bootloader mode");
                Main.postConsoleMessage("Entering Bootloader mode.  tgFX will be un-responsive for then next 30 seconds.\n"
                        + "Your TinyG will start blinking rapidly while being programmed");
                enterBootloaderMode();


                //Download TinyG.hex
                URL url;
                try {
                    url = new URL(tinygHexFileUrl);
                    URLConnection urlConnection = url.openConnection();
                    System.out.println("Opened Connection to Github");
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
                        System.out.println("Finished Downloading tinyg.hex");
                    }
                } catch (MalformedURLException ex) {
                    Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                }

                Runtime rt = Runtime.getRuntime();

                try {
                    Process process = rt.exec(avrdudePath + " -p x192a3 -C " + avrconfigPath + " -c avr109 -b 115200 -P " + TinygDriver.getInstance().getPortName() + " -U flash:w:tinyg.hex");
                    InputStream is = process.getInputStream();
                    Main.postConsoleMessage("Attempting to update TinyG's firmware.");
                    process.waitFor();

                } catch (IOException ex) {
                    Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Updating TinyG Now... Please Wait");

            }
        });
    }

    @FXML
    private void checkFirmwareUpdate(ActionEvent event) {
        System.out.println("Checking current Firmware Version");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(currentFirmwareFile);
                    URLConnection urlConnection = url.openConnection();

                    //                    
                    //                Main.postConsoleMessage("Downloading tinyg.hex file from github.com");
                    InputStream input;
                    input = urlConnection.getInputStream();
                    byte[] buffer = new byte[4096];
                    System.out.println("Checking end");
                    input.read(buffer);
                    String _currentVersionString = new String(buffer);
                    Double currentVal;
                    if(TinygDriver.getInstance().m.getFirmwareBuild() < Double.valueOf(_currentVersionString)){
                        
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
        NumberExpression ne = new SimpleDoubleProperty(_currentVersionString.doubleValue()).subtract(TinygDriver.getInstance().m.getFirmwareBuild());
        
//        BooleanExpression be = new SimpleDoubleProperty(TinygDriver.getInstance().m.getFirmwareVersion());
//        _currentVersionString.TinygDriver.getInstance().m.firmwareBuild);
    }

    protected void enterBootloaderMode() {
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
