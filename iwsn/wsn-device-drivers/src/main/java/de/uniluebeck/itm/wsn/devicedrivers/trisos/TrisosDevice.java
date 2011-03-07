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

import de.uniluebeck.itm.tr.util.TimeDiff;
import de.uniluebeck.itm.wsn.devicedrivers.exceptions.*;
import de.uniluebeck.itm.wsn.devicedrivers.generic.*;
import de.uniluebeck.itm.wsn.devicedrivers.jennic.FlashType;
import de.uniluebeck.itm.wsn.devicedrivers.jennic.Sectors.SectorIndex;
import gnu.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;


public class TrisosDevice extends iSenseDeviceImpl implements SerialPortEventListener {

    	private enum ComPortMode {
		Normal, Program
	}
	private String serialPortName = "";

	private SerialPort serialPort = null;
	private InputStream inputStream = null;
	private OutputStream outputStream = null;
	private int baudrate = 38400;
	private int stopbits = SerialPort.STOPBITS_1;
	private int databits = SerialPort.DATABITS_8;
	private int parityBit = SerialPort.PARITY_NONE;
	private int receiveTimeout = 2000;
	private static final int MAX_RETRIES = 5;
        private boolean connected;
        private Object dataAvailableMonitor = new Object();



	public TrisosDevice(String serialPortName) {
                logDebug("TriSosDevice CONSTRUCTOR");
                logDebug("String serialPortName="+serialPortName);
		this.serialPortName = serialPortName;
		connected = false;
		connect();
                setReceiveMode(MessageMode.PLAIN);
	}

	public boolean connect() {
		if (serialPortName == null) {
			return false;
		}
		// if(!connected){
		if (serialPortName != null && serialPort == null) {
			try {
				setSerialPort(serialPortName);
				if (serialPort == null) {
					logDebug("connect(): serialPort==null");
				}
			} catch (PortInUseException piue) {
				logDebug("Port already in use. Connection will be removed. ");
				if (serialPort != null) {
					serialPort.close();
				}
				// this.owner.removeConnection(this);
				return false;
			} catch (Exception e) {
				if (serialPort != null) {
					serialPort.close();
				}
				logDebug("Port does not exist. Connection will be removed. " + e, e);
				return false;
			}
			return true;
		}
		return true;
	}


	@SuppressWarnings("unchecked")
	public void setSerialPort(String port) throws Exception {
		logDebug("TrisosDevice.setSerialPort({})", port);
		CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(port);
                logDebug("TrisosDevice cpi.getName()={}", cpi.getName());
		SerialPort sp = null;
		CommPort commPort = null;
		for (int i = 0; i < MAX_RETRIES; i++) {
			try {
				commPort = cpi.open(this.getClass().getName(), 1000);
				break;
			} catch (PortInUseException piue) {
				logDebug("Port in Use Retrying to connect");
				if (i >= MAX_RETRIES - 1) {
					throw (piue);
				}
				Thread.sleep(200);
			}
		}

		if (commPort instanceof SerialPort) {
			sp = (SerialPort) commPort;
		} else {
			logDebug("Port is no SerialPort");
		}

		serialPort = sp;
		serialPort.addEventListener(this);
		serialPort.notifyOnDataAvailable(true);
		serialPort.setSerialPortParams(baudrate, databits, stopbits, parityBit);

		outputStream = new BufferedOutputStream(serialPort.getOutputStream());
		inputStream = new BufferedInputStream(serialPort.getInputStream());
		connected = true;
	}


    

    @Override
    public void send(MessagePacket p) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void leaveProgrammingMode() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Operation getOperation() {
        if (operation == null) {
                return Operation.NONE;
        } else {
                return operation.getOperation();
        }
    }

    @Override
    public boolean reset() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean enterProgrammingMode() throws Exception {
        //throw new UnsupportedOperationException("Not supported yet.");
        logDebug("Switched to ProgrammingMode on TrisosDevice");
        return true;
    }

