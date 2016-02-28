/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core.internal.launch;

import org.eclipse.andmore.core.internal.remote.AVDConnectionProviderService;
import org.eclipse.launchbar.remote.core.RemoteLaunchTargetProvider;

public class AndroidLaunchTargetProvider extends RemoteLaunchTargetProvider {

	@Override
	protected String getTypeId() {
		return AVDConnectionProviderService.TYPE_ID;
	}

}
