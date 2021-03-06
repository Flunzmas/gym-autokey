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

package org.key_project.sed.key.ui.test.testcase.swtbot;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.ui.IPageLayout;
import org.key_project.sed.core.model.ISEStatement;
import org.key_project.sed.core.model.ISEThread;
import org.key_project.sed.key.ui.test.Activator;
import org.key_project.sed.ui.visualization.test.util.TestVisualizationUtil;
import org.key_project.util.eclipse.BundleUtil;
import org.key_project.util.test.util.SWTBotTabbedPropertyList;
import org.key_project.util.test.util.TestUtilsUtil;

/**
 * Provides the basic functionality to test property tabs in Graphiti editors and views.
 * @author Martin Hentschel
 */
public class AbstractSWTBotGraphitiKeYPropertyTabTest extends AbstractSWTBotKeYPropertyTabTest {
   /**
    * {@inheritDoc}
    */
   @Override
   protected void selectThread(SWTBotView debugView) throws Exception {
      super.selectThread(debugView);
      TestVisualizationUtil.setFocusToSymbolicExecutionTreeView();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void selectStatement(SWTBotView debugView) throws Exception {
      super.selectStatement(debugView);
      TestVisualizationUtil.setFocusToSymbolicExecutionTreeView();
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected void selectDebugTarget(SWTBotView debugView) throws Exception {
      super.selectDebugTarget(debugView);
      TestVisualizationUtil.setFocusToSymbolicExecutionTreeView();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void selectMethodReturn(SWTBotView debugView) throws Exception {
      super.selectMethodReturn(debugView);
      TestVisualizationUtil.setFocusToSymbolicExecutionTreeView();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void selectLaunch(SWTBotView debugView) throws Exception {
      super.selectLaunch(debugView);
      TestVisualizationUtil.setFocusToSymbolicExecutionTreeView();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void selectMethodContract(SWTBotView debugView) throws Exception {
      super.selectMethodContract(debugView);
      TestVisualizationUtil.setFocusToSymbolicExecutionTreeView();
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected void selectTermination(SWTBotView debugView) throws Exception {
      super.selectTermination(debugView);
      TestVisualizationUtil.setFocusToSymbolicExecutionTreeView();
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected void selectLoopInvariant(SWTBotView debugView) throws Exception {
      super.selectLoopInvariant(debugView);
      TestVisualizationUtil.setFocusToSymbolicExecutionTreeView();
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected void selectLoopBodyTermination(SWTBotView debugView) throws Exception {
      super.selectLoopBodyTermination(debugView);
      TestVisualizationUtil.setFocusToSymbolicExecutionTreeView();
   }

   /**
    * Does some test steps on an opened diagram in an editable editor.
    * @param steps The test steps to execute.
    * @throws Exception Occurred Exception.
    */
   protected void doInDiagramEditorTest(IEditorTestSteps steps) throws Exception {
      // Close welcome screen
      SWTGefBot bot = new SWTGefBot();
      TestUtilsUtil.closeWelcomeView(bot);
      // Open properties view
      TestUtilsUtil.openView(IPageLayout.ID_PROP_SHEET);
      SWTBotView propertiesView = TestUtilsUtil.getPropertiesView(bot);
      // Create project
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("AbstractSWTBotGraphitiPropertyTabTest_doInDiagramEditorTest");
      if (!project.exists()) {
         project.create(null);
      }
      if (!project.isOpen()) {
         project.open(null);
      }
      BundleUtil.extractFromBundleToWorkspace(Activator.PLUGIN_ID, "data/diagrams/FlatSteps", project);
      // Open diagram file
      TestUtilsUtil.openEditor(project.getFile("FlatSteps.set_diagram"));
      SWTBotGefEditor editor = bot.gefEditor("FlatSteps.set_diagram");
      try {
         // Select thread
         editor.select("<start>");
         SWTBotTabbedPropertyList tabs = SWTBotTabbedPropertyList.tabbedPropertyList(propertiesView.bot());
         assertNotNull(tabs);
         steps.assertThread(editor, propertiesView, tabs);
         // Select first statement
         editor.select("int x = 1;");
         steps.assertStatement(editor, propertiesView, tabs);
         // Select diagram
         editor.select(editor.rootEditPart());
         steps.assertDiagram(editor, propertiesView, tabs);
      }
      finally {
         editor.close();
      }
   }
   
   /**
    * Defines the test steps to execute via {@link AbstractSWTBotGraphitiKeYPropertyTabTest#doInDiagramEditorTest(IEditorTestSteps)}.
    * @author Martin Hentschel
    */
   protected static interface IEditorTestSteps {
      /**
       * Do some assertions on an {@link ISEThread}.
       * @param editor The editor.
       * @param propertiesView The properties view.
       * @param tabs The properties tabs.
       * @throws Exception Occurred Exception.
       */
      public void assertThread(SWTBotGefEditor editor, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception;

      /**
       * Do some assertions on an {@link ISEStatement}.
       * @param editor The editor.
       * @param propertiesView The properties view.
       * @param tabs The properties tabs.
       * @throws Exception Occurred Exception.
       */
      public void assertStatement(SWTBotGefEditor editor, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception;

      /**
       * Do some assertions on a {@link Diagram}.
       * @param editor The editor.
       * @param propertiesView The properties view.
       * @param tabs The properties tabs.
       * @throws Exception Occurred Exception.
       */
      public void assertDiagram(SWTBotGefEditor editor, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception;
   }
}