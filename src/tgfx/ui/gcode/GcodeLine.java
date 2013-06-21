/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.ui.gcode;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author ril3y
 */
public class GcodeLine {
    public SimpleStringProperty codeLine;// = new SimpleStringProperty();// = new SimpleStringProperty("<gcodeLine>");
    public int gcodeLineNumber;
    

    public GcodeLine(String gc, int gcl_number){
        this.codeLine = new SimpleStringProperty(gc);
        this.gcodeLineNumber = gcl_number;
    }
    
    public int getGcodeLineNumber(){
        return this.gcodeLineNumber;
    }
    
    public String getCodeLine(){
        return codeLine.get();
    } 
    
    public String getGcodeLineJsonified(){
        return("{\"gc\":\""+codeLine.get()+"\"}\n");
    }
    
    
    
}