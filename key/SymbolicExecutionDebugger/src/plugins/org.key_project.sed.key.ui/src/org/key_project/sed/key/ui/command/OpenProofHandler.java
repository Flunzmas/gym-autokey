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

package org.key_project.sed.key.ui.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.key_project.key4eclipse.common.ui.util.StarterUtil;
import org.key_project.sed.key.core.model.KeYDebugTarget;
import org.key_project.sed.key.ui.util.LogUtil;
import org.key_project.util.eclipse.swt.SWTUtil;

import de.uka.ilkd.key.control.DefaultUserInterfaceControl;
import de.uka.ilkd.key.proof.Proof;

/**
 * This {@link IHandler} opens a save as dialog to save the {@link Proof}
 * of the currently selected {@link KeYDebugTarget}s as *.proof file.
 * @author Martin Hentschel
 */
public class OpenProofHandler extends AbstractHandler {
   /**
    * {@inheritDoc}
    */
   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException {
      try {
         ISelection selection = HandlerUtil.getCurrentSelection(event);
         Object[] elements = SWTUtil.toArray(selection);
         for (Object element : elements) {
            // Find proof
            if (element instanceof ILaunch) {
               element = ((ILaunch)element).getDebugTarget();
            }
            if (element instanceof IDebugElement) {
               IDebugTarget target = ((IDebugElement)element).getDebugTarget();
               if (target instanceof KeYDebugTarget) {
                  KeYDebugTarget keyTarget = (KeYDebugTarget)target;
                  if (keyTarget.getEnvironment().getUi() instanceof DefaultUserInterfaceControl) {
                     StarterUtil.openProofStarter(HandlerUtil.getActiveShell(event), 
                                                  keyTarget.getProof(), 
                                                  keyTarget.getEnvironment(), 
                                                  keyTarget.getMethod(),
                                                  true,
                                                  true,
                                                  true,
                                                  false);
                  }
               }
            }
         }
      }
      catch (Exception e) {
         LogUtil.getLogger().logError(e);
         LogUtil.getLogger().openErrorDialog(HandlerUtil.getActiveShell(event), e);
      }
      return null;
   }
}