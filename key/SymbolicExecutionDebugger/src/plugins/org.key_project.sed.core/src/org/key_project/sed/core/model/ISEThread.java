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

package org.key_project.sed.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.key_project.sed.core.model.impl.AbstractSEThread;
import org.key_project.sed.core.model.memory.SEMemoryThread;

/**
 * A thread is the root of the symbolic execution tree.
 * <p>
 * A symbolic thread is also a normal thread ({@link IThread})
 * for compatibility reasons with the Eclipse debug API. But the default
 * provided {@link IStackFrame}s are not used anymore. Instead the contained
 * {@link ISENode}s are used.
 * </p>
 * <p>
 * Clients may implement this interface. It is recommended to subclass
 * from {@link AbstractSEThread} instead of implementing this
 * interface directly. {@link SEMemoryThread} is also a default
 * implementation that stores all values in the memory.
 * </p>
 * @author Martin Hentschel
 * @see ISENode
 */
public interface ISEThread extends ISENode, IThread, IStackFrame {
   /**
    * Returns all leaf nodes to select.
    * @return The leaf nodes to select.
    */
   public ISENode[] getLeafsToSelect() throws DebugException;

   /**
    * Returns the up to know discovered {@link ISETermination}s.
    * @return The up to know discovered {@link ISETermination}s.
    * @exception DebugException if this method fails.  Reasons include:
    * <ul><li>Failure communicating with the VM.  The DebugException's
    * status code contains the underlying exception responsible for
    * the failure.</li>
    */
   public ISETermination[] getTerminations() throws DebugException;
}