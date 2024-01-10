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

import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.NoSuchVertexException;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A mapping between two graphs that preserves the structure of the graphs for {@link IntGraph}.
 *
 * <p>
 * This is a specialization of {@link IsomorphismMapping} for {@link IntGraph}. See the generic interface for more
 * details.
 *
 * @author Barak Ugav
 */
public interface IsomorphismIMapping extends IsomorphismMapping<Integer, Integer, Integer, Integer> {

	/**
	 * Map a vertex from the first graph to a vertex of the second graph.
	 *
	 * @param  vertex                the vertex to map
	 * @return                       the mapped vertex, or {@code -1} if {@code v1} is not mapped
	 * @throws NoSuchVertexException if the vertex does not exist in the first graph
	 */
	int mapVertex(int vertex);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #mapVertex(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer mapVertex(Integer vertex) {
		int v2 = mapVertex(vertex.intValue());
		return v2 < 0 ? null : Integer.valueOf(v2);
	}

	/**
	 * Map an edge from the first graph to an edge of the second graph.
	 *
	 * @param  edge                the edge to map
	 * @return                     the mapped edge, or {@code -1} if {@code e1} is not mapped
	 * @throws NoSuchEdgeException if the edge does not exist in the first graph
	 */
	int mapEdge(int edge);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #mapEdge(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer mapEdge(Integer edge) {
		int e2 = mapEdge(edge.intValue());
		return e2 < 0 ? null : Integer.valueOf(e2);
	}

	@Override
	IsomorphismIMapping inverse();

	@Override
	IntGraph sourceGraph();

	@Override
	IntGraph targetGraph();

	@Override
	IntSet mappedVertices();

	@Override
	IntSet mappedEdges();

}
