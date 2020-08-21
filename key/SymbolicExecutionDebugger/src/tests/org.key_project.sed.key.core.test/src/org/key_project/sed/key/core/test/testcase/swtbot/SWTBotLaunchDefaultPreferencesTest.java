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

package org.key_project.sed.key.core.test.testcase.swtbot;

import java.util.Iterator;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Test;
import org.key_project.key4eclipse.starter.core.util.KeYUtil;
import org.key_project.sed.core.model.ISEDebugTarget;
import org.key_project.sed.core.test.util.TestSedCoreUtil;
import org.key_project.sed.key.core.model.IKeYSENode;
import org.key_project.sed.key.core.test.Activator;
import org.key_project.sed.key.core.util.KeYSEDPreferences;
import org.key_project.sed.key.ui.view.SymbolicExecutionSettingsView;
import org.key_project.sed.ui.text.SymbolicallyReachedAnnotation;
import org.key_project.util.java.ArrayUtil;
import org.key_project.util.test.util.TestUtilsUtil;

import de.uka.ilkd.key.gui.MainWindow;
import de.uka.ilkd.key.symbolic_execution.model.IExecutionVariable;
import de.uka.ilkd.key.symbolic_execution.strategy.SymbolicExecutionStrategy;

/**
 * Tests the launch configuration default values.
 * @author Martin Hentschel
 */
public class SWTBotLaunchDefaultPreferencesTest extends AbstractKeYDebugTargetTestCase {
   
   /**
    * Tests the launch where full branch conditions are hidden.
    */
   @Test
   public void testHidingOfFullBranchConditions() throws Exception {
      doHidingOfFullBranchConditionsTest("SWTBotLaunchDefaultPreferencesTest_testHidingOfFullBranchConditions", true);
   }

   /**
    * Tests the launch where full branch conditions are not hidden.
    */
   @Test
   public void testDoNotHidingOfFullBranchConditions() throws Exception {
      doHidingOfFullBranchConditionsTest("SWTBotLaunchDefaultPreferencesTest_testDoNotHidingOfFullBranchConditions", false);
   }
   
   /**
    * Does the test steps of {@link #testSimplifyConditions()}
    * and {@link #testDoNotSimplifyConditions()}.
    * @param projectName The project name to use.
    * @param hideFullBranchConditions Hide full branch conditions?
    * @throws Exception Occurred Exception
    */
   protected void doHidingOfFullBranchConditionsTest(String projectName, 
                                                     final boolean hideFullBranchConditions) throws Exception {
      boolean originalSimplifyConditions = KeYSEDPreferences.isHideFullBranchConditionIfAdditionalLabelIsAvailable();
      try {
         // Set preference
         SWTWorkbenchBot bot = new SWTWorkbenchBot();
         SWTBotShell preferenceShell = TestUtilsUtil.openPreferencePage(bot, "Run/Debug", "Symbolic Execution Debugger (SED)", "KeY Launch Defaults");
         if (hideFullBranchConditions) {
            preferenceShell.bot().checkBox("Hide full branch conditions when an alternative label is available").select();
         }
         else {
            preferenceShell.bot().checkBox("Hide full branch conditions when an alternative label is available").deselect();
         }
         preferenceShell.bot().button("OK").click();
         assertEquals(hideFullBranchConditions, KeYSEDPreferences.isHideFullBranchConditionIfAdditionalLabelIsAvailable());
         // Launch something
         IKeYDebugTargetTestExecutor executor = new AbstractKeYDebugTargetTestExecutor() {
            @Override
            public void test(SWTWorkbenchBot bot, IJavaProject project, IMethod method, String targetName, SWTBotView debugView, SWTBotTree debugTree, ISEDebugTarget target, ILaunch launch) throws Exception {
               SWTBotView symbolicSettingsView = bot.viewById(SymbolicExecutionSettingsView.VIEW_ID);
               TestUtilsUtil.clickDirectly(symbolicSettingsView.bot().radio(SymbolicExecutionStrategy.Factory.LOOP_TREATMENT_INVARIANT));
               // Get debug target TreeItem
               SWTBotTreeItem item = TestSedCoreUtil.selectInDebugTree(debugView, 0, 0, 0); // Select thread
               // Do run
               resume(bot, item, target);
               TestUtilsUtil.clickDirectly(symbolicSettingsView.bot().radio(SymbolicExecutionStrategy.Factory.LOOP_TREATMENT_EXPAND, 0));
               if (hideFullBranchConditions) {
                  assertDebugTargetViaOracle(target, Activator.PLUGIN_ID, "data/useLoopInvariantArraySumWhile/oracle/ArraySumWhileHideFullBranchConditions.xml", false, false, false);
               }
               else {
                  assertDebugTargetViaOracle(target, Activator.PLUGIN_ID, "data/useLoopInvariantArraySumWhile/oracle/ArraySumWhileDoNotHideFullBranchConditions.xml", false, false, false);
               }
            }
         };
         doKeYDebugTargetTest(projectName,
                              "data/useLoopInvariantArraySumWhile/test",
                              true,
                              true,
                              createMethodSelector("ArraySumWhile", "sum", "[I"),
                              null,
                              null,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              null,
                              14, 
                              executor);
      }
      finally {
         // Restore original value
         KeYSEDPreferences.setHideFullBranchConditionIfAdditionalLabelIsAvailable(originalSimplifyConditions);
         assertEquals(originalSimplifyConditions, KeYSEDPreferences.isHideFullBranchConditionIfAdditionalLabelIsAvailable());
      }
   }
   
