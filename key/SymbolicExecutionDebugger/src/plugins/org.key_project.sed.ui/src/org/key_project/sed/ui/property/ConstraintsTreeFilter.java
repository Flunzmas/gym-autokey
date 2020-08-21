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

package org.key_project.sed.ui.property;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.viewers.IFilter;
import org.key_project.sed.core.model.ISEConstraint;
import org.key_project.sed.core.model.ISENode;
import org.key_project.sed.core.model.ISEGroupable;
import org.key_project.util.java.ArrayUtil;

/**
 * {@link IFilter} implementation used to check if
 * {@link ISEGroupable#getGroupEndConditions()} is not empty.
 * @author Martin Hentschel
 */
public class ConstraintsTreeFilter extends SENodeTreeFilter {
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean select(Object toTest) {
      try {
         ISENode node = AbstractSENodePropertySection.getDebugNode(toTest);
         if (node != null) {
            ISEConstraint[] constraints = node.getConstraints();
            return !ArrayUtil.isEmpty(constraints);
         }
         else {
            return false;
         }
      }
      catch (DebugException e) {
         return false;
      }
   }
}