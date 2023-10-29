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
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class CoresAlgoTest extends TestBase {

	@Test
	public void testRandDiGraphOutDegree() {
		final long seed = 0x9580a32b3a337964L;
		testCoresAlgo(CoresAlgo.DegreeType.OutDegree, true, seed);
	}

	@Test
	public void testRandUGraphOutDegree() {
		final long seed = 0xbbc94ada47517633L;
		testCoresAlgo(CoresAlgo.DegreeType.OutDegree, false, seed);
	}

	@Test
	public void testRandDiGraphInDegree() {
		final long seed = 0xfc360f8910a1daf5L;
		testCoresAlgo(CoresAlgo.DegreeType.InDegree, true, seed);
	}

	@Test
	public void testRandUGraphInDegree() {
		final long seed = 0x45fdea8a6c65ad42L;
		testCoresAlgo(CoresAlgo.DegreeType.InDegree, false, seed);
	}

	@Test
	public void testRandDiGraphOutAndInDegree() {
		final long seed = 0x934896c8f5134dceL;
		testCoresAlgo(CoresAlgo.DegreeType.OutAndInDegree, true, seed);
	}

	@Test
	public void testRandUGraphOutAndInDegree() {
		final long seed = 0xc17b43624b506d6bL;
		testCoresAlgo(CoresAlgo.DegreeType.OutAndInDegree, false, seed);
	}

	private static void testCoresAlgo(CoresAlgo.DegreeType degreeType, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(128, 256).repeat(32);
		tester.addPhase().withArgs(1024, 4096).repeat(8);
		tester.run((n, m) -> {
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();

			CoresAlgo algo = new CoresAlgoImpl();
			testCoresAlgo(g, algo, degreeType);
		});
	}

	private static void testCoresAlgo(IntGraph g, CoresAlgo algo, CoresAlgo.DegreeType degreeType) {
		CoresAlgo.Result res;
		if (degreeType == CoresAlgo.DegreeType.OutAndInDegree) {
			res = algo.computeCores(g);
		} else {
			res = algo.computeCores(g, degreeType);
		}

		final boolean directed = g.isDirected();
		Int2IntMap vertex2core = new Int2IntOpenHashMap();
		int maxCoreExpected = 0;
		coreComputation: for (int k = 0;; k++) {
			IntSet vs = new IntOpenHashSet(g.vertices());
			for (;;) {
				if (vs.isEmpty()) {
					maxCoreExpected = k - 1;
					break coreComputation;
				}
				boolean improve = false;
				for (IntIterator vit = vs.iterator(); vit.hasNext();) {
					int u = vit.nextInt();
					int degree = 0;
					if (!directed || degreeType == CoresAlgo.DegreeType.OutDegree) {
						for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
							eit.nextInt();
							if (vs.contains(eit.targetInt()))
								degree++;
						}
					} else if (degreeType == CoresAlgo.DegreeType.InDegree) {
						for (IEdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
							eit.nextInt();
							if (vs.contains(eit.sourceInt()))
								degree++;
						}
					} else {
						assert degreeType == CoresAlgo.DegreeType.OutAndInDegree;
						for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
							eit.nextInt();
							if (vs.contains(eit.targetInt()))
								degree++;
						}
						for (IEdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
							eit.nextInt();
							if (vs.contains(eit.sourceInt()))
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
			for (int v : vs)
				vertex2core.put(v, k);
		}

		assertEquals(maxCoreExpected, res.maxCore());
		for (int v : g.vertices())
			assertEquals(vertex2core.get(v), res.vertexCoreNum(v));
		for (int k0 = 0; k0 <= maxCoreExpected; k0++) {
			final int k = k0;
			assertEquals(res.coreVertices(k).size(), res.coreVertices(k).intStream().distinct().count());
			assertEquals(res.coreShell(k).size(), res.coreShell(k).intStream().distinct().count());
			assertEquals(res.coreCrust(k).size(), res.coreCrust(k).intStream().distinct().count());
			IntSet expectedCore = vertex2core.int2IntEntrySet().stream().filter(e -> e.getIntValue() >= k)
					.mapToInt(e -> e.getIntKey()).collect(IntOpenHashSet::new, IntSet::add, IntSet::addAll);
			IntSet expectedShell = vertex2core.int2IntEntrySet().stream().filter(e -> e.getIntValue() == k)
					.mapToInt(e -> e.getIntKey()).collect(IntOpenHashSet::new, IntSet::add, IntSet::addAll);
			IntSet expectedCrust = vertex2core.int2IntEntrySet().stream().filter(e -> e.getIntValue() < k)
					.mapToInt(e -> e.getIntKey()).collect(IntOpenHashSet::new, IntSet::add, IntSet::addAll);
			assertEquals(expectedCore, res.coreVertices(k));
			assertEquals(expectedShell, res.coreShell(k));
			assertEquals(expectedCrust, res.coreCrust(k));
		}
	}

}