   /**
    * Tests the launch where branch conditions are simplified.
    */
   @Test
   public void testSimplifyConditions() throws Exception {
      doSimplifyConditionsTest("SWTBotLaunchDefaultPreferencesTest_testSimplifyConditions", true);
   }

   /**
    * Tests the launch where branch conditions are not simplified.
    */
   @Test
   public void testDoNotSimplifyConditions() throws Exception {
      doSimplifyConditionsTest("SWTBotLaunchDefaultPreferencesTest_testDoNotSimplifyConditions", false);
   }
   
   /**
    * Does the test steps of {@link #testSimplifyConditions()}
    * and {@link #testDoNotSimplifyConditions()}.
    * @param projectName The project name to use.
    * @param simplifyConditions Simplify branch conditions?
    * @throws Exception Occurred Exception
    */
   protected void doSimplifyConditionsTest(String projectName, 
                                           final boolean simplifyConditions) throws Exception {
      boolean originalSimplifyConditions = KeYSEDPreferences.isSimplifyConditions();
      try {
         // Set preference
         SWTWorkbenchBot bot = new SWTWorkbenchBot();
         SWTBotShell preferenceShell = TestUtilsUtil.openPreferencePage(bot, "Run/Debug", "Symbolic Execution Debugger (SED)", "KeY Launch Defaults");
         if (simplifyConditions) {
            preferenceShell.bot().checkBox("Simplify conditions (recommended)").select();
         }
         else {
            preferenceShell.bot().checkBox("Simplify conditions (recommended)").deselect();
         }
         preferenceShell.bot().button("OK").click();
         assertEquals(simplifyConditions, KeYSEDPreferences.isSimplifyConditions());
         // Launch something
         IKeYDebugTargetTestExecutor executor = new AbstractKeYDebugTargetTestExecutor() {
            @Override
            public void test(SWTWorkbenchBot bot, IJavaProject project, IMethod method, String targetName, SWTBotView debugView, SWTBotTree debugTree, ISEDebugTarget target, ILaunch launch) throws Exception {
               // Get debug target TreeItem
               SWTBotTreeItem item = TestSedCoreUtil.selectInDebugTree(debugView, 0, 0, 0); // Select thread
               // Do run
               resume(bot, item, target);
               if (simplifyConditions) {
                  assertDebugTargetViaOracle(target, Activator.PLUGIN_ID, "data/simpleIf/oracle/SimpleIf_BranchConditionsSimplified.xml", false, false, false);
               }
               else {
                  assertDebugTargetViaOracle(target, Activator.PLUGIN_ID, "data/simpleIf/oracle/SimpleIf_BranchConditionsNotSimplified.xml", false, false, false);
               }
            }
         };
         doKeYDebugTargetTest(projectName,
                              "data/simpleIf/test",
                              true,
                              true,
                              createMethodSelector("SimpleIf", "min", "I", "I"),
                              null,
                              null,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              null,
                              Boolean.FALSE,
                              8, 
                              executor);
      }
      finally {
         // Restore original value
         KeYSEDPreferences.setSimplifyConditions(originalSimplifyConditions);
         assertEquals(originalSimplifyConditions, KeYSEDPreferences.isSimplifyConditions());
      }
   }
   
   /**
    * Tests the launch with highlighting of reached source code.
    */
   @Test
   public void testHighlightSourcecode() throws Exception {
      doHiglightSourceCodeTest("SWTBotLaunchDefaultPreferencesTest_testDoNotHighlightSourcecode", true);
   }

   /**
    * Tests the launch without highlighting of reached source code.
    */
   @Test
   public void testDoNotHighlightSourcecode() throws Exception {
      doHiglightSourceCodeTest("SWTBotLaunchDefaultPreferencesTest_testHighlightSourcecode", false);
   }
   