    @Override
    public void eraseFlash() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void eraseFlash(SectorIndex sector) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] writeFlash(int address, byte[] bytes, int offset, int len) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] readFlash(int address, int len) throws Exception {

		// Send flash program request
		sendBootLoaderMessage(Messages.flashReadRequestMessage(address, len));

		// Read flash program response
		byte[] response = receiveBootLoaderReply(Messages.FLASH_READ_RESPONSE);

		// Remove type and success octet
		byte[] data = new byte[response.length - 2];
		System.arraycopy(response, 2, data, 0, response.length - 2);

		// Return data
		return data;
    }

    /**
     *
     */
    protected void flushReceiveBuffer() {
            long i = 0;
            // logDebug("Flushing serial rx buffer");

            if (isConnected()) {

                    try {
                            while ((i = inputStream.available()) > 0) {
                                    logDebug("Skipping " + i + " characters while flushing on the serial rx");
                                    inputStream.skip(i);
                            }
                    } catch (IOException e) {
                            logWarn("Error while serial rx flushing buffer: " + e, e);
                    }
            }
    }

    /**
     *
     */
    protected boolean waitForConnection() {
            try {
                    // Send flash read request (in fact, this could be any valid message
                    // to which the
                    // device is supposed to respond)
                    sendBootLoaderMessage(Messages.flashReadRequestMessage(0x24, 0x20));
                    receiveBootLoaderReply(Messages.FLASH_READ_RESPONSE);
                    logInfo("Device connection established");
                    return true;
            } catch (TimeoutException to) {
                    logDebug("Still waiting for a connection.");
            } catch (Exception error) {
                    logWarn("Exception while waiting for connection", error);
            }

            flushReceiveBuffer();
            return false;
    }

    /**
     *
     */
    private void sendBootLoaderMessage(byte[] message) throws IOException {
            // Allocate buffer for length + message + checksum
            byte[] data = new byte[message.length + 2];

            // Prepend length (of message + checksum)
            data[0] = (byte) (message.length + 1);

            // Copy message into the buffer
            System.arraycopy(message, 0, data, 1, message.length);

            // Calculate and append checksum
            data[data.length - 1] = Messages.calculateChecksum(data, 0, data.length - 1);

            // Send message
            outputStream.write(data);
            outputStream.flush();
            System.out.println("SEND DATA(byte[]) TO TRISOS:"+data);
    }

    /**
     *
     */
    protected byte[] receiveBootLoaderReply(int type) throws TimeoutException, UnexpectedResponseException,
                    InvalidChecksumException, IOException, NullPointerException {

            waitDataAvailable(inputStream, receiveTimeout);
            // Read message length
            int length = (int) inputStream.read();

            // Allocate message buffer
            byte[] message = new byte[length - 1];

            // Read rest of the message (except the checksum
            for (int i = 0; i < message.length; ++i) {
                    waitDataAvailable(inputStream, timeoutMillis);
                    message[i] = (byte) inputStream.read();
            }

            // logDebug("Received boot loader msg: " + Tools.toHexString(message));

            // Read checksum
            waitDataAvailable(inputStream, timeoutMillis);
            byte recvChecksum = (byte) inputStream.read();

            // Concatenate length and message for checksum calculation
            byte[] fullMessage = new byte[message.length + 1];
            fullMessage[0] = (byte) length;
            System.arraycopy(message, 0, fullMessage, 1, message.length);

//TODO CHECK LATER ON CHECKSUM
            // Throw exception if checksums diffe
/*            byte checksum = Messages.calculateChecksum(fullMessage);
            if (checksum != recvChecksum) {
                    throw new InvalidChecksumException();
            }
*/
            // Check if the response type is unexpected
            if (message[0] != type) {
                    throw new UnexpectedResponseException(type, message[0]);
            }

            return message;
    }


    /**
     * Wait at most timeoutMillis for the input stream to become available
     *
     * @param istream	   The stream to monitor
     * @param timeoutMillis Milliseconds to wait until timeout, 0 for no timeout
     * @return The number of characters available
     * @throws IOException
     */
    private int waitDataAvailable(InputStream istream, int timeoutMillis) throws TimeoutException, IOException {
            TimeDiff timeDiff = new TimeDiff();
            int avail = 0;

            while (isConnected() && inputStream != null && (avail = inputStream.available()) == 0) {
                    if (timeoutMillis > 0 && timeDiff.ms() >= timeoutMillis) {
                            logWarn("Timeout waiting for data (waited: " + timeDiff.ms() + ", timeoutMs:" + timeoutMillis + ")");
                            throw new TimeoutException();
                    }

                    synchronized (dataAvailableMonitor) {
                            try {
                                    dataAvailableMonitor.wait(50);
                            } catch (InterruptedException e) {
                                    logError("Interrupted: " + e, e);
                            }
                    }
            }
            return avail;
    }
    

    

    @Override
    public ChipType getChipType() throws Exception {
        //throw new UnsupportedOperationException("Not supported yet.");
        return ChipType.TRISOS;
    }

    @Override
    public FlashType getFlashType() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean triggerProgram(IDeviceBinFile program, boolean rebootAfterFlashing) {
            //this.rebootAfterFlashing = rebootAfterFlashing;
            if (operationInProgress()) {
                    logError("Already another operation in progress (" + operation + ")");
                    return false;
            }

            operation = new FlashProgramOperation(this, program, true);
            operation.setLogIdentifier(logIdentifier);
            operation.start();
            return true;
    }

    @Override
    public void triggerSetMacAddress(MacAddress mac, boolean rebootAfterFlashing) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void triggerGetMacAddress(boolean rebootAfterFlashing) throws Exception {
//        this.rebootAfterFlashing = rebootAfterFlashing;
        //TODO Reboot
        logDebug("TrisosDevice: trigger Mac Adress");
        if (operationInProgress()) {
                logError("Already another operation in progress (" + operation + ")");
                return;
        }
        operation = new ReadMacAddressOperation(this);
        operation.setLogIdentifier(logIdentifier);
        operation.start();
    }

    @Override
    public boolean triggerReboot() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
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

    @Override
    public void shutdown() {
		// if (connected) {

		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException e) {
			logDebug("Failed to close in-stream :" + e, e);
		}
		try {
			if (outputStream != null) {
				outputStream.close();
			}
		} catch (IOException e) {
			logDebug("Failed to close out-stream :" + e, e);
		}
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
			connected = false;
			serialPort = null;
		}
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void serialEvent(SerialPortEvent spe) {
                //logDebug("Trisos Serial even:" + spe.toString());
		switch (spe.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE:

				synchronized (dataAvailableMonitor) {
					// logDebug("DM");
					dataAvailableMonitor.notifyAll();
				}

				if (operation == null) {
					receive(inputStream);
				} 

				break;
			default:
				logDebug("Serial event (other than data available): " + spe);
				break;
		}
    }
}
