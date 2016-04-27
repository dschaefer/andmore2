/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.andmore.core.internal.Activator;
import org.eclipse.andmore.core.internal.AndroidBuilder;
import org.eclipse.andmore.core.internal.AndroidClasspathContainer;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.tools.templates.freemarker.FMProjectGenerator;
import org.eclipse.tools.templates.freemarker.SourceRoot;
import org.osgi.framework.Bundle;

public class AppProjectGenerator extends FMProjectGenerator {

	private String packageName;
	private String activityName;
	private String layoutName;

	public AppProjectGenerator(String manifestPath) {
		super(manifestPath);
	}
	
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public void setLayoutName(String layoutName) {
		this.layoutName = layoutName;
	}

	@Override
	protected void initProjectDescription(IProjectDescription description) {
		description.setNatureIds(new String[] { JavaCore.NATURE_ID, AndroidNature.ID });

		ICommand javaBuilder = description.newCommand();
		javaBuilder.setBuilderName(JavaCore.BUILDER_ID);

		ICommand androidBuilder = description.newCommand();
		androidBuilder.setBuilderName(AndroidBuilder.ID);
		androidBuilder.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, false);

		description.setBuildSpec(new ICommand[] { javaBuilder, androidBuilder });
	}

	@Override
	public Bundle getSourceBundle() {
		return Activator.getPlugin().getBundle();
	}
	
	@Override
	public void generate(Map<String, Object> model, IProgressMonitor monitor) throws CoreException {
		model.put("packageName", packageName); //$NON-NLS-1$
		model.put("packagePath", packageName.replace('.', '/')); //$NON-NLS-1$
		model.put("activityName", activityName); //$NON-NLS-1$
		model.put("layoutName", layoutName); //$NON-NLS-1$

		super.generate(model, monitor);

		IProject project = getProject();

		// Do initial code generation from gradle
		monitor.setTaskName("Generating initial sources...");
		AndroidBuilder.gradleBuild(project, "generateDebugSources", monitor); //$NON-NLS-1$

		// Mark the build and .gradle folders derived
		project.getFolder("build").setDerived(true, monitor); //$NON-NLS-1$
		project.getFolder(".gradle").setDerived(true, monitor); //$NON-NLS-1$

		// Set up Java project
		IJavaProject javaProject = JavaCore.create(project);

		// Source folders
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		List<SourceRoot> srcRoots = getManifest().getSrcRoots();
		List<IClasspathEntry> entries = new ArrayList<>();
		if (srcRoots != null) {
			for (SourceRoot srcRoot : srcRoots) {
				IPath srcPath = project.getFolder(srcRoot.getDir()).getFullPath();
				entries.add(JavaCore.newSourceEntry(srcPath));
			}
		}

		// Generated source - TODO this is in the model too
		IPath genPath = project.getFolder("/build/generated/source/r/debug").getFullPath(); //$NON-NLS-1$
		entries.add(JavaCore.newSourceEntry(genPath));

		// Android Gradle container
		entries.add(JavaCore.newContainerEntry(AndroidClasspathContainer.path));

		// JRE
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		IPath vmPath = JavaRuntime.newJREContainerPath(vm);
		entries.add(JavaCore.newContainerEntry(vmPath));

		javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), monitor);
	}

}
