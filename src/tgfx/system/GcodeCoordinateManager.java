/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.system;


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

    public GcodeCoordinateManager() {
        
    g54 = new GcodeCoordinateSystem("g54");
    g55 = new GcodeCoordinateSystem("g55");
    g56 = new GcodeCoordinateSystem("g56");
    g57 = new GcodeCoordinateSystem("g57");
    g58 = new GcodeCoordinateSystem("g58");
    g59 = new GcodeCoordinateSystem("g59");
    currentGcodeCoordinateSystem = new GcodeCoordinateSystem();

    
    }
    
   
    
}
