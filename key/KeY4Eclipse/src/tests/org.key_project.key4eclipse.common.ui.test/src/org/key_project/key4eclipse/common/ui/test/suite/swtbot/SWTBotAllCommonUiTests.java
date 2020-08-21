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

package org.key_project.key4eclipse.common.ui.test.suite.swtbot;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.key_project.key4eclipse.common.ui.test.testcase.swtbot.SWTBotCompleteAndApplyTacletMatchWizardPageTest;
import org.key_project.key4eclipse.common.ui.test.testcase.swtbot.SWTBotExportProjectFileWizardTest;
import org.key_project.key4eclipse.common.ui.test.testcase.swtbot.SWTBotKeYExampleNewWizardTest;
import org.key_project.key4eclipse.common.ui.test.testcase.swtbot.SWTBotKeYIncludesPropertyPageTest;
import org.key_project.key4eclipse.common.ui.test.testcase.swtbot.SWTBotKeYStubGenerationCustomizationTest;
import org.key_project.key4eclipse.common.ui.test.testcase.swtbot.SWTBotLoopInvariantRuleCompletionTest;
import org.key_project.key4eclipse.common.ui.test.testcase.swtbot.SWTBotNodeCounterExampleGeneratorJobTest;
import org.key_project.key4eclipse.common.ui.test.testcase.swtbot.SWTBotProofFileGenerateTestsJobTest;
import org.key_project.key4eclipse.common.ui.test.testcase.swtbot.SWTBotProofGenerateTestsJobTest;
import org.key_project.key4eclipse.common.ui.test.testcase.swtbot.SWTBotStarterTest;
import org.key_project.key4eclipse.common.ui.test.testcase.swtbot.SWTBotTacletOptionsPreferencePageTest;

/**
 * Run all contained JUnit 4 test cases that requires SWT Bot.
 * @author Martin Hentschel
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   SWTBotExportProjectFileWizardTest.class,
   SWTBotKeYExampleNewWizardTest.class,
   SWTBotKeYIncludesPropertyPageTest.class,
   SWTBotKeYStubGenerationCustomizationTest.class,
   SWTBotNodeCounterExampleGeneratorJobTest.class,
   SWTBotProofFileGenerateTestsJobTest.class,
   SWTBotProofGenerateTestsJobTest.class,
   SWTBotStarterTest.class,
   SWTBotTacletOptionsPreferencePageTest.class,
   SWTBotLoopInvariantRuleCompletionTest.class,
   SWTBotCompleteAndApplyTacletMatchWizardPageTest.class
})
public class SWTBotAllCommonUiTests {
}