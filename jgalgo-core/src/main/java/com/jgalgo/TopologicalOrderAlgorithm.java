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

/**
 * Algorithm that calculate a topological order of graph vertices.
 * <p>
 * A topological ordering of a directed graph is a linear ordering of its vertices such that for every directed edge
 * \((u,v)\), \(u\) comes before \(v\) in the ordering. A topological ordering exist if and only if the graph is
 * directed and acyclic (DAG).
 * <p>
 * This algorithm compute the topological ordering of a given DAG graph in linear time and space.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Topological_sorting">Wikipedia</a>
 * @author Barak Ugav
 */
public interface TopologicalOrderAlgorithm {

	/**
	 * Compute the topological order of a DAG vertices.
	 *
	 * @param  g                        a directed acyclic graph (DAG).
	 * @return                          an array of size \(n\) with the vertices of the graph order in the topological
	 *                                  order.
	 * @throws IllegalArgumentException if the graph is not DAG
	 */
	public int[] computeTopologicalSorting(Graph g);

	/**
	 * Create a new topological order algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link TopologicalOrderAlgorithm} object.
	 *
	 * @return a new builder that can build {@link TopologicalOrderAlgorithm} objects
	 */
	static TopologicalOrderAlgorithm.Builder newBuilder() {
		return TopologicalOrderAlgorithmImpl::new;
	}

	/**
	 * A builder for {@link TopologicalOrderAlgorithm} objects.
	 *
	 * @see    TopologicalOrderAlgorithm#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for topological order computation.
		 *
		 * @return a new topological order algorithm
		 */
		TopologicalOrderAlgorithm build();
	}

}