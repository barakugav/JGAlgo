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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsObj;
import com.jgalgo.internal.util.JGAlgoUtils;
import com.jgalgo.internal.util.JGAlgoUtils.BiInt2LongFunc;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.Range;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

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
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			testUGraph(BiConnectedComponentsAlgo.newInstance(), g);
		});
	}

	@SuppressWarnings("boxing")
	private static void testUGraph(BiConnectedComponentsAlgo algo, IntGraph g) {
		BiConnectedComponentsAlgo.IResult res = (BiConnectedComponentsAlgo.IResult) algo.findBiConnectedComponents(g);

		/* Check that each vertex is contained in some BiCc */
		for (int v : g.vertices()) {
			IntCollection vBiccs = res.getVertexBiCcs(v);
			assertFalse(vBiccs.isEmpty(), "a vertex is not contained in any BiConnected component: " + v);
			assertEquals(new IntOpenHashSet(vBiccs).size(), vBiccs.size(),
					"vertex BiCcs list contains duplications" + vBiccs);
		}

		/* Check that each edge is contained in exactly one BiCc (unless its a self loop) */
		IWeightsObj<IntSet> edgeToBiccs = IWeights.createExternalEdgesWeights(g, IntSet.class, null);
		for (int bccIdx = 0; bccIdx < res.getNumberOfBiCcs(); bccIdx++) {
			IntCollection biccEdges = res.getBiCcEdges(bccIdx);
			for (int e : biccEdges) {
				IntSet eBiccs = edgeToBiccs.get(e);
				if (eBiccs == null)
					edgeToBiccs.set(e, eBiccs = new IntOpenHashSet());
				eBiccs.add(bccIdx);
			}
			assertEquals(new IntOpenHashSet(biccEdges).size(), biccEdges.size(),
					"BiCc edges list contains duplications" + biccEdges);
		}
		for (int e : g.edges()) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
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
		final IVertexPartition gCcs = ccAlgo.findWeaklyConnectedComponents(g);

		/* Check that each bicc is actually a BiConnected component */
		for (int bccIdx = 0; bccIdx < res.getNumberOfBiCcs(); bccIdx++) {
			IntCollection vertices = res.getBiCcVertices(bccIdx);
			assertFalse(vertices.isEmpty(), "BiConnected component can't be empty");
			assertEquals(new IntOpenHashSet(vertices).size(), vertices.size(),
					"BiCc vertices list contains duplications" + vertices);
			if (vertices.size() == 1)
				continue;

			IntSet ccIdxs = new IntOpenHashSet();
			for (int v : vertices)
				ccIdxs.add(gCcs.vertexBlock(v));
			assertTrue(ccIdxs.size() == 1, "BiConnected component vertices are not in a the connected component");

			for (final int vToRemove : vertices) {
				IntGraph gWithoutV = g.copy();
				gWithoutV.removeEdgesOf(vToRemove);

				IVertexPartition ccsWithoutV = ccAlgo.findWeaklyConnectedComponents(gWithoutV);
				ccIdxs.clear();
				for (int u : vertices)
					if (u != vToRemove)
						ccIdxs.add(ccsWithoutV.vertexBlock(u));
				assertEquals(1, ccIdxs.size(),
						"BiConnected component vertices are not in a the connected component after remove a single vertex: "
								+ vToRemove);
			}
		}

		/* Check that we couldn't merge two biccs into one */
		for (int i = 0; i < res.getNumberOfBiCcs(); i++) {
			for (int j = i + 1; j < res.getNumberOfBiCcs(); j++) {
				IntCollection vs1 = res.getBiCcVertices(i);
				IntCollection vs2 = res.getBiCcVertices(j);
				if (gCcs.vertexBlock(vs1.iterator().nextInt()) != gCcs.vertexBlock(vs2.iterator().nextInt()))
					continue; /* not connected at all */

				boolean sameCcForAllV = true;
				for (IntIterator vit = IntIterators.concat(vs1.iterator(), vs2.iterator()); vit.hasNext();) {
					final int vToRemove = vit.nextInt();
					IntGraph gWithoutV = g.copy();
					gWithoutV.removeEdgesOf(vToRemove);

					IVertexPartition ccsWithoutV = ccAlgo.findWeaklyConnectedComponents(gWithoutV);
					IntSet ccIdxs = new IntOpenHashSet();
					for (IntIterator uit = IntIterators.concat(vs1.iterator(), vs2.iterator()); uit.hasNext();) {
						int u = uit.nextInt();
						if (u != vToRemove)
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
		IntSet cutVertices = new IntOpenHashSet();
		final int originalNumberOfCcs = ccAlgo.findWeaklyConnectedComponents(g).numberOfBlocks();
		for (int v : g.vertices()) {
			IntGraph gWithoutV = g.copy();
			gWithoutV.removeVertex(v);
			if (ccAlgo.findWeaklyConnectedComponents(gWithoutV).numberOfBlocks() > originalNumberOfCcs)
				cutVertices.add(v);
		}
		for (int v : g.vertices())
			assertEquals(cutVertices.contains(v), res.isCutVertex(v));
		assertEquals(cutVertices, res.getCutVertices());

		/* check block graph */
		IntGraph blockGraph = res.getBlockGraph();
		assertEquals(Range.of(res.getNumberOfBiCcs()), blockGraph.vertices());
		BiFunction<IntSet, IntSet, IntSet> intersect = (s1, s2) -> {
			IntSet inter = new IntOpenHashSet(s1);
			inter.retainAll(s2);
			return inter;
		};
		BiInt2LongFunc key = (b1, b2) -> JGAlgoUtils.longPack(Math.min(b1, b2), Math.max(b1, b2));
		LongSet expectedBlockEdges = new LongOpenHashSet();
		for (int b1 : Range.of(res.getNumberOfBiCcs()))
			for (int b2 : Range.of(b1 + 1, res.getNumberOfBiCcs()))
				if (!intersect.apply(res.getBiCcVertices(b1), res.getBiCcVertices(b2)).isEmpty())
					expectedBlockEdges.add(key.apply(b1, b2));
		LongSet actualBlockEdges = new LongOpenHashSet();
		for (int e : blockGraph.edges())
			actualBlockEdges.add(key.apply(blockGraph.edgeSource(e), blockGraph.edgeTarget(e)));
		assertEquals(expectedBlockEdges, actualBlockEdges);
	}

}
