/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.hardwarePlatforms;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import tgfx.tinyg.TinygDriver;

/**
 *
 * @author ril3y
 */
public class HardwarePlatformManager {
    private ArrayList<HardwarePlatform> availablePlatforms = new ArrayList<>();
    private File folder = new File("hardwarePlatforms");
    static final Logger logger = Logger.getLogger(HardwarePlatformManager.class);
    
    public HardwarePlatformManager() {
        this.LoadPlatforConfigs();
        
    }
    
    public boolean applyPlatformByName(String name){
        HardwarePlatform _t = availablePlatforms.iterator().next();
       while(availablePlatforms.iterator().hasNext()){
           if(_t.getPlatformName().equals(name)){
               TinygDriver.getInstance().hardwarePlatform = _t;
               logger.info("Applied " + name + " hardware Profile to System");
               return true;
           }
       }
       return false;
    }
    
    
    private int LoadPlatforConfigs() {
        File file;
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                file = listOfFiles[i].getAbsoluteFile();
                if (file.getName().endsWith(".json")) {
                    try {
                        Gson gson = new Gson();
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        HardwarePlatform hp = gson.fromJson(br, HardwarePlatform.class);
                        availablePlatforms.add(hp);
                       
                    } catch (FileNotFoundException | JsonSyntaxException | JsonIOException ex) {
                        logger.error("Error loading hardware platforms: "  + ex.getMessage());
                    }
                }
            }
        }
        logger.info("Loaded " + availablePlatforms.size() +" platform files");
        return availablePlatforms.size();
    }
    
    public static HardwarePlatformManager getInstance() {
        return HardwarePlatformManagerHolder.INSTANCE;
    }
    
    private static class HardwarePlatformManagerHolder {

        private static final HardwarePlatformManager INSTANCE = new HardwarePlatformManager();
    }
    
    private void updatePlatformFiles(){
        //todo code in support for updating platform files from remote server
    }
    
    
}
