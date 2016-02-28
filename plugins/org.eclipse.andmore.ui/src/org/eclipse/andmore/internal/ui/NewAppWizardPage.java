package org.eclipse.andmore.internal.ui;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewAppWizardPage extends WizardPage {

	protected NewAppWizardPage() {
		super("NewAppWizardPage"); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		control.setLayout(layout);

		Label label = new Label(control, SWT.NONE);
		label.setText("Application Name:");

		Text text = new Text(control, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		setControl(control);
	}

}
