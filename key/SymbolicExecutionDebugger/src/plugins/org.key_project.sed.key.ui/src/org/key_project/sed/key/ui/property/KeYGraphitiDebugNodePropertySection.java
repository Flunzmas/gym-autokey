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

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.platform.IDiagramContainer;
import org.eclipse.graphiti.ui.platform.GFPropertySection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.key_project.sed.core.model.ISENode;
import org.key_project.sed.key.core.model.IKeYSENode;

/**
 * {@link ISection} implementation to show the properties of {@link ISENode}s.
 * @author Martin Hentschel
 */
public class KeYGraphitiDebugNodePropertySection extends GFPropertySection {
   /**
    * The shown content.
    */
   private KeYTabComposite contentComposite;
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
      super.createControls(parent, tabbedPropertySheetPage);
      contentComposite = new KeYTabComposite(parent, SWT.NONE, getWidgetFactory());
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void dispose() {
      if (contentComposite != null) {
         contentComposite.dispose();
      }
      super.dispose();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void refresh() {
      contentComposite.updateContent(getDebugNode());
   }
   
   /**
    * Returns the {@link ISENode} to show.
    * @return The {@link ISENode} to show or {@code null} if no one should be shown.
    */
   public IKeYSENode<?> getDebugNode() {
      return getDebugNode(getSelectedPictogramElement());
   }
   
   /**
    * Returns the {@link ISENode} to show.
    * @param pe The currently selected {@link PictogramElement}.
    * @return The {@link ISENode} to show or {@code null} if no one should be shown.
    */
   public IKeYSENode<?> getDebugNode(PictogramElement pe) {
      IKeYSENode<?> node = null;
      if (pe != null) {
         IDiagramTypeProvider diagramProvider = getDiagramTypeProvider();
         if (diagramProvider != null) {
            IFeatureProvider featureProvider = diagramProvider.getFeatureProvider();
            if (featureProvider != null) {
               Object bo = diagramProvider.getFeatureProvider().getBusinessObjectForPictogramElement(pe);
               node = KeYDebugNodePropertySection.getDebugNode(bo);
            }
         }
      }
      return node;
   }

   /**
    * <p>
    * {@inheritDoc}
    * </p>
    * <p>
    * Changed visibility to public.
    * </p>
    */
   @Override
   public IDiagramContainer getDiagramContainer() {
      IDiagramContainer container = super.getDiagramContainer();
      if (container == null) {
         IWorkbenchPart part = getPart();
         if (part != null) {
            IEditorPart editPart = (IEditorPart)part.getAdapter(IEditorPart.class);
            if (editPart instanceof IDiagramContainer) {
               container = (IDiagramContainer)editPart;
            }
         }
      }
      return container;
   }
}