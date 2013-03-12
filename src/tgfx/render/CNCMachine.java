/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.render;

import java.text.DecimalFormat;
import java.util.Iterator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
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

    private DecimalFormat df = new DecimalFormat("#.##");
    private  final Circle cursorPoint = new Circle(2, javafx.scene.paint.Color.RED);
    
    public CNCMachine() {
        this.setStyle("-fx-background-color: black; -fx-border-color: orange;  -fx-border-width: .5;");
        
        /*####################################
         *Cursor Set
         #################################### */
       
        //Cursor point indicator
        cursorPoint.setRadius(1);
        cursorPoint.translateYProperty().bind(this.heightProperty().subtract(TinygDriver.getInstance().m.getAxisByName("y").getMachinePositionSimple()));
        cursorPoint.layoutXProperty().bind(TinygDriver.getInstance().m.getAxisByName("x").getMachinePositionSimple());
        
//        cncMachine.getHeight() - tg.m.getAxisByName("y").getMachinePosition().get();
        this.setMaxSize(0, 0);  //hide this element until we connect
        //Set our machine size from tinyg travel max

        this.maxHeightProperty().bind(TinygDriver.getInstance().m.getAxisByName("y").getTravelMaxSimple().multiply(TinygDriver.getInstance().m.gcodeUnitDivision));
        this.maxWidthProperty().bind(TinygDriver.getInstance().m.getAxisByName("x").getTravelMaxSimple().multiply(TinygDriver.getInstance().m.gcodeUnitDivision));

        final Circle c = new Circle(2, Color.RED);

        final Text cursorText = new Text("None");
//        cursorText.setStroke(Color.YELLOW);
        cursorText.setFill(Color.YELLOW);
        cursorText.setFont(Font.font("Arial", 6));

        setupLayout(); //initial layout setup in constructor

        this.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
//                gcodePane.getChildren().remove(c);
                getChildren().remove(cursorText);

            }
        });

        this.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
//                gcodePane.getChildren().remove(c);
                getChildren().add(cursorText);

            }
        });

        this.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                cursorText.setText("(X: " + df.format(me.getX()) + ")\n(Y: " + df.format((getHeight() - me.getY())) + ")");
                cursorText.setX(me.getX() + 10);
                cursorText.setY(me.getY());

            }
        });



        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent me) {
                //This is so we can set our machine position when a machine does not have homing switches
                if(me.getButton().equals(me.getButton().SECONDARY)){
                    //Right Clicked
                    ContextMenu cm = new ContextMenu();
                    MenuItem menuItem1 = new MenuItem("Set Machine Position");                  
                    menuItem1.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            Draw2d.setFirstDraw(true); //We do not want to draw a line from our previous position
                            TinygDriver.getInstance().cmdManager.setMachinePosition(me.getX(), getHeight() - me.getY());
                            
                        }
                    });
                    cm.getItems().add(menuItem1);
                    cm.show((Node)me.getSource(), me.getScreenX(), me.getScreenY());                    
                }
                
//                cm.getItems().add(cmItem1);
//pic.addEventHandler(MouseEvent.MOUSE_CLICKED,
//    new EventHandler<MouseEvent>() {
//        @Override public void handle(MouseEvent e) {
//            if (e.getButton() == MouseButton.SECONDARY)  
//                cm.show(pic, e.getScreenX(), e.getScreenY());
//        }
//});
                
//                Circle c = new Circle(2, Color.YELLOWGREEN);
//                c.setLayoutX(me.getX());
//                c.setLayoutY(me.getY());
//                Text coordsText = new Text("(" + me.getX() + "," + me.getY() + ")");
//                coordsText.setStroke(Color.YELLOW);
//                coordsText.setFill(Color.YELLOW);
//                coordsText.setFont(Font.font("Arial", 10));
//                coordsText.setX(me.getX() + 10);
//                coordsText.setY(me.getY());
//                getChildren().add(coordsText);
//                getChildren().add(c);
            }
        });



    }

    public boolean checkBoundsY(Line l) {
        if ((this.getHeight() - l.getEndY()) >= 0 && (this.getHeight() - l.getEndY()) <= this.getHeight() + 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkBoundsX(Line l) {
        if (l.getEndX() >= 0 && l.getEndX() <= this.getWidth()) {
            return true;
        } else {
            return false;
        }
    }

    public void clearScreen() {
        this.getChildren().clear();
        setupLayout();  //re-draw the needed elements.
    }

    private void setupLayout() {
        //This draws the x axis text as well as grid etc
        Text xText = new Text("X Axis");
        Text yText = new Text("Y Axis");

        xText.setY(-10);
        xText.xProperty().bind(this.heightProperty().divide(2));
        xText.setRotate(0);
        xText.setFill(Color.YELLOW);
        xText.setFont(Font.font("Arial", 10));

        yText.setX(-25);
        yText.yProperty().bind(this.widthProperty().divide(2));
        yText.setRotate(-90);
        yText.setFill(Color.YELLOW);
        yText.setFont(Font.font("Arial", 10));



        this.getChildren().add(xText);
        this.getChildren().add(yText);

        this.setCursor(Cursor.CROSSHAIR);
        this.getChildren().add(cursorPoint);
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
