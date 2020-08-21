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

package org.key_project.sed.key.core.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IMethod;
import org.key_project.sed.core.model.ISEBlockContractTermination;
import org.key_project.sed.core.model.ISENode;
import org.key_project.sed.core.model.impl.AbstractSEBlockContractTermination;
import org.key_project.sed.core.model.memory.SEMemoryBranchCondition;
import org.key_project.sed.key.core.util.KeYModelUtil;
import org.key_project.sed.key.core.util.LogUtil;
import org.key_project.util.eclipse.ResourceUtil;

import de.uka.ilkd.key.proof.init.ProofInputException;
import de.uka.ilkd.key.symbolic_execution.model.IExecutionNode;
import de.uka.ilkd.key.symbolic_execution.model.IExecutionTermination;
import de.uka.ilkd.key.symbolic_execution.profile.SymbolicExecutionJavaProfile;
import de.uka.ilkd.key.symbolic_execution.util.SymbolicExecutionUtil;

/**
 * Implementation of {@link ISEBlockContractTermination} for the symbolic execution debugger (SED)
 * based on KeY.
 * @author Martin Hentschel
 */
public class KeYBlockContractTermination extends AbstractSEBlockContractTermination implements IKeYTerminationNode<IExecutionTermination> {
   /**
    * The {@link IExecutionTermination} to represent by this debug node.
    */
   private final IExecutionTermination executionNode;

   /**
    * The contained children.
    */
   private IKeYSENode<?>[] children;

   /**
    * The method call stack.
    */
   private IKeYSENode<?>[] callStack;
   
   /**
    * The constraints
    */
   private KeYConstraint[] constraints;
   
   /**
    * The contained KeY variables.
    */
   private KeYVariable[] variables;
   
   /**
    * The conditions under which a group ending in this node starts.
    */
   private SEMemoryBranchCondition[] groupStartConditions;
   
   /**
    * The outgoing links.
    */
   private final List<KeYNodeLink> outgoingLinks = new LinkedList<KeYNodeLink>();

   /**
    * The incoming links.
    */
   private final List<KeYNodeLink> incomingLinks = new LinkedList<KeYNodeLink>();

   /**
    * Constructor.
    * @param target The {@link KeYDebugTarget} in that this branch condition is contained.
    * @param parent The parent in that this node is contained as child.
    * @param thread The {@link KeYThread} in that this node is contained.
    * @param executionNode The {@link IExecutionTermination} to represent by this debug node.
    */
   public KeYBlockContractTermination(KeYDebugTarget target, 
                                 IKeYSENode<?> parent, 
                                 KeYThread thread, 
                                 IExecutionTermination executionNode) throws DebugException {
      super(target, parent, thread);
      Assert.isNotNull(executionNode);
      this.executionNode = executionNode;
      target.registerDebugNode(this);
      thread.addTermination(this);
      initializeAnnotations();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public KeYThread getThread() {
      return (KeYThread)super.getThread();
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public KeYDebugTarget getDebugTarget() {
      return (KeYDebugTarget)super.getDebugTarget();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public IKeYSENode<?> getParent() throws DebugException {
      return (IKeYSENode<?>)super.getParent();
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
   public IKeYSENode<?>[] getChildren() throws DebugException {
      synchronized (this) { // Thread save execution is required because thanks lazy loading different threads will create different result arrays otherwise.
         IExecutionNode<?>[] executionChildren = executionNode.getChildren();
         if (children == null) {
            children = KeYModelUtil.createChildren(this, executionChildren);
         }
         else if (children.length != executionChildren.length) { // Assumption: Only new children are added, they are never replaced or removed
            children = KeYModelUtil.updateChildren(this, children, executionChildren);
         }
         return children;
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public IExecutionTermination getExecutionNode() {
      return executionNode;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getName() throws DebugException {
      try {
         return executionNode.getName();
      }
      catch (ProofInputException e) {
         throw new DebugException(LogUtil.getLogger().createErrorStatus("Can't compute name.", e));
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getPathCondition() throws DebugException {
      try {
         return executionNode.getFormatedPathCondition();
      }
      catch (ProofInputException e) {
         throw new DebugException(LogUtil.getLogger().createErrorStatus("Can't compute path condition.", e));
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public IKeYSENode<?>[] getCallStack() throws DebugException {
      synchronized (this) {
         if (callStack == null) {
            callStack = KeYModelUtil.createCallStack(getDebugTarget(), executionNode.getCallStack()); 
         }
         return callStack;
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isVerified() {
      return executionNode.isBranchVerified();
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean hasConstraints() throws DebugException {
      return !isTerminated() && super.hasConstraints();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public KeYConstraint[] getConstraints() throws DebugException {
      synchronized (this) {
         if (constraints == null) {
            constraints = KeYModelUtil.createConstraints(this, executionNode);
         }
         return constraints;
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean hasVariables() throws DebugException {
      try {
         return getDebugTarget().getLaunchSettings().isShowVariablesOfSelectedDebugNode() &&
                !executionNode.isDisposed() && 
                SymbolicExecutionUtil.canComputeVariables(executionNode, executionNode.getServices()) &&
                super.hasVariables();
      }
      catch (ProofInputException e) {
         throw new DebugException(LogUtil.getLogger().createErrorStatus(e));
      }
   }   
   
   /**
    * {@inheritDoc}
    */
   @Override
   public KeYVariable[] getVariables() throws DebugException {
      synchronized (this) {
         if (variables == null) {
            variables = KeYModelUtil.createVariables(this, executionNode);
         }
         return variables;
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getLineNumber() throws DebugException {
      return -1;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getCharStart() throws DebugException {
      return -1;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getCharEnd() throws DebugException {
      return -1;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getSourcePath() {
      IMethod method = getDebugTarget().getLaunchSettings().getMethod();
      if (method != null) {
         File localFile = ResourceUtil.getLocation(method.getResource());
         return localFile != null ? localFile.getAbsolutePath() : null;
      }
      else {
         return null;
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SEMemoryBranchCondition[] getGroupStartConditions() throws DebugException {
      synchronized (this) { // Thread save execution is required because thanks lazy loading different threads will create different result arrays otherwise.
         if (groupStartConditions == null) {
            groupStartConditions = KeYModelUtil.createCompletedBlocksConditions(this);
         }
         return groupStartConditions;
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isTruthValueTracingEnabled() {
      return SymbolicExecutionJavaProfile.isTruthValueTracingEnabled(getExecutionNode().getProof());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void addOutgoingLink(KeYNodeLink link) {
      outgoingLinks.add(link);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public KeYNodeLink[] getOutgoingLinks() throws DebugException {
      return outgoingLinks.toArray(new KeYNodeLink[outgoingLinks.size()]);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public KeYNodeLink[] getIncomingLinks() throws DebugException {
      return incomingLinks.toArray(new KeYNodeLink[incomingLinks.size()]);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void addIncomingLink(KeYNodeLink link) {
      incomingLinks.add(link);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void removeIncomingLink(KeYNodeLink link) {
      incomingLinks.remove(link);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void removeOutgoingLink(KeYNodeLink link) {
      outgoingLinks.remove(link);
   }
}