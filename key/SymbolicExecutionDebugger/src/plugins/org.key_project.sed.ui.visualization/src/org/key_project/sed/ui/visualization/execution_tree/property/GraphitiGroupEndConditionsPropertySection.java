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

package org.key_project.sed.ui.visualization.execution_tree.property;

import org.eclipse.ui.views.properties.tabbed.ISection;
import org.key_project.sed.core.model.ISEGroupable;
import org.key_project.sed.ui.property.GroupEndConditionsTabComposite;
import org.key_project.sed.ui.property.ISENodeTabContent;

/**
 * {@link ISection} implementation to show the group ending conditions of an {@link ISEGroupable}
 * which is provided via {@link ISEGroupable#getGroupEndConditions()}.
 * @author Martin Hentschel
 */
public class GraphitiGroupEndConditionsPropertySection extends AbstractGraphitiDebugNodePropertySection {
   /**
    * {@inheritDoc}
    */
   @Override
   protected ISENodeTabContent createContent() {
      return new GroupEndConditionsTabComposite();
   }
}