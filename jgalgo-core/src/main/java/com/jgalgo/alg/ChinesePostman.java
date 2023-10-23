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
package com.jgalgo.alg;

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;

/**
 * An algorithm for the chinese postman problem.
 * <p>
 * The chinese postman problem is to find a closed path that visits all edges in the graph at least once, with minimum
 * weight sum with respect to a given edge weight function.
 * <p>
 * The problem can be solved in polynomial time.
 *
 * @author Barak Ugav
 */
public interface ChinesePostman {

	/**
	 * Compute the shortest circuit that visits all edges in the graph at least once.
	 *
	 * @param  g a graph
	 * @param  w an edge weight function
	 * @return   a closed path that visits all edges in the graph, with minimum weight sum with respect to the given
	 *           edge weight function
	 */
	Path computeShortestEdgeVisitorCircle(Graph g, WeightFunction w);

	/**
	 * Create a new builder for chinese postman algorithms.
	 * <p>
	 * This is the recommended way to instantiate a new {@link ChinesePostman} object.
	 *
	 * @return a new builder that can build {@link ChinesePostman} objects
	 */
	static ChinesePostman.Builder newBuilder() {
		return ChinesePostmanImpl::new;
	}

	/**
	 * A builder for {@link ChinesePostman} objects.
	 *
	 * @see    ChinesePostman#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for chinese postman problem.
		 *
		 * @return a new chinese postman algorithm
		 */
		ChinesePostman build();

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default ChinesePostman.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}

	}

}
