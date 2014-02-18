/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.ui.tinygconfig;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.log4j.Logger;
import tgfx.Main;
import tgfx.system.Axis;
import tgfx.system.Machine;
import tgfx.system.Motor;
import tgfx.tinyg.TinygDriver;

/**
 * FXML Controller class
 *
 * @author rileyporter
 */
public class TinyGConfigController implements Initializable {

    private static final Logger logger = Logger.getLogger(TinyGConfigController.class);
    private static DecimalFormat decimalFormat = new DecimalFormat("#0.000");

    public TinyGConfigController() {
    }
    @FXML
    private static TextField motor1ConfigTravelPerRev, motor2ConfigTravelPerRev, motor3ConfigTravelPerRev, motor4ConfigTravelPerRev,
            motor1ConfigStepAngle, motor2ConfigStepAngle, motor3ConfigStepAngle, motor4ConfigStepAngle,
            axisAmaxFeedRate, axisBmaxFeedRate, axisCmaxFeedRate, axisXmaxFeedRate, axisYmaxFeedRate, axisZmaxFeedRate,
            axisAmaxTravel, axisBmaxTravel, axisCmaxTravel, axisXmaxTravel, axisYmaxTravel, axisZmaxTravel,
            axisAjunctionDeviation, axisBjunctionDeviation, axisCjunctionDeviation, axisXjunctionDeviation, axisYjunctionDeviation, axisZjunctionDeviation,
            axisAsearchVelocity, axisBsearchVelocity, axisCsearchVelocity,
            axisXsearchVelocity, axisYsearchVelocity, axisZsearchVelocity,
            axisAzeroBackoff, axisBzeroBackoff, axisCzeroBackoff, axisXzeroBackoff, axisYzeroBackoff, axisZzeroBackoff,
            axisAmaxVelocity, axisBmaxVelocity, axisCmaxVelocity, axisXmaxVelocity, axisYmaxVelocity, axisZmaxVelocity,
            axisAmaxJerk, axisBmaxJerk, axisCmaxJerk, axisXmaxJerk, axisYmaxJerk, axisZmaxJerk,
            axisAradius, axisBradius, axisCradius, axisXradius, axisYradius, axisZradius,
            axisAlatchVelocity, axisBlatchVelocity, axisClatchVelocity, axisXlatchVelocity, axisYlatchVelocity, axisZlatchVelocity, externalConnections,
            materialThickness, gcodeLoaded, axisXlatchBackoff, axisYlatchBackoff, axisZlatchBackoff, axisAlatchBackoff, axisBlatchBackoff, axisClatchBackoff, MachineStatusInterval;
    @FXML
    private static ChoiceBox motor1ConfigMapAxis, motor2ConfigMapAxis, motor3ConfigMapAxis, motor4ConfigMapAxis,
            motor1ConfigMicroSteps, motor2ConfigMicroSteps, motor3ConfigMicroSteps, motor4ConfigMicroSteps,
            motor1ConfigPolarity, motor2ConfigPolarity, motor3ConfigPolarity, motor4ConfigPolarity,
            motor1ConfigPowerMode, motor2ConfigPowerMode, motor3ConfigPowerMode, motor4ConfigPowerMode,
            axisAmode, axisBmode, axisCmode, axisXmode, axisYmode, axisZmode,
            axisAswitchModeMin, axisAswitchModeMax, axisBswitchModeMin, axisBswitchModeMax, axisCswitchModeMin, axisCswitchModeMax, axisXswitchModeMin, axisXswitchModeMax, axisYswitchModeMin, axisYswitchModeMax,
            axisZswitchModeMin, axisZswitchModeMax, gcodePlane, movementMinLineSegment, movementTimeSegment, movementMinArcSegment, gcodeUnitMode, gcodeCoordSystem;
    @FXML
    private TabPane motorTabPane, axisTabPane;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    void handleEnableAllAxis(ActionEvent evt) throws Exception {
        if (TinygDriver.getInstance().isConnected().get()) {
            tgfx.Main.postConsoleMessage("[+]Enabling All Axis.... Motors Live!.\n");
            logger.info("Enabling All Axis");
            TinygDriver.getInstance().cmdManager.enableAllAxis();

        } else {
            tgfx.Main.postConsoleMessage("[!]TinyG is Not Connected...\n");
        }

    }

