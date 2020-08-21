package org.key_project.sed.ui.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.key_project.sed.core.model.ISEDebugElement;
import org.key_project.sed.core.model.ISEDebugTarget;
import org.key_project.sed.core.sourcesummary.ISESourceModel;
import org.key_project.sed.core.sourcesummary.ISESourceRange;
import org.key_project.sed.core.sourcesummary.ISESourceSummary;
import org.key_project.sed.ui.util.LaunchViewManager;
import org.key_project.sed.ui.util.LogUtil;
import org.key_project.util.eclipse.swt.SWTUtil;
import org.key_project.util.java.CollectionUtil;

/**
 * <p>
 * Instances of this class are responsible to highlight the source code
 * reached during symbolic execution with help of {@link SymbolicallyReachedAnnotation}s.
 * </p>
 * <p>
 * For each {@link LaunchView} is an instance of {@link AnnotationManager}
 * by the {@link LaunchViewManager} created.
 * </p>
 * @author Martin Hentschel
 */
@SuppressWarnings("restriction")
public class AnnotationManager implements IDisposable {
   /**
    * The observed {@link IDebugView}.
    */
   private final IDebugView debugView;
   
   /**
    * Listens for selection changes on {@link #debugView}.
    */
   private final ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
         handleSelectionChanged(event);
      }
   };

   /**
    * Listens for debug events.
    */
   private final IDebugEventSetListener debugListener = new IDebugEventSetListener() {
      @Override
      public void handleDebugEvents(DebugEvent[] events) {
         AnnotationManager.this.handleDebugEvents(events);
      }
   };
   
   /**
    * Listens for opened {@link IWorkbenchPart}s.
    */
   private final IPartListener partListener = new IPartListener() {
      @Override
      public void partOpened(IWorkbenchPart part) {
         handlePartOpened(part);
      }
      
      @Override
      public void partDeactivated(IWorkbenchPart part) {
      }
      
      @Override
      public void partClosed(IWorkbenchPart part) {
      }
      
      @Override
      public void partBroughtToTop(IWorkbenchPart part) {
      }
      
      @Override
      public void partActivated(IWorkbenchPart part) {
      }
   };
   
   /**
    * The currently annotated {@link ISEDebugTarget}s.
    */
   private Set<ISEDebugTarget> annotatedDebugTargets;
   
   /**
    * Constructor.
    * @param debugView The {@link IDebugView} to work with.
    */
   public AnnotationManager(final IDebugView debugView) {
      Assert.isNotNull(debugView);
      this.debugView = debugView;
      DebugPlugin.getDefault().addDebugEventListener(debugListener);
      debugView.getViewer().addSelectionChangedListener(selectionListener);
      debugView.getSite().getShell().getDisplay().syncExec(new Runnable() {
         @Override
         public void run() {
            updateAnnotations(debugView.getViewer().getSelection());
         }
      });
      debugView.getSite().getPage().addPartListener(partListener);
   }

   /**
    * When the selection of {@link #debugView} has changed.
    * @param event The event.
    */
   protected void handleSelectionChanged(SelectionChangedEvent event) {
      updateAnnotations(event.getSelection());
   }
   
   /**
    * Updates the shown annotations according to the {@link ISelection}.
    * @param selection The {@link ISelection} with the {@link ISEDebugTarget}s to annotate.
    */
   protected void updateAnnotations(ISelection selection) {
      Set<ISEDebugTarget> newTargets = new HashSet<ISEDebugTarget>();
      Object[] selected = SWTUtil.toArray(selection);
      for (Object element : selected) {
         if (element instanceof ILaunch) {
            ILaunch launch = (ILaunch)element;
            IDebugTarget[] targets = launch.getDebugTargets();
            for (IDebugTarget target : targets) {
               if (target instanceof ISEDebugTarget) {
                  newTargets.add((ISEDebugTarget)target);
               }
            }
         }
         else if (element instanceof IDebugTarget) {
            if (element instanceof ISEDebugTarget) {
               newTargets.add((ISEDebugTarget)element);
            }
         }
         else if (element instanceof IDebugElement) {
            IDebugTarget target = ((IDebugElement)element).getDebugTarget();
            if (target instanceof ISEDebugTarget) {
               newTargets.add((ISEDebugTarget)target);
            }
         }
      }
      if (!CollectionUtil.containsSame(annotatedDebugTargets, newTargets)) {
         updateAnnotations(newTargets.isEmpty() ? null : newTargets);
      }
   }
   
   /**
    * Changes the currently shown annotations.
    * @param targets The new {@link ISEDebugTarget}s to annotate, may {@code null}.
    */
   protected void updateAnnotations(Set<ISEDebugTarget> targets) {
      if (annotatedDebugTargets != null) {
         if (targets == null) {
            for (ISEDebugTarget target : annotatedDebugTargets) {
               removeAnnotations(target);
            }
         }
         else {
            for (ISEDebugTarget target : annotatedDebugTargets) {
               if (!targets.contains(target)) {
                  removeAnnotations(target);
               }
            }
         }
      }
      Set<ISEDebugTarget> oldTargets = annotatedDebugTargets;
      annotatedDebugTargets = targets;
      if (annotatedDebugTargets != null) {
         if (oldTargets == null) {
            for (ISEDebugTarget target : annotatedDebugTargets) {
               showAnnotations(target);
            }
         }
         else {
            for (ISEDebugTarget target : annotatedDebugTargets) {
               if (!oldTargets.contains(target)) {
                  showAnnotations(target);
               }
            }
         }
      }
   }

   /**
    * Removes all annotations of the given {@link ISEDebugTarget}.
    * @param target The {@link ISEDebugTarget} to remove its annotations.
    */
   protected void removeAnnotations(ISEDebugTarget target) {
      IEditorReference[] editorReferences = debugView.getSite().getPage().getEditorReferences();
      for (IEditorReference reference : editorReferences) {
         IEditorPart editor = reference.getEditor(false);
         if (editor instanceof ITextEditor) {
            ITextEditor te = (ITextEditor)editor;
            IDocumentProvider provider = te.getDocumentProvider();
            IAnnotationModel model = provider.getAnnotationModel(editor.getEditorInput());
            if (model != null) {
               Iterator<?> iter = model.getAnnotationIterator();
               while (iter.hasNext()) {
                  Object next = iter.next();
                  if (next instanceof SymbolicallyReachedAnnotation) {
                     SymbolicallyReachedAnnotation annotation = (SymbolicallyReachedAnnotation)next;
                     removeTarget(model, annotation, target);
                  }
               }
            }
         }
      }
   }

   /**
    * Removes the given {@link ISEDebugTarget} from the {@link SymbolicallyReachedAnnotation}
    * and the {@link SymbolicallyReachedAnnotation} if empty from the {@link IAnnotationModel}.
    * @param model The {@link IAnnotationModel}.
    * @param annotation The {@link SymbolicallyReachedAnnotation} to {@link ISEDebugTarget} in.
    * @param target The {@link ISEDebugTarget} to remove.
    */
   protected void removeTarget(IAnnotationModel model, SymbolicallyReachedAnnotation annotation, ISEDebugTarget target) {
      annotation.removeTarget(target);
      if (!annotation.hasTargets()) {
         model.removeAnnotation(annotation);
      }
   }

   /**
    * Shows all annotations of the given {@link ISEDebugTarget}.
    * @param target The {@link ISEDebugTarget} which provides the reached source code.
    */
   protected void showAnnotations(ISEDebugTarget target) {
      IEditorReference[] editorReferences = debugView.getSite().getPage().getEditorReferences();
      for (IEditorReference reference : editorReferences) {
         IEditorPart editor = reference.getEditor(false);
         updateAnnotations(editor, target);
      }
   }
   
   /**
    * Computes the {@link Position} of the given {@link ISESourceRange} in the given {@link IDocument}.
    * @param document The {@link IDocument} to compute the {@link Position} in.
    * @param range The {@link ISESourceRange}.
    * @return The computed {@link Position} or {@code null} if not available.
    */
   protected Position computePosition(IDocument document, ISESourceRange range) {
      if (range.getCharStart() >= 0 && range.getCharEnd() >= range.getCharStart()) {
         return new Position(range.getCharStart(), range.getCharEnd() - range.getCharStart());
      }
      else if (range.getLineNumber() >= 0) {
         try {
            if (document != null) {
               IRegion line = document.getLineInformation(range.getLineNumber());
               if (line != null) {
                  return new Position(line.getOffset(), line.getLength());
               }
            }
         }
         catch (BadLocationException e) {
            LogUtil.getLogger().logError(e);
         }
      }
      return null;
   }

   /**
    * Returns the source {@link Object} of the given {@link IEditorPart}.
    * @param editor The {@link IEditorPart} to get its source {@link Object}.
    * @return The source {@link Object} or {@code null} if {@link IEditorPart} is not supported.
    */
   protected Object getEditorSource(IEditorPart editor) {
      Object source = null;
      if (editor != null) {
         IEditorInput input = editor.getEditorInput();
         if (input instanceof IFileEditorInput) {
            source = ((IFileEditorInput)input).getFile();
         }
      }
      return source;
   }

   /**
    * When {@link DebugEvent} occurred.
    * @param events The occurred {@link DebugEvent}s.
    */
   protected void handleDebugEvents(DebugEvent[] events) {
      if (annotatedDebugTargets != null) {
         for (DebugEvent event : events) {
            if (DebugEvent.SUSPEND == event.getKind() && 
                event.getSource() instanceof ISEDebugElement) {
               ISEDebugTarget target = ((ISEDebugElement) event.getSource()).getDebugTarget();
               if (annotatedDebugTargets.contains(target)) {
                  updateAnnotations(target);
               }
            }
         }
      }
   }

   /**
    * Updates the currently shown annotations of the given {@link ISEDebugTarget}.
    * @param target The {@link ISEDebugTarget} to update its annotations.
    */
   protected void updateAnnotations(ISEDebugTarget target) {
      IEditorReference[] editorReferences = debugView.getSite().getPage().getEditorReferences();
      for (IEditorReference reference : editorReferences) {
         IEditorPart editor = reference.getEditor(false);
         updateAnnotations(editor, target);
      }
   }
   
   /**
    * Updates the {@link Annotation}s in the given {@link IEditorPart} for the given {@link ISEDebugTarget}.
    * @param editor The {@link IEditorPart} to update its {@link Annotation}s.
    * @param target The {@link ISEDebugTarget} to handle.
    */
   protected void updateAnnotations(IEditorPart editor, ISEDebugTarget target) {
      try {
         if (editor instanceof ITextEditor) {
            Object editorSource = getEditorSource(editor);
            ISESourceModel sourceModel = target.getSourceModel();
            if (sourceModel != null) {
               sourceModel.ensureCompleteness();
               ISESourceSummary summary = sourceModel.getSourceSummary(editorSource);
               if (summary != null) {
                  ITextEditor te = (ITextEditor)editor;
                  IDocumentProvider provider = te.getDocumentProvider();
                  IAnnotationModel model = provider.getAnnotationModel(editor.getEditorInput());
                  IDocument document = provider.getDocument(editor.getEditorInput());
                  if (editorSource != null) {
                     Map<Position, SymbolicallyReachedAnnotation> existingAnnotations = createAnnotationRangeMap(model);
                     // Update existing annotations and add new annotations if not already present.
                     for (ISESourceRange range : summary.getSourceRanges()) {
                        Position position = computePosition(document, range);
                        if (position != null) {
                           SymbolicallyReachedAnnotation annotation = existingAnnotations.remove(position);
                           if (annotation == null) {
                              annotation = new SymbolicallyReachedAnnotation(position);
                              annotation.setRange(target, range);
                              model.addAnnotation(annotation, position);
                           }
                           else {
                              annotation.setRange(target, range);
                           }
                        }
                     }
                     // Remove no longer needed annotations.
                     for (SymbolicallyReachedAnnotation annotation : existingAnnotations.values()) {
                        removeTarget(model, annotation, target);
                     }
                  }
               }
            }
         }
      }
      catch (DebugException e) {
         LogUtil.getLogger().logError(e);
      }
   }
   
   /**
    * Lists all {@link SymbolicallyReachedAnnotation} in the given {@link IAnnotationModel}.
    * @param model The {@link IAnnotationModel} to analyze.
    * @return The found {@link SymbolicallyReachedAnnotation}s.
    */
   protected Map<Position, SymbolicallyReachedAnnotation> createAnnotationRangeMap(IAnnotationModel model) {
      Map<Position, SymbolicallyReachedAnnotation> result = new HashMap<Position, SymbolicallyReachedAnnotation>();
      Iterator<?> iter = model.getAnnotationIterator();
      while (iter.hasNext()) {
         Object next = iter.next();
         if (next instanceof SymbolicallyReachedAnnotation) {
            SymbolicallyReachedAnnotation annotation = (SymbolicallyReachedAnnotation)next;
            result.put(annotation.getPosition(), annotation);
         }
      }
      return result;
   }

   /**
    * When a new {@link IWorkbenchPart} is opened.
    * @param part The opened {@link IWorkbenchPart}.
    */
   protected void handlePartOpened(IWorkbenchPart part) {
      if (annotatedDebugTargets != null && part instanceof IEditorPart) {
         for (ISEDebugTarget target : annotatedDebugTargets) {
            updateAnnotations((IEditorPart)part, target);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void dispose() {
      debugView.getSite().getPage().removePartListener(partListener);
      DebugPlugin.getDefault().removeDebugEventListener(debugListener);
      debugView.getViewer().removeSelectionChangedListener(selectionListener);
   }
}
