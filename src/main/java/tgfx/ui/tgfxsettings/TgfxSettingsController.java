/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.ui.tgfxsettings;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import org.apache.log4j.Logger;
import tgfx.utility.UtilityFunctions;

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
    private ToggleButton settingDrawBtn;
    
    @FXML
    public static ToggleButton settingDebugBtn;

    

    public static void updateTgfxSettings() {
    }
    private static boolean drawPreview = true;
    
     @FXML
    private void handleTogglePreview(ActionEvent event) {
        if(settingDrawBtn.isSelected()){
            settingDrawBtn.setText("Enabled");
            setDrawPreview(true);
            
        }else{
            setDrawPreview(false);
            settingDrawBtn.setText("Disabled");
//        }
//        if (settingDrawBtn.getText().equals("ON")) {
//            settingDrawBtn.setText("OFF");
//            setDrawPreview(true);
//        } else {
//            settingDrawBtn.setText("ON");
//            setDrawPreview(false);
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
        
        settingDrawBtn.setSelected(true);  //We set drawing preview to default
        settingDrawBtn.setText("Enabled");
        
        tgfxBuildNumber.setText(UtilityFunctions.getBuildInfo("BUILD"));
        tgfxVersion.setText(".95");

        tgfxBuildDate.setId("lblMachine");
        tgfxBuildNumber.setId("lblMachine");
        tgfxVersion.setId("lblMachine");
    }
}
