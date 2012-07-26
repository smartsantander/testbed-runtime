/**********************************************************************************************************************
 * Copyright (c) 2010, Institute of Telematics, University of Luebeck                                                  *
 * All rights reserved.                                                                                               *
 *                                                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
 * following conditions are met:                                                                                      *
 *                                                                                                                    *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
 *   disclaimer.                                                                                                      *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
 *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
 * - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote *
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

package de.uniluebeck.itm.tr.runtime.portalapp;

import de.uniluebeck.itm.tr.util.Service;
import eu.wisebed.api.sm.ExperimentNotRunningException_Exception;
import eu.wisebed.api.sm.SecretReservationKey;
import eu.wisebed.api.sm.UnknownReservationIdException_Exception;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public interface SessionManagementService extends Service {

	/**
	 * See {@link eu.wisebed.api.sm.SessionManagement#getInstance(java.util.List, String)}
	 */
	String getInstance(List<SecretReservationKey> secretReservationKeys, String controller)
			throws ExperimentNotRunningException_Exception, UnknownReservationIdException_Exception;

	/**
	 * See {@link eu.wisebed.api.sm.SessionManagement#areNodesAlive(java.util.List, String)}
	 */
	String areNodesAlive(final List<String> nodes, final String controllerEndpointUrl);

	/**
	 * See {@link eu.wisebed.api.sm.SessionManagement#free(java.util.List)}
	 */
	void free(List<SecretReservationKey> secretReservationKeyList)
			throws ExperimentNotRunningException_Exception, UnknownReservationIdException_Exception;

	@Nullable
	WSNServiceHandle getWsnServiceHandle(@Nonnull String secretReservationKey);
}
