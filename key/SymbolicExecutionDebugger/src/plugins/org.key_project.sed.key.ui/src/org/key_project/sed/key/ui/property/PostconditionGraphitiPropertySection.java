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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.key_project.sed.core.model.ISENode;
import org.key_project.sed.key.core.model.IKeYTerminationNode;

/**
 * {@link ISection} implementation to show the properties of {@link ISENode}s.
 * @author Martin Hentschel
 */
public class PostconditionGraphitiPropertySection extends AbstractTruthValueGraphitiPropertySection {
   /**
    * {@inheritDoc}
    */
   @Override
   public IKeYTerminationNode<?> getDebugNode(PictogramElement pe) {
      IKeYTerminationNode<?> node = null;
      if (pe != null) {
         IDiagramTypeProvider diagramProvider = getDiagramTypeProvider();
         if (diagramProvider != null) {
            IFeatureProvider featureProvider = diagramProvider.getFeatureProvider();
            if (featureProvider != null) {
               Object bo = diagramProvider.getFeatureProvider().getBusinessObjectForPictogramElement(pe);
               node = PostconditionPropertySection.getDebugNode(bo);
            }
         }
      }
      return node;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected AbstractTruthValueComposite createContentComposite(Composite parent) {
      return new PostconditionComposite(parent, getWidgetFactory(), this);
   }
}