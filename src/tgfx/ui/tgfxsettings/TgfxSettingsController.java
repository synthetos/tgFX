/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.ui.tgfxsettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;
import tgfx.Main;
import static tgfx.Main.getBuildInfo;
import tgfx.tinyg.CommandManager;
import tgfx.tinyg.TinygDriver;

/**
 * FXML Controller class
 *
 * @author rileyporter
 */
public class TgfxSettingsController implements Initializable {

    private static final Logger logger = Logger.getLogger(TgfxSettingsController.class);
    
    @FXML
    private Label tgfxBuildNumber, tgfxBuildDate, tgfxVersion;
    
    @FXML
    private Button settingDrawBtn;

    public static void updateTgfxSettings() {
    }
    private static boolean drawPreview;
    
     @FXML
    private void handleTogglePreview(ActionEvent event) {
        if (settingDrawBtn.getText().equals("ON")) {
            settingDrawBtn.setText("OFF");
            setDrawPreview(true);
        } else {
            settingDrawBtn.setText("ON");
            setDrawPreview(false);
        }
    }

    public static boolean isDrawPreview() {
        return drawPreview;
    }

    public void setDrawPreview(boolean drawPreview) {
        TgfxSettingsController.drawPreview = drawPreview;
    }
     
     

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        tgfxBuildNumber.setText(getBuildInfo("BUILD"));
        tgfxVersion.setText(".95");

        tgfxBuildDate.setId("lblMachine");
        tgfxBuildNumber.setId("lblMachine");
        tgfxVersion.setId("lblMachine");
    }
}
