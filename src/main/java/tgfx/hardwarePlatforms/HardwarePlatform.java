/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.hardwarePlatforms;

import java.io.File;
import java.util.ArrayList;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author ril3y
 */
public class HardwarePlatform {

    
    private ArrayList<HardwarePlatform> availablePlatforms = new ArrayList<>();
    
 
    private String platformName;
    private Double minimalBuildVersion;
    private String latestVersionUrl;
    private String manufacturer;
    private String firmwareUrl;
    private int hardwarePlatformVersion = -1;
    private boolean isUpgradeable;

    public int getHardwarePlatformVersion() {
        return hardwarePlatformVersion;
    }

    public void setHardwarePlatformVersion(int hardwarePlatformVersion) {
        this.hardwarePlatformVersion = hardwarePlatformVersion;
    }

    public boolean isIsUpgradeable() {
        return isUpgradeable;
    }

    public void setIsUpgradeable(boolean isUpgradeable) {
        this.isUpgradeable = isUpgradeable;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public Double getMinimalBuildVersion() {
        return minimalBuildVersion;
    }

    public void setMinimalBuildVersion(Double minimalBuildVersion) {
        this.minimalBuildVersion = minimalBuildVersion;
    }

    public String getLatestVersionUrl() {
        return latestVersionUrl;
    }

    public void setLatestVersionUrl(String latestVersionUrl) {
        this.latestVersionUrl = latestVersionUrl;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getFirmwareUrl() {
        return firmwareUrl;
    }

    public void setFirmwareUrl(String firmwareUrl) {
        this.firmwareUrl = firmwareUrl;
    }
   

    public HardwarePlatform() {
    }

    public static HardwarePlatform getInstance() {
        return HardwarePlatformHolder.INSTANCE;
    }

    private static class HardwarePlatformHolder {

        private static final HardwarePlatform INSTANCE = new HardwarePlatform();
    }


  
}
