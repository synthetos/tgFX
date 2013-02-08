/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.render;

import java.util.Iterator;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import tgfx.tinyg.TinygDriver;

/**
 *
 * @author rileyporter
 */
public class CNCMachine extends Pane {

    public CNCMachine() {
        this.setMaxSize(0, 0);  //hide this element until we connect
        //Set our machine size from tinyg travel max
        this.maxHeightProperty().bind(TinygDriver.getInstance().m.getAxisByName("y").getTravelMaxSimple());
        this.maxWidthProperty().bind(TinygDriver.getInstance().m.getAxisByName("x").getTravelMaxSimple());
//        machineOutline.scaleXProperty().bind(machineX);
//        machineOutline.scaleYProperty().bind(tester.heightProperty());

//        machineOutline.scaleXProperty().bind((StackPane) machineOutline.getPadding());
        final Circle c = new Circle(2, Color.RED);

        final Text cursorText = new Text("None");
        cursorText.setStroke(Color.YELLOW);
        cursorText.setFill(Color.YELLOW);
        cursorText.setFont(Font.font("Arial", 10));
        
        Text xText = new Text("X Axis");
        Text yText = new Text("Y Axis");
        
        xText.setY(-10);
        xText.xProperty().bind(this.heightProperty().divide(2));
        xText.setRotate(0);
        xText.setStroke(Color.YELLOW);
        xText.setFill(Color.YELLOW);
        xText.setFont(Font.font("Arial", 10));
        
        yText.setX(-25);
        yText.yProperty().bind(this.widthProperty().divide(2));
        yText.setRotate(-90);
        yText.setStroke(Color.YELLOW);
        yText.setFill(Color.YELLOW);
        yText.setFont(Font.font("Arial", 10));
        
        
        
        this.getChildren().add(xText);
        this.getChildren().add(yText);

        this.setCursor(Cursor.CROSSHAIR);

        this.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
//                gcodePane.getChildren().remove(c);
                getChildren().remove(cursorText);

            }
        });

        this.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
//                gcodePane.getChildren().remove(c);
                getChildren().add(cursorText);

            }
        });

        this.setOnMouseMoved(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                cursorText.setText("(xpos: " + me.getX() + ")\n(ypos: " + me.getY() + ")");
                cursorText.setX(me.getX() + 10);
                cursorText.setY(me.getY());

            }
        });

        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                Circle c = new Circle(2, Color.YELLOWGREEN);
                c.setLayoutX(me.getX());
                c.setLayoutY(me.getY());
                Text coordsText = new Text("(" + me.getX() + "," + me.getY() + ")");
                coordsText.setStroke(Color.YELLOW);
                coordsText.setFill(Color.YELLOW);
                coordsText.setFont(Font.font("Arial", 10));
                coordsText.setX(me.getX() + 10);
                coordsText.setY(me.getY());
                getChildren().add(coordsText);
                getChildren().add(c);
            }
        });



    }

    public void autoScaleWorkTravelSpace(double scaleAmount) {
        /*
         * TODO:
         * Fix it so that if your table is larger than the "pixels" in the size of the gcodePreview box, then the scaling does down to a decimal or negative number.
         * 
         * 
         */

        //Get the axis with the smallest available space.  Think aspect ratio really





        double stroke = 2 / scaleAmount;
        this.setScaleX(scaleAmount);
        this.setScaleY(scaleAmount);
        Iterator ii = this.getChildren().iterator();

        while (ii.hasNext()) {
            if (ii.next().getClass().getName().endsWith("Line")) {
                Line l = (Line) ii.next();
                l.setStrokeWidth(stroke);
            }
        }
    }
}
