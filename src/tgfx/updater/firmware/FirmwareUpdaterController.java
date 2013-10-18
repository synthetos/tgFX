/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.updater.firmware;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import tgfx.Main;
import tgfx.tinyg.*;
import gnu.io.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * FXML Controller class
 *
 * @author ril3y
 */
public class FirmwareUpdaterController implements Initializable {

    private String tinygHexFileUrl = "https://raw.github.com/synthetos/TinyG/master/firmware/tinyg/default/tinyg.hex";
    private String avrdudePath = new String();
    private String avrconfigPath = new String();
    static HashMap<String, String> platformSetup = new HashMap<>();

    /**
     * Initializes the controller class.
     */
    @FXML
    private void handleUpdateFirmware(ActionEvent event) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

//                avrconfigPath = "../tools" + File.separator + "config" + File.separator + "avrdude.conf";
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
                enterBootloaderMode();


                //Download TinyG.hex
                URL url;
                try {
                    url = new URL(tinygHexFileUrl);
                    URLConnection urlConnection = url.openConnection();
                    System.out.println("Opened Connection to Github");
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

                    process.waitFor();

                } catch (IOException ex) {
                    Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FirmwareUpdaterController.class.getName()).log(Level.SEVERE, null, ex);
                }
                // /avrdude -p x192a3 -C ../etc/avrdude.conf -c avr109 -b 115200 -P /dev/cu.usbserial-AE01DVZI -U flash:w:
                System.out.println("Updating TinyG Now... Please Wait");
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
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
