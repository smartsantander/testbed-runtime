/**********************************************************************************************************************
 * Copyright (c) 2010, coalesenses GmbH                                                                               *
 * All rights reserved.                                                                                               *
 *                                                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
 * following conditions are met:                                                                                      *
 *                                                                                                                    *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
 *   disclaimer.                                                                                                      *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
 *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
 * - Neither the name of the coalesenses GmbH nor the names of its contributors may be used to endorse or promote     *
 *   products derived from this software without specific prior written permission.                                   *
 *                                                                                                                    *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE      *
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,         *
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE *
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY   *
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                                *
 **********************************************************************************************************************/

package de.uniluebeck.itm.wsn.devicedrivers.trisos;

import de.uniluebeck.itm.wsn.devicedrivers.exceptions.ProgramChipMismatchException;
import de.uniluebeck.itm.wsn.devicedrivers.generic.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class FlashProgramOperation extends iSenseDeviceOperation {

	private TrisosDevice device;

	private IDeviceBinFile program;

	//private boolean rebootAfterFlashing;

	public FlashProgramOperation(TrisosDevice device, IDeviceBinFile program, boolean rebootAfterFlashing) {
		super(device);
		this.device = device;
		this.program = program;
		//this.rebootAfterFlashing = rebootAfterFlashing;
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	private boolean programFlash() throws Exception {
		//TrisosBinFile trisosProgram = null;
		// Enter programming mode
		if (!device.enterProgrammingMode()) {
			logError("Unable to enter programming mode");
			return false;
		}

		// Return with success if the user has requested to cancel this
		// operation
		if (isCancelled()) {
			logDebug("Operation has been cancelled");
			device.operationCancelled(this);
			return false;
		}

		// Connection established, determine chip type
		ChipType chipType = device.getChipType();
		// logDebug("Chip type is " + chipType);

		// Check if file and current chip match
		if (!program.isCompatible(chipType)) {
			logError("Chip type(" + chipType + ") and bin-program type(" + program.getFileType() + ") do not match");
			throw new ProgramChipMismatchException(chipType, program.getFileType());
		}

		//trisosProgram = (TrisosBinFile) program;


//		int flash_crc = trisosProgram.calcCRC();

//		System.out.println("CRC " + flash_crc);


                //Call programming tool right here  
                //String exec_prog = null;
                //exec_prog = "avrdude -p m2560 -c jtagmkII -e -U flash:w:"+trisosProgram.getFilename();
                //exec_prog = "..\\programming_atmega.bat "+trisosProgram.getFilename()+"";
                //exec_prog = "..\\JTAGICEmkII\\jtagiceii.exe -d ATmega2560 -e -pa -ia ..\\JTAGICEmkII\\flashMe.elf";
                //exec_prog = "avrdude -p m2560 -c jtagmkII -e -U flash:w:fwtest.hex -P com2 -v"; for happyjtaguse

                /* Fetching properties */
                Properties props = new Properties();
                FileInputStream in = new FileInputStream("../conf/trisos-device-config.properties");
                props.load(in);
                in.close();

                /* Counting parameters for the programmer executable */
                int numberOfParms = 0;
		for(; props.getProperty("trisos.programmer.program.param." + numberOfParms) != null; ++numberOfParms){;}

                /* Assemble command array */
		String command[] = new String[numberOfParms + 1];
		command[0] = props.getProperty("trisos.programmer.program.command");
                if( props.containsKey(command[0]) ) command[0] = props.getProperty(command[0]);
                /* Fetching name of the file to be flashed on the nodes */
		String binFileName = props.getProperty("trisos.programmer.program.binfile");
                /* Assemble command array */
                for( int i = 0; i < numberOfParms; ++i ) {
			command[i + 1] = props.getProperty("trisos.programmer.program.param." + i);
                        if( props.containsKey(command[i + 1] )) command[i + 1] = props.getProperty(command[i + 1]);
			command[i + 1] = command[i + 1].replace("trisos.programmer.program.binfile", binFileName);
		}
                /* Assembling command for debug output */
                String debugString = "";
                for(int i = 0; i < command.length; ++i)
                    debugString += command[i] + " ";
                logDebug("Execute: " + debugString);
                /* Execute command */
                Process p = Runtime.getRuntime().exec(command);
		BufferedReader process_in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                //BufferedReader process_err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line = null;

                /* Had to comment this out because system stops */
                /*while( (line=process_err.readLine())!=null )
                {
                    System.out.println(line);
                }*/

                while( (line=process_in.readLine())!=null )
                {
                    logDebug(line);
                }
                
                p.destroy();
                
		return true;
	}

	/**
	 * @param line
	 */
	public void printLine(byte[] line) {
		for (int i = 0; i < line.length; i++)
			System.out.print(line[i]);
		System.out.println("");
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	public void run() {
		try {
			if (programFlash() && program != null) {
				operationDone(program);
				return;
			}
		} catch (Throwable t) {
			logError("Unhandled error in thread: " + t, t);
			operationDone(t);
			return;
		} finally {
			try {
				device.leaveProgrammingMode();
			} catch (Throwable e) {
				logWarn("Unable to leave programming mode:" + e, e);
			}
		}

		// Indicate failure
		operationDone(null);
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	@Override
	public Operation getOperation() {
		return Operation.PROGRAM;
	}

}
