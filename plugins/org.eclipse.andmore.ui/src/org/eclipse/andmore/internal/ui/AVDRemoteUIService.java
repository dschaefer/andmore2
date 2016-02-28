/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.internal.ui;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionType.Service;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public class AVDRemoteUIService implements IRemoteUIConnectionService {

	private final IRemoteConnectionType type;

	public static class Factory implements IRemoteUIConnectionService.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnectionType connectionType, Class<T> service) {
			if (IRemoteUIConnectionService.class.equals(service)) {
				return (T) new AVDRemoteUIService(connectionType);
			}
			return null;
		}
	}

	private AVDRemoteUIService(IRemoteConnectionType type) {
		this.type = type;
	}

	@Override
	public IRemoteConnectionType getConnectionType() {
		return type;
	}

	@Override
	public IRemoteUIConnectionWizard getConnectionWizard(Shell shell) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void openConnectionWithProgress(Shell shell, IRunnableContext context, IRemoteConnection connection) {
		// TODO Auto-generated method stub
	}

	@Override
	public ILabelProvider getLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IRemoteConnection) {
					return ((IRemoteConnection) element).getName();
				} else {
					return super.getText(element);
				}
			}

			@Override
			public Image getImage(Object element) {
				return Activator.getImage(Activator.IMG_ANDROID_16);
			}
		};
	}

}
