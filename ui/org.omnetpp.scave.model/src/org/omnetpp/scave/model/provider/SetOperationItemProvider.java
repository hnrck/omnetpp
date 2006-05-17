/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.omnetpp.scave.model.provider;


import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.ResourceLocator;

import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;

import org.omnetpp.scave.model.ScaveModelPackage;
import org.omnetpp.scave.model.SetOperation;

/**
 * This is the item provider adapter for a {@link org.omnetpp.scave.model.SetOperation} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class SetOperationItemProvider
	extends ItemProviderAdapter
	implements	
		IEditingDomainItemProvider,	
		IStructuredItemContentProvider,	
		ITreeItemContentProvider,	
		IItemLabelProvider,	
		IItemPropertySource {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SetOperationItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	/**
	 * This returns the property descriptors for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List getPropertyDescriptors(Object object) {
		if (itemPropertyDescriptors == null) {
			super.getPropertyDescriptors(object);

			addFilenamePatternPropertyDescriptor(object);
			addRunNamePatternPropertyDescriptor(object);
			addExperimentNamePatternPropertyDescriptor(object);
			addMeasurementNamePatternPropertyDescriptor(object);
			addReplicationNamePatternPropertyDescriptor(object);
			addModuleNamePatternPropertyDescriptor(object);
			addNamePatternPropertyDescriptor(object);
			addSourceDatasetPropertyDescriptor(object);
			addFromRunsWherePropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Filename Pattern feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFilenamePatternPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_SetOperation_filenamePattern_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_SetOperation_filenamePattern_feature", "_UI_SetOperation_type"),
				 ScaveModelPackage.Literals.SET_OPERATION__FILENAME_PATTERN,
				 true,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Run Name Pattern feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addRunNamePatternPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_SetOperation_runNamePattern_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_SetOperation_runNamePattern_feature", "_UI_SetOperation_type"),
				 ScaveModelPackage.Literals.SET_OPERATION__RUN_NAME_PATTERN,
				 true,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Experiment Name Pattern feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addExperimentNamePatternPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_SetOperation_experimentNamePattern_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_SetOperation_experimentNamePattern_feature", "_UI_SetOperation_type"),
				 ScaveModelPackage.Literals.SET_OPERATION__EXPERIMENT_NAME_PATTERN,
				 true,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Measurement Name Pattern feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addMeasurementNamePatternPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_SetOperation_measurementNamePattern_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_SetOperation_measurementNamePattern_feature", "_UI_SetOperation_type"),
				 ScaveModelPackage.Literals.SET_OPERATION__MEASUREMENT_NAME_PATTERN,
				 true,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Replication Name Pattern feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addReplicationNamePatternPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_SetOperation_replicationNamePattern_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_SetOperation_replicationNamePattern_feature", "_UI_SetOperation_type"),
				 ScaveModelPackage.Literals.SET_OPERATION__REPLICATION_NAME_PATTERN,
				 true,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Module Name Pattern feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addModuleNamePatternPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_SetOperation_moduleNamePattern_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_SetOperation_moduleNamePattern_feature", "_UI_SetOperation_type"),
				 ScaveModelPackage.Literals.SET_OPERATION__MODULE_NAME_PATTERN,
				 true,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Name Pattern feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addNamePatternPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_SetOperation_namePattern_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_SetOperation_namePattern_feature", "_UI_SetOperation_type"),
				 ScaveModelPackage.Literals.SET_OPERATION__NAME_PATTERN,
				 true,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Source Dataset feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSourceDatasetPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_SetOperation_sourceDataset_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_SetOperation_sourceDataset_feature", "_UI_SetOperation_type"),
				 ScaveModelPackage.Literals.SET_OPERATION__SOURCE_DATASET,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the From Runs Where feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addFromRunsWherePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_SetOperation_fromRunsWhere_feature"),
				 getString("_UI_PropertyDescriptor_description", "_UI_SetOperation_fromRunsWhere_feature", "_UI_SetOperation_type"),
				 ScaveModelPackage.Literals.SET_OPERATION__FROM_RUNS_WHERE,
				 true,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This returns SetOperation.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/SetOperation"));
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getText(Object object) {
		String label = ((SetOperation)object).getFilenamePattern();
		return label == null || label.length() == 0 ?
			getString("_UI_SetOperation_type") :
			getString("_UI_SetOperation_type") + " " + label;
	}

	/**
	 * This handles model notifications by calling {@link #updateChildren} to update any cached
	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void notifyChanged(Notification notification) {
		updateChildren(notification);

		switch (notification.getFeatureID(SetOperation.class)) {
			case ScaveModelPackage.SET_OPERATION__FILENAME_PATTERN:
			case ScaveModelPackage.SET_OPERATION__RUN_NAME_PATTERN:
			case ScaveModelPackage.SET_OPERATION__EXPERIMENT_NAME_PATTERN:
			case ScaveModelPackage.SET_OPERATION__MEASUREMENT_NAME_PATTERN:
			case ScaveModelPackage.SET_OPERATION__REPLICATION_NAME_PATTERN:
			case ScaveModelPackage.SET_OPERATION__MODULE_NAME_PATTERN:
			case ScaveModelPackage.SET_OPERATION__NAME_PATTERN:
			case ScaveModelPackage.SET_OPERATION__SOURCE_DATASET:
			case ScaveModelPackage.SET_OPERATION__FROM_RUNS_WHERE:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
		}
		super.notifyChanged(notification);
	}

	/**
	 * This adds to the collection of {@link org.eclipse.emf.edit.command.CommandParameter}s
	 * describing all of the children that can be created under this object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void collectNewChildDescriptors(Collection newChildDescriptors, Object object) {
		super.collectNewChildDescriptors(newChildDescriptors, object);
	}

	/**
	 * Return the resource locator for this item provider's resources.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ResourceLocator getResourceLocator() {
		return ScaveEditPlugin.INSTANCE;
	}

}
