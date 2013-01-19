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
    String statusType;
    
    

//    private emum
//    "INTERNAL","GCODE","INPUT","LOW-LEVEL"
//    // Input errors (400's, if you will)
//    List<String> places = Arrays.asList(new StatusCode(40, "Unrecgonized Command", line),
//            );
    
    private static final int TG_UNRECOGNIZED_COMMAND = 40;      // parser didn't recognize the command
    private static final int TG_EXPECTED_COMMAND_LETTER = 41;   // malformed line to parser
    private static final int TG_BAD_NUMBER_FORMAT = 42;         // number format error
    private static final int TG_INPUT_EXCEEDS_MAX_LENGTH = 43;  // input string is too long 
    private static final int TG_INPUT_VALUE_TOO_SMALL = 44;     // input error: value is under minimum
    private static final int TG_INPUT_VALUE_TOO_LARGE = 45;     // input error: value is over maximum
    private static final int TG_INPUT_VALUE_RANGE_ERROR = 46;   // input error: value is out-of-range
    private static final int TG_INPUT_VALUE_UNSUPPORTED = 47;   // input error: value is not supported
    private static final int TG_JSON_SYNTAX_ERROR = 48;         // JSON string is not well formed
    private static final int TG_JSON_TOO_MANY_PAIRS = 49;       // JSON string or has too many JSON pairs
// Gcode and machining errors
    private static final int TG_ZERO_LENGTH_MOVE = 60;          // move is zero length
    private static final int TG_GCODE_BLOCK_SKIPPED = 61;       // block is too short - was skipped
    private static final int TG_GCODE_INPUT_ERROR = 62;         // general error for gcode input 
    private static final int TG_GCODE_FEEDRATE_ERROR = 63;      // move has no feedrate
    private static final int TG_GCODE_AXIS_WORD_MISSING = 64;   // command requires at least one axis present
    private static final int TG_MODAL_GROUP_VIOLATION = 65;     // gcode modal group error
    private static final int TG_HOMING_CYCLE_FAILED = 66;       // homing cycle did not complete
    private static final int TG_MAX_TRAVEL_EXCEEDED = 67;
    private static final int TG_MAX_SPINDLE_SPEED_EXCEEDED = 68;
    private static final int TG_ARC_SPECIFICATION_ERROR = 69;   // arc specification error

//    public StatusCode mapIntToStatusCode(int sc) {
//        switch (sc) {
//            case(TG_UNRECOGNIZED_COMMAND):
//                
//        }
//    }

    public StatusCode(int sn, String msg, String type) {
        statusNUmber = sn;
        message = msg;
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

}
