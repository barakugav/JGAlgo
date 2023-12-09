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

import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Finds the k-vertex connected components of a graph.
 *
 * <p>
 * Given a graph \(G = (V, E)\) and an integer \(k\), we say that \(G\) is k-vertex connected if it has at least \(k +
 * 1\) vertices and remains connected after removing any \(k - 1\) vertices. If \(G\) is a clique of size \(k + 1\),
 * then \(G\) is k-vertex connected. The k-vertex connected components of \(G\) are the maximal k-vertex connected
 * subgraphs of \(G\). Note that for a general \(k\), the k-vertex connected components of a graph are not disjoint, and
 * their union is not necessarily the entire graph. For \(k = 1\), the k-vertex connected components are the
 * {@linkplain StronglyConnectedComponentsAlgo (strongly) connected components} of \(G\). For \(k = 2\), the k-vertex
 * connected components are the {@linkplain BiConnectedComponentsAlgo bi-connected components} of \(G\). Isolated
 * vertices (with no edges) are considered to be 0-vertex connected components (also can be treated as a clique of size
 * 1).
 *
 * <p>
 * For k-edge connected components, see {@link KEdgeConnectedComponentsAlgo}.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/K-vertex-connected_graph">Wikipedia</a>
 * @see    StronglyConnectedComponentsAlgo
 * @see    BiConnectedComponentsAlgo
 * @see    KEdgeConnectedComponentsAlgo
 * @author Barak Ugav
 */
public interface KVertexConnectedComponentsAlgo {

	/**
	 * Find all k-vertex connected components in a graph.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a graph
	 * @param  k                        the k value, non negative
	 * @return                          a result object containing the k-vertex connected components
	 * @throws IllegalArgumentException if {@code k} is negative
	 */
	<V, E> KVertexConnectedComponentsAlgo.Result<V, E> findKVertexConnectedComponents(Graph<V, E> g, int k);

	/**
	 *
	 * Result of a {@link KVertexConnectedComponentsAlgo} computation.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @author     Barak Ugav
	 */
	static interface Result<V, E> {

		/**
		 * The number of k-vertex connected components.
		 *
		 * @return the number of k-vertex connected components
		 */
		int componentsNum();

		/**
		 * The vertices of the k-vertex connected component with the given index.
		 *
		 * @param  compIndex the index of the component
		 * @return           the vertices of the component
		 */
		Set<V> componentVertices(int compIndex);

		/**
		 * The subgraph of the k-vertex connected component with the given index.
		 *
		 * @param  compIndex the index of the component
		 * @return           the subgraph of the component
		 */
		default Graph<V, E> componentSubGraph(int compIndex) {
			return graph().subGraphCopy(componentVertices(compIndex), null);
		}

		/**
		 * The graph on which the k-vertex connected components were found.
		 *
		 * @return the graph
		 */
		Graph<V, E> graph();

	}

	/**
	 * Result of a {@link KVertexConnectedComponentsAlgo} computation for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	static interface IResult extends KVertexConnectedComponentsAlgo.Result<Integer, Integer> {

		@Override
		IntSet componentVertices(int compIndex);

		@Override
		default IntGraph componentSubGraph(int compIndex) {
			return (IntGraph) KVertexConnectedComponentsAlgo.Result.super.componentSubGraph(compIndex);
		}

		@Override
		IntGraph graph();
	}

	/**
	 * Create a new k-connected components algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link KVertexConnectedComponentsAlgo} object. implementations.
	 *
	 * @return a default implementation of {@link KVertexConnectedComponentsAlgo}
	 */
	static KVertexConnectedComponentsAlgo newInstance() {
		return new KVertexConnectedComponentsWhiteMoody();
	}

}
