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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.BuilderAbstract;

/**
 * Global Minimum Cut algorithm without terminal vertices.
 * <p>
 * Given a graph \(G=(V,E)\), a cut is a partition of \(V\) into two sets \(C, \bar{C} = V \setminus C\). Given a weight
 * function, the weight of a cut \((C,\bar{C})\) is the weight sum of all edges \((u,v)\) such that \(u\) is in \(C\)
 * and \(v\) is in \(\bar{C}\). There are two variants of the problem to find a minimum weight cut: (1) With terminal
 * vertices, and (2) without terminal vertices. In the variant with terminal vertices, we are given two special vertices
 * {@code source (S)} and {@code sink (T)} and we need to find the minimum cut \((C,\bar{C})\) such that the
 * {@code source} is in \(C\) and the {@code sink} is in \(\bar{C}\). In the variant without terminal vertices we need
 * to find the global cut, and \(C,\bar{C}\) simply must not be empty.
 * <p>
 * Algorithms implementing this interface compute the global minimum cut without terminal vertices.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Minimum_cut">Wikipedia</a>
 * @author Barak Ugav
 */
public interface MinimumCutGlobal {

	/**
	 * Compute the global minimum cut in a graph.
	 * <p>
	 * Given a graph \(G=(V,E)\), a cut is a partition of \(V\) into twos sets \(C, \bar{C} = V \setminus C\). The
	 * return value of this function is the set \(C\), and \(\bar{C}\) can be computed easily by the caller if needed.
	 *
	 * @param  g                        a graph
	 * @param  w                        an edge weight function
	 * @return                          the cut that was computed
	 * @throws IllegalArgumentException if the graph has less than two vertices
	 */
	Cut computeMinimumCut(Graph g, WeightFunction w);

	/**
	 * Create a new global minimum cut algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumCutGlobal} object.
	 *
	 * @return a new builder that can build {@link MinimumCutGlobal} objects
	 */
	static MinimumCutGlobal.Builder newBuilder() {
		return MinimumCutGlobalStoerWagner::new;
	}

	/**
	 * A builder for {@link MinimumCutGlobal} objects.
	 *
	 * @see    MinimumCutGlobal#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<MinimumCutGlobal.Builder> {

		/**
		 * Create a new algorithm object for global minimum cut computation.
		 *
		 * @return a new minimum cut algorithm
		 */
		MinimumCutGlobal build();
	}
}
