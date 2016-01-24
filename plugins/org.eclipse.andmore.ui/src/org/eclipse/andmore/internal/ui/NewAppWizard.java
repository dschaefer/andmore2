/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.internal.ui;

import java.util.List;

import org.eclipse.andmore.core.AppProjectGenerator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class NewAppWizard extends BasicNewProjectResourceWizard {

	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		super.addPages();
	}

	@Override
	public boolean performFinish() {
		if (!super.performFinish())
			return false;

		new Job("Creating Android app project") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					AppProjectGenerator generator = new AppProjectGenerator();
					generator.setProject(getNewProject());
					generator.setPackageName("com.example.emptyapp");
					generator.setActivityName("MainActivity");
					generator.setLayoutName("activity_main");
					generator.generate(monitor);
					List<IFile> toOpen = generator.getFilesToOpen();
					if (toOpen.size() > 0) {
						getWorkbench().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								try {
									IWorkbenchPage activePage = getWorkbench().getActiveWorkbenchWindow()
											.getActivePage();
									for (IFile file : toOpen) {
										IDE.openEditor(activePage, file);
									}
								} catch (PartInitException e) {
									Activator.getDefault().getLog().log(e.getStatus());
								}
							}
						});
					}
					return Status.OK_STATUS;
				} catch (CoreException e) {
					return e.getStatus();
				}
			}
		}.schedule();

		return true;
	}

}
