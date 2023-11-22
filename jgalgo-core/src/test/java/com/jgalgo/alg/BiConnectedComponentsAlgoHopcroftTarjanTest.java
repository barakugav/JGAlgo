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

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsObj;
import com.jgalgo.internal.util.JGAlgoUtils;
import com.jgalgo.internal.util.JGAlgoUtils.BiInt2LongFunc;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class BiConnectedComponentsAlgoHopcroftTarjanTest extends TestBase {

	@Test
	public void randGraphUndirected() {
		final long seed = 0xda9272921794ecfaL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(5, 6).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(64);
		tester.addPhase().withArgs(64, 256).repeat(32);
		tester.addPhase().withArgs(165, 666).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false)
					.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();
			testUGraph(BiConnectedComponentsAlgo.newInstance(), g);
		});
	}

	@SuppressWarnings("boxing")
	private static <V, E> void testUGraph(BiConnectedComponentsAlgo algo, Graph<V, E> g) {
		BiConnectedComponentsAlgo.Result<V, E> res = algo.findBiConnectedComponents(g);

		/* Check that each vertex is contained in some BiCc */
		for (V v : g.vertices()) {
			IntCollection vBiccs = res.getVertexBiCcs(v);
			assertFalse(vBiccs.isEmpty(), "a vertex is not contained in any BiConnected component: " + v);
			assertEquals(new IntOpenHashSet(vBiccs).size(), vBiccs.size(),
					"vertex BiCcs list contains duplications" + vBiccs);
		}

		/* Check that each edge is contained in exactly one BiCc (unless its a self loop) */
		WeightsObj<E, IntSet> edgeToBiccs = Weights.createExternalEdgesWeights(g, IntSet.class, null);
		for (int bccIdx = 0; bccIdx < res.getNumberOfBiCcs(); bccIdx++) {
			Collection<E> biccEdges = res.getBiCcEdges(bccIdx);
			for (E e : biccEdges) {
				IntSet eBiccs = edgeToBiccs.get(e);
				if (eBiccs == null)
					edgeToBiccs.set(e, eBiccs = new IntOpenHashSet());
				eBiccs.add(bccIdx);
			}
			assertEquals(new ObjectOpenHashSet<>(biccEdges).size(), biccEdges.size(),
					"BiCc edges list contains duplications" + biccEdges);
		}
		for (E e : g.edges()) {
			V u = g.edgeSource(e), v = g.edgeTarget(e);
			IntSet eBiccs = edgeToBiccs.get(e);
			assertNotNull(eBiccs, "edge doesn't belong to any ci-connected comps");
			if (u != v) {
				assertEquals(1, eBiccs.size(), "we expect each edge to be in exactly one BiCc: " + eBiccs);
			} else {
				assertEquals(res.getVertexBiCcs(u).size(), eBiccs.size(),
						"self edge should be included in any BiCc that contains the vertex" + eBiccs);
			}
		}

		final WeaklyConnectedComponentsAlgo ccAlgo = WeaklyConnectedComponentsAlgo.newInstance();
		final VertexPartition<V, E> gCcs = ccAlgo.findWeaklyConnectedComponents(g);

		/* Check that each bicc is actually a BiConnected component */
		for (int bccIdx = 0; bccIdx < res.getNumberOfBiCcs(); bccIdx++) {
			Collection<V> vertices = res.getBiCcVertices(bccIdx);
			assertFalse(vertices.isEmpty(), "BiConnected component can't be empty");
			assertEquals(new ObjectOpenHashSet<>(vertices).size(), vertices.size(),
					"BiCc vertices list contains duplications" + vertices);
			if (vertices.size() == 1)
				continue;

			IntSet ccIdxs = new IntOpenHashSet();
			for (V v : vertices)
				ccIdxs.add(gCcs.vertexBlock(v));
			assertTrue(ccIdxs.size() == 1, "BiConnected component vertices are not in a the connected component");

			for (final V vToRemove : vertices) {
				Graph<V, E> gWithoutV = g.copy();
				gWithoutV.removeEdgesOf(vToRemove);

				VertexPartition<V, E> ccsWithoutV = ccAlgo.findWeaklyConnectedComponents(gWithoutV);
				ccIdxs.clear();
				for (V u : vertices)
					if (!u.equals(vToRemove))
						ccIdxs.add(ccsWithoutV.vertexBlock(u));
				assertEquals(1, ccIdxs.size(),
						"BiConnected component vertices are not in a the connected component after remove a single vertex: "
								+ vToRemove);
			}
		}

		/* Check that we couldn't merge two biccs into one */
		for (int i = 0; i < res.getNumberOfBiCcs(); i++) {
			for (int j = i + 1; j < res.getNumberOfBiCcs(); j++) {
				ObjectList<V> vs1 = new ObjectArrayList<>(res.getBiCcVertices(i));
				ObjectList<V> vs2 = new ObjectArrayList<>(res.getBiCcVertices(j));
				if (gCcs.vertexBlock(vs1.iterator().next()) != gCcs.vertexBlock(vs2.iterator().next()))
					continue; /* not connected at all */

				boolean sameCcForAllV = true;
				for (Iterator<V> vit = ObjectIterators.concat(vs1.iterator(), vs2.iterator()); vit.hasNext();) {
					final V vToRemove = vit.next();
					Graph<V, E> gWithoutV = g.copy();
					gWithoutV.removeEdgesOf(vToRemove);

					VertexPartition<V, E> ccsWithoutV = ccAlgo.findWeaklyConnectedComponents(gWithoutV);
					IntSet ccIdxs = new IntOpenHashSet();
					for (Iterator<V> uit = ObjectIterators.concat(vs1.iterator(), vs2.iterator()); uit.hasNext();) {
						V u = uit.next();
						if (!u.equals(vToRemove))
							ccIdxs.add(ccsWithoutV.vertexBlock(u));
					}
					if (ccIdxs.size() > 1)
						sameCcForAllV = false;
				}
				assertFalse(sameCcForAllV,
						"two biccs were connected after removing any vertex. Should be the same bicc");
			}
		}

		/* find all cut vertices manually */
		Set<V> cutVertices = new ObjectOpenHashSet<>();
		final int originalNumberOfCcs = ccAlgo.findWeaklyConnectedComponents(g).numberOfBlocks();
		for (V v : g.vertices()) {
			Graph<V, E> gWithoutV = g.copy();
			gWithoutV.removeVertex(v);
			if (ccAlgo.findWeaklyConnectedComponents(gWithoutV).numberOfBlocks() > originalNumberOfCcs)
				cutVertices.add(v);
		}
		for (V v : g.vertices())
			assertEquals(cutVertices.contains(v), res.isCutVertex(v));
		assertEquals(cutVertices, res.getCutVertices());

		/* check block graph */
		IntGraph blockGraph = res.getBlockGraph();
		assertEquals(range(res.getNumberOfBiCcs()), blockGraph.vertices());
		BiFunction<Set<V>, Set<V>, Set<V>> intersect = (s1, s2) -> {
			Set<V> inter = new ObjectOpenHashSet<>(s1);
			inter.retainAll(s2);
			return inter;
		};
		BiInt2LongFunc key = (b1, b2) -> JGAlgoUtils.longPack(Math.min(b1, b2), Math.max(b1, b2));
		LongSet expectedBlockEdges = new LongOpenHashSet();
		for (int b1 : range(res.getNumberOfBiCcs()))
			for (int b2 : range(b1 + 1, res.getNumberOfBiCcs()))
				if (!intersect.apply(res.getBiCcVertices(b1), res.getBiCcVertices(b2)).isEmpty())
					expectedBlockEdges.add(key.apply(b1, b2));
		LongSet actualBlockEdges = new LongOpenHashSet();
		for (int e : blockGraph.edges())
			actualBlockEdges.add(key.apply(blockGraph.edgeSource(e), blockGraph.edgeTarget(e)));
		assertEquals(expectedBlockEdges, actualBlockEdges);
	}

}
