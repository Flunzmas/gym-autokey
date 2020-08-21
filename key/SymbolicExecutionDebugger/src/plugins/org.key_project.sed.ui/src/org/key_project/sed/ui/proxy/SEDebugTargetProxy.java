package org.key_project.sed.ui.proxy;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.viewers.model.TreeModelContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler;
import org.eclipse.debug.internal.ui.viewers.update.DebugTargetProxy;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.ui.IWorkbenchPart;
import org.key_project.sed.core.model.ISENode;
import org.key_project.sed.core.model.ISEDebugTarget;
import org.key_project.sed.core.model.ISEThread;
import org.key_project.sed.ui.util.LogUtil;
import org.key_project.sed.ui.util.SEDUIUtil;
import org.key_project.util.java.CollectionUtil;

/**
 * <p>
 * The {@link DebugTargetProxy} used for {@link ISEDebugTarget}s.
 * </p>
 * <p>
 * The shown content is updated as usual in the debug platform via
 * {@link IModelDelta}s. But to support multi selection the selection is set
 * after an {@link IModelDelta} was completely treated by the 
 * {@link TreeModelContentProvider} of the {@link IDebugView} via
 * {@link SEDUIUtil#selectInDebugView(IWorkbenchPart, IDebugView, List)}.
 * </p>
 * @author Martin Hentschel
 */
@SuppressWarnings("restriction")
public class SEDebugTargetProxy extends DebugTargetProxy {
   /**
    * The {@link IDebugView} in which this proxy is used.
    */
   private IDebugView debugView;
   
   /**
    * The handled {@link ISEDebugTarget}.
    */
   private final ISEDebugTarget target;
   
   /**
    * The observed {@link TreeModelContentProvider}.
    */
   private TreeModelContentProvider contentProvider;
   
   /**
    * The listener temporary installed on {@link #contentProvider} when {@link #leafsToSelect} is not {@code null}.
    */
   private final IViewerUpdateListener contentProviderListener = new IViewerUpdateListener() {
      @Override
      public void viewerUpdatesComplete() {
         handleViewerUpdatesComplete();
      }
      
      @Override
      public void viewerUpdatesBegin() {
      }
      
      @Override
      public void updateStarted(IViewerUpdate update) {
      }
      
      @Override
      public void updateComplete(IViewerUpdate update) {
      }
   };
   
   /**
    * {@code true} {@link #contentProviderObserved} observes {@link #contentProvider},
    * {@code false} {@link #contentProviderObserved} does not observe {@link #contentProvider}.
    */
   private boolean contentProviderObserved = false;
   
   /**
    * The {@link ISENode}s to select.
    */
   private List<ISENode> leafsToSelect;

