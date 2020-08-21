package org.key_project.sed.core.provider;

import org.eclipse.debug.internal.ui.elements.adapters.VariableColumnFactoryAdapter;
import org.eclipse.debug.internal.ui.elements.adapters.VariableColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.key_project.sed.core.util.ISEConstants;

/**
 * Extended {@link VariableColumnFactoryAdapter} to support {@link ISEConstants#ID_CALL_STATE}.
 * @author Martin Hentschel
 */
@SuppressWarnings("restriction")
public class SEBaseMethodReturnColumnPresentationFactory extends VariableColumnFactoryAdapter {
   /**
    * {@inheritDoc}
    */
   @Override
   public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
      if (ISEConstants.ID_CALL_STATE.equals(context.getId())) {
         return new VariableColumnPresentation();
      }
      else {
         return super.createColumnPresentation(context, element);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getColumnPresentationId(IPresentationContext context, Object element) {
      if (ISEConstants.ID_CALL_STATE.equals(context.getId())) {
         return IDebugUIConstants.COLUMN_PRESENTATION_ID_VARIABLE;
      }
      else {
         return super.getColumnPresentationId(context, element);
      }
   }
}
