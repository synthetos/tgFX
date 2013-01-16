/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.tinyg;

import org.apache.log4j.Logger;

/**
 *
 * @author ril3y
 */
public class CommandManager {
    
    private static Logger logger = Logger.getLogger(CommandManager.class);

    
    public static final String CMD_QUERY_COORDINATE_SYSTEM = "{\"coor\":\"\"}\n";
    public static final String CMD_QUERY_HARDWARE_BUILD_NUMBER = "{\"fb\":\"\"}\n";
    public static final String CMD_QUERY_HARDWARE_FIRMWARE_NUMBER = "{\"fv\":\"\"}\n";
    public static final String CMD_QUERY_OK_PROMPT = "{\"gc\":\"?\"}\n";
    
    
    public static final String CMD_QUERY_STATUS_REPORT = "{\"sr\":\"\"}\n";
    public static final String CMD_QUERY_AXIS_X = "{\"x\":null}\n";
    public static final String CMD_QUERY_AXIS_Y = "{\"y\":null}\n";
    public static final String CMD_QUERY_AXIS_Z = "{\"z\":null}\n";
    public static final String CMD_QUERY_AXIS_A = "{\"a\":null}\n";
    public static final String CMD_QUERY_AXIS_B = "{\"b\":null}\n";
    public static final String CMD_QUERY_AXIS_C = "{\"c\":null}\n";
    
    public static final String CMD_QUERY_MOTOR_1_SETTINGS = "{\"1\":null}\n";
    public static final String CMD_QUERY_MOTOR_2_SETTINGS = "{\"2\":null}\n";
    public static final String CMD_QUERY_MOTOR_3_SETTINGS = "{\"3\":null}\n";
    public static final String CMD_QUERY_MOTOR_4_SETTINGS = "{\"4\":null}\n";
    public static final String CMD_QUERY_SYSTEM_SETTINGS = "{\"sys\":null}\n";
    public static final String CMD_APPLY_SYSTEM_ZERO_ALL_AXIS = "{\"gc\":\"g92x0y0z0a0\"}\n";
    
    
    public static final String CMD_APPLY_SYSTEM_GCODE_UNITS_INCHES = "{\""+MnemonicManager.MNEMONIC_SYSTEM_GCODE_UNIT_MODE+"\":0}\n"; //0=inches
    public static final String CMD_APPLY_SYSTEM_GCODE_UNITS_MM = "{\""+MnemonicManager.MNEMONIC_SYSTEM_GCODE_UNIT_MODE+"\":1}\n"; //1=mm
    public static final String CMD_APPLY_SYSTEM_DISABLE_LOCAL_ECHO = "{\""+MnemonicManager.MNEMONIC_SYSTEM_ENABLE_ECHO+"\":0}\n";
    public static final String CMD_APPLY_SYSTEM_ENABLE_LOCAL_ECHO = "{\""+MnemonicManager.MNEMONIC_SYSTEM_ENABLE_ECHO+"\":0}\n";
    public static final String CMD_APPLY_SYSTEM_MNEMONIC_SYSTEM_SWITCH_TYPE_NC = "{\""+MnemonicManager.MNEMONIC_SYSTEM_SWITCH_TYPE+"\":1}\n";
    
    public static final String CMD_QUERY_SYSTEM_GCODE_UNIT_MODE = "{\""+MnemonicManager.MNEMONIC_SYSTEM_GCODE_UNIT_MODE+"\":null}\n";
    public static final String CMD_QUERY_SYSTEM_GCODE_PLANE = "{\""+MnemonicManager.MNEMONIC_SYSTEM_GCODE_PLANE+"\":null}\n";
    
    public static final String CMD_APPLY_DISABLE_HASHCODE = "{\"eh\":0\"}\n";
    public static final String CMD_APPLY_DEFAULT_SETTINGS = "{\"defaults\":1}\n";
    public static final String CMD_APPLY_STATUS_UPDATE_INTERVAL = "{\"si\":200}\n";
    public static final String CMD_APPLY_JSON_VOBERSITY = "{\"jv\":3}\n";
    public static final String CMD_APPLY_PAUSE = "!\n";
    public static final String CMD_APPLY_RESUME = "~\n";
    
//    public static final String CMD_APPLY_RESET = "\x18\n";
    public static final String CMD_APPLY_DISABLE_XON_XOFF = "{\"ex\":1}\n";
    public static final String CMD_ZERO_ALL_AXIS = "{\"gc\":G920g0x0y0z0}\n";

    public CommandManager() {
    }
    
    
     /**
     *
     * Query All Motors for their current settings
     */
    public void queryAllMotorSettings() throws Exception {
         
        try {
            TinygDriver.getInstance().write(CMD_QUERY_MOTOR_1_SETTINGS);
            logger.info("[+]Getting Motor 1 Settings");

            TinygDriver.getInstance().write(CMD_QUERY_MOTOR_2_SETTINGS);
            logger.info("[+]Getting Motor 2 Settings");

            TinygDriver.getInstance().write(CMD_QUERY_MOTOR_3_SETTINGS);
            logger.info("[+]Getting Motor 3 Settings");

            TinygDriver.getInstance().write(CMD_QUERY_MOTOR_4_SETTINGS);
            logger.info("[+]Getting Motor 4 Settings");

        } catch (Exception ex) {
            logger.error("[!]Exception in getAllMotorSettings()...");
            logger.error(ex.getMessage());
        }
    }
    
    /**
     * writes the commands to query current hardware settings on the tinyg board
     * @throws Exception
     */
    public void queryAllHardwareAxisSettings() throws Exception {
        try {

            System.out.println("[+]Getting A AXIS Settings");
            TinygDriver.getInstance().write(CommandManager.CMD_QUERY_AXIS_A);

            System.out.println("[+]Getting B AXIS Settings");
            TinygDriver.getInstance().write(CommandManager.CMD_QUERY_AXIS_B);

            System.out.println("[+]Getting C AXIS Settings");
            TinygDriver.getInstance().write(CommandManager.CMD_QUERY_AXIS_C);

            TinygDriver.getInstance().write(CommandManager.CMD_QUERY_AXIS_X);
            System.out.println("[+]Getting X AXIS Settings");

            TinygDriver.getInstance().write(CommandManager.CMD_QUERY_AXIS_Y);
            System.out.println("[+]Getting Y AXIS Settings");

            TinygDriver.getInstance().write(CommandManager.CMD_QUERY_AXIS_Z);
            System.out.println("[+]Getting Z AXIS Settings");

        } catch (Exception ex) {
            logger.error("[!]Error in queryHardwareAxisSettings()");
        }
    }
}
