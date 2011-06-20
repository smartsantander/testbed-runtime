/**********************************************************************************************************************
 * Copyright (c) 2010, Institute of Telematics, University of Luebeck                                                 *
 * All rights reserved.                                                                                               *
 *                                                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
 * following conditions are met:                                                                                      *
 *                                                                                                                    *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
 *   disclaimer.                                                                                                      *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
 *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
 * - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote*
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

package de.uniluebeck.itm.tr.runtime.wsnapp;

import de.uniluebeck.itm.gtr.common.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;


public class WSNDeviceAppConnectorFactory {

	private static final Logger log = LoggerFactory.getLogger(WSNDeviceAppConnectorFactory.class);

	public static WSNDeviceAppConnector create(final String nodeUrn, final String nodeType, final String nodeUSBChipID,
											   final String nodeSerialInterface, final Integer timeoutNodeAPI,
											   final Integer maximumMessageRate,
											   final SchedulerService schedulerService,
											   final Integer timeoutReset,
											   final Integer timeoutFlash) {

		boolean newDrivers = !System.getProperties().containsKey("testbed.olddrivers");

		if (newDrivers) {

			log.debug("{} => Using new drivers", nodeUrn);
			return new WSNDeviceAppConnectorImpl(
					nodeUrn,
					nodeType,
					nodeUSBChipID,
					nodeSerialInterface,
					timeoutNodeAPI,
					maximumMessageRate,
					timeoutReset,
					timeoutFlash,
					schedulerService
			);

		} else {

			log.debug("{} => Using old drivers", nodeUrn);
			return new WSNDeviceAppConnectorOld(
					nodeUrn,
					nodeType,
					nodeUSBChipID,
					nodeSerialInterface,
					timeoutNodeAPI,
					maximumMessageRate,
					schedulerService,
					timeoutReset,
					timeoutFlash
			);
		}

	}
}
