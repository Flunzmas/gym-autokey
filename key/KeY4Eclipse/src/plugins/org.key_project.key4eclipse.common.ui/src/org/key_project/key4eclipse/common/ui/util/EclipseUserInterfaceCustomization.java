package org.key_project.key4eclipse.common.ui.util;

import java.util.LinkedList;
import java.util.List;

import org.key_project.key4eclipse.common.ui.completion.*;
import org.key_project.key4eclipse.common.ui.wizard.CompleteAndApplyTacletMatchWizard;
import org.key_project.util.eclipse.WorkbenchUtil;

import de.uka.ilkd.key.control.RuleCompletionHandler;
import de.uka.ilkd.key.control.instantiation_model.TacletInstantiationModel;
import de.uka.ilkd.key.gui.InteractiveRuleApplicationCompletion;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.IBuiltInRuleApp;

/**
 * An {@link RuleCompletionHandler} which gets user input directly within Eclipse.
 * @author Martin Hentschel
 */
public final class EclipseUserInterfaceCustomization implements RuleCompletionHandler {
   /**
    * The singleton instance of this class.
    */
   private static final EclipseUserInterfaceCustomization instance = new EclipseUserInterfaceCustomization();

   private final List<InteractiveRuleApplicationCompletion> completions = new LinkedList<InteractiveRuleApplicationCompletion>();
   
   /**
    * Forbid other instances.
    */
   private EclipseUserInterfaceCustomization() {
      completions.add(new FunctionalOperationContractCompletion());
      completions.add(new DependencyContractCompletion());
      completions.add(new LoopInvariantRuleCompletion());
      completions.add(new BlockContractInternalCompletion());
      completions.add(new BlockContractExternalCompletion());

   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void completeAndApplyTacletMatch(TacletInstantiationModel[] models, Goal goal) {
      CompleteAndApplyTacletMatchWizard.openWizard(WorkbenchUtil.getActiveShell(), models, goal);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public IBuiltInRuleApp completeBuiltInRuleApp(IBuiltInRuleApp app, Goal goal, boolean forced) {
      IBuiltInRuleApp result = app;
      for (InteractiveRuleApplicationCompletion compl : completions ) {
          if (compl.canComplete(app)) {
              result = compl.complete(app, goal, forced);
              break;
          }
      }
      return (result != null && result.complete()) ? result : null;
   }

   /**
    * Returns the only instance of this class.
    * @return The only instance of this class.
    */
   public static EclipseUserInterfaceCustomization getInstance() {
      return instance;
   }
}
