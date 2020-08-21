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
import org.key_project.key4eclipse.common.ui.starter.IMethodStarter;
import org.key_project.util.collection.ImmutableList;
import org.key_project.util.collection.ImmutableSLList;

/**
 * {@link IMethodStarter} which logs the calls of {@link #open(IMethod)}.
 * @author Martin Hentschel
 */
public class SecondLoggingMethodStarter implements IMethodStarter, ITestedStarter {
   /**
    * The unique ID of this starter.
    */
   public static final String ID = "org.key_project.key4eclipse.common.ui.test.starter.SecondLoggingMethodStarterID";

   /**
    * The unique Name of this starter.
    */
   public static final String NAME = "Second Method Starter";

   /**
    * The description of this starter.
    */
   public static final String DESCRIPTION = "Description of Second Method Starter";
   
   /**
    * The logged calls.
    */
   private ImmutableList<IMethod> log = ImmutableSLList.nil();

   /**
    * {@inheritDoc}
    */
   @Override
   public void open(IMethod method) throws Exception {
      log = log.append(method);
   }
   
   /**
    * Returns the logged calls and clears it.
    * @return The logged calls.
    */
   public ImmutableList<IMethod> getAndResetLog() {
      ImmutableList<IMethod> result = log;
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