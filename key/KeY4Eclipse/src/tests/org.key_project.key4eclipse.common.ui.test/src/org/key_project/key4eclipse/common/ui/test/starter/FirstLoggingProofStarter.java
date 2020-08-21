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

package org.key_project.key4eclipse.common.ui.test.starter;

import org.eclipse.jdt.core.IMethod;
import org.key_project.key4eclipse.common.ui.starter.IProofStarter;
import org.key_project.util.collection.ImmutableList;
import org.key_project.util.collection.ImmutableSLList;

import de.uka.ilkd.key.control.KeYEnvironment;
import de.uka.ilkd.key.proof.Proof;

/**
 * {@link IProofStarter} which logs the calls of {@link #open(Proof, KeYEnvironment, IMethod)}.
 * @author Martin Hentschel
 */
public class FirstLoggingProofStarter implements IProofStarter, ITestedStarter {
   /**
    * The unique ID of this starter.
    */
   public static final String ID = "org.key_project.key4eclipse.common.ui.test.starter.FirstLoggingProofStarterID";

   /**
    * The unique Name of this starter.
    */
   public static final String NAME = "First Proof Starter";

   /**
    * The description of this starter.
    */
   public static final String DESCRIPTION = "Description of First Proof Starter";

   /**
    * The logged calls.
    */
   private ImmutableList<Proof> log = ImmutableSLList.nil();

   /**
    * {@inheritDoc}
    */
   @Override
   public void open(Proof proof, 
                    KeYEnvironment<?> environment, 
                    IMethod method,
                    boolean canStartAutomode,
                    boolean canApplyRules,
                    boolean canPruneProof,
                    boolean canStartSMTSolver) throws Exception {
      log = log.append(proof);
   }
   
   /**
    * Returns the logged calls and clears it.
    * @return The logged calls.
    */
   public ImmutableList<Proof> getAndResetLog() {
      ImmutableList<Proof> result = log;
      log = ImmutableSLList.nil();
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getId() {
      return ID;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getName() {
      return NAME;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getDescription() {
      return DESCRIPTION;
   }
}