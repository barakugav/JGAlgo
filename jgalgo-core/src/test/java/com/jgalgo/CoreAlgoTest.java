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

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class CoreAlgoTest extends TestBase {

	@Test
	public void testRandDiGraphOutDegree() {
		final long seed = 0x9580a32b3a337964L;
		testCoreAlgo(CoreAlgo.DegreeType.OutDegree, true, seed);
	}

	@Test
	public void testRandUGraphOutDegree() {
		final long seed = 0xbbc94ada47517633L;
		testCoreAlgo(CoreAlgo.DegreeType.OutDegree, false, seed);
	}

	@Test
	public void testRandDiGraphInDegree() {
		final long seed = 0xfc360f8910a1daf5L;
		testCoreAlgo(CoreAlgo.DegreeType.InDegree, true, seed);
	}

	@Test
	public void testRandUGraphInDegree() {
		final long seed = 0x45fdea8a6c65ad42L;
		testCoreAlgo(CoreAlgo.DegreeType.InDegree, false, seed);
	}

	@Test
	public void testRandDiGraphOutAndInDegree() {
		final long seed = 0x934896c8f5134dceL;
		testCoreAlgo(CoreAlgo.DegreeType.OutAndInDegree, true, seed);
	}

	@Test
	public void testRandUGraphOutAndInDegree() {
		final long seed = 0xc17b43624b506d6bL;
		testCoreAlgo(CoreAlgo.DegreeType.OutAndInDegree, false, seed);
	}

	private static void testCoreAlgo(CoreAlgo.DegreeType degreeType, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(128, 256).repeat(32);
		tester.addPhase().withArgs(1024, 4096).repeat(8);
		tester.run((n, m) -> {
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();

			CoreAlgo algo = new CoreAlgoImpl();
			testCoreAlgo(g, algo, degreeType);
		});
	}

	private static void testCoreAlgo(Graph g, CoreAlgo algo, CoreAlgo.DegreeType degreeType) {
		CoreAlgo.Result res;
		if (degreeType == CoreAlgo.DegreeType.OutAndInDegree) {
			res = algo.computeCores(g);
		} else {
			res = algo.computeCores(g, degreeType);
		}

		final boolean directed = g.getCapabilities().directed();
		for (int k = 0;; k++) {
			IntSet vs = new IntOpenHashSet(g.vertices());
			for (;;) {
				if (vs.isEmpty()) {
					assertEquals(k - 1, res.maxCore());
					return;
				}
				boolean improve = false;
				for (IntIterator it = vs.iterator(); it.hasNext();) {
					int u = it.nextInt();
					int degree = 0;
					if (!directed || degreeType == CoreAlgo.DegreeType.OutDegree) {
						for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
							eit.nextInt();
							if (vs.contains(eit.target()))
								degree++;
						}
					} else if (degreeType == CoreAlgo.DegreeType.InDegree) {
						for (EdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
							eit.nextInt();
							if (vs.contains(eit.source()))
								degree++;
						}
					} else {
						assert degreeType == CoreAlgo.DegreeType.OutAndInDegree;
						for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
							eit.nextInt();
							if (vs.contains(eit.target()))
								degree++;
						}
						for (EdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
							eit.nextInt();
							if (vs.contains(eit.source()))
								degree++;
						}
					}
					if (degree < k) {
						improve = true;
						it.remove();
					}
				}
				if (!improve)
					break;
			}
			IntSet resSet = new IntOpenHashSet(res.coreVertices(k));
			assertEquals(resSet.size(), res.coreVertices(k).size());
			assertEquals(vs, resSet);
		}
	}

}
