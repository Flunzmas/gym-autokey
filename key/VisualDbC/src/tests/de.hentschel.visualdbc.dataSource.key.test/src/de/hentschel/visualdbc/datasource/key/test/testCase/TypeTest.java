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

package de.hentschel.visualdbc.datasource.key.test.testCase;

import org.junit.Test;

import de.hentschel.visualdbc.datasource.key.model.KeyConnection;
import de.hentschel.visualdbc.datasource.key.test.Activator;
import de.hentschel.visualdbc.datasource.model.DSPackageManagement;

/**
 * Tests the handling of classes in a {@link KeyConnection}.
 * @author Martin Hentschel
 */
public class TypeTest extends AbstractKeYTest {
   /**
    * Tests the flags of classes.
    */
   @Test
   public void testClassFlags() throws Exception {
      testKeyConnection("ClassTest_testClassFlags",
                        "data/typeTest/test",
                        null,
                        DSPackageManagement.NO_PACKAGES,
                        Activator.PLUGIN_ID,
                        "data/typeTest/oracle/typeTest.dbc");
   }
}