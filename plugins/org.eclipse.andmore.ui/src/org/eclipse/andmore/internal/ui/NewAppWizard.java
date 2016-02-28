/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;

import org.eclipse.andmore.core.AppProjectGenerator;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class NewAppWizard extends BasicNewResourceWizard implements INewWizard {

	private WizardNewProjectCreationPage mainPage;
	private WizardNewProjectReferencePage referencePage;

	public NewAppWizard() {
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		super.addPages();

		mainPage = new WizardNewProjectCreationPage("basicNewProjectPage") { //$NON-NLS-1$
			@Override
			public void createControl(Composite parent) {
				super.createControl(parent);
				createWorkingSetGroup((Composite) getControl(), getSelection(),
						new String[] { "org.eclipse.ui.resourceWorkingSetPage" }); //$NON-NLS-1$
				Dialog.applyDialogFont(getControl());
			}
		};
		mainPage.setTitle("Android Application");
		mainPage.setDescription("Create Android Application Project");
		this.addPage(mainPage);

		addPage(new NewAppWizardPage());

		// only add page if there are already projects in the workspace
		if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
			referencePage = new WizardNewProjectReferencePage("basicReferenceProjectPage");//$NON-NLS-1$
			referencePage.setTitle("Project References");
			referencePage.setDescription("Project References");
			this.addPage(referencePage);
		}

	}

	@Override
	public boolean performFinish() {
		AppProjectGenerator generator = new AppProjectGenerator(mainPage.getProjectHandle());
		URI location = null;
		if (!mainPage.useDefaults()) {
			location = mainPage.getLocationURI();
		}
		IProject[] refProjects = null;
		if (referencePage != null) {
			refProjects = referencePage.getReferencedProjects();
		}
		IProjectDescription description = generator.getProjectDescription(location, refProjects);

		CreateProjectOperation op = new CreateProjectOperation(description, "Create Android project") {
			@Override
			protected void doExecute(IProgressMonitor monitor, IAdaptable uiInfo) throws CoreException {
				super.doExecute(monitor, uiInfo);
				generator.setPackageName("com.example.emptyapp");
				generator.setActivityName("MainActivity");
				generator.setLayoutName("activity_main");
				generator.generate(monitor);
				List<IFile> toOpen = generator.getFilesToOpen();
				for (IFile file : toOpen) {
					selectAndReveal(file);
				}
			}

			@Override
			public boolean canUndo() {
				// Not set up to undo
				return false;
			}

			@Override
			protected ISchedulingRule getExecuteSchedulingRule() {
				// Lock the workspace. JDT has a magic project it needs to lock.
				return ResourcesPlugin.getWorkspace().getRoot();
			}
		};

		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						op.execute(monitor, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
					} catch (ExecutionException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InterruptedException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return true;
	}

}