   /**
    * Does the test steps of {@link #testHighlightSourcecode()}
    * and {@link #testDoNotHighlightSourcecode()}.
    * @param projectName The project name to use.
    * @param highlightSourceCode Highlight source code?
    * @throws Exception Occurred Exception
    */
   protected void doHiglightSourceCodeTest(String projectName, 
                                           final boolean highlightSourceCode) throws Exception {
      boolean originalHighlightSourceCode = KeYSEDPreferences.isHighlightReachedSourceCode();
      try {
         KeYSEDPreferences.setUsePrettyPrinting(true);
         // Set preference
         SWTWorkbenchBot bot = new SWTWorkbenchBot();
         SWTBotShell preferenceShell = TestUtilsUtil.openPreferencePage(bot, "Run/Debug", "Symbolic Execution Debugger (SED)", "KeY Launch Defaults");
         if (highlightSourceCode) {
            preferenceShell.bot().checkBox("Highlight reached source code during symbolic execution").select();
         }
         else {
            preferenceShell.bot().checkBox("Highlight reached source code during symbolic execution").deselect();
         }
         preferenceShell.bot().button("OK").click();
         assertEquals(highlightSourceCode, KeYSEDPreferences.isHighlightReachedSourceCode());
         // Launch something
         IKeYDebugTargetTestExecutor executor = new AbstractKeYDebugTargetTestExecutor() {
            @Override
            public void test(SWTWorkbenchBot bot, IJavaProject project, IMethod method, String targetName, SWTBotView debugView, SWTBotTree debugTree, ISEDebugTarget target, ILaunch launch) throws Exception {
               // Get debug target TreeItem
               SWTBotTreeItem item = TestSedCoreUtil.selectInDebugTree(debugView, 0, 0, 0); // Select thread
               resume(bot, item, target);
               // Check source code highlighting
               SWTBotEditor editor = bot.activeEditor();
               IEditorPart editorPart = editor.getReference().getEditor(true);
               assertTrue(editorPart instanceof ITextEditor);
               IDocumentProvider documentProvider = ((ITextEditor) editorPart).getDocumentProvider();
               IAnnotationModel annotationModel = documentProvider.getAnnotationModel(editorPart.getEditorInput());
               boolean reachedAnnotationAvailable = false;
               Iterator<?> iterator = annotationModel.getAnnotationIterator();
               while (iterator.hasNext()) {
                  Object next = iterator.next();
                  if (next instanceof SymbolicallyReachedAnnotation) {
                     reachedAnnotationAvailable = true;
                  }
               }
               assertEquals(highlightSourceCode, reachedAnnotationAvailable);
            }
         };
         doKeYDebugTargetTest(projectName,
                              "data/unicodeTest/test",
                              true,
                              true,
                              createMethodSelector("UnicodeTest", "magic", "Z", "Z"),
                              null,
                              null,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              null,
                              Boolean.FALSE,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              8, 
                              executor);
      }
      finally {
         // Restore original value
         KeYSEDPreferences.setHighlightReachedSourceCode(originalHighlightSourceCode);
         assertEquals(originalHighlightSourceCode, KeYSEDPreferences.isHighlightReachedSourceCode());
      }
   }
   
   /**
    * Tests the launch where truth value tracing is enabled.
    */
   @Test
   public void testTruthValueTracingEnabled() throws Exception {
      doTruthValueTracingTest("SWTBotLaunchDefaultPreferencesTest_testTruthValueTracingEnabled", true);
   }

   /**
    * Tests the launch where truth value tracing is disabled.
    */
   @Test
   public void testTruthValueTracingDisabled() throws Exception {
      doTruthValueTracingTest("SWTBotLaunchDefaultPreferencesTest_testTruthValueTracingDisabled", false);
   }
   
   /**
    * Does the test steps of {@link #testTruthValueTracingEnabled()}
    * and {@link #testTruthValueTracingDisabled()}.
    * @param projectName The project name to use.
    * @param truthValueTracingEnabled Is truth value tracing enabled?
    * @throws Exception Occurred Exception
    */
   protected void doTruthValueTracingTest(String projectName, 
                                          final boolean truthValueTracingEnabled) throws Exception {
      boolean originalTruthValueTracingEnabled = KeYSEDPreferences.isTruthValueTracingEnabled();
      try {
         KeYSEDPreferences.setUsePrettyPrinting(true);
         // Set preference
         SWTWorkbenchBot bot = new SWTWorkbenchBot();
         SWTBotShell preferenceShell = TestUtilsUtil.openPreferencePage(bot, "Run/Debug", "Symbolic Execution Debugger (SED)", "KeY Launch Defaults");
         if (truthValueTracingEnabled) {
            preferenceShell.bot().checkBox("Truth status tracing enabled").select();
         }
         else {
            preferenceShell.bot().checkBox("Truth status tracing enabled").deselect();
         }
         preferenceShell.bot().button("OK").click();
         assertEquals(truthValueTracingEnabled, KeYSEDPreferences.isTruthValueTracingEnabled());
         // Launch something
         IKeYDebugTargetTestExecutor executor = new AbstractKeYDebugTargetTestExecutor() {
            @Override
            public void test(SWTWorkbenchBot bot, IJavaProject project, IMethod method, String targetName, SWTBotView debugView, SWTBotTree debugTree, ISEDebugTarget target, ILaunch launch) throws Exception {
               // Get debug target TreeItem
               SWTBotTreeItem item = TestSedCoreUtil.selectInDebugTree(debugView, 0, 0, 0); // Select thread
               // Check launch
               Object threadObject = TestUtilsUtil.getTreeItemData(item);
               assertTrue(threadObject instanceof IKeYSENode);
               assertEquals(truthValueTracingEnabled, ((IKeYSENode<?>) threadObject).isTruthValueTracingEnabled());
            }
         };
         doKeYDebugTargetTest(projectName,
                              "data/unicodeTest/test",
                              true,
                              true,
                              createMethodSelector("UnicodeTest", "magic", "Z", "Z"),
                              null,
                              null,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.TRUE,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              null,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              8, 
                              executor);
      }
      finally {
         // Restore original value
         KeYSEDPreferences.setTruthValueTracingEnabled(originalTruthValueTracingEnabled);
         assertEquals(originalTruthValueTracingEnabled, KeYSEDPreferences.isTruthValueTracingEnabled());
      }
   }
   
