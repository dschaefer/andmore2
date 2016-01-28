/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core.internal;

import org.eclipse.andmore.core.sdk.IAndroidSDKService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator extends Plugin {

	private static Plugin plugin;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		plugin = this;
		bundleContext.registerService(IAndroidSDKService.class, new AndroidSDKService(), null);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		plugin = null;
	}

	public static Plugin getPlugin() {
		return plugin;
	}

	public static String getId() {
		return plugin.getBundle().getSymbolicName();
	}

	public static <T> T getService(Class<T> service) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

	public static void log(Throwable e) {
		if (e instanceof CoreException) {
			log(((CoreException) e).getStatus());
		} else {
			logError("exception", e);
		}

	}

	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}

	public static void logError(String msg, Throwable e) {
		plugin.getLog().log(new Status(IStatus.ERROR, getId(), msg, e));
	}

}
