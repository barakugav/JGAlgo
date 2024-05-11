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
package com.jgalgo.alg.connect;

import com.jgalgo.alg.IVertexPartition;
import com.jgalgo.alg.VertexPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;

/**
 * Abstract class for k-edge connected components algorithms.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class KEdgeConnectedComponentsAlgoAbstract implements KEdgeConnectedComponentsAlgo {

	/**
	 * Default constructor.
	 */
	public KEdgeConnectedComponentsAlgoAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> VertexPartition<V, E> computeKEdgeConnectedComponents(Graph<V, E> g, int k) {
		if (g instanceof IndexGraph) {
			return (VertexPartition<V, E>) computeKEdgeConnectedComponents((IndexGraph) g, k);

		} else {
			IndexGraph ig = g.indexGraph();
			IVertexPartition indexPartition = computeKEdgeConnectedComponents(ig, k);
			return VertexPartition.partitionFromIndexPartition(g, indexPartition);
		}
	}

	/**
	 * Compute the k-edge connected components of a graph.
	 *
	 * @see      #computeKEdgeConnectedComponents(Graph, int)
	 * @param  g a graph
	 * @param  k the \(k\) parameter, which define the number of edges that must be removed to disconnect each returned
	 *               connected component
	 * @return   an {@link IVertexPartition} object that represents the k-edge connected components of the graph
	 */
	protected abstract IVertexPartition computeKEdgeConnectedComponents(IndexGraph g, int k);

}
