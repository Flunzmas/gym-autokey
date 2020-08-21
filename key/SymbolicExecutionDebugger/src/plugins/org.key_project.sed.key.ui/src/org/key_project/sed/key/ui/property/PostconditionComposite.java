/*******************************************************************************
 * Copyright (c) 2014 Karlsruhe Institute of Technology, Germany
 *                    Technical University Darmstadt, Germany
 *                    Chalmers University of Technology, Sweden
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Technical University Darmstadt - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.key_project.sed.key.ui.property;

import java.util.Set;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.key_project.sed.key.core.model.IKeYSENode;
import org.key_project.util.collection.ImmutableList;

import de.uka.ilkd.key.logic.PosInTerm;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.proof.init.AbstractOperationPO;
import de.uka.ilkd.key.symbolic_execution.model.IExecutionNode;
import de.uka.ilkd.key.util.Triple;

/**
 * This composite provides the content shown in {@link PostconditionPropertySection}
 * and {@link PostconditionGraphitiPropertySection}.
 * @author Martin Hentschel
 */
public class PostconditionComposite extends AbstractTruthValueComposite {
   /**
    * Constructor.
    * @param parent The parent {@link Composite}.
    * @param factory The {@link TabbedPropertySheetWidgetFactory} to use.
    * @param layoutListener An optional {@link ILayoutListener} invoked when the shown content has changed.
    */
   public PostconditionComposite(Composite parent, TabbedPropertySheetWidgetFactory factory, ILayoutListener layoutListener) {
      super(parent, factory, layoutListener);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Triple<Term, PosInTerm, Term> computeTermToShow(IKeYSENode<?> node,
                                                             IExecutionNode<?> executionNode, 
                                                             Node keyNode) {
      Term term = keyNode.getAppliedRuleApp().posInOccurrence().subTerm();
      if (term.op() instanceof Modality) {
         term = term.sub(0);
      }
      Term uninterpretedPredicate = AbstractOperationPO.getUninterpretedPredicate(executionNode.getProof());
      Term sfTerm = keyNode.getAppliedRuleApp().posInOccurrence().sequentFormula().formula();
      ImmutableList<Term> updates = TermBuilder.goBelowUpdates2(sfTerm).first;
      if (uninterpretedPredicate != null) {
         Set<Term> additionalPredicates = AbstractOperationPO.getAdditionalUninterpretedPredicates(executionNode.getProof());
         PosInTerm predicatePosition = findUninterpretedPredicateTerm(node, term, uninterpretedPredicate, additionalPredicates);
         Term termWithoutPredicate = predicatePosition != null ? 
                                     removeUninterpretedPredicate(keyNode, term, predicatePosition.getSubTerm(term)) :
                                     term;
         return new Triple<Term, PosInTerm, Term>(INCLUDE_UPDATES ? keyNode.proof().getServices().getTermBuilder().applySequential(updates, termWithoutPredicate) : termWithoutPredicate, 
                                                  predicatePosition,
                                                  term);
      }
      else {
         return new Triple<Term, PosInTerm, Term>(INCLUDE_UPDATES ? keyNode.proof().getServices().getTermBuilder().applySequential(updates, term) : term, 
                                                  null,
                                                  null);
      }
   }
}