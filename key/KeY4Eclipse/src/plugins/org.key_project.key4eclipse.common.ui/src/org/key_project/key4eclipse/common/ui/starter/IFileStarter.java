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

package org.key_project.key4eclipse.common.ui.starter;

import org.eclipse.core.resources.IFile;

/**
 * Instances of this class are used to start an application
 * and to load the given {@link IFile} in it.
 * @author Martin Hentschel
 */
public interface IFileStarter {
   /**
    * Open the application.
    * @param file The {@link IFile} to load.
    * @throws Exception Occurred Exception.
    */
   public void open(IFile file) throws Exception;
}