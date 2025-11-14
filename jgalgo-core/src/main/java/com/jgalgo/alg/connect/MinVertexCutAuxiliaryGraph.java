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

import static com.jgalgo.internal.util.Range.range;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexGraphFactory;
import com.jgalgo.graph.WeightFunction;

class MinVertexCutAuxiliaryGraph {

	final IndexGraph graph;
	final IWeightFunction weights;
	private final int verticesEdgesThreshold;

	MinVertexCutAuxiliaryGraph(IndexGraph g, IWeightFunction w) {
		this(g, w, false);
	}

	MinVertexCutAuxiliaryGraph(IndexGraph g, IWeightFunction w, boolean mutable) {
		final int n = g.vertices().size();
		final int m = g.edges().size();
		IndexGraphBuilder builder = IndexGraphFactory.directed().allowParallelEdges().newBuilder();
		builder.addVertices(range(n * 2));
		if (g.isDirected()) {
			builder.ensureEdgeCapacity(m + n);
		} else {
			builder.ensureEdgeCapacity(2 * m + n);
		}

		if (WeightFunction.isCardinality(w)) {

			/*
			 * The reduction in the paper from unweighted minimum vertex-cut to minimum edge-cut create one and two
			 * edges for each edge in the original graph for directed and undirected graphs, respectively. We use two
			 * and four edges, respectively. The reduction in the paper is correct in that the minimum edge-cut
			 * <b>value</b> in the auxiliary graph is the same as the minimum vertex-cut <b>value</b> in the original
			 * graph. However, we want that the minimum edge-cut cross edges will be edges connecting pairs of
			 * duplicated edges only. Therefore, we add additional edges to force the min edge cut will not contain any
			 * of the edges connecting the original vertices, without weights.
			 */

			if (g.isDirected()) {
				for (int e : range(m)) {
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					if (u == v)
						continue;
					builder.addEdge(u * 2 + 1, v * 2 + 0);
					builder.addEdge(u * 2 + 1, v * 2 + 0);
				}
			} else {
				for (int e : range(m)) {
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					if (u == v)
						continue;
					builder.addEdge(u * 2 + 1, v * 2 + 0);
					builder.addEdge(u * 2 + 1, v * 2 + 0);
					builder.addEdge(v * 2 + 1, u * 2 + 0);
					builder.addEdge(v * 2 + 1, u * 2 + 0);
				}
			}
			verticesEdgesThreshold = builder.edges().size();
			weights = null;

		} else {
			if (g.isDirected()) {
				for (int e : range(m)) {
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					if (u == v)
						continue;
					builder.addEdge(u * 2 + 1, v * 2 + 0);
				}
			} else {
				for (int e : range(m)) {
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					if (u == v)
						continue;
					builder.addEdge(u * 2 + 1, v * 2 + 0);
					builder.addEdge(v * 2 + 1, u * 2 + 0);
				}
			}
			verticesEdgesThreshold = builder.edges().size();

			if (WeightFunction.isInteger(w)) {
				IWeightFunctionInt wInt = (IWeightFunctionInt) w;
				long hugeWeight0 = 1 + n * range(n).mapToLong(wInt::weightInt).sum();
				final int hugeWeight = (int) Math.min(hugeWeight0, Integer.MAX_VALUE);
				IWeightFunctionInt w0Int =
						e -> e < verticesEdgesThreshold ? hugeWeight : wInt.weightInt(e - verticesEdgesThreshold);
				weights = w0Int;

			} else {
				final double hugeWeight = 1 + n * range(n).mapToDouble(w::weight).sum();
				weights = e -> e < verticesEdgesThreshold ? hugeWeight : w.weight(e - verticesEdgesThreshold);
			}

		}

		for (int v : range(n))
			builder.addEdge(v * 2 + 0, v * 2 + 1);
		graph = mutable ? builder.buildMutable() : builder.build();
	}

	void edgeCutToVertexCut(int[] edgeCut) {
		for (int i : range(edgeCut.length)) {
			assert edgeCut[i] >= verticesEdgesThreshold;
			edgeCut[i] -= verticesEdgesThreshold;
		}
	}

}
