/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx;

/**
 *
 * @author ril3y
 */
public class ResponseFooter {
    
    
    //{"b":{"xvm":12000},"f":[1,0,255,1234]}
    //"f":[<protocol_version>, <status_code>, <input_available>, <checksum>]
    
    private int protocolVersion;
    private static int statusCode = 0;
    public static int rxRecvd = 254;
    private static long checkSum;    
    public ResponseFooter(){
       
    }

    
    public int getRxRecvd() {
        return rxRecvd;
    }

    
    public  long getCheckSum() {
        return checkSum;
    }
    
    
    

    
    
    
    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public  void setStatusCode(int statusCode) {
        ResponseFooter.statusCode = statusCode;
    }

    public  void setRxRecvd(int rxRecvd) {
        ResponseFooter.rxRecvd = rxRecvd;
    }

    public  void setCheckSum(long checkSum) {
        ResponseFooter.checkSum = checkSum;
    }

    
    
    
    
    public int getProtocolVersion() {
        return protocolVersion;
    }
    
    public  int getBufferAvailable() {
        return rxRecvd;
    }
  
    public static int getStatusCode() {
        return statusCode;
    }
   
    
//    public void parseResponseFooter(JsonNode responseNodeObject){
//        protocolVersion = Integer.valueOf(responseNodeObject.getNode("f").getElements().get(0).getText());
//        statusCode = Integer.valueOf(responseNodeObject.getNode("f").getElements().get(1).getText());
//        if(statusCode != 0 && statusCode !=60 ){  //60 is a zero length move.
//            TinygDriver.getInstance().serialWriter.setThrottled(true);
//        }
//        rxRecvd = Integer.valueOf(responseNodeObject.getNode("f").getElements().get(2).getText());
//        checkSum = Long.valueOf(responseNodeObject.getNode("f").getElements().get(3).getText());
//    }    
}
