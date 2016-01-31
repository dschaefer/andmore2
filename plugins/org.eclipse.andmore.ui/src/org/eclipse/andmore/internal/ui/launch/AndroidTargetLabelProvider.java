/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.internal.ui.launch;

import org.eclipse.andmore.core.internal.launch.AndroidLaunchTargetProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.swt.graphics.Image;

public class AndroidTargetLabelProvider extends LabelProvider {

	@Override
	public Image getImage(Object element) {
		// TODO different image for AVD versus real device
		return super.getImage(element);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ILaunchTarget) {
			String id = ((ILaunchTarget) element).getId();
			String[] comps = id.split("\\" + AndroidLaunchTargetProvider.DELIMITER); //$NON-NLS-1$
			if (comps.length > 1) {
				return comps[1];
			} else {
				return id;
			}
		}
		return null;
	}

}
