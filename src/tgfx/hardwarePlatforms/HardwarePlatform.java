/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.hardwarePlatforms;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import tgfx.tinyg.TinygDriver;

/**
 *
 * @author ril3y
 */
public class HardwarePlatform {

    private String platformName;
    private Double minimalBuildVersion;
    private String latestVersionUrl;
    private String manufacturer;
    private String firmwareUrl;

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

    private HardwarePlatform() {
    }

    public static HardwarePlatform getInstance() {
        return HardwarePlatformHolder.INSTANCE;
    }

    private static class HardwarePlatformHolder {

        private static final HardwarePlatform INSTANCE = new HardwarePlatform();
    }

    public void applyPlatformConfig(File f) {
    }

    public boolean getPlatformByName(String platformName) throws IOException {
        String filename;
        File file;
        File folder = new File("hardwarePlatforms");
        File[] listOfFiles = folder.listFiles();
        



        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                file = listOfFiles[i].getAbsoluteFile();
                if (file.getName().endsWith(".json")) {

                    try {
                        Gson gson = new Gson();
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        TinygDriver.getInstance().hardwarePlatform = gson.fromJson(br, HardwarePlatform.class);
                        if(TinygDriver.getInstance().hardwarePlatform.getPlatformName().equals(platformName)){
                            return true;
                        }

                    } catch (FileNotFoundException | JsonSyntaxException | JsonIOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
        }
        return false;
    }
}
