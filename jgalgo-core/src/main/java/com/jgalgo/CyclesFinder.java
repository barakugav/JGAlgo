/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo;

import java.util.Iterator;
import com.jgalgo.graph.Graph;

/**
 * An algorithm that finds all cycles in a graph.
 *
 * @author Barak Ugav
 */
public interface CyclesFinder {

	/**
	 * Find all cycles in the given graph.
	 *
	 * @param  g a graph
	 * @return   an iterator that iteration over all cycles in the graph
	 */
	public Iterator<Path> findAllCycles(Graph g);

	/**
	 * Create a new cycles finder algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link CyclesFinder} object.
	 *
	 * @return a new builder that can build {@link CyclesFinder} objects
	 */
	static CyclesFinder.Builder newBuilder() {
		return CyclesFinderTarjan::new;
	}

	/**
	 * A builder for {@link CyclesFinder} objects.
	 *
	 * @see    CyclesFinder#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<CyclesFinder.Builder> {

		/**
		 * Create a new algorithm object for cycles computation.
		 *
		 * @return a new cycles finder algorithm
		 */
		CyclesFinder build();
	}

}
