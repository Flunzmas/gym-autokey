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

package org.key_project.key4eclipse.starter.core.test.testcase.swtbot;

import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.junit.Test;
import org.key_project.key4eclipse.starter.core.util.KeYUtil;
import org.key_project.swtbot.swing.bot.SwingBotJButton;
import org.key_project.swtbot.swing.bot.SwingBotJDialog;
import org.key_project.swtbot.swing.bot.SwingBotJFrame;
import org.key_project.swtbot.swing.bot.SwingBotJMenu;
import org.key_project.swtbot.swing.bot.SwingBotJTree;
import org.key_project.ui.test.util.TestKeYUIUtil;
import org.key_project.util.test.testcase.AbstractSetupTestCase;
import org.key_project.util.test.util.TestUtilsUtil.MethodTreatment;

import de.uka.ilkd.key.gui.MainWindow;

/**
 * Tests the example dialog in the Eclipse integration.
 * @author Martin Hentschel
 */
public class SWTBotExampleTest extends AbstractSetupTestCase {
    /**
     * Opens the example wizard, selects an example and loads it.
     */
    @Test
    public void testExampleDialog() throws InterruptedException, InvocationTargetException {
        long originalTimeout = SWTBotPreferences.TIMEOUT;
        try {
            // Increase timeout
            SWTBotPreferences.TIMEOUT = SWTBotPreferences.TIMEOUT * 4;
            // Open KeY
            KeYUtil.openMainWindowAsync();
            // Get KeY window
            SwingBotJFrame frame = TestKeYUIUtil.keyGetMainWindow();
            // Open example dialog.
            SwingBotJMenu fileMenu = frame.bot().jMenuBar().menu("File");
            fileMenu.item("Load Example").click();
            SwingBotJDialog dialog = frame.bot().jDialog("Load Example");
            SwingBotJTree tree = dialog.bot().jTree();
            // Select example
            tree.select("Dynamic Frames", "Cell");
            // Close dialog and load example
            SwingBotJButton loadButton = dialog.bot().jButton("Load Example");
            loadButton.clickAndWait();
            // Start proof
            TestKeYUIUtil.keyStartSelectedProofInProofManagementDiaolog();
            TestKeYUIUtil.keyCheckProofs(TestKeYUIUtil.createOperationContractId("Cell", "Cell", "Cell()", "0", "normal_behavior"), TestKeYUIUtil.createOperationContractId("Cell", "Cell", "Cell()", "0", "normal_behavior"));
            // Finish proof automatically
            TestKeYUIUtil.keyFinishSelectedProofAutomatically(frame, MethodTreatment.EXPAND);
            // Clear proof list
            KeYUtil.clearProofList(MainWindow.getInstance());
            TestCase.assertTrue(KeYUtil.isProofListEmpty(MainWindow.getInstance()));
            // Close main window
            TestKeYUIUtil.keyCloseMainWindow();
        }
        finally {
            SWTBotPreferences.TIMEOUT = originalTimeout;
        }
    }
}