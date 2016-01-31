/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core.internal.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.andmore.core.internal.Activator;
import org.eclipse.andmore.core.sdk.AndroidVirtualDevice;
import org.eclipse.andmore.core.sdk.IAndroidSDKService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionProviderService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionType.Service;
import org.eclipse.remote.core.exception.RemoteConnectionException;

public class AVDConnectionProviderService implements IRemoteConnectionProviderService {

	public static final String TYPE_ID = "org.eclipse.andmore.core.connectionType.avd"; //$NON-NLS-1$

	private final IRemoteConnectionType type;

	public AVDConnectionProviderService(IRemoteConnectionType type) {
		this.type = type;
	}

	public static class Factory implements IRemoteConnectionProviderService.Factory {
		@Override
		@SuppressWarnings("unchecked")
		public <T extends Service> T getService(IRemoteConnectionType connectionType, Class<T> service) {
			return (T) new AVDConnectionProviderService(connectionType);
		}
	}

	@Override
	public IRemoteConnectionType getConnectionType() {
		return type;
	}

	@Override
	public void init() {
		Map<String, IRemoteConnection> existing = new HashMap<>();
		for (IRemoteConnection connection : type.getConnections()) {
			existing.put(connection.getName(), connection);
		}

		IAndroidSDKService sdk = Activator.getService(IAndroidSDKService.class);
		try {
			for (AndroidVirtualDevice device : sdk.getAVDs()) {
				String name = device.getName();
				if (!existing.containsKey(device.getName())) {
					try {
						type.newConnection(name).save();
					} catch (RemoteConnectionException e) {
						Activator.logError("creating connection", e); //$NON-NLS-1$
					}
				} else {
					// Mark it as here
					existing.remove(name);
				}
			}
		} catch (IOException e) {
			Activator.logError("fetching AVDs", e); //$NON-NLS-1$
		}

		// Remove ones that don't exist any more
		for (IRemoteConnection connection : existing.values()) {
			try {
				type.removeConnection(connection);
			} catch (RemoteConnectionException e) {
				Activator.logError("removing connection", e); //$NON-NLS-1$
			}
		}
	}

}
