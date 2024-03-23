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
package com.jgalgo.alg.flow;

import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchEdgeException;

/**
 * Flow on {@link IntGraph} edges.
 *
 * <p>
 * This interface is a specialization of {@link Flow} for {@link IntGraph} graphs.
 *
 * @author Barak Ugav
 */
public interface IFlow extends Flow<Integer, Integer> {

	/**
	 * Get the amount of flow units going along an edge.
	 *
	 * <p>
	 * If the graph is directed, a flow of \(f\) units on {@code e}, for \(0 \leq f \leq cap(e)\), means a flow of \(f\)
	 * units of flow from {@code edgeSource(e)} to {@code edgeTarget(e)}.
	 *
	 * <p>
	 * If the graph is undirected, a flow of \(+f\) units on {@code e}, for \(0 \leq f \leq cap(e)\), means a flow of
	 * \(f\) units of flow from {@code edgeSource(e)} to {@code edgeTarget(e)}, while a flow of \(-f\) units on
	 * {@code e}, for \(-cap(e) \leq -f \leq 0\), means a flow of \(|-f|\) units of flow from {@code edgeTarget(e)} to
	 * {@code edgeSource(e)} (opposite direction).
	 *
	 * @param  edge                an edge in the graph
	 * @return                     the amount of flow units going along an edge
	 * @throws NoSuchEdgeException if {@code edge} is not a valid edge
	 */
	double getFlow(int edge);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #getFlow(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default double getFlow(Integer edge) {
		return getFlow(edge.intValue());
	}

	/**
	 * Get the sum of flow units going out of a vertex, minus the sum of flow units going into a vertex.
	 *
	 * <p>
	 * In the classical {@linkplain MaximumFlow maximum flow problem} with two terminal nodes {@code s} and {@code t},
	 * the supply of {@code s} is the total amount of flow in the network, and it is equal to the negative of the supply
	 * of {@code t}. The negative of the supply is sometimes called the demand. For any other vertex, the supply is
	 * zero.
	 *
	 *
	 * @param  vertex a vertex in the graph
	 * @return        the sum of flow units going out of a vertex, minus the sum of flow units going into a vertex
	 */
	double getSupply(int vertex);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #getSupply(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default double getSupply(Integer vertex) {
		return getSupply(vertex.intValue());
	}

}
