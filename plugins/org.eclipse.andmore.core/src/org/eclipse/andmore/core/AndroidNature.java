/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core;

import org.eclipse.andmore.core.internal.Activator;
import org.eclipse.andmore.core.internal.AndroidBuilder;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public class AndroidNature implements IProjectNature {

	public static final String ID = Activator.getId() + ".androidNature"; //$NON-NLS-1$

	private IProject project;

	@Override
	public void configure() throws CoreException {
		// Setup the builder
		IProjectDescription desc = project.getDescription();
		ICommand[] builders = desc.getBuildSpec();
		ICommand[] newBuilders = new ICommand[builders.length + 1];
		System.arraycopy(builders, 0, newBuilders, 0, builders.length);

		ICommand androidBuilder = desc.newCommand();
		androidBuilder.setBuilderName(AndroidBuilder.ID);
		// We don't autobuild.
		androidBuilder.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, false);
		newBuilders[builders.length] = androidBuilder;
		desc.setBuildSpec(newBuilders);

		project.setDescription(desc, 0, new NullProgressMonitor());
	}

	@Override
	public void deconfigure() throws CoreException {
		// Nothing to clean up
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}