   /**
    * Tests the launch where unicode signs are used.
    */
   @Test
   public void testUseUnicode() throws Exception {
      doTestUseUnicode("SWTBotLaunchDefaultPreferencesTest_testUseUnicode", true);
   }

   /**
    * Tests the launch where unicode signs are not used.
    */
   @Test
   public void testDoNotUseUnicode() throws Exception {
      doTestUseUnicode("SWTBotLaunchDefaultPreferencesTest_testDoNotUseUnicode", false);
   }
   
   /**
    * Does the test steps of {@link #testUseUnicode()}
    * and {@link #testDoNotUseUnicode()}.
    * @param projectName The project name to use.
    * @param useUnicode Use unicode signs?
    * @throws Exception Occurred Exception
    */
   protected void doTestUseUnicode(String projectName, 
                                   final boolean useUnicode) throws Exception {
      boolean originalUseUnicode = KeYSEDPreferences.isUseUnicode();
      try {
         KeYSEDPreferences.setUsePrettyPrinting(true);
         // Set preference
         SWTWorkbenchBot bot = new SWTWorkbenchBot();
         SWTBotShell preferenceShell = TestUtilsUtil.openPreferencePage(bot, "Run/Debug", "Symbolic Execution Debugger (SED)", "KeY Launch Defaults");
         if (useUnicode) {
            preferenceShell.bot().checkBox("Use unicode symbols").select();
         }
         else {
            preferenceShell.bot().checkBox("Use unicode symbols").deselect();
         }
         preferenceShell.bot().button("OK").click();
         assertEquals(useUnicode, KeYSEDPreferences.isUseUnicode());
         // Launch something
         IKeYDebugTargetTestExecutor executor = new AbstractKeYDebugTargetTestExecutor() {
            @Override
            public void test(SWTWorkbenchBot bot, IJavaProject project, IMethod method, String targetName, SWTBotView debugView, SWTBotTree debugTree, ISEDebugTarget target, ILaunch launch) throws Exception {
               // Get debug target TreeItem
               SWTBotTreeItem item = TestSedCoreUtil.selectInDebugTree(debugView, 0, 0, 0); // Select thread
               // Do run
               resume(bot, item, target);
               if (useUnicode) {
                  assertDebugTargetViaOracle(target, Activator.PLUGIN_ID, "data/unicodeTest/oracle/UnicodeTest_Enabled.xml", false, false, false);
               }
               else {
                  assertDebugTargetViaOracle(target, Activator.PLUGIN_ID, "data/unicodeTest/oracle/UnicodeTest_Disabled.xml", false, false, false);
               }
            }
         };
         doKeYDebugTargetTest(projectName,
                              "data/unicodeTest/test",
                              true,
                              true,
                              createMethodSelector("UnicodeTest", "magic", "Z", "Z"),
                              null,
                              null,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              null,
                              Boolean.TRUE,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              8, 
                              executor);
      }
      finally {
         // Restore original value
         KeYSEDPreferences.setUseUnicode(originalUseUnicode);
         assertEquals(originalUseUnicode, KeYSEDPreferences.isUseUnicode());
      }
   }
   
   /**
    * Tests the launch where pretty printing is used.
    */
   @Test
   public void testUsePrettyPrinting() throws Exception {
      doTestUsePrettyPrinting("SWTBotLaunchDefaultPreferencesTest_testUsePrettyPrinting", true);
   }

   /**
    * Tests the launch where pretty printing is not used.
    */
   @Test
   public void testDoNotUsePrettyPrinting() throws Exception {
      doTestUsePrettyPrinting("SWTBotLaunchDefaultPreferencesTest_testDoNotUsePrettyPrinting", false);
   }
   
   /**
    * Does the test steps of {@link #testUsePrettyPrinting()}
    * and {@link #testDoNotUsePrettyPrinting()}.
    * @param projectName The project name to use.
    * @param usePrettyPrinting Use pretty printing?
    * @throws Exception Occurred Exception
    */
   protected void doTestUsePrettyPrinting(String projectName, 
                                          final boolean usePrettyPrinting) throws Exception {
      boolean originalUsePrettyPrinting = KeYSEDPreferences.isUsePrettyPrinting();
      try {
         // Set preference
         SWTWorkbenchBot bot = new SWTWorkbenchBot();
         SWTBotShell preferenceShell = TestUtilsUtil.openPreferencePage(bot, "Run/Debug", "Symbolic Execution Debugger (SED)", "KeY Launch Defaults");
         if (usePrettyPrinting) {
            preferenceShell.bot().checkBox("Use pretty printing").select();
         }
         else {
            preferenceShell.bot().checkBox("Use pretty printing").deselect();
         }
         preferenceShell.bot().button("OK").click();
         assertEquals(usePrettyPrinting, KeYSEDPreferences.isUsePrettyPrinting());
         // Launch something
         IKeYDebugTargetTestExecutor executor = new AbstractKeYDebugTargetTestExecutor() {
            @Override
            public void test(SWTWorkbenchBot bot, IJavaProject project, IMethod method, String targetName, SWTBotView debugView, SWTBotTree debugTree, ISEDebugTarget target, ILaunch launch) throws Exception {
               // Get debug target TreeItem
               SWTBotTreeItem item = TestSedCoreUtil.selectInDebugTree(debugView, 0, 0, 0); // Select thread
               // Do run
               resume(bot, item, target);
               if (usePrettyPrinting) {
                  assertDebugTargetViaOracle(target, Activator.PLUGIN_ID, "data/prettyPrintSimpleTest/oracleUsePrettyPrinting/PrettyPrintSimpleTest.xml", false, false, false);
               }
               else {
                  assertDebugTargetViaOracle(target, Activator.PLUGIN_ID, "data/prettyPrintSimpleTest/oracleUsePrettyPrinting/NotPrettyPrintedPrettyPrintSimpleTest.xml", false, false, false);
               }
            }
         };
         doKeYDebugTargetTest(projectName,
                              "data/prettyPrintSimpleTest/test",
                              true,
                              true,
                              createMethodSelector("PrettyPrintSimpleTest", "main", "I"),
                              null,
                              null,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              null,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              8, 
                              executor);
      }
      finally {
         // Restore original value
         KeYSEDPreferences.setUsePrettyPrinting(originalUsePrettyPrinting);
         assertEquals(originalUsePrettyPrinting, KeYSEDPreferences.isUsePrettyPrinting());
      }
   }
   
