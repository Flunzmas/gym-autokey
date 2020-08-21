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

package org.key_project.keyide.ui.property;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.key_project.keyide.ui.editor.KeYEditor;
import org.key_project.keyide.ui.providers.BranchFolder;
import org.key_project.util.eclipse.swt.SWTUtil;
import org.key_project.util.java.ArrayUtil;
import org.key_project.util.java.IFilter;

import de.uka.ilkd.key.core.KeYSelectionEvent;
import de.uka.ilkd.key.core.KeYSelectionListener;
import de.uka.ilkd.key.core.KeYSelectionModel;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.proof.Proof;

/**
 * Provides the basic functionality of {@link AbstractPropertySection}s
 * which shows content of {@link Node}s or its related stuff like the {@link Proof}
 * or the selected {@link Term}.
 * @author Martin Hentschel
 */
public abstract class AbstractNodePropertySection extends AbstractPropertySection {
   /**
    * The observed {@link KeYSelectionModel} which provides the currently selected {@link Node}.
    */
   private KeYSelectionModel selectionModel;
   
   /**
    * The used {@link KeYSelectionListener} to observe {@link #selectionModel}.
    */
   private final KeYSelectionListener selectionListener = new KeYSelectionListener() {
      @Override
      public void selectedProofChanged(KeYSelectionEvent e) {
         updateShownContentThreadSave();
      }
      
      @Override
      public void selectedNodeChanged(KeYSelectionEvent e) {
         updateShownContentThreadSave();
      }
   };
   
   /**
    * In case {@link #selectionModel} is {@code null}, this {@link ISelectionProvider} is used instead.
    */
   private ISelectionProvider selectionProvider;

   /**
    * The used {@link ISelectionProvider} to observe {@link #selectionProvider}.
    */
   private final ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
         updateShownContentThreadSave();
      }
   };

   /**
    * Adds a row to the form.
    * @param factory The {@link TabbedPropertySheetWidgetFactory} to use.
    * @param parent The parent {@link Composite}.
    * @param previous An optional previous {@link Composite} of the previous row.
    * @param button The {@link Button} to add.
    * @param label The label to use.
    */
   protected void addControlRow(TabbedPropertySheetWidgetFactory factory, Composite parent, Control previous, Button button, String label) {
      button.setEnabled(false);
      addControlRow(factory, parent, previous, (Control)button, label);
   }

   /**
    * Adds a row to the form.
    * @param factory The {@link TabbedPropertySheetWidgetFactory} to use.
    * @param parent The parent {@link Composite}.
    * @param previous An optional previous {@link Composite} of the previous row.
    * @param text The {@link Text} to add.
    * @param label The label to use.
    */
   protected void addControlRow(TabbedPropertySheetWidgetFactory factory, Composite parent, Control previous, Text text, String label) {
      text.setEditable(false);
      addControlRow(factory, parent, previous, (Control)text, label);
   }

   /**
    * Adds a row to the form.
    * @param factory The {@link TabbedPropertySheetWidgetFactory} to use.
    * @param parent The parent {@link Composite}.
    * @param previous An optional previous {@link Composite} of the previous row.
    * @param control The {@link Control} to add.
    * @param label The label to use.
    */
   protected void addControlRow(TabbedPropertySheetWidgetFactory factory, Composite parent, Control previous, Control control, String label) {
      FormData data = new FormData();
      data.left = new FormAttachment(0, AbstractPropertySection.STANDARD_LABEL_WIDTH * 2);
      data.right = new FormAttachment(100, 0);
      if (previous != null) {
         data.top = new FormAttachment(previous, ITabbedPropertyConstants.VSPACE);
      }
      else {
         data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
      }
      control.setLayoutData(data);

      CLabel nodeLabel = factory.createCLabel(parent, label);
      data = new FormData();
      data.left = new FormAttachment(0, 0);
      data.right = new FormAttachment(control, -ITabbedPropertyConstants.HSPACE);
      data.top = new FormAttachment(control, 0, SWT.TOP);
      nodeLabel.setLayoutData(data);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setInput(IWorkbenchPart part, ISelection selection) {
      super.setInput(part, selection);
      if (selectionModel != null) {
         selectionModel.removeKeYSelectionListener(selectionListener);
         selectionModel = null;
      }
      if (selectionProvider != null) {
         selectionProvider.removeSelectionChangedListener(selectionChangedListener);
         selectionProvider = null;
      }
      part = updatePart(part);
      if (part instanceof KeYEditor) {
         selectionModel = ((KeYEditor) part).getSelectionModel();
         if (selectionModel != null) {
            selectionModel.addKeYSelectionListener(selectionListener);
         }
      }
      else {
         selectionProvider = part.getSite().getSelectionProvider();
         selectionProvider.addSelectionChangedListener(selectionChangedListener);
      }
      updateShownContent(getSelectedNode());
   }
   
   /**
    * Returns the {@link IWorkbenchPart} to handle.
    * @param part The active {@link IWorkbenchPart}.
    * @return The {@link IWorkbenchPart} to handle.
    */
   protected IWorkbenchPart updatePart(IWorkbenchPart part) {
      IContributedContentsView view = (IContributedContentsView)part.getAdapter(IContributedContentsView.class);
      if (view != null) {
         part = view.getContributingPart(); // In case of outline page the editor is returned.
      }
      return part;
   }

   /**
    * Updates the shown content {@link Thread} save.
    */
   protected void updateShownContentThreadSave() {
      Display.getDefault().asyncExec(new Runnable() {
         @Override
         public void run() {
            updateShownContent(getSelectedNode());
         }
      });
   }
   
   /**
    * Returns the selected {@link Node}.
    * @return The selected {@link Node}.
    */
   protected Node getSelectedNode() {
      if (selectionModel != null) {
         return selectionModel.getSelectedNode();
      }
      else if (selectionProvider != null) {
         Object[] elements = SWTUtil.toArray(selectionProvider.getSelection());
         Object element = ArrayUtil.search(elements, new IFilter<Object>() {
            @Override
            public boolean select(Object element) {
               return element instanceof Node || 
                      element instanceof BranchFolder;
            }
         });
         if (element instanceof Node) {
            return (Node) element;
         }
         else if (element instanceof BranchFolder) {
            return ((BranchFolder) element).getChild();
         }
         else {
            return null;
         }
      }
      else {
         return null;
      }
   }

   /**
    * Updates the shown content.
    * @param node The {@link Node} to show.
    */
   protected abstract void updateShownContent(Node node);

   /**
    * Sets the given items in the {@link List}.
    * @param list The {@link List} to show items in.
    * @param items The items to show.
    */
   protected void setListValues(List list, Iterable<?> items) {
      list.removeAll();
      if (items != null) {
         for (Object item : items) {
            if (item != null) {
               list.add(item.toString());
            }
         }
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void dispose() {
      if (selectionModel != null) {
         selectionModel.removeKeYSelectionListener(selectionListener);
      }
      if (selectionProvider != null) {
         selectionProvider.removeSelectionChangedListener(selectionChangedListener);
      }
      super.dispose();
   }
}