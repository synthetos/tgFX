/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.tinyg;

import argo.jdom.JsonRootNode;

/**
 *
 * @author ril3y
 */
public class QueueReport {
    
    private int lineIndex;
    private int pba = 24;

    public int getLineIndex() {
        return lineIndex;
    }

    public synchronized int getPba() {
        return pba;
    }
    
    public synchronized void updateQueue(JsonRootNode json, String line){
        lineIndex = Integer.valueOf(json.getNode("r").getNode("bd").getNode("qr").getNode("lix").getText());
        pba = Integer.valueOf(json.getNode("r").getNode("bd").getNode("qr").getNode("pba").getText());
    }
    
    private QueueReport() {
    }
    
    public static QueueReport getInstance() {
        return QueueReportHolder.INSTANCE;
    }
    
    private static class QueueReportHolder {

        private static final QueueReport INSTANCE = new QueueReport();
    }
    
    
}
