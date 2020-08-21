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

package org.key_project.sed.core.util;

import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.key_project.sed.core.model.ISEBaseMethodReturn;
import org.key_project.sed.core.model.ISENode;
import org.key_project.sed.core.model.ISEGroupable;
import org.key_project.sed.core.model.ISEMethodCall;

/**
 * Provides Symbolic Execution Debugger (SED) specific constants.
 * @author Martin Hentschel
 */
@SuppressWarnings("restriction")
public interface ISEConstants {
   /**
    * The ID used for the call stack, accessible via {@link ISENode#getCallStack()}.
    */
   public static final String ID_CALL_STACK = "org.key_project.sed.core.callStack";
   /**
    * The ID used for the call stack, accessible via {@link ISEBaseMethodReturn#getCallStateVariables()}.
    */
   public static final String ID_CALL_STATE = "org.key_project.sed.core.callState";

   /**
    * The ID used for the method return conditions, accessible via {@link ISEMethodCall#getMethodReturnConditions()}.
    */
   public static final String ID_METHOD_RETURN_CONDITIONS = "org.key_project.sed.core.methodReturnConditions";

   /**
    * The ID used for the method return conditions, accessible via {@link ISENode#getGroupStartConditions()}.
    */
   public static final String ID_GROUP_START_CONDITIONS = "org.key_project.sed.core.groupStarts";

   /**
    * The ID used for the method return conditions, accessible via {@link ISEGroupable#getGroupEndConditions()}.
    */
   public static final String ID_GROUP_END_CONDITIONS = "org.key_project.sed.core.groupEnds";
   
   /**
    * Property key used in {@link PresentationContext} to define the input of the {@link TreeModelViewer} in which the {@link PresentationContext} is used.
    */
   public static final String PRESENTATION_CONTEXT_PROPERTY_INPUT = "org.key_project.sed.core.input";
}