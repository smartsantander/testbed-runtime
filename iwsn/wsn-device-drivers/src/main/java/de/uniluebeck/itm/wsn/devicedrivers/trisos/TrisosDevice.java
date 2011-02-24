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

import de.uniluebeck.itm.wsn.devicedrivers.exceptions.*;
import de.uniluebeck.itm.wsn.devicedrivers.generic.*;
import de.uniluebeck.itm.wsn.devicedrivers.jennic.FlashType;
import de.uniluebeck.itm.wsn.devicedrivers.jennic.Sectors;
import gnu.io.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TrisosDevice extends iSenseDeviceImpl implements
		SerialPortEventListener {

	private static final Logger log = LoggerFactory
			.getLogger(TrisosDevice.class);

	@Override
	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE:

//                                synchronized (bsl.dataAvailableMonitor) {
//					bsl.dataAvailableMonitor.notifyAll();
//				}
				if (operation == null) {
					receive(inputStream);
				}
				break;
			default:
				logDebug("Serial event (other than data available): " + event);
				break;
		}
	}

	/** */
	private enum ComPortMode {

		Normal, Program
	}

	;

	// iSenseTelos true = Telos node with iSense Os and uart messages with type
	// false = no packet type everything as plain text message
	// private boolean iSenseTelos = true;
//TODO rename
	private final int BSL_DATABITS = SerialPort.DATABITS_8;

	private final int BSL_STOPBITS = SerialPort.STOPBITS_1;

//	private final int BSL_PARITY_EVEN = SerialPort.PARITY_EVEN;

	private final int BSL_PARITY_NONE = SerialPort.PARITY_NONE;

	private final int MAX_OPEN_PORT_RETRIES = 10;

	/*
	 * initial baud rate for communicating over the serial port, can can only be
	 * changed temporarily later on via bsl command
	 */

	private final int READ_BAUDRATE = 115200;

	private final int FLASH_BAUDRATE = 9600;

	/* time out for opening a serial port */

	private final int PORTOPEN_TIMEOUTMS = 1000;

	/**
	 * Strings for saving device state in a memento
	 */
	public static final String MEM_PORT = "Port";

	private String serialPortName = "";

	private SerialPort serialPort = null;

	private InputStream inputStream = null;

	private OutputStream outputStream = null;

