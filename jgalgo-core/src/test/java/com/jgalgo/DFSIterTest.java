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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class DFSIterTest extends TestBase {

	@Test
	public void testDfsConnected() {
		final long seed = 0x77678e2ce068199cL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 8).repeat(256);
		tester.addPhase().withArgs(32, 64).repeat(128);
		tester.addPhase().withArgs(2048, 8192).repeat(4);
		tester.run((n, m) -> {
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(true).build();
			int[] vs = g.vertices().toIntArray();
			int source = vs[rand.nextInt(vs.length)];

			IntSet visited = new IntOpenHashSet();
			for (DFSIter it = DFSIter.newInstance(g, source); it.hasNext();) {
				int v = it.nextInt();
				IntList pathFromSource = it.edgePath();
				int e = v == source ? -1 : pathFromSource.getInt(pathFromSource.size() - 1);
				assertFalse(visited.contains(v), "already visited vertex " + v);
				if (v != source)
					assertTrue(g.edgeEndpoint(e, g.edgeEndpoint(e, v)) == v, "v is not an endpoint of inEdge");
				visited.add(v);
			}

			for (int v : g.vertices())
				assertTrue(visited.contains(v));
		});
	}

}
