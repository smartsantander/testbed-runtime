/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.uniluebeck.itm.wsn.devicedrivers.trisos;

import de.uniluebeck.itm.wsn.devicedrivers.generic.Operation;
import de.uniluebeck.itm.wsn.devicedrivers.generic.iSenseDeviceOperation;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 *
 * @author teublert
 */
public class TrisosResetOperation  extends iSenseDeviceOperation {

    private TrisosDevice device;

    public TrisosResetOperation(TrisosDevice device) {
            super(device);
            this.device = device;

    }

    @Override
    public Operation getOperation() {
        return Operation.RESET;
    }

    @Override
    public void run() {
        try {
            if (resetNode()) {
                operationDone(true);
                return;
            }
        } catch (Throwable t) {
            logError("Unhandled error in thread: " + t, t);
            operationDone(t);
            return;
        } finally {
            try {
                device.operationDone(Operation.RESET, device);
            } catch (Throwable e) {
                logWarn("Error in reset:" + e, e);
            }
        }

            // Indicate failure
            operationDone(false);
        }

    private boolean resetNode() throws IOException {

        // Return with success if the user has requested to cancel this
        // operation
        if (isCancelled()) {
                logDebug("Operation has been cancelled");
                device.operationCancelled(this);
                return true;
        }

        Properties props = new Properties();
        FileInputStream in = new FileInputStream("../conf/trisos-device-config.properties");
        props.load(in);
        in.close();

        /* Counting parameters for the programmer executable */
        int numberOfParms = 0;
        for(; props.getProperty("trisos.programmer.reset.param." + numberOfParms) != null; ++numberOfParms){;}

        /* Assemble command array */
        String command[] = new String[numberOfParms + 1];
        command[0] = props.getProperty("trisos.programmer.reset.command");
        if( props.containsKey(command[0]) ) command[0] = props.getProperty(command[0]);
        /* Assemble command array */
        for( int i = 0; i < numberOfParms; ++i ) {
                command[i + 1] = props.getProperty("trisos.programmer.reset.param." + i);
                if( props.containsKey(command[i + 1] )) command[i + 1] = props.getProperty(command[i + 1]);
        }
        /* Assembling command for debug output */
        String debugString = "";
        for(int i = 0; i < command.length; ++i)
            debugString += command[i] + " ";
        logDebug("Execute: " + debugString);
        /* Execute command */
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader process_in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null;

        while( (line=process_in.readLine())!=null ) {
            logDebug(line);
        }

        p.destroy();

        return true;
    }

}
