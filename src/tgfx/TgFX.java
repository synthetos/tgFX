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
import org.apache.log4j.Logger;

/**
 *
 * @author ril3y
 */
public class TgFX extends Application {

    public static String[] arguments;

    private Stage mainStage;
    private static final Logger logger = Logger.getLogger(TgFX.class);
    
//    public TgFX(Broker broker){
//        this.broker = broker;
//        Application.launch(TgFX.class);
//        broker.subscribe("/launch/application", new Callback() {
//
//            @Override
//            public void notify(String uri, EventData eventData) throws Exception {
//              logger.debug("/launch/application/event");
//               
//              
//                       
//                       
//            }
//        });
//        logger.debug("Just registered launch/application from TgFX thread.");
//        
//    }
    
//    @Override
//    public void start(Stage stage) throws Exception {
//       this.setMainStage(stage);
//       broker.publish("/app/started/tgfx", mainStage);
//    }
//    
//    
//    public Stage getMainStage(){
//        return(mainStage);
//    }
//    
//    public void setMainStage(Stage mainStage){
//        this.mainStage = mainStage;
//    }
//    
    @Override
    public void start(Stage stage) throws Exception {

//        context = new ClassPathXmlApplicationContext("beans.xml");


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

        public static void main(String[] args) {
            PropertyConfigurator.configure("log4j.properties");
            Application.launch(TgFX.class, args);
        }
    
    
}
