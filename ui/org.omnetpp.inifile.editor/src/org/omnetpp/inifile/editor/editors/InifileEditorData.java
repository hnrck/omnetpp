package org.omnetpp.inifile.editor.editors;

import java.util.HashMap;

import org.eclipse.core.runtime.Assert;
import org.omnetpp.inifile.editor.model.IInifileDocument;
import org.omnetpp.inifile.editor.model.InifileAnalyzer;

/**
 * Data held by InifileEditor. Introduced because we don't want to pass around
 * a reference to the whole editor.
 * 
 * @author Andras
 */
public class InifileEditorData {
	private InifileEditor editor;
	private IInifileDocument document;
	private InifileAnalyzer analyzer;

	/**
	 * Whether the form page with the given category is in the "Detailed" or "Normal" view 
	 */
	public HashMap<String,Boolean> formPageCategoryDetailedFlags = new HashMap<String, Boolean>(); 
	
	/**
	 * Initialize the object
	 */
	public void initialize(InifileEditor editor, IInifileDocument document, InifileAnalyzer analyzer) {
		Assert.isTrue(this.editor==null);
		this.editor = editor;
		this.document = document;
		this.analyzer = analyzer;
	}

	/**
	 * Access to inifile contents.
	 */
	public IInifileDocument getInifileDocument() {
		return document;
	}

	/**
	 * Access to the editor's inifile analyzer. 
	 */
	public InifileAnalyzer getInifileAnalyzer() {
		return analyzer;
	}
	
	/**
	 * Unfortunately, at some places we still need the editor reference...
	 */
	public InifileEditor getInifileEditor() {
		return editor;
	}

}
