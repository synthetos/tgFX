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
    private static int bufferAvailable = 0;
    private static double lineNumber;
    
    public ResponseHeader(){
       
    }
    
    public  int getBufferAvailable() {
        return bufferAvailable;
    }
    public  double getLineNumber() {
        return lineNumber;
    }
    public static int getStatusCode() {
        return statusCode;
    }
    public  String getStatusMessage() {
        return statusMessage;
    }
    
    public void parseResponseHeader(JsonNode responseNodeObject){
        statusMessage = responseNodeObject.getNode("r").getNode("sm").getText();
        statusCode = Integer.valueOf(responseNodeObject.getNode("r").getNode("sc").getText());
        bufferAvailable = Integer.valueOf(responseNodeObject.getNode("r").getNode("buf").getText());
        lineNumber = Double.valueOf(responseNodeObject.getNode("r").getNode("ln").getText());
    }
    
}


//
//   private String[] getStatusMessage(JsonRootNode json) {
//        /**
//         * This function parses all return status codes and messages before
//         * anything else
//         */
//        String statusMessage;
//        String statusCode;
//        try {
//            statusMessage = json.getNode("r").getNode("sm").getText();
//            statusCode = json.getNode("r").getNode("sc").getText();
//            String[] ret = {statusMessage, statusCode};
//            return (ret);
//        } catch (Exception ex) {
//            String[] ret = {"JSON Invalid", "-1"};
//            return (ret);
//        }
//    }
