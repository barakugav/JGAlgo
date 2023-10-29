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

import java.util.List;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;

/**
 * An algorithm for computing the K shortest paths between two vertices in a graph.
 * <p>
 * Given a graph \(G=(V,E)\), and a weight function \(w:E \rightarrow R\), one might ask what are the K shortest paths
 * from a <i>source</i> vertex to a <i>target</i> vertex, where the 'shortest' is defined by comparing the sum of edges
 * weights of each path. This interface computes such paths. It differ from {@link ShortestPathST}, as it computes
 * multiple paths, and not just one.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @see    ShortestPathST
 * @see    ShortestPathSingleSource
 * @author Barak Ugav
 */
public interface KShortestPathsST {

	/**
	 * Compute the K shortest paths from a source vertex to a target vertex.
	 *
	 * @param  g      the graph
	 * @param  w      an edge weight function
	 * @param  source the source vertex
	 * @param  target the target vertex
	 * @param  k      the number of shortest paths to compute
	 * @return        {@code k} shortest paths from the source to the target, or less if there are no such {@code k}
	 *                paths
	 */
	List<Path> computeKShortestPaths(Graph g, WeightFunction w, int source, int target, int k);

	/**
	 * Create a new K shortest paths algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link KShortestPathsST} object. The
	 * {@link KShortestPathsST.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link KShortestPathsST}
	 */
	static KShortestPathsST newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new K shortest paths algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link KShortestPathsST} objects
	 */
	static KShortestPathsST.Builder newBuilder() {
		return KShortestPathsSTYen::new;
	}

	/**
	 * A builder for {@link KShortestPathsST} objects.
	 *
	 * @see    KShortestPathsST#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for K shortest paths computation.
		 *
		 * @return a new K shortest paths algorithm
		 */
		KShortestPathsST build();

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default KShortestPathsST.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
