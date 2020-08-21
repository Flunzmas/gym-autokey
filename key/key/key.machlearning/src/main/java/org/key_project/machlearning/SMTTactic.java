package org.key_project.machlearning;

import de.uka.ilkd.key.gui.smt.SolverListener;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.rule.IBuiltInRuleApp;
import de.uka.ilkd.key.settings.ProofIndependentSettings;
import de.uka.ilkd.key.settings.SMTSettings;
import de.uka.ilkd.key.smt.RuleAppSMT;
import de.uka.ilkd.key.smt.SMTProblem;
import de.uka.ilkd.key.smt.SMTSolver;
import de.uka.ilkd.key.smt.SMTSolverResult.ThreeValuedTruth;
import de.uka.ilkd.key.smt.SolverLauncher;
import de.uka.ilkd.key.smt.SolverLauncherListener;
import de.uka.ilkd.key.smt.SolverType;
import de.uka.ilkd.key.ui.AbstractMediatorUserInterfaceControl;
import de.uka.ilkd.key.ui.ConsoleUserInterfaceControl;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SMTTactic implements Tactic {

    private List<SolverType> SOLVERS = Arrays.asList(SolverType.Z3_SOLVER);

    @Override
    public void apply(AbstractMediatorUserInterfaceControl ui, Proof proof, Goal goal, JSONObject command) throws Exception {

        SMTSettings settings = new SMTSettings(goal.proof().getSettings().getSMTSettings(),
                ProofIndependentSettings.DEFAULT_INSTANCE.getSMTSettings(), goal.proof());
        SolverLauncher launcher = new SolverLauncher(settings);
        launcher.addListener(new Listener());
        Collection<SMTProblem> list = new LinkedList<SMTProblem>();
        list.add(new SMTProblem(goal));
        launcher.launch(SOLVERS, list, goal.proof().getServices());
    }

    private class Listener implements SolverLauncherListener {
        @Override
        public void launcherStopped(SolverLauncher launcher, Collection<SMTSolver> finishedSolvers) {
            for (SMTSolver solver : finishedSolvers) {
                if (solver.getFinalResult().isValid() == ThreeValuedTruth.VALID) {
                    IBuiltInRuleApp app =
                            RuleAppSMT.rule.createApp( null ).
                                    setTitle( "Z3" );
                    solver.getProblem().getGoal().apply(app);
                    System.err.println(" ... Z3 closes goal " + solver.getProblem().getGoal().node().serialNr());
                } else {
                    System.err.println(" ... Z3 doesn't close goal " + solver.getProblem().getGoal().node().serialNr());
                }
            }
        }

        @Override
        public void launcherStarted(Collection<SMTProblem> problems, Collection<SolverType> solverTypes, SolverLauncher launcher) {
            System.err.println(" ... Z3 started");
        }
    }
}
