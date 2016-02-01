/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core.internal.model;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.andmore.core.manifest.Manifest;
import org.eclipse.andmore.core.model.IAndroidProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

public class AndroidProject implements IAndroidProject {

	private final IProject project;

	public AndroidProject(IProject project) {
		this.project = project;
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public Manifest getAndroidManifest() throws IOException {
		// TODO cache this thing
		try {
			JAXBContext context = JAXBContext.newInstance(Manifest.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			IFile file = project.getFile("/src/main/AndroidManifest.xml"); //$NON-NLS-1$
			return (Manifest) unmarshaller.unmarshal(new File(file.getLocationURI()));
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

}
