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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Test;
import org.key_project.sed.core.model.ISENode;
import org.key_project.sed.core.model.ISEDebugTarget;
import org.key_project.sed.core.test.util.TestSedCoreUtil;
import org.key_project.sed.key.core.model.IKeYSENode;
import org.key_project.util.java.CollectionUtil;
import org.key_project.util.java.ObjectUtil;

import de.uka.ilkd.key.symbolic_execution.model.IExecutionNode;

/**
 * Tests access of {@link ISENode#getChildren()} from different threads
 * to make sure that in KeY's implementation no multiple {@link IKeYSENode}s are
 * created for the same {@link IExecutionNode}.
 * @author Martin Hentschel
 */
public class SWTBotKeYModelThreadSaveChildAccessTest extends AbstractKeYDebugTargetTestCase {
   /**
    * Tests access of {@link ISENode#getChildren()} from different threads
    * to make sure that in KeY's implementation no multiple {@link IKeYSENode}s are
    * created for the same {@link IExecutionNode}.
    */
   @Test
   public void testChildAccessOfElseIfTest() throws Exception {
      IKeYDebugTargetTestExecutor executor = new AbstractKeYDebugTargetTestExecutor() {
         @Override
         public void test(SWTWorkbenchBot bot, IJavaProject project, IMethod method, String targetName, SWTBotView debugView, SWTBotTree debugTree, ISEDebugTarget target, ILaunch launch) throws Exception {
            // Get debug target TreeItem
            SWTBotTreeItem item = TestSedCoreUtil.selectInDebugTree(debugView, 0, 0, 0); // Select thread
            // Resume
            resume(bot, item, target);
            // Start with threads
            List<ISENode> toTest = new LinkedList<ISENode>();
            CollectionUtil.addAll(toTest, target.getSymbolicThreads());
            assertFalse(toTest.isEmpty());
            // Iterate over the whole containment hierarchy
            while (!toTest.isEmpty()) {
               // Define current node
               ISENode current = toTest.remove(0);
               // Access ISEDDebugNode#getChildren() from different threads
               ChildAccessThread[] threads = new ChildAccessThread[3];
               for (int i = 0; i < threads.length; i++) {
                  threads[i] = new ChildAccessThread(current);
               }
               for (int i = 0; i < threads.length; i++) {
                  threads[i].start();
               }
               ObjectUtil.waitForThreads(threads);
               // Test result
               ISENode[] children = current.getChildren();
               assertNotNull(children);
               for (ChildAccessThread thread : threads) {
                  assertNull(thread.getException());
                  assertNotNull(thread.getChildren());
                  assertSame(children, thread.getChildren());
               }
               // Add children in test set
               CollectionUtil.addAll(toTest, children);
            }
         }
      };
      doKeYDebugTargetTest("SWTBotKeYModelThreadSaveChildAccessTest_testChildAccessOfElseIfTest",
                           "data/elseIfTest/test",
                           true,
                           true, // Must be true because otherwise instantiates the sed visualization all children!
                           createMethodSelector("ElseIfTest", "elseIf", "I"),
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
   
   /**
    * A {@link Thread} which executes {@link ISENode#getChildren()}
    * and provides the result.
    * @author Martin Hentschel
    */
   private static class ChildAccessThread extends Thread {
      /**
       * The parent to execute {@link ISENode#getChildren()} on.
       */
      private ISENode parent;
      
      /**
       * The result of The parent to execute {@link ISENode#getChildren()} on.
       */
      private ISENode[] children;
      
      /**
       * The occurred exception or {@code null} if access was successful.
       */
      private DebugException exception;
      
      /**
       * Constructor. 
       * @param parent The parent to execute {@link ISENode#getChildren()} on.
       */
      public ChildAccessThread(ISENode parent) {
         super();
         this.parent = parent;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void run() {
         try {
            children = parent.getChildren();
         }
         catch (DebugException e) {
            exception = e;
         }
      }

      /**
       * Returns the result of The parent to execute {@link ISENode#getChildren()} on.
       * @return The result of The parent to execute {@link ISENode#getChildren()} on.
       */
      public ISENode[] getChildren() {
         return children;
      }

      /**
       * Returns the occurred exception or {@code null} if access was successful.
       * @return The occurred exception or {@code null} if access was successful.
       */
      public DebugException getException() {
         return exception;
      }
   }
}