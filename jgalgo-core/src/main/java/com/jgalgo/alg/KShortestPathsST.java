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
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;

/**
 * An algorithm for computing the K shortest paths between two vertices in a graph.
 *
 * <p>
 * Given a graph \(G=(V,E)\), and a weight function \(w:E \rightarrow R\), one might ask what are the K shortest paths
 * from a <i>source</i> vertex to a <i>target</i> vertex, where the 'shortest' is defined by comparing the sum of edges
 * weights of each path. This interface computes such paths. It differ from {@link ShortestPathST}, as it computes
 * multiple paths, and not just one.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    ShortestPathST
 * @see    ShortestPathSingleSource
 * @author Barak Ugav
 */
public interface KShortestPathsST {

	/**
	 * Compute the K shortest paths from a source vertex to a target vertex.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned object is a list of {@link IPath}. If {@code g} is
	 * {@link IntGraph}, prefer to pass {@link IWeightFunction} for best performance.
	 *
	 * @param  <V>    the vertices type
	 * @param  <E>    the edges type
	 * @param  g      the graph
	 * @param  w      an edge weight function
	 * @param  source the source vertex
	 * @param  target the target vertex
	 * @param  k      the number of shortest paths to compute
	 * @return        {@code k} shortest paths from the source to the target, or less if there are no such {@code k}
	 *                paths
	 */
	<V, E> List<Path<V, E>> computeKShortestPaths(Graph<V, E> g, WeightFunction<E> w, V source, V target, int k);

	/**
	 * Create a new K shortest paths algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link KShortestPathsST} object. The
	 * {@link KShortestPathsST.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link KShortestPathsST}
	 */
	static KShortestPathsST newInstance() {
		return builder().build();
	}

	/**
	 * Create a new minimum spanning tree algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link MinimumSpanningTree} objects
	 */
	static KShortestPathsST.Builder builder() {
		return new KShortestPathsST.Builder() {
			String impl;
			int fastReplacementThreshold = Integer.MAX_VALUE;
			boolean fastReplacementThresholdValid = false;

			@Override
			public KShortestPathsST build() {
				if (impl != null) {
					switch (impl) {
						case "yen":
							return new KShortestPathsSTYen();

						case "katoh-ibaraki-mine": {
							KShortestPathsSTKatohIbarakiMine algo = new KShortestPathsSTKatohIbarakiMine();
							if (fastReplacementThresholdValid)
								algo.setFastReplacementThreshold(fastReplacementThreshold);
							return algo;
						}
						case "hershberger-maxel-suri": {
							KShortestPathsSTHershbergerMaxelSuri algo = new KShortestPathsSTHershbergerMaxelSuri();
							if (fastReplacementThresholdValid)
								algo.setFastReplacementThreshold(fastReplacementThreshold);
							return algo;
						}
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				/*-
				 * Katoh-Ibaraki-Mine algorithm has a better complexity than Yen's algorithm, so theoretically we should
				 * use it for undirected graphs by default, but benchmarks (for relatively small k) showed otherwise:
				 * KShortestPathsSTKatohIbarakiMine    |V|=200 |E|=1500 k=5  avgt    3   1.716 ±  0.601  ms/op
				 * KShortestPathsSTKatohIbarakiMine  |V|=800 |E|=10000 k=15  avgt    3  38.263 ±  1.930  ms/op
				 * KShortestPathsSTKatohIbarakiMine  |V|=1500 |E|=3000 k=50  avgt    3  91.850 ± 11.972  ms/op
				 * KShortestPathsSTYen                 |V|=200 |E|=1500 k=5  avgt    3   0.435 ±  0.012  ms/op
				 * KShortestPathsSTYen               |V|=800 |E|=10000 k=15  avgt    3   8.372 ±  0.932  ms/op
				 * KShortestPathsSTYen               |V|=1500 |E|=3000 k=50  avgt    3   8.783 ±  0.181  ms/op
				 */
				// return new KShortestPathsST() {
				// private final KShortestPathsST directedAlgo = new KShortestPathsSTYen();
				// private final KShortestPathsST undirectedAlgo = new KShortestPathsSTKatohIbarakiMine();
				// @Override
				// public <V, E> List<Path<V, E>> computeKShortestPaths(Graph<V, E> g, WeightFunction<E> w, V source,
				// V target, int k) {
				// if (g.isDirected()) {
				// return directedAlgo.computeKShortestPaths(g, w, source, target, k);
				// } else {
				// return undirectedAlgo.computeKShortestPaths(g, w, source, target, k);
				// } } };
				return new KShortestPathsSTYen();
			}

			@Override
			public void setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					case "fast-replacement-threshold":
						fastReplacementThreshold = ((Integer) value).intValue();
						fastReplacementThresholdValid = true;
						break;
					default:
						KShortestPathsST.Builder.super.setOption(key, value);
				}
			}
		};
	}

	/**
	 * A builder for {@link KShortestPathsST} objects.
	 *
	 * @see    KShortestPathsST#builder()
	 * @author Barak Ugav
	 */
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new algorithm object for k shortest paths computation.
		 *
		 * @return a new k shortest paths algorithm
		 */
		KShortestPathsST build();
	}

}
