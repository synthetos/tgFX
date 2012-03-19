/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.render;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 *
 * @author ril3y
 */
public class Draw2d {

    static Paint retPaint;
    public static Paint SLOWEST = (Paint.valueOf("#ee1a0f"));
    public static Paint SLOW = (Paint.valueOf("#ff9933"));
    public static Paint MEDIUM_SLOW = (Paint.valueOf("#eed00f"));
    public static Paint MEDUIM = (Paint.valueOf("#c1ff66"));
    public static Paint FAST = (Paint.valueOf("#85ff22"));
    public static Paint FASTEST = (Paint.valueOf("#0fee17"));

    public static Paint TRAVERSE = (Paint.valueOf("#d0d0d0"));
    private float MAX_MACHINE_VELOCITY;
    private static double stroke_weight = .5;
    
    public static void setStrokeWeight(double w){
        if(w < 0){
            w = 0.05;
        }
        stroke_weight = w;
    }
    
    public static double getStrokeWeight(){
        return stroke_weight;
    }
    
    public static Paint getLineColorFromVelocity(float vel) {

        if (vel > 1 && vel < 100) {
            return (SLOWEST);
        } else if (vel > 101 && vel <= 250) {
            return (SLOW);
        } else if (vel > 251 && vel <= 350) {
            return (MEDIUM_SLOW);
        } else if (vel > 351 && vel <= 390) {
            return (MEDUIM);
        } else if (vel > 391 && vel <= 650) {
            return (FAST);
        } else if (vel > 651) {
            return (FASTEST);

        }


        return retPaint;
    }
}
