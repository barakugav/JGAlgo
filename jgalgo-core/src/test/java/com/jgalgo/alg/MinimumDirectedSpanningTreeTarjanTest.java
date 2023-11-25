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

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Iterator;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightsObj;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class MinimumDirectedSpanningTreeTarjanTest extends TestBase {

	private static class MdstUndirectedWrapper implements MinimumSpanningTree {

		private final MinimumDirectedSpanningTree algo;

		MdstUndirectedWrapper(MinimumDirectedSpanningTree algo) {
			this.algo = algo;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> MinimumSpanningTree.Result<V, E> computeMinimumSpanningTree(Graph<V, E> g, WeightFunction<E> w) {
			if (g.isDirected())
				return algo.computeMinimumDirectedSpanningTree(g, w, g.vertices().iterator().next());
			int n = g.vertices().size();
			IntGraph dg = IntGraphFactory.newDirected().expectedVerticesNum(n).newGraph();
			for (int i = 0; i < n; i++)
				dg.addVertex();

			Object2IntMap<V> gToDg = new Object2IntOpenHashMap<>();
			Iterator<V> it1 = g.vertices().iterator();
			IntIterator it2 = dg.vertices().iterator();
			for (int i = 0; i < n; i++) {
				assert it1.hasNext();
				assert it2.hasNext();
				gToDg.put(it1.next(), it2.nextInt());
			}

			IWeightsObj<E> edgeRef = dg.addEdgesWeights("edgeRef", Object.class);
			for (V u : g.vertices()) {
				for (EdgeIter<V, E> eit = g.outEdges(u).iterator(); eit.hasNext();) {
					E e = eit.next();
					V v = eit.target();
					edgeRef.set(dg.addEdge(gToDg.getInt(u), gToDg.getInt(v)), e);
					edgeRef.set(dg.addEdge(gToDg.getInt(v), gToDg.getInt(u)), e);
				}
			}
			IWeightFunction w0 = e -> w.weight(edgeRef.get(e));

			/*
			 * MDST algorithm compute the directed spanning tree of the vertices reachable from the root, not the full
			 * forest of the graph, so we compute MDST for each weak connected component independently.
			 */
			WeaklyConnectedComponentsAlgo wccAlgo = WeaklyConnectedComponentsAlgo.newInstance();
			VertexPartition<V, E> wccs = wccAlgo.findWeaklyConnectedComponents(g);
			IntCollection mst = new IntArrayList();
			for (int wcc = 0; wcc < wccs.numberOfBlocks(); wcc++) {
				int root = gToDg.getInt(wccs.blockVertices(wcc).iterator().next());
				MinimumSpanningTree.IResult wccMst = (MinimumSpanningTree.IResult) algo
						.computeMinimumDirectedSpanningTree(dg, w0, Integer.valueOf(root));
				mst.addAll(wccMst.edges());
			}

			int[] mstIndex = mst.intStream().map(e -> g.indexGraphEdgesMap().idToIndex(edgeRef.get(e))).toArray();
			MinimumSpanningTree.IResult indexRes = new MinimumSpanningTreeUtils.ResultImpl(mstIndex);

			if (g instanceof IntGraph) {
				return (MinimumSpanningTree.Result<V, E>) new MinimumSpanningTreeUtils.IntResultFromIndexResult(
						(IntGraph) g, indexRes);
			} else {
				return new MinimumSpanningTreeUtils.ObjResultFromIndexResult<>(g, indexRes);
			}
		}
	}

	@Test
	public void testRandGraphUndirected() {
		final long seed = 0x9234356819f0ea1dL;
		MinimumSpanningTreeTestUtils.testRandGraph(new MdstUndirectedWrapper(new MinimumDirectedSpanningTreeTarjan()),
				seed);
	}

	@Test
	public void testRandGraphDirected() {
		final long seed = 0xdb81d5dd5fe0d5b3L;
		testRandGraph(new MinimumDirectedSpanningTreeTarjan(), GraphsTestUtils.defaultGraphImpl(seed), seed);
	}

	public static void testRandGraph(MinimumDirectedSpanningTree algo,
			Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl, long seed) {
		boolean selfEdges = graphImpl.get(true).isAllowSelfEdges();
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 5).repeat(256);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(128, 256).repeat(32);
		tester.addPhase().withArgs(1024, 4096).repeat(8);
		tester.addPhase().withArgs(4096, 16384).repeat(2);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true)
					.parallelEdges(false).selfEdges(selfEdges).cycles(true).connected(false).graphImpl(graphImpl).build();
			g = maybeIndexGraph(g, rand);
			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

			testRandGraph(algo, g, w);
		});
	}

	private static <V, E> void testRandGraph(MinimumDirectedSpanningTree algo, Graph<V, E> g, WeightFunction<E> w) {
		V root = g.vertices().iterator().next();
		MinimumSpanningTree.Result<V, E> mstRes = algo.computeMinimumDirectedSpanningTree(g, w, root);
		Graph<V, E> mst = g.subGraphCopy(g.vertices(), mstRes.edges());

		assertEquals(Path.reachableVertices(g, root), Path.reachableVertices(mst, root));
	}

	@Test
	public void testDefaultImpl() {
		MinimumDirectedSpanningTree algo = MinimumDirectedSpanningTree.newInstance();
		assertEquals(MinimumDirectedSpanningTreeTarjan.class, algo.getClass());
	}

}