   /**
    * Tests the launch where branch conditions are merged.
    */
   @Test
   public void testMergeBranchCondtions() throws Exception {
      doTestMergeBranchConditions("SWTBotLaunchDefaultPreferencesTest_testMergeBranchCondtions", true);
   }

   /**
    * Tests the launch where branch conditions are not merged.
    */
   @Test
   public void testDoNotMergeBranchCondtions() throws Exception {
      doTestMergeBranchConditions("SWTBotLaunchDefaultPreferencesTest_testDoNotMergeBranchCondtions", false);
   }
   
   /**
    * Does the test steps of {@link #testMergeBranchCondtions()}
    * and {@link #testDoNotMergeBranchCondtions()}.
    * @param projectName The project name to use.
    * @param mergeBranchConditions Merge branch conditions?
    * @throws Exception Occurred Exception
    */
   protected void doTestMergeBranchConditions(String projectName, 
                                              final boolean mergeBranchConditions) throws Exception {
      boolean originalMergeBranchConditions = KeYSEDPreferences.isMergeBranchConditions();
      try {
         // Set preference
         SWTWorkbenchBot bot = new SWTWorkbenchBot();
         SWTBotShell preferenceShell = TestUtilsUtil.openPreferencePage(bot, "Run/Debug", "Symbolic Execution Debugger (SED)", "KeY Launch Defaults");
         if (mergeBranchConditions) {
            preferenceShell.bot().checkBox("Merge branch conditions").select();
         }
         else {
            preferenceShell.bot().checkBox("Merge branch conditions").deselect();
         }
         preferenceShell.bot().button("OK").click();
         assertEquals(mergeBranchConditions, KeYSEDPreferences.isMergeBranchConditions());
         // Launch something
         IKeYDebugTargetTestExecutor executor = new AbstractKeYDebugTargetTestExecutor() {
            @Override
            public void test(SWTWorkbenchBot bot, IJavaProject project, IMethod method, String targetName, SWTBotView debugView, SWTBotTree debugTree, ISEDebugTarget target, ILaunch launch) throws Exception {
               // Get debug target TreeItem
               SWTBotTreeItem item = TestSedCoreUtil.selectInDebugTree(debugView, 0, 0, 0); // Select thread
               // Do run
               resume(bot, item, target);
               if (mergeBranchConditions) {
                  assertDebugTargetViaOracle(target, Activator.PLUGIN_ID, "data/switchCaseTest/oracleMergeBranchConditions/MergedSwitchCaseTest.xml", false, false, false);
               }
               else {
                  assertDebugTargetViaOracle(target, Activator.PLUGIN_ID, "data/switchCaseTest/oracleMergeBranchConditions/NotMergedSwitchCaseTest.xml", false, false, false);
               }
            }
         };
         doKeYDebugTargetTest(projectName,
                              "data/switchCaseTest/test",
                              true,
                              true,
                              createMethodSelector("SwitchCaseTest", "switchCase", "I"),
                              null,
                              null,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              null,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              8, 
                              executor);
      }
      finally {
         // Restore original value
         KeYSEDPreferences.setMergeBranchConditions(originalMergeBranchConditions);
         assertEquals(originalMergeBranchConditions, KeYSEDPreferences.isMergeBranchConditions());
      }
   }
   
   /**
    * Tests the launch where variable values are shown.
    */
   @Test
   public void testShowVariableValues() throws Exception {
      doTestShowVariableValues("SWTBotLaunchDefaultPreferencesTest_testShowVariableValues", true);
   }

   /**
    * Tests the launch where variable values are hidden.
    */
   @Test
   public void testDoNotShowVariableValues() throws Exception {
      doTestShowVariableValues("SWTBotLaunchDefaultPreferencesTest_testDoNotShowVariableValues", false);
   }
   
