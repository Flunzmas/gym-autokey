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

package org.key_project.sed.core.model.memory;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.key_project.sed.core.model.ISEBlockContractTermination;
import org.key_project.sed.core.model.ISEBranchCondition;
import org.key_project.sed.core.model.ISEConstraint;
import org.key_project.sed.core.model.ISEDebugTarget;
import org.key_project.sed.core.model.ISENode;
import org.key_project.sed.core.model.ISENodeLink;
import org.key_project.sed.core.model.ISEThread;
import org.key_project.sed.core.model.impl.AbstractSEBlockContractTermination;

/**
 * Implementation of {@link ISEBlockContractTermination} that stores all
 * information in the memory.
 * @author Martin Hentschel
 */
public class SEMemoryBlockContractTermination extends AbstractSEBlockContractTermination implements ISEMemoryStackFrameCompatibleDebugNode, ISEMemoryNode {
   /**
    * The contained child nodes.
    */
   private final List<ISENode> children = new LinkedList<ISENode>();
   
   /**
    * The name of this debug node.
    */
   private String name;
   
   /**
    * The human readable path condition to this node.
    */
   private String pathCondition;

   /**
    * The method call stack.
    */
   private ISENode[] callStack;
   
   /**
    * Verified?
    */
   private boolean verified;
   
   /**
    * The contained {@link ISEConstraint}s.
    */
   private final List<ISEConstraint> constraints = new LinkedList<ISEConstraint>();

   /**
    * The source path.
    */
   private String sourcePath;
   
   /**
    * The line number.
    */
   private int lineNumber = -1;

   /**
    * The index of the start character.
    */
   private int charStart = -1;
   
   /**
    * The index of the end character.
    */
   private int charEnd = -1;
   
   /**
    * The contained variables.
    */
   private final List<IVariable> variables = new LinkedList<IVariable>();
   
   /**
    * The known group start conditions.
    */
   private final List<ISEBranchCondition> groupStartConditions = new LinkedList<ISEBranchCondition>();
   
   /**
    * The outgoing {@link ISENodeLink}s.
    */
   private final List<ISENodeLink> outgoingLinks = new LinkedList<ISENodeLink>();
   
   /**
    * The incoming {@link ISENodeLink}s.
    */
   private final List<ISENodeLink> incomingLinks = new LinkedList<ISENodeLink>();
   
   /**
    * Constructor.
    * @param target The {@link ISEDebugTarget} in that this exceptional termination is contained.
    * @param parent The parent in that this node is contained as child.
    * @param thread The {@link ISEThread} in that this exceptional termination is contained.
    * @param verified Verified?
    */
   public SEMemoryBlockContractTermination(ISEDebugTarget target, 
                                          ISENode parent,
                                          ISEThread thread,
                                          boolean verified) {
      super(target, parent, thread);
      this.verified = verified;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getName() throws DebugException {
      return name;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ISENode[] getChildren() throws DebugException {
      return children.toArray(new ISENode[children.size()]);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void addChild(ISENode child) {
      if (child != null) {
         children.add(child);
      }
   }

   /**
    * {@inheritDoc}
    */   
   @Override
   public void removeChild(ISENode child) {
      if (child != null) {
         children.remove(child);
      }
   }

   /**
    * {@inheritDoc}
    */   
   @Override
   public void addChild(int index, ISENode child) {
      if (child != null) {
         children.add(index, child);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int indexOfChild(ISENode child) {
      if (child != null) {
         return children.indexOf(child);
      }
      else {
         return -1;
      }
   }
   
   /**
    * Sets the name of this node.
    * @param name the name to set.
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * <p>
    * {@inheritDoc}
    * </p>
    * <p>
    * Changed visibility to public.
    * </p>
    */
   @Override
   public void setId(String id) {
      super.setId(id);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getPathCondition() throws DebugException {
      return pathCondition;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setPathCondition(String pathCondition) {
      this.pathCondition = pathCondition;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ISENode[] getCallStack() throws DebugException {
      return callStack;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setCallStack(ISENode[] callStack) {
      this.callStack = callStack;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isVerified() {
      return verified;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void addConstraint(ISEConstraint constraint) {
      if (constraint != null) {
         constraints.add(constraint);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ISEConstraint[] getConstraints() throws DebugException {
      return constraints.toArray(new ISEConstraint[constraints.size()]);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getSourcePath() {
      return sourcePath;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public int getLineNumber() throws DebugException {
      return lineNumber;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getCharStart() throws DebugException {
      return charStart;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getCharEnd() throws DebugException {
      return charEnd;
   }
   
   /**
    * Sets the line number.
    * @param lineNumber The line number or {@code -1} if it is unknown.
    */
   public void setLineNumber(int lineNumber) {
      this.lineNumber = lineNumber;
   }

   /**
    * Sets the index of the start character.
    * @param charStart The index or {@code -1} if it is unknown.
    */
   public void setCharStart(int charStart) {
      this.charStart = charStart;
   }

   /**
    * Sets the index of the end character.
    * @param charEnd The index or {@code -1} if it is unknown.
    */
   public void setCharEnd(int charEnd) {
      this.charEnd = charEnd;
   }
   
   /**
    * Sets the source path.
    * @param sourcePath The source path to set.
    */
   public void setSourcePath(String sourcePath) {
      this.sourcePath = sourcePath;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void addVariable(IVariable variable) {
      variables.add(variable);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public IVariable[] getVariables() throws DebugException {
      return variables.toArray(new IVariable[variables.size()]);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ISEBranchCondition[] getGroupStartConditions() throws DebugException {
      return groupStartConditions.toArray(new ISEBranchCondition[groupStartConditions.size()]);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void addGroupStartCondition(ISEBranchCondition groupStartCondition) {
      if (groupStartCondition != null) {
         groupStartConditions.add(groupStartCondition);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setParent(ISENode parent) {
      super.setParent(parent);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setThread(ISEThread thread) {
      super.setThread(thread);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void addOutgoingLink(ISENodeLink link) {
      outgoingLinks.add(link);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ISENodeLink[] getOutgoingLinks() throws DebugException {
      return outgoingLinks.toArray(new ISENodeLink[outgoingLinks.size()]);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void addIncomingLink(ISENodeLink link) {
      incomingLinks.add(link);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ISENodeLink[] getIncomingLinks() throws DebugException {
      return incomingLinks.toArray(new ISENodeLink[incomingLinks.size()]);
   }
}