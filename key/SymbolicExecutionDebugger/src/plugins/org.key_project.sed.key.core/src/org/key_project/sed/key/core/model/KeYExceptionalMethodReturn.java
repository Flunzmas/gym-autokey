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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.key_project.key4eclipse.starter.core.util.KeYUtil.SourceLocation;
import org.key_project.sed.core.model.ISEExceptionalMethodReturn;
import org.key_project.sed.core.model.ISENode;
import org.key_project.sed.core.model.ISEVariable;
import org.key_project.sed.core.model.impl.AbstractSEExceptionalMethodReturn;
import org.key_project.sed.core.model.memory.SEMemoryBranchCondition;
import org.key_project.sed.key.core.util.KeYModelUtil;
import org.key_project.sed.key.core.util.LogUtil;
import org.key_project.util.java.ArrayUtil;

import de.uka.ilkd.key.proof.init.ProofInputException;
import de.uka.ilkd.key.symbolic_execution.model.IExecutionExceptionalMethodReturn;
import de.uka.ilkd.key.symbolic_execution.model.IExecutionNode;
import de.uka.ilkd.key.symbolic_execution.profile.SymbolicExecutionJavaProfile;
import de.uka.ilkd.key.symbolic_execution.util.SymbolicExecutionUtil;

/**
 * Implementation of {@link ISEExceptionalMethodReturn} for the symbolic execution debugger (SED)
 * based on KeY.
 * @author Martin Hentschel
 */
public class KeYExceptionalMethodReturn extends AbstractSEExceptionalMethodReturn implements IKeYSENode<IExecutionExceptionalMethodReturn>, IKeYBaseMethodReturn {
   /**
    * The {@link IExecutionExceptionalMethodReturn} to represent by this debug node.
    */
   private final IExecutionExceptionalMethodReturn executionNode;

   /**
    * The contained children.
    */
   private IKeYSENode<?>[] children;

   /**
    * The source name.
    */
   private String sourceName;

   /**
    * The {@link SourceLocation} of this {@link IStackFrame}.
    */
   private SourceLocation sourceLocation;
   
   /**
    * The contained KeY variables.
    */
   private KeYVariable[] variables;
   
   /**
    * The constraints
    */
   private KeYConstraint[] constraints;

   /**
    * The method call stack.
    */
   private IKeYSENode<?>[] callStack;

   /**
    * The condition under which this method return is reached from its calling node.
    */
   private SEMemoryBranchCondition methodReturnCondition;
   
   /**
    * The {@link KeYMethodCall} which is now returned.
    */
   private final KeYMethodCall methodCall;
   
   /**
    * The conditions under which a group ending in this node starts.
    */
   private SEMemoryBranchCondition[] groupStartConditions;
   
