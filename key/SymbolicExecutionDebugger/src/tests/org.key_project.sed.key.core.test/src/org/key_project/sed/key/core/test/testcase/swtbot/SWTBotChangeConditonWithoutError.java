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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Test;
import org.key_project.sed.core.model.ISEDebugTarget;
import org.key_project.sed.core.test.util.TestSedCoreUtil;
import org.key_project.sed.key.core.test.util.TestBreakpointsUtil;
import org.key_project.util.test.util.TestUtilsUtil;

public class SWTBotChangeConditonWithoutError extends AbstractKeYDebugTargetTestCase {
   
   private static final String CALLER_PATH= ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toString() + "/SWTBotChangeConditionWithoutError_test/src/BreakpointStopCallerAndLoop.java";
   
   @Test
   public void test() throws Exception{
      IKeYDebugTargetTestExecutor executor = new AbstractKeYDebugTargetTestExecutor() {
         @Override
         public void test(SWTWorkbenchBot bot, IJavaProject project, IMethod method, String targetName, SWTBotView debugView, SWTBotTree debugTree, ISEDebugTarget target, ILaunch launch) throws Exception {            
            // Get debug target TreeItem
            TestBreakpointsUtil.addSomeBreakpoints(CALLER_PATH, bot, 15, 14);
            SWTBotTreeItem item = TestSedCoreUtil.selectInDebugTree(debugView, 0, 0, 0);
            resume(bot, item, target);
            assertTrue(TestBreakpointsUtil.checkTargetConditiondofAllBreakpoints(target, null, false));
            assertTrue(TestBreakpointsUtil.changeCondition(bot, "BreakpointStopCallerAndLoop [entry] - main(int)", "a==0&&x==5"));
            assertTrue(TestBreakpointsUtil.changeCondition(bot, "BreakpointStopCallerAndLoop [line: 16] - main(int)", "a==0&&x==5"));
            TestUtilsUtil.sleep(2000);
            assertTrue(TestBreakpointsUtil.checkTargetConditiondofAllBreakpoints(target, "a==0&&x==5", true));
            TestBreakpointsUtil.removeAllBreakpoints();
         }
      };
      doKeYDebugTargetTest("SWTBotChangeConditionWithoutError_test",
            "data/BreakpointTest/test",
            true,
            true,
            createMethodSelector("BreakpointStopCallerAndLoop", "main"),
            null,
            null,
            Boolean.FALSE,
            Boolean.FALSE,
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
}
