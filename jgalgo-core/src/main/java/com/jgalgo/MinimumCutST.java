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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;

/**
 * Minimum Cut algorithm with terminal vertices (source-sink, S-T).
 * <p>
 * Given a graph \(G=(V,E)\), a cut is a partition of \(V\) into two sets \(C, \bar{C} = V \setminus C\). Given a weight
 * function, the weight of a cut \((C,\bar{C})\) is the weight sum of all edges \((u,v)\) such that \(u\) is in \(C\)
 * and \(v\) is in \(\bar{C}\). There are two variants of the problem to find a minimum weight cut: (1) With terminal
 * vertices, and (2) without terminal vertices. In the variant with terminal vertices, we are given two special vertices
 * {@code source (S)} and {@code sink (T)} and we need to find the minimum cut \((C,\bar{C})\) such that the
 * {@code source} is in \(C\) and the {@code sink} is in \(\bar{C}\). In the variant without terminal vertices we need
 * to find the global cut, and \(C,\bar{C}\) simply must not be empty.
 * <p>
 * Algorithms implementing this interface compute the minimum cut given two terminal vertices, {@code source (S)} and
 * {@code sink (T)}.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Minimum_cut">Wikipedia</a>
 * @author Barak Ugav
 */
public interface MinimumCutST {

	/**
	 * Compute the minimum cut in a graph and a weight function with two terminal vertices.
	 * <p>
	 * Given a graph \(G=(V,E)\), a cut is a partition of \(V\) into twos sets \(C, \bar{C} = V \setminus C\). The
	 * return value of this function is the set \(C\), and \(\bar{C}\) can be computed easily by the caller if needed.
	 *
	 * @param  g                        a graph
	 * @param  w                        an edge weight function
	 * @param  source                   a special vertex that will be in \(C\)
	 * @param  sink                     a special vertex that will be in \(\bar{C}\)
	 * @return                          the cut that was computed
	 * @throws IllegalArgumentException if the source and the sink are the same vertex
	 */
	Cut computeMinimumCut(Graph g, WeightFunction w, int source, int sink);

	/**
	 * Create a new minimum cut algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumCutST} object.
	 *
	 * @return a new builder that can build {@link MinimumCutST} objects
	 */
	static MinimumCutST.Builder newBuilder() {
		return MaximumFlowPushRelabelHighestFirst::new;
	}

	/**
	 * Create a new minimum cut algorithm using a maximum flow algorithm.
	 * <p>
	 * By first computing a maximum flow between the source and the sink, the minimum cut can be realized from the
	 * maximum flow without increasing the asymptotical running time of the maximum flow algorithm running time.
	 *
	 * @param  maxFlowAlg a maximum flow algorithm
	 * @return            a minimum cut algorithm based on the provided maximum flow algorithm
	 */
	static MinimumCutST newFromMaximumFlow(MaximumFlow maxFlowAlg) {
		return MinimumCutSTUtils.buildFromMaxFlow(maxFlowAlg);
	}

	/**
	 * A builder for {@link MinimumCutST} objects.
	 *
	 * @see    MinimumCutST#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<MinimumCutST.Builder> {

		/**
		 * Create a new algorithm object for minimum cut computation.
		 *
		 * @return a new minimum cut algorithm
		 */
		MinimumCutST build();
	}

}
