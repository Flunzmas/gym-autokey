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

package org.key_project.sed.ui.test.testcase.swtbot;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.Test;
import org.key_project.sed.core.model.ISEConstraint;
import org.key_project.sed.core.model.ISEDebugTarget;
import org.key_project.sed.core.model.ISEMethodCall;
import org.key_project.sed.core.model.ISEMethodReturn;
import org.key_project.sed.core.model.ISEStatement;
import org.key_project.sed.core.model.ISEThread;
import org.key_project.util.java.ArrayUtil;
import org.key_project.util.test.util.SWTBotTabbedPropertyList;
import org.key_project.util.test.util.TestUtilsUtil;

/**
 * Tests the property page tab "Constraints".
 * @author Martin Hentschel
 */
public class SWTBotConstraintsNodeTabTest extends AbstractSWTBotPropertyTabTest {
   /**
    * Tests the shown values and the existence of tab "Node".
    */
   @Test
   public void testValuesAndTabExistence() throws Exception {
      doFixedExampleTest(createFixedExampleSteps());
   }
   
   /**
    * Creates the test steps to execute.
    * @return The created test steps.
    */
   public static ITestSteps createFixedExampleSteps() {
      return new AbstractTestSteps() {
         @Override
         public void assertThread(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs, ISEThread thread) throws Exception {
            assertTrue(tabs.selectTabItem("Constraints"));
            assertEquals(1, propertiesView.bot().table().rowCount());
            Object itemData = TestUtilsUtil.getTableItemData(propertiesView.bot().table().getTableItem(0));
            assertTrue(itemData instanceof ISEConstraint);
            assertEquals("Thread's Constraint", ((ISEConstraint) itemData).getName());
         }
         
         @Override
         public void assertStatement(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs, ISEStatement statement) throws Exception {
            assertTrue(tabs.selectTabItem("Constraints"));
            assertEquals(4, propertiesView.bot().table().rowCount());
            Object itemData = TestUtilsUtil.getTableItemData(propertiesView.bot().table().getTableItem(0));
            assertTrue(itemData instanceof ISEConstraint);
            assertEquals("int x = 1 Constraint", ((ISEConstraint) itemData).getName());
         }
         
         @Override
         public void assertDebugTarget(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs, ISEDebugTarget target) throws Exception {
            assertFalse(tabs.hasTabItem("Constraints"));
         }

         @Override
         public void assertMethodReturn(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs, ISEMethodReturn methodReturn) throws Exception {
            if (!ArrayUtil.isEmpty(methodReturn.getConstraints())) {
               assertTrue(tabs.selectTabItem("Constraints"));
               assertEquals(0, propertiesView.bot().table().rowCount());
            }
            else {
               assertFalse(tabs.selectTabItem("Constraints"));
            }
         }

         @Override
         public void assertMethodCall(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs, ISEMethodCall methodCall) throws Exception {
            if (!ArrayUtil.isEmpty(methodCall.getConstraints())) {
               assertTrue(tabs.selectTabItem("Constraints"));
               assertEquals(0, propertiesView.bot().table().rowCount());
            }
            else {
               assertFalse(tabs.selectTabItem("Constraints"));
            }
         }
      };
   }
}