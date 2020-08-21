package org.key_project.sed.ui.dialog;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.key_project.sed.core.annotation.ISEAnnotation;
import org.key_project.sed.core.annotation.ISEAnnotationLink;
import org.key_project.sed.core.model.ISEDebugTarget;
import org.key_project.sed.core.model.event.ISEAnnotationListener;
import org.key_project.sed.core.model.event.SEAnnotationEvent;
import org.key_project.sed.core.provider.SEDebugNodeContentProvider;
import org.key_project.sed.ui.action.ISEAnnotationLinkEditAction;
import org.key_project.sed.ui.provider.AnnotationAnnotationLinkLabelProvider;
import org.key_project.sed.ui.provider.AnnotationAnnotationLinkLazyContentProvider;
import org.key_project.sed.ui.util.LogUtil;
import org.key_project.sed.ui.util.SEDImages;
import org.key_project.sed.ui.util.SEDUIUtil;
import org.key_project.util.eclipse.swt.SWTUtil;
import org.key_project.util.java.ArrayUtil;

/**
 * This {@link Dialog} is used to list all {@link ISEAnnotationLink}s of
 * an {@link ISEAnnotation}.
 * @author Martin Hentschel
 */
public class AnnotationLinksDialog extends TitleAreaDialog {
   /**
    * The dialog result.
    */
   public static final int CLOSE = 2;
   
   /**
    * The {@link ISEDebugTarget} in which {@link #annotation} is used.
    */
   private final ISEDebugTarget target;
   
   /**
    * Listens for changes on {@link #target}.
    */
   private final ISEAnnotationListener targetListener = new ISEAnnotationListener() {
      @Override
      public void annotationUnregistered(SEAnnotationEvent e) {
         handleAnnotationUnregistered(e);
      }
      
      @Override
      public void annotationRegistered(SEAnnotationEvent e) {
      }
      
      @Override
      public void annotationMoved(SEAnnotationEvent e) {
      }
   };
   
   /**
    * The {@link ISEAnnotation} which provides the shown {@link ISEAnnotationLink}s.
    */
   private final ISEAnnotation annotation;
   
   /**
    * Shows the {@link ISEAnnotationLink}s.
    */
   private TableViewer linksViewer;
   
   /**
    * THe {@link ILazyContentProvider} of {@link #linksViewer}.
    */
   private AnnotationAnnotationLinkLazyContentProvider linksContentProvider;
   
   /**
    * The {@link ITableLabelProvider} of {@link #linksViewer}.
    */
   private AnnotationAnnotationLinkLabelProvider linksLabelProvider;

   /**
    * Context menu item of {@link #linksViewer} to follow the {@link ISEAnnotationLink}.
    */
   private Action followAction;

   /**
    * Context menu item of {@link #linksViewer} to edit the {@link ISEAnnotationLink}.
    */
   private Action editAction;

   /**
    * Context menu item of {@link #linksViewer} to delete the {@link ISEAnnotationLink}.
    */
   private Action deleteAction;
   
