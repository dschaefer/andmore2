/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.andmore.core.AppProjectGenerator;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
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

		// only add page if there are already projects in the workspace
		if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
			referencePage = new WizardNewProjectReferencePage("basicReferenceProjectPage");//$NON-NLS-1$
			referencePage.setTitle("Project References");
			referencePage.setDescription("Project References");
			this.addPage(referencePage);
		}
	}

	protected String getTemplateManifestPath() {
		return "templates/app/empty/manifest.xml"; //$NON-NLS-1$
	}

	@Override
	public boolean performFinish() {
		AppProjectGenerator generator = new AppProjectGenerator(getTemplateManifestPath());
		generator.setProjectName(mainPage.getProjectName());
		if (!mainPage.useDefaults()) {
			generator.setLocationURI(mainPage.getLocationURI());
		}
		generator.setPackageName("com.example.emptyapp");
		generator.setActivityName("MainActivity");
		generator.setLayoutName("activity_main");

		Map<String, Object> model = new HashMap<>();

		try {
			getContainer().run(true, true, new WorkspaceModifyOperation() {
				@Override
				protected void execute(IProgressMonitor monitor)
						throws CoreException, InvocationTargetException, InterruptedException {
					monitor.beginTask("Generating project", 1); //$NON-NLS-1$
					generator.generate(model, monitor);
					monitor.done();
				}

				@Override
				public ISchedulingRule getRule() {
					return ResourcesPlugin.getWorkspace().getRoot();
				}
			});
		} catch (InterruptedException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return true;
	}

}
