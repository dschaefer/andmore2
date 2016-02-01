/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core.internal;

import org.eclipse.andmore.core.AndroidNature;
import org.eclipse.andmore.core.internal.model.AndroidProject;
import org.eclipse.andmore.core.model.IAndroidProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;

public class AndroidProjectAdapterFactory implements IAdapterFactory {

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof IProject && adapterType.equals(IAndroidProject.class)) {
			IProject project = (IProject) adaptableObject;
			try {
				if (project.hasNature(AndroidNature.ID)) {
					return (T) new AndroidProject(project);
				}
			} catch (CoreException e) {
				Activator.log(e.getStatus());
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { IAndroidProject.class };
	}

}
