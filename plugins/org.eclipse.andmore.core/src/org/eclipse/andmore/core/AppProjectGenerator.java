/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.andmore.core.internal.Activator;
import org.eclipse.andmore.core.internal.AndroidBuilder;
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
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Dependencies;
import com.android.builder.model.JavaArtifact;
import com.android.builder.model.JavaLibrary;
import com.android.builder.model.Variant;
import com.google.gson.Gson;

public class AppProjectGenerator {

	private static final String templateRoot = "/templates/app/empty"; //$NON-NLS-1$

	private final TemplateGenerator generator = new TemplateGenerator(new Path(templateRoot));

	private IProject project;
	private String packageName;
	private String activityName;
	private String layoutName;

	private final Map<String, Object> model = new HashMap<>();
	private ProjectTemplateManifest manifest;
	private final List<IFile> filesToOpen = new ArrayList<>();

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
		// The model for the templates
		model.put("projectPath", project.getFullPath().toString()); //$NON-NLS-1$
		model.put("projectName", project.getName()); //$NON-NLS-1$
		model.put("packageName", packageName); //$NON-NLS-1$
		model.put("packagePath", packageName.replace('.', '/')); //$NON-NLS-1$
		model.put("activityName", activityName); //$NON-NLS-1$
		model.put("layoutName", layoutName); //$NON-NLS-1$

		// load template manifest
		StringWriter writer = new StringWriter();
		generator.loadFile("manifest.json", model, writer); //$NON-NLS-1$
		manifest = new Gson().fromJson(writer.toString(), ProjectTemplateManifest.class);

		generateSources(monitor);

		// Run Gradle build
		AndroidBuilder.gradleBuild(project, "assembleDebug", monitor); //$NON-NLS-1$

		// Set up Java project
		addNatures(monitor);
		IJavaProject javaProject = JavaCore.create(project);

		// Source folders
		List<IClasspathEntry> entries = new ArrayList<>();
		if (manifest.getSrcEntries() != null) {
			for (String srcEntry : manifest.getSrcEntries()) {
				IPath srcPath = project.getFolder(srcEntry).getFullPath();
				entries.add(JavaCore.newSourceEntry(srcPath));
			}
		}

		// Generated source - TODO this is in the model too
		IPath genPath = project.getFolder("/build/generated/source/r/debug").getFullPath(); //$NON-NLS-1$
		entries.add(JavaCore.newSourceEntry(genPath));

		// JRE
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		IPath vmPath = JavaRuntime.newJREContainerPath(vm);
		entries.add(JavaCore.newContainerEntry(vmPath));

		// Android jars out of gradle - TODO source if we have it
		ProjectConnection connection = GradleConnector.newConnector()
				.forProjectDirectory(new File(project.getLocationURI())).connect();
		AndroidProject androidProject = connection.getModel(AndroidProject.class);

		for (String jar : androidProject.getBootClasspath()) {
			entries.add(JavaCore.newLibraryEntry(new Path(jar), null, null));
		}

		Variant debugVariant = null;
		for (Variant variant : androidProject.getVariants()) {
			if ("debug".equals(variant.getName())) { //$NON-NLS-1$
				debugVariant = variant;
				break;
			}
		}

		if (debugVariant != null) {
			addDependencies(entries, debugVariant.getMainArtifact().getDependencies());
			for (AndroidArtifact artifact : debugVariant.getExtraAndroidArtifacts()) {
				addDependencies(entries, artifact.getDependencies());
			}
			for (JavaArtifact artifact : debugVariant.getExtraJavaArtifacts()) {
				addDependencies(entries, artifact.getDependencies());
			}
		}

		javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), monitor);
		connection.close();
	}

	private void addDependencies(List<IClasspathEntry> entries, Dependencies deps) {
		for (JavaLibrary lib : deps.getJavaLibraries()) {
			addJavaLibrary(entries, lib);
		}
		for (AndroidLibrary lib : deps.getLibraries()) {
			addAndroidLibrary(entries, lib);
		}
	}

	private void addJavaLibrary(List<IClasspathEntry> entries, JavaLibrary lib) {
		entries.add(JavaCore.newLibraryEntry(new Path(lib.getJarFile().getAbsolutePath()), null, null));
		for (JavaLibrary dep : lib.getDependencies()) {
			addJavaLibrary(entries, dep);
		}
	}

	private void addAndroidLibrary(List<IClasspathEntry> entries, AndroidLibrary lib) {
		entries.add(JavaCore.newLibraryEntry(new Path(lib.getJarFile().getAbsolutePath()), null, null));
		for (AndroidLibrary dep : lib.getLibraryDependencies()) {
			addAndroidLibrary(entries, dep);
		}
		for (File local : lib.getLocalJars()) {
			entries.add(JavaCore.newLibraryEntry(new Path(local.getAbsolutePath()), null, null));
		}
	}

	public List<IFile> getFilesToOpen() {
		return filesToOpen;
	}

	private void addNatures(IProgressMonitor monitor) throws CoreException {
		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		if (!project.hasNature(JavaCore.NATURE_ID)) {
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 2];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = JavaCore.NATURE_ID;
			newNatures[prevNatures.length + 1] = AndroidNature.ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, monitor);
		} else {
			if (monitor != null) {
				monitor.worked(1);
			}
		}
	}

	private void generateSources(IProgressMonitor monitor) throws CoreException {
		// Generate the files
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile fileToShow = null;
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
					}
					fileToShow = file;
				} else {
					filesToOpen.add(file);
				}
			}
		}

		if (fileToShow != null) {
			filesToOpen.add(fileToShow);
		}
	}
}
