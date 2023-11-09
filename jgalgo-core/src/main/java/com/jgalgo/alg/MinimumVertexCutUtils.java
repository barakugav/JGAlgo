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

import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntSet;

class MinimumVertexCutUtils {

	abstract static class AbstractImplST implements MinimumVertexCutST {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Set<V> computeMinimumCut(Graph<V, E> g, WeightFunction<V> w, V source, V sink) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				int source0 = ((Integer) source).intValue(), sink0 = ((Integer) sink).intValue();
				return (Set<V>) computeMinimumCut((IndexGraph) g, w0, source0, sink0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, viMap);
				int iSource = viMap.idToIndex(source);
				int iSink = viMap.idToIndex(sink);
				IntSet indexCut = computeMinimumCut(iGraph, iw, iSource, iSink);
				return indexCut == null ? null : IndexIdMaps.indexToIdSet(indexCut, viMap);
			}
		}

		abstract IntSet computeMinimumCut(IndexGraph g, IWeightFunction w, int source, int sink);
	}

	abstract static class AbstractImplGlobal implements MinimumVertexCutGlobal {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Set<V> computeMinimumCut(Graph<V, E> g, WeightFunction<V> w) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				return (Set<V>) computeMinimumCut((IndexGraph) g, w0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, viMap);
				IntSet indexCut = computeMinimumCut(iGraph, iw);
				return indexCut == null ? null : IndexIdMaps.indexToIdSet(indexCut, viMap);
			}
		}

		abstract IntSet computeMinimumCut(IndexGraph g, IWeightFunction w);
	}

	static Pair<IndexGraph, IWeightFunction> auxiliaryGraph(IndexGraph g, IWeightFunction w) {
		final int n = g.vertices().size();
		final int m = g.edges().size();
		IndexGraphBuilder g0 = IndexGraphBuilder.newDirected();
		g0.expectedVerticesNum(n * 2);
		if (g.isDirected()) {
			g0.expectedVerticesNum(m + n);
		} else {
			g0.expectedVerticesNum(2 * m + n);
		}
		for (int v = 0; v < n; v++) {
			g0.addVertex();
			g0.addVertex();
		}
		if (g.isDirected()) {
			for (int e = 0; e < m; e++) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				g0.addEdge(u * 2 + 1, v * 2 + 0);
			}
		} else {
			for (int e = 0; e < m; e++) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				g0.addEdge(u * 2 + 1, v * 2 + 0);
				g0.addEdge(v * 2 + 1, u * 2 + 0);
			}
		}
		final int verticesEdgesThreshold = g0.edges().size();
		for (int v = 0; v < n; v++)
			g0.addEdge(v * 2 + 0, v * 2 + 1);

		IWeightFunction w0;
		if (w == null || w instanceof IWeightFunctionInt) {
			IWeightFunctionInt wInt = w == null ? IWeightFunction.CardinalityWeightFunction : (IWeightFunctionInt) w;
			long hugeWeight = 1;
			if (w != IWeightFunction.CardinalityWeightFunction)
				for (int v = 0; v < n; v++)
					hugeWeight = Math.max(hugeWeight, wInt.weightInt(v));
			hugeWeight = hugeWeight * n + 1;
			int hugeWeight0 = hugeWeight > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) hugeWeight;
			IWeightFunctionInt w0Int =
					e -> e < verticesEdgesThreshold ? hugeWeight0 : wInt.weightInt(e - verticesEdgesThreshold);
			w0 = w0Int;

		} else {
			double hugeWeight = 1;
			for (int v = 0; v < n; v++)
				hugeWeight = Math.max(hugeWeight, w.weight(v));
			double hugeWeight0 = hugeWeight * n + 1;
			w0 = e -> e < verticesEdgesThreshold ? hugeWeight0 : w.weight(e - verticesEdgesThreshold);
		}

		return Pair.of(g0.build(), w0);
	}

}
