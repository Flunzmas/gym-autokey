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

package org.key_project.sed.key.core.model;

import org.eclipse.debug.core.DebugException;
import org.key_project.sed.core.model.ISENode;

import de.uka.ilkd.key.symbolic_execution.model.IExecutionNode;

/**
 * Provides methods each {@link ISENode} of the symbolic execution debugger (SED)
 * based on KeY must have.
 * @author Martin Hentschel
 */
public interface IKeYSENode<E extends IExecutionNode<?>> extends ISENode {
   /**
    * Returns the represented {@link IExecutionNode}.
    * @return The reprsented {@link IExecutionNode}.
    */
   public E getExecutionNode();
   
   /**
    * {@inheritDoc}
    */
   @Override
   public KeYDebugTarget getDebugTarget();
   
   /**
    * {@inheritDoc}
    */
   @Override
   public KeYThread getThread();
   
   /**
    * {@inheritDoc}
    */
   @Override
   public IKeYSENode<?> getParent() throws DebugException;
   
   /**
    * {@inheritDoc}
    */
   @Override
   public IKeYSENode<?>[] getChildren() throws DebugException;
   
   /**
    * {@inheritDoc}
    */
   @Override
   public KeYConstraint[] getConstraints() throws DebugException;

   /**
    * It is valid to set the parent as long it was not defined before.
    * So a parent might be set lazily later but can never be changed.
    * @param parent The new parent to set.
    */
   public void setParent(ISENode parent);
   
   /**
    * Checks if truth value tracing is enabled or not.
    * @return {@code true} enabled, {@code false} disabled.
    */
   public boolean isTruthValueTracingEnabled();

   /**
    * Adds the outgoing link.
    * @param link The outgoing link to add.
    */
   public void addOutgoingLink(KeYNodeLink link);

   /**
    * Removes the outgoing link.
    * @param link The outgoing link to remove.
    */
   public void removeOutgoingLink(KeYNodeLink link);
   
   /**
    * {@inheritDoc}
    */
   @Override
   public KeYNodeLink[] getOutgoingLinks() throws DebugException;

   /**
    * Adds the incoming link.
    * @param link The incoming link to add.
    */
   public void addIncomingLink(KeYNodeLink link);

   /**
    * Removes the incoming link.
    * @param link The incoming link to remove.
    */
   public void removeIncomingLink(KeYNodeLink link);

   /**
    * {@inheritDoc}
    */
   @Override
   public KeYNodeLink[] getIncomingLinks() throws DebugException;
}