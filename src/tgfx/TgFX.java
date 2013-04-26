/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import org.apache.log4j.PropertyConfigurator;

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
    	PropertyConfigurator.configure("log4j.properties");
        Application.launch(TgFX.class, args);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader();

//        FooController fooController = (FooController) fxmlLoader.getController();
        TgFX TgFXController = (TgFX) fxmlLoader.getController();
        
        Scene scene = new Scene(root);
                
        
        stage.setMinHeight(800);
        stage.setMinWidth(1280);
        stage.setScene(scene);
        stage.show();
    }
}
