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
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;

/**
 * An algorithm for the chinese postman problem.
 *
 * <p>
 * The chinese postman problem is to find a closed path that visits all edges in the graph at least once, with minimum
 * weight sum with respect to a given edge weight function.
 *
 * <p>
 * The problem can be solved in polynomial time.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @author Barak Ugav
 */
public interface ChinesePostman {

	/**
	 * Compute the shortest circuit that visits all edges in the graph at least once.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned object is {@link IPath}. If {@code g} is {@link IntGraph}, prefer
	 * to pass {@link IWeightFunction} for best performance.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @param  w   an edge weight function
	 * @return     a closed path that visits all edges in the graph, with minimum weight sum with respect to the given
	 *             edge weight function
	 */
	<V, E> Path<V, E> computeShortestEdgeVisitorCircle(Graph<V, E> g, WeightFunction<E> w);

	/**
	 * Create a new algorithm object for chinese postman problem.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link ChinesePostman} object. The
	 * {@link ChinesePostman.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link ChinesePostman}
	 */
	static ChinesePostman newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new builder for chinese postman algorithms.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
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
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new algorithm object for chinese postman problem.
		 *
		 * @return a new chinese postman algorithm
		 */
		ChinesePostman build();
	}

}
