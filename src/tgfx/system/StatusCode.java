/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.system;

/**
 *
 * @author ril3y
 */
public class StatusCode {

    int statusNUmber;
    String message;
    String line;
    String statusType;

    public StatusCode(int sn, String msg, String lne, String type) {
        statusNUmber = sn;
        message = msg;
        line = lne;
        statusType = type;
    }

    public String getStatusType() {
        return statusType;
    }

    public int getStatusNUmber() {
        return statusNUmber;
    }

    public String getMessage() {
        return message;
    }

    public String getLine() {
        return line;
    }
}
