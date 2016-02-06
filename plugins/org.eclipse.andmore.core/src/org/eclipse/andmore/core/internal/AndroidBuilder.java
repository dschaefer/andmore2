/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core.internal;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.andmore.core.IConsoleService;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

public class AndroidBuilder extends IncrementalProjectBuilder {

	public static final String ID = Activator.getId() + ".androidBuilder"; //$NON-NLS-1$

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		gradleBuild(getProject(), "assembleDebug", monitor); //$NON-NLS-1$

		// Update the dependencies
		// TODO should really so this when build files change
		IJavaProject javaProject = JavaCore.create(getProject());
		JavaCore.setClasspathContainer(AndroidClasspathContainer.path, new IJavaProject[] { javaProject },
				new IClasspathContainer[] { new AndroidClasspathContainer(javaProject) }, monitor);

		return new IProject[] { getProject() };
	}

	public static void gradleBuild(IProject project, String task, IProgressMonitor monitor) throws CoreException {
		String taskName = String.format("Executing %s on project %s", task, project.getName());
		try {
			IConsoleService console = Activator.getService(IConsoleService.class);
			console.activate();
			console.writeOutput(String.format("\n%s\n\n", taskName)); //$NON-NLS-1$

			File projectDir = new File(project.getLocationURI());
			ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(projectDir).connect();
			connection.newBuild().forTasks(task).setStandardOutput(console.getOutputStream())
					.setStandardError(console.getErrorStream()).run();
			connection.close();
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (BuildException | IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), taskName, e));
		}
	}

}
