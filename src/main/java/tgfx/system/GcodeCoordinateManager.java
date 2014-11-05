/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.system;

import java.util.ArrayList;
import java.util.Iterator;
import javafx.beans.property.StringProperty;

/**
 *
 * @author rileyporter
 */
public class GcodeCoordinateManager {

    private GcodeCoordinateSystem g54;
    private GcodeCoordinateSystem g55;
    private GcodeCoordinateSystem g56;
    private GcodeCoordinateSystem g57;
    private GcodeCoordinateSystem g58;
    private GcodeCoordinateSystem g59;
    private GcodeCoordinateSystem currentGcodeCoordinateSystem;
    private ArrayList<GcodeCoordinateSystem> coordinateSystems = new ArrayList<>();

    public GcodeCoordinateManager() {

        g54 = new GcodeCoordinateSystem("g54");
        g55 = new GcodeCoordinateSystem("g55");
        g56 = new GcodeCoordinateSystem("g56");
        g57 = new GcodeCoordinateSystem("g57");
        g58 = new GcodeCoordinateSystem("g58");
        g59 = new GcodeCoordinateSystem("g59");

        coordinateSystems.add(g54);
        coordinateSystems.add(g55);
        coordinateSystems.add(g56);
        coordinateSystems.add(g57);
        coordinateSystems.add(g58);
        coordinateSystems.add(g59);

        currentGcodeCoordinateSystem = new GcodeCoordinateSystem();

    }

    

    public GcodeCoordinateSystem getCurrentGcodeCoordinateSystem() { 
        return(currentGcodeCoordinateSystem);
    }
    
    public StringProperty getCurrentGcodeCoordinateSystemName() { 
        return(currentGcodeCoordinateSystem.getGcodeCoordinateSystemProperty());
    }
    
    

    public void setCurrentGcodeCoordinateSystem(int gcu) {
        switch(gcu){
            case 1: 
                setCurrentGcodeCoordinateSystem("g54");
                break;
            case 2: 
                setCurrentGcodeCoordinateSystem("g55");
                break;
            case 3: 
                setCurrentGcodeCoordinateSystem("g56");
                break;
            case 4: 
                setCurrentGcodeCoordinateSystem("g57");
                break;
            case 5: 
                setCurrentGcodeCoordinateSystem("g58");
                break;
            case 6: 
                setCurrentGcodeCoordinateSystem("g59");
                break;         
        }
    }


    public void setCurrentGcodeCoordinateSystem(String gcu) {

        Iterator ii;
        ii = coordinateSystems.iterator();
        while (ii.hasNext()) {
            GcodeCoordinateSystem _gc = (GcodeCoordinateSystem) ii.next();
            switch (_gc.getCoordinate().toLowerCase()) {
                case "g54":
                    currentGcodeCoordinateSystem.setCoordinate(gcu);
                    break;
                case "g55":
                    currentGcodeCoordinateSystem.setCoordinate(gcu);
                    break;
                case "g56":
                    currentGcodeCoordinateSystem.setCoordinate(gcu);
                    break;
                case "g57":
                    currentGcodeCoordinateSystem.setCoordinate(gcu);
                    break;
                case "g58":
                    currentGcodeCoordinateSystem.setCoordinate(gcu);
                    break;
                case "g59":
                    currentGcodeCoordinateSystem.setCoordinate(gcu);
                    break;
            }
        }
    }

    public void setCurrentGcodeCoordinateSystem(GcodeCoordinateSystem currentGcodeCoordinateSystem) {
        this.currentGcodeCoordinateSystem = currentGcodeCoordinateSystem;
    }
}
