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

package org.key_project.sed.core.model.impl;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.key_project.sed.core.model.ISEDebugTarget;
import org.key_project.sed.core.model.ISEVariable;
import org.key_project.sed.core.provider.SEVariableContentProvider;
import org.key_project.sed.core.util.LogUtil;

/**
 * Provides a basic implementation of {@link ISEVariable}.
 * @author Martin Hentschel
 * @see ISEVariable
 */
@SuppressWarnings("restriction")
public abstract class AbstractSEVariable extends AbstractSEDebugElement implements ISEVariable {
   /**
    * The parent {@link IStackFrame} in which this {@link ISEVariable} is shown.
    */
   private final IStackFrame stackFrame; 

   /**
    * Constructor.
    * @param target The {@link ISEDebugTarget} in that this element is contained.
    * @param stackFrame The parent {@link IStackFrame} in which this {@link ISEVariable} is shown.
    */
   public AbstractSEVariable(ISEDebugTarget target, IStackFrame stackFrame) {
      super(target);
      Assert.isNotNull(stackFrame);
      this.stackFrame = stackFrame;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean supportsValueModification() {
      return false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean verifyValue(IValue value) throws DebugException {
      return false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setValue(String expression) throws DebugException {
      throw new DebugException(LogUtil.getLogger().createErrorStatus("Value modification is not supported."));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setValue(IValue value) throws DebugException {
      throw new DebugException(LogUtil.getLogger().createErrorStatus("Value modification is not supported."));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean verifyValue(String expression) throws DebugException {
      return false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public IStackFrame getStackFrame() {
      return stackFrame;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("rawtypes")
   @Override
   public Object getAdapter(Class adapter) {
      if (IElementContentProvider.class.equals(adapter)) {
         return new SEVariableContentProvider();
      }
      else {
         return super.getAdapter(adapter);
      }
   }
}