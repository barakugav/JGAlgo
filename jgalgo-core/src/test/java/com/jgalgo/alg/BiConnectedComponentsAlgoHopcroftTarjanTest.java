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
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

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
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			testUGraph(BiConnectedComponentsAlgo.newBuilder().build(), g);
		});
	}

	private static void testUGraph(BiConnectedComponentsAlgo algo, Graph g) {
		BiConnectedComponentsAlgo.Result res = algo.findBiConnectedComponents(g);

		/* Check that each vertex is contained in some BiCc */
		for (int v : g.vertices()) {
			IntCollection vBiccs = res.getVertexBiCcs(v);
			assertFalse(vBiccs.isEmpty(), "a vertex is not contained in any BiConnected component: " + v);
			assertEquals(new IntOpenHashSet(vBiccs).size(), vBiccs.size(),
					"vertex BiCcs list contains duplications" + vBiccs);
		}

		/* Check that each edge is contained in exactly one BiCc (unless its a self loop) */
		Weights<IntSet> edgeToBiccs = Weights.createExternalEdgesWeights(g, IntSet.class, null);
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
			if (u != v) {
				assertEquals(1, eBiccs.size(), "we expect each edge to be in exactly one BiCc: " + eBiccs);
			} else {
				assertEquals(res.getVertexBiCcs(u).size(), eBiccs.size(),
						"self edge should be included in any BiCc that contains the vertex" + eBiccs);
			}
		}

		final ConnectedComponentsAlgo ccAlgo = ConnectedComponentsAlgo.newBuilder().build();
		final VertexPartition gCcs = ccAlgo.findConnectedComponents(g);

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
				Graph gWithoutV = g.copy();
				gWithoutV.removeEdgesOf(vToRemove);

				VertexPartition ccsWithoutV = ccAlgo.findConnectedComponents(gWithoutV);
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
					Graph gWithoutV = g.copy();
					gWithoutV.removeEdgesOf(vToRemove);

					VertexPartition ccsWithoutV = ccAlgo.findConnectedComponents(gWithoutV);
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
	}

}
