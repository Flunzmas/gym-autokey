package org.key_project.machlearning;

import de.uka.ilkd.key.control.UserInterfaceControl;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.macros.AbstractProofMacro;
import de.uka.ilkd.key.macros.ProofMacroFinishedInfo;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.prover.ProverTaskListener;
import de.uka.ilkd.key.ui.AbstractMediatorUserInterfaceControl;
import org.key_project.util.collection.ImmutableList;

import javax.swing.JOptionPane;

public class AITacticMacro extends AbstractProofMacro {
    @Override
    public String getName() {
        return "Apply a tactic";
    }

    @Override
    public String getCategory() {
        return "AI";
    }

    @Override
    public String getDescription() {
        return "XXX";
    }

    @Override
    public boolean canApplyTo(Proof proof, ImmutableList<Goal> goals, PosInOccurrence posInOcc) {
        return true;
    }

    @Override
    public ProofMacroFinishedInfo applyTo(UserInterfaceControl uic, Proof proof, ImmutableList<Goal> goals, PosInOccurrence posInOcc, ProverTaskListener listener) throws InterruptedException, Exception {

        String[] tactics = KeYConnection.TACTICS.keySet().toArray(new String[KeYConnection.TACTICS.size()]);
        int no = JOptionPane.showOptionDialog(null, "Choose tactic", "Tactic",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, tactics, tactics[0]);

        if (no < 0) {
            return new ProofMacroFinishedInfo(this, proof, true);
        }

        String tacticName = tactics[no];
        Tactic tactic = KeYConnection.TACTICS.get(tacticName);

        Goal goal = goals.head();
        tactic.apply((AbstractMediatorUserInterfaceControl) uic, proof, goal, null);

        return new ProofMacroFinishedInfo(this, goal);
    }
}
