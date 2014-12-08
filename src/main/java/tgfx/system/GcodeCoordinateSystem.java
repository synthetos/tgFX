/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.system;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author rileyporter
 */
public final class GcodeCoordinateSystem {

    private StringProperty coordinateSystemName = new SimpleStringProperty();
    private int coordinateNumber;
    private int coordinateNumberTgFormat;
    private double xOffset;
    private double yOffset;
    private double zOffset;
    private double aOffset;
    private double bOffset;
    private double cOffset;
    

    public GcodeCoordinateSystem(String coordinateName) {
        setCoordinate(coordinateName);
        setCoordinateNumberMnemonic(Integer.valueOf(String.valueOf(coordinateName).substring(1, 2)));

    }
    
    public StringProperty getGcodeCoordinateSystemProperty() {
        return(this.coordinateSystemName);
    }
    
    public GcodeCoordinateSystem() {
        
    }

    public int getCoordinateNumberMnemonic() {
        //Returns a 54 vs a 1 
        return coordinateNumber;
    }

    public int getCoordinateNumberByTgFormat() {
        //Returns a 54 vs a 1 
        return coordinateNumberTgFormat;
    }

    public void setCoordinateNumber(int number) {
        //sets a 1 for g54 etc...
        switch (number) {
            case 1:
                setCoordinate("g54");
                setCoordinateNumber(number);
                setCoordinateNumberMnemonic(54);
                break;
            case 2:
                setCoordinate("g55");
                setCoordinateNumber(number);
                setCoordinateNumberMnemonic(55);
                break;
            case 3:
                setCoordinate("g56");
                setCoordinateNumber(number);
                setCoordinateNumberMnemonic(56);
                break;
            case 4:
                setCoordinate("g57");
                setCoordinateNumber(number);
                setCoordinateNumberMnemonic(57);
                break;
            case 5:
                setCoordinate("g58");
                setCoordinateNumber(number);
                setCoordinateNumberMnemonic(59);
                break;
            case 6:
                setCoordinate("g59");
                setCoordinateNumber(number);
                setCoordinateNumberMnemonic(59);
                break;
        }
    }

    public void setCoordinateNumberMnemonic(int coordinateNumber) {
        if (coordinateNumber > 59 || coordinateNumber < 54) {
            //invalid range
        } else {
            this.coordinateNumber = coordinateNumber;
        }
    }

    public void setCoordinateNumberTgFormat(int coordinateNumberTgFormat) {
        if (coordinateNumberTgFormat > 6 || coordinateNumberTgFormat < 1) {
            //invalid number range
        } else {
            this.coordinateNumberTgFormat = coordinateNumberTgFormat;
        }
    }

    public String getCoordinate() {
        return coordinateSystemName.get();
    }

    public void setCoordinate(String coordinate) {
        this.coordinateSystemName.set(coordinate);
        this.coordinateSystemName.set(coordinate);
    }

    public double getxOffset() {
        return xOffset;
    }

    public void setxOffset(double xOffset) {
        this.xOffset = xOffset;
    }

    public double getyOffset() {
        return yOffset;
    }

    public void setyOffset(double yOffset) {
        this.yOffset = yOffset;
    }

    public double getzOffset() {
        return zOffset;
    }

    public void setzOffset(double zOffset) {
        this.zOffset = zOffset;
    }

    public double getaOffset() {
        return aOffset;
    }

    public void setaOffset(double aOffset) {
        this.aOffset = aOffset;
    }

    public double getbOffset() {
        return bOffset;
    }

    public void setbOffset(double bOffset) {
        this.bOffset = bOffset;
    }

    public double getcOffset() {
        return cOffset;
    }

    public void setcOffset(double cOffset) {
        this.cOffset = cOffset;
    }
}