    @FXML
    void handleInhibitAllAxis(ActionEvent evt) throws Exception {
        if (TinygDriver.getInstance().isConnected().get()) {
            tgfx.Main.postConsoleMessage("[+]Inhibiting All Axis.... Motors Inhibited... However always verify!\n");
            logger.info("Inhibiting All Axis");
            TinygDriver.getInstance().cmdManager.inhibitAllAxis();

        } else {
            tgfx.Main.postConsoleMessage("[!]TinyG is Not Connected...\n");
        }
    }

    public static void _updateGuiAxisSettings(String axname) {
        Axis ax = TinygDriver.getInstance().machine.getAxisByName(axname);
        _updateGuiAxisSettings(ax);
    }

    
    public static void updateGuiMotorSettings() {
        //No motor was provided... Update them all.
        updateGuiMotorSettings(null);
    }

    public static void updateGuiMotorSettings(final String arg) {
        //Update the GUI for config settings
        Platform.runLater(new Runnable() {
            String MOTOR_ARGUMENT = arg;

            @Override
            public void run() {
                try {
                    if (MOTOR_ARGUMENT == null) {
                        //Update ALL motor's gui settings
                        for (Motor m : TinygDriver.getInstance().machine.getMotors()) {
                            _updateGuiMotorSettings(String.valueOf(m.getId_number()));
                        }
                    } else {
                        //Update only ONE motor's gui settings
                        _updateGuiMotorSettings(MOTOR_ARGUMENT);
                    }
                } catch (Exception ex) {
                    Main.print("[!]Exception in updateGuiMotorSettings...");
                    Main.print(ex.getMessage());
                }
            }
        });
    }
    
