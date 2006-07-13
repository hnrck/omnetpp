package org.omnetpp.scave2.editors.ui;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * This is the edit dialog for scave model objects.
 * 
 * It receives an object and optionally a set of features to be edited
 * (defaults to all editable features).
 * It responses with the changed values.
 * 
 * @author tomi
 */
public class EditDialog extends Dialog {

	EObject object;
	EStructuralFeature[] features;
	ScaveObjectEditForm form;
	Object[] values;
	boolean[] isDirty;
	
	public EditDialog(
			Shell parentShell,
			EObject object) {
		this(parentShell, object, null);
	}
	
	public EditDialog(
			Shell parentShell,
			EObject object,
			EStructuralFeature[] features) {
		super(parentShell);
		this.object = object;
		this.features = features;
	}
	
	public EStructuralFeature[] getFeatures() {
		return form.getFeatures();
	}
	
	public boolean isDirty(int index) {
		return form.isDirty(index);
	}
	
	public Object getValue(int index) {
		return form.getValue(index);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Edit " + object.eClass().getName());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
		Composite panel = new Composite(composite, SWT.NONE);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		form = features == null ?
				ScaveObjectEditFormFactory.instance().createForm(object) :
				ScaveObjectEditFormFactory.instance().createForm(object, features);
		form.populatePanel(panel);
		features = form.getFeatures();
		for (int i = 0; i < features.length; ++i)
			form.setValue(i, object.eGet(features[i]));
		return composite;
	}
	
	@Override
	protected void okPressed() {
		applyChanges();
		super.okPressed();
	}
	
	private void applyChanges() {
		if (features != null) {
			isDirty = new boolean[features.length];
			values = new Object[features.length];
			for (int i = 0; i < values.length; ++i) {
				isDirty[i] = form.isDirty(i);
				values[i] = form.getValue(i);
			}
		}
	}
}