   /**
    * The contained KeY variables at the call state.
    */
   private KeYVariable[] callStateVariables;
   
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
    * @param methodCall The {@link KeYMethodCall} which is now returned.
    * @param executionNode The {@link IExecutionExceptionalMethodReturn} to represent by this debug node.
    */
   public KeYExceptionalMethodReturn(KeYDebugTarget target, 
                          IKeYSENode<?> parent, 
                          KeYThread thread, 
                          KeYMethodCall methodCall,
                          IExecutionExceptionalMethodReturn executionNode) throws DebugException {
      super(target, parent, thread);
      Assert.isNotNull(executionNode);
      Assert.isNotNull(methodCall);
      this.methodCall = methodCall;
      this.executionNode = executionNode;
      getMethodCall().addMehodReturn(this);
      target.registerDebugNode(this);
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
   public IExecutionExceptionalMethodReturn getExecutionNode() {
      return executionNode;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getName() throws DebugException {
      try {
         if (getDebugTarget().isShowSignatureOnMethodReturnNodes()) {
            return executionNode.getSignature();
         }
         else {
            return executionNode.getName();
         }
      }
      catch (ProofInputException e) {
         throw new DebugException(LogUtil.getLogger().createErrorStatus("Can't compute method return name including return value.", e));
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getSourcePath() {
      if (sourceName == null) {
         KeYMethodCall debugCallNode = getMethodCall();
         sourceName = debugCallNode != null ? debugCallNode.getSourcePath() : null;
      }
      return sourceName;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getLineNumber() throws DebugException {
      if (sourceLocation == null) {
         sourceLocation = computeSourceLocation();
      }
      return sourceLocation.getLineNumber();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getCharStart() throws DebugException {
      if (sourceLocation == null) {
         sourceLocation = computeSourceLocation();
      }
      return sourceLocation.getCharStart();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getCharEnd() throws DebugException {
      if (sourceLocation == null) {
         sourceLocation = computeSourceLocation();
      }
      return sourceLocation.getCharEnd();
   }
   
   /**
    * Computes the {@link SourceLocation} which values are returned via
    * {@link #getLineNumber()}, {@link #getCharStart()} and {@link #getCharEnd()}.
    * @return The computed {@link SourceLocation}.
    * @throws DebugException Occurred Exception.
    */
   protected SourceLocation computeSourceLocation() throws DebugException {
      KeYMethodCall debugCallNode = getMethodCall();
      return debugCallNode != null ? 
             new SourceLocation(debugCallNode.getLineNumber(), debugCallNode.getCharStart(), debugCallNode.getCharEnd()) : 
             null;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public KeYMethodCall getMethodCall() {
      return methodCall;
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
   public ISEVariable[] getCallStateVariables() throws DebugException {
      synchronized (this) {
         if (callStateVariables == null) {
            callStateVariables = KeYModelUtil.createCallStateVariables(this, executionNode);
         }
         return callStateVariables;
      }
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
   public boolean canStepInto() {
      return getThread().canStepInto(this);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void stepInto() throws DebugException {
      getThread().stepInto(this);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean canStepOver() {
      return getThread().canStepOver(this);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void stepOver() throws DebugException {
      getThread().stepOver(this);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean canStepReturn() {
      return getThread().canStepReturn(this);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void stepReturn() throws DebugException {
      getThread().stepReturn(this);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean canResume() {
      return getThread().canResume(this);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void resume() throws DebugException {
      getThread().resume(this);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean canSuspend() {
      return getThread().canSuspend(this);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void suspend() throws DebugException {
      getThread().suspend(this);
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
   public SEMemoryBranchCondition getMethodReturnCondition() throws DebugException {
      try {
         synchronized (this) { // Thread save execution is required because thanks lazy loading different threads will create different result arrays otherwise.
            if (methodReturnCondition == null) {
               KeYMethodCall methodCall = getMethodCall();
               methodReturnCondition = new SEMemoryBranchCondition(getDebugTarget(), methodCall, getThread());
               methodReturnCondition.addChild(this);
               methodReturnCondition.setCallStack(KeYModelUtil.createCallStack(methodCall.getDebugTarget(), methodCall.getExecutionNode().getCallStack()));
               methodReturnCondition.setName(executionNode.getFormatedMethodReturnCondition());
               methodReturnCondition.setPathCondition(methodCall.getPathCondition());
               methodReturnCondition.setSourcePath(methodCall.getSourcePath());
            }
            return methodReturnCondition;
         }
      }
      catch (ProofInputException e) {
         throw new DebugException(LogUtil.getLogger().createErrorStatus("Can't compute method return condition.", e));
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
   public SEMemoryBranchCondition[] getGroupStartConditions() throws DebugException {
      synchronized (this) { // Thread save execution is required because thanks lazy loading different threads will create different result arrays otherwise.
         if (groupStartConditions == null) {
            SEMemoryBranchCondition returnCondition = getMethodReturnCondition();
            SEMemoryBranchCondition[] completedBlockConditions = KeYModelUtil.createCompletedBlocksConditions(this);
            if (returnCondition != null) {
               groupStartConditions = ArrayUtil.insert(completedBlockConditions, returnCondition, 0);
               KeYModelUtil.sortyByOccurrence(this, groupStartConditions); // Sort conditions to ensure order of occurrence // TODO: To increase performance use binary insertion instead of sorting
            }
            else {
               groupStartConditions = completedBlockConditions;
            }
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