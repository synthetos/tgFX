/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

import argo.jdom.JsonNode;

/**
 *
 * @author ril3y
 */
public class ResponseHeader {
    //{"r":{"bd":{"":""},"sc":0,"sm":"OK","buf":255,"ln":1,"cks":""}}
    public static String statusMessage = "";
    private static int statusCode = 0;
    private static int bufferAvailable = 254;
    private static long lineNumber = -72;
    
    public ResponseHeader(){
       
    }
    
    public  int getBufferAvailable() {
        return bufferAvailable;
    }
    public  long getLineNumber() {
        return lineNumber;
    }
    public static int getStatusCode() {
        return statusCode;
    }
    public  String getStatusMessage() {
        return statusMessage;
        //TODO THESE IN A HASHMAP IN TinyG Driver
    }
    
    public void parseResponseHeader(JsonNode responseNodeObject){
//        statusMessage = responseNodeObject.getNode("r").getNode("sm").getText();
        statusCode = Integer.valueOf(responseNodeObject.getNode("r").getNode("sc").getText());
        try {
//        	bufferAvailable = Integer.valueOf(responseNodeObject.getNode("r").getNode("buf").getText());
        lineNumber = Long.valueOf(responseNodeObject.getNode("r").getNode("bd").getNode("qr").getNode("lix").getText());
        } catch(Exception pjex) {
        	//bufferAvailable = -1;
        	//lineNumber = -1;
        }
    }    
}
