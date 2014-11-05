/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.ui.gcode;

import java.util.ArrayList;
import tgfx.Main;

/**
 *
 * @author rileyporter
 */
public final class GcodeHistory {

    private ArrayList<String> commandHistory = new ArrayList<>();
    private int commandIndex = -1;

    public GcodeHistory() {
        addCommandToHistory("");
    }

    public void addCommandToHistory(String gcl) {
        commandHistory.add(gcl);
        commandIndex++;
    }

    public void clearCommandHistory() {
        commandHistory.clear();
        addCommandToHistory("");
    }

    public String getNextHistoryCommand() {

        if (commandIndex == 0) {
            commandIndex++; //Edge case when you are at the 0th command
            String _tmpHistory = commandHistory.get(commandIndex);
//            Main.print(" Get Next History got " + _tmpHistory + " at index " + commandIndex);
            return (_tmpHistory);
        } else {

            if (commandIndex == commandHistory.size() - 1) {
                String _tmpHistory = commandHistory.get(commandIndex);
//                Main.print(" Get Next History got " + _tmpHistory + " at index " + commandIndex);
                return (_tmpHistory);
            } else {
                commandIndex++;
                String _tmpHistory = commandHistory.get(commandIndex);
//                Main.print(" Get Next History got " + _tmpHistory + " at index " + commandIndex);
                return (_tmpHistory);
            }
        }
    }

    public String getPreviousHistoryCommand() {
        if (commandIndex == commandHistory.size() - 1) {
            commandIndex--; //Edge case when you are at the last command in the history
            String _tmpHistory = commandHistory.get(commandIndex);
//            Main.print(" Get Next History got " + _tmpHistory + " at index " + commandIndex);
            return (_tmpHistory);
        } else {
            String _tmpHistory = commandHistory.get(commandIndex);
//            Main.print("Get Previous History got " + _tmpHistory + " at index " + commandIndex);
            if (commandIndex == 0) {
                return (_tmpHistory);
            } else {
                commandIndex--; //increment the command history index..
                return (_tmpHistory);
            }
        }
    }
}
