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

package org.key_project.sed.ui.util;

import java.net.URL;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.viewers.model.TreeModelContentProvider;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ILazyTreePathContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.key_project.sed.core.annotation.ISEAnnotationType;
import org.key_project.sed.core.model.ISEDebugElement;
import org.key_project.sed.core.model.ISEDebugTarget;
import org.key_project.sed.core.model.ISENode;
import org.key_project.sed.core.provider.SEDebugNodeContentProvider;
import org.key_project.sed.core.provider.SEDebugTargetContentProvider;
import org.key_project.sed.core.util.LogUtil;
import org.key_project.sed.core.util.SEPreorderIterator;
import org.key_project.sed.ui.action.ISEAnnotationAction;
import org.key_project.sed.ui.action.ISEAnnotationLinkAction;
import org.key_project.sed.ui.action.ISEAnnotationLinkEditAction;
import org.key_project.sed.ui.edit.ISEAnnotationEditor;
import org.key_project.util.eclipse.WorkbenchUtil;
import org.key_project.util.eclipse.job.AbstractDependingOnObjectsJob;
import org.key_project.util.eclipse.swt.SWTUtil;
import org.key_project.util.java.ArrayUtil;
import org.key_project.util.java.CollectionUtil;
import org.key_project.util.java.ObjectUtil;
import org.key_project.util.java.StringUtil;
import org.key_project.util.java.thread.AbstractRunnableWithException;
import org.key_project.util.java.thread.AbstractRunnableWithResult;
import org.key_project.util.java.thread.IRunnableWithException;
import org.key_project.util.java.thread.IRunnableWithResult;

/**
 * Provides static methods which makes working with the Eclipse Debug UI and 
 * SED UI easier.
 * @author Martin Hentschel
 */
@SuppressWarnings("restriction")
public final class SEDUIUtil {
   /**
    * The ID Of the extension point with the annotation actions.
    */
   public static final String ANNOTATION_ACTIONS = "org.key_project.sed.ui.annotationActions";

   /**
    * The ID Of the extension point with the annotation editors.
    */
   public static final String ANNOTATION_EDITORS = "org.key_project.sed.ui.annotationEditors";
   
   /**
    * The ID Of the extension point with the annotation link actions.
    */
   public static final String ANNOTATION_LINK_ACTIONS = "org.key_project.sed.ui.annotationLinkActions";
   
   /**
    * The ID Of the extension point with the annotation link edit actions.
    */
   public static final String ANNOTATION_LINK_EDIT_ACTIONS = "org.key_project.sed.ui.annotationLinkEditActions";
   
   /**
    * The ID Of the extension point with the annotation type images.
    */
   public static final String ANNOTATION_TYPE_IMAGES = "org.key_project.sed.ui.annotationTypeImages";

   /**
    * Contains all available {@link SEDAnnotationActionDescription}s.
    */
   private static final List<SEDAnnotationActionDescription> annotationActionDescriptions = createAnnotationActionDescriptions();

   /**
    * Contains all available {@link SEDAnnotationLinkActionDescription}s.
    */
   private static final List<SEDAnnotationLinkActionDescription> annotationLinkActionDescriptions = createAnnotationLinkActionDescriptions();
   
   /**
    * All available {@link ISEAnnotationLinkEditAction}s.
    */
   private static final Map<String, ISEAnnotationLinkEditAction> annotationLinkEditActions = createAnnotationLinkEditActions();
   
   /**
    * All available {@link Image}s of {@link ISEAnnotationType}s.
    */
   private static final Map<String, Image> annotationTypeImages = createAnnotationTypeImages();
   
   /**
    * Forbid instances.
    */
   private SEDUIUtil() {
   }

   /**
    * Selects the given elements in the given {@link IDebugView}.
    * @param parentPart The current {@link IWorkbenchPart} which requests the selection change.
    * @param debugView The {@link IDebugView} to change selection in.
    * @param selection The new selection to set in the {@link IDebugView}.
    */
   public static void selectInDebugView(IWorkbenchPart parentPart, 
                                        final IDebugView debugView, 
                                        ISelection selection) {
      selectInDebugView(parentPart, debugView, SWTUtil.toList(selection));
   }

