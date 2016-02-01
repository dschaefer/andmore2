/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core.model;

import java.io.IOException;

import org.eclipse.andmore.core.manifest.Manifest;
import org.eclipse.core.resources.IProject;

public interface IAndroidProject {

	IProject getProject();

	Manifest getAndroidManifest() throws IOException;

}
