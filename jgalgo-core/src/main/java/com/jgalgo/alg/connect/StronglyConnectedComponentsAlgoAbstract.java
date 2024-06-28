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

import com.jgalgo.alg.common.IVertexPartition;
import com.jgalgo.alg.common.VertexPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;

/**
 * Abstract class for computing strongly connected components in a graph.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class StronglyConnectedComponentsAlgoAbstract implements StronglyConnectedComponentsAlgo {

	private final WeaklyConnectedComponentsAlgo weaklyConnectedComponentsAlgo =
			WeaklyConnectedComponentsAlgo.newInstance();

	/**
	 * Default constructor.
	 */
	public StronglyConnectedComponentsAlgoAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> VertexPartition<V, E> findStronglyConnectedComponents(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return (VertexPartition<V, E>) findStronglyConnectedComponents((IndexGraph) g);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IVertexPartition indexResult = findStronglyConnectedComponents(iGraph);
			return VertexPartition.partitionFromIndexPartition(g, indexResult);
		}
	}

	/**
	 * Find all strongly connected components in a graph.
	 *
	 * @see      #findStronglyConnectedComponents(Graph)
	 * @param  g a graph
	 * @return   a result object containing the partition of the vertices into strongly connected components
	 */
	protected IVertexPartition findStronglyConnectedComponents(IndexGraph g) {
		if (g.isDirected()) {
			return findStronglyConnectedComponentsDirected(g);
		} else {
			return (IVertexPartition) weaklyConnectedComponentsAlgo.findWeaklyConnectedComponents(g);
		}
	}

	@Override
	public <V, E> boolean isStronglyConnected(Graph<V, E> g) {
		return g instanceof IndexGraph ? isStronglyConnected((IndexGraph) g) : isStronglyConnected(g.indexGraph());
	}

	/**
	 * Find all strongly connected components in a graph.
	 *
	 * @see      #findStronglyConnectedComponents(Graph)
	 * @param  g a graph
	 * @return   a result object containing the partition of the vertices into strongly connected components
	 */
	protected abstract IVertexPartition findStronglyConnectedComponentsDirected(IndexGraph g);

	/**
	 * Check whether a graph is strongly connected.
	 *
	 * @see      #isStronglyConnected(Graph)
	 * @param  g a graph
	 * @return   {@code true} if the graph is strongly connected, {@code false} otherwise
	 */
	protected abstract boolean isStronglyConnected(IndexGraph g);

}
