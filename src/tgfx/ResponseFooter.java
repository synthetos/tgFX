/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import argo.jdom.JsonNode;
import tgfx.tinyg.TinygDriver;

/**
 *
 * @author ril3y
 */
public class ResponseFooter {
    
    
    //{"b":{"xvm":12000},"f":[1,0,255,1234]}
    //"f":[<protocol_version>, <status_code>, <input_available>, <checksum>]
    
    private int protocolVersion;
    private static int statusCode = 0;
    private static int inputAvailable = 254;
    private static long checkSum;    
    public ResponseFooter(){
       
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }
    
    public  int getBufferAvailable() {
        return inputAvailable;
    }
  
    public static int getStatusCode() {
        return statusCode;
    }
   
    
    public void parseResponseFooter(JsonNode responseNodeObject){
        protocolVersion = Integer.valueOf(responseNodeObject.getNode("f").getElements().get(0).getText());
        statusCode = Integer.valueOf(responseNodeObject.getNode("f").getElements().get(1).getText());
        if(statusCode != 0 && statusCode !=60 ){  //60 is a zero length move.
            TinygDriver.getInstance().serialWriter.setThrottled(true);
        }
        inputAvailable = Integer.valueOf(responseNodeObject.getNode("f").getElements().get(2).getText());
        checkSum = Long.valueOf(responseNodeObject.getNode("f").getElements().get(3).getText());
    }    
}
