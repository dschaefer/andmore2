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
import java.util.List;

import org.eclipse.andmore.core.IConsoleService;
import org.eclipse.andmore.core.internal.Activator;
import org.eclipse.andmore.core.manifest.Activity;
import org.eclipse.andmore.core.manifest.Application;
import org.eclipse.andmore.core.manifest.Manifest;
import org.eclipse.andmore.core.model.IAndroidProject;
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
			IProject project = configuration.getMappedResources()[0].getProject();
			ILaunchTarget target = ((ITargetedLaunch) launch).getLaunchTarget();
			IConsoleService console = Activator.getService(IConsoleService.class);

			Activity mainActivity = null;
			IAndroidProject androidProject = project.getAdapter(IAndroidProject.class);
			Manifest manifest = androidProject.getAndroidManifest();
			List<Application> apps = manifest.getApplications();
			if (apps != null) {
				lookForMain: for (Application app : apps) {
					List<Activity> activities = app.getActivities();
					if (activities != null) {
						for (Activity activity : activities) {
							if (activity.supportsAction("android.intent.action.MAIN")) { //$NON-NLS-1$
								mainActivity = activity;
								break lookForMain;
							}
						}
					}
				}
			}

			if (mainActivity == null) {
				String msg = String.format("Failed to launch %s. No main activity found.", project.getName());
				console.writeError(msg);
				throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), msg));
			}

			IAndroidSDKService sdk = Activator.getService(IAndroidSDKService.class);

			console.writeOutput(String.format("\nInstalling app %s...\n", project.getName()));
			Path apkPath = Paths.get(project.getLocationURI())
					.resolve("build/outputs/apk/" + project.getName() + "-debug.apk"); //$NON-NLS-1$ //$NON-NLS-2$
			sdk.installAPK(apkPath);

			console.writeOutput(String.format("Starting app %s...\n", project.getName()));
			sdk.startApp(manifest.getPackage(), mainActivity.getName());

			console.writeOutput("App started.\n");
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "launching", e)); //$NON-NLS-1$
		}
	}

}
