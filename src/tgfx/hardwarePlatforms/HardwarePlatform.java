/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.hardwarePlatforms;

import java.io.File;
import java.util.ArrayList;

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
    private int hardwarePlatformVersion;
    private boolean isUpgradeable;

    public boolean isIsUpgradeable() {
        return isUpgradeable;
    }

    public void setIsUpgradeable(boolean isUpgradeable) {
        this.isUpgradeable = isUpgradeable;
    }
    
    
    
    public int getPlatformHardwareVersion(){
        return this.hardwarePlatformVersion;
    }

    public void setHardwarePlatformVersion(int hardwarePlatformVersion) {
        this.hardwarePlatformVersion = hardwarePlatformVersion;
    }
    
    

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getLatestVersionUrl() {
        return latestVersionUrl;
    }

    public void setLatestVersionUrl(String latestVersionUrl) {
        this.latestVersionUrl = latestVersionUrl;
    }

    public String getFirmwareUrl() {
        return firmwareUrl;
    }

    public void setFirmwareUrl(String firmwareUrl) {
        this.firmwareUrl = firmwareUrl;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Double getMinimalBuildVersion() {
        return minimalBuildVersion;
    }

    public void setMinimalBuildVersion(Double minimalBuildVersion) {
        this.minimalBuildVersion = minimalBuildVersion;
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
