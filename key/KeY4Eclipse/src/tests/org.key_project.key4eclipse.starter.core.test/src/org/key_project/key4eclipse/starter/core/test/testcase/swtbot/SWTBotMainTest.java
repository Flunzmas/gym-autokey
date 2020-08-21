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

import org.junit.Test;
import org.key_project.key4eclipse.starter.core.util.KeYUtil;
import org.key_project.swtbot.swing.bot.SwingBotJFrame;
import org.key_project.ui.test.util.TestKeYUIUtil;
import org.key_project.util.test.testcase.AbstractSetupTestCase;

import de.uka.ilkd.key.core.Main;
import de.uka.ilkd.key.gui.MainWindow;

/**
 * Tests for {@link Main}.
 * @author Martin Hentschel
 */
public class SWTBotMainTest extends AbstractSetupTestCase {
    /**
     * Starts the normal KeY application via {@link KeYUtil#openMainWindowAsync()}
     * and closes it. If something is wrong with the KeY eclipse integration
     * an exception is thrown. 
     */
    @Test
    public void testOpeningMainWindow() throws InterruptedException, InvocationTargetException {
        // Open KeY user interface and make sure that a window is opened.
        KeYUtil.openMainWindowAsync();
        SwingBotJFrame frame = TestKeYUIUtil.keyGetMainWindow();
        assertTrue(frame.isOpen());
        assertNotNull(MainWindow.getInstance());
        assertTrue(MainWindow.getInstance().isVisible());
        // Get main window and close it
        MainWindow.getInstance().setVisible(false);
        assertFalse(MainWindow.getInstance().isVisible());
        assertFalse(frame.isOpen());
    }
}