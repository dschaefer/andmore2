/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core.sdk;

public class AndroidVirtualDevice {

	private String name;
	private String device;
	private String path;
	private String target;
	private String abi;
	private String skin;

	public String getName() {
		return name;
	}

	public AndroidVirtualDevice setName(String name) {
		this.name = name;
		return this;
	}

	public String getDevice() {
		return device;
	}

	public AndroidVirtualDevice setDevice(String device) {
		this.device = device;
		return this;
	}

	public String getPath() {
		return path;
	}

	public AndroidVirtualDevice setPath(String path) {
		this.path = path;
		return this;
	}

	public String getTarget() {
		return target;
	}

	public AndroidVirtualDevice setTarget(String target) {
		this.target = target;
		return this;
	}

	public String getAbi() {
		return abi;
	}

	public AndroidVirtualDevice setAbi(String abi) {
		this.abi = abi;
		return this;
	}

	public String getSkin() {
		return skin;
	}

	public AndroidVirtualDevice setSkin(String skin) {
		this.skin = skin;
		return this;
	}

}
