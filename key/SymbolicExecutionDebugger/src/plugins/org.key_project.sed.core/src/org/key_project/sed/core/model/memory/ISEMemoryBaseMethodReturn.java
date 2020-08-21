package org.key_project.sed.core.model.memory;

import org.eclipse.debug.core.model.IVariable;
import org.key_project.sed.core.model.ISEBaseMethodReturn;

/**
 * Defines the public methods to edit an {@link ISEBaseMethodReturn} in
 * the main memory.
 * @author Martin Hentschel
 */
public interface ISEMemoryBaseMethodReturn extends ISEMemoryNode, ISEBaseMethodReturn {
   /**
    * Adds the given {@link IVariable} of the call state.
    * @param variable The {@link IVariable} of the call state to add.
    */
   public void addCallStateVariable(IVariable variable);
}
