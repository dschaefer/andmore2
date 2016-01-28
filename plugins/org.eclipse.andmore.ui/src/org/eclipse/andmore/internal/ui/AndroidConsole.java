/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.internal.ui;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.andmore.core.IConsoleService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class AndroidConsole implements IConsoleService {

	private MessageConsole console;
	private MessageConsoleStream out;
	private MessageConsoleStream err;

	public AndroidConsole() {
		console = new MessageConsole("Android", null);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
		out = console.newMessageStream();
		err = console.newMessageStream();

		// set the colors
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				out.setColor(display.getSystemColor(SWT.COLOR_BLACK));
				err.setColor(display.getSystemColor(SWT.COLOR_RED));
			}
		});
	}

	@Override
	public OutputStream getOutputStream() {
		return out;
	}

	@Override
	public OutputStream getErrorStream() {
		return err;
	}

	@Override
	public void writeOutput(String msg) throws IOException {
		out.write(msg);
	}

	@Override
	public void writeError(String msg) throws IOException {
		err.write(msg);
	}

	@Override
	public void clear() {
		console.clearConsole();
	}

	@Override
	public void activate() {
		console.activate();
	}

}
