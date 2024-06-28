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
package com.jgalgo.alg.color;

import com.jgalgo.alg.common.IVertexPartition;
import com.jgalgo.alg.common.VertexPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;

/**
 * Abstract class for coloring algorithms.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class ColoringAlgoAbstract implements ColoringAlgo {

	/**
	 * Default constructor.
	 */
	public ColoringAlgoAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> VertexPartition<V, E> computeColoring(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return (VertexPartition<V, E>) computeColoring((IndexGraph) g);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IVertexPartition indexResult = computeColoring(iGraph);
			return VertexPartition.partitionFromIndexPartition(g, indexResult);
		}
	}

	/**
	 * Assign a color to each vertex of the given graph, resulting in a valid coloring.
	 *
	 * @see      #computeColoring(Graph)
	 * @param  g a graph
	 * @return   a valid coloring with (hopefully) small number of different colors
	 */
	protected abstract IVertexPartition computeColoring(IndexGraph g);

}
