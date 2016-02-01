/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core.manifest;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Activity {

	private String name;
	private IntentFilter intentFilter;

	@XmlAttribute(namespace = "http://schemas.android.com/apk/res/android")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name = "intent-filter")
	public IntentFilter getIntentFilter() {
		return intentFilter;
	}

	public void setIntentFilter(IntentFilter intentFilter) {
		this.intentFilter = intentFilter;
	}

	public boolean supportsAction(String actionName) {
		if (actionName != null && intentFilter != null) {
			List<Action> actions = intentFilter.getActions();
			if (actions != null) {
				for (Action action : actions) {
					if (actionName.equals(action.getName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
