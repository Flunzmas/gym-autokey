// This file is part of KeY - Integrated Deductive Software Design
//
// Copyright (C) 2001-2011 Universitaet Karlsruhe (TH), Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
// Copyright (C) 2011-2014 Karlsruhe Institute of Technology, Germany
//                         Technical University Darmstadt, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General
// Public License. See LICENSE.TXT for details.
//

package de.uka.ilkd.key.speclang;

import org.key_project.util.collection.ImmutableList;
import org.key_project.util.collection.ImmutableSet;

import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;
import de.uka.ilkd.key.java.statement.LabeledStatement;
import de.uka.ilkd.key.java.statement.LoopStatement;
import de.uka.ilkd.key.java.statement.MergePointStatement;
import de.uka.ilkd.key.logic.op.IProgramMethod;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.speclang.translation.SLTranslationException;

/**
 * Extracts specifications from comments.
 */
public interface SpecExtractor {

    /**
     * Returns the operation contracts for the passed operation.
     */
    public ImmutableSet<SpecificationElement> extractMethodSpecs(
            IProgramMethod pm) throws SLTranslationException;

    public ImmutableSet<SpecificationElement> extractMethodSpecs(
            IProgramMethod pm, boolean addInvariant)
            throws SLTranslationException;

    /**
     * Returns the class invariants for the passed type.
     */
    public ImmutableSet<SpecificationElement> extractClassSpecs(KeYJavaType kjt)
            throws SLTranslationException;

    /**
     * Returns the loop invariant for the passed loop (if any).
     */
    public LoopSpecification extractLoopInvariant(IProgramMethod pm,
            LoopStatement loop) throws SLTranslationException;

    /**
     * Returns the block contracts for the passed block.
     * @param method the program method
     * @param block the statement block
     * @return the block contracts
     */
    public ImmutableSet<BlockContract> extractBlockContracts(
            IProgramMethod method, StatementBlock block)
            throws SLTranslationException;

    /**
     * Returns the loop contracts for the passed block.
     * @param method the program method containing the block.
     * @param block the block.
     * @return the loop contracts
     * @throws SLTranslationException a translation exception
     */
    public ImmutableSet<LoopContract> extractLoopContracts(
            IProgramMethod method, StatementBlock block)
            throws SLTranslationException;

    /**
     * Returns the loop contracts for the passed loop.
     * @param method the program method containing the loop.
     * @param loop the loop.
     * @return the loop contracts
     * @throws SLTranslationException a translation exception
     */
    public ImmutableSet<LoopContract> extractLoopContracts(
            IProgramMethod method, LoopStatement loop)
            throws SLTranslationException;

    /**
     * Returns the {@link MergeContract}s for the given
     * {@link MergePointStatement}.
     *
     * @param methodParams
     *            TODO
     */
    public ImmutableSet<MergeContract> extractMergeContracts(
            IProgramMethod method, MergePointStatement mps,
            ImmutableList<ProgramVariable> methodParams)
            throws SLTranslationException;

    /**
     * Returns the block contracts for the passed labeled statement if it labels
     * a block.
     * @param method the program method
     * @param labeled the labeled statement
     * @return the block contracts
     * @throws SLTranslationException a translation exception
     */
    public ImmutableSet<BlockContract> extractBlockContracts(
            IProgramMethod method, LabeledStatement labeled)
            throws SLTranslationException;

    /**
     * Returns the loop contracts for the passed labeled statement if it labels
     * a block.
     * @param method the program method
     * @param labeled the labeled statement
     * @return the loop contracts
     * @throws SLTranslationException a translation exception
     */
    public ImmutableSet<LoopContract> extractLoopContracts(
            IProgramMethod method, LabeledStatement labeled)
            throws SLTranslationException;

    /**
     * Returns all warnings generated so far in the translation process. (e.g.
     * this may warn about unsupported features which have been ignored by the
     * translation)
     */
    public ImmutableSet<PositionedString> getWarnings();
}