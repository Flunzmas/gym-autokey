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

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.Test;
import org.key_project.util.test.util.SWTBotTabbedPropertyList;
import org.key_project.util.test.util.TestUtilsUtil;

/**
 * Tests the property page tab "Postcondition".
 * @author Martin Hentschel
 */
public class SWTBotPostconditionTabTest extends AbstractSWTBotKeYPropertyTabTest {
   /**
    * Tests the shown values and the existence of tab "KeY".
    */
   @Test
   public void testValuesAndTabExistence() throws Exception {
      doAllNodeTypesTest("SWTBotPostconditionTabTest_testValuesAndTabExistence", createAllNodeTypesSteps());
   }
   
   /**
    * Creates the test steps to execute.
    * @return The created test steps.
    */
   public static ITestSteps createAllNodeTypesSteps() {
      return new ITestSteps() {
         @Override
         public void assertThread(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception {
            assertFalse(tabs.selectTabItem("Postcondition"));
         }
         
         @Override
         public void assertStatement(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception {
            assertFalse(tabs.selectTabItem("Postcondition"));
         }
         
         @Override
         public void assertDebugTarget(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception {
            assertFalse(tabs.hasTabItem("Postcondition"));
         }

         @Override
         public void assertMethodReturn(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception {
            assertFalse(tabs.selectTabItem("Postcondition"));
         }

         @Override
         public void assertLaunch(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception {
            assertFalse(tabs.hasTabItem("Postcondition"));
         }

         @Override
         public void assertTermination(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception {
            assertTrue(tabs.selectTabItem("Postcondition"));
            TestUtilsUtil.waitForJobs();
            assertEquals(" true\n"
                         + "==>\n"
                         + " {_obj:=obj\n"
                         + "  || exc:=null\n"
                         + "  || heap:=heapAfter_doubleValue[obj.value := 2]}\n"
                         + "   (exc = null)", propertiesView.bot().styledText(0).getText());
         }

         @Override
         public void assertMethodContract(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception {
            assertFalse(tabs.hasTabItem("Postcondition"));
         }

         @Override
         public void assertLoopInvariant(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception {
            assertFalse(tabs.selectTabItem("Postcondition"));
         }

         @Override
         public void assertLoopBodyTermination(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception {
            assertFalse(tabs.selectTabItem("Postcondition"));
         }
      };
   }
}