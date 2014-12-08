/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.tinyg;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author ril3y
 */
public class QueueReport {

    private int pba = 24;
    private int added = 0;
    private int removed = 0;
    private static final Logger logger = Logger.getLogger(QueueReport.class);

    public synchronized void parse(JSONObject js) throws JSONException {
        logger.info("QUEUE REPORT");
        JSONArray jsa;
        jsa = js.getJSONArray("qr");
        setPba(jsa.getInt(0));
        setAdded(jsa.getInt(1));
        setRemoved(jsa.getInt(2));
    }

    public int getAdded() {
        return added;
    }

    public void setAdded(int added) {
        this.added = added;
    }

    public int getRemoved() {
        return removed;
    }

    public void setRemoved(int removed) {
        this.removed = removed;
    }

    public int getPba() {
        return pba;
    }

    private QueueReport() {
    }

    public void setPba(int pba) {
        this.pba = pba;
    }

    public static QueueReport getInstance() {
        return QueueReportHolder.INSTANCE;
    }

    private static class QueueReportHolder {

        private static final QueueReport INSTANCE = new QueueReport();
    }
}
