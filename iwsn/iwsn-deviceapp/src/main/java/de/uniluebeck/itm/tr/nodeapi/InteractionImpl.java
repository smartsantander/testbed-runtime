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

package de.uniluebeck.itm.tr.nodeapi;

import com.google.common.util.concurrent.SettableFuture;
import de.uniluebeck.itm.tr.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

import static de.uniluebeck.itm.tr.util.StringUtils.toPrintableString;


class InteractionImpl implements Interaction {

	private static final Logger log = LoggerFactory.getLogger(Interaction.class);

	private final String nodeUrn;

	private final NodeApi nodeApi;

	public InteractionImpl(final String nodeUrn, NodeApi nodeApi) {
		this.nodeUrn = nodeUrn;
		this.nodeApi = nodeApi;
	}

	@Override
	public Future<NodeApiCallResult> sendVirtualLinkMessage(byte RSSI, byte LQI, long destination, long source, byte[] payload) {

		if (log.isTraceEnabled()) {

			log.trace(
					"{} => InteractionImpl.sendVirtualLinkMessage(rssi={}, lqi={}, destination={}, source={}, payload={}, callback)",
					new Object[]{
							nodeUrn,
							StringUtils.toHexString(RSSI),
							StringUtils.toHexString(LQI),
							destination,
							source,
							toPrintableString(payload, 200),
					}
			);
		}

		int requestId = nodeApi.nextRequestId();
		ByteBuffer buffer = Packets.Interaction.newVirtualLinkMessagePacket(
				requestId, RSSI, LQI, destination, source, payload
		);
		SettableFuture<NodeApiCallResult> future = SettableFuture.create();
		nodeApi.sendToNode(requestId, future, buffer);
		return future;
	}

	@Override
	public Future<NodeApiCallResult> sendVirtualLinkMessage(long destination, long source, byte[] payload) {

		int requestId = nodeApi.nextRequestId();
		ByteBuffer buffer = Packets.Interaction.newVirtualLinkMessagePacket(
				requestId, destination, source, payload
		);
		SettableFuture<NodeApiCallResult> future = SettableFuture.create();
		nodeApi.sendToNode(requestId, future, buffer);
		return future;
	}

	@Override
	public Future<NodeApiCallResult> sendByteMessage(byte binaryType, byte[] payload) {

		int requestId = nodeApi.nextRequestId();
		ByteBuffer buffer = Packets.Interaction.newByteMessagePacket(
				requestId, binaryType, payload
		);
		SettableFuture<NodeApiCallResult> future = SettableFuture.create();
		nodeApi.sendToNode(requestId, future, buffer);
		return future;
	}

	@Override
	public Future<NodeApiCallResult> flashProgram(byte[] payload) {

		int requestId = nodeApi.nextRequestId();
		ByteBuffer buffer = Packets.Interaction.newFlashProgramPacket(
				requestId, payload
		);
		SettableFuture<NodeApiCallResult> future = SettableFuture.create();
		nodeApi.sendToNode(requestId, future, buffer);
		return future;
	}

}
