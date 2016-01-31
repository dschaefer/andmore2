/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core.internal.launch;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.andmore.core.IConsoleService;
import org.eclipse.andmore.core.internal.Activator;
import org.eclipse.andmore.core.sdk.IAndroidSDKService;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;
import org.eclipse.launchbar.core.target.launch.LaunchConfigurationTargetedDelegate;

public class AndroidRunLaunchConfigDelegate extends LaunchConfigurationTargetedDelegate {

	public static final String TYPE_ID = "org.eclipse.andmore.core.appLaunch"; //$NON-NLS-1$

	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
		return new IProject[] { configuration.getMappedResources()[0].getProject() };
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		try {
			ILaunchTarget target = ((ITargetedLaunch) launch).getLaunchTarget();

			IProject project = configuration.getMappedResources()[0].getProject();
			Path apkPath = Paths.get(project.getLocationURI())
					.resolve("build/outputs/apk/" + project.getName() + "-debug.apk"); //$NON-NLS-1$ //$NON-NLS-2$

			IConsoleService console = Activator.getService(IConsoleService.class);
			IAndroidSDKService sdk = Activator.getService(IAndroidSDKService.class);
			console.writeOutput("Installing app...\n");
			sdk.installAPK(apkPath);
			console.writeOutput("Starting app...\n");
			sdk.startApp("com.example.emptyapp", ".MainActivity");
			console.writeOutput("App started.\n");
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "launching", e));
		}
	}

}
