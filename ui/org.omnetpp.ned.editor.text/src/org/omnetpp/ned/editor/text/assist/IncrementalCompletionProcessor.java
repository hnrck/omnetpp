package org.omnetpp.ned.editor.text.assist;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;
import org.omnetpp.ned.editor.text.NedHelper;
import org.omnetpp.ned.editor.text.TextualNedEditorPlugin;

/**
 * Generic incremental type completion processor.
 */
public abstract class IncrementalCompletionProcessor extends TemplateCompletionProcessor {

    private static final String DEFAULT_IMAGE= "icons/16/template.gif"; //$NON-NLS-1$

    /**
     * Helper comparator calss to compare CompletionProposals using relevance and the the display name 
     */
    protected static class CompletionProposalComparator implements Comparator {
        private static CompletionProposalComparator instance = null;
        
        public static CompletionProposalComparator getInstance() {
            if (instance == null)
                instance = new CompletionProposalComparator();
            return instance;
        }
        
        public int compare(Object arg0, Object arg1) {
           
            // first order according to the relevance
            if (arg0 instanceof TemplateProposal && arg1 instanceof TemplateProposal) {
                int compRes = ((TemplateProposal) arg1).getRelevance() - ((TemplateProposal) arg0).getRelevance();
                if (compRes != 0) return compRes;
                return ((TemplateProposal)arg0).getDisplayString().compareToIgnoreCase(
                        ((TemplateProposal)arg1).getDisplayString());
            }
            // if relevance is the same compare using display names
            return ((ICompletionProposal)arg0).getDisplayString().compareToIgnoreCase(
                        ((ICompletionProposal)arg1).getDisplayString());
        }
    }

    /**
     * Create a List of ICompletionProposal from an array of string. Checks the word under the current cursor
     * position and filters the proposal accordingly.
     * @param viewer
     * @param documentOffset
     * @param wordDetector
     * @param proposalString
     * @return List of proposals
     */
    protected List createProposals(ITextViewer viewer, int documentOffset, IWordDetector wordDetector, String startStr, String[] proposalString, String endStr, String description) {
        List propList = new ArrayList();
        IRegion wordRegion = detectWordRegion(viewer, documentOffset, wordDetector); 
        String prefix = "";
        try {
            prefix = viewer.getDocument().get(wordRegion.getOffset(), documentOffset - wordRegion.getOffset());
        } catch (BadLocationException e) { }

        Arrays.sort(proposalString);
        
        for (int i = 0 ;i < proposalString.length; ++i) {
            String prop = startStr + proposalString[i] + endStr;
            if (prop.toLowerCase().startsWith(prefix.toLowerCase())) {
            	String displayText = description==null ? prop : prop+" - "+description;
                propList.add(new CompletionProposal(prop, wordRegion.getOffset(), wordRegion.getLength(), prop.length(), null, displayText, null, null));
            }
        }

        return propList;
    }
    
    /**
     * Detects the boundary of a single word under the current position (defined by the wordDetector)  
     * @param viewer The document viewer where the word must be detected
     * @param documentOffset urrent cursor offset
     * @param wordDetector A specific word detector to detect in-word characters
     * @return The region that should be replaced
     */
    protected IRegion detectWordRegion(ITextViewer viewer, int documentOffset, IWordDetector wordDetector) {
        int offset = documentOffset;
        int length = 0;
        
        try {
            // find the first char that may not be the trailing part of a word.
            while (offset > 0 && wordDetector.isWordPart(viewer.getDocument().getChar(offset - 1)))
                offset--;
            
            // check if the first char of the word is also ok.
            if(offset > 0 && wordDetector.isWordStart(viewer.getDocument().getChar(offset - 1)))
                offset--;
            // now we should stand on the first char of the word
            if(offset + length < viewer.getDocument().getLength() 
                    && wordDetector.isWordStart(viewer.getDocument().getChar(offset + length)))
                length++;
            // now iterate throug the rest of chars until a character cannot be recognized as an in/word char
            while(offset + length < viewer.getDocument().getLength() 
                    && wordDetector.isWordPart(viewer.getDocument().getChar(offset + length)))
                length++;
            
        } catch (BadLocationException e) {
            offset = documentOffset;
            length = 0;
        }
        
        return new Region(offset, length);
        
    }

	/**
	 * This method is necessary because TemplateCompletionProcessor.computeCompletionProposals()
	 * doesn't let us specify what templates we want to add, but insists on calling
	 * getTemplates() instread. This is a copy of that computeCompletionProposals(), with
	 * Template[] added to the arg list. 
	 *
	 * @author andras
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	public ICompletionProposal[] createTemplateProposals(ITextViewer viewer, int offset, Template[] templates) {

		ITextSelection selection= (ITextSelection) viewer.getSelectionProvider().getSelection();

		// adjust offset to end of normalized selection
		if (selection.getOffset() == offset)
			offset= selection.getOffset() + selection.getLength();

		String prefix= extractPrefix(viewer, offset);
		Region region= new Region(offset - prefix.length(), prefix.length());
		TemplateContext context= createContext(viewer, region);
		if (context == null)
			return new ICompletionProposal[0];

		context.setVariable("selection", selection.getText()); // name of the selection variables {line, word}_selection //$NON-NLS-1$

		//Template[] templates= getTemplates(context.getContextType().getId());

		List matches= new ArrayList();
		for (int i= 0; i < templates.length; i++) {
			Template template= templates[i];
			try {
				context.getContextType().validate(template.getPattern());
			} catch (TemplateException e) {
				continue;
			}
			if (template.matches(prefix, context.getContextType().getId()))
				matches.add(createProposal(template, context, (IRegion) region, getRelevance(template, prefix)));
		}

		Collections.sort(matches, CompletionProposalComparator.getInstance());

		return (ICompletionProposal[]) matches.toArray(new ICompletionProposal[matches.size()]);
	}
    
    /**
     * Returns the all templates for this contextTypeId.
     * 
     * @param contextTypeId the context type
     * @return all templates
     */
    protected Template[] getTemplates(String contextTypeId) {
        return TextualNedEditorPlugin.getDefault().getTemplateStore().getTemplates(contextTypeId);
    }

    /**
     * Return the XML context type that is supported by this plug-in.
     * 
     * @param viewer the viewer, ignored in this implementation
     * @param region the region, ignored in this implementation
     * @return the supported XML context type
     */
    protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
        return TextualNedEditorPlugin.getDefault().getContextTypeRegistry().getContextType(NedContextType.DEFAULT_CONTEXT_TYPE);
    }

    /**
     * Always return the default image.
     * 
     * @param template the template, ignored in this implementation
     * @return the defaul template image
     */
    protected Image getImage(Template template) {
        ImageRegistry registry= TextualNedEditorPlugin.getDefault().getImageRegistry();
        Image image= registry.get(DEFAULT_IMAGE);
        if (image == null) {
            ImageDescriptor desc= TextualNedEditorPlugin.imageDescriptorFromPlugin(DEFAULT_IMAGE); //$NON-NLS-1$
            registry.put(DEFAULT_IMAGE, desc);
            image= registry.get(DEFAULT_IMAGE);
        }
        return image;
    }


}
