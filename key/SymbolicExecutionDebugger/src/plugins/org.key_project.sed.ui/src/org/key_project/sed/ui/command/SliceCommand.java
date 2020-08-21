package org.key_project.sed.ui.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.key_project.sed.core.model.ISENode;
import org.key_project.sed.core.model.ISEDebugTarget;
import org.key_project.sed.core.model.ISEVariable;
import org.key_project.sed.core.slicing.ISESlicer;
import org.key_project.sed.ui.wizard.SliceWizard;
import org.key_project.util.eclipse.swt.SWTUtil;
import org.key_project.util.java.ArrayUtil;

/**
 * Opens a dialog to select an available {@link ISESlicer} and performs the slicing.
 * @author Martin Hentschel
 */
public class SliceCommand extends AbstractHandler {
   /**
    * {@inheritDoc}
    */
   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException {
      ISelection selection = HandlerUtil.getCurrentSelection(event);
      Object[] elements = SWTUtil.toArray(selection);
      for (Object element : elements) {
         if (element instanceof ISEVariable) {
            ISEVariable seedVariable = (ISEVariable) element;
            ISEDebugTarget target = seedVariable.getDebugTarget();
            if (target != null && seedVariable.getStackFrame() instanceof ISENode) {
               ISENode seedNode = (ISENode) seedVariable.getStackFrame();
               ISESlicer[] slicer = target.getSlicer(seedNode, seedVariable);
               if (!ArrayUtil.isEmpty(slicer)) {
                  SliceWizard.openWizard(HandlerUtil.getActiveShell(event), seedNode, seedVariable, slicer);
               }
            }
         }
      }
      return null;
   }
}
