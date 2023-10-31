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

import java.util.Iterator;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IWeightsObj;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class MinimumDirectedSpanningTreeTarjanTest extends TestBase {

	private static class MDSTUndirectedWrapper implements MinimumSpanningTree {

		private final MinimumDirectedSpanningTree algo;

		MDSTUndirectedWrapper(MinimumDirectedSpanningTree algo) {
			this.algo = algo;
		}

		@Override
		public <V, E> MinimumSpanningTree.Result<V, E> computeMinimumSpanningTree(Graph<V, E> g, WeightFunction<E> w) {
			if (g.isDirected())
				return algo.computeMinimumDirectedSpanningTree(g, w, g.vertices().iterator().next());
			int n = g.vertices().size();
			IntGraph dg = IntGraphFactory.newDirected().expectedVerticesNum(n).newGraph();
			for (int i = 0; i < n; i++)
				dg.addVertex();

			Object2IntMap<E> gToDg = new Object2IntOpenHashMap<>();
			Iterator<E> it1 = g.edges().iterator();
			IntIterator it2 = dg.vertices().iterator();
			for (; it1.hasNext();)
				gToDg.put(it1.next(), it2.nextInt());

			IWeightsObj<E> edgeRef = dg.addEdgesWeights("edgeRef", Object.class);
			for (V u : g.vertices()) {
				for (EdgeIter<V, E> eit = g.outEdges(u).iterator(); eit.hasNext();) {
					E e = eit.next();
					V v = eit.target();
					edgeRef.set(dg.addEdge(gToDg.getInt(u), gToDg.getInt(v)), e);
					edgeRef.set(dg.addEdge(gToDg.getInt(v), gToDg.getInt(u)), e);
				}
			}
			int root = dg.vertices().iterator().nextInt();
			IWeightFunction w0 = e -> w.weight(edgeRef.get(e));
			MinimumSpanningTree.IResult mst0 = (MinimumSpanningTree.IResult) algo.computeMinimumDirectedSpanningTree(dg,
					w0, Integer.valueOf(root));
			IntCollection mst = new IntArrayList(mst0.edges().size());
			for (int e : mst0.edges())
				mst.add(g.indexGraphEdgesMap().idToIndex(edgeRef.get(e)));
			MinimumSpanningTree.IResult indexRes = new MinimumSpanningTreeUtils.ResultImpl(mst);
			return new MinimumSpanningTreeUtils.ObjResultFromIndexResult<>(indexRes, g.indexGraphEdgesMap());
		}
	}

	@Test
	public void testRandGraphUndirected() {
		final long seed = 0x9234356819f0ea1dL;
		MinimumSpanningTreeTestUtils.testRandGraph(new MDSTUndirectedWrapper(new MinimumDirectedSpanningTreeTarjan()),
				seed);
	}

	@Test
	public void testRandGraphDirected() {
		final long seed = 0xdb81d5dd5fe0d5b3L;
		testRandGraph(new MinimumDirectedSpanningTreeTarjan(), GraphsTestUtils.defaultGraphImpl(), seed);
	}

	public static void testRandGraph(MinimumDirectedSpanningTree algo, Boolean2ObjectFunction<IntGraph> graphImpl,
			long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 5).repeat(256);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(128, 256).repeat(32);
		tester.addPhase().withArgs(1024, 4096).repeat(8);
		tester.addPhase().withArgs(4096, 16384).repeat(2);
		tester.run((n, m) -> {
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(false)
					.selfEdges(true).cycles(true).connected(false).graphImpl(graphImpl).build();
			IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			testRandGraph(algo, g, w);
		});
	}

	private static void testRandGraph(MinimumDirectedSpanningTree algo, IntGraph g, IWeightFunction w) {
		int root = g.vertices().iterator().nextInt();
		@SuppressWarnings("unused")
		MinimumSpanningTree.IResult mst =
				(MinimumSpanningTree.IResult) algo.computeMinimumDirectedSpanningTree(g, w, root);
		// TODO verify the result
	}

}
