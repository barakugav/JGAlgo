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

import java.util.Iterator;
import com.jgalgo.graph.Graph;

/**
 * An algorithm that finds all simple paths between a source and a target.
 * <p>
 * Given a graph \(G=(V,E)\), a path is a sequence of edges \(e_1,e_2,\ldots,e_k\) such that \(e_i=(v_{i-1},v_i)\) and
 * \(v_i\neq v_j\) for \(i\neq j\). A simple path is a path that does not contain a cycle, namely the vertices visited
 * by the path are distinct. Algorithms implementing this interface find all simple paths between a source and a target
 * vertices in a given graph. Note that there may be exponentially many simple paths between two vertices in a graph.
 *
 * @author Barak Ugav
 */
public interface SimplePathsFinder {

	/**
	 * Find all the simple paths between a source and a target vertices in the given graph.
	 *
	 * @param  g      a graph
	 * @param  source the source vertex
	 * @param  target the target vertex
	 * @return        an iterator that iteration over all simple paths between the two vertices in the graph
	 */
	 Iterator<Path> findAllSimplePaths(Graph g, int source, int target);

	/**
	 * Create a new algorithm for simple paths finding.
	 * <p>
	 * This is the recommended way to instantiate a new {@link SimplePathsFinder} object. The
	 * {@link SimplePathsFinder.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link SimplePathsFinder}
	 */
	static SimplePathsFinder newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new simple paths finder algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link SimplePathsFinder} objects
	 */
	static SimplePathsFinder.Builder newBuilder() {
		return SimplePathsFinderSedgewick::new;
	}

	/**
	 * A builder for {@link SimplePathsFinder} objects.
	 *
	 * @see    SimplePathsFinder#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for simple paths computation.
		 *
		 * @return a new simple paths finder algorithm
		 */
		SimplePathsFinder build();

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
		default SimplePathsFinder.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
