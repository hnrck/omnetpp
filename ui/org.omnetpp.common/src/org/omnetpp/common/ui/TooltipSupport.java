package org.omnetpp.common.ui;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 * Provides tooltip for a widget. SWT's Control.setTooltipText() has several
 * limitations which makes it unsuitable for presenting large multi-line
 * tooltips:
 *   - tends to wrap long lines (SWT's Tooltip class does that too)
 *   - tooltip disappears after about 5s (Windows), which is not enough to
 *     read long texts
 *   - lazy generation of tooltip text is not possible (there's no such thing 
 *     as TooltipAboutToShowListener)
 * This class overcomes these limitations.
 * 
 * One instance can adapt several controls (ie. controls may share the same 
 * tooltip).
 *   
 * @author Andras
 */
public class TooltipSupport {
	protected DefaultInformationControl tooltipWidget;
	protected AllInOneMouseListener mouseListener = new AllInOneMouseListener();
	protected ITooltipProvider tooltipProvider;

	private class AllInOneMouseListener implements MouseListener, MouseTrackListener, MouseMoveListener {
		public void mouseDoubleClick(MouseEvent e) {}
		public void mouseUp(MouseEvent e) {}
		public void mouseEnter(MouseEvent e) {}
		public void mouseExit(MouseEvent e) {}

		public void mouseHover(MouseEvent e) {
			if (e.widget instanceof Control && (e.stateMask & SWT.BUTTON_MASK) == 0)
				displayTooltip((Control)e.widget, e.x, e.y);
		}

		public void mouseMove(MouseEvent e) {
			removeTooltip();
		}

		public void mouseDown(MouseEvent e) {
			removeTooltip();
		}
	}
	
	/**
	 * Create a tooltip support object.
	 */
	public TooltipSupport(ITooltipProvider tooltipProvider) {
		this.tooltipProvider = tooltipProvider;
	}

	/**
	 * Create a tooltip support object, and adapt the given control.
	 */
	public TooltipSupport(final Control c, ITooltipProvider tooltipProvider) {
		this(tooltipProvider);
		adapt(c);
	}

	/**
	 * Adds tooltip support for the given control.
	 */
	public void adapt(final Control c) {
		c.addMouseListener(mouseListener);
		c.addMouseMoveListener(mouseListener);
		c.addMouseTrackListener(mouseListener);
		c.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				forget(c);
			}
		});
	}

	/**
	 * Removes tooltip support from the given control. This does NOT need to 
	 * be called when the widget gets disposed, because this class listens on
	 * widgetDisposed() automatically.
	 */
	public void forget(Control c) {
		c.removeMouseListener(mouseListener);
		c.removeMouseMoveListener(mouseListener);
		c.removeMouseTrackListener(mouseListener);
	}
	
	protected void displayTooltip(Control control, int x, int y) {
		String tooltipText = tooltipProvider.getTooltipFor(control, x, y);
		if (tooltipText!=null) {
			tooltipWidget = new DefaultInformationControl(control.getShell());
			tooltipWidget.setInformation(" "+tooltipText.replaceAll("\n", "\n ")); // prefix each line with a space, for left margin
			tooltipWidget.setLocation(control.toDisplay(x,y+20));
			Point size = tooltipWidget.computeSizeHint();
			tooltipWidget.setSize(size.x+3, size.y+3); // add some right/bottom margin 
			tooltipWidget.setVisible(true);
		}
	}

	protected void removeTooltip() {
		if (tooltipWidget!=null) {
			tooltipWidget.setVisible(false);
			tooltipWidget.dispose();
			tooltipWidget = null;
		}
	}
}


