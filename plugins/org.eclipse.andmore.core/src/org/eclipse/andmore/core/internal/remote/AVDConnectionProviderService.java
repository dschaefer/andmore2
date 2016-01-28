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

	private final IRemoteConnectionType type;

	public AVDConnectionProviderService(IRemoteConnectionType type) {
		this.type = type;
	}

	public class Factory implements IRemoteConnectionProviderService.Factory {
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
				if (!existing.containsKey(device.getName())) {
					try {
						existing.put(device.getName(), type.newConnection(device.getName()).save());
					} catch (RemoteConnectionException e) {
						Activator.logError("creating connection", e);
					}
				}
			}
		} catch (IOException e) {
			Activator.logError("fetching AVDs", e);
		}
	}

}
