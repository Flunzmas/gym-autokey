package de.uka.ilkd.key.speclang;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.key_project.util.collection.DefaultImmutableSet;
import org.key_project.util.collection.ImmutableList;
import org.key_project.util.collection.ImmutableSet;
import org.key_project.util.java.StringUtil;

import de.uka.ilkd.key.java.Label;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.StatementBlock;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;
import de.uka.ilkd.key.java.declaration.modifier.VisibilityModifier;
import de.uka.ilkd.key.ldt.HeapLDT;
import de.uka.ilkd.key.logic.Sorted;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.TermServices;
import de.uka.ilkd.key.logic.op.IProgramMethod;
import de.uka.ilkd.key.logic.op.LocationVariable;
import de.uka.ilkd.key.logic.op.Modality;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.op.SVSubstitute;
import de.uka.ilkd.key.pp.LogicPrinter;
import de.uka.ilkd.key.proof.OpReplacer;
import de.uka.ilkd.key.speclang.Contract.OriginalVariables;
import de.uka.ilkd.key.speclang.jml.pretranslation.Behavior;
import de.uka.ilkd.key.util.InfFlowSpec;

/**
 * Abstract base class for all default implementations of the sub-interfaces of
 * {@link AuxiliaryContract}.
 *
 * @author wacker, lanzinger
 */
public abstract class AbstractAuxiliaryContractImpl implements AuxiliaryContract {

    /**
     * @see #getBlock()
     */
    protected final StatementBlock block;

    /**
     * @see #getLabels()
     */
    protected final List<Label> labels;

    /**
     * @see #getMethod()
     */
    protected final IProgramMethod method;

    /**
     * @see #getModality()
     */
    protected final Modality modality;

    /**
     * @see #getInstantiationSelfTerm()
     */
    protected Term instantiationSelf;

    /**
     * @see #getPrecondition(LocationVariable, Services)
     */
    protected final Map<LocationVariable, Term> preconditions;

    /**
     * @see #getMby()
     */
    protected final Term measuredBy;

    /**
     * @see #getPostcondition(LocationVariable, Services)
     */
    protected final Map<LocationVariable, Term> postconditions;

    /**
     * @see #getModifiesClause(LocationVariable, Services)
     */
    protected final Map<LocationVariable, Term> modifiesClauses;

    /**
     * @see #getInfFlowSpecs()
     */
    protected ImmutableList<InfFlowSpec> infFlowSpecs;

    /**
     * @see #getVariables()
     */
    protected final Variables variables;

    /**
     * @see #isTransactionApplicable()
     */
    protected final boolean transactionApplicable;

    /**
     * @see #hasModifiesClause(LocationVariable)
     */
    protected final Map<LocationVariable, Boolean> hasMod;

    /**
     * @see #getBaseName()
     */
    protected final String baseName;

    /**
     * @see AuxiliaryContract#getFunctionalContracts()
     */
    private ImmutableSet<FunctionalAuxiliaryContract<?>> functionalContracts;

    /**
     *
     * @param baseName
     *            the base name.
     * @param block
     *            the block this contract belongs to.
     * @param labels
     *            all labels belonging to the block.
     * @param method
     *            the method containing the block.
     * @param modality
     *            this contract's modality.
     * @param preconditions
     *            this contract's preconditions on every heap.
     * @param measuredBy
     *            this contract's measured-by term.
     * @param postconditions
     *            this contract's postconditions on every heap.
     * @param modifiesClauses
     *            this contract's modifies clauses on every heap.
     * @param infFlowSpecs
     *            this contract's information flow specifications.
     * @param variables
     *            this contract's variables.
     * @param transactionApplicable
     *            whether or not this contract is applicable for transactions.
     * @param hasMod
     *            a map specifying on which heaps this contract has a modified clause.
     * @param functionalContracts
     *            the functional contracts corresponding to this contract.
     */
    public AbstractAuxiliaryContractImpl(final String baseName, final StatementBlock block,
            final List<Label> labels, final IProgramMethod method, final Modality modality,
            final Map<LocationVariable, Term> preconditions, final Term measuredBy,
            final Map<LocationVariable, Term> postconditions,
            final Map<LocationVariable, Term> modifiesClauses,
            final ImmutableList<InfFlowSpec> infFlowSpecs, final Variables variables,
            final boolean transactionApplicable, final Map<LocationVariable, Boolean> hasMod,
            ImmutableSet<FunctionalAuxiliaryContract<?>> functionalContracts) {
        assert block != null;
        assert labels != null;
        assert method != null;
        assert modality != null;
        assert preconditions != null;
        assert postconditions != null;
        assert modifiesClauses != null;
        assert variables.breakFlags != null;
        assert variables.continueFlags != null;
        assert variables.exception != null;
        assert variables.remembranceHeaps != null && variables.remembranceHeaps.size() > 0;
        assert variables.remembranceLocalVariables != null;
        this.baseName = baseName;
        this.block = block;
        this.labels = labels;
        this.method = method;
        this.modality = modality;
        this.preconditions = preconditions;
        this.measuredBy = measuredBy;
        this.postconditions = postconditions;
        this.modifiesClauses = modifiesClauses;
        this.infFlowSpecs = infFlowSpecs;
        this.variables = variables;
        this.transactionApplicable = transactionApplicable;
        this.hasMod = hasMod;
        this.functionalContracts = functionalContracts;
    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    @Override
    public StatementBlock getBlock() {
        return block;
    }

    @Override
    public List<Label> getLabels() {
        return labels;
    }

    @Override
    public IProgramMethod getMethod() {
        return method;
    }

    @Override
    public KeYJavaType getKJT() {
        return method.getContainerType();
    }

    @Override
    public Modality getModality() {
        return modality;
    }

    @Override
    public Variables getPlaceholderVariables() {
        return variables;
    }

    @Override
    public VisibilityModifier getVisibility() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTransactionApplicable() {
        return transactionApplicable;
    }

    @Override
    public boolean isReadOnly(final Services services) {
        return modifiesClauses.get(services.getTypeConverter().getHeapLDT().getHeap())
                .op() == services.getTypeConverter().getLocSetLDT().getEmpty();
    }

    @Override
    public boolean hasModifiesClause(LocationVariable heap) {
        return hasMod.get(heap);
    }

    @Override
    public Variables getVariables() {
        return variables;
    }

    @Override
    public Terms getVariablesAsTerms(Services services) {
        Term selfTerm
                = (this.variables.self != null ? services.getTermBuilder().var(this.variables.self)
                        : null);
        return variables.termify(selfTerm);
    }

    /**
     * Replaces variables in a map of terms
     *
     * @param term a term.
     * @param variables replacements for {@link #getVariables()}
     * @param services services.
     * @return the term with every occurrence of a variable from {@link #getVariables()} replaced.
     */
    public Term getTerm(
            final Term term,
            final Variables variables,
            final Services services) {
        assert variables != null;
        assert (variables.self == null) == (this.variables.self == null);
        assert services != null;

        final OpReplacer replacer = new OpReplacer(createReplacementMap(variables, services),
                services.getTermFactory());
        return replacer.replace(term);
    }

    /**
     * Replaces variables in a map of terms
     *
     * @param term a term.
     * @param heap the replacement heap
     * @param terms replacements for {@link #getVariables()}
     * @param services services.
     * @return the term with every occurrence of a variable from {@link #getVariables()} replaced.
     */
    public Term getTerm(
            final Term term,
            final Term heap,
            final Terms terms,
            final Services services) {
        assert terms != null;
        assert (terms.self == null) == (this.variables.self == null);
        assert services != null;

        final OpReplacer replacer = new OpReplacer(createReplacementMap(heap, terms, services),
                services.getTermFactory(), services.getProof());
        return replacer.replace(term);
    }

    @Override
    public Term getMby() {
        return measuredBy;
    }

    @Override
    public Term getMby(Variables variables, Services services) {
        return getTerm(measuredBy, variables, services);
    }

    @Override
    public Term getMby(ProgramVariable selfVar, Services services) {
        return getTerm(
                measuredBy,
                new Variables(selfVar, null, null, null, null, null, null,
                        null, null, null, services),
                services);
    }

    @Override
    public Term getMby(Map<LocationVariable, Term> heapTerms, Term selfTerm,
            Map<LocationVariable, Term> atPres, Services services) {
        return getTerm(
                measuredBy,
                null,
                new Terms(selfTerm, null, null, null, null, null, null, null, null, atPres),
                services);
    }

    @Override
    public Term getPrecondition(final LocationVariable heap, final ProgramVariable self,
            final Map<LocationVariable, LocationVariable> atPres, final Services services) {
        return getTerm(
                preconditions.get(heap),
                new Variables(
                        self, null, null, null, null, null, null, null, null, atPres, services),
                services);
    }

    @Override
    public Term getPrecondition(final LocationVariable heapVariable, final Term heap,
            final Term self, final Map<LocationVariable, Term> atPres, final Services services) {
        return getTerm(
                preconditions.get(heapVariable),
                heap,
                new Terms(self, null, null, null, null, null, null, null, null, atPres),
                services);
    }

    @Override
    public Term getPrecondition(final LocationVariable heap, final Services services) {
        return getPrecondition(heap, variables.self, variables.outerRemembranceVariables, services);
    }

    @Override
    public Term getPrecondition(LocationVariable heap, Variables variables, Services services) {
        return getTerm(preconditions.get(heap), variables, services);
    }

    @Override
    public Term getPrecondition(LocationVariable heapVariable, Term heap, Terms terms,
            Services services) {
        return getTerm(preconditions.get(heapVariable), heap, terms, services);
    }

    @Override
    public Term getPostcondition(final LocationVariable heap, final Variables variables,
            final Services services) {
        return getTerm(postconditions.get(heap), variables, services);
    }

    @Override
    public Term getPostcondition(final LocationVariable heapVariable, final Term heap,
            final Terms terms, final Services services) {
        return getTerm(postconditions.get(heapVariable), heap, terms, services);
    }

    @Override
    public Term getPostcondition(final LocationVariable heap, final Services services) {
        return getPostcondition(heap, variables, services);
    }

    @Override
    public Term getModifiesClause(final LocationVariable heap, final ProgramVariable self,
            final Services services) {
        return getTerm(
                modifiesClauses.get(heap),
                new Variables(self, null, null, null, null, null, null, null, null, null, services),
                services);
    }

    @Override
    public Term getModifiesClause(final LocationVariable heapVariable, final Term heap,
            final Term self, final Services services) {
        return getTerm(
                modifiesClauses.get(heapVariable),
                heap,
                new Terms(self, null, null, null, null, null, null, null, null, null),
                services);
    }

    @Override
    public Term getModifiesClause(
            final LocationVariable heap, final Variables variables, final Services services) {
        return getTerm(modifiesClauses.get(heap), variables, services);
    }

    @Override
    public Term getModifiesClause(final LocationVariable heap, final Services services) {
        return getModifiesClause(heap, variables.self, services);
    }

    @Override
    public Term getPre(Services services) {
        return preconditions.get(services.getTypeConverter().getHeapLDT().getHeap());
    }

    @Override
    public Term getRequires(LocationVariable heap) {
        return preconditions.get(heap);
    }

    @Override
    public Term getPost(Services services) {
        return postconditions.get(services.getTypeConverter().getHeapLDT().getHeap());
    }

    @Override
    public Term getEnsures(LocationVariable heap) {
        return postconditions.get(heap);
    }

    @Override
    public Term getMod(Services services) {
        return modifiesClauses.get(services.getTypeConverter().getHeapLDT().getHeap());
    }

    @Override
    public ImmutableList<InfFlowSpec> getInfFlowSpecs() {
        return infFlowSpecs;
    }

    @Override
    public boolean hasMby() {
        return measuredBy != null;
    }

    @Override
    public boolean hasInfFlowSpecs() {
        return infFlowSpecs != null;
    }

    @Override
    public void setInstantiationSelf(Term selfInstantiation) {
        this.instantiationSelf = selfInstantiation;
    }

    @Override
    public Term getInstantiationSelfTerm() {
        return instantiationSelf;
    }

    @Override
    public Term getInstantiationSelfTerm(TermServices services) {
        if (instantiationSelf != null) {
            return instantiationSelf;
        } else if (variables.self != null) {
            return services.getTermBuilder().var(variables.self);
        } else {
            return null;
        }
    }

    @Override
    public IProgramMethod getTarget() {
        return method;
    }

    @Override
    public Term getAssignable(LocationVariable heap) {
        return modifiesClauses.get(heap);
    }

    @Override
    public String getHtmlText(final Services services) {
        assert services != null;
        // TODO Clean up.
        final HeapLDT heapLDT = services.getTypeConverter().getHeapLDT();
        final LocationVariable baseHeap = heapLDT.getHeap();
        final StringBuilder stringBuilder = new StringBuilder();
        if (variables.result != null) {
            stringBuilder.append(variables.result);
            stringBuilder.append(" = ");
        } else if (method.isConstructor()) {
            stringBuilder.append(variables.self);
            stringBuilder.append(" = new ");
        }
        if (!method.isStatic() && !method.isConstructor()) {
            stringBuilder.append(variables.self);
            stringBuilder.append("#");
        }
        stringBuilder.append(method.getName());
        stringBuilder.append("()");
        stringBuilder.append(")");
        stringBuilder.append(" catch(");
        stringBuilder.append(variables.exception);
        stringBuilder.append(")");
        String mods = getHtmlMods(baseHeap, heapLDT, services);
        String pres = getHtmlPres(baseHeap, heapLDT, services);
        String posts = getHtmlPosts(baseHeap, heapLDT, services);
        return "<html>" + "<i>" + LogicPrinter.escapeHTML(stringBuilder.toString(), false) + "</i>"
                + pres + posts + mods + "<br><b>termination</b> " + getModality()
                /*
                 * + (transactionApplicableContract() ?
                 * "<br><b>transactionApplicable applicable</b>" : "")
                 */
                + "</html>";
    }

    @Override
    public String getPlainText(Services services) {
        return getPlainText(services, new Terms(variables, services.getTermBuilder()));
    }

    @Override
    public String getPlainText(final Services services, Terms terms) {
        assert services != null;
        // TODO Clean up.
        final HeapLDT heapLDT = services.getTypeConverter().getHeapLDT();
        final LocationVariable baseHeap = heapLDT.getHeap();
        final StringBuilder stringBuilder = new StringBuilder();
        if (terms.result != null) {
            stringBuilder.append(terms.result);
            stringBuilder.append(" = ");
        } else if (method.isConstructor()) {
            stringBuilder.append(terms.self);
            stringBuilder.append(" = new ");
        }
        if (!method.isStatic() && !method.isConstructor()) {
            stringBuilder.append(terms.self);
            stringBuilder.append("#");
        }
        stringBuilder.append(method.getName());
        stringBuilder.append("()");
        stringBuilder.append(")");
        stringBuilder.append(" catch(");
        stringBuilder.append(terms.exception);
        stringBuilder.append(")");
        String mods = getPlainMods(terms.self, baseHeap, heapLDT, services);
        String pres = getPlainPres(terms, baseHeap, heapLDT, services);
        String posts = getPlainPosts(terms, baseHeap, heapLDT, services);
        return stringBuilder.toString() + pres + posts + mods + "termination " + getModality();
    }

    @Override
    public OriginalVariables getOrigVars() {
        return variables.toOrigVars();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AbstractAuxiliaryContractImpl other = (AbstractAuxiliaryContractImpl) obj;
        if ((block == null && other.block != null)
                || (block != null && !block.equals(other.block))) {
            return false;
        } else if ((hasMod == null && other.hasMod != null)
                || (hasMod != null && !hasMod.equals(other.hasMod))) {
            return false;
        } else if ((infFlowSpecs == null && other.infFlowSpecs != null)
                || (infFlowSpecs != null && !infFlowSpecs.equals(other.infFlowSpecs))) {
            return false;
        } else if ((instantiationSelf == null && other.instantiationSelf != null)
                || (instantiationSelf != null
                        && !instantiationSelf.equals(other.instantiationSelf))) {
            return false;
        } else if ((labels == null && other.labels != null)
                || (labels != null && !labels.equals(other.labels))) {
            return false;
        } else if ((method == null && other.method != null)
                || (method != null && !method.equals(other.method))) {
            return false;
        } else if ((modality == null && other.modality != null)
                || (modality != null && !modality.equals(other.modality))) {
            return false;
        } else if ((modifiesClauses == null && other.modifiesClauses != null)
                || (modifiesClauses != null && !modifiesClauses.equals(other.modifiesClauses))) {
            return false;
        } else if ((postconditions == null && other.postconditions != null)
                || (postconditions != null && !postconditions.equals(other.postconditions))) {
            return false;
        } else if ((preconditions == null && other.preconditions != null)
                || (preconditions != null && !preconditions.equals(other.preconditions))) {
            return false;
        } else if (transactionApplicable != other.transactionApplicable) {
            return false;
        } else if ((variables == null && other.variables != null)
                || (variables != null && !variables.equals(other.variables))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((block == null) ? 0 : block.hashCode());
        result = prime * result + ((hasMod == null) ? 0 : hasMod.hashCode());
        result = prime * result + ((infFlowSpecs == null) ? 0 : infFlowSpecs.hashCode());
        result = prime * result + ((instantiationSelf == null) ? 0 : instantiationSelf.hashCode());
        result = prime * result + ((labels == null) ? 0 : labels.hashCode());
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((modality == null) ? 0 : modality.hashCode());
        result = prime * result + ((modifiesClauses == null) ? 0 : modifiesClauses.hashCode());
        result = prime * result + ((postconditions == null) ? 0 : postconditions.hashCode());
        result = prime * result + ((preconditions == null) ? 0 : preconditions.hashCode());
        result = prime * result + (transactionApplicable ? 1231 : 1237);
        result = prime * result + ((variables == null) ? 0 : variables.hashCode());
        return result;
    }

    @Override
    public ImmutableSet<FunctionalAuxiliaryContract<?>> getFunctionalContracts() {
        return functionalContracts;
    }

    @Override
    public void setFunctionalContract(FunctionalAuxiliaryContract<?> contract) {
        assert contract.id() != Contract.INVALID_ID;
        functionalContracts = DefaultImmutableSet.<FunctionalAuxiliaryContract<?>>nil()
                .add(contract);
    }

    /**
     *
     * @param newVariables
     *            new variables.
     * @param services
     *            services.
     * @return a map from every variable in {@link #getVariables()} to its counterpart in
     *         {@code newVariables}.
     */
    protected Map<ProgramVariable, ProgramVariable>
            createReplacementMap(final Variables newVariables, final Services services) {
        final VariableReplacementMap result = new VariableReplacementMap(services.getTermFactory());
        result.replaceSelf(variables.self, newVariables.self, services);
        result.replaceFlags(variables.breakFlags, newVariables.breakFlags, services);
        result.replaceFlags(variables.continueFlags, newVariables.continueFlags, services);
        result.replaceVariable(variables.returnFlag, newVariables.returnFlag, services);
        result.replaceVariable(variables.result, newVariables.result, services);
        result.replaceVariable(variables.exception, newVariables.exception, services);
        result.replaceRemembranceHeaps(variables.remembranceHeaps, newVariables.remembranceHeaps,
                services);
        result.replaceRemembranceLocalVariables(variables.remembranceLocalVariables,
                newVariables.remembranceLocalVariables, services);
        result.replaceRemembranceHeaps(variables.outerRemembranceHeaps,
                newVariables.outerRemembranceHeaps, services);
        result.replaceRemembranceLocalVariables(variables.outerRemembranceVariables,
                newVariables.outerRemembranceVariables, services);
        return result;
    }

    /**
     *
     * @param newHeap
     *            new base heap.
     * @param newTerms
     *            new terms.
     * @param services
     *            services.
     * @return a map from every term in {@code getVariables().termify()} to its counterpart in
     *         {@code newTerms}, and from the base heap to {@code heap}.
     */
    protected Map<Term, Term> createReplacementMap(final Term newHeap, final Terms newTerms,
            final Services services) {
        final TermReplacementMap result = new TermReplacementMap(services.getTermFactory());
        result.replaceHeap(newHeap, services);
        result.replaceSelf(variables.self, newTerms.self, services);
        result.replaceFlags(variables.breakFlags, newTerms.breakFlags, services);
        result.replaceFlags(variables.continueFlags, newTerms.continueFlags, services);
        result.replaceVariable(variables.returnFlag, newTerms.returnFlag, services);
        result.replaceVariable(variables.result, newTerms.result, services);
        result.replaceVariable(variables.exception, newTerms.exception, services);
        result.replaceRemembranceHeaps(variables.remembranceHeaps, newTerms.remembranceHeaps,
                services);
        result.replaceRemembranceLocalVariables(variables.remembranceLocalVariables,
                newTerms.remembranceLocalVariables, services);
        result.replaceRemembranceHeaps(variables.outerRemembranceHeaps,
                newTerms.outerRemembranceHeaps, services);
        result.replaceRemembranceLocalVariables(variables.outerRemembranceVariables,
                newTerms.outerRemembranceVariables, services);
        return result;
    }

    /**
     *
     * @param baseHeap
     *            base heap.
     * @param heapLDT
     *            heap LDT.
     * @param services
     *            services.
     * @return a HTML representation of this contract's modifies clauses.
     */
    private String getHtmlMods(final LocationVariable baseHeap, final HeapLDT heapLDT,
            final Services services) {
        String mods = "";
        for (LocationVariable heap : heapLDT.getAllHeaps()) {
            if (modifiesClauses.get(heap) != null) {
                mods = mods + "<br><b>mod" + (heap == baseHeap ? "" : "[" + heap + "]") + "</b> "
                        + LogicPrinter.escapeHTML(
                                LogicPrinter.quickPrintTerm(modifiesClauses.get(heap), services),
                                false);
                /*
                 * if (heap == baseHeap && !hasRealModifiesClause) { mods = mods +
                 * "<b>, creates no new objects</b>"; }
                 */
            }
        }
        return mods;
    }

    /**
     *
     * @param baseHeap
     *            base heap.
     * @param heapLDT
     *            heap LDT.
     * @param services
     *            services.
     * @return a HTML representation of this contract's preconditions.
     */
    private String getHtmlPres(final LocationVariable baseHeap, final HeapLDT heapLDT,
            final Services services) {
        String pres = "";
        for (LocationVariable heap : heapLDT.getAllHeaps()) {
            if (preconditions.get(heap) != null) {
                pres = pres + "<br><b>pre" + (heap == baseHeap ? "" : "[" + heap + "]") + "</b> "
                        + LogicPrinter.escapeHTML(
                                LogicPrinter.quickPrintTerm(preconditions.get(heap), services),
                                false);
            }
        }
        return pres;
    }

    /**
     *
     * @param baseHeap
     *            base heap.
     * @param heapLDT
     *            heap LDT.
     * @param services
     *            services.
     * @return a HTML representation of this contract's postconditions.
     */
    private String getHtmlPosts(final LocationVariable baseHeap, final HeapLDT heapLDT,
            final Services services) {
        String posts = "";
        for (LocationVariable heap : heapLDT.getAllHeaps()) {
            if (postconditions.get(heap) != null) {
                posts = posts + "<br><b>post" + (heap == baseHeap ? "" : "[" + heap + "]") + "</b> "
                        + LogicPrinter.escapeHTML(
                                LogicPrinter.quickPrintTerm(postconditions.get(heap), services),
                                false);
            }
        }
        return posts;
    }

    /**
     *
     * @param self
     *            the self term
     * @param baseHeap
     *            base heap.
     * @param heapLDT
     *            heap LDT.
     * @param services
     *            services.
     * @return a plain text representation of this contract's modifies clauses.
     */
    private String getPlainMods(Term self, final LocationVariable baseHeap, final HeapLDT heapLDT,
            final Services services) {
        String mods = "";
        for (LocationVariable heap : heapLDT.getAllHeaps()) {
            Term modifiesClause
                    = getModifiesClause(heap, services.getTermBuilder().var(heap), self, services);
            if (modifiesClause != null) {
                mods = mods + "\nmod" + (heap == baseHeap ? "" : "[" + heap + "]") + " "
                        + StringUtil.trim(LogicPrinter.quickPrintTerm(modifiesClause, services));
                /*
                 * if (heap == baseHeap && !hasRealModifiesClause) { mods = mods +
                 * "<b>, creates no new objects</b>"; }
                 */
            }
        }
        return mods;
    }

    /**
     *
     * @param terms
     *            the terms to use.
     * @param baseHeap
     *            base heap.
     * @param heapLDT
     *            heap LDT.
     * @param services
     *            services.
     * @return a plain text representation of this contract's preconditions.
     */
    private String getPlainPres(Terms terms, final LocationVariable baseHeap, final HeapLDT heapLDT,
            final Services services) {
        String pres = "";
        for (LocationVariable heap : heapLDT.getAllHeaps()) {
            Term precondition = getPrecondition(heap, services.getTermBuilder().var(baseHeap),
                    terms.self, terms.remembranceHeaps, services);
            if (precondition != null) {
                pres = pres + "\npre" + (heap == baseHeap ? "" : "[" + heap + "]") + " "
                        + StringUtil.trim(LogicPrinter.quickPrintTerm(precondition, services));
            }
        }
        return pres;
    }

    /**
     *
     * @param terms
     *            the terms to use.
     * @param baseHeap
     *            base heap.
     * @param heapLDT
     *            heap LDT.
     * @param services
     *            services.
     * @return a plain text representation of this contract's postconditions.
     */
    private String getPlainPosts(Terms terms, final LocationVariable baseHeap,
            final HeapLDT heapLDT, final Services services) {
        String posts = "";
        for (LocationVariable heap : heapLDT.getAllHeaps()) {
            Term postcondition = getPostcondition(heap, services.getTermBuilder().var(baseHeap),
                    terms, services);
            if (postcondition != null) {
                posts = posts + "\npost" + (heap == baseHeap ? "" : "[" + heap + "]") + " "
                        + StringUtil.trim(LogicPrinter.quickPrintTerm(postcondition, services));
            }
        }
        return posts;
    }

    /**
     * A map from some type to the same type.
     *
     * @param <S>
     *            the key and value type.
     */
    private abstract static class ReplacementMap<S extends Sorted & SVSubstitute>
        extends de.uka.ilkd.key.proof.ReplacementMap.NoIrrelevantLabelsReplacementMap<S, S> {

        public ReplacementMap(TermFactory tf) {
            super(tf);
        }

        /**
         * Adds a mapping for the self variable.
         *
         * @param oldSelf
         *            the old self variable.
         * @param newSelf
         *            the new self variable.
         * @param services
         *            services.
         */
        public void replaceSelf(final ProgramVariable oldSelf, final S newSelf,
                TermServices services) {
            if (newSelf != null) {
                assert newSelf.sort().extendsTrans(oldSelf.sort());
                put(convert(oldSelf, services), newSelf);
            }
        }

        /**
         * Adds a mapping for every flag.
         *
         * @param oldFlags
         *            old flags.
         * @param newFlags
         *            new flags.
         * @param services
         *            services.
         */
        public void replaceFlags(final Map<Label, ProgramVariable> oldFlags,
                final Map<Label, S> newFlags, TermServices services) {
            if (newFlags != null) {
                assert newFlags.size() == oldFlags.size();
                for (Map.Entry<Label, ProgramVariable> oldFlag : oldFlags.entrySet()) {
                    replaceVariable(oldFlag.getValue(), newFlags.get(oldFlag.getKey()), services);
                }
            }
        }

        /**
         * Adds a mapping for a variable.
         *
         * @param oldVariable
         *            old variable.
         * @param newVariable
         *            new variable.
         * @param services
         *            services.
         */
        public void replaceVariable(final ProgramVariable oldVariable, final S newVariable,
                TermServices services) {
            if (newVariable != null) {
                assert oldVariable.sort().equals(newVariable.sort());
                put(convert(oldVariable, services), newVariable);
            }
        }

        /**
         * Adds mappings for the remembrance heaps.
         *
         * @param oldRemembranceHeaps
         *            old remembrance heaps.
         * @param newRemembranceHeaps
         *            new remembrance heaps.
         * @param services
         *            services.
         */
        public void replaceRemembranceHeaps(
                final Map<LocationVariable, LocationVariable> oldRemembranceHeaps,
                final Map<LocationVariable, ? extends S> newRemembranceHeaps,
                final Services services) {
            if (newRemembranceHeaps != null) {
                for (LocationVariable heap : services.getTypeConverter().getHeapLDT()
                        .getAllHeaps()) {
                    if (heap.name().equals(HeapLDT.SAVED_HEAP_NAME)) {
                        continue;
                    }

                    if (oldRemembranceHeaps.get(heap) != null) {
                        final LocationVariable oldRemembranceHeap = oldRemembranceHeaps.get(heap);
                        final S newRemembranceHeap = newRemembranceHeaps.get(heap);
                        assert oldRemembranceHeap.sort().equals(newRemembranceHeap.sort());
                        put(convert(oldRemembranceHeap, services), newRemembranceHeap);
                    }
                }
            }
        }

        /**
         * Adds mappings for the remembrance variables.
         *
         * @param oldRemembranceLocalVariables
         *            old remembrance variables.
         * @param newRemembranceLocalVariables
         *            new remembrance variables.
         * @param services
         *            services
         */
        public void replaceRemembranceLocalVariables(
                final Map<LocationVariable, LocationVariable> oldRemembranceLocalVariables,
                final Map<LocationVariable, ? extends S> newRemembranceLocalVariables,
                final TermServices services) {
            if (newRemembranceLocalVariables != null) {
                for (LocationVariable localVariable : oldRemembranceLocalVariables.keySet()) {
                    if (newRemembranceLocalVariables.get(localVariable) != null) {
                        LocationVariable oldRemembranceLocalVariable
                                = oldRemembranceLocalVariables.get(localVariable);
                        S newRemembranceLocalVariable
                                = newRemembranceLocalVariables.get(localVariable);
                        assert oldRemembranceLocalVariable.sort()
                                .equals(newRemembranceLocalVariable.sort());
                        put(convert(oldRemembranceLocalVariable, services),
                                newRemembranceLocalVariable);
                    }
                }
            }
        }

        /**
         *
         * @param variable
         *            a variable.
         * @param services
         *            services.
         * @return a conversion of the specified variable to the type {@code S}.
         */
        protected abstract S convert(ProgramVariable variable, TermServices services);

    }

    /**
     * A replacement map for variables.
     */
    private static class VariableReplacementMap extends ReplacementMap<ProgramVariable> {

        public VariableReplacementMap(TermFactory tf) {
            super(tf);
        }

        @Override
        protected ProgramVariable convert(ProgramVariable variable, TermServices services) {
            return variable;
        }

    }

    /**
     * A replacement map for terms.
     */
    private static class TermReplacementMap extends ReplacementMap<Term> {

        public TermReplacementMap(TermFactory tf) {
            super(tf);
        }

        public void replaceHeap(final Term newHeap, final Services services) {
            assert newHeap != null;
            assert newHeap.sort().equals(services.getTypeConverter().getHeapLDT().targetSort());
            put(services.getTermBuilder().getBaseHeap(), newHeap);
        }

        @Override
        protected Term convert(ProgramVariable variable, TermServices services) {
            return services.getTermBuilder().var(variable);
        }

    }

    /**
     * This class contains a builder method for {@link AbstractAuxiliaryContractImpl}s
     * ({@link Creator#create()}). It should be overridden in every subclass.
     *
     * @param <T>
     *            the type of the subclass.
     */
    protected static abstract class Creator<T extends AuxiliaryContract>
            extends TermBuilder {

        /**
         * @see AuxiliaryContract#getBaseName()
         */
        private final String baseName;

        /**
         * @see AuxiliaryContract#getBlock()
         */
        private final StatementBlock block;

        /**
         * @see AuxiliaryContract#getLabels()
         */
        private final List<Label> labels;

        /**
         * @see AuxiliaryContract#getMethod()
         */
        private final IProgramMethod method;

        /**
         * This contract's behavior.
         */
        private final Behavior behavior;

        /**
         * @see AuxiliaryContract#getVariables()
         */
        private final Variables variables;

        /**
         * @see AuxiliaryContract#getMby()
         */
        private final Term measuredBy;

        /**
         * @see AuxiliaryContract#getRequires(LocationVariable)
         */
        private final Map<LocationVariable, Term> requires;

        /**
         * Postcondition for normal termination.
         */
        private final Map<LocationVariable, Term> ensures;

        /**
         * @see AuxiliaryContract#getInfFlowSpecs()
         */
        private final ImmutableList<InfFlowSpec> infFlowSpecs;

        /**
         * Postconditions for abrupt termination with {@code break} statements.
         */
        private final Map<Label, Term> breaks;

        /**
         * Postconditions for abrupt termination with {@code continue} statements.
         */
        private final Map<Label, Term> continues;

        /**
         * Postcondition for abrupt termination with {@code return} statements.
         */
        private final Term returns;

        /**
         * Postcondition for abrupt termination due to an uncaught exception.
         */
        private final Term signals;

        /**
         * A term specifying which uncaught exceptions may occur.
         */
        private final Term signalsOnly;

        /**
         * A diverges term.
         */
        private final Term diverges;

        /**
         * A map from every heap to an assignable term.
         */
        private final Map<LocationVariable, Term> assignables;

        /**
         * A list of heaps used in this contract.
         */
        private final ImmutableList<LocationVariable> heaps;

        /**
         * A map specifying on which heaps this contract has a modifies clause.
         */
        private final Map<LocationVariable, Boolean> hasMod;

        /**
         *
         * @param baseName
         *            the contract's base name.
         * @param block
         *            the block the contract belongs to.
         * @param labels
         *            all labels belonging to the block.
         * @param method
         *            the method containing the block.
         * @param behavior
         *            the contract's behavior.
         * @param variables
         *            the variables.
         * @param requires
         *            the contract's precondition.
         * @param measuredBy
         *            the contract's measured-by clause.
         * @param ensures
         *            the contracts postcondition due to normal termination.
         * @param infFlowSpecs
         *            the contract's information flow specifications.
         * @param breaks
         *            the contract's postconditions for abrupt termination with {@code break}
         *            statements.
         * @param continues
         *            the contract's postconditions for abrupt termination with {@code continue}
         *            statements.
         * @param returns
         *            the contract's postcondition for abrupt termination with {@code return}
         *            statements.
         * @param signals
         *            the contract's postcondition for abrupt termination due to abrupt
         *            termintation.
         * @param signalsOnly
         *            a term specifying which uncaught exceptions may occur.
         * @param diverges
         *            a diverges clause.
         * @param assignables
         *            map from every heap to an assignable term.
         * @param hasMod
         *            map specifying on which heaps this contract has a modifies clause.
         * @param services
         *            services.
         */
        public Creator(final String baseName, final StatementBlock block, final List<Label> labels,
                final IProgramMethod method, final Behavior behavior, final Variables variables,
                final Map<LocationVariable, Term> requires, final Term measuredBy,
                final Map<LocationVariable, Term> ensures,
                final ImmutableList<InfFlowSpec> infFlowSpecs, final Map<Label, Term> breaks,
                final Map<Label, Term> continues, final Term returns, final Term signals,
                final Term signalsOnly, final Term diverges,
                final Map<LocationVariable, Term> assignables,
                final Map<LocationVariable, Boolean> hasMod, final Services services) {
            super(services.getTermFactory(), services);
            this.baseName = baseName;
            this.block = block;
            this.labels = labels;
            this.method = method;
            this.behavior = behavior;
            this.variables = variables;
            this.requires = requires;
            this.measuredBy = measuredBy;
            this.ensures = ensures;
            this.infFlowSpecs = infFlowSpecs;
            this.breaks = breaks;
            this.continues = continues;
            this.returns = returns;
            this.signals = signals;
            this.signalsOnly = signalsOnly;
            this.diverges = diverges;
            this.assignables = assignables;
            this.heaps = services.getTypeConverter().getHeapLDT().getAllHeaps();
            this.hasMod = hasMod;
        }

        /**
         *
         * @return a new contract.
         */
        public ImmutableSet<T> create() {
            return create(buildPreconditions(), buildPostconditions(), buildModifiesClauses(),
                    infFlowSpecs);
        }

        /**
         *
         * @return the contract's preconditions.
         */
        protected Map<LocationVariable, Term> buildPreconditions() {
            final Map<LocationVariable, Term> result = new LinkedHashMap<LocationVariable, Term>();
            for (LocationVariable heap : heaps) {
                // Add JML precondition to precondition
                if (requires.get(heap) != null) {
                    result.put(heap, convertToFormula(requires.get(heap)));
                }

                // Add measured by term to precondition
                Term old = result.get(heap);
                Term mbyTerm;

                if (measuredBy != null && !measuredBy.equals(measuredByEmpty())) {
                    Map<Term, Term> replacementMap = new LinkedHashMap<Term, Term>();

                    for (Map.Entry<LocationVariable, LocationVariable> remembranceVariable
                            :variables.outerRemembranceVariables.entrySet()) {
                        if (remembranceVariable.getValue() != null) {
                            replacementMap.put(var(remembranceVariable.getKey()),
                                    var(remembranceVariable.getValue()));
                        }
                    }

                    for (Map.Entry<LocationVariable,
                            LocationVariable> remembranceVariable : variables.outerRemembranceHeaps
                                    .entrySet()) {
                        if (remembranceVariable.getValue() != null) {
                            replacementMap.put(var(remembranceVariable.getKey()),
                                    var(remembranceVariable.getValue()));
                        }
                    }
                    mbyTerm = measuredBy(new OpReplacer(replacementMap, services.getTermFactory(),
                            services.getProof())
                            .replace(measuredBy));
                } else {
                    mbyTerm = measuredByEmpty();
                }

                // InfFlow preconditions are without Mby term
                // (FIXME: a bit hacky for now, but works)
                if (old == null && (infFlowSpecs == null || infFlowSpecs.size() <= 0)) {
                    result.put(heap, mbyTerm);
                } else if (infFlowSpecs == null || infFlowSpecs.size() <= 0) {
                    result.put(heap, and(mbyTerm, old));
                }
            }
            return result;
        }

        /**
         *
         * @return the contract's postconditions.
         */
        protected Map<LocationVariable, Term> buildPostconditions() {
            final Map<LocationVariable, Term> postconditions
                    = new LinkedHashMap<LocationVariable, Term>();
            for (LocationVariable heap : heaps) {
                if (ensures.get(heap) != null) {
                    postconditions.put(heap, buildPostcondition(heap));
                }
            }
            return postconditions;
        }

        /**
         *
         * @param heap
         *            the heap to use.
         * @return the contract's postcondition on the specified heap.
         */
        private Term buildPostcondition(final LocationVariable heap) {
            final Term breakPostcondition = conditionPostconditions(variables.breakFlags, breaks);
            final Term continuePostcondition
                    = conditionPostconditions(variables.continueFlags, continues);
            final Term returnPostcondition = conditionPostcondition(variables.returnFlag, returns);
            final Term throwPostcondition = buildThrowPostcondition();
            // TODO Why do we handle the two cases differently?
            // Surely has something to do with transactions.
            if (heap == services.getTypeConverter().getHeapLDT().getHeap()) {
                if (behavior == Behavior.NORMAL_BEHAVIOR) {
                    return and(buildNormalTerminationCondition(),
                            convertToFormula(ensures.get(heap)));
                } else if (behavior == Behavior.BREAK_BEHAVIOR) {
                    return and(buildBreakTerminationCondition(), breakPostcondition);
                } else if (behavior == Behavior.CONTINUE_BEHAVIOR) {
                    return and(buildContinueTerminationCondition(), continuePostcondition);
                } else if (behavior == Behavior.RETURN_BEHAVIOR) {
                    return and(buildReturnTerminationCondition(), returnPostcondition);
                } else if (behavior == Behavior.EXCEPTIONAL_BEHAVIOR) {
                    return and(buildThrowTerminationCondition(), throwPostcondition);
                } else {
                    return and(
                            imp(buildNormalTerminationCondition(),
                                    convertToFormula(ensures.get(heap))),
                            breakPostcondition, continuePostcondition, returnPostcondition,
                            throwPostcondition);
                }
            } else {
                if (behavior == Behavior.NORMAL_BEHAVIOR) {
                    return and(buildNormalTerminationCondition(),
                            convertToFormula(ensures.get(heap)));
                } else {
                    return imp(buildNormalTerminationCondition(),
                            convertToFormula(ensures.get(heap)));
                }
            }
        }

        /**
         *
         * @param flags
         *            abrupt termination flags.
         * @param postconditions
         *            postconditions for abrupt termination.
         * @return a postcondition created conjunctively from the specified postconditions.
         */
        private Term conditionPostconditions(final Map<Label, ProgramVariable> flags,
                final Map<Label, Term> postconditions) {
            Term result = tt();
            for (Label label : flags.keySet()) {
                result = and(result,
                        conditionPostcondition(flags.get(label), postconditions.get(label)));
            }
            return result;
        }

        /**
         *
         * @param flag
         *            an abrupt termination flag.
         * @param postcondition
         *            a postcondition for abrupt termination with the specifed flag.
         * @return a part of the postcondition.
         */
        private Term conditionPostcondition(final ProgramVariable flag, final Term postcondition) {
            Term result = tt();
            if (flag != null) {
                result = imp(
                        equals(services.getTypeConverter().convertToLogicElement(flag), TRUE()),
                        postcondition == null ? tt() : postcondition);
            }
            return result;
        }

        /**
         *
         * @return the postcondition for abrupt termination due to an uncaught exception.
         */
        private Term buildThrowPostcondition() {
            return imp(not(equals(var(variables.exception), NULL())),
                    and(convertToFormula(signals), convertToFormula(signalsOnly)));
        }

        /**
         *
         * @return a term corresponding to {@link Behavior#NORMAL_BEHAVIOR}
         */
        private Term buildNormalTerminationCondition() {
            return and(buildNormalTerminationCondition(variables.breakFlags),
                    buildNormalTerminationCondition(variables.continueFlags),
                    buildFlagIsCondition(variables.returnFlag, FALSE()),
                    buildExceptionIsNullCondition());
        }

        /**
         *
         * @return a term corresponding to {@link Behavior#BREAK_BEHAVIOR}
         */
        private Term buildBreakTerminationCondition() {
            return and(buildAbruptTerminationCondition(variables.breakFlags),
                    buildNormalTerminationCondition(variables.continueFlags),
                    buildFlagIsCondition(variables.returnFlag, FALSE()),
                    buildExceptionIsNullCondition());
        }

        /**
         *
         * @return a term corresponding to {@link Behavior#CONTINUE_BEHAVIOR}
         */
        private Term buildContinueTerminationCondition() {
            return and(buildNormalTerminationCondition(variables.breakFlags),
                    buildAbruptTerminationCondition(variables.continueFlags),
                    buildFlagIsCondition(variables.returnFlag, FALSE()),
                    buildExceptionIsNullCondition());
        }

        /**
         *
         * @return a term corresponding to {@link Behavior#RETURN_BEHAVIOR}
         */
        private Term buildReturnTerminationCondition() {
            return and(buildNormalTerminationCondition(variables.breakFlags),
                    buildNormalTerminationCondition(variables.continueFlags),
                    buildFlagIsCondition(variables.returnFlag, TRUE()),
                    buildExceptionIsNullCondition());
        }

        /**
         *
         * @return a term corresponding to {@link Behavior#EXCEPTIONAL_BEHAVIOR}
         */
        private Term buildThrowTerminationCondition() {
            return and(buildNormalTerminationCondition(variables.breakFlags),
                    buildNormalTerminationCondition(variables.continueFlags),
                    buildFlagIsCondition(variables.returnFlag, FALSE()),
                    not(buildExceptionIsNullCondition()));
        }

        /**
         *
         * @param flags
         *            a map containing all abrupt termination flags.
         * @return a term corresponding to {@link Behavior#NORMAL_BEHAVIOR}
         */
        private Term buildNormalTerminationCondition(final Map<Label, ProgramVariable> flags) {
            Term result = tt();
            for (Label label : flags.keySet()) {
                result = and(result, buildFlagIsCondition(flags.get(label), FALSE()));
            }
            return result;
        }

        /**
         *
         * @param flags
         *            a map containing all abrupt termination flags.
         * @return a term equivalent to the negation of {@link #buildNormalTerminationCondition()}
         */
        private Term buildAbruptTerminationCondition(final Map<Label, ProgramVariable> flags) {
            Term result = ff();
            for (Label label : flags.keySet()) {
                result = or(result, buildFlagIsCondition(flags.get(label), TRUE()));
            }
            return result;
        }

        /**
         *
         * @param flag
         *            a boolean variable.
         * @param truth
         *            a boolean term.
         * @return a term which is true iff the flag is equal to the term.
         */
        private Term buildFlagIsCondition(final ProgramVariable flag, final Term truth) {
            Term result = tt();
            if (flag != null) {
                result = equals(var(flag), truth);
            }
            return result;
        }

        /**
         *
         * @return a term which is true iff {@code variables.exception == null}.
         */
        private Term buildExceptionIsNullCondition() {
            return equals(var(variables.exception), NULL());
        }

        /**
         *
         * @return the contract's modifies clauses.
         */
        private Map<LocationVariable, Term> buildModifiesClauses() {
            return assignables;
        }

        /**
         *
         * @param preconditions
         *            the contracts' preconditions.
         * @param postconditions
         *            the contracts' postconditions.
         * @param modifiesClauses
         *            the contracts' modifies clauses.
         * @param infFlowSpecs
         *            the contracts' information flow specifications.
         * @return a set of one or two contracts (depending on whether the {@code diverges} clause
         *         is trivial (i.e., {@code true} or {@code false}) or not.
         */
        private ImmutableSet<T> create(final Map<LocationVariable, Term> preconditions,
                final Map<LocationVariable, Term> postconditions,
                final Map<LocationVariable, Term> modifiesClauses,
                final ImmutableList<InfFlowSpec> infFlowSpecs) {
            ImmutableSet<T> result = DefaultImmutableSet.nil();
            final boolean transactionApplicable = modifiesClauses
                    .get(services.getTypeConverter().getHeapLDT().getSavedHeap()) != null;
            result = result.add(build(baseName, block, labels, method,
                    diverges.equals(ff()) ? Modality.DIA : Modality.BOX, preconditions, measuredBy,
                    postconditions, modifiesClauses, infFlowSpecs, variables, transactionApplicable,
                    hasMod));
            if (divergesConditionCannotBeExpressedByAModality()) {
                result = result.add(build(baseName, block, labels, method, Modality.DIA,
                        addNegatedDivergesConditionToPreconditions(preconditions), measuredBy,
                        postconditions, modifiesClauses, infFlowSpecs, variables,
                        transactionApplicable, hasMod));
            }
            return result;
        }

        /**
         *
         * @param baseName
         *            the base name.
         * @param block
         *            the block this contract belongs to.
         * @param labels
         *            all labels belonging to the block.
         * @param method
         *            the method containing the block.
         * @param modality
         *            this contract's modality.
         * @param preconditions
         *            this contract's preconditions on every heap.
         * @param measuredBy
         *            this contract's measured-by term.
         * @param postconditions
         *            this contract's postconditions on every heap.
         * @param modifiesClauses
         *            this contract's modifies clauses on every heap.
         * @param infFlowSpecs
         *            this contract's information flow specifications.
         * @param variables
         *            this contract's variables.
         * @param transactionApplicable
         *            whether or not this contract is applicable for transactions.
         * @param hasMod
         *            a map specifying on which heaps this contract has a modified clause.
         * @return an instance of {@code T} with the specified attributes.
         */
        protected abstract T build(String baseName, StatementBlock block, List<Label> labels,
                IProgramMethod method, Modality modality, Map<LocationVariable, Term> preconditions,
                Term measuredBy, Map<LocationVariable, Term> postconditions,
                Map<LocationVariable, Term> modifiesClauses,
                ImmutableList<InfFlowSpec> infFlowSpecs, Variables variables,
                boolean transactionApplicable, Map<LocationVariable, Boolean> hasMod);

        /**
         *
         * @return {@code true} iff the diverges condition can be expressed by a modality.
         */
        private boolean divergesConditionCannotBeExpressedByAModality() {
            return !diverges.equals(ff()) && !diverges.equals(tt());
        }

        /**
         *
         * @param preconditions
         *            a map containing the contract's preconditions.
         * @return a map with the negated diverges condition added to every precondition.
         */
        private Map<LocationVariable, Term> addNegatedDivergesConditionToPreconditions(
                final Map<LocationVariable, Term> preconditions) {
            final Map<LocationVariable, Term> result = new LinkedHashMap<LocationVariable, Term>();
            for (LocationVariable heap : heaps) {
                if (preconditions.get(heap) != null) {
                    result.put(heap, and(preconditions.get(heap), not(convertToFormula(diverges))));
                }
            }
            return result;
        }

    }

    /**
     * This class is used to to combine multiple contracts for the same block and apply them
     * simultaneously. It should be overridden in every subclass.
     *
     * @param <T>
     *            the type of the subclass.
     */
    protected static abstract class Combinator<T extends AuxiliaryContract>
            extends TermBuilder {

        /**
         * The contracts to combine.
         */
        protected final T[] contracts;

        /**
         * @see AuxiliaryContract#getPlaceholderVariables()
         */
        protected Variables placeholderVariables;

        /**
         * @see Variables#remembranceLocalVariables
         */
        protected Map<LocationVariable, LocationVariable> remembranceVariables;

        /**
         * @see AuxiliaryContract#getPrecondition(LocationVariable, Services)
         */
        protected final Map<LocationVariable, Term> preconditions;

        /**
         * @see AuxiliaryContract#getPostcondition(LocationVariable, Services)
         */
        protected final Map<LocationVariable, Term> postconditions;

        /**
         * @see AuxiliaryContract#getModifiesClause(LocationVariable, Services)
         */
        protected final Map<LocationVariable, Term> modifiesClauses;

        /**
         *
         * @param contracts
         *            the contracts to combine.
         * @param services
         *            services.
         */
        public Combinator(final T[] contracts, final Services services) {
            super(services.getTermFactory(), services);
            this.contracts = sort(contracts);
            preconditions = new LinkedHashMap<LocationVariable, Term>();
            postconditions = new LinkedHashMap<LocationVariable, Term>();
            modifiesClauses = new LinkedHashMap<LocationVariable, Term>();
        }

        /**
         *
         * @param contracts
         *            the contract's to sort.
         * @return an array containg the specified contracts sorted alphabetically by name.
         */
        private T[] sort(final T[] contracts) {
            // sort contracts alphabetically (for determinism)
            Arrays.sort(contracts, new Comparator<T>() {
                @Override
                public int compare(T firstContract, T secondContract) {
                    return firstContract.getName().compareTo(secondContract.getName());
                }
            });
            return contracts;
        }

        /**
         *
         * @return the combined contract.
         */
        protected abstract T combine();

        /**
         *
         * @param contract
         *            the contract whose conditions to add.
         */
        protected void addConditionsFrom(final T contract) {
            for (LocationVariable heap : services.getTypeConverter().getHeapLDT().getAllHeaps()) {
                final Term precondition = addPreconditionFrom(contract, heap);
                addPostconditionFrom(precondition, contract, heap);
                addModifiesClauseFrom(contract, heap);
            }
        }

        /**
         *
         * @param contract
         *            the contract whose precondition to add.
         * @param heap
         *            the heap to use.
         * @return the precondition.
         */
        private Term addPreconditionFrom(final T contract, final LocationVariable heap) {
            final Term precondition = contract.getPrecondition(heap, placeholderVariables.self,
                    placeholderVariables.remembranceHeaps, services);
            if (precondition != null) {
                preconditions.put(heap, orPossiblyNull(preconditions.get(heap), precondition));
            }
            return precondition;
        }

        /**
         *
         * @param postcondition
         *            the postcondition to add.
         * @param contract
         *            the contract the postcondition belongs to.
         * @param heap
         *            the heap to use.
         */
        private void addPostconditionFrom(final Term postcondition, final T contract,
                final LocationVariable heap) {
            final Term unconditionalPostcondition
                    = contract.getPostcondition(heap, placeholderVariables, services);
            if (unconditionalPostcondition != null) {
                final Term conditionalPostcondition
                        = imp(preify(postcondition), unconditionalPostcondition);
                postconditions.put(heap,
                        andPossiblyNull(postconditions.get(heap), conditionalPostcondition));
            }
        }

        /**
         *
         * @param contract
         *            the contract whose modified clause to add.
         * @param heap
         *            the heap to use.
         */
        private void addModifiesClauseFrom(final T contract, final LocationVariable heap) {
            final Term additionalModifiesClause
                    = contract.getModifiesClause(heap, placeholderVariables.self, services);
            if (additionalModifiesClause != null) {
                modifiesClauses.put(heap,
                        unionPossiblyNull(modifiesClauses.get(heap), additionalModifiesClause));
            }
        }

        /**
         *
         * @param currentCondition
         *            a condition or {@code null}.
         * @param additionalCondition
         *            a condition.
         * @return the disjunction of the conditions.
         */
        private Term orPossiblyNull(final Term currentCondition, final Term additionalCondition) {
            if (currentCondition == null) {
                return additionalCondition;
            } else {
                return or(currentCondition, additionalCondition);
            }
        }

        /**
         *
         * @param currentCondition
         *            a condition or {@code null}.
         * @param additionalCondition
         *            a condition.
         * @return the conjunction of the conditions.
         */
        private Term andPossiblyNull(final Term currentCondition, final Term additionalCondition) {
            if (currentCondition == null) {
                return additionalCondition;
            } else {
                return and(currentCondition, additionalCondition);
            }
        }

        /**
         *
         * @param currentLocationSet
         *            a location set or {@code null}.
         * @param additionalLocationSet
         *            a location set.
         * @return the union of the location sets.
         */
        private Term unionPossiblyNull(final Term currentLocationSet,
                final Term additionalLocationSet) {
            if (currentLocationSet == null) {
                return additionalLocationSet;
            } else if (additionalLocationSet == null) {
                return currentLocationSet;
            } else {
                return union(currentLocationSet, additionalLocationSet);
            }
        }

        /**
         *
         * @param formula
         *            a formula.
         * @return the formula with all variables replaced by the remembrance variables.
         */
        private Term preify(final Term formula) {
            if (formula == null) {
                return tt();
            } else {
                final Map<Term, Term> replacementMap = new LinkedHashMap<Term, Term>();

                for (Map.Entry<LocationVariable,
                        LocationVariable> remembranceVariable : remembranceVariables.entrySet()) {
                    if (remembranceVariable.getValue() != null) {
                        replacementMap.put(var(remembranceVariable.getKey()),
                                var(remembranceVariable.getValue()));
                    }
                }
                return new OpReplacer(
                        replacementMap, services.getTermFactory(), services.getProof())
                        .replace(formula);
            }
        }

    }
}
