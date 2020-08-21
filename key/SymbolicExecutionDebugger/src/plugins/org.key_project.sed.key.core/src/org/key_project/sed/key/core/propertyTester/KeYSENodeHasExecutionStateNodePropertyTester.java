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

package org.key_project.sed.key.core.propertyTester;

import org.eclipse.core.expressions.PropertyTester;
import org.key_project.sed.key.core.model.IKeYSENode;
import org.key_project.util.java.ArrayUtil;

import de.uka.ilkd.key.symbolic_execution.model.IExecutionNode;

/**
 * This property tester can be used to make sure that an {@link IKeYSENode} 
 * contains an {@link IExecutionNode}. 
 * @author Martin Hentschel
 */
public class KeYSENodeHasExecutionStateNodePropertyTester extends PropertyTester {
   /**
    * Argument to verify also that the {@link IKeYSENode} is not disposed.
    */
   public static final String NOT_DISPOSED_ARGUMENT = "notDisposed";

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean test(Object receiver, 
                       String property, 
                       Object[] args, 
                       Object expectedValue) {
      if (receiver instanceof IKeYSENode<?>) {
         IExecutionNode<?> node = ((IKeYSENode<?>)receiver).getExecutionNode();
         if (ArrayUtil.contains(args, NOT_DISPOSED_ARGUMENT)) {
            return node instanceof IExecutionNode<?> && !node.isDisposed();
         }
         else {
            return node instanceof IExecutionNode<?>;
         }
      }
      else {
         return false;
      }
   }
}