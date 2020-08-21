package org.key_project.sed.ui.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.key_project.sed.core.annotation.ISEAnnotationLink;
import org.key_project.sed.core.annotation.ISEAnnotationType;
import org.key_project.sed.core.annotation.impl.SearchAnnotation;
import org.key_project.sed.core.annotation.impl.SearchAnnotationType;
import org.key_project.sed.core.model.ISEDebugElement;
import org.key_project.sed.core.model.ISENode;
import org.key_project.sed.core.model.ISEDebugTarget;
import org.key_project.sed.core.util.SEAnnotationUtil;
import org.key_project.sed.core.util.SEPreorderIterator;
import org.key_project.sed.ui.util.LogUtil;
import org.key_project.sed.ui.wizard.page.SearchWizardPage;
import org.key_project.util.eclipse.swt.SWTUtil;

/**
 * The search {@link Wizard} which performs a search with help of a {@link SearchAnnotation}.
 * @author Martin Hentschel
 */
public class SearchWizard extends Wizard {
   /**
    * The {@link ISEDebugTarget} to search in.
    */
   private final ISEDebugTarget target;
   
   private final ISEAnnotationType annotationType;
   
   /**
    * The shown {@link SearchWizardPage}.
    */
   private SearchWizardPage searchWizardPage;

   /**
    * Constructor.
    * @param target The {@link ISEDebugTarget} to search in.
    */
   public SearchWizard(ISEDebugTarget target) {
      this.target = target;
      this.annotationType = SEAnnotationUtil.getAnnotationtype(SearchAnnotationType.TYPE_ID);
      Assert.isNotNull(target);
      Assert.isNotNull(annotationType);
      setWindowTitle("Search");
      setNeedsProgressMonitor(true);
      setHelpAvailable(false);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void addPages() {
      searchWizardPage = new SearchWizardPage("searchWizardPage", annotationType);
      addPage(searchWizardPage);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean performFinish() {
      try {
         final SearchAnnotation annotation = (SearchAnnotation)annotationType.createAnnotation();
         annotation.setSearch(searchWizardPage.getSearch());
         searchWizardPage.applyAppearance(annotation);
         getContainer().run(true, true, new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
               try {
                  // Perform search
                  List<ISEAnnotationLink> links = search(annotationType, annotation, target, annotation.getSearch(), monitor);
                  // Add annotation and links
                  SWTUtil.checkCanceled(monitor);
                  target.registerAnnotation(annotation);
                  monitor.worked(1);
                  for (ISEAnnotationLink link : links) {
                     link.getTarget().addAnnotationLink(link);
                     monitor.worked(1);
                  }
                  monitor.done();
               }
               catch (DebugException e) {
                  throw new InvocationTargetException(e);
               }
            }
         });
         return true;
      }
      catch (OperationCanceledException e) {
         return false;
      }
      catch (Exception e) {
         LogUtil.getLogger().logError(e);
         return false;
      }
   }
   
   /**
    * Performs the search.
    * @param type The {@link ISEAnnotationType} to use.
    * @param annotation The {@link SearchAnnotation} to use.
    * @param target The {@link ISEDebugTarget} to search in.
    * @param search The text to search for.
    * @param monitor The {@link IProgressMonitor} to use.
    * @return The created {@link ISEAnnotationLink}s linking to found {@link ISENode}.
    * @throws DebugException Occurred Exception.
    */
   public static List<ISEAnnotationLink> search(ISEAnnotationType type, 
                                                 SearchAnnotation annotation,
                                                 ISEDebugTarget target, 
                                                 String search, 
                                                 IProgressMonitor monitor) throws DebugException {
      monitor.beginTask("Performing search", IProgressMonitor.UNKNOWN);
      List<ISEAnnotationLink> foundNodes = new LinkedList<ISEAnnotationLink>();
      SEPreorderIterator iterator = new SEPreorderIterator(target);
      while (iterator.hasNext()) {
         SWTUtil.checkCanceled(monitor);
         ISEDebugElement next = iterator.next();
         if (next instanceof ISENode) {
            ISENode node = (ISENode)next;
            if (SearchAnnotationType.accept(node, search)) {
               ISEAnnotationLink link = type.createLink(annotation, node);
               foundNodes.add(link);
            }
         }
      }
      return foundNodes;
   }
   
   /**
    * Opens the {@link SearchWizard} in a {@link WizardDialog}.
    * @param parentShell The parent {@link Shell}.
    * @param target The {@link ISEDebugTarget} to search in.
    * @return The dialog result.
    */
   public static int openWizard(Shell parentShell, ISEDebugTarget target) {
      WizardDialog dialog = new WizardDialog(parentShell, new SearchWizard(target));
      dialog.setHelpAvailable(false);
      return dialog.open();
   }
}
