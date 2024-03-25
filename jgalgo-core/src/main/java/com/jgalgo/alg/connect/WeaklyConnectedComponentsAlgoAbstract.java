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
 * Abstract class for computing weakly connected components in a graph.
 *
 * <p>
 * The class implements the interface by solving the problem on the index graph and then maps the results back to the
 * original graph. The implementation for the index graph is abstract and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class WeaklyConnectedComponentsAlgoAbstract implements WeaklyConnectedComponentsAlgo {

	/**
	 * Default constructor.
	 */
	public WeaklyConnectedComponentsAlgoAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> VertexPartition<V, E> findWeaklyConnectedComponents(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return (VertexPartition<V, E>) findWeaklyConnectedComponents((IndexGraph) g);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IVertexPartition indexResult = findWeaklyConnectedComponents(iGraph);
			return VertexPartition.partitionFromIndexPartition(g, indexResult);
		}
	}

	@Override
	public <V, E> boolean isWeaklyConnected(Graph<V, E> g) {
		return g instanceof IndexGraph ? isWeaklyConnected((IndexGraph) g) : isWeaklyConnected(g.indexGraph());
	}

	/**
	 * Compute all weakly connected components in a graph.
	 *
	 * @see      #findWeaklyConnectedComponents(Graph)
	 * @param  g a graph
	 * @return   a result object containing the partition of the vertices into weakly connected components
	 */
	protected abstract IVertexPartition findWeaklyConnectedComponents(IndexGraph g);

	/**
	 * Check whether a graph is weakly connected.
	 *
	 * @see      #isWeaklyConnected(Graph)
	 * @param  g a graph
	 * @return   {@code true} if the graph is weakly connected, {@code false} otherwise
	 */
	protected abstract boolean isWeaklyConnected(IndexGraph g);

}
