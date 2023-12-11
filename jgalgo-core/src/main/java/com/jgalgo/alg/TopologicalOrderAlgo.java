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

import java.util.Comparator;
import java.util.List;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Algorithm that calculate a topological order of graph vertices.
 *
 * <p>
 * A topological ordering of a directed graph is a linear ordering of its vertices such that for every directed edge
 * \((u,v)\), \(u\) comes before \(v\) in the ordering. A topological ordering exist if and only if the graph is
 * directed and acyclic (DAG).
 *
 * <p>
 * This algorithm compute the topological ordering of a given DAG graph in linear time and space.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Topological_sorting">Wikipedia</a>
 * @author Barak Ugav
 */
public interface TopologicalOrderAlgo {

	/**
	 * Compute the topological order of a DAG vertices.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned object is {@link TopologicalOrderAlgo.IResult}.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a directed acyclic graph (DAG).
	 * @return                          a result object containing the computed order
	 * @throws IllegalArgumentException if the graph is not DAG
	 */
	<V, E> TopologicalOrderAlgo.Result<V, E> computeTopologicalSorting(Graph<V, E> g);

	/**
	 * A result object of a {@link TopologicalOrderAlgo} algorithm.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @author     Barak Ugav
	 */
	@SuppressWarnings("unused")
	static interface Result<V, E> {

		/**
		 * Get all the vertices ordered in the list by the topological order.
		 *
		 * @return all the vertices ordered in the list by the topological order
		 */
		List<V> orderedVertices();

		/**
		 * Get the index of a vertex in the topological order.
		 *
		 * @param  vertex the vertex
		 * @return        the index of the vertex in the topological order, in range \([0, n)\)
		 */
		int vertexOrderIndex(V vertex);

		/**
		 * Get a comparator that compare vertices by their order in the topological order.
		 *
		 * @return a comparator that compare vertices by their order in the topological order
		 */
		default Comparator<V> orderComparator() {
			return (v1, v2) -> Integer.compare(vertexOrderIndex(v1), vertexOrderIndex(v2));
		}
	}

	/**
	 * A result object of a {@link TopologicalOrderAlgo} algorithm for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	static interface IResult extends TopologicalOrderAlgo.Result<Integer, Integer> {

		@Override
		IntList orderedVertices();

		/**
		 * Get the index of a vertex in the topological order.
		 *
		 * @param  vertex the vertex
		 * @return        the index of the vertex in the topological order, in range \([0, n)\)
		 */
		int vertexOrderIndex(int vertex);

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #vertexOrderIndex(int)} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default int vertexOrderIndex(Integer vertex) {
			return vertexOrderIndex(vertex.intValue());
		}

		@Override
		default IntComparator orderComparator() {
			return (v1, v2) -> Integer.compare(vertexOrderIndex(v1), vertexOrderIndex(v2));
		}
	}

	/**
	 * Create a new topological order algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link TopologicalOrderAlgo} object.
	 *
	 * @return a default implementation of {@link TopologicalOrderAlgo}
	 */
	static TopologicalOrderAlgo newInstance() {
		return new TopologicalOrderAlgoImpl();
	}

}
