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

package org.key_project.util.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.ui.workbench.renderers.swt.HandledContributionItem;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.ArrayResult;
import org.eclipse.swtbot.swt.finder.results.BoolResult;
import org.eclipse.swtbot.swt.finder.results.IntResult;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.results.WidgetResult;
import org.eclipse.swtbot.swt.finder.utils.MessageFormat;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarPushButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarRadioButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarSeparatorButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarToggleButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.key_project.util.eclipse.Logger;
import org.key_project.util.eclipse.WorkbenchUtil;
import org.key_project.util.eclipse.setup.SetupStartup;
import org.key_project.util.helper.HelperClassForUtilityTests;
import org.key_project.util.java.ArrayUtil;
import org.key_project.util.java.IFilter;
import org.key_project.util.java.IOUtil;
import org.key_project.util.java.ObjectUtil;
import org.key_project.util.java.StringUtil;
import org.key_project.util.java.thread.AbstractRunnableWithResult;
import org.key_project.util.java.thread.IRunnableWithResult;
import org.key_project.util.jdt.JDTUtil;
import org.key_project.util.test.Activator;
import org.key_project.util.test.util.internal.ContextMenuHelper;

/**
 * Provides static methods that make testing easier.
 * @author Martin Hentschel
 */
@SuppressWarnings("restriction")
public class TestUtilsUtil {
   /**
    * Forbid instances.
    */
   private TestUtilsUtil() {
   }

   /**
    * Closes the welcome view if it is opened. Otherwise nothing is done.
    */
   public static void closeWelcomeView() {
      UIThreadRunnable.syncExec(new VoidResult() {
         @Override
         public void run() {
            WorkbenchUtil.closeWelcomeView();
         }
      });
   }
   
   /**
    * Creates a {@link Logger} instance for testing.
    * @return The {@link Logger} instance for testing.
    */
   public static Logger createLogger() {
      return new Logger(Activator.getDefault(), Activator.PLUGIN_ID);
   }
   
   /**
    * Creates a new project and makes sure that it not already exists.
    * @param name The project name.
    * @return The created and opened project.
    * @throws CoreException Occurred Exception.
    */
   public static IProject createProject(String name) {
      try {
         TestCase.assertNotNull(name);
         IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
         if (!project.exists()) {
            project.create(null);
         }
         else {
            TestCase.fail("Project \"" + name + "\" already exists.");
         }
         if (!project.isOpen()) {
            project.open(null);
         }
         return project;
      }
      catch (CoreException e) {
         e.printStackTrace();
         TestCase.fail();
         return null;
      }
   }

   /**
    * Creates a new {@link IJavaProject} that is an {@link IProject} with
    * a JDT nature.
    * @param name The project name.
    * @return The created {@link IJavaProject}.
    * @throws CoreException Occurred Exception.
    * @throws InterruptedException Occurred Exception.
    */
   public static IJavaProject createJavaProjectNoBinSourceFolders(String name) throws CoreException, InterruptedException {
      final IProject project = createProject(name);
      final IJavaProject javaProject = JavaCore.create(project); 
      Result<Exception> run = new Result<Exception>() {
         @Override
         public Exception run() {
            try {
               JavaCapabilityConfigurationPage page = new JavaCapabilityConfigurationPage();
               IClasspathEntry[] entries = new IClasspathEntry[] {JavaCore.newSourceEntry(project.getFullPath())};
               entries = ArrayUtil.addAll(entries, JDTUtil.getDefaultJRELibrary());
               page.init(javaProject, project.getFullPath(), entries, false);
               page.configureJavaProject(null);
               return null;
            }
            catch (Exception e) {
               return e;
            }
         }
      };
      Exception exception = UIThreadRunnable.syncExec(run);
      if (exception instanceof CoreException) {
         throw (CoreException) exception;
      }
      else if (exception instanceof InterruptedException) {
         throw (InterruptedException) exception;
      }
      else if (exception != null) {
         throw new CoreException(new Logger(Activator.getDefault(), Activator.PLUGIN_ID).createErrorStatus(exception));
      }
      return javaProject;
   }

   /**
    * Creates a new {@link IJavaProject} that is an {@link IProject} with a JDT nature.
    * @param name The project name.
    * @return The created {@link IJavaProject}.
    * @throws CoreException Occurred Exception.
    * @throws InterruptedException Occurred Exception.
    */
   public static IJavaProject createJavaProject(String name) throws CoreException, InterruptedException {
      return JDTUtil.createJavaProject(name);
   }
   
   /**
    * Creates a new {@link IJavaProject} that is an {@link IProject} with a JDT nature. 
    * @param name The project name.
    * @param sourceFolderNames The name of the project source folders.
    * @return The created {@link IJavaProject}.
    * @throws CoreException Occurred Exception.
    * @throws InterruptedException Occurred Exception.
    */
   public static IJavaProject createJavaProject(String name, String... sourceFolderNames) throws CoreException, InterruptedException {
      return JDTUtil.createJavaProject(name, JDTUtil.getOutputFolderName(), sourceFolderNames);
   }

   /**
    * Creates a new folder and makes sure that it not already exists.
    * @param parent The parent.
    * @param name The folder name.
    * @return The created folder.
    */
   public static IFolder createFolder(IContainer parent, String name) {
      try {
         TestCase.assertNotNull(parent);
         TestCase.assertTrue(parent.exists());
         TestCase.assertNotNull(name);
         IFolder folder = parent.getFolder(new Path(name));
         if (!folder.exists()) {
            folder.create(true, true, null);
         }
         else {
            TestCase.fail("Folder \"" + name + "\" already exists in \"" + parent.getFullPath().toString() + "\".");
         }
         return folder;
      }
      catch (CoreException e) {
         e.printStackTrace();
         TestCase.fail();
         return null;
      }
   }

   /**
    * Creates a new file and makes sure that it not already exists.
    * @param parent The parent container.
    * @param name The name of the file.
    * @param content The content to set in the file.
    * @return The created file.
    */
   public static IFile createFile(IContainer parent, String name, String content) {
      try {
         TestCase.assertNotNull(parent);
         TestCase.assertTrue(parent.exists());
         TestCase.assertNotNull(name);
         TestCase.assertNotNull(content);
         IFile file = parent.getFile(new Path(name));
         if (!file.exists()) {
            file.create(new ByteArrayInputStream(content.getBytes()), true, null);
         }
         else {
            TestCase.fail("File \"" + name + "\" already exists in \"" + parent.getFullPath().toString() + "\".");
         }
         return file;
      }
      catch (CoreException e) {
         e.printStackTrace();
         TestCase.fail();
         return null;
      }
   }

   /**
    * Closes the welcome view if it is opened. Otherwise nothing is done.
    * @param bot The {@link SWTWorkbenchBot} to use.
    */
   public static void closeWelcomeView(SWTWorkbenchBot bot) {
      List<SWTBotView> views = bot.views();
      for (SWTBotView view : views) {
         if ("Welcome".equals(view.getTitle())) {
            view.close();
         }
      }
   }

   /**
    * Returns the project explorer view or its JDT version package explorer.
    * @param bot The {@link SWTWorkbenchBot} to use.
    * @return The found {@link SWTBotView}.
    */
   public static SWTBotView getProjectExplorer(SWTWorkbenchBot bot) {
      SWTBotView viewBot = null;
      try {
         viewBot = bot.viewById(JavaUI.ID_PACKAGES);
         viewBot.show();
      }
      catch (WidgetNotFoundException e) {
         viewBot = bot.viewById(IPageLayout.ID_PROJECT_EXPLORER);
         viewBot.show();
      }
      return viewBot;
   }
   
   /**
    * Selects the project explorer view and the defined path.
    * @param bot The {@link SWTBotTree} to find the package explorer view.
    * @param toSelects The path to select.
    * @return The selected element.
    */
   public static SWTBotTreeItem selectInProjectExplorer(SWTWorkbenchBot bot, String... toSelects) {
      SWTBotView viewBot = getProjectExplorer(bot);
      return selectInTree(viewBot.bot().tree(), toSelects);
   }

   /**
    * Selects the element path in the tree.
    * @param treeBot The {@link SWTBotTree} to select in.
    * @param toSelects The path to select.
    * @return The selected element.
    */
   public static SWTBotTreeItem selectInTree(SWTBotTree treeBot, String... toSelects) {
      SWTBotTreeItem lastItem = null;
      for (String segment : toSelects) {
         if (lastItem == null) {
            lastItem = treeBot.getTreeItem(segment);
            if (!lastItem.isExpanded()) {
               lastItem.expand();
            }
         }
         else {
            lastItem = lastItem.getNode(segment);
            if (!lastItem.isExpanded()) {
               lastItem.expand();
            }
         }
      }
      treeBot.select(lastItem);
      return lastItem;
   }

   /**
    * <p>
    * Collects all leaf nodes in the subtree starting at the given {@link SWTBotTreeItem}.
    * </p>
    * </p>
    * <b>Attention:</b> Lazy provider are also supported. For this reason
    * is the selection changed and each node in the subtree expanded.
    * <p>
    * @param item The {@link SWTBotTreeItem} to start collecting.
    * @return The found leaf {@link SWTBotTreeItem}s.
    */
   public static List<SWTBotTreeItem> collectLeafs(SWTBotTreeItem item) {
      List<SWTBotTreeItem> result = new LinkedList<SWTBotTreeItem>();
      internalCollectLeafs(result, item);
      return result;
   }

   /**
    * Internal methods to collect leaf items recursive of {@link #collectLeafs(SWTBotTreeItem)}.
    * @param leafItems The result {@link List} to fill.
    * @param item The current item.
    */
   private static void internalCollectLeafs(List<SWTBotTreeItem> leafItems, SWTBotTreeItem item) {
      if (item != null) {
         if (getTreeItemData(item) == null) {
            item.select();
         }
         if (!item.isExpanded()) {
            item.expand();
         }
         SWTBotTreeItem[] children = item.getItems();
         if (ArrayUtil.isEmpty(children)) {
            leafItems.add(item);
         }
         else {
            for (SWTBotTreeItem child : children) {
               internalCollectLeafs(leafItems, child);
            }
         }
      }
   }

   /**
    * Executes a click in the main menu.
    * @param bot The {@link SWTWorkbenchBot} to use.
    * @param menuItems The menu path to click on.
    * @return The menu on that the click was executed.
    */
   public static SWTBotMenu menuClick(SWTWorkbenchBot bot, String... menuItems) {
      SWTBotMenu lastMenu = null;
      for (String menuItem : menuItems) {
         if (lastMenu == null) {
            lastMenu = bot.menu(menuItem);
         }
         else {
            lastMenu = lastMenu.menu(menuItem);
         }
      }
      if (lastMenu != null) {
         lastMenu.click();
         return lastMenu;
      }
      else {
         return null;
      }
   }

   /**
    * Opens the preference page in the preference dialog.
    * @param bot The {@link SWTBot} to use.
    * @param preferencePagePath The path to the preference page to open.
    * @return The opened preference dialog shell.
    */
   public static SWTBotShell openPreferencePage(SWTWorkbenchBot bot, String... preferencePagePath) {
      // Open preference dialog (Usage of TestUtilsUtil.menuClick(bot, "Window", "Preferences") is not possible because Mac OS has entry in special menu)
      UIThreadRunnable.asyncExec(new VoidResult() {
         @Override
         public void run() {
            Shell shell = WorkbenchUtil.getActiveShell();
            PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell, null, null, null);
            dialog.open();
         }
      });
      // Open preference page
      SWTBotShell shell = bot.shell("Preferences");
      TestUtilsUtil.selectInTree(shell.bot().tree(), preferencePagePath);
      return shell;
   }

   /**
    * Opens the properties page in the properties dialog of the given {@link IResource}.
    * @param bot The {@link SWTBot} to use.
    * @param resource The {@link IResource} to open its properties page.
    * @param preferencePagePath The path to the preference page to open.
    * @return The opened preference dialog shell.
    */
   public static SWTBotShell openPropertiesPage(SWTWorkbenchBot bot, final IResource resource, String... preferencePagePath) {
      assertNotNull(resource);
      return openPropertiesPage(bot, resource, resource.getName(), preferencePagePath);
   }

   /**
    * Opens the properties page in the properties dialog of the given {@link IAdaptable}.
    * @param bot The {@link SWTBot} to use.
    * @param element The {@link IAdaptable} to open its properties page.
    * @param elementName The name of the element.
    * @param preferencePagePath The path to the preference page to open.
    * @return The opened preference dialog shell.
    */
   public static SWTBotShell openPropertiesPage(SWTWorkbenchBot bot, final IAdaptable element, String elementName, String... preferencePagePath) {
      // Open preference dialog
      UIThreadRunnable.asyncExec(new VoidResult() {
         @Override
         public void run() {
            Shell shell = WorkbenchUtil.getActiveShell();
            PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(shell, element, null, null, null);
            dialog.open();
         }
      });
      // Open preference page
      SWTBotShell shell = bot.shell("Properties for " + elementName);
      TestUtilsUtil.selectInTree(shell.bot().tree(), preferencePagePath);
      return shell;
   }
   
   /**
    * Opens an editor for the given file thread save.
    * @param file The file to open.
    * @return The opened {@link IEditorPart}.
    */
   public static IEditorPart openEditor(final IFile file) throws Exception {
      ResultWithException<IEditorPart> run = new ResultWithException<IEditorPart>() {
         @Override
         public IEditorPart doRun() throws Exception {
            return WorkbenchUtil.openEditor(file);
         }
      };
      IEditorPart result = ResultWithException.syncExec(run);
      assertNotNull(result);
      return result;
   }
   
   /**
    * A {@link Result} which allows to throw {@link Exception}s.
    * @author Martin Hentschel
    */
   public abstract static class ResultWithException<T> implements Result<T> {
      /**
       * The caught exception.
       */
      private Exception exception;
      
      /**
       * {@inheritDoc}
       */
      @Override
      public T run() {
         try {
            return doRun();
         }
         catch (Exception e) {
            exception = e;
            return null;
         }
      }

      /**
       * Performs the operation.
       * @return The result.
       * @throws Exception The occurred exception.
       */
      public abstract T doRun() throws Exception;
      
      /**
       * Returns the caught exception.
       * @return The caught exception.
       */
      public Exception getException() {
         return exception;
      }
      
      /**
       * Executes the given {@link ResultWithException} synchronously in the UI thread.
       * @param run The {@link ResultWithException} to execute.
       * @return The result.
       * @throws Exception The occurred exception.
       */
      public static <T> T syncExec(ResultWithException<T> run) throws Exception {
         T result = UIThreadRunnable.syncExec(run);
         if (run.getException() != null) {
            throw run.getException();
         }
         else {
            return result;
         }
      }
   }

   public static Object getObjectInTree(SWTBotTree treeBot, String... toSelects) {
      final SWTBotTreeItem item = selectInTree(treeBot, toSelects);
      Result<Object> run = new Result<Object>() {
         @Override
         public Object run() {
            return item.widget.getData();
         }
      };
      return UIThreadRunnable.syncExec(run);
   }
   
   /**
    * Blocks the current {@link Thread} until the given {@link Job} has finished.
    * @param job The {@link Job} to wait for.
    */
   public static void waitForJob(Job job) {
      if (job != null) {
         while (job.getState() != Job.NONE) {
            sleep(100);
         }
         TestCase.assertEquals(Job.NONE, job.getState());
      }
   }
   
   /**
    * Sleeps the current {@link Thread} for the given time.
    * @param time The time to sleep.
    */
   public static void sleep(int time) {
      try {
         Thread.sleep(time);
      }
      catch (InterruptedException e) {
         // Nothing to do.
      }
   }

   /**
    * Returns the specified {@link IMethod}.
    * @param javaProject The {@link IJavaProject} that contains the source code.
    * @param typeName The type name.
    * @param methodName The method name.
    * @param parameters The method parameters.
    * @return The found {@link IMethod}.
    * @throws JavaModelException Occurred Exception.
    */
   public static IMethod getJdtMethod(IJavaProject javaProject,
                                      String typeName, 
                                      String methodName, 
                                      String... parameters) throws JavaModelException {
       IType type = javaProject.findType(typeName);
       assertNotNull(type);
       IMethod method = type.getMethod(methodName, parameters);
       assertNotNull(method);
       return method;
   }

   /**
    * Creates an {@link ICondition} that makes sure that the given 
    * {@link SWTBotTree} has a selection.
    * @param tree The {@link SWTBotTree} to check.
    * @return The created {@link ICondition}.
    */
   public static ICondition hasSelection(final SWTBotTree tree) {
      return new DefaultCondition() {
         @Override
         public boolean test() throws Exception {
            BoolResult result = new BoolResult() {
               @Override
               public Boolean run() {
                  return tree.widget.getSelectionCount() >= 1;
               }
            };
            return UIThreadRunnable.syncExec(result);
         }
        
         @Override
         public String getFailureMessage() {
            return "The component " + tree + " has no selection.";
         }
      };
   }
   
   /**
    * Creates a folder.
    * @param folder The folder to create.
    * @return The created folder.
    */
   public static File createFolder(File folder) {
       return HelperClassForUtilityTests.createFolder(folder);
   }
   
   /**
    * Creates a file
    * @param file The file to create.
    * @param content The content to write to file.
    * @return The created file.
    * @throws IOException Occurred Exception.
    */
   public static File createFile(File file, String content) throws IOException {
       return HelperClassForUtilityTests.createFile(file, content);
   }
   
   /**
    * Possible method treatments that are configurable inside the 
    * "Proof Search Strategy" tab.
    * @author Martin Hentschel
    */
   public enum MethodTreatment {
      /**
       * Expand.
       */
      EXPAND,
      
      /**
       * Contracts
       */
      CONTRACTS
   }

   /**
    * Makes sure that the given {@link SWTBotTable} without columns
    * contains the correct rows.
    * @param table The {@link SWTBotTable} to test.
    * @param rowValues The expected row values.
    */
   public static void assertTableRows(SWTBotTable table, String... rowValues) {
       TestCase.assertEquals(rowValues.length, table.rowCount());
       for (int i = 0; i < table.rowCount(); i++) {
           SWTBotTableItem item = table.getTableItem(i);
           TestCase.assertEquals(rowValues[i], item.getText());
       }
   }

   /**
    * Waits until the build process has finished. 
    */
   public static void waitForBuild() {
       IJobManager manager = Job.getJobManager();
       Job[] jobs = manager.find(ResourcesPlugin.FAMILY_AUTO_BUILD);
       if (jobs != null) {
           for (Job job : jobs) {
               waitForJob(job);
           }
       }
   }

   /**
    * Waits until all {@link Job}s have finished.
    */
   public static void waitForJobs() {
       IJobManager manager = Job.getJobManager();
       Job job;
       while ((job = manager.currentJob()) != null) {
           waitForJob(job);
       }
   }

   /**
    * Expands all elements in the given {@link SWTBotTree}
    * @param tree The {@link SWTBotTree} to expand.
    */
   public static void expandAll(SWTBotTree tree) {
      SWTBotTreeItem[] items = tree.getAllItems();
      for (SWTBotTreeItem item : items) {
         expandAll(item);
      }
   }

   /**
    * Expands the given {@link SWTBotTreeItem} and all of his children.
    * @param tree The {@link SWTBotTreeItem} to expand.
    */
   public static void expandAll(SWTBotTreeItem item) {
      if (!item.widget.isDisposed()) {
         if (!item.isExpanded()) {
            item.expand();
         }
         SWTBotTreeItem[] children = item.getItems();
         for (SWTBotTreeItem child : children) {
            expandAll(child);
         }
      }
   }

   /**
    * Returns {@link TreeItem#getData()}.
    * @param item The {@link SWTBotTreeItem} to return from.
    * @return The data {@link Object}.
    */
   public static Object getTreeItemData(final SWTBotTreeItem item) {
      IRunnableWithResult<Object> run = new AbstractRunnableWithResult<Object>() {
         @Override
         public void run() {
            setResult(item.widget.getData());
         }
      };
      item.widget.getDisplay().syncExec(run);
      return run.getResult();
   }

   /**
    * Returns {@link TableItem#getData()}.
    * @param item The {@link SWTBotTableItem} to return from.
    * @return The data {@link Object}.
    */   
   public static Object getTableItemData(final SWTBotTableItem item) {
      IRunnableWithResult<Object> run = new AbstractRunnableWithResult<Object>() {
         @Override
         public void run() {
            setResult(item.widget.getData());
         }
      };
      item.widget.getDisplay().syncExec(run);
      return run.getResult();
   }

   /**
    * Selects the item in the tree that is defined by the path indices.
    * @param debugTree The {@link SWTBotTree} to select a {@link SWTBotTreeItem} in.
    * @param indexPathToItem The path to the item to select which consists of the path indices.
    * @return The selected {@link SWTBotTreeItem}.
    */
   public static SWTBotTreeItem selectInTree(SWTBotTree debugTree, int... indexPathToItem) {
      TestCase.assertNotNull(debugTree);
      SWTBotTreeItem parent = null;
      for (int index : indexPathToItem) {
         SWTBotTreeItem[] items; 
         if (parent == null) {
            items = debugTree.getAllItems();
         }
         else {
            items = parent.getItems();
         }
         TestCase.assertTrue(index >= 0);
         TestCase.assertTrue("Index " + index + " is not smaler as max index " + items.length, index < items.length);
         parent = items[index];
      }
      TestCase.assertNotNull(parent);
      parent.select();
      return parent;
   }

   /**
    * Waits until an active editor is available.
    * @param bot The {@link SWTWorkbenchBot} to use.
    * @return The active {@link SWTBotEditor}.
    */
   public static SWTBotEditor waitForEditor(SWTWorkbenchBot bot) {
      WaitForEditorCondition wait = new WaitForEditorCondition();
      bot.waitUntil(wait);
      return wait.getEditor();
   }
   
   /**
    * Waits until an active editor is available.
    * @author Martin Hentschel
    */
   private static class WaitForEditorCondition implements ICondition {
      /**
       * The used {@link SWTBot}.
       */
      private SWTBot bot;

      /**
       * The found active editor.
       */
      private SWTBotEditor editor;
      
      /**
       * {@inheritDoc}
       */
      @Override
      public boolean test() throws Exception {
         if (bot instanceof SWTWorkbenchBot) {
            editor = ((SWTWorkbenchBot)bot).activeEditor();
         }
         return editor != null;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void init(SWTBot bot) {
         this.bot = bot;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public String getFailureMessage() {
         return "No editor available.";
      }

      /**
       * The found active editor.
       * @return The found editor.
       */
      public SWTBotEditor getEditor() {
         return editor;
      }
   }

   /**
    * Activates the given {@link SWTBotView}.
    * @param view The {@link SWTBotView} to activate.
    */
   public static void activateView(final SWTBotView view) {
      TestCase.assertNotNull(view);
      UIThreadRunnable.syncExec(new VoidResult() {
         @Override
         public void run() {
            view.getReference().getPage().activate(view.getReference().getPart(true));
         }
      });
      TestCase.assertTrue(view.isActive());
   }

   /**
    * Opens a view with the given ID in the active {@link IWorkbenchPage}.
    * @param viewId The view ID to open.
    * @return The opened {@link IViewPart}.
    * @throws Exception Occurred Exception.
    */
   public static IViewPart openView(final String viewId) throws Exception {
      ResultWithException<IViewPart> run = new ResultWithException<IViewPart>() {
         @Override
         public IViewPart doRun() throws Exception {
            return WorkbenchUtil.openView(viewId);
         }
      };
      IViewPart result = ResultWithException.syncExec(run);
      TestCase.assertNotNull(result);
      return result;
   }

   /**
    * Closes a view with the given ID in the active {@link IWorkbenchPage}.
    * @param viewId The ID of the view to close.
    * @return {@code true} view was closed, {@code false} view was not opened.
    */
   public static boolean closeView(final String viewId) {
      BoolResult run = new BoolResult() {
         @Override
         public Boolean run() {
            IViewPart view = WorkbenchUtil.findView(viewId);
            if (view != null) {
               WorkbenchUtil.closeView(view);
               return Boolean.TRUE;
            }
            else {
               return Boolean.FALSE;
            }
         }
      };
      return UIThreadRunnable.syncExec(run);
   }

   /**
    * Returns the active perspective of the active {@link IWorkbenchPage}.
    * @return The active perspective.
    */
   public static IPerspectiveDescriptor getActivePerspective() {
      Result<IPerspectiveDescriptor> run = new Result<IPerspectiveDescriptor>() {
         @Override
         public IPerspectiveDescriptor run() {
            IWorkbenchPage page = WorkbenchUtil.getActivePage();
            if (page != null) {
               return page.getPerspective();
            }
            else {
               return null;
            }
         }
      };
      return UIThreadRunnable.syncExec(run);
   }

   /**
    * Opens the given perspective in the active {@link IWorkbenchPage}.
    * @param perspectiveDescriptor The perspective to open.
    */
   public static void openPerspective(final IPerspectiveDescriptor perspectiveDescriptor) {
      TestCase.assertNotNull(perspectiveDescriptor);
      UIThreadRunnable.syncExec(new VoidResult() {
         @Override
         public void run() {
            IWorkbenchPage page = WorkbenchUtil.getActivePage();
            if (page != null) {
               page.setPerspective(perspectiveDescriptor);
            }
         }
      });
   }

   /**
    * Returns the {@link SWTBotView} for the properties view.
    * @param bot The {@link SWTWorkbenchBot} to use.
    * @return The {@link SWTBotView}.
    */
   public static SWTBotView getPropertiesView(SWTWorkbenchBot bot) {
      return bot.viewById(IPageLayout.ID_PROP_SHEET);
   }

   /**
    * Returns the {@link SWTBotView} for the outline view.
    * @param bot The {@link SWTWorkbenchBot} to use.
    * @return The {@link SWTBotView}.
    */
   public static SWTBotView getOutlineView(SWTWorkbenchBot bot) {
      return bot.viewById(IPageLayout.ID_OUTLINE);
   }

   /**
    * Closes the given editor thread save.
    * @param editor The {@link IEditorPart} to close.
    * @param save Save changes?
    */
   public static void closeEditor(final IEditorPart editor, final boolean save) {
      if (editor != null) {
         Shell shell = editor.getEditorSite().getShell();
         if (!shell.isDisposed()) {
            shell.getDisplay().syncExec(new Runnable() {
               @Override
               public void run() {
                  WorkbenchUtil.closeEditor(editor, save);
               }
            });
         }
      }
   }
   
   /**
    * <p>
    * Performs a click on a menu item of the context menu of the given {@link AbstractSWTBot}.
    * </p>
    * <p>
    * This utility method solves some SWTBot issues on context menu like missing
    * support for structured context menus and widget is disposed exceptions o
    * items provided to context menus via extension points.
    * </p>
    * @param bot The {@link AbstractSWTBot} to execute a context menu click on it.
    * @param texts The path to the context menu item to click on.
    */
   public static void clickContextMenu(AbstractSWTBot<?> bot, String... texts) {
      clickContextMenu(bot, 0, 0, texts);
   }
   
   /**
    * <p>
    * Performs a click on a menu item of the context menu of the given {@link AbstractSWTBot}.
    * </p>
    * <p>
    * This utility method solves some SWTBot issues on context menu like missing
    * support for structured context menus and widget is disposed exceptions o
    * items provided to context menus via extension points.
    * </p>
    * @param bot The {@link AbstractSWTBot} to execute a context menu click on it.
    * @param x The x-coordinate on the {@link AbstractSWTBot} to open context menu at.
    * @param x The y-coordinate on the {@link AbstractSWTBot} to open context menu at.
    * @param texts The path to the context menu item to click on.
    */
   public static void clickContextMenu(AbstractSWTBot<?> bot, int x, int y, String... texts) {
      ContextMenuHelper.clickContextMenu(bot, x, y, texts);
   }
   
   /**
    * Waits until the selection of the given {@link SWTBotTree} contains the given element. 
    * @param bot The {@link SWTWorkbenchBot} to use.
    * @param tree The {@link SWTBotTree} to check selection.
    * @param element The element to check if it is contained in the selection of the tree.
    */
   public static void waitUntilSelected(SWTWorkbenchBot bot, SWTBotTree tree, Object element) {
      WaitForSelectedCondition condition = new WaitForSelectedCondition(tree, element);
      bot.waitUntil(condition);
   }
   
   /**
    * {@link ICondition} to check if the given element is selected.
    * @author Martin Hentschel
    */
   private static class WaitForSelectedCondition implements ICondition {
      /**
       * The {@link SWTBotTree} to check selection.
       */
      private SWTBotTree tree;
      
      /**
       * The element to check if it is contained in the selection of {@link #tree}.
       */
      private Object element;

      /**
       * Constructor.
       * @param tree The {@link SWTBotTree} to check selection.
       * @param element The element to check if it is contained in the selection of the tree.
       */
      public WaitForSelectedCondition(SWTBotTree tree, Object element) {
         this.tree = tree;
         this.element = element;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean test() throws Exception {
         return UIThreadRunnable.syncExec(new BoolResult() {
            @Override
            public Boolean run() {
               boolean containsElement = false;
               TreeItem[] selection = tree.widget.getSelection();
               if (selection != null) {
                  int i = 0;
                  while (!containsElement && i < selection.length) {
                     if (ObjectUtil.equals(selection[i].getData(), element)) {
                        containsElement = true;
                     }
                     i++;
                  }
               }
               return containsElement;
            }
         });
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void init(SWTBot bot) {
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public String getFailureMessage() {
         return "Element \"" + element + "\" is not selected.";
      }
   }

   /**
    * Clicks on the button with the given text provided by the given
    * {@link SWTBot} directly without any other events.
    * @param bot The {@link SWTBot} which provides the button.
    * @param buttonText The text of the button to click directly on.
    */
   public static void clickDirectly(SWTBot bot, String buttonText) {
      assertNotNull(bot);
      assertNotNull(buttonText);
      SWTBotButton button = bot.button(buttonText);
      clickDirectly(button);
   }
   
   /**
    * Clicks on the given {@link SWTBotButton} directly without
    * any other events.
    * @param button The {@link SWTBotButton} to perform a direct click on.
    */
   public static void clickDirectly(SWTBotButton button) {
      assertNotNull(button);
      new SWTBotSimpleClickButton(button.widget).click();
   }
   
   /**
    * Utility method used in {@link TestUtilsUtil#clickDirectly(SWTBotButton)}
    * to perform a direct click without other events.
    * @author Martin Hentschel
    */
   private static final class SWTBotSimpleClickButton extends SWTBotButton {
      /**
       * Constructor.
       * @param button The {@link Button}.
       */
      public SWTBotSimpleClickButton(Button button) {
         super(button);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public SWTBotButton click() {
         log.debug(MessageFormat.format("Clicking on {0}", SWTUtils.getText(widget)));
         waitForEnabled();
         notify(SWT.Selection);
         log.debug(MessageFormat.format("Clicked on {0}", SWTUtils.getText(widget)));
         return this;
      }
   }
   
   /**
    * Performs a click on the given {@link SWTBotRadio} to change the
    * selected radio {@link Button} without any additional events.
    * @param radio The {@link SWTBotRadio} to click on.
    */
   public static void clickDirectly(SWTBotRadio radio) {
      assertNotNull(radio);
      new SWTBotSimpleRadio(radio.widget).click();
   }
   
   /**
    * Utility class used in {@link TestUtilsUtil#clickDirectly(SWTBotRadio)}
    * to change the selected radio {@link Button} without any additional events.
    * @author Martin Hentschel
    */
   private static final class SWTBotSimpleRadio extends SWTBotRadio {
      /** 
       * Constructor.
       * @param radio The radio {@link Button}.
       * @throws WidgetNotFoundException Occurred Exception
       */
      public SWTBotSimpleRadio(Button radio) throws WidgetNotFoundException {
         super(radio);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public SWTBotRadio click() {
         if (isSelected()) {
            log.debug(MessageFormat.format("Widget {0} is already selected, not clicking again.", this)); //$NON-NLS-1$
            return this;
         }
         waitForEnabled();

         log.debug(MessageFormat.format("Clicking on {0}", this)); //$NON-NLS-1$

         final SWTBotSimpleRadio otherSelectedButton = otherSelectedButton();

         if (otherSelectedButton != null) {
            asyncExec(new VoidResult() {
               public void run() {
                  otherSelectedButton.widget.setSelection(false);
               }
            });
         }

         asyncExec(new VoidResult() {
            public void run() {
               widget.setSelection(true);
            }
         });
         notify(SWT.Selection);
         log.debug(MessageFormat.format("Clicked on {0}", this)); //$NON-NLS-1$
         return this;
      }
      
      /**
       * Copied code from {@link SWTBotRadio#otherSelectedButton()}.
       */
      private SWTBotSimpleRadio otherSelectedButton() {
         Button button = syncExec(new WidgetResult<Button>() {
            public Button run() {
               if (hasStyle(widget.getParent(), SWT.NO_RADIO_GROUP))
                  return null;
               Widget[] siblings = SWTUtils.siblings(widget);
               for (Widget widget : siblings) {
                  if ((widget instanceof Button) && hasStyle(widget, SWT.RADIO))
                     if (((Button) widget).getSelection())
                        return (Button) widget;
               }
               return null;
            }
         });

         if (button != null)
            return new SWTBotSimpleRadio(button);
         return null;
      }
      
   }
   
   /**
    * Clicks on the given {@link SWTBotToolbarButton} directly without
    * any other events.
    * @param button The {@link SWTBotToolbarButton} to perform a direct click on.
    */
   public static void clickDirectly(SWTBotToolbarButton button) {
      assertNotNull(button);
      new SWTBotSimpleClickToolbarButton(button.widget).click();
   }
   
   /**
    * Utility method used in {@link TestUtilsUtil#clickDirectly(SWTBotToolbarButton)}
    * to perform a direct click without other events.
    * @author Martin Hentschel
    */
   private static final class SWTBotSimpleClickToolbarButton extends SWTBotToolbarButton {
      /**
       * Constructor.
       * @param toolItem The {@link ToolItem}.
       */
      public SWTBotSimpleClickToolbarButton(ToolItem toolItem) {
         super(toolItem);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public SWTBotToolbarButton click() {
         log.debug(MessageFormat.format("Clicking on {0}", SWTUtils.getText(widget)));
         waitForEnabled();
         notify(SWT.Selection);
         log.debug(MessageFormat.format("Clicked on {0}", SWTUtils.getText(widget)));
         return this;
      }

      /**
       * {@inheritDoc}
       */
//      @Override
      public SWTBotToolbarButton click(int stateMask) {
         log.debug(MessageFormat.format("Clicking on {0}", SWTUtils.getText(widget)));
         waitForEnabled();
         notify(SWT.Selection, createSelectionEvent(stateMask));
         log.debug(MessageFormat.format("Clicked on {0}", SWTUtils.getText(widget)));
         return this;
      }
   }
   
   /**
    * Sets the cursor location in the given {@link AbstractSWTBot}.
    * @param widget The {@link AbstractSWTBot} to set cursor location in,.
    * @param x The x coordinate inside the widget to set.
    * @param y The y coordinate inside the widget to set.
    */
   public static <W extends Control> void setCursorLocation(final AbstractSWTBot<W> widget, 
                                                            final int x, 
                                                            final int y) {
      Assert.isNotNull(widget);
      // Change display location
      final Display display = widget.widget.getDisplay();
      display.syncExec(new Runnable() {
         @Override
         public void run() {
            Point point = widget.widget.toDisplay(x, y);
            display.setCursorLocation(point);
         }
      });
      // Simulate mouse move event.
      new AbstractSWTBot<W>(widget.widget) {
         public void notifyMouseMove(int x, int y) {
            Event event = createEvent();
            event.x = x;
            event.y = y;
            notify(SWT.MouseMove, event, widget);
         }
      }.notifyMouseMove(x, y);
   }

   /**
    * Closes the given {@link IViewPart}.
    * @param view The {@link IViewPart} to close.
    */
   public static void closeView(final IViewPart view) {
      assertNotNull(view);
      view.getSite().getShell().getDisplay().syncExec(new Runnable() {
         @Override
         public void run() {
            WorkbenchUtil.closeView(view);
         }
      });
   }

   /**
    * Searches an {@link IViewPart} with the given ID in the active {@link IWorkbenchPage}.
    * @param viewId The view ID to search.
    * @return The found {@link IViewPart} or {@code null} if no one was found.
    */
   public static IViewPart findView(final String viewId) {
      Result<IViewPart> run = new Result<IViewPart>() {
         @Override
         public IViewPart run() {
            return WorkbenchUtil.findView(viewId);
         }
      };
      return UIThreadRunnable.syncExec(run);
   }

   /**
    * Blocks the current thread until the workspace is initialized
    * by the {@link SetupStartup}.
    */
   public static void waitUntilWorkspaceInitialized() {
      while (!SetupStartup.isSetupDone()) {
         sleep(500);
      }
   }

   public static void openJavaPerspective() {
      openPerspective(getPerspective(JavaUI.ID_PERSPECTIVE));
   }
   
   /**
    * Returns the perspective with the given ID.
    * @param id The ID to search.
    * @return The found perspective or {@code null} if no perspective was found.
    */
   public static IPerspectiveDescriptor getPerspective(String id) {
      IPerspectiveDescriptor result = null;
      IPerspectiveDescriptor[] perspectives = PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives();
      int i = 0;
      while (result == null && i < perspectives.length) {
         if (ObjectUtil.equals(perspectives[i].getId(), id)) {
            result = perspectives[i];
         }
         i++;
      }
      return result;
   }

   /**
    * Searches the {@link SWTBotToolbarButton} with the given ID.
    * @param view The {@link SWTBotView} to search toolbar button in.
    * @param id The ID of the toolbar item to search.
    * @return The found {@link SWTBotToolbarButton}.
    * @throws Exception Occurred Exception.
    */
   public static SWTBotToolbarButton getToolbarButtonWithId(final SWTBotView view, final String id) throws Exception {
      IRunnableWithResult<IContributionItem> run = new AbstractRunnableWithResult<IContributionItem>() {
         @Override
         public void run() {
            IViewPart viewPart = view.getReference().getView(true);
            IActionBars bar = viewPart.getViewSite().getActionBars();
            IContributionItem[] items = bar.getToolBarManager().getItems();
            IContributionItem item = ArrayUtil.search(items, new IFilter<IContributionItem>() {
               @Override
               public boolean select(IContributionItem element) {
                  return ObjectUtil.equals(id, element.getId());
               }
            });
            setResult(item);
         }
      };
      view.bot().getDisplay().syncExec(run);
      if (run.getException() != null) {
         throw run.getException();
      }
      IContributionItem item = run.getResult();
      Widget widget;
      if (item instanceof HandledContributionItem) {
         widget = ((HandledContributionItem)item).getWidget();
      }
      else if (item instanceof ActionContributionItem) {
         widget = ((ActionContributionItem) item).getWidget();
      }
      else {
         throw new IllegalStateException("Unsupported item: " + item);
      }
      if (!(widget instanceof ToolItem)) {
         throw new IllegalStateException("ToolItem expected, but is: " + widget);
      }
      ToolItem toolItem = (ToolItem)widget;
      if (SWTUtils.hasStyle(toolItem, SWT.PUSH)) {
         return new SWTBotToolbarPushButton(toolItem);
      }
      else if(SWTUtils.hasStyle(toolItem, SWT.CHECK)) {
         return new SWTBotToolbarToggleButton(toolItem);
      }
      else if(SWTUtils.hasStyle(toolItem, SWT.RADIO)) {
         return new SWTBotToolbarRadioButton(toolItem);
      }
      else if(SWTUtils.hasStyle(toolItem, SWT.DROP_DOWN)) {
         return new SWTBotToolbarDropDownButton(toolItem);
      }
      else if(SWTUtils.hasStyle(toolItem, SWT.SEPARATOR)) {
         return new SWTBotToolbarSeparatorButton(toolItem);
      }
      else {
         throw new IllegalStateException("Unsupported tool item: " + toolItem);
      }
   }
   
   /**
    * Unifies all line breaks of files with the given extensions in the given {@link IProject}.
    * @param project The {@link IProject} to operate on.
    * @param fileExtensions The file extensions of files to modify.
    * @throws CoreException Occurred Exception.
    */
   public static void unifyLineBreaks(IProject project, final String... fileExtensions) throws CoreException {
      if (project != null) {
         project.accept(new IResourceVisitor() {
            @Override
            public boolean visit(IResource resource) throws CoreException {
               try {
                  if (resource instanceof IFile &&
                      ArrayUtil.contains(fileExtensions, resource.getFileExtension())) {
                     IFile file = (IFile)resource;
                     file.setContents(IOUtil.unifyLineBreaks(file.getContents()), true, true, null);
                  }
                  return true;
               }
               catch (IOException e) {
                  throw new CoreException(new Logger(Activator.getDefault(), Activator.PLUGIN_ID).createErrorStatus(e));
               }
            }
         }, IResource.DEPTH_INFINITE, IResource.FILE);
         waitForBuild();
      }
   }

   /**
    * Performs the switch into the new perspective.
    * @param bot The {@link SWTWorkbenchBot} to use.
    * @param expectedTargetPerspectiveId The expected ID of the target perspective.
    */
   public static void confirmPerspectiveSwitch(SWTWorkbenchBot bot, String expectedTargetPerspectiveId) {
      SWTBotShell switchShel = bot.shell("Confirm Perspective Switch");
      switchShel.bot().button("Yes").click();
      IPerspectiveDescriptor debugPerspective = TestUtilsUtil.getActivePerspective();
      bot.waitUntil(org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses(switchShel));
      assertEquals(expectedTargetPerspectiveId, debugPerspective.getId());
   }

   /**
    * Returns the selection of the given {@link SWTBotTree}.
    * @param tree The {@link SWTBotTree}.
    * @return The selected {@link Object}s.
    * @throws Exception Occurred Exception.
    */
   public static Object[] getSelectedObjects(final SWTBotTree tree) throws Exception {
      IRunnableWithResult<Object[]> run = new AbstractRunnableWithResult<Object[]>() {
         @Override
         public void run() {
            List<Object> result = new LinkedList<Object>();
            for (TreeItem item : tree.widget.getSelection()) {
               result.add(item.getData());
            }
            setResult(result.toArray(new Object[result.size()]));
         }
      };
      tree.widget.getDisplay().syncExec(run);
      if (run.getException() != null) {
         throw run.getException();
      }
      return run.getResult();
   }

   /**
    * Returns the selection of the given {@link SWTBotTable}.
    * @param table The {@link SWTBotTable}.
    * @return The selected {@link Object}s.
    * @throws Exception Occurred Exception.
    */
   public static Object[] getSelectedObjects(final SWTBotTable table) throws Exception {
      IRunnableWithResult<Object[]> run = new AbstractRunnableWithResult<Object[]>() {
         @Override
         public void run() {
            List<Object> result = new LinkedList<Object>();
            for (TableItem item : table.widget.getSelection()) {
               result.add(item.getData());
            }
            setResult(result.toArray(new Object[result.size()]));
         }
      };
      table.widget.getDisplay().syncExec(run);
      if (run.getException() != null) {
         throw run.getException();
      }
      return run.getResult();
   }

   /**
    * Waits until the {@link SWTBotTreeItem} is expanded.
    * @param bot The {@link SWTWorkbenchBot} to use. 
    * @param item The {@link SWTBotTreeItem} to wait for.
    */
   public static void waitUntilExpanded(SWTWorkbenchBot bot, SWTBotTreeItem item) {
      bot.waitUntil(new ExpandedCondition(item));
   }
   
   /**
    * {@link ICondition} used by {@link TestUtilsUtil#waitUntilExpanded(SWTWorkbenchBot, SWTBotTreeItem)}.
    * @author Martin Hentschel
    */
   private static class ExpandedCondition implements ICondition {
      /**
       * The {@link SWTBotTreeItem} to wait for.
       */
      private final SWTBotTreeItem item;
      
      /**
       * Constructor.
       * @param item The {@link SWTBotTreeItem} to wait for.
       */
      public ExpandedCondition(SWTBotTreeItem item) {
         this.item = item;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean test() throws Exception {
         return item.isExpanded();
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void init(SWTBot bot) {
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public String getFailureMessage() {
         return "Item " + item + " is not expanded.";
      }
   }

   /**
    * Waits until the item is deselected.
    * @param bot The {@link SWTBot} to use.
    * @param item The {@link SWTBotTreeItem} to wait for a selection change. 
    */
   public static void waitUntilDeselected(SWTBot bot, SWTBotTreeItem item) {
      bot.waitUntil(new DeslectedCondition(item));
   }
   
   /**
    * {@link ICondition} used by {@link TestUtilsUtil#waitUntilDeselected(SWTBot, SWTBotTreeItem)}.
    * @author Martin Hentschel
    */
   private static class DeslectedCondition implements ICondition {
      /**
       * The {@link SWTBotTreeItem} to wait for.
       */
      private final SWTBotTreeItem item;
      
      /**
       * Constructor.
       * @param item The {@link SWTBotTreeItem} to wait for.
       */
      public DeslectedCondition(SWTBotTreeItem item) {
         this.item = item;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean test() throws Exception {
         TreeItem[] selection = UIThreadRunnable.syncExec(new ArrayResult<TreeItem>() {
            @Override
            public TreeItem[] run() {
               return item.widget.getParent().getSelection();
            }
         });
         return !ArrayUtil.contains(selection, item.widget);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void init(SWTBot bot) {
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public String getFailureMessage() {
         return "Item " + item + " is still selected.";
      }
   }

   /**
    * Selects the given text in the given {@link SWTBotStyledText}.
    * @param styledText The {@link SWTBotStyledText} to select text in.
    * @param text The text to select.
    * @return The x and y coordinate of the selected text.
    */
   public static Point selectText(final SWTBotStyledText styledText, 
                                  final String text) {
      return UIThreadRunnable.syncExec(new Result<Point>() {
         @Override
         public Point run() {
            int index = styledText.widget.getText().indexOf(text);
            styledText.widget.setCaretOffset(index);
            styledText.widget.setSelection(index, index + text.length());
            int offset = styledText.widget.getCaretOffset();
            return styledText.widget.getLocationAtOffset(offset);
         }
      });
   }
   
   /**
    * Ensures that the given arrays contain the same elements.
    * @param expected The first array.
    * @param actual The second array.
    */
   public static <T> void assertArrayEquals(T[] expected, T[] actual) {
      if (expected != null) {
         assertNotNull(actual);
         assertEquals(expected.length, actual.length);
         for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
         }
      }
      else {
         assertNull(actual);
      }
   }

   /**
    * Performs {@link WorkbenchUtil#selectAndReveal(IResource)} thread save.
    * @param file The {@link IFile} to select.
    */
   public static void selectAndReveal(final IFile file) {
      UIThreadRunnable.syncExec(new VoidResult() {
         @Override
         public void run() {
            WorkbenchUtil.selectAndReveal(file);
         }
      });
   }

   /**
    * Returns the foreground {@link Color}.
    * @param item The {@link SWTBotTreeItem}.
    * @return The foreground {@link Color} of the given {@link SWTBotTreeItem}.
    */
   public static Color getForeground(final SWTBotTreeItem item) {
      return UIThreadRunnable.syncExec(new Result<Color>() {
         @Override
         public Color run() {
            return item.widget.getForeground();
         }
      });
   }

   /**
    * Returns the data of the given {@link AbstractSWTBotControl} meaning
    * {@link Widget#getData()}.
    * @param control The {@link AbstractSWTBotControl} to read its data.
    * @return The read data.
    */
   public static Object getData(final AbstractSWTBotControl<?> control) {
      return UIThreadRunnable.syncExec(new Result<Object>() {
         @Override
         public Object run() {
            return control.widget.getData();
         }
      });
   }

   /**
    * Waits until the {@link SWTBotTable} contains at least some rows.
    * @param bot The {@link SWTBot} to use.
    * @param table The {@link SWTBotTable} to test.
    * @param minRowCount The epected minimal number of rows to wait for.
    */
   public static void waitUntilTableHasAtLeastRows(SWTBot bot, final SWTBotTable table, final int minRowCount) {
      bot.waitUntil(new ICondition() {
         @Override
         public boolean test() throws Exception {
            return table.rowCount() >= minRowCount;
         }
         
         @Override
         public void init(SWTBot bot) {
         }
         
         @Override
         public String getFailureMessage() {
            return "Timed out waiting for " + table + " to contain at least " + minRowCount + " rows.";
         }
      });
   }

   /**
    * Compares the given {@link String}s ignoring white space.
    * @param expected The expected text.
    * @param actual The actual text.
    */
   public static void assertEqualsIgnoreWhiteSpace(String expected, String actual) {
      if (!StringUtil.equalIgnoreWhiteSpace(expected, actual)) {
         assertEquals(expected, actual);
      }
   }

   /**
    * Waits until the given {@link IProject} exists and is open.
    * @param bot The {@link SWTBot} to use.
    * @param project The {@link IProject} to wait for.
    */
   public static void waitForProject(SWTBot bot, final IProject project) {
      assertNotNull(project);
      bot.waitUntil(new ICondition() {
         @Override
         public boolean test() throws Exception {
            return project.exists() && project.isOpen();
         }
         
         @Override
         public void init(SWTBot bot) {
         }
         
         @Override
         public String getFailureMessage() {
            return "Timed out waiting for " + project + " to exist and to be open.";
         }
      });
   }

   /**
    * Opens the perspective 'Resource'.
    * @return The {@link IPerspectiveDescriptor} of the perspective 'Resource'.
    */
   public static IPerspectiveDescriptor openResourcePerspective() {
      IPerspectiveDescriptor resourcePerspective = getResourcePerspective();
      openPerspective(resourcePerspective);
      return resourcePerspective;
   }

   /**
    * Returns the {@link IPerspectiveDescriptor} of the perspective 'Resource'.
    * @return The {@link IPerspectiveDescriptor} of the perspective 'Resource'.
    */
   public static IPerspectiveDescriptor getResourcePerspective() {
      return TestUtilsUtil.getPerspective("org.eclipse.ui.resourcePerspective");
   }

   /**
    * Waits until the given {@link SWTBotTree} has items.
    * @param bot The {@link SWTWorkbenchBot} to use.
    * @param tree THe {@link SWTBotTree} to wait for.
    */
   public static void waitUntilTreeHasItems(SWTWorkbenchBot bot, final SWTBotTree tree) {
      bot.waitUntil(new ICondition() {
         @Override
         public boolean test() throws Exception {
            SWTBotTreeItem[] items = tree.getAllItems();
            return !ArrayUtil.isEmpty(items);
         }
         
         @Override
         public void init(SWTBot bot) {
         }
         
         @Override
         public String getFailureMessage() {
            return "Tree has no items.";
         }
      });
   }
   
   /**
    * Compares the given {@link String}s ignoring whitespace.
    * @param expected The expected value.
    * @param actual The current value.
    */
   public static void assertEqualsIgnoreWhitespace(String expected, String actual) {
      if (!StringUtil.equalIgnoreWhiteSpace(expected, actual)) {
         TestCase.assertEquals(expected, actual);
      }
   }

   /**
    * Checks if the given {@link SWTBotText} is editable or not.
    * @param text The {@link SWTBotText} to check.
    * @return {@code true} editable, {@code false} read-only.
    */
   public static boolean isEditable(final SWTBotText text) {
      assertNotNull(text);
      int style = UIThreadRunnable.syncExec(new IntResult() {
         @Override
         public Integer run() {
            return text.widget.getStyle();
         }
      });
      return (style & SWT.READ_ONLY) != SWT.READ_ONLY;
   }
   
   /**
    * Ensures that the given {@link IResource}s have the same content.
    * @param expected The expected {@link IResource}s.
    * @param current The current {@link IResource}s.
    * @throws Exception Occurred Exception.
    */
   public static void assertResources(IResource[] expected, IResource[] current) throws Exception {
      if (expected != null) {
         assertNotNull(current);
         assertEquals(expected.length, current.length);
         for (int i = 0; i < expected.length; i++) {
            assertResource(expected[i], current[i]);
         }
      }
      else {
         assertNull(current);
      }
   }
  
   /**
    * Ensures the same content of the given {@link IResource}s.
    * @param expected The expected {@link IResource}.
    * @param current The current {@link IResource}.
    * @throws Exception Occurred Exception.
    */
   public static void assertResource(IResource expected, IResource current) throws Exception {
      assertEquals(expected.getName(), current.getName());
      assertEquals(expected.getType(), current.getType());
      if (expected instanceof IFolder) {
         TestCase.assertTrue(current instanceof IFolder);
         assertResources(((IFolder) expected).members(), ((IFolder) current).members());
      }
      else {
         TestCase.assertFalse(current instanceof IFolder);
      }
      if (expected instanceof IFile) {
         TestCase.assertTrue(current instanceof IFile);
         String expectedContent = IOUtil.readFrom(((IFile) expected).getContents());
         String currentContent = IOUtil.readFrom(((IFile) current).getContents());
         if (!StringUtil.equalIgnoreWhiteSpace(expectedContent, currentContent)) {
            assertEquals(expectedContent, currentContent); // Let the test fail
         }
      }
      else {
         TestCase.assertFalse(current instanceof IFile);
      }
   }
}