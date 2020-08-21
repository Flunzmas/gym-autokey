package org.key_project.sed.core.model;

import org.eclipse.debug.core.model.IStackFrame;
import org.key_project.sed.core.model.impl.AbstractSEExceptionalMethodReturn;
import org.key_project.sed.core.model.memory.SEMemoryExceptionalMethodReturn;

/**
 * A node in the symbolic execution tree which represents an exceptional method return.
 * <p>
 * A symbolic exceptional method return is also a normal stack frame ({@link IStackFrame})
 * for compatibility reasons with the Eclipse debug API.
 * </p>
 * <p>
 * Clients may implement this interface. It is recommended to subclass
 * from {@link AbstractSEExceptionalMethodReturn} instead of implementing this
 * interface directly. {@link SEMemoryExceptionalMethodReturn} is also a default
 * implementation that stores all values in the memory.
 * </p>
 * @author Martin Hentschel
 * @see ISENode
 */
public interface ISEExceptionalMethodReturn extends ISEBaseMethodReturn {
}