   /**
    * Does the test steps of {@link #testShowVariableValues()}
    * and {@link #testDoNotShowVariableValues()}.
    * @param projectName The project name to use.
    * @param showVariableValues Show variable values?
    * @throws Exception Occurred Exception
    */
   protected void doTestShowVariableValues(String projectName, 
                                           final boolean showVariableValues) throws Exception {
      boolean originalShowVariableValues = KeYSEDPreferences.isShowVariablesOfSelectedDebugNode();
      try {
         // Set preference
         SWTWorkbenchBot bot = new SWTWorkbenchBot();
         SWTBotShell preferenceShell = TestUtilsUtil.openPreferencePage(bot, "Run/Debug", "Symbolic Execution Debugger (SED)", "KeY Launch Defaults");
         if (showVariableValues) {
            preferenceShell.bot().checkBox("Show variables of selected debug node").select();
         }
         else {
            preferenceShell.bot().checkBox("Show variables of selected debug node").deselect();
         }
         preferenceShell.bot().button("OK").click();
         assertEquals(showVariableValues, KeYSEDPreferences.isShowVariablesOfSelectedDebugNode());
         // Launch something
         IKeYDebugTargetTestExecutor executor = new AbstractKeYDebugTargetTestExecutor() {
            @Override
            public void test(SWTWorkbenchBot bot, IJavaProject project, IMethod method, String targetName, SWTBotView debugView, SWTBotTree debugTree, ISEDebugTarget target, ILaunch launch) throws Exception {
               // Get variables view
               SWTBotView variablesView = TestSedCoreUtil.getVariablesView(bot);
               // Get debug target TreeItem
               SWTBotTreeItem item = TestSedCoreUtil.selectInDebugTree(debugView, 0, 0, 0); // Select thread
               // Do run
               resume(bot, item, target);
               // Select statement int result;
               item = TestSedCoreUtil.selectInDebugTree(debugView, 0, 0, 0, 1); 
               // Wait for jobs
               variablesView.show();
               TestUtilsUtil.sleep(1000); // Give the UI the chance to show variables.
               TestUtilsUtil.waitForJobs();
               // Get variables
               SWTBotTree variablesTree = variablesView.bot().tree();
               if (showVariableValues) {
                  TestUtilsUtil.waitUntilTreeHasItems(bot, variablesTree);
               }
               SWTBotTreeItem[] items = variablesTree.getAllItems();
               assertEquals(items != null ? "items found: " + items.length : "items are null", showVariableValues, !ArrayUtil.isEmpty(items));
            }
         };
         doKeYDebugTargetTest(projectName,
                              "data/simpleIf/test",
                              true,
                              true,
                              createMethodSelector("SimpleIf", "min", "I", "I"),
                              null,
                              null,
                              Boolean.FALSE,
                              null,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              8, 
                              executor);
      }
      finally {
         // Restore original value
         KeYSEDPreferences.setShowVariablesOfSelectedDebugNode(originalShowVariableValues);
         assertEquals(originalShowVariableValues, KeYSEDPreferences.isShowVariablesOfSelectedDebugNode());
      }
   }

   /**
    * Tests a launch in which KeY's main window is shown.
    */
   @Test
   public void testShowKeYMainWindow() throws Exception {
      doTestMainWindowLaunch("SWTBotLaunchDefaultPreferencesTest_testShowKeYMainWindow", true);
   }

   /**
    * Tests a launch in which KeY's main window is not shown.
    */
   @Test
   public void testDoNotShowKeYMainWindow() throws Exception {
      doTestMainWindowLaunch("SWTBotLaunchDefaultPreferencesTest_testDoNotShowKeYMainWindow", false);
   }
   
   /**
    * Executes the test steps of {@link #testShowKeYMainWindow()} and
    * {@link #testDoNotShowKeYMainWindow()}.
    * @param projectName The name of the java project to use.
    * @param showMainWindow {@code true} show main window, {@code false} hide main window.
    */
   protected void doTestMainWindowLaunch(String projectName, 
                                         final boolean showMainWindow) throws Exception {
      boolean originalShowMainWindow = KeYSEDPreferences.isShowKeYMainWindow();
      try {
         // Make sure that KeY's main window is hidden and contains no proofs.
         if (MainWindow.hasInstance()) {
            KeYUtil.clearProofList(MainWindow.getInstance());
            MainWindow.getInstance().setVisible(false);
         }
         // Set preference
         SWTWorkbenchBot bot = new SWTWorkbenchBot();
         SWTBotShell preferenceShell = TestUtilsUtil.openPreferencePage(bot, "Run/Debug", "Symbolic Execution Debugger (SED)", "KeY Launch Defaults");
         if (showMainWindow) {
            preferenceShell.bot().checkBox("Show KeY's main window (only for experienced user)").select();
         }
         else {
            preferenceShell.bot().checkBox("Show KeY's main window (only for experienced user)").deselect();
         }
         preferenceShell.bot().button("OK").click();
         assertEquals(showMainWindow, KeYSEDPreferences.isShowKeYMainWindow());
         // Launch something
         IKeYDebugTargetTestExecutor executor = new AbstractKeYDebugTargetTestExecutor() {
            @Override
            public void test(SWTWorkbenchBot bot, IJavaProject project, IMethod method, String targetName, SWTBotView debugView, SWTBotTree debugTree, ISEDebugTarget target, ILaunch launch) throws Exception {
               if (showMainWindow) {
                  assertTrue(MainWindow.hasInstance());
                  assertTrue(MainWindow.getInstance().isVisible());
                  assertFalse(KeYUtil.isProofListEmpty(MainWindow.getInstance()));
               }
               else {
                  if (MainWindow.hasInstance()) {
                     assertFalse(MainWindow.getInstance().isVisible());
                     assertTrue(KeYUtil.isProofListEmpty(MainWindow.getInstance()));
                  }
               }
            }
         };
         doKeYDebugTargetTest(projectName,
                              "data/simpleIf/test",
                              true,
                              true,
                              createMethodSelector("SimpleIf", "min", "I", "I"),
                              null,
                              null,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              null,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              8, 
                              executor);
      }
      finally {
         // Restore original value
         KeYSEDPreferences.setShowMethodReturnValuesInDebugNode(originalShowMainWindow);
         assertEquals(originalShowMainWindow, KeYSEDPreferences.isShowMethodReturnValuesInDebugNode());
      }
   }
   
