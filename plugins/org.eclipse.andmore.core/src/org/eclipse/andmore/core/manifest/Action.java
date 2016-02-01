/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core.manifest;

import javax.xml.bind.annotation.XmlAttribute;

public class Action {

	private String name;

	@XmlAttribute(namespace = "http://schemas.android.com/apk/res/android")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
