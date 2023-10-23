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
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Algorithm that calculate a topological order of graph vertices.
 * <p>
 * A topological ordering of a directed graph is a linear ordering of its vertices such that for every directed edge
 * \((u,v)\), \(u\) comes before \(v\) in the ordering. A topological ordering exist if and only if the graph is
 * directed and acyclic (DAG).
 * <p>
 * This algorithm compute the topological ordering of a given DAG graph in linear time and space.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Topological_sorting">Wikipedia</a>
 * @author Barak Ugav
 */
public interface TopologicalOrderAlgo {

	/**
	 * Compute the topological order of a DAG vertices.
	 *
	 * @param  g                        a directed acyclic graph (DAG).
	 * @return                          a result object containing the computed order
	 * @throws IllegalArgumentException if the graph is not DAG
	 */
	TopologicalOrderAlgo.Result computeTopologicalSorting(Graph g);

	/**
	 * A result object of a {@link TopologicalOrderAlgo} algorithm.
	 *
	 * @author Barak Ugav
	 */
	static interface Result {

		/**
		 * Get all the vertices ordered in the list by the topological order.
		 *
		 * @return all the vertices ordered in the list by the topological order
		 */
		IntList orderedVertices();

		/**
		 * Get the index of a vertex in the topological order.
		 *
		 * @param  vertex the vertex
		 * @return        the index of the vertex in the topological order, in range \([0, n)\)
		 */
		int vertexOrderIndex(int vertex);

		/**
		 * Get a comparator that compare vertices by their order in the topological order.
		 *
		 * @return a comparator that compare vertices by their order in the topological order
		 */
		default IntComparator orderComparator() {
			return (v1, v2) -> Integer.compare(vertexOrderIndex(v1), vertexOrderIndex(v2));
		}

	}

	/**
	 * Create a new topological order algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link TopologicalOrderAlgo} object. The
	 * {@link TopologicalOrderAlgo.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link TopologicalOrderAlgo}
	 */
	static TopologicalOrderAlgo newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new topological order algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link TopologicalOrderAlgo} objects
	 */
	static TopologicalOrderAlgo.Builder newBuilder() {
		return TopologicalOrderAlgoImpl::new;
	}

	/**
	 * A builder for {@link TopologicalOrderAlgo} objects.
	 *
	 * @see    TopologicalOrderAlgo#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for topological order computation.
		 *
		 * @return a new topological order algorithm
		 */
		TopologicalOrderAlgo build();

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
		default TopologicalOrderAlgo.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
