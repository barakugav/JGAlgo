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
package com.jgalgo.alg.distancemeasures;

import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A set of graph distance measures for {@link IntGraph}.
 *
 * <p>
 * This interface is a specific version of {@link DistanceMeasures} for {@link IntGraph}. See the generic interface for
 * the full documentation.
 *
 * @author Barak Ugav
 */
public interface IDistanceMeasures extends DistanceMeasures<Integer, Integer> {

	/**
	 * Get the eccentricity of a vertex.
	 *
	 * <p>
	 * Given a graph \(G=(V,E)\), and an edge weight function \(w:E \rightarrow \mathbb{R}\), the eccentricity of a
	 * vertex \(v \in V\) is the maximum distance from \(v\) to any other vertex in the graph. If the graph is directed,
	 * the eccentricity of a vertex \(v \in V\) is the maximum <i>directed</i> distance from \(v\) to any other vertex
	 * in the graph.
	 *
	 * @param  v a vertex
	 * @return   the eccentricity of the vertex
	 */
	double eccentricity(int v);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #eccentricity(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default double eccentricity(Integer v) {
		return eccentricity(v.intValue());
	}

	@Override
	IntSet center();

	@Override
	IntSet periphery();

}
