package org.omnetpp.ned.editor.graph.edit;

import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.omnetpp.ned2.model.NEDElement;
import org.omnetpp.ned2.model.notification.INEDChangeListener;
import org.omnetpp.ned2.model.notification.NEDModelEvent;

/**
 * Provides support for Container EditParts.
 */
abstract public class ContainerEditPart 
   extends AbstractGraphicalEditPart implements INEDChangeListener {

    protected long lastEventSerial;
    
    @Override
    public void activate() {
        if (isActive()) return;
        super.activate();
        // register as listener of the model object
        getNEDModel().getListeners().add(this);
    }

    /**
     * Makes the EditPart insensible to changes in the model by removing itself
     * from the model's list of listeners.
     */
    @Override
    public void deactivate() {
        if (!isActive()) return;
        super.deactivate();
        getNEDModel().getListeners().remove(this);
    }

    /**
     * Installs the desired EditPolicies for this.
     */
    @Override
    protected void createEditPolicies() {
    }

    /**
     * Returns the model associated with this as a NEDElement.
     * 
     * @return The model of this as a NedElement.
     */
    protected NEDElement getNEDModel() {
        return (NEDElement) getModel();
    }
    
    /**
     * Refreshes all visuals for ALL children (delegates to the children)
     */
    protected void refreshChildrenVisuals() {
    	for(Object child : getChildren())
    		((ContainerEditPart)child).refreshVisuals();
    }
    
    
    protected void refreshChildrenConnections() {
        for(Object child : getChildren()) {
            for(Object conn : ((ContainerEditPart)child).getSourceConnections())
                ((ModuleConnectionEditPart)conn).refresh();
            
            for(Object conn : ((ContainerEditPart)child).getTargetConnections())
                ((ModuleConnectionEditPart)conn).refresh();
        }
    }

    public void modelChanged(NEDModelEvent event) {
        String nameString = getNEDModel().getAttribute("name");
        if (nameString == null) 
            nameString = "";
        System.out.println("NOTIFY ON: "+getModel().getClass().getSimpleName()+" "+nameString+" "+event);
    }
}