   /**
    * Tests the launch where return values are shown in tree.
    */
   @Test
   public void testShowMethodReturnValuesInDebugNodes_Signature() throws Exception {
      doTestShowMethodReturnValuesInDebugNodes("SWTBotLaunchDefaultPreferencesTest_testShowMethodReturnValuesInDebugNodesWithSignature", true, true);
   }

   /**
    * Tests the launch where return values are not shown in tree.
    */
   @Test
   public void testDoNotShowMethodReturnValuesInDebugNodes_Signature() throws Exception {
      doTestShowMethodReturnValuesInDebugNodes("SWTBotLaunchDefaultPreferencesTest_testDoNotShowMethodReturnValuesInDebugNodesWithSignature", false, true);
   }
   
   /**
    * Tests the launch where return values are shown in tree.
    */
   @Test
   public void testShowMethodReturnValuesInDebugNodes_NameOnly() throws Exception {
      doTestShowMethodReturnValuesInDebugNodes("SWTBotLaunchDefaultPreferencesTest_testShowMethodReturnValuesInDebugNodes", true, false);
   }

   /**
    * Tests the launch where return values are not shown in tree.
    */
   @Test
   public void testDoNotShowMethodReturnValuesInDebugNodes_NameOnly() throws Exception {
      doTestShowMethodReturnValuesInDebugNodes("SWTBotLaunchDefaultPreferencesTest_testDoNotShowMethodReturnValuesInDebugNodes", false, false);
   }
   
   /**
    * Does the test steps of {@link #testShowMethodReturnValuesInDebugNodes()}
    * and {@link #testDoNotShowMethodReturnValuesInDebugNodes()}.
    * @param projectName The project name to use.
    * @param showMethodReturnValuesInDebugNodes Show method return values in debug node?
    * @throws Exception Occurred Exception
    */
   protected void doTestShowMethodReturnValuesInDebugNodes(String projectName, 
                                                           final boolean showMethodReturnValuesInDebugNodes,
                                                           final boolean showSignature) throws Exception {
      boolean originalShowMethodReturnValuesInDebugNodes = KeYSEDPreferences.isShowMethodReturnValuesInDebugNode();
      try {
         // Set preference
         SWTWorkbenchBot bot = new SWTWorkbenchBot();
         SWTBotShell preferenceShell = TestUtilsUtil.openPreferencePage(bot, "Run/Debug", "Symbolic Execution Debugger (SED)", "KeY Launch Defaults");
         if (showMethodReturnValuesInDebugNodes) {
            preferenceShell.bot().checkBox("Show method return values in debug nodes").select();
         }
         else {
            preferenceShell.bot().checkBox("Show method return values in debug nodes").deselect();
         }
         if (showSignature) {
            preferenceShell.bot().checkBox("Show signature instead of only the name on method return nodes").select();
         }
         else {
            preferenceShell.bot().checkBox("Show signature instead of only the name on method return nodes").deselect();
         }
         preferenceShell.bot().button("OK").click();
         assertEquals(showMethodReturnValuesInDebugNodes, KeYSEDPreferences.isShowMethodReturnValuesInDebugNode());
         // Launch something
         IKeYDebugTargetTestExecutor executor = new AbstractKeYDebugTargetTestExecutor() {
            @Override
            public void test(SWTWorkbenchBot bot, IJavaProject project, IMethod method, String targetName, SWTBotView debugView, SWTBotTree debugTree, ISEDebugTarget target, ILaunch launch) throws Exception {
               // Get debug target TreeItem
               SWTBotTreeItem item = TestSedCoreUtil.selectInDebugTree(debugView, 0, 0, 0); // Select thread
               // Do resume and test created tree
               String expectedModelPathInBundle;
               if (showSignature) {
                  expectedModelPathInBundle = showMethodReturnValuesInDebugNodes ? 
                                              "data/simpleIf/oracle/SimpleIf.xml" : 
                                              "data/simpleIf/oracle_noMethodReturnValues/SimpleIf.xml";
               }
               else {
                  expectedModelPathInBundle = showMethodReturnValuesInDebugNodes ? 
                                              "data/simpleIf/oracle/SimpleIf_NoSignature.xml" : 
                                              "data/simpleIf/oracle_noMethodReturnValues/SimpleIf_NoSignature.xml";
               }
               resume(bot, item, target);
               assertDebugTargetViaOracle(target, Activator.PLUGIN_ID, expectedModelPathInBundle, false, false, false);
            }
         };
         doKeYDebugTargetTest(projectName,
                              "data/simpleIf/test",
                              true,
                              true,
                              createMethodSelector("SimpleIf", "min", "I", "I"),
                              null,
                              null,
                              null,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              null,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              8, 
                              executor);
      }
      finally {
         // Restore original value
         KeYSEDPreferences.setShowMethodReturnValuesInDebugNode(originalShowMethodReturnValuesInDebugNodes);
         assertEquals(originalShowMethodReturnValuesInDebugNodes, KeYSEDPreferences.isShowMethodReturnValuesInDebugNode());
      }
   }
   /**
    * Tests the launch where variables are based on the sequent.
    */
   @Test
   public void testVariablesBasedOnSequent() throws Exception {
      doVariablesComputationTest("SWTBotLaunchDefaultPreferencesTest_testVariablesBasedOnSequent", true);
   }

