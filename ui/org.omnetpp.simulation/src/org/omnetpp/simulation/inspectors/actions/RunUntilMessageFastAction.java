package org.omnetpp.simulation.inspectors.actions;

import java.io.IOException;

import org.omnetpp.simulation.SimulationPlugin;
import org.omnetpp.simulation.SimulationUIConstants;
import org.omnetpp.simulation.controller.Simulation.RunMode;
import org.omnetpp.simulation.model.cMessage;

/**
 *
 * @author Andras
 */
public class RunUntilMessageFastAction extends AbstractInspectorAction {
    public RunUntilMessageFastAction() {
        super("Run fast until next arrival of this message", AS_PUSH_BUTTON, SimulationPlugin.getImageDescriptor(SimulationUIConstants.IMG_TOOL_MRUN));
    }

    @Override
    public void run() {
        try {
            cMessage message = getMessage();
            getSimulationController().runUntilMessage(RunMode.FAST, message);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        setEnabled(getMessage() != null && getSimulationController().isSimulationOK());
    }

}