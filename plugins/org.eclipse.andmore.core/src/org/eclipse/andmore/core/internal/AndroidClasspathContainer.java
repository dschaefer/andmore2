/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Dependencies;
import com.android.builder.model.JavaArtifact;
import com.android.builder.model.JavaLibrary;
import com.android.builder.model.Variant;

public class AndroidClasspathContainer implements IClasspathContainer {

	public static IPath path = new Path(Activator.getId() + ".androidGradleDeps"); //$NON-NLS-1$

	private final IClasspathEntry[] entries;

	public AndroidClasspathContainer(IJavaProject javaProject) {
		IProject project = javaProject.getProject();
		List<IClasspathEntry> entryList = new ArrayList<>();

		// Android jars out of gradle - TODO source if we have it
		ProjectConnection connection = GradleConnector.newConnector()
				.forProjectDirectory(new File(project.getLocationURI())).connect();
		AndroidProject androidProject = connection.getModel(AndroidProject.class);

		for (String jar : androidProject.getBootClasspath()) {
			entryList.add(JavaCore.newLibraryEntry(new Path(jar), null, null));
		}

		Variant debugVariant = null;
		for (Variant variant : androidProject.getVariants()) {
			if ("debug".equals(variant.getName())) { //$NON-NLS-1$
				debugVariant = variant;
				break;
			}
		}

		if (debugVariant != null) {
			addDependencies(entryList, debugVariant.getMainArtifact().getDependencies());
			for (AndroidArtifact artifact : debugVariant.getExtraAndroidArtifacts()) {
				addDependencies(entryList, artifact.getDependencies());
			}
			for (JavaArtifact artifact : debugVariant.getExtraJavaArtifacts()) {
				addDependencies(entryList, artifact.getDependencies());
			}
		}

		this.entries = entryList.toArray(new IClasspathEntry[entryList.size()]);
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

	@Override
	public IClasspathEntry[] getClasspathEntries() {
		return entries;
	}

	@Override
	public String getDescription() {
		return "Android Dependencies";
	}

	@Override
	public int getKind() {
		return K_APPLICATION;
	}

	@Override
	public IPath getPath() {
		return path;
	}

}
