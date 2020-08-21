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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.key_project.key4eclipse.starter.core.util.KeYUtil;
import org.key_project.key4eclipse.starter.core.util.KeYUtil.SourceLocation;
import org.key_project.sed.core.model.ISEBranchCondition;
import org.key_project.sed.core.model.ISEMethodCall;
import org.key_project.sed.core.model.ISENode;
import org.key_project.sed.core.model.impl.AbstractSEMethodCall;
import org.key_project.sed.core.model.memory.SEMemoryBranchCondition;
import org.key_project.sed.key.core.util.KeYModelUtil;
import org.key_project.sed.key.core.util.LogUtil;
import org.key_project.util.collection.ImmutableList;
import org.key_project.util.jdt.JDTUtil;

import de.uka.ilkd.key.logic.op.IProgramMethod;
import de.uka.ilkd.key.proof.init.ProofInputException;
import de.uka.ilkd.key.symbolic_execution.model.IExecutionBaseMethodReturn;
import de.uka.ilkd.key.symbolic_execution.model.IExecutionExceptionalMethodReturn;
import de.uka.ilkd.key.symbolic_execution.model.IExecutionMethodCall;
import de.uka.ilkd.key.symbolic_execution.model.IExecutionMethodReturn;
import de.uka.ilkd.key.symbolic_execution.model.IExecutionNode;
import de.uka.ilkd.key.symbolic_execution.profile.SymbolicExecutionJavaProfile;
import de.uka.ilkd.key.symbolic_execution.util.SymbolicExecutionUtil;

/**
 * Implementation of {@link ISEMethodCall} for the symbolic execution debugger (SED)
 * based on KeY.
 * @author Martin Hentschel
 */
public class KeYMethodCall extends AbstractSEMethodCall implements IKeYSENode<IExecutionMethodCall> {
   /**
    * The {@link IExecutionMethodCall} to represent by this debug node.
    */
   private final IExecutionMethodCall executionNode;

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
    * The up to know discovered {@link IKeYBaseMethodReturn} nodes.
    */
   private final Map<IExecutionBaseMethodReturn<?>, IKeYBaseMethodReturn> knownMethodReturns = new HashMap<IExecutionBaseMethodReturn<?>, IKeYBaseMethodReturn>();
   
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
    * @param executionNode The {@link IExecutionMethodCall} to represent by this debug node.
    */
   public KeYMethodCall(KeYDebugTarget target, 
                        IKeYSENode<?> parent, 
                        KeYThread thread, 
                        IExecutionMethodCall executionNode) throws DebugException {
      super(target, parent, thread);
      Assert.isNotNull(executionNode);
      this.executionNode = executionNode;
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
   public IExecutionMethodCall getExecutionNode() {
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
   public String getSourcePath() {
      if (sourceName == null) {
         sourceName = SymbolicExecutionUtil.getSourcePath(executionNode.getProgramMethod().getPositionInfo());
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
      IProgramMethod explicitConstructor = executionNode.getExplicitConstructorProgramMethod();
      SourceLocation location = KeYUtil.convertToSourceLocation(explicitConstructor != null ?
                                                                     explicitConstructor.getPositionInfo() :
                                                                     executionNode.getProgramMethod().getPositionInfo());
      // Try to update the position info with the position of the method name provided by JDT.
      try {
         if (location.getCharEnd() >= 0) {
            ICompilationUnit compilationUnit = KeYModelUtil.findCompilationUnit(this);
            if (compilationUnit != null) {
               IMethod method = JDTUtil.findJDTMethod(compilationUnit, location.getCharEnd());
               if (method != null) {
                  ISourceRange range = method.getNameRange();
                  location = new SourceLocation(-1, range.getOffset(), range.getOffset() + range.getLength());
               }
            }
         }
         return location;
      }
      catch (Exception e) {
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
    * Registers the given {@link IKeYBaseMethodReturn} of this node.
    * @param methodReturn The {@link IKeYBaseMethodReturn} to register.
    */
   public void addMehodReturn(IKeYBaseMethodReturn methodReturn) {
      synchronized (this) { // Thread save execution is required because thanks lazy loading different threads will create different result arrays otherwise.
         Assert.isNotNull(methodReturn);
         Assert.isTrue(methodReturn.getMethodCall() == this);
         IKeYBaseMethodReturn oldReturn = knownMethodReturns.put(methodReturn.getExecutionNode(), methodReturn);
         Assert.isTrue(oldReturn == null);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ISEBranchCondition[] getMethodReturnConditions() throws DebugException {
      synchronized (this) { // Thread save execution is required because thanks lazy loading different threads will create different result arrays otherwise.
         ImmutableList<IExecutionBaseMethodReturn<?>> executionReturns = executionNode.getMethodReturns();
         ISEBranchCondition[] result = new ISEBranchCondition[executionReturns.size()];
         int i = 0;
         for (IExecutionBaseMethodReturn<?> executionReturn : executionReturns) {
            IKeYBaseMethodReturn keyReturn = getMethodReturn(executionReturn);
            if (keyReturn == null) {
               // Create new method return, its parent will be set later when the full child hierarchy is explored.
               if (executionReturn instanceof IExecutionMethodReturn) {
                  keyReturn = KeYModelUtil.createMethodReturn(getDebugTarget(), getThread(), null, this, (IExecutionMethodReturn)executionReturn);
               }
               else if (executionReturn instanceof IExecutionExceptionalMethodReturn) {
                  keyReturn = KeYModelUtil.createExceptionalMethodReturn(getDebugTarget(), getThread(), null, this, (IExecutionExceptionalMethodReturn)executionReturn);
               }
               else {
                  throw new DebugException(LogUtil.getLogger().createErrorStatus("Unsupported execution return: " + executionReturn));
               }
            }
            result[i] = keyReturn.getMethodReturnCondition();
            i++;
         }
         return result;
      }
   }
   
   /**
    * Returns the {@link IKeYBaseMethodReturn} with the given {@link IExecutionMethodReturn} if available.
    * @param executionReturn The {@link IExecutionMethodReturn} to search its {@link IKeYBaseMethodReturn}.
    * @return The found {@link IKeYBaseMethodReturn} or {@code null} if not available.
    */
   public IKeYBaseMethodReturn getMethodReturn(final IExecutionBaseMethodReturn<?> executionReturn) {
      return knownMethodReturns.get(executionReturn);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ISEBranchCondition[] getGroupEndConditions() throws DebugException {
      return getMethodReturnConditions();
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
   public void setParent(ISENode parent) {
      super.setParent(parent);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isGroupable() {
      return true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isTruthValueTracingEnabled() {
      return SymbolicExecutionJavaProfile.isTruthValueTracingEnabled(getExecutionNode().getProof());
   }
   
   /**
    * Returns the map containing all method returns. 
    * @return The map containing the {@link IExecutionBaseMethodReturn}'s and their corresponding {@link IKeYBaseMethodReturn}'s.
    * @author Anna Filighera
    */
   public Map<IExecutionBaseMethodReturn<?>, IKeYBaseMethodReturn> getAllMethodReturns() {
	   return knownMethodReturns;
   }
   
   /**
    * Removes the given {@link IExecutionBaseMethodReturn} from the method return map.
    * @param methodReturn The {@link IExecutionBaseMethodReturn} to be removed.
    * @author Anna Filighera
    */
   public void removeMethodReturn(IExecutionBaseMethodReturn<?> methodReturn) {
	   knownMethodReturns.remove(methodReturn);
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