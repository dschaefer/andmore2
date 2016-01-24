/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.andmore.core.internal.Activator;
import org.eclipse.andmore.core.internal.ProjectTemplateManifest;
import org.eclipse.andmore.core.internal.ProjectTemplateManifest.FileTemplate;
import org.eclipse.andmore.core.internal.TemplateGenerator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;

import com.google.gson.Gson;

public class AppProjectGenerator {

	private static final String templateRoot = "/templates/app/empty"; //$NON-NLS-1$

	private final TemplateGenerator generator = new TemplateGenerator(new Path(templateRoot));

	private IProject project;
	private String packageName;
	private String activityName;
	private String layoutName;

	private List<IFile> filesToOpen = new ArrayList<>();
	private IFile fileToShow;

	public void setProject(IProject project) {
		this.project = project;
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

	public void generate(IProgressMonitor monitor) throws CoreException {
		addNatures(project, monitor);

		// The model for the templates
		Map<String, String> model = new HashMap<>();
		model.put("projectPath", project.getFullPath().toString()); //$NON-NLS-1$
		model.put("packageName", packageName); //$NON-NLS-1$
		model.put("packagePath", packageName.replace('.', '/')); //$NON-NLS-1$
		model.put("activityName", activityName); //$NON-NLS-1$
		model.put("layoutName", layoutName); //$NON-NLS-1$

		// load template manifest
		StringWriter writer = new StringWriter();
		generator.loadFile("manifest.json", model, writer); //$NON-NLS-1$
		ProjectTemplateManifest manifest = new Gson().fromJson(writer.toString(), ProjectTemplateManifest.class);

		// Generate the files
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (FileTemplate fileTemplate : manifest.getFiles()) {
			IPath destPath = new Path(fileTemplate.getDest());
			IProject project = root.getProject(destPath.segment(0));
			IFile file = project.getFile(destPath.removeFirstSegments(1));
			if (!fileTemplate.isSkipTemplate()) {
				generator.generateFile(fileTemplate.getSrc(), model, file, monitor);
			} else {
				try {
					URL url = FileLocator.find(Activator.getPlugin().getBundle(),
							new Path(templateRoot).append(fileTemplate.getSrc()), null);
					try (InputStream in = url.openStream()) {
						TemplateGenerator.createParent(file, monitor);
						if (file.exists()) {
							file.setContents(in, true, true, monitor);
						} else {
							file.create(in, true, monitor);
						}
					}
				} catch (IOException e) {
					throw new CoreException(
							new Status(IStatus.ERROR, Activator.getId(), "Reading file " + fileTemplate.getSrc(), e));
				}
			}

			if (fileTemplate.isOpen()) {
				if (fileTemplate.isShow()) {
					if (fileToShow != null) {
						filesToOpen.add(fileToShow);
						fileToShow = file;
					}
				} else {
					filesToOpen.add(fileToShow);
				}
			}
		}
	}

	public IFile[] getFilesToOpen() {
		int n = filesToOpen.size();
		if (fileToShow != null) {
			n++;
		}

		IFile[] files = new IFile[n];
		filesToOpen.toArray(files);

		if (fileToShow != null) {
			files[files.length - 1] = fileToShow;
		}

		return files;
	}

	private static void addNatures(IProject project, IProgressMonitor monitor) throws CoreException {
		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		if (!project.hasNature(JavaCore.NATURE_ID)) {
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = JavaCore.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, monitor);
		} else {
			if (monitor != null) {
				monitor.worked(1);
			}
		}
	}

}
