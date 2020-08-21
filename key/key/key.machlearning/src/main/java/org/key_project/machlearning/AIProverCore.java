package org.key_project.machlearning;

import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.proof.ProofTreeListener;
import de.uka.ilkd.key.prover.GoalChooser;
import de.uka.ilkd.key.prover.impl.AbstractProverCore;
import de.uka.ilkd.key.prover.impl.ApplyStrategyInfo;
import de.uka.ilkd.key.settings.ProofSettings;
import de.uka.ilkd.key.settings.StrategySettings;
import de.uka.ilkd.key.strategy.StrategyProperties;
import de.uka.ilkd.key.util.Debug;
import org.key_project.util.collection.ImmutableList;
import org.key_project.util.collection.ImmutableSLList;

public class AIProverCore extends AbstractProverCore {

    private boolean cancelled = false;

    private PythonConnection pythonConnection = null;
    private final GoalChooser goalChooser;

    public AIProverCore(GoalChooser goalChooser) {
        this.goalChooser = goalChooser;
    }

    @Override
    public ApplyStrategyInfo start(Proof proof, Goal goal) {
        return start(proof, ImmutableSLList.<Goal>nil().prepend(goal));
    }

    @Override
    public ApplyStrategyInfo start(Proof proof, ImmutableList<Goal> goals) {
        ProofSettings settings = proof.getSettings();
        StrategySettings stratSet = settings.getStrategySettings();
        return start(proof, goals, stratSet);
    }

    @Override
    public ApplyStrategyInfo start(Proof proof, ImmutableList<Goal> goals, StrategySettings stratSet) {

        int maxSteps = stratSet.getMaxSteps();
        long timeout = stratSet.getTimeout();

        boolean stopAtFirstNonCloseableGoal =
                proof.getSettings().getStrategySettings()
                        .getActiveStrategyProperties().getProperty(
                        StrategyProperties.STOPMODE_OPTIONS_KEY)
                        .equals(StrategyProperties.STOPMODE_NONCLOSE);
        return start(proof, goals, maxSteps, timeout, stopAtFirstNonCloseableGoal);
    }

    @Override
    public ApplyStrategyInfo start(Proof proof, ImmutableList<Goal> goals, int maxSteps, long timeout, boolean stopAtFirstNonCloseableGoal) {
        assert proof != null;

        ApplyStrategyInfo result = executeStrategy(proof);
        return result;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean hasBeenInterrupted() {
        return false;
    }

    private synchronized ApplyStrategyInfo executeStrategy(Proof proof) {

        long time = System.currentTimeMillis();
        try {
            if (pythonConnection == null) {
                pythonConnection = new PythonConnection();
                pythonConnection.connect();
            }

            ImmutableList<Goal> goals = proof.openGoals();
            while (!goals.isEmpty()) {
                Goal goal = goals.head();
                Tactic tactic = pythonConnection.queryTactic(goal);
                tactic.apply(null, proof, goal, null);
            }

            return new ApplyStrategyInfo(
                    "Execution finished",
                    proof, null, (Goal) null, System.currentTimeMillis()-time,
                    countApplied, 0);

        } catch(InterruptedException iex) {
            this.cancelled = true;
            return new ApplyStrategyInfo("Interrupted.", proof, null, null,
                    System.currentTimeMillis()-time, countApplied, 0);

        } catch(Exception ex) {
            ex.printStackTrace();
            return new ApplyStrategyInfo("Error.", proof, ex,
                    null, System.currentTimeMillis() - time,
                    countApplied, 0);

        } finally {
            time = System.currentTimeMillis()-time;
            System.out.println("Strategy stopped.");
            System.out.println("Applied " + countApplied);
            System.out.println("Time elapsed: " + time);
        }
    }
}
