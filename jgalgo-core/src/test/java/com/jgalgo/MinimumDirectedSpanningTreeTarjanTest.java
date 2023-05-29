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

import java.util.List;
import org.junit.jupiter.api.Test;
import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class MinimumDirectedSpanningTreeTarjanTest extends TestBase {

	private static class MDSTUndirectedWrapper implements MinimumSpanningTree {

		private final MinimumDirectedSpanningTree algo;

		MDSTUndirectedWrapper(MinimumDirectedSpanningTree algo) {
			this.algo = algo;
		}

		@Override
		public MinimumSpanningTree.Result computeMinimumSpanningTree(Graph g, EdgeWeightFunc w) {
			if (g.getCapabilities().directed())
				return algo.computeMinimumSpanningTree(g, w, 0);
			int n = g.vertices().size();
			Graph dg = GraphBuilder.newDirected().expectedVerticesNum(n).build();
			for (int i = 0; i < n; i++)
				dg.addVertex();
			Weights.Int edgeRef = dg.addEdgesWeights("edgeRef", int.class, Integer.valueOf(-1));
			for (int u = 0; u < n; u++) {
				for (EdgeIter eit = g.edgesOut(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.target();
					edgeRef.set(dg.addEdge(u, v), e);
					edgeRef.set(dg.addEdge(v, u), e);
				}
			}
			MinimumSpanningTree.Result mst0 = algo.computeMinimumSpanningTree(dg, e -> w.weight(edgeRef.getInt(e)), 0);
			IntCollection mst = new IntArrayList(mst0.edges().size());
			for (IntIterator it = mst0.edges().iterator(); it.hasNext();)
				mst.add(edgeRef.getInt(it.nextInt()));
			return new MinimumSpanningTreeResultImpl(mst);
		}
	}

	@Test
	public void testRandGraphUndirected() {
		final long seed = 0x9234356819f0ea1dL;
		MinimumSpanningTreeTestUtils.testRandGraph(new MDSTUndirectedWrapper(new MinimumDirectedSpanningTreeTarjan()), seed);
	}

	@Test
	public void testRandGraphDirected() {
		final long seed = 0xdb81d5dd5fe0d5b3L;
		testRandGraph(new MinimumDirectedSpanningTreeTarjan(), seed);
	}

	private static void testRandGraph(MinimumDirectedSpanningTree algo, long seed) {
		testRandGraph(algo, GraphBuilder.newUndirected(), seed);
	}

	static void testRandGraph(MinimumDirectedSpanningTree algo, GraphBuilder graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(1, 0, 0), phase(256, 6, 5), phase(128, 16, 32), phase(64, 64, 128),
				phase(32, 128, 256), phase(8, 1024, 4096), phase(2, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).graphImpl(graphImpl).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			testRandGraph(algo, g, w);
		});
	}

	private static void testRandGraph(MinimumDirectedSpanningTree algo, Graph g, EdgeWeightFunc w) {
		@SuppressWarnings("unused")
		MinimumSpanningTree.Result mst = algo.computeMinimumSpanningTree(g, w, 0);
		// TODO verify the result
	}

}
