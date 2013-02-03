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
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
        
        
        Scene scene = new Scene(root);
//        scene.getStylesheets().add(this.getClass().getResource("new_main.css.css").toExternalForm());
        
        
        
        stage.setScene(scene);
        stage.show();
    }
}
