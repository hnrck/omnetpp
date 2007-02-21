package org.omnetpp.scave.model2;

import static org.omnetpp.scave.model2.RunAttribute.EXPERIMENT;
import static org.omnetpp.scave.model2.RunAttribute.MEASUREMENT;
import static org.omnetpp.scave.model2.RunAttribute.REPLICATION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.omnetpp.scave.engine.FileRunList;
import org.omnetpp.scave.engine.IDList;
import org.omnetpp.scave.engine.ResultFile;
import org.omnetpp.scave.engine.ResultFileList;
import org.omnetpp.scave.engine.ResultFileManager;
import org.omnetpp.scave.engine.ResultItem;
import org.omnetpp.scave.engine.Run;
import org.omnetpp.scave.engine.RunList;
import org.omnetpp.scave.engine.StringMap;
import org.omnetpp.scave.model.Add;
import org.omnetpp.scave.model.Analysis;
import org.omnetpp.scave.model.Chart;
import org.omnetpp.scave.model.ChartSheet;
import org.omnetpp.scave.model.ChartSheets;
import org.omnetpp.scave.model.Dataset;
import org.omnetpp.scave.model.DatasetType;
import org.omnetpp.scave.model.Group;
import org.omnetpp.scave.model.Property;
import org.omnetpp.scave.model.ScaveModelFactory;
import org.omnetpp.scave.model.ScaveModelPackage;

/**
 * A collection of static methods to manipulate model objects
 * @author andras
 */
public class ScaveModelUtil {
	
	private static final String DEFAULT_CHARTSHEET_NAME = "default";
	
	private static final ScaveModelFactory factory = ScaveModelFactory.eINSTANCE;
	private static final ScaveModelPackage pkg = ScaveModelPackage.eINSTANCE;
	
	public static ChartSheet createDefaultChartSheet() {
		ChartSheet chartsheet = factory.createChartSheet();
		chartsheet.setName(DEFAULT_CHARTSHEET_NAME);
		return chartsheet;
	}
	
	public static Dataset createDataset(String name, DatasetType type) {
		Dataset dataset = factory.createDataset();
		dataset.setName(name);
		dataset.setType(type);
		return dataset;
	}

	public static Dataset createDataset(String name, DatasetType type, Filter filter) {
		Dataset dataset = factory.createDataset();
		dataset.setName(name);
		dataset.setType(type);
		dataset.getItems().add(createAdd(filter));
		return dataset;
	}
	
	public static Dataset createDataset(String name, DatasetType type, ResultItem[] items, String[] runidFields) {
		Dataset dataset = factory.createDataset();
		dataset.setName(name);
		dataset.setType(type);
		dataset.getItems().addAll(createAdds(items, runidFields));
		return dataset;
	}
	
	public static Chart createChart(String name) {
		Chart chart = factory.createChart();
		chart.setName(name);
		return chart;
	}
	
	public static Add createAdd(String filterString) {
		Add add = factory.createAdd();
		add.setFilterPattern(filterString);
		return add;
	}

	public static Add createAdd(Filter filter) {
		Add add = factory.createAdd();
		add.setFilenamePattern(filter.getField(FilterHints.FIELD_FILENAME));
		add.setRunNamePattern(filter.getField(FilterHints.FIELD_RUNNAME));
		add.setExperimentNamePattern(filter.getField(FilterHints.FIELD_EXPERIMENT));
		add.setMeasurementNamePattern(filter.getField(FilterHints.FIELD_MEASUREMENT));
		add.setReplicationNamePattern(filter.getField(FilterHints.FIELD_REPLICATION));
		add.setModuleNamePattern(filter.getField(FilterHints.FIELD_MODULENAME));
		add.setNamePattern(filter.getField(FilterHints.FIELD_DATANAME));
		return add;
	}
	
	public static Collection<Add> createAdds(ResultItem[] items, String[] runidFields) {
		List<Add> adds = new ArrayList<Add>(items.length);
		for (ResultItem item : items)
			adds.add(createAdd(item, runidFields));
		return adds;
	}
	
	public static Add createAdd(ResultItem item, String[] runidFields) {
		Add add = factory.createAdd();
		ResultFile file = item.getFileRun().getFile();
		Run run = item.getFileRun().getRun();
		for (String field : runidFields) {
			if (field == FilterHints.FIELD_FILENAME)
				add.setFilenamePattern(file.getFilePath());
			else if (field == FilterHints.FIELD_RUNNAME)
				add.setRunNamePattern(run.getRunName());
			else if (field == FilterHints.FIELD_EXPERIMENT)
				add.setExperimentNamePattern(run.getAttribute(RunAttribute.EXPERIMENT));
			else if (field == FilterHints.FIELD_MEASUREMENT)
				add.setExperimentNamePattern(run.getAttribute(RunAttribute.MEASUREMENT));
			else if (field == FilterHints.FIELD_REPLICATION)
				add.setExperimentNamePattern(run.getAttribute(RunAttribute.REPLICATION));
		}
		add.setModuleNamePattern(item.getModuleName());
		add.setNamePattern(item.getName());
		return add;
	}
	
	/**
	 * Returns the analysis node of the specified resource.
	 * It is assumed that the resource contains exactly one analysis node as 
	 * content.
	 */
	public static Analysis getAnalysis(Resource resource) {
		Assert.isTrue(resource.getContents().size() == 1 && resource.getContents().get(0) instanceof Analysis,
				"Analysis node not found in: " + resource.getURI().toString());
		return (Analysis)resource.getContents().get(0);
	}
	
	/**
	 * Returns the analysis node containing <code>eobject</code>. 
	 */
	public static Analysis getAnalysis(EObject eobject) {
		Assert.isTrue(eobject.eClass().getEPackage() == pkg,
				"Scave model object expected, received: " + eobject.toString());
		return getAnalysis(eobject.eResource());
	}
	
	public static ChartSheet getDefaultChartSheet(Resource resource) {
		Analysis analysis = getAnalysis(resource);
		for (ChartSheet chartsheet : (List<ChartSheet>)analysis.getChartSheets().getChartSheets())
			if (DEFAULT_CHARTSHEET_NAME.equals(chartsheet.getName()))
				return chartsheet;
		return null;
	}
	
	public static Dataset findEnclosingDataset(Chart chart) {
		EObject parent = chart.eContainer();
		while (parent != null && !(parent instanceof Dataset))
			parent = parent.eContainer();
		return (Dataset)parent;
	}
	
	public static DatasetType getDatasetType(Chart chart) {
		Dataset dataset = findEnclosingDataset(chart);
		return dataset != null ? dataset.getType() : null;
	}

	/**
	 * Returns the datasets in the resource having the specified type.
	 */
	public static List<Dataset> findDatasets(Resource resource, DatasetType type) {
		List<Dataset> result = new ArrayList<Dataset>();
		Analysis analysis = getAnalysis(resource);
		if (analysis.getDatasets() != null) {
			for (Object object : analysis.getDatasets().getDatasets()) {
				Dataset dataset = (Dataset)object;
				if (dataset.getType().equals(type))
					result.add((Dataset)dataset);
			}
		}
		return result;
	}
	
	public static <T extends EObject> T findEnclosingObject(EObject object, Class<T> type) {
		while (object != null && !type.isInstance(object))
			object = object.eContainer();
		return (T)object;
	}
	
	/**
	 * Returns all object in the container having the specified type. 
	 */
	public static <T extends EObject> List<T> findObjects(EObject container, Class<T> type) {
		ArrayList<T> objects = new ArrayList<T>();
		for (TreeIterator iterator = container.eAllContents(); iterator.hasNext(); ) {
			Object object = iterator.next();
			if (type.isInstance(object))
				objects.add((T)object);
		}
		return objects;
 	}
	
	/**
	 * Returns all objects in the resource having the specified type.
	 */
	public static <T extends EObject> List<T> findObjects(Resource resource, Class<T> type) {
		ArrayList<T> objects = new ArrayList<T>();
		for (TreeIterator iterator = resource.getAllContents(); iterator.hasNext(); ) {
			Object object = iterator.next();
			if (type.isInstance(object))
				objects.add((T)object);
		}
		return objects;
	}
	
	/**
	 * Collect charts from the given collection. 
	 */
	public static List<Chart> collectCharts(Collection items) {
		List<Chart> charts = new ArrayList<Chart>();
		for (Object item : items)
			if (item instanceof Chart) {
				charts.add((Chart)item);
			}
			else if (item instanceof Dataset || item instanceof Group) {
				for (TreeIterator iter = ((EObject)item).eAllContents(); iter.hasNext(); ) {
					Object object = iter.next();
					if (object instanceof Chart)
						charts.add((Chart)object);
					else if (!(object instanceof Dataset || object instanceof Group))
						iter.prune();
				}
			}
		return charts;
	}
	
