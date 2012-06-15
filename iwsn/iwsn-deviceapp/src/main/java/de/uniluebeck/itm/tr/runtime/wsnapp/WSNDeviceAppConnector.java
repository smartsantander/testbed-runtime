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

import com.google.common.collect.Multimap;
import de.uniluebeck.itm.tr.util.Listenable;
import de.uniluebeck.itm.tr.util.Service;
import de.uniluebeck.itm.tr.util.Tuple;

import javax.annotation.Nullable;
import java.util.List;

public interface WSNDeviceAppConnector extends Listenable<WSNDeviceAppConnector.NodeOutputListener>, Service {

	public static interface NodeOutputListener {

		void receivedPacket(byte[] bytes);

		void receiveNotification(String notification);

	}
	public static interface Callback {

		void success(@Nullable byte[] replyPayload);

		void failure(byte responseType, byte[] replyPayload);

		void timeout();

	}
	public static interface FlashProgramCallback extends Callback {

		void progress(float percentage);

	}
	void enableNode(Callback listener);

	void enablePhysicalLink(long nodeB, Callback listener);

	void destroyVirtualLink(long targetNode, Callback listener);

	void disableNode(Callback listener);

	void disablePhysicalLink(long nodeB, Callback listener);

	void flashProgram(WSNAppMessages.Program program, FlashProgramCallback listener);

	void isNodeAlive(Callback listener);

	void isNodeAliveSm(Callback callback);

	void resetNode(Callback listener);

	void sendMessage(byte[] binaryMessage, Callback listener);

	void setVirtualLink(long targetNode, Callback listener);

	void setDefaultChannelPipeline(Callback callback);

	void setChannelPipeline(List<Tuple<String, Multimap<String, String>>> channelHandlerConfigurations,
							Callback callback);
}
