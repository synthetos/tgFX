/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import java.util.logging.Level;
import org.apache.log4j.PropertyConfigurator;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import tgfx.tinyg.TinygDriver;

/**
 *
 * @author ril3y
 */
public class TgFX extends Application {

    public static String[] arguments;
    private Stage mainStage;
    private static final Logger logger = Logger.getLogger(TgFX.class);
    private static Thread serialWriterThread;
    private static Thread threadResponseParser;

    @Override
    public void start(Stage stage) throws Exception {

        /*######################################
         * THREAD INITS
         ######################################*/


        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                try {
                    System.out.println("CLOSING!");
                    TinygDriver.getInstance().priorityWrite("!\n");
                    Thread.sleep(200);
                    TinygDriver.getInstance().priorityWrite("%\n");
                    Thread.sleep(200);
                    TinygDriver.getInstance().priorityWrite("~\n");
                    
                    TinygDriver.getInstance().serialWriter.setRun(false); //We need to stop our threads when the program stops
                    TinygDriver.getInstance().resParse.setRUN(false);
                    //We need to wake these guys up as they are in a blocking state waiting for a line to be added to a queue.
                    threadResponseParser.interrupt();
                    serialWriterThread.interrupt();
                    System.exit(0);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(TgFX.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        serialWriterThread = new Thread(TinygDriver.getInstance().serialWriter);

        serialWriterThread.setName("SerialWriter");
        serialWriterThread.setDaemon(true);
        serialWriterThread.start();
        threadResponseParser = new Thread(TinygDriver.getInstance().resParse);

        threadResponseParser.setDaemon(true);
        threadResponseParser.setName("ResponseParser");
        threadResponseParser.start();


        Parent root;
        root = (Parent) FXMLLoader.load(getClass().getResource("Main.fxml"));
        Scene scene = new Scene(root);

        scene.setRoot(root);
        FXMLLoader fxmlLoader = new FXMLLoader();
        TgFX TgFXController = (TgFX) fxmlLoader.getController();
        
        stage.setMinHeight(648);
        stage.setMinWidth(1152);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        final TinygDriver tg = TinygDriver.getInstance();

        



        PropertyConfigurator.configure("log4j.properties");
        Application.launch(TgFX.class, args);
    }
}