   /**
    * Constructor.
    * @param parentShell The parent {@link Shell}.
    * @param target The {@link ISEDebugTarget} in which {@link #annotation} is used.
    * @param annotation The {@link ISEAnnotation} which provides the shown {@link ISEAnnotationLink}s.
    */
   public AnnotationLinksDialog(Shell parentShell, 
                                ISEDebugTarget target,
                                ISEAnnotation annotation) {
      super(parentShell);
      Assert.isNotNull(target);
      Assert.isNotNull(annotation);
      this.target = target;
      this.target.addAnnotationListener(targetListener);
      this.annotation = annotation;
      setShellStyle(SWT.SHELL_TRIM | SWT.RESIZE);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void create() {
     super.create();
     setTitle("Annotation links of " + annotation);
     setMessage("Inspect all available annotation links.");
     setTitleImage(SEDImages.getImage(SEDImages.ANNOTATION_LINKS_WIZARD));
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected Control createDialogArea(Composite parent) {
      Composite area = (Composite) super.createDialogArea(parent);
      Composite container = new Composite(area, SWT.NONE);
      container.setLayoutData(new GridData(GridData.FILL_BOTH));
      container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      container.setLayout(new GridLayout(1, false));  
      
      String[] additionalColumnTexts = annotation.getType().getAdditionalLinkColumns(annotation);
      if (!ArrayUtil.isEmpty(additionalColumnTexts)) {
         Composite linksViewerComposite = new Composite(container, SWT.NONE);
         linksViewerComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
         TableColumnLayout linksViewerLayout = new TableColumnLayout();
         linksViewerComposite.setLayout(linksViewerLayout);
         linksViewer = new TableViewer(linksViewerComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL);
         linksViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
         TableViewerColumn nodeColumn = new TableViewerColumn(linksViewer, SWT.NONE);
         nodeColumn.getColumn().setText("Node");
         nodeColumn.getColumn().setResizable(true);
         nodeColumn.getColumn().setWidth(200);
         linksViewerLayout.setColumnData(nodeColumn.getColumn(), new ColumnWeightData(50, 300, true));
         for (String additionalColumnText : additionalColumnTexts) {
            TableViewerColumn column = new TableViewerColumn(linksViewer, SWT.NONE);
            column.getColumn().setText(additionalColumnText);
            column.getColumn().setResizable(true);
            linksViewerLayout.setColumnData(column.getColumn(), new ColumnWeightData(50 / additionalColumnTexts.length, 100, true));
         }
         linksViewer.getTable().setHeaderVisible(true);
         linksViewer.getTable().setLinesVisible(true);
      }
      else {
         linksViewer = new TableViewer(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL);
         linksViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
      }
      linksViewer.setUseHashlookup(true);
      linksViewer.addDoubleClickListener(new IDoubleClickListener() {
         @Override
         public void doubleClick(DoubleClickEvent event) {
            handleDoubleClick(event);
         }
      });
      MenuManager linksViewerMenuManager = new MenuManager();
      followAction = new Action("&Follow", SEDImages.getImageDescriptor(SEDImages.ANNOTATION_GO_TO)) {
         @Override
         public void run() {
            selectLinkTarget();
         }
      };
      followAction.setEnabled(false);
      linksViewerMenuManager.add(followAction);
      linksViewerMenuManager.add(new Separator());
      editAction = new Action("&Edit", SEDImages.getImageDescriptor(SEDImages.ANNOTATION_EDIT)) {
         @Override
         public void run() {
            editLink();
         }
      };
      editAction.setEnabled(false);
      linksViewerMenuManager.add(editAction);
      deleteAction = new Action("&Delete", SEDImages.getImageDescriptor(SEDImages.ANNOTATION_DELETE)) {
         @Override
         public void run() {
            deleteLink();
         }
      };
      linksViewerMenuManager.add(deleteAction);
      deleteAction.setEnabled(false);
      linksViewer.getControl().setMenu(linksViewerMenuManager.createContextMenu(linksViewer.getControl()));
      linksViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         @Override
         public void selectionChanged(SelectionChangedEvent event) {
            handleSelectionChanged(event);
         }
      });
      linksContentProvider = new AnnotationAnnotationLinkLazyContentProvider();
      linksViewer.setContentProvider(linksContentProvider);
      linksLabelProvider = new AnnotationAnnotationLinkLabelProvider(annotation);
      linksViewer.setLabelProvider(linksLabelProvider);
      linksViewer.setInput(annotation);
      return area;
   }

   /**
    * Follows the currently selected {@link ISEAnnotationLink} if possible.
    */
   public void selectLinkTarget() {
      followLink(linksViewer.getSelection());
   }