    public static void _updateGuiMotorSettings(String motor) {

        switch (TinygDriver.getInstance().machine.getMotorByNumber(Integer.valueOf(motor)).getId_number()) {
            case 1:
                motor1ConfigMapAxis.getSelectionModel().select((TinygDriver.getInstance().machine.getMotorByNumber(1).getMapToAxis()));
                motor1ConfigMicroSteps.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(1).getMicrosteps());
                motor1ConfigPolarity.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(1).isPolarityInt());
                motor1ConfigPowerMode.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(1).isPower_managementInt());
                motor1ConfigStepAngle.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(1).getStep_angle()));
                motor1ConfigTravelPerRev.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(1).getTravel_per_revolution()));
                break;
            case 2:
                motor2ConfigMapAxis.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(2).getMapToAxis());
                motor2ConfigMicroSteps.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(2).getMicrosteps());
                motor2ConfigPolarity.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(2).isPolarityInt());
                motor2ConfigPowerMode.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(2).isPower_managementInt());
                motor2ConfigStepAngle.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(2).getStep_angle()));
                motor2ConfigTravelPerRev.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(2).getTravel_per_revolution()));
                break;
            case 3:
                motor3ConfigMapAxis.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(3).getMapToAxis());
                motor3ConfigMicroSteps.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(3).getMicrosteps());
                motor3ConfigPolarity.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(3).isPolarityInt());
                motor3ConfigPowerMode.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(3).isPower_managementInt());
                motor3ConfigStepAngle.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(3).getStep_angle()));
                motor3ConfigTravelPerRev.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(3).getTravel_per_revolution()));
                break;
            case 4:
                motor4ConfigMapAxis.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(4).getMapToAxis());
                motor4ConfigMicroSteps.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(4).getMicrosteps());
                motor4ConfigPolarity.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(4).isPolarityInt());
                motor4ConfigPowerMode.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(4).isPower_managementInt());
                motor4ConfigStepAngle.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(4).getStep_angle()));
                motor4ConfigTravelPerRev.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(4).getTravel_per_revolution()));
                break;
        }
    }

    public static void _updateGuiAxisSettings(Axis ax) {
        
        switch (ax.getAxis_name().toLowerCase()) {
            case "a":
                axisAmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                axisAmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                axisAmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                axisAjunctionDeviation.setText(String.valueOf(ax.getJunction_devation()));
                axisAmaxVelocity.setText(String.valueOf(ax.getVelocityMaximum()));
                axisAmaxJerk.setText(decimalFormat.format(ax.getJerkMaximum()));
                axisAradius.setText(String.valueOf(ax.getRadius()));
                axisAsearchVelocity.setText(String.valueOf(ax.getSearch_velocity()));
                axisAzeroBackoff.setText(String.valueOf(ax.getZero_backoff()));
                //Rotational Do not have these.
//                axisAsearchVelocity.setDisable(true);
//                axisAlatchVelocity.setDisable(true);
//                axisAlatchBackoff.setDisable(true);
                axisAswitchModeMax.getSelectionModel().select(ax.getMaxSwitchMode().ordinal());
                axisAswitchModeMin.getSelectionModel().select(ax.getMinSwitchMode().ordinal());
                axisAlatchBackoff.setText(String.valueOf(ax.getLatch_backoff()));
                axisAlatchVelocity.setText(String.valueOf(ax.getLatch_velocity()));

//                axisAswitchModeMax.setDisable(true);
//                axisAswitchModeMin.setDisable(true);
//                axisAzeroBackoff.setDisable(true);

                break;
            case "b":
                axisBmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                axisBmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                axisBmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                axisBjunctionDeviation.setText(String.valueOf(ax.getJunction_devation()));
                axisBmaxVelocity.setText(String.valueOf(ax.getVelocityMaximum()));
                axisBmaxJerk.setText(decimalFormat.format(ax.getJerkMaximum()));
                axisBradius.setText(String.valueOf(ax.getRadius()));
                //Rotational Do not have these.
                axisBsearchVelocity.setDisable(true);
                axisBlatchVelocity.setDisable(true);
                axisBlatchBackoff.setDisable(true);
                axisBswitchModeMax.setDisable(true);
                axisBswitchModeMin.setDisable(true);
                axisBzeroBackoff.setDisable(true);

                break;
            case "c":
                axisCmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                axisCmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                axisCmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                axisCjunctionDeviation.setText(String.valueOf(ax.getJunction_devation()));
                axisCmaxVelocity.setText(String.valueOf(ax.getVelocityMaximum()));
                axisCmaxJerk.setText(decimalFormat.format(ax.getJerkMaximum()));
                axisCradius.setText(String.valueOf(ax.getRadius()));

                //Rotational Do not have these.
                axisCsearchVelocity.setDisable(true);
                axisClatchVelocity.setDisable(true);
                axisClatchBackoff.setDisable(true);
                axisCswitchModeMax.setDisable(true);
                axisCswitchModeMin.setDisable(true);
                axisCzeroBackoff.setDisable(true);
                break;
            case "x":
//                axisXradius.setText("NA");
//                axisXradius.setStyle("-fx-text-fill: red");
//                axisXradius.setDisable(true);
//                axisXradius.setEditable(false);
                axisXmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                axisXmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                axisXmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                axisXjunctionDeviation.setText(String.valueOf(ax.getJunction_devation()));
                axisXsearchVelocity.setText(String.valueOf(ax.getSearch_velocity()));
                axisXzeroBackoff.setText(String.valueOf(ax.getZero_backoff()));
                axisXswitchModeMax.getSelectionModel().select(ax.getMaxSwitchMode().ordinal());
                axisXswitchModeMin.getSelectionModel().select(ax.getMinSwitchMode().ordinal());
                axisXmaxJerk.setText(decimalFormat.format(ax.getJerkMaximum()));

                axisXmaxVelocity.setText(String.valueOf(ax.getVelocityMaximum()));
                axisXlatchBackoff.setText(String.valueOf(ax.getLatch_backoff()));
                axisXlatchVelocity.setText(String.valueOf(ax.getLatch_velocity()));
                break;
            case "y":
//                axisYradius.setText("NA");
//                axisYradius.setStyle("-fx-text-fill: red");
//                axisYradius.setDisable(true);
//                axisYradius.setEditable(false);
                axisYmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                axisYmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                axisYmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                axisYjunctionDeviation.setText(String.valueOf(ax.getJunction_devation()));
                axisYsearchVelocity.setText(String.valueOf(ax.getSearch_velocity()));
                axisYzeroBackoff.setText(String.valueOf(ax.getZero_backoff()));
                axisYswitchModeMax.getSelectionModel().select(ax.getMaxSwitchMode().ordinal());
                axisYswitchModeMin.getSelectionModel().select(ax.getMinSwitchMode().ordinal());
                axisYmaxVelocity.setText(String.valueOf(ax.getVelocityMaximum()));
                axisYmaxJerk.setText(decimalFormat.format(ax.getJerkMaximum()));
//                                axisYmaxJerk.setText(String.valueOf(ax.getJerk_maximum()));
                // axisYradius.setText(String.valueOf(ax.getRadius()));
                axisYlatchVelocity.setText(String.valueOf(ax.getLatch_velocity()));
                axisYlatchBackoff.setText(String.valueOf(ax.getLatch_backoff()));
                break;
            case "z":
//                axisZradius.setText("NA");
//                axisZradius.setStyle("-fx-text-fill: red");
//                axisZradius.setDisable(true);
//                axisZradius.setEditable(false);
                axisZmode.getSelectionModel().select(ax.getAxis_mode().ordinal());
                axisZmaxFeedRate.setText(String.valueOf(ax.getFeed_rate_maximum()));
                axisZmaxTravel.setText(String.valueOf(ax.getTravel_maximum()));
                axisZjunctionDeviation.setText(String.valueOf(ax.getJunction_devation()));
                axisZsearchVelocity.setText(String.valueOf(ax.getSearch_velocity()));
                axisZzeroBackoff.setText(String.valueOf(ax.getZero_backoff()));
                axisZswitchModeMin.getSelectionModel().select(ax.getMinSwitchMode().ordinal());
                axisZswitchModeMax.getSelectionModel().select(ax.getMaxSwitchMode().ordinal());
                axisZmaxVelocity.setText(String.valueOf(ax.getVelocityMaximum()));
                axisZmaxJerk.setText(decimalFormat.format(ax.getJerkMaximum()));
//                                axisZmaxJerk.setText(String.valueOf(ax.getJerk_maximum()));
                //axisZradius.setText(String.valueOf(ax.getRadius()));
                axisZlatchVelocity.setText(String.valueOf(ax.getLatch_velocity()));
                axisZlatchBackoff.setText(String.valueOf(ax.getLatch_backoff()));
                break;
        }
    }

    public static void updateGuiAxisSettings(Axis ax) {
        updateGuiAxisSettings(ax);
    }

    public static void updateGuiAxisSettings(String axname) {
        //Update the GUI for Axis Config Settings
        final String AXIS_NAME = axname;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //We are now back in the EventThread and can update the GUI for the CMD SETTINGS
                //Right now this is how I am doing this.  However I think there can be a more optimized way
                //Perhaps by passing a routing message as to which motor was updated then not all have to be updated
                //every time one is.
                try {
                    if (AXIS_NAME == null) {
                        //Axis was not provied as a sting argument.. so we update all of them
                        for (Axis ax : TinygDriver.getInstance().machine.getAllAxis()) {
                            _updateGuiAxisSettings(ax);
                        }
                    } else {
                        //We were provided with a specific axis to update.  Update it.
                        _updateGuiAxisSettings(AXIS_NAME);
                    }
                } catch (Exception ex) {
                    Main.print("[!]EXCEPTION in updateGuiAxisSettings");
                    Main.print("LINE: ");
                    Main.print(ex.getMessage());
                }

            }
        });
    }

    private void updateGUIConfigState() {
        //Update the GUI for config settings
        Platform.runLater(new Runnable() {
            float vel;

            @Override
            public void run() {
                //We are now back in the EventThread and can update the GUI for the CMD SETTINGS
                //Right now this is how I am doing this.  However I think there can be a more optimized way
                //Perhaps by passing a routing message as to which motor was updated then not all have to be updated
                //every time one is.

                try {
                    for (Motor m : TinygDriver.getInstance().machine.getMotors()) {
                        if (m.getId_number() == 1) {
                            motor1ConfigMapAxis.getSelectionModel().select((TinygDriver.getInstance().machine.getMotorByNumber(1).getMapToAxis()));
                            motor1ConfigMicroSteps.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(1).getMicrosteps());
                            motor1ConfigPolarity.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(1).isPolarityInt());
                            motor1ConfigPowerMode.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(1).isPower_managementInt());
                            motor1ConfigStepAngle.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(1).getStep_angle()));
                            motor1ConfigTravelPerRev.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(1).getTravel_per_revolution()));
                        } else if (m.getId_number() == 2) {
                            motor2ConfigMapAxis.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(2).getMapToAxis());
                            motor2ConfigMicroSteps.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(2).getMicrosteps());
                            motor2ConfigPolarity.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(2).isPolarityInt());
                            motor2ConfigPowerMode.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(2).isPower_managementInt());
                            motor2ConfigStepAngle.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(2).getStep_angle()));
                            motor2ConfigTravelPerRev.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(2).getTravel_per_revolution()));
                        } else if (m.getId_number() == 3) {
                            motor3ConfigMapAxis.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(3).getMapToAxis());
                            motor3ConfigMicroSteps.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(3).getMicrosteps());
                            motor3ConfigPolarity.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(3).isPolarityInt());
                            motor3ConfigPowerMode.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(3).isPower_managementInt());
                            motor3ConfigStepAngle.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(3).getStep_angle()));
                            motor3ConfigTravelPerRev.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(3).getTravel_per_revolution()));
                        } else if (m.getId_number() == 4) {
                            motor4ConfigMapAxis.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(4).getMapToAxis());
                            motor4ConfigMicroSteps.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(4).getMicrosteps());
                            motor4ConfigPolarity.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(4).isPolarityInt());
                            motor4ConfigPowerMode.getSelectionModel().select(TinygDriver.getInstance().machine.getMotorByNumber(4).isPower_managementInt());
                            motor4ConfigStepAngle.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(4).getStep_angle()));
                            motor4ConfigTravelPerRev.setText(String.valueOf(TinygDriver.getInstance().machine.getMotorByNumber(4).getTravel_per_revolution()));
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Exception in updateGUIConfigState");
                    logger.error(ex.getMessage());
                }

            }
        });
    }

    @FXML
    void handleMotorQuerySettings(ActionEvent evt) {
        Main.print("[+]Querying Motor Config...");
//        Detect what motor tab is "active"...
        try {
            //            updateGuiAxisSettings();
            switch (motorTabPane.getSelectionModel().getSelectedItem().getText()) {
                case "Motor 1":
                    TinygDriver.getInstance().queryHardwareSingleMotorSettings(1);
                    break;
                case "Motor 2":
                    TinygDriver.getInstance().queryHardwareSingleMotorSettings(2);
                    break;
                case "Motor 3":
                    TinygDriver.getInstance().queryHardwareSingleMotorSettings(3);
                    break;
                case "Motor 4":
                    TinygDriver.getInstance().queryHardwareSingleMotorSettings(4);
                    break;
            }
        } catch (Exception ex) {
            Main.print("[!]Error Querying Single Motor....");
        }
    }

    @FXML
    private void handleAxisApplySettings(ActionEvent evt) {
        tgfx.Main.postConsoleMessage("[+]Applying Axis...\n");
        try {

            TinygDriver.getInstance().applyHardwareAxisSettings(axisTabPane.getSelectionModel().getSelectedItem());

            //TODO:  Breakout Individual response messages vs having to call queryAllHardwareAxisSettings
            //something like if {"1po":1} then parse and update only the polarity setting
//            Thread.sleep(TinygDriver.CONFIG_DELAY);
//            TinygDriver.getInstance().queryAllHardwareAxisSettings();

        } catch (Exception ex) {
            Main.print(ex.getMessage());
            Main.print("ERROR IN HANDLEAPPLYAXISSETTINGS");
        }

    }

    @FXML
    private void handleAxisQuerySettings(ActionEvent evt) throws Exception {
        String _axisSelected = axisTabPane.getSelectionModel().getSelectedItem().getText().toLowerCase();
        tgfx.Main.postConsoleMessage("[+]Querying Axis: " + _axisSelected + "\n");
        try {
            TinygDriver.getInstance().queryHardwareSingleAxisSettings(_axisSelected.charAt(0));
        } catch (Exception ex) {
            Main.print("[!]Error Querying Axis: " + _axisSelected);
        }
    }
    
    @FXML
    private void handleMotorEnter(final InputEvent event) throws Exception {
        //private void handleEnter(ActionEvent event) throws Exception {
        final KeyEvent keyEvent = (KeyEvent) event;
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            TextField tf = (TextField) event.getSource();
            Motor _motor = TinygDriver.getInstance().machine.getMotorByNumber((motorTabPane.getSelectionModel().getSelectedItem().getText().toLowerCase().split(" "))[1]);
            //TODO: move the applyHardwareMotorSettings to this controller vs TinyGDriver.
             try {
                    tgfx.Main.postConsoleMessage("[+]Applying "+ tf.getId()+ ":"+ tf.getText()+"... \n");
                     TinygDriver.getInstance().applyHardwareMotorSettings(_motor, tf);
                } catch (NumberFormatException ex) {
                    tgfx.Main.postConsoleMessage("[!]" + tf.getText() +" is an invalid Setting Entered.. Ignoring.");
                    logger.error(ex.getMessage());
                    TinygDriver.getInstance().queryHardwareSingleMotorSettings(_motor.getId_number()); //This will reset the input that was bad to the current settings
                }
            }
        }
    

    @FXML
    private void handleAxisEnter(final InputEvent event) throws Exception {
        //private void handleEnter(ActionEvent event) throws Exception {
        final KeyEvent keyEvent = (KeyEvent) event;
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            Axis _axis = Machine.getInstance().getAxisByName(axisTabPane.getSelectionModel().getSelectedItem().getText().toLowerCase().substring(0, 1));
            if (event.getSource().toString().startsWith("TextField")) {
                //Object Returned is a TextField Object
                TextField tf = (TextField) event.getSource();
                try {
                    tgfx.Main.postConsoleMessage("[+]Applying Axis.......\n");
                    TinygDriver.getInstance().applyHardwareAxisSettings(_axis, tf);
                } catch (NumberFormatException ex) {
                    tgfx.Main.postConsoleMessage("[!]Invalid Setting Entered.. Ignoring.");
                    logger.error(ex.getMessage());
                    TinygDriver.getInstance().queryHardwareSingleAxisSettings(_axis.getAxis_name()); //This will reset the input that was bad to the current settings
                }
            }
        }
    }

    @FXML
    void handleMotorApplySettings(ActionEvent evt) {
        tgfx.Main.postConsoleMessage("[+]Applying Motor.......\n");
        try {
            TinygDriver.getInstance().applyHardwareMotorSettings(motorTabPane.getSelectionModel().getSelectedItem());
        } catch (Exception ex) {
            logger.error("Exception in handleMotorApplySettings" + ex.getMessage());
        }
    }
}
