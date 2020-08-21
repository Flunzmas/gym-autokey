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

package org.key_project.key4eclipse.common.ui.provider;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.key_project.util.collection.ImmutableArray;
import org.key_project.util.collection.ImmutableList;
import org.key_project.util.collection.ImmutableSet;

/**
 * General implementation of {@link IStructuredContentProvider} for
 * {@link ImmutableArray}, {@link ImmutableList} and {@link ImmutableSet}.
 * @author Martin Hentschel
 */
public class ImmutableCollectionContentProvider extends ArrayContentProvider {
    /**
     * The shared instance.
     */
    private static ImmutableCollectionContentProvider instance;

    /**
     * Returns an instance of {@link ImmutableCollectionContentProvider}. Since instances of this
     * class do not maintain any state, they can be shared between multiple
     * clients.
     * 
     * @return an instance of {@link ImmutableCollectionContentProvider}
     */
    public static ImmutableCollectionContentProvider getInstance() {
        synchronized(ImmutableCollectionContentProvider.class) {
            if (instance == null) {
                instance = new ImmutableCollectionContentProvider();
            }
            return instance;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof ImmutableArray) {
            ImmutableArray<?> immutable = (ImmutableArray<?>)inputElement;
            return immutable.toArray(new Object[immutable.size()]);
        }
        else if (inputElement instanceof ImmutableList) {
            ImmutableList<?> set = (ImmutableList<?>)inputElement;
            return set.toArray(new Object[set.size()]);
        }
        else if (inputElement instanceof ImmutableSet) {
            ImmutableSet<?> set = (ImmutableSet<?>)inputElement;
            return set.toArray(new Object[set.size()]);
        }
        else {
            return super.getElements(inputElement);
        }
    }
}