/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author ril3y
 */
public class TgFX extends Application {
    
    public static void main(String[] args) {
        Application.launch(TgFX.class, args);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
        
        
        Scene scene = new Scene(root);
//        scene.getStylesheets().add(this.getClass().getResource("composer.css").toExternalForm());
        
        stage.setScene(scene);
        stage.show();
    }
}
