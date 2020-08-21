package org.key_project.machlearning;

import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.ui.AbstractMediatorUserInterfaceControl;
import de.uka.ilkd.key.ui.ConsoleUserInterfaceControl;
import org.json.simple.JSONObject;

public interface Tactic {
    void apply(AbstractMediatorUserInterfaceControl ui, Proof proof, Goal goal, JSONObject command) throws Exception;
}
