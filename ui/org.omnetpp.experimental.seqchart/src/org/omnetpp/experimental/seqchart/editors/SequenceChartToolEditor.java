package org.omnetpp.experimental.seqchart.editors;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.omnetpp.experimental.seqchart.moduletree.ModuleTreeBuilder;
import org.omnetpp.experimental.seqchart.moduletree.ModuleTreeItem;
import org.omnetpp.scave.engine.EventEntry;
import org.omnetpp.scave.engine.EventLog;
import org.omnetpp.scave.engine.JavaFriendlyEventLogFacade;
import org.omnetpp.scave.engine.MessageEntry;
import org.omnetpp.scave.engine.ModuleEntry;

/**
 * Sequence chart display tool. (It is not actually an editor; it is only named so
 * because it extends EditorPart).
 * 
 * @author andras
 */
public class SequenceChartToolEditor extends EditorPart {

	private SashForm sashForm;
	//private Text text;
	private Canvas canvas;
	private Figure rootFigure;
	private XYLayout rootLayout;
	private SeqChartFigure seqChartFigure;
	private Combo eventcombo;  //XXX instead of this combo, events should be selectable from the text view at the bottom
	private EventLogTable eventLogTable;
	
	private EventLog eventLog;  // the log file loaded
	private ModuleTreeItem moduleTree; // modules in eventLog
	private ArrayList<ModuleTreeItem> axisModules; // which modules should have an axis
	private int currentEventNumber = -1;
	private EventLog filteredEventLog; // eventLog filtered for currentEventNumber

	private final Color CANVAS_BG_COLOR = ColorConstants.white;

	public SequenceChartToolEditor() {
		super();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		//TODO site.setSelectionProvider(new WhateverSelectionProvider(this));
		
		IFileEditorInput fileInput = (IFileEditorInput)input;
		String fileName = fileInput.getFile().getLocation().toFile().getAbsolutePath();

		eventLog = new EventLog(fileName);
		System.out.println("read "+eventLog.getNumEvents()+" events in "+eventLog.getNumModules()+" modules from "+fileName);
		
		extractModuleTree();
	}

