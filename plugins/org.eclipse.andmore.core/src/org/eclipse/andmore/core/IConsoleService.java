/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core;

import java.io.IOException;
import java.io.OutputStream;

public interface IConsoleService {

	OutputStream getOutputStream();

	OutputStream getErrorStream();

	void writeOutput(String msg) throws IOException;

	void writeError(String msg) throws IOException;

	void clear();

	void activate();

}
