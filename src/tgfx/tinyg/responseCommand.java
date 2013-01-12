/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.tinyg;

import org.json.JSONObject;

/**
 *
 * @author ril3y
 */
public class responseCommand {
    private String settingParent;
    private String settingKey;
    private String settingValue;
    
    public responseCommand(){
        
    }
    public responseCommand(String sp, String sk, String sv){
        settingParent = sp;
        settingKey = sk;
        settingValue = sv;
                
    }

    public String getSettingParent() {
        return settingParent;
    }

    public void setSettingParent(String settingParent) {
        this.settingParent = settingParent;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }
    
    public JSONObject buildJsonObject() throws Exception{
        return(new JSONObject("{\"" + this.getSettingParent() + "\":{\"" + this.getSettingKey() + "\":" + this.getSettingValue() + "}}"));
    }
    
    
    
}
