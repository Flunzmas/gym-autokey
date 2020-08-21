package org.key_project.machlearning;

import de.uka.ilkd.key.control.UserInterfaceControl;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.macros.AbstractProofMacro;
import de.uka.ilkd.key.macros.ProofMacroFinishedInfo;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.prover.GoalChooser;
import de.uka.ilkd.key.prover.ProverTaskListener;
import de.uka.ilkd.key.prover.impl.ApplyStrategyInfo;
import org.key_project.util.collection.ImmutableList;

import java.io.IOException;

public class AIServerMacro extends AbstractProofMacro {

    private PythonConnection pythonConnection = null;

    @Override
    public String getName() {
        return "Apply a learnt strategy (via server)";
    }

    @Override
    public String getCategory() {
        return "AI";
    }

    @Override
    public String getDescription() {
        return "xxx";
    }

    @Override
    public boolean canApplyTo(Proof proof, ImmutableList<Goal> goals, PosInOccurrence posInOcc) {
        return true;
    }

    @Override
    public ProofMacroFinishedInfo applyTo(UserInterfaceControl uic, Proof proof, ImmutableList<Goal> goals, PosInOccurrence posInOcc, ProverTaskListener listener) throws InterruptedException, Exception {
        return executeStrategy(proof);
    }

    private synchronized ProofMacroFinishedInfo executeStrategy(Proof proof) {

        long time = System.currentTimeMillis();
        try {
            if (pythonConnection == null) {
                pythonConnection = new PythonConnection();
                pythonConnection.connect();
            }

            ImmutableList<Goal> goals = proof.openGoals();
            while (!goals.isEmpty()) {
                boolean hit = false;
                while (!goals.isEmpty()) {
                    Goal goal = goals.head();
                    goals = goals.tail();
                    Tactic tactic = pythonConnection.queryTactic(goal);
                    System.out.println("Goal " + goal.node().serialNr() + ": " + tactic);
                    if(tactic != null) {
                        hit = true;
                        tactic.apply(null, proof, goal, null);
                    }
                }
                if (!hit) {
                    break;
                }
                goals = proof.openGoals();
            }

            return new ProofMacroFinishedInfo(this, proof);

        } catch(InterruptedException iex) {
            return new ProofMacroFinishedInfo(this, proof, true);

        } catch(Exception ex) {
            if(pythonConnection != null) {
                try {
                    pythonConnection.close();
                    pythonConnection = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ex.printStackTrace();
            return new ProofMacroFinishedInfo(this, proof, true);

        } finally {
            if(pythonConnection != null) {
                try {
                    pythonConnection.close();
                    pythonConnection = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            time = System.currentTimeMillis()-time;
            System.out.println("Strategy stopped.");
            System.out.println("Time elapsed: " + time);
        }
    }
}
