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
    
 
    
    
    private SimpleStringProperty  platformName;
    private SimpleDoubleProperty minimalBuildVersion = new SimpleDoubleProperty(0.0);
    private SimpleStringProperty latestVersionUrl = new SimpleStringProperty("");
    private SimpleStringProperty manufacturer = new SimpleStringProperty("");
    private SimpleStringProperty firmwareUrl = new SimpleStringProperty("");
    private SimpleIntegerProperty hardwarePlatformVersion = new SimpleIntegerProperty(-1);

    public SimpleIntegerProperty getHardwarePlatformVersion() {
        return hardwarePlatformVersion;
    }
    private SimpleBooleanProperty isUpgradeable = new SimpleBooleanProperty(false);

    public boolean isIsUpgradeable() {
        return isUpgradeable.get();
    }

    public void setIsUpgradeable(boolean isUpgradeable) {
        this.isUpgradeable.set(isUpgradeable);
    }
    
    
    
    public int getPlatformHardwareVersion(){
        return this.hardwarePlatformVersion.get();
    }

    public void setHardwarePlatformVersion(int hardwarePlatformVersion) {
        this.hardwarePlatformVersion.set(hardwarePlatformVersion);
    }
    
    

    public String getPlatformName() {
        return platformName.get();
    }

    public void setPlatformName(String platformName) {
        this.platformName.set(platformName);
    }

    public String getLatestVersionUrl() {
        return latestVersionUrl.get();
    }

    public void setLatestVersionUrl(String latestVersionUrl) {
        this.latestVersionUrl.set(latestVersionUrl);
    }

    public String getFirmwareUrl() {
        return firmwareUrl.get();
    }

    public void setFirmwareUrl(String firmwareUrl) {
        this.firmwareUrl.set(firmwareUrl);
    }

    public String getManufacturer() {
        return manufacturer.get();
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer.set(manufacturer);
    }

    public Double getMinimalBuildVersion() {
        return minimalBuildVersion.get();
    }

    public void setMinimalBuildVersion(Double minimalBuildVersion) {
        this.minimalBuildVersion.set(minimalBuildVersion);
    }

    public HardwarePlatform() {
    }

    public static HardwarePlatform getInstance() {
        return HardwarePlatformHolder.INSTANCE;
    }

    private static class HardwarePlatformHolder {

        private static final HardwarePlatform INSTANCE = new HardwarePlatform();
    }

    public void applyPlatformConfig(File f) {
    }
    
  
}
