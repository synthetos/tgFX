/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.utility;

import com.sun.media.jfxmedia.logging.Logger;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import tgfx.Main;
import static tgfx.Main.OS;

/**
 *
 * @author ril3y
 */
public class UtilityFunctions {
    
    final static ResourceBundle rb = ResourceBundle.getBundle("version");   //Used to track build date and build number
    
    public static String getOperatingSystem() {
        if (isWindows()) {
            return ("win");
        } else if (isMac()) {
            return ("mac");
        } else if (isUnix()) {
            return ("unix");
        } else if (isLinux()) {
            return ("linux");  //not tested yet 380.08
        } else {
            return ("win");
        }
    }

    private static boolean isLinux() {
        return (OS.indexOf("lin") >= 0);
    }

    private static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    private static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    private static boolean isUnix() {
        return (OS.indexOf("nux") >= 0);
    }

    public void testMessage(String message) {
        Main.print("Message Hit");

    }
    
    
    public static String getBuildInfo(String propToken) {
        String msg = "";
        try {
            msg = rb.getString(propToken);
        } catch (MissingResourceException e) {
            Logger.logMsg(Logger.ERROR, "Error Getting Build Info Token ".concat(propToken).concat(" not in Propertyfile!"));
        }
        return msg;
    }
}