   /**
    * Tests the launch where variables are based on the visible type structure.
    */
   @Test
   public void testVariablesBasedOnVisibleTypeStructure() throws Exception {
      doVariablesComputationTest("SWTBotLaunchDefaultPreferencesTest_testVariablesBasedOnVisibleTypeStructure", false);
   }
   
   /**
    * Does the test steps of {@link #testVariablesBasedOnSequent()}
    * and {@link #testVariablesBasedOnVisibleTypeStructure()}.
    * @param projectName The project name to use.
    * @param variablesAreOnlyComputedFromUpdates {@code true} {@link IExecutionVariable} are only computed from updates, {@code false} {@link IExecutionVariable}s are computed according to the type structure of the visible memory.
    * @throws Exception Occurred Exception
    */
   protected void doVariablesComputationTest(String projectName, 
                                             final boolean variablesAreOnlyComputedFromUpdates) throws Exception {
      boolean originalSetting = KeYSEDPreferences.isVariablesAreOnlyComputedFromUpdates();
      boolean originalShowVariablesSetting = KeYSEDPreferences.isShowVariablesOfSelectedDebugNode();
      try {
         KeYSEDPreferences.setVariablesAreOnlyComputedFromUpdates(false);
         KeYSEDPreferences.setShowVariablesOfSelectedDebugNode(true);
         // Set preference
         SWTWorkbenchBot bot = new SWTWorkbenchBot();
         SWTBotShell preferenceShell = TestUtilsUtil.openPreferencePage(bot, "Run/Debug", "Symbolic Execution Debugger (SED)", "KeY Launch Defaults");
         if (variablesAreOnlyComputedFromUpdates) {
            preferenceShell.bot().comboBox(0).setSelection("Based on sequent");
         }
         else {
            preferenceShell.bot().comboBox(0).setSelection("Based on visible type structure");
         }
         preferenceShell.bot().button("OK").click();
         assertEquals(variablesAreOnlyComputedFromUpdates, KeYSEDPreferences.isVariablesAreOnlyComputedFromUpdates());
         // Launch something
         IKeYDebugTargetTestExecutor executor = new AbstractKeYDebugTargetTestExecutor() {
            @Override
            public void test(SWTWorkbenchBot bot, IJavaProject project, IMethod method, String targetName, SWTBotView debugView, SWTBotTree debugTree, ISEDebugTarget target, ILaunch launch) throws Exception {
               // Get debug target TreeItem
               SWTBotTreeItem item = TestSedCoreUtil.selectInDebugTree(debugView, 0, 0, 0); // Select thread
               // Do run
               resume(bot, item, target);
               if (variablesAreOnlyComputedFromUpdates) {
                  assertDebugTargetViaOracle(target, Activator.PLUGIN_ID, "data/arrayVariables/oracle/ArrayVariablesTest_sequent.xml", true, false, false);
               }
               else {
                  assertDebugTargetViaOracle(target, Activator.PLUGIN_ID, "data/arrayVariables/oracle/ArrayVariablesTest_structure.xml", true, false, false);
               }
            }
         };
         doKeYDebugTargetTest(projectName,
                              "data/arrayVariables/test",
                              true,
                              true,
                              createMethodSelector("ArrayVariablesTest", "arrayTest", "[I"),
                              null,
                              null,
                              Boolean.FALSE,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.FALSE,
                              Boolean.TRUE,
                              Boolean.FALSE,
                              8, 
                              executor);
      }
      finally {
         // Restore original value
         KeYSEDPreferences.setVariablesAreOnlyComputedFromUpdates(originalSetting);
         assertEquals(originalSetting, KeYSEDPreferences.isVariablesAreOnlyComputedFromUpdates());
         KeYSEDPreferences.setShowVariablesOfSelectedDebugNode(originalShowVariablesSetting);
         assertEquals(originalShowVariablesSetting, KeYSEDPreferences.isShowVariablesOfSelectedDebugNode());
      }
   }
}