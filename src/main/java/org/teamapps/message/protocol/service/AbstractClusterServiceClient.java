/*-
 * ========================LICENSE_START=================================
 * TeamApps Message Protocol
 * ---
 * Copyright (C) 2022 - 2023 TeamApps.org
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.teamapps.message.protocol.service;


import org.teamapps.message.protocol.message.Message;
import org.teamapps.message.protocol.model.PojoObjectDecoder;

public abstract class AbstractClusterServiceClient {

	private final ClusterServiceRegistry clusterServiceRegistry;
	private final String serviceName;

	public AbstractClusterServiceClient(ClusterServiceRegistry clusterServiceRegistry, String serviceName) {
		this.clusterServiceRegistry = clusterServiceRegistry;
		this.serviceName = serviceName;
	}

	protected <REQUEST extends Message, RESPONSE extends Message> RESPONSE executeClusterServiceMethod(String method, REQUEST request, PojoObjectDecoder<RESPONSE> responseDecoder) {
		return clusterServiceRegistry.executeServiceMethod(serviceName, method, request, responseDecoder);
	}

	public boolean isAvailable() {
		return clusterServiceRegistry.isServiceAvailable(serviceName);
	}

}