	/**
	 * Collect unreferenced charts from the given collection. 
	 */
	public static Collection<Chart> collectUnreferencedCharts(Collection items) {
		List<Chart> charts = collectCharts(items);
		if (charts.size() > 0) {
			Map references = ScaveCrossReferencer.find(charts.get(0).eResource());
			charts.removeAll(references.keySet());
		}
		return charts;
	}
	
	/**
	 * Collect references to scave objects.
	 * Currently the only references are from chart sheets to charts,
	 * so the scope of the search is limited to chart sheets. 
	 */
	static class ScaveCrossReferencer extends EcoreUtil.CrossReferencer {

		protected ScaveCrossReferencer(Collection eobjects) {
			super(eobjects);
		}
		
		public static Map find(Resource resource) {
			return EcoreUtil.CrossReferencer.find(Collections.singleton(resource));
		}

		@Override
		protected boolean containment(EObject eObject) {
			return eObject instanceof Resource ||
				   eObject instanceof Analysis ||
				   eObject instanceof ChartSheets ||
				   eObject instanceof ChartSheet;
		}
	}
	
	public static Property getChartProperty(Chart chart, String propertyName) {
		for (Object object : chart.getProperties()) {
			Property property = (Property)object;
			if (property.getName().equals(propertyName))
				return property;
		}
		return null;
	}
	
	public static void setChartProperty(EditingDomain ed, Chart chart, String propertyName, String propertyValue) {
		Property property = getChartProperty(chart, propertyName);
		Command command;
		if (property == null) {
			property = factory.createProperty();
			property.setName(propertyName);
			property.setValue(propertyValue);
			command = AddCommand.create(
						ed,
						chart,
						pkg.getChart_Properties(),
						property);
		}
		else {
			command = SetCommand.create(
						ed,
						property,
						pkg.getProperty_Value(),
						propertyValue);
		}
		ed.getCommandStack().execute(command);
	}
	
	public static IDList getAllIDs(ResultFileManager manager, DatasetType type) {
		switch (type.getValue()) {
		case DatasetType.SCALAR: 	return manager.getAllScalars();
		case DatasetType.VECTOR:	return manager.getAllVectors();
		case DatasetType.HISTOGRAM:	return new IDList(); // TODO
		}
		
		Assert.isTrue(false, "Unknown dataset type: " + type);
		return null;
	}
	
	public static ResultItem[] getResultItems(IDList idlist, ResultFileManager manager) {
		int size = (int)idlist.size();
		ResultItem[] items = new ResultItem[size];
		for (int i = 0; i < size; ++i)
			items[i] = manager.getItem(idlist.get(i));
		return items;
	}
	
	public static IDList filterIDList(IDList idlist, Filter params, ResultFileManager manager) {
		Assert.isTrue(params.getFilterPattern() != null);
		// TODO: pattern may be malformed, catch exceptions and report error
		return manager.filterIDList(idlist, params.getFilterPattern());
	}
	
	private static void addAttribute(StringMap attrs, String name, String value) {
		if (value != null && value.length() > 0)
			attrs.set(name, value);
	}
	
	/**
	 * Returns the default chart sheet.
	 * When the resource did not contain default chart sheet a new one is created,
	 * and a AddCommand is appended to the <code>command</command>, that adds
	 * the new chart sheet to the resource. 
	 */
	public static ChartSheet getOrCreateDefaultChartSheet(EditingDomain ed, CompoundCommand command, Resource resource) {
		ChartSheet chartsheet = getDefaultChartSheet(resource);
		if (chartsheet == null) {
			chartsheet = createDefaultChartSheet();
			command.append(
				AddCommand.create(
					ed,
					getAnalysis(resource).getChartSheets(),
					pkg.getChartSheets_ChartSheets(),
					chartsheet,
					0));
		}
		return chartsheet;
	}
	
	public static void dumpIDList(String header, IDList idlist, ResultFileManager manager) {
		System.out.print(header);
		if (idlist.size() == 0)
			System.out.println("Empty");
		else {
			System.out.println();
			for (int i = 0; i < idlist.size(); ++i) {
				ResultItem r = manager.getItem(idlist.get(i));
				System.out.println(
					String.format("File: %s Run: %s Module: %s Name: %s",
						r.getFileRun().getFile().getFilePath(),
						r.getFileRun().getRun().getRunName(),
						r.getModuleName(),
						r.getName()));
			}
		}
	}
}
