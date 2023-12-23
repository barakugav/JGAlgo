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
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class CoresAlgoTest extends TestBase {

	@Test
	public void testRandDiGraphOutDegree() {
		final long seed = 0x9580a32b3a337964L;
		testCoresAlgo(EdgeDirection.Out, true, seed);
	}

	@Test
	public void testRandUGraphOutDegree() {
		final long seed = 0xbbc94ada47517633L;
		testCoresAlgo(EdgeDirection.Out, false, seed);
	}

	@Test
	public void testRandDiGraphInDegree() {
		final long seed = 0xfc360f8910a1daf5L;
		testCoresAlgo(EdgeDirection.In, true, seed);
	}

	@Test
	public void testRandUGraphInDegree() {
		final long seed = 0x45fdea8a6c65ad42L;
		testCoresAlgo(EdgeDirection.In, false, seed);
	}

	@Test
	public void testRandDiGraphOutAndInDegree() {
		final long seed = 0x934896c8f5134dceL;
		testCoresAlgo(EdgeDirection.All, true, seed);
	}

	@Test
	public void testRandUGraphOutAndInDegree() {
		final long seed = 0xc17b43624b506d6bL;
		testCoresAlgo(EdgeDirection.All, false, seed);
	}

	private static void testCoresAlgo(EdgeDirection degreeType, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(128, 256).repeat(32);
		tester.addPhase().withArgs(1024, 4096).repeat(8);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);

			CoresAlgo algo = new CoresAlgoImpl();
			testCoresAlgo(g, algo, degreeType);
		});
	}

	private static <V, E> void testCoresAlgo(Graph<V, E> g, CoresAlgo algo, EdgeDirection degreeType) {
		CoresAlgo.Result<V, E> res;
		if (degreeType == EdgeDirection.All) {
			res = algo.computeCores(g);
		} else {
			res = algo.computeCores(g, degreeType);
		}

		final boolean directed = g.isDirected();
		Object2IntMap<V> vertex2core = new Object2IntOpenHashMap<>();
		int maxCoreExpected = 0;
		coreComputation: for (int k = 0;; k++) {
			Set<V> vs = new ObjectOpenHashSet<>(g.vertices());
			for (;;) {
				if (vs.isEmpty()) {
					maxCoreExpected = k - 1;
					break coreComputation;
				}
				boolean improve = false;
				for (Iterator<V> vit = vs.iterator(); vit.hasNext();) {
					V u = vit.next();
					int degree = 0;
					if (!directed || degreeType == EdgeDirection.Out) {
						for (EdgeIter<V, E> eit = g.outEdges(u).iterator(); eit.hasNext();) {
							eit.next();
							if (vs.contains(eit.target()))
								degree++;
						}
					} else if (degreeType == EdgeDirection.In) {
						for (EdgeIter<V, E> eit = g.inEdges(u).iterator(); eit.hasNext();) {
							eit.next();
							if (vs.contains(eit.source()))
								degree++;
						}
					} else {
						assert degreeType == EdgeDirection.All;
						for (EdgeIter<V, E> eit = g.outEdges(u).iterator(); eit.hasNext();) {
							eit.next();
							if (vs.contains(eit.target()))
								degree++;
						}
						for (EdgeIter<V, E> eit = g.inEdges(u).iterator(); eit.hasNext();) {
							eit.next();
							if (vs.contains(eit.source()))
								degree++;
						}
					}
					if (degree < k) {
						improve = true;
						vit.remove();
					}
				}
				if (!improve)
					break;
			}
			for (V v : vs)
				vertex2core.put(v, k);
		}

		assertEquals(maxCoreExpected, res.maxCore());
		for (V v : g.vertices())
			assertEquals(vertex2core.getInt(v), res.vertexCoreNum(v));
		for (int k0 = 0; k0 <= maxCoreExpected; k0++) {
			final int k = k0;
			assertEquals(res.coreVertices(k).size(), res.coreVertices(k).stream().distinct().count());
			assertEquals(res.coreShell(k).size(), res.coreShell(k).stream().distinct().count());
			assertEquals(res.coreCrust(k).size(), res.coreCrust(k).stream().distinct().count());
			Set<V> expectedCore = vertex2core
					.object2IntEntrySet()
					.stream()
					.filter(e -> e.getIntValue() >= k)
					.map(e -> e.getKey())
					.collect(Collectors.toSet());
			Set<V> expectedShell = vertex2core
					.object2IntEntrySet()
					.stream()
					.filter(e -> e.getIntValue() == k)
					.map(e -> e.getKey())
					.collect(Collectors.toSet());
			Set<V> expectedCrust = vertex2core
					.object2IntEntrySet()
					.stream()
					.filter(e -> e.getIntValue() < k)
					.map(e -> e.getKey())
					.collect(Collectors.toSet());
			assertEquals(expectedCore, res.coreVertices(k));
			assertEquals(expectedShell, res.coreShell(k));
			assertEquals(expectedCrust, res.coreCrust(k));
			for (V v : g.vertices()) {
				assertEqualsBool(expectedCore.contains(v), res.coreVertices(k).contains(v));
				assertEqualsBool(expectedShell.contains(v), res.coreShell(k).contains(v));
				assertEqualsBool(expectedCrust.contains(v), res.coreCrust(k).contains(v));
			}
		}
	}

}