   /**
    * Edits the currently selected {@link ISEAnnotationLink} if possible.
    */
   public void editLink() {
      Object object = SWTUtil.getFirstElement(linksViewer.getSelection());
      if (object instanceof ISEAnnotationLink) {
         ISEAnnotationLink link = (ISEAnnotationLink)object;
         ISEAnnotationLinkEditAction action = SEDUIUtil.getAnnotationLinkEditAction(link.getSource().getType().getTypeId());
         if (action != null && action.canEdit(link)) {
            action.edit(getShell(), link);
         }
      }
   }

   /**
    * Deletes the currently selected {@link ISEAnnotationLink} if possible.
    */
   public void deleteLink() {
      Object object = SWTUtil.getFirstElement(linksViewer.getSelection());
      if (object instanceof ISEAnnotationLink) {
         ISEAnnotationLink link = (ISEAnnotationLink)object;
         if (link.canDelete()) {
            link.delete();
         }
      }
   }

   /**
    * When the selection of {@link #linksViewer} has changed.
    * @param event The event.
    */
   protected void handleSelectionChanged(SelectionChangedEvent event) {
      Object object = SWTUtil.getFirstElement(event.getSelection());
      if (object instanceof ISEAnnotationLink) {
         ISEAnnotationLink link = (ISEAnnotationLink)object;
         followAction.setEnabled(link.getTarget() != null);
         ISEAnnotationLinkEditAction action = SEDUIUtil.getAnnotationLinkEditAction(link.getSource().getType().getTypeId());
         editAction.setEnabled(action != null && action.canEdit(link));
         deleteAction.setEnabled(link.canDelete());
      }
      else {
         followAction.setEnabled(false);
         editAction.setEnabled(false);
         deleteAction.setEnabled(false);
      }
   }

   /**
    * Handles a double click on {@link #linksViewer}.
    * @param event The event.
    */
   protected void handleDoubleClick(DoubleClickEvent event) {
      followLink(event.getSelection());
   }

   /**
    * Follows the {@link ISEAnnotationLink} of the {@link ISelection}.
    * @param selection The {@link ISelection}.
    */
   protected void followLink(ISelection selection) {
      try {
         Object object = SWTUtil.getFirstElement(selection);
         if (object instanceof ISEAnnotationLink) {
            ISEAnnotationLink link = (ISEAnnotationLink)object;
            if (SEDebugNodeContentProvider.getDefaultInstance().isShown(link.getTarget())) {
               IDebugView debugView = SEDUIUtil.getDebugView(getParentShell());
               if (debugView != null) {
                  SEDUIUtil.selectInDebugView(debugView.getSite().getPart(), 
                                              debugView, 
                                              SWTUtil.createSelection(link.getTarget()));
               }
            }
         }
      }
      catch (DebugException e) {
         LogUtil.getLogger().logError(e);
         LogUtil.getLogger().openErrorDialog(getShell(), e);
      }
   }

   /**
    * {@inheritDoc}
    * @return
    */
   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
   }
   
   /**
    * {@inheritDoc}
    * @return
    */
   @Override
   protected void buttonPressed(int buttonId) {
      if (IDialogConstants.CLOSE_ID == buttonId) {
         closePressed();
      }
      else {
         super.buttonPressed(buttonId);
      }
   }

   /**
    * When the closed {@link Button} was pressed.
    */
   protected void closePressed() {
      setReturnCode(CLOSE);
      close();
   }

   /**
    * {@inheritDoc}
    * @return
    */
   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Annotation links");
   }

   /**
    * When an {@link ISEAnnotation} was removed from {@link #target}.
    * @param e The event.
    */
   protected void handleAnnotationUnregistered(SEAnnotationEvent e) {
      if (e.getAnnotation() == annotation) {
         getShell().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
               close();
            }
         });
      }
   }

   /**
    * {@inheritDoc}
    * @return
    */
   @Override
   public boolean close() {
      this.target.removeAnnotationListener(targetListener);
      if (linksContentProvider != null) {
         linksContentProvider.dispose();
      }
      if (linksLabelProvider != null) {
         linksLabelProvider.dispose();
      }
      return super.close();
   }
}