	private void extractModuleTree() {
		ArrayList<ModuleTreeItem> modules = new ArrayList<ModuleTreeItem>();
		ModuleTreeBuilder treeBuilder = new ModuleTreeBuilder();
		for (int i=0; i<eventLog.getNumModules(); i++) {
			ModuleEntry mod = eventLog.getModule(i);
			modules.add(treeBuilder.addModule(mod.getModuleFullPath(), mod.getModuleClassName(), mod.getModuleId()));
		}
		moduleTree = treeBuilder.getModuleTree();
		axisModules = modules;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		sashForm = new SashForm(parent, SWT.VERTICAL | SWT.BORDER | SWT.SMOOTH);

		// add canvas into the upper half
		Composite upper = new Composite(sashForm, SWT.NONE);
		upper.setLayout(new GridLayout());

		Composite controlStrip = createControlStrip(upper);
		controlStrip.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// create canvas and add chart figure
		canvas = new Canvas(upper, SWT.DOUBLE_BUFFERED);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setupCanvas(canvas);

		// add event log table into lower half
		Composite lower = new Composite(sashForm, SWT.NONE);
		lower.setLayout(new FillLayout());

		eventLogTable = new EventLogTable(lower, SWT.MULTI);
		eventLogTable.getTable().addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// double-click or enter: filter for the current event
				int sel = eventLogTable.getTable().getSelectionIndex();
				EventEntry event = eventLogTable.eventForTableIndex(sel);
				showSequenceChartForEvent(event.getEventNumber());
			}
			public void widgetSelected(SelectionEvent e) {
				// mark selected events in the chart as well
				int[] sel = eventLogTable.getTable().getSelectionIndices();
				filteredEventLog.deselectAllEvents();
				for (int i=0; i<sel.length; i++) {
					EventEntry event = eventLogTable.eventForTableIndex(sel[i]);
					event.setIsSelected(true);
				}
				// show (scroll to) currently selected event
				int cur = eventLogTable.getTable().getSelectionIndex();
				EventEntry curEvent = eventLogTable.eventForTableIndex(cur);
				seqChartFigure.gotoTime(curEvent.getSimulationTime()); 
				seqChartFigure.repaint(); //XXX or just invalidate?
			}
		});
		
		// fill combo box with events
		fillEventCombo();
		// give eventLog to the chart for display
		showFullSequenceChart();
		
		seqChartFigure.setAxisModules(axisModules);
		
		//XXX this is an attempt at improving drag in the chart, but it apparently doesn't do the job
		//canvas.addMouseListener(new MouseListener() {
		//	public void mouseDoubleClick(MouseEvent e) {}
		//	public void mouseDown(MouseEvent e) {
		//		System.out.println("CANVAS CAPTURED");
		//		canvas.setCapture(true);
		//	}
		//	public void mouseUp(MouseEvent e) {
		//		canvas.setCapture(false);
		//		System.out.println("CANVAS RELEASED");
		//	}});
	}

	private Composite createControlStrip(Composite upper) {
		Composite controlStrip = new Composite(upper, SWT.NONE);
		controlStrip.setLayout(new GridLayout(5, false));
		eventcombo = new Combo(controlStrip, SWT.NONE);
		eventcombo.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		eventcombo.setVisibleItemCount(20);
		Button showNonDeliveryMessages = new Button(controlStrip, SWT.CHECK);
		showNonDeliveryMessages.setText("Blue");
		Button zoomIn = new Button(controlStrip, SWT.NONE);
		zoomIn.setText("Zoom in");
		Button zoomOut = new Button(controlStrip, SWT.NONE);
		zoomOut.setText("Zoom out");
		Button selectModules = new Button(controlStrip, SWT.NONE);
		selectModules.setText("Modules...");

		// add event handlers
		eventcombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				String sel = eventcombo.getText();
				sel = sel.trim();
				if (sel.startsWith("All")) {
					showFullSequenceChart();
				} else {
					sel = sel.replaceFirst(" .*", "");
					sel = sel.replaceAll("#", "");
					int eventNumber = -1;
					try {eventNumber = Integer.parseInt(sel);} catch (NumberFormatException ex) {}
					if (eventNumber<0) {
						messageBox(SWT.ICON_ERROR, "Error", "Please specify event number as \"#nnn\".");
						return;
					}
					showSequenceChartForEvent(eventNumber);
				}
			}
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}});

		zoomIn.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				seqChartFigure.zoomIn();
			}
			public void widgetSelected(SelectionEvent e) {
				seqChartFigure.zoomIn();
			}});
		
		zoomOut.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				seqChartFigure.zoomOut();
			}
			public void widgetSelected(SelectionEvent e) {
				seqChartFigure.zoomOut();
			}});

		selectModules.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				displayModuleTreeDialog();
			}
			public void widgetSelected(SelectionEvent e) {
				displayModuleTreeDialog();
			}});

		showNonDeliveryMessages.addSelectionListener(new SelectionListener () {
			public void widgetDefaultSelected(SelectionEvent e) {
				seqChartFigure.setShowNonDeliveryMessages(((Button)e.getSource()).getSelection());
			}
			public void widgetSelected(SelectionEvent e) {
				seqChartFigure.setShowNonDeliveryMessages(((Button)e.getSource()).getSelection());
			}
		});
		
		return controlStrip;
	}

	private void fillEventCombo() {
		eventcombo.removeAll();
    	eventcombo.add("All events");
    	JavaFriendlyEventLogFacade logFacade = new JavaFriendlyEventLogFacade(eventLog);
	    for (int i=0; i<logFacade.getNumEvents(); i++) 
	    	eventcombo.add(getLabelForEvent(logFacade, i));
    	eventcombo.select(0);
	}

	private String getLabelForEvent(JavaFriendlyEventLogFacade logFacade, int pos) {
		String label = "#"+logFacade.getEvent_i_eventNumber(pos)
			+" at t="+logFacade.getEvent_i_simulationTime(pos)
			+", module ("+logFacade.getEvent_i_module_moduleClassName(pos)+")"
			+logFacade.getEvent_i_module_moduleFullPath(pos)
			+" (id="+logFacade.getEvent_i_module_moduleId(pos)+"),"
			+" message ("+logFacade.getEvent_i_cause_messageClassName(pos)+")"
			+logFacade.getEvent_i_cause_messageName(pos);
		return label;
	}

	private void setupCanvas(Canvas canvas) {
		canvas.setBackground(CANVAS_BG_COLOR);
		
		LightweightSystem lws = new LightweightSystem(canvas);
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setScrollBarVisibility(ScrollPane.AUTOMATIC);
		rootFigure = new Figure();
		scrollPane.setContents(rootFigure);
		rootLayout = new XYLayout();
		rootFigure.setLayoutManager(rootLayout);
		lws.setContents(scrollPane);

		//addLabelFigure(10, 10, "Bla");
		seqChartFigure = new SeqChartFigure(canvas, scrollPane);
		rootFigure.add(seqChartFigure);
		rootLayout.setConstraint(seqChartFigure, new Rectangle(0,0,-1,-1));
		
		// set up canvas mouse operations: click, double-click
		seqChartFigure.addMouseListener(new MouseListener() {
			public void mouseDoubleClicked(MouseEvent me) {
				// filter for double-clicked event
				ArrayList<EventEntry> events = new ArrayList<EventEntry>();
				ArrayList<MessageEntry> msgs = new ArrayList<MessageEntry>();
				seqChartFigure.collectStuffUnderMouse(me.x, me.y, events, msgs);
				if (events.size()>1) { 
					//XXX pop up selection dialog instead?
					messageBox(SWT.ICON_INFORMATION, "Information", "Ambiguous double-click: there are "+events.size()+" events under the mouse! Zooming may help.");
				} else if (events.size()==1) {
					showSequenceChartForEvent(events.get(0).getEventNumber());
				}
			}
			public void mousePressed(MouseEvent me) {
				// goto that event in the log 
				ArrayList<EventEntry> events = new ArrayList<EventEntry>();
				ArrayList<MessageEntry> msgs = new ArrayList<MessageEntry>();
				seqChartFigure.collectStuffUnderMouse(me.x, me.y, events, msgs);
				filteredEventLog.deselectAllEvents(); //we may handle ctrl-click to extend selection as well
				if (events.size()>=1) { 
					EventEntry event = events.get(0);
					event.setIsSelected(true);
					eventLogTable.gotoEvent(event);
				}
				seqChartFigure.repaint(); // as selection has changed
			}
			public void mouseReleased(MouseEvent me) {}
		});
	}

	/**
	 * Goes to the given event and updates the chart.
	 */
	private void showSequenceChartForEvent(int eventNumber) {
		EventEntry event = eventLog.getEventByNumber(eventNumber);
		if (event==null) {
			messageBox(SWT.ICON_ERROR, "Error", "Event #"+eventNumber+" not found.");
			return;
		}
		currentEventNumber = eventNumber;
		String eventLabel = getLabelForEvent(new JavaFriendlyEventLogFacade(eventLog), eventLog.findEvent(event));
		eventcombo.setText(eventLabel);

		filteredEventLog = eventLog.traceEvent(event, true, true);
		System.out.println("filtered log: "+filteredEventLog.getNumEvents()+" events in "+filteredEventLog.getNumModules()+" modules");

		filteredEventLogChanged();
	}

	private void showFullSequenceChart() {
		currentEventNumber = -1;
		filteredEventLog = eventLog;

		filteredEventLogChanged();
	}

	private void filteredEventLogChanged() {
		seqChartFigure.setEventLog(filteredEventLog, moduleTree);
		eventLogTable.setInput(filteredEventLog);
	}

	private int messageBox(int style, String title, String message) {
		MessageBox m = new MessageBox(getEditorSite().getShell(), style);
		m.setText(title);
		m.setMessage(message);
		return m.open();
	}
	
	private void addLabelFigure(int x, int y, String text) {
/*
		Font someFont = new Font(null, "Arial", 12, SWT.BOLD); //XXX cache fonts!
		Label label = new Label(text, null);
		label.setFont(someFont);

		rootFigure.add(label);
		rootLayout.setConstraint(label, new Rectangle(x,y,-1,-1));
*/	}

	protected void displayModuleTreeDialog() {
		ModuleTreeDialog dialog = new ModuleTreeDialog(getSite().getShell(), moduleTree, axisModules);
		dialog.open();
		Object[] selection = dialog.getResult(); 
		if (selection != null) { // not cancelled
			axisModules = new ArrayList<ModuleTreeItem>();
			for (Object sel : selection)
				axisModules.add((ModuleTreeItem)sel);

			System.out.println("Selected:");
			for (ModuleTreeItem sel : axisModules)
				System.out.println(" "+sel.getModuleFullPath());

			// update chart
			seqChartFigure.setAxisModules(axisModules); 
		}
	}

	public void dispose() {
		super.dispose();
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}
	
	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
}