//	private BSLTelosb bsl = null;

	private boolean connected;

	//private boolean verify = true;

	/**
	 * @param serialPortName
	 */
	public TrisosDevice(String serialPortName) {
		this.serialPortName = serialPortName;
		connected = false;
		connect();
	}
	/**
	 *
	 */
	public TrisosDevice() {
	}

	private boolean connect() {
		Enumeration allIdentifiers = null;
		CommPortIdentifier portIdentifier = null;
		CommPort commPort = null;
		int tries = 0;
		boolean portOpened = false;
		boolean portFound = false;

		if (serialPort != null) {
			connected = false;
			return true;
		}

		if (serialPortName == null) {
			connected = false;
			return false;
		}

		allIdentifiers = CommPortIdentifier.getPortIdentifiers();
		while (allIdentifiers.hasMoreElements() && !portFound) {
			portIdentifier = (CommPortIdentifier) allIdentifiers.nextElement();
			if (portIdentifier.getName().equals(serialPortName)) {
				portFound = true;
			}
		}

		if (!portFound) {
			logDebug("Failed to connect to port '" + serialPortName
					+ "': port does not exist."
			);
			connected = false;
			return false;
		}

		// open port
		while (tries < MAX_OPEN_PORT_RETRIES && !portOpened) {
			try {
				tries++;
				commPort = portIdentifier.open(this.getClass().getName(),
						PORTOPEN_TIMEOUTMS
				);
				portOpened = true;
			} catch (PortInUseException e) {
				if (tries < MAX_OPEN_PORT_RETRIES) {
					logDebug("Port '" + serialPortName
							+ "' is already in use, retrying to connect..."
					);
					portOpened = false;
				} else {
					logDebug("Port '" + serialPortName
							+ "' is already in use, failed to connect."
					);
					connected = false;
					return false;
				}
			}
		}

		// cancel if opened port is no serial port
		if (!(commPort instanceof SerialPort)) {
			logDebug("Com Port '" + serialPortName
					+ "' is no serial port, will not connect."
			);
			connected = false;
			return false;
		}

		serialPort = (SerialPort) commPort;
		try {
			serialPort.setSerialPortParams(READ_BAUDRATE, BSL_DATABITS,
					BSL_STOPBITS, BSL_PARITY_NONE
			);
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
		} catch (UnsupportedCommOperationException e) {
			logError("Failed to connect to port '" + serialPortName + "'. "
					+ e.getMessage(), e
			);
			connected = false;
			return false;
		}

		serialPort.setRTS(true);
		serialPort.setDTR(true);

		try {
			inputStream = serialPort.getInputStream();
			outputStream = serialPort.getOutputStream();

//			bsl = new BSLTelosb(serialPort);
		} catch (IOException e) {
			logError("Unable to get I/O streams of port " + serialPortName
					+ ", failed to connect.", e
			);
			connected = false;
			return false;
		}

		try {
			serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			logError("Unable to register as event listener for serial port "
					+ serialPortName, e
			);
			connected = false;
			return false;
		}
		serialPort.notifyOnDataAvailable(true);

		logDebug("Device connected to serial port " + serialPort.getName());

		connected = true;
		return true;
	}
	// -------------------------------------------------------------------------

	/**
	 *
	 */
	@Override
	public boolean enterProgrammingMode() throws TimeoutException,
			InvalidChecksumException, ReceivedIncorrectDataException,
			IOException, FlashEraseFailedException,
			UnexpectedResponseException, Exception {
		if (log.isDebugEnabled()) {
			logDebug("enterProgrammingMode()");
		}
		//this.setComPort(ComPortMode.Program);

		//return startBSL();
                return true; //TODO
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	@Override
	public void eraseFlash() throws Exception {
		logWarn("No device connection available (Ignoring action)");
                //TODO erase flash via external tool call
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	@Override
	public ChipType getChipType() throws Exception {
		return ChipType.Unknown;
                //TODO return ChipType.Trisos;
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	@Override
	public FlashType getFlashType() throws Exception {
		return FlashType.Unknown;
                //TODO
	}

	// -------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 *
	 * @see ishell.device.iSenseDeviceImpl#getOperation()
	 */

	@Override
	public Operation getOperation() {
		if (operation == null) {
			return Operation.NONE;
		} else {
			return operation.getOperation();
		}
	}

	// -------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 *
	 * @see ishell.device.iSenseDeviceImpl#leaveProgrammingMode()
	 */
	@Override
	public void leaveProgrammingMode() throws Exception {
		logWarn("No device connection available (Ignoring action)");
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	@Override
	public byte[] readFlash(int address, int len) throws Exception {
		logWarn("No device connection available (Ignoring action)");
                //TODO read flash with external programming tool and return
		return new byte[]{};
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	@Override
	public boolean reset() throws Exception {
		logWarn("No device connection available (Ignoring action)");
                //TODO reset trisos node over jtag or com?
		return false;
	}

	// -------------------------------------------------------------------------

/*
	 * (non-Javadoc)
	 *
	 * @see ishell.device.iSenseDeviceImpl#send(ishell.device.MessagePacket)
	 */

	@Override
	public void send(MessagePacket p) throws Exception {
		// TODO Auto-generated method stub

		if (operationInProgress()) {
			log
					.error("Skipping packet. Another operation already in progress ("
							+ operation.getClass().getName() + ")"
					);
			return;
		}

		byte type = (byte) (0xFF & p.getType());
		byte b[] = p.getContent();

		boolean iSenseStyle = true;

		// if the type was set to 0 send the message without iSense framing to the node
		// e.g. to Contiki or TinyOs Os
		//if (type == 0x64)
		//	iSenseStyle = false;

		if (b == null || type > 0xFF) {
			log.warn("Skipping empty packet or type > 0xFF.");
			return;
		}
		if (b.length > 150) {
			log.warn("Skipping too large packet (length " + b.length + ")");
			return;
		}

		if (iSenseStyle == true){
			// Send start signal DLE STX
			this.outputStream.write(DLE_STX);

			// Send the type escaped
			outputStream.write(type);
			if (type == DLE) {
				outputStream.write(DLE);
			}

			// Transmit each byte escaped
			for (int i = 0; i < b.length; ++i) {
				outputStream.write(b[i]);
				if (b[i] == DLE) {
					outputStream.write(DLE);
				}
			}

			// Send final DLT ETX
			outputStream.write(DLE_ETX);
			outputStream.flush();
		} else {
			// Transmit the byte array without dle framing
			for (int i = 0; i < b.length; ++i) {
				outputStream.write(b[i]);
			}
		}
	}

	@Override
	public void eraseFlash(Sectors.SectorIndex sector) throws Exception {
		logWarn("No device connection available (Ignoring action)");
	}

	// -------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 *
	 * @see ishell.device.iSenseDeviceImpl#shutdown()
	 */

	@Override
	public void shutdown() {
		if (log.isDebugEnabled()) {
			logDebug("Shutting down device");
		}

		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				logDebug("Unable to close input stream: " + e);
			}
		}
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				logDebug("Unable to close output stream: " + e);
			}
		}

		if (serialPort != null) {
			serialPort.setRTS(true);
			serialPort.setDTR(false);

			serialPort.removeEventListener();
			serialPort.close();
			serialPort = null;
			connected = false;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ishell.device.iSenseDeviceImpl#writeFlash(int, byte[], int, int)
	 */

	@Override
	public byte[] writeFlash(int address, byte[] bytes, int offset, int len)
			throws IOException {
            byte[] reply = null;

            try {
                
                if (enterProgrammingMode()) {
                    //TODO start programming over external tool
                }
            } catch (TimeoutException ex) {
                java.util.logging.Logger.getLogger(TrisosDevice.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidChecksumException ex) {
                java.util.logging.Logger.getLogger(TrisosDevice.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ReceivedIncorrectDataException ex) {
                java.util.logging.Logger.getLogger(TrisosDevice.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FlashEraseFailedException ex) {
                java.util.logging.Logger.getLogger(TrisosDevice.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnexpectedResponseException ex) {
                java.util.logging.Logger.getLogger(TrisosDevice.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(TrisosDevice.class.getName()).log(Level.SEVERE, null, ex);
            }

            return reply;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	// -------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 *
	 * @see ishell.device.iSenseDevice#triggerGetMacAddress(boolean)
	 */

	@Override
	public void triggerGetMacAddress(boolean rebootAfterFlashing)
			throws Exception {
		if (log.isDebugEnabled()) {
			log
					.debug("Device getMAC Address triggered but not yet implemented.");
		}
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	@Override
	public boolean triggerProgram(IDeviceBinFile program, boolean rebootAfterFlashing) throws Exception {
		logWarn("No device connection available (Ignoring action)");
		return false;
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	@Override
	public void triggerSetMacAddress(MacAddress mac, boolean rebootAfterFlashing) throws Exception {
		logWarn("No device connection available (Ignoring action)");
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	@Override
	public boolean triggerReboot() throws Exception {
		logWarn("No device connection available (Ignoring action)");
		return false;
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	@Override
	public String toString() {
		return "NullDevice";
	}

	@Override
	public int[] getChannels() {
		int[] channels = {11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
		return channels;
	}


	@Override
	public IDeviceBinFile loadBinFile(String fileName) throws Exception {
		return new TrisosBinFile(fileName);
	}
}