   /**
    * Constructor.
    * @param target The {@link ISEDebugTarget} to handle.
    */
   public SEDebugTargetProxy(ISEDebugTarget target) {
      super(target);
      this.target = target;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void init(IPresentationContext context) {
      super.init(context);
      if (context != null) {
         IWorkbenchPart part = context.getPart();
         if (part instanceof IDebugView) {
            debugView = (IDebugView)part;
            contentProvider = SEDUIUtil.getContentProvider(debugView);
         }
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public synchronized void dispose() {
      deinstallContentProviderListener();
      super.dispose();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected DebugEventHandler[] createEventHandlers() {
      return new DebugEventHandler[] { new SEDebugTargetEventHandler(this), 
                                       new SEThreadEventHandler(this) 
                                      };
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected ModelDelta getNextSuspendedThreadDelta(IThread currentThread, boolean reverse) {
      ModelDelta delta = super.getNextSuspendedThreadDelta(currentThread, reverse);
      try {
         if (delta == null) {
            ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
            delta = new ModelDelta(manager, IModelDelta.NO_CHANGE);
            List<ISENode> leafsToSelect = collectLeafsToSelect(target);
            setLeafsToSelect(leafsToSelect);
            fillModelDeltaToUpdateTarget(manager, delta, IModelDelta.EXPAND);
         }
      }
      catch (DebugException e) {
         LogUtil.getLogger().logError(e);
      }
      return delta;
   }
   
   /**
    * Collects all leafs on all {@link ISEThread}s of the given {@link ISEDebugTarget}.
    * @param target The {@link ISEDebugTarget} to collect its leaf.
    * @return All leafs collected via {@link ISEThread#getLeafsToSelect()}.
    * @throws DebugException Occurred Exceptions.
    */
   protected List<ISENode> collectLeafsToSelect(ISEDebugTarget target) throws DebugException {
      List<ISENode> leafsToSelect = new LinkedList<ISENode>();
      for (ISEThread thread : target.getSymbolicThreads()) {
         CollectionUtil.addAll(leafsToSelect, thread.getLeafsToSelect());
      }
      return leafsToSelect;
   }
   
   /**
    * Fills the given {@link ModelDelta} to update an {@link ISEDebugTarget}.
    * @param manager The {@link ILaunchManager} to use.
    * @param delta The {@link ModelDelta} to fill.
    * @param targetFlags The flags to use for the {@link ISEDebugTarget}.
    * @param leafsToSelect The {@link ISENode}s to select.
    * @throws DebugException Occurred Exceptions.
    */
   protected void fillModelDeltaToUpdateTarget(ILaunchManager manager,
                                               ModelDelta delta, 
                                               int targetFlags) throws DebugException {
      // Add launch
      ILaunch launch = target.getLaunch();
      int launchIndex = indexOf(manager.getLaunches(), launch);
      ModelDelta node = delta.addNode(launch, launchIndex, IModelDelta.NO_CHANGE, launch.getChildren().length);
      // Add target
      int targetIndex = indexOf(launch.getChildren(), target);
      node = node.addNode(target, targetIndex, targetFlags, target.getSymbolicThreads().length);
   }
   
   /**
    * Fires an {@link IModelDelta} and defines the leafs to select.
    * @param delta The {@link IModelDelta} to fire.
    * @param leafsToSelect The leafs to select.
    */
   public void fireModelChangedWithMultiSelect(IModelDelta delta, List<ISENode> leafsToSelect) {
      setLeafsToSelect(leafsToSelect);
      fireModelChanged(delta);
   }

   /**
    * Defines the leafs to select.
    * @param leafsToSelect The new leafs to select.
    */
   public void setLeafsToSelect(List<ISENode> leafsToSelect) {
      this.leafsToSelect = leafsToSelect;
      if (leafsToSelect != null) {
         installContentProviderListener();
      }
      else {
         deinstallContentProviderListener();
      }
   }

   /**
    * Adds {@link #contentProviderListener} from {@link #contentProvider} if required and possible.
    */
   private synchronized void installContentProviderListener() {
      if (!contentProviderObserved && contentProvider != null && debugView != null) {
         contentProvider.addViewerUpdateListener(contentProviderListener);
         contentProviderObserved = true;
      }
   }

   /**
    * Removes {@link #contentProviderListener} from {@link #contentProvider} if required.
    */
   private synchronized void deinstallContentProviderListener() {
      if (contentProviderObserved) {
         contentProvider.removeViewerUpdateListener(contentProviderListener);
         contentProviderObserved = false;
      }
   }

   /**
    * When an {@link IModelDelta} is completely treated.
    */
   protected void handleViewerUpdatesComplete() {
      selectLeafsToSelectInDebugView();
   }
   
   /**
    * Selects the defined leafs to select in the {@link IDebugView}.
    */
   protected void selectLeafsToSelectInDebugView() {
      if (!CollectionUtil.isEmpty(leafsToSelect)) {
         final List<ISENode> toSelect = leafsToSelect;
         setLeafsToSelect(null);
         debugView.getSite().getShell().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
               SEDUIUtil.selectInDebugView(debugView, debugView, toSelect);
            }
         });
      }
   }
}