   /**
    * Selects the given elements in the given {@link IDebugView}.
    * @param parentPart The current {@link IWorkbenchPart} which requests the selection change.
    * @param debugView The {@link IDebugView} to change selection in.
    * @param selection The new selection to set in the {@link IDebugView}.
    */
   public static void selectInDebugView(IWorkbenchPart parentPart, 
                                        final IDebugView debugView, 
                                        final List<?> selection) {
      // Make sure that the old selected business objects are different to the new one
      ISelection oldSelection = debugView.getViewer().getSelection();
      if (!selection.equals(SWTUtil.toList(oldSelection))) {
         // Change selection in debug view if new elements are selected in a Job because the debug view uses Jobs itself to expand the debug model and it is required to wait for them.
         AbstractDependingOnObjectsJob.cancelJobs(parentPart);
         Job selectJob = new AbstractDependingOnObjectsJob("Synchronizing selection", parentPart) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
               try {
                  SWTUtil.checkCanceled(monitor);
                  // Expand viewer up to the elements to select.
                  final Viewer debugViewer = debugView.getViewer();
                  if (debugViewer instanceof TreeViewer) {
                     TreeViewer treeViewer = (TreeViewer)debugViewer;
                     for (Object element : selection) {
                        SWTUtil.checkCanceled(monitor);
                        try {
                           monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
                           monitor.subTask("Collecting unknown elements");
                           Deque<Object> expandQue = collectUnknownElementsInParentHierarchy(treeViewer, element);
                           monitor.beginTask(getName(), expandQue.size() + 1);
                           monitor.subTask("Expanding unknown elements");
                           injectElements(treeViewer, expandQue, monitor);
                        }
                        catch (DebugException e) {
                           LogUtil.getLogger().logError("Can't expand debug view to element \"" + element + "\".", e);
                        }
                     }
                  }
                  // Select new elements
                  SWTUtil.checkCanceled(monitor);
                  monitor.beginTask("Select element", 1);
                  ISelection newSelection = SWTUtil.createSelection(selection);
                  SWTUtil.select(debugViewer, newSelection, true);
                  monitor.worked(1);
                  monitor.done();
                  return Status.OK_STATUS;
               }
               catch (OperationCanceledException e) {
                  return Status.CANCEL_STATUS;
               }
            }
         };
         selectJob.schedule();
      }
   }
   
   /**
    * Expands all given elements in the {@link IDebugView}.
    * @param parentPart The current {@link IWorkbenchPart} which requests the selection change.
    * @param debugView The {@link IDebugView} to change selection in.
    * @param toExpand The elements to expand.
    */
   public static void expandInDebugView(IWorkbenchPart parentPart, 
                                        final IDebugView debugView, 
                                        final List<?> toExpand) {
      // Change selection in debug view if new elements are selected in a Job because the debug view uses Jobs itself to expand the debug model and it is required to wait for them.
      AbstractDependingOnObjectsJob.cancelJobs(parentPart);
      Job selectJob = new AbstractDependingOnObjectsJob("Expanding elements", parentPart) {
         @Override
         protected IStatus run(IProgressMonitor monitor) {
            try {
               final Viewer debugViewer = debugView.getViewer();
               if (debugViewer instanceof TreeViewer) {
                  // Collect elements to expand
                  monitor.beginTask("Collecting unknown leafs", IProgressMonitor.UNKNOWN);
                  List<ISEDebugElement> leafs = new LinkedList<ISEDebugElement>();
                  TreeViewer treeViewer = (TreeViewer)debugViewer;
                  for (Object element : toExpand) {
                     try {
                        if (element instanceof ILaunch) {
                           for (IDebugTarget target : ((ILaunch)element).getDebugTargets()) {
                              if (target instanceof ISEDebugTarget) {
                                 collectElementsToExpand(monitor, treeViewer, (ISEDebugTarget)target, leafs);
                              }
                           }
                        }
                        else if (element instanceof ISEDebugElement) {
                           collectElementsToExpand(monitor, treeViewer, (ISEDebugElement)element, leafs);
                        }
                     }
                     catch (DebugException e) {
                        LogUtil.getLogger().logError("Can't collect leafs of \"" + element + "\".", e);
                     }
                  }
                  monitor.done();
                  // Inject unknown elements
                  monitor.beginTask("Injecting paths to leafs (" + leafs.size() + ")", leafs.size());
                  for (ISEDebugElement element : leafs) {
                     try {
                        SWTUtil.checkCanceled(monitor);
                        monitor.subTask("Collecting unknown elements");
                        Deque<Object> expandQue = collectUnknownElementsInParentHierarchy(treeViewer, element);
                        monitor.subTask("Injecting unknown elements");
                        injectElements(treeViewer, expandQue, new NullProgressMonitor());
                        monitor.worked(1);
                     }
                     catch (DebugException e) {
                        LogUtil.getLogger().logError("Can't inject debug view element \"" + element + "\".", e);
                     }
                  }
                  monitor.done();
                  // Expand elements
                  monitor.beginTask("Expanding elements", toExpand.size());
                  for (Object element : toExpand) {
                     SWTUtil.checkCanceled(monitor);
                     SWTUtil.expandToLevel(treeViewer, element, TreeViewer.ALL_LEVELS);
                  }
                  monitor.done();
               }
               monitor.done();
               return Status.OK_STATUS;
            }
            catch (OperationCanceledException e) {
               return Status.CANCEL_STATUS;
            }
         }
         
         protected void collectElementsToExpand(IProgressMonitor monitor, 
                                                TreeViewer treeViewer, 
                                                ISEDebugElement element, 
                                                List<ISEDebugElement> toFill) throws DebugException {
            SEPreorderIterator iterator = new SEPreorderIterator(element);
            while (iterator.hasNext()) {
               SWTUtil.checkCanceled(monitor);
               ISEDebugElement next = iterator.next();
               if (next instanceof ISENode) {
                  ISENode node = (ISENode)next;
                  if (node.getChildren().length == 0) { // Test for leaf
                     if (isUnknownInTreeViewer(treeViewer, node)) { // Test if leaf is unknown in the TreeViewer
                        toFill.add(next);
                     }
                  }
               }
               monitor.worked(0);
            }
         }
      };
      selectJob.schedule();
   }

   /**
    * <p>
    * Collects all elements in the parent hierarchy starting from the given one
    * which are unknown in the given {@link TreeViewer}.
    * </p>
    * <p>
    * Unknown elements are possible if an {@link ILazyTreeContentProvider} or
    * an {@link ILazyTreePathContentProvider} is used.
    * </p>
    * @param treeViewer The {@link TreeViewer} to search in.
    * @param element The element to start search for unknown elements.
    * @return A {@link Deque} which contains all unknown elements in order from root to given element.
    * @throws DebugException Occurred Exception.
    */
   protected static Deque<Object> collectUnknownElementsInParentHierarchy(final TreeViewer treeViewer, Object element) throws DebugException {
      Deque<Object> expandQue = new LinkedList<Object>();
      while (element != null) {
         // Check if the element is unknown in tree
         if (isUnknownInTreeViewer(treeViewer, element)) {
            // Element is not known, add to deque and continue with parent.
            expandQue.addFirst(element);
            // Update current element for next loop iteration.
            element = getParent(element);
         }
         else {
            // Element is known in tree, search can be stopped.
            element = null;
         }
      }
      return expandQue;
   }
   
   /**
    * Checks if the given {@link Object} is unknown in the {@link TreeViewer}.
    * @param treeViewer The {@link TreeViewer}.
    * @param toTest The {@link Object} to test.
    * @return {@code true} is unknown, {@code false} is known.
    */
   protected static boolean isUnknownInTreeViewer(final TreeViewer treeViewer, final Object toTest) {
      IRunnableWithResult<Boolean> run = new AbstractRunnableWithResult<Boolean>() {
         @Override
         public void run() {
            Widget item = treeViewer.testFindItem(toTest);
            setResult(item == null);
         }
      };
      treeViewer.getControl().getDisplay().syncExec(run);
      return run.getResult() != null && run.getResult().booleanValue();
   }
   
   /**
    * Computes the parent of the given element, because the used
    * {@link ILazyTreePathContentProvider} implementation
    * of the debug viewer returns {@code null} via
    * {@link ILazyTreePathContentProvider#getParents(Object)}.
    * @param element The current element.
    * @return The parent element if available or {@code null} otherwise.
    * @throws DebugException Occurred Exception.
    */
   public static Object getParent(Object element) throws DebugException {
      if (element instanceof ISENode) {
         return SEDebugNodeContentProvider.getDefaultInstance().getDebugNodeParent(element);
      }
      else if (element instanceof ISEDebugTarget) {
         return SEDebugTargetContentProvider.getDefaultInstance().getParent(element);
      }
      else {
         return null;
      }
   }
   
   /**
    * Returns the children of the given {@link Object} shown in
    * the viewer of view "Debug".
    * @param element The element to get children for.
    * @return The available children.
    * @throws DebugException Occurred Exception.
    */
   public static Object[] getChildren(Object element) throws DebugException {
      if (element instanceof ISEDebugTarget) {
         return SEDebugTargetContentProvider.getDefaultInstance().getAllChildren(element);
      }
      else if (element instanceof ISENode) {
         return SEDebugNodeContentProvider.getDefaultInstance().getAllDebugNodeChildren(element);
      }
      else {
         return null;
      }
   }

   /**
    * Injects all unknown elements of the {@link TreeViewer} in the
    * parent hierarchy order defined by the given {@link Deque}.
    * @param treeViewer The {@link TreeViewer} to make elements known in.
    * @param injectQue The {@link Deque} which contains the unknown elements from parent to leaf.
    * @param monitor The {@link IProgressMonitor} to use.
    * @throws DebugException Occurred Exception
    */
   protected static void injectElements(TreeViewer treeViewer, 
                                        Deque<Object> injectQue,
                                        IProgressMonitor monitor) throws DebugException {
      // Check if something must be done
      if (!CollectionUtil.isEmpty(injectQue)) {
         // Check if the provider is of the expected form.
         IContentProvider cp = treeViewer.getContentProvider();
         if (cp instanceof ILazyTreePathContentProvider) {
            final ILazyTreePathContentProvider lazyContentProvider = (ILazyTreePathContentProvider)cp;
            // Create tree path to last known element
            final LinkedList<Object> tpElements = new LinkedList<Object>();
            Object knownParent = getParent(injectQue.getFirst());
            while (knownParent != null) {
               SWTUtil.checkCanceled(monitor);
               tpElements.addFirst(knownParent);
               knownParent = getParent(knownParent);
            }
            // Injects elements starting at the root to make them familiar in tree
            for (final Object toInject : injectQue) {
               SWTUtil.checkCanceled(monitor);
               // Compute index on parent
               Object parent = getParent(toInject);
               final int viewIndex = ArrayUtil.indexOf(getChildren(parent), toInject);
               if (viewIndex >= 0) {
                  // Create tree path to current element
                  final TreePath tp = new TreePath(tpElements.toArray());
                  // Inject the element into the TreeViewer
                  runInViewerThread(treeViewer, lazyContentProvider, new AbstractRunnableWithException() {
                     @Override
                     public void run() {
                        try {
                           lazyContentProvider.updateChildCount(tp, 0);
                        }
                        catch (Exception e) {
                           setException(e);
                        }
                     }
                  });
                  runInViewerThread(treeViewer, lazyContentProvider, new AbstractRunnableWithException() {
                     @Override
                     public void run() {
                        try {
                           lazyContentProvider.updateElement(tp, viewIndex);
                        }
                        catch (Exception e) {
                           setException(e);
                        }
                     }
                  });
                  // Update tree path for next loop iteration
                  tpElements.add(toInject);
                  // Update monitor
                  monitor.worked(1);                
               }
               else {
                  // Something has changed in between and injection is not possible.
                  monitor.setCanceled(true);
               }
            }
         }
      }
   }
   
   /**
    * Executes the given {@link IRunnableWithResult} and waits until
    * the {@link ILazyTreeContentProvider} has done his work. 
    * @param treeViewer The {@link TreeViewer} to use.
    * @param lazyContentProvider The {@link ILazyTreePathContentProvider} to use.
    * @param run The {@link IRunnableWithException} to execute.
    * @throws DebugException Occurred Exception.
    */
   protected static void runInViewerThread(TreeViewer treeViewer, 
                                           ILazyTreePathContentProvider lazyContentProvider, 
                                           IRunnableWithException run) throws DebugException {
      treeViewer.getControl().getDisplay().syncExec(run);
      if (run.getException() != null) {
         throw new DebugException(LogUtil.getLogger().createErrorStatus(run.getException().getMessage(), run.getException()));
      }
      // Wait until all pending requests in content provider are done
      waitForPendingRequests(treeViewer, lazyContentProvider);
   }

   /**
    * Blocks the current thread until the given {@link ILazyTreeContentProvider}
    * has no pending requests.
    * @param treeViewer The {@link TreeViewer} to use.
    * @param lazyContentProvider The {@link ILazyTreeContentProvider} to wait for.
    * @throws DebugException Occurred Exception.
    */
   public static void waitForPendingRequests(TreeViewer treeViewer, 
                                             ILazyTreePathContentProvider lazyContentProvider) throws DebugException {
      while (hasPendingRequests(treeViewer, lazyContentProvider)) {
         try {
            Thread.sleep(10);
         }
         catch (InterruptedException e) {
         }
      }
   }

   /**
    * Checks if the given {@link ILazyTreeContentProvider} has pending requests.
    * @param treeViewer The {@link TreeViewer} to use.
    * @param lazyContentProvider The {@link ILazyTreeContentProvider} to check.
    * @return {@code true} has pending requests, {@code false} no pending requests.
    * @throws DebugException Occurred Exception.
    */
   protected static boolean hasPendingRequests(TreeViewer treeViewer, 
                                               final ILazyTreePathContentProvider lazyContentProvider) throws DebugException {
      if (lazyContentProvider instanceof TreeModelContentProvider){
         IRunnableWithResult<Boolean> run = new AbstractRunnableWithResult<Boolean>() {
            @Override
            public void run() {
               try {
                  synchronized (lazyContentProvider) {
                     Boolean result = ObjectUtil.invoke(lazyContentProvider, "areRequestsPending"); // Run in viewer thread in which all requests are scheduled.
                     setResult(result);
                  }
               }
               catch (Exception e) {
                  setException(e);
               }
            }
         };
         treeViewer.getControl().getDisplay().syncExec(run);
         if (run.getException() != null) {
            throw new DebugException(LogUtil.getLogger().createErrorStatus("Can't check if content provider \"" + lazyContentProvider + "\" has pending requests.", run.getException()));
         }
         return run.getResult() != null && run.getResult().booleanValue();
      }
      else {
         return false;
      }
   }
   
   /**
    * Returns all available annotation action descriptions.
    * @return All available annotation action descriptions.
    */
   public static List<SEDAnnotationActionDescription> getAnnotationActionDescriptions() {
      return annotationActionDescriptions;
   }

   /**
    * Creates all available {@link SEDAnnotationActionDescription}.
    * @return The available {@link SEDAnnotationActionDescription}.
    */
   private static List<SEDAnnotationActionDescription> createAnnotationActionDescriptions() {
      List<SEDAnnotationActionDescription> descriptions = new LinkedList<SEDAnnotationActionDescription>();
      // Add drivers registered by the extension point
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      if (registry != null) {
         IExtensionPoint point = registry.getExtensionPoint(ANNOTATION_ACTIONS);
         if (point != null) {
            // Analyze the extension point
            IExtension[] extensions = point.getExtensions();
            for (IExtension extension : extensions) {
               IConfigurationElement[] configElements = extension.getConfigurationElements();
               for (IConfigurationElement configElement : configElements) {
                  try {
                     String text = configElement.getAttribute("text");
                     ISEAnnotationAction action = (ISEAnnotationAction)configElement.createExecutableExtension("class");
                     String imagePath = configElement.getAttribute("icon");
                     String toolTipText = configElement.getAttribute("toolTipText");
                     Image image = null;;
                     if (!StringUtil.isEmpty(imagePath)) {
                        URL url = Platform.getBundle(extension.getNamespaceIdentifier()).getResource(imagePath);
                        if (url != null) {
                           image = new Image(Display.getDefault(), url.openStream());
                        }
                     }
                     descriptions.add(new SEDAnnotationActionDescription(text, image, toolTipText, action));
                  }
                  catch (Exception e) {
                     LogUtil.getLogger().logError(e);
                  }
               }
            }
         }
         else {
            LogUtil.getLogger().logError("Extension point \"" + ANNOTATION_ACTIONS + "\" doesn't exist.");
         }
      }
      else {
         LogUtil.getLogger().logError("Extension point registry is not loaded.");
      }
      return Collections.unmodifiableList(descriptions);
   }
   
   /**
    * Returns all available annotation action descriptions.
    * @return All available annotation action descriptions.
    */
   public static List<SEDAnnotationLinkActionDescription> getAnnotationLinkActionDescriptions() {
      return annotationLinkActionDescriptions;
   }

   /**
    * Creates all available {@link SEDAnnotationActionDescription}.
    * @return The available {@link SEDAnnotationActionDescription}.
    */
   private static List<SEDAnnotationLinkActionDescription> createAnnotationLinkActionDescriptions() {
      List<SEDAnnotationLinkActionDescription> descriptions = new LinkedList<SEDAnnotationLinkActionDescription>();
      // Add drivers registered by the extension point
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      if (registry != null) {
         IExtensionPoint point = registry.getExtensionPoint(ANNOTATION_LINK_ACTIONS);
         if (point != null) {
            // Analyze the extension point
            IExtension[] extensions = point.getExtensions();
            for (IExtension extension : extensions) {
               IConfigurationElement[] configElements = extension.getConfigurationElements();
               for (IConfigurationElement configElement : configElements) {
                  try {
                     String text = configElement.getAttribute("text");
                     ISEAnnotationLinkAction action = (ISEAnnotationLinkAction)configElement.createExecutableExtension("class");
                     String imagePath = configElement.getAttribute("icon");
                     String toolTipText = configElement.getAttribute("toolTipText");
                     Image image = null;;
                     if (!StringUtil.isEmpty(imagePath)) {
                        URL url = Platform.getBundle(extension.getNamespaceIdentifier()).getResource(imagePath);
                        if (url != null) {
                           image = new Image(Display.getDefault(), url.openStream());
                        }
                     }
                     descriptions.add(new SEDAnnotationLinkActionDescription(text, image, toolTipText, action));
                  }
                  catch (Exception e) {
                     LogUtil.getLogger().logError(e);
                  }
               }
            }
         }
         else {
            LogUtil.getLogger().logError("Extension point \"" + ANNOTATION_LINK_ACTIONS + "\" doesn't exist.");
         }
      }
      else {
         LogUtil.getLogger().logError("Extension point registry is not loaded.");
      }
      return Collections.unmodifiableList(descriptions);
   }
   
   /**
    * Provides the basic implementation of action descriptions.
    * @author Martin Hentschel
    */
   public static abstract class AbstractActionDescription {
      /**
       * The text.
       */
      private final String text;
      
      /**
       * The image.
       */
      private final Image image;
      
      /**
       * The tool tip text.
       */
      private final String toolTipText;
      
      /**
       * Constructor.
       * @param text The text.
       * @param image The image.
       * @param toolTipText The tool tip text.
       */
      public AbstractActionDescription(String text, Image image, String toolTipText) {
         this.text = text;
         this.image = image;
         this.toolTipText = toolTipText;
      }

      /**
       * Returns the text.
       * @return The text.
       */
      public String getText() {
         return text;
      }

      /**
       * Returns the image.
       * @return The image.
       */
      public Image getImage() {
         return image;
      }

      /**
       * Returns the tool tip text.
       * @return The tool tip text.
       */
      public String getToolTipText() {
         return toolTipText;
      }
   }
   
   /**
    * An annotation action description with the content
    * provided via the extension point {@link SEDUIUtil#ANNOTATION_ACTIONS}.
    * @author Martin Hentschel
    */
   public static final class SEDAnnotationActionDescription extends AbstractActionDescription {
      /**
       * The {@link ISEAnnotationAction}.
       */
      private final ISEAnnotationAction action;

      /**
       * Constructor.
       * @param text The text.
       * @param image The image.
       * @param toolTipText The tool tip text.
       * @param action The {@link ISEAnnotationAction}.
       */
      public SEDAnnotationActionDescription(String text, Image image, String toolTipText, ISEAnnotationAction action) {
         super(text, image, toolTipText);
         this.action = action;
      }

      /**
       * Returns the {@link ISEAnnotationAction}.
       * @return The {@link ISEAnnotationAction}.
       */
      public ISEAnnotationAction getAction() {
         return action;
      }
   }
   
   /**
    * An annotation action description with the content
    * provided via the extension point {@link SEDUIUtil#ANNOTATION_LINK_ACTIONS}.
    * @author Martin Hentschel
    */
   public static final class SEDAnnotationLinkActionDescription extends AbstractActionDescription {
      /**
       * The {@link ISEAnnotationLinkAction}.
       */
      private final ISEAnnotationLinkAction action;

      /**
       * Constructor.
       * @param text The text.
       * @param image The image.
       * @param toolTipText The tool tip text.
       * @param action The {@link ISEAnnotationLinkAction}.
       */
      public SEDAnnotationLinkActionDescription(String text, Image image, String toolTipText, ISEAnnotationLinkAction action) {
         super(text, image, toolTipText);
         this.action = action;
      }

      /**
       * Returns the {@link ISEAnnotationLinkAction}.
       * @return The {@link ISEAnnotationLinkAction}.
       */
      public ISEAnnotationLinkAction getAction() {
         return action;
      }
   }
   
   /**
    * Returns the {@link ISEAnnotationEditor} for the given {@link ISEAnnotationType}.
    * @param type The {@link ISEAnnotationType}.
    * @return A fresh created {@link ISEAnnotationEditor} or {@code null} if not available.
    */
   public static ISEAnnotationEditor getAnnotationEditor(ISEAnnotationType type) {
      return type != null ? getAnnotationEditor(type.getTypeId()) : null;
   }
   
   /**
    * Returns the {@link ISEAnnotationEditor} for the given annotation type ID.
    * @param typeId The given annotation type ID.
    * @return A fresh created {@link ISEAnnotationEditor} or {@code null} if not available.
    */
   public static ISEAnnotationEditor getAnnotationEditor(String typeId) {
      ISEAnnotationEditor result = null;
      // Add drivers registered by the extension point
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      if (registry != null) {
         IExtensionPoint point = registry.getExtensionPoint(ANNOTATION_EDITORS);
         if (point != null) {
            // Analyze the extension point
            IExtension[] extensions = point.getExtensions();
            int i = 0;
            while (result == null && i < extensions.length) {
               IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
               for (IConfigurationElement configElement : configElements) {
                  try {
                     String annotationTypeID = configElement.getAttribute("annotationTypeID");
                     if (ObjectUtil.equals(annotationTypeID, typeId)) {
                        result = (ISEAnnotationEditor)configElement.createExecutableExtension("class");
                     }
                  }
                  catch (Exception e) {
                     LogUtil.getLogger().logError(e);
                  }
               }
               i++;
            }
         }
         else {
            LogUtil.getLogger().logError("Extension point \"" + ANNOTATION_EDITORS + "\" doesn't exist.");
         }
      }
      else {
         LogUtil.getLogger().logError("Extension point registry is not loaded.");
      }
      return result;
   }
   
   /**
    * Returns the {@link ISEAnnotationLinkEditAction} for the given annotation type id.
    * @param annotationTypeID The annotation type id.
    * @return The {@link ISEAnnotationLinkEditAction} if available or {@code null} otherwise.
    */
   public static ISEAnnotationLinkEditAction getAnnotationLinkEditAction(String annotationTypeID) {
      return annotationLinkEditActions.get(annotationTypeID);
   }
   
   /**
    * Lists all available {@link ISEAnnotationLinkEditAction}s.
    * @return All available {@link ISEAnnotationLinkEditAction}s.
    */
   private static Map<String, ISEAnnotationLinkEditAction> createAnnotationLinkEditActions() {
      Map<String, ISEAnnotationLinkEditAction> result = new HashMap<String, ISEAnnotationLinkEditAction>();
      // Add drivers registered by the extension point
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      if (registry != null) {
         IExtensionPoint point = registry.getExtensionPoint(ANNOTATION_LINK_EDIT_ACTIONS);
         if (point != null) {
            // Analyze the extension point
            IExtension[] extensions = point.getExtensions();
            for (IExtension extension : extensions) {
               IConfigurationElement[] configElements = extension.getConfigurationElements();
               for (IConfigurationElement configElement : configElements) {
                  try {
                     String annotationTypeID = configElement.getAttribute("annotationTypeID");
                     ISEAnnotationLinkEditAction editAction = result.get(annotationTypeID);
                     if (editAction == null) {
                        result.put(annotationTypeID, (ISEAnnotationLinkEditAction)configElement.createExecutableExtension("class"));
                     }
                     else {
                        LogUtil.getLogger().logError("Annotion link edit action for annotation type \"" + annotationTypeID + "\" already found.");
                     }
                  }
                  catch (Exception e) {
                     LogUtil.getLogger().logError(e);
                  }
               }
            }
         }
         else {
            LogUtil.getLogger().logError("Extension point \"" + ANNOTATION_LINK_EDIT_ACTIONS + "\" doesn't exist.");
         }
      }
      else {
         LogUtil.getLogger().logError("Extension point registry is not loaded.");
      }
      return result;
   }

   /**
    * Returns the {@link IDebugView} shown in the given {@link Shell} if available.
    * @param shell The {@link Shell} to search the {@link IDebugView} in.
    * @return The found {@link IDebugView} or {@code null} if not available.
    */
   public static IDebugView getDebugView(Shell shell) {
      IWorkbenchWindow window = WorkbenchUtil.findWorkbenchWindow(shell);
      if (window != null) {
         IViewPart view = window.getActivePage().findView(IDebugUIConstants.ID_DEBUG_VIEW);
         return view instanceof IDebugView ? (IDebugView)view : null;
      }
      else {
         return null;
      }
   }

   /**
    * Returns the image of the given {@link ISEAnnotationType}.
    * @param type The {@link ISEAnnotationType}.
    * @return The found {@link Image} or {@code null} if not available.
    */
   public static Image getAnnotationTypeImage(ISEAnnotationType type) {
      if (type != null) {
         return getAnnotationTypeImage(type.getTypeId());
      }
      else {
         return null;
      }
   }

   /**
    * Returns the image for the annotation type ID.
    * @param annotationTypeID The annotation type ID.
    * @return The found {@link Image} or {@code null} if not available.
    */
   public static Image getAnnotationTypeImage(String annotationTypeID) {
      return annotationTypeImages.get(annotationTypeID);
   }
   
   /**
    * Creates all available annotation type images.
    * @return The available annotation type images.
    */
   private static Map<String, Image> createAnnotationTypeImages() {
      Map<String, Image> result = new HashMap<String, Image>();
      // Add drivers registered by the extension point
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      if (registry != null) {
         IExtensionPoint point = registry.getExtensionPoint(ANNOTATION_TYPE_IMAGES);
         if (point != null) {
            // Analyze the extension point
            IExtension[] extensions = point.getExtensions();
            for (IExtension extension : extensions) {
               IConfigurationElement[] configElements = extension.getConfigurationElements();
               for (IConfigurationElement configElement : configElements) {
                  try {
                     String annotationTypeID = configElement.getAttribute("annotationTypeID");
                     Image image = result.get(annotationTypeID);
                     if (image == null) {
                        String imagePath = configElement.getAttribute("icon");
                        if (!StringUtil.isEmpty(imagePath)) {
                           URL url = Platform.getBundle(extension.getNamespaceIdentifier()).getResource(imagePath);
                           if (url != null) {
                              image = new Image(Display.getDefault(), url.openStream());
                              result.put(annotationTypeID, image);
                           }
                        }
                     }
                     else {
                        LogUtil.getLogger().logError("Annotion link edit action for annotation type \"" + annotationTypeID + "\" already found.");
                     }
                  }
                  catch (Exception e) {
                     LogUtil.getLogger().logError(e);
                  }
               }
            }
         }
         else {
            LogUtil.getLogger().logError("Extension point \"" + ANNOTATION_TYPE_IMAGES + "\" doesn't exist.");
         }
      }
      else {
         LogUtil.getLogger().logError("Extension point registry is not loaded.");
      }
      return result;
   }
   
   /**
    * Returns the {@link TreeModelContentProvider} used in the given {@link IDebugView}.
    * @param debugView The {@link IDebugView} to get its {@link TreeModelContentProvider}.
    * @return The {@link TreeModelContentProvider} or {@code null} if not available.
    */
   public static TreeModelContentProvider getContentProvider(IDebugView debugView) {
      TreeModelContentProvider result = null;
      if (debugView != null) {
         Viewer viewer = debugView.getViewer();
         if (viewer instanceof ContentViewer) {
            IContentProvider cp = ((ContentViewer)viewer).getContentProvider();
            if (cp instanceof TreeModelContentProvider) {
               result = (TreeModelContentProvider)cp;
            }
         }
      }
      return result;
   }
}