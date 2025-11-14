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

package com.jgalgo.alg.tree;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.common.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.internal.util.TestBase;

public class TreesTest extends TestBase {

	@Test
	public void testIsTreeUnrootedPositive() {
		final long seed = 0xb83f3ebfa35ba7a8L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(2048).repeat(4);
		tester.run(n -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randTree(n, seedGen.nextSeed());
			assertTrue(Trees.isTree(g));
		});
	}

	@Test
	public void testIsTreeUnrootedNegativeUnconnected() {
		final long seed = 0x77ec2f837d2f095bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(2048).repeat(4);
		tester.run(n -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randTree(n, seedGen.nextSeed());
			g.removeEdge(Graphs.randEdge(g, rand));
			assertFalse(Trees.isTree(g));
		});
	}

	@Test
	public void testIsTreeUnrootedNegativeCycle() {
		final long seed = 0x2545a2e6fdbf259cL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(2048).repeat(4);
		tester.run(n -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randTree(n, seedGen.nextSeed());

			Integer u, v, e;
			do {
				u = Graphs.randVertex(g, rand);
				v = Graphs.randVertex(g, rand);
				e = Integer.valueOf(rand.nextInt());
			} while (u.equals(v) || e.intValue() <= 0 || g.edges().contains(e));
			g.addEdge(u, v, e);

			assertFalse(Trees.isTree(g));
		});
	}

	@Test
	public void testIsTreeRootedPositive() {
		final long seed = 0x15d7bb062a63d066L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(2048).repeat(4);
		tester.run(n -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randTree(n, seedGen.nextSeed());
			Integer root = Graphs.randVertex(g, rand);
			assertTrue(Trees.isTree(g, root));
		});
	}

	@Test
	public void testIsTreeRootedNegativeUnconnected() {
		final long seed = 0xa06f15857aeff09dL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(2048).repeat(4);
		tester.run(n -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randTree(n, seedGen.nextSeed());
			g.removeEdge(Graphs.randEdge(g, rand));
			Integer root = Graphs.randVertex(g, rand);
			assertFalse(Trees.isTree(g, root));
		});
	}

	@Test
	public void testIsTreeRootedNegativeCycle() {
		final long seed = 0xad27b6b0cb625eb3L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(2048).repeat(4);
		tester.run(n -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randTree(n, seedGen.nextSeed());
			Integer root = Graphs.randVertex(g, rand);

			Integer u, v, e;
			do {
				u = Graphs.randVertex(g, rand);
				v = Graphs.randVertex(g, rand);
				e = Integer.valueOf(rand.nextInt());
			} while (u.equals(v) || e.intValue() <= 0 || g.edges().contains(e));
			g.addEdge(u, v, e);

			assertFalse(Trees.isTree(g, root));
		});
	}

	@Test
	public void testIsForestPositive() {
		final long seed = 0xb63ccfd25f531281L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(2048).repeat(4);
		tester.run(n -> {
			int m = n - 1;

			Graph<Integer, Integer> g = GraphsTestUtils.randTree(n, seedGen.nextSeed());
			// remove a few edges
			for (int i = 0; i < m / 10; i++)
				g.removeEdge(Graphs.randEdge(g, rand));
			assertTrue(Trees.isForest(g));
		});
	}

	@Test
	public void testIsForestNegative() {
		final long seed = 0xe1a9a20ecb9e816bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16).repeat(256);
		tester.addPhase().withArgs(32).repeat(128);
		tester.addPhase().withArgs(2048).repeat(4);
		tester.run(n -> {
			int m = n - 1;

			Graph<Integer, Integer> g = GraphsTestUtils.randTree(n, seedGen.nextSeed());
			// remove a few edges
			for (int i = 0; i < m / 10; i++)
				g.removeEdge(Graphs.randEdge(g, rand));
			// close a random cycle
			for (;;) {
				Integer u = Graphs.randVertex(g, rand);
				Integer v = Graphs.randVertex(g, rand);
				if (!u.equals(v) && Path.findPath(g, u, v) != null) {
					int e;
					do {
						e = rand.nextInt();
					} while (e <= 0 || g.edges().contains(Integer.valueOf(e)));
					g.addEdge(u, v, Integer.valueOf(e));
					break;
				}
			}
			assertFalse(Trees.isForest(g));
		});
	}

}
