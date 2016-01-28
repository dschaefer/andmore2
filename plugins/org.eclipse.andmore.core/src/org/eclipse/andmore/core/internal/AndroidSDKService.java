/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.andmore.core.IConsoleService;
import org.eclipse.andmore.core.sdk.AndroidVirtualDevice;
import org.eclipse.andmore.core.sdk.IAndroidSDKService;

public class AndroidSDKService implements IAndroidSDKService {

	private String sdkLocation = System.getProperty("user.home") + "/Library/Android/sdk";

	private static class ErrorReaper extends Thread {
		private final String task;
		private final BufferedReader err;
		private IConsoleService console;

		public ErrorReaper(String task, InputStream err) {
			this.task = task;
			this.err = new BufferedReader(new InputStreamReader(err));
		}

		@Override
		public void run() {
			try {
				String line = err.readLine();
				while (line != null) {
					msg(line + '\n');
				}
			} catch (IOException e) {
				Activator.logError("reporting tools errors", e);
			}
		}

		private void msg(String line) throws IOException {
			if (console == null) {
				console = Activator.getService(IConsoleService.class);
				console.activate();
				console.writeError("Error " + task + "\n");
			}
			console.writeError(line);
		}
	}

	@Override
	public Collection<AndroidVirtualDevice> getAVDs() throws IOException {
		Process proc = new ProcessBuilder(getAndroidCommand(), "list", "avd").start();

		ErrorReaper reaper = new ErrorReaper("listing AVDs", proc.getErrorStream());
		reaper.start();

		Collection<AndroidVirtualDevice> avds = new ArrayList<>();
		Pattern field = Pattern.compile("^\\s*([^\\s]+):\\s*(.*)");
		Pattern separator = Pattern.compile("^--+");
		AndroidVirtualDevice avd = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String line = in.readLine();
		while (line != null) {
			Matcher matcher = field.matcher(line);
			if (matcher.matches()) {
				String key = matcher.group(1);
				String value = matcher.group(2);
				if (avd == null) {
					avd = new AndroidVirtualDevice();
				}
				switch (key) {
				case "Name":
					avd.setName(value);
					break;
				case "Device":
					avd.setDevice(value);
					break;
				case "Path":
					avd.setPath(value);
					break;
				case "Target":
					avd.setTarget(value);
					break;
				case "Tag/ABI":
					avd.setAbi(value);
					break;
				case "Skin":
					avd.setSkin(value);
					break;
				}
				continue;
			}
			matcher = separator.matcher(line);
			if (matcher.matches()) {
				avds.add(avd);
				avd = null;
				continue;
			}
		}

		if (avd != null) {
			avds.add(avd);
		}

		try {
			reaper.join();
		} catch (InterruptedException e) {
			Activator.log(e);
		}

		return avds;
	}

	private String getAndroidCommand() {
		return sdkLocation + "/tools/android";
	}

}
