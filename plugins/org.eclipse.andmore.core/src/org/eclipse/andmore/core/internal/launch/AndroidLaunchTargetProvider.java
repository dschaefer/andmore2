/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core.internal.launch;

import org.eclipse.andmore.core.internal.Activator;
import org.eclipse.andmore.core.internal.remote.AVDConnectionProviderService;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetProvider;
import org.eclipse.launchbar.core.target.TargetStatus;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;

public class AndroidLaunchTargetProvider implements ILaunchTargetProvider {

	public static final String TYPE_ID = "org.eclipse.andmore.core.androidTarget"; //$NON-NLS-1$
	public static final String DELIMITER = "|"; //$NON-NLS-1$

	private static IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);

	@Override
	public void init(ILaunchTargetManager targetManager) {
		// TODO What do about USB devices?

		// Remove missing ones
		// AVDs
		for (ILaunchTarget target : targetManager.getLaunchTargetsOfType(TYPE_ID)) {
			if (getRemote(target) == null) {
				targetManager.removeLaunchTarget(target);
			}
		}

		// Add new ones
		// AVDs
		IRemoteConnectionType connectionType = remoteManager.getConnectionType(AVDConnectionProviderService.TYPE_ID);
		for (IRemoteConnection connection : connectionType.getConnections()) {
			String id = getTargetId(connection);
			if (targetManager.getLaunchTarget(TYPE_ID, id) == null) {
				targetManager.addLaunchTarget(TYPE_ID, id);
			}
		}
	}

	public static String getTargetId(IRemoteConnection connection) {
		return connection.getConnectionType().getId() + DELIMITER + connection.getName();
	}

	public static IRemoteConnection getRemote(ILaunchTarget target) {
		String[] comps = target.getId().split("\\" + DELIMITER);
		if (comps.length < 2) {
			return null;
		}

		IRemoteConnectionType type = remoteManager.getConnectionType(comps[0]);
		if (type == null) {
			return null;
		}

		return type.getConnection(comps[1]);
	}

	@Override
	public TargetStatus getStatus(ILaunchTarget target) {
		// TODO - connected/disconnected status?
		return TargetStatus.OK_STATUS;
	}

}
