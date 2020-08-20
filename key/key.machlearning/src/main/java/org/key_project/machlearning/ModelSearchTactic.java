package org.key_project.machlearning;

import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.macros.FilterStrategy;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.rule.RuleApp;
import de.uka.ilkd.key.strategy.RuleAppCost;
import de.uka.ilkd.key.strategy.RuleAppCostCollector;
import de.uka.ilkd.key.strategy.Strategy;
import de.uka.ilkd.key.strategy.StrategyProperties;
import de.uka.ilkd.key.strategy.TopRuleAppCost;

public class ModelSearchTactic extends AutoTactic {
    private static final Name NAME = new Name("Limited Branches for ModelSearch");
    private static final int THRESHOLD = 8;

    public ModelSearchTactic() {
        super("[StrategyProperty]NON_LIN_ARITH_OPTIONS_KEY=NON_LIN_ARITH_COMPLETION\n" +
                        "[Strategy]MaximumNumberOfAutomaticApplications=1000");
    }

    @Override
    protected Strategy makeStrategy(Proof proof, StrategyProperties strategyProperties) {
        return new LimitedBranchStrategy(super.makeStrategy(proof, strategyProperties), proof.openGoals().size());
    }

    private class LimitedBranchStrategy implements Strategy {

        private final int initialSize;

        private final Strategy delegate;

        public LimitedBranchStrategy(Strategy delegate, int initialSize) {
            this.delegate = delegate;
            this.initialSize = initialSize;
        }

        @Override
        public RuleAppCost computeCost(RuleApp app, PosInOccurrence pio, Goal goal) {
            return delegate.computeCost(app, pio, goal);
        }

        @Override
        public void instantiateApp(RuleApp app, PosInOccurrence pio, Goal goal,
                                   RuleAppCostCollector collector) {
            delegate.instantiateApp(app, pio, goal, collector);
        }

        @Override
        public boolean isStopAtFirstNonCloseableGoal() {
            return delegate.isStopAtFirstNonCloseableGoal();
        }

        @Override
        public Name name() {
            return NAME;
        }

        @Override
        public boolean isApprovedApp(RuleApp app, PosInOccurrence pio, Goal goal) {
            int currentSize = goal.proof().openGoals().size();
            if (currentSize > initialSize + THRESHOLD) {
                return false;
            }
            return delegate.isApprovedApp(app, pio, goal);
        }
    }
}
