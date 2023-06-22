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
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;
import com.jgalgo.graph.Graph;

public class TreesTest extends TestBase {

	@Test
	public void testIsTreeUnrootedPositive() {
		final long seed = 0xb83f3ebfa35ba7a8L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = n - 1;
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(false)
					.selfEdges(false).cycles(false).connected(true).build();

			assertTrue(Trees.isTree(g));
		});
	}

	@Test
	public void testIsTreeUnrootedNegativeUnconnected() {
		final long seed = 0x77ec2f837d2f095bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = n - 1;
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(false)
					.selfEdges(false).cycles(false).connected(true).build();
			int[] edges = g.edges().toIntArray();
			int e = edges[rand.nextInt(edges.length)];
			g.removeEdge(e);

			assertFalse(Trees.isTree(g));
		});
	}

	@Test
	public void testIsTreeUnrootedNegativeCycle() {
		final long seed = 0x2545a2e6fdbf259cL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = n - 1;
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(false)
					.selfEdges(false).cycles(false).connected(true).build();
			int u, v;
			int[] vs = g.vertices().toIntArray();
			do {
				u = vs[rand.nextInt(n)];
				v = vs[rand.nextInt(n)];
			} while (u == v);
			g.addEdge(u, v);

			assertFalse(Trees.isTree(g));
		});
	}

	@Test
	public void testIsTreeRootedPositive() {
		final long seed = 0x15d7bb062a63d066L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = n - 1;
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(false)
					.selfEdges(false).cycles(false).connected(true).build();
			int[] vs = g.vertices().toIntArray();
			int root = vs[rand.nextInt(n)];

			assertTrue(Trees.isTree(g, root));
		});
	}

	@Test
	public void testIsTreeRootedNegativeUnconnected() {
		final long seed = 0xa06f15857aeff09dL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = n - 1;
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(false)
					.selfEdges(false).cycles(false).connected(true).build();
			int[] vs = g.vertices().toIntArray();
			int root = vs[rand.nextInt(n)];
			int[] edges = g.edges().toIntArray();
			int e = edges[rand.nextInt(edges.length)];
			g.removeEdge(e);

			assertFalse(Trees.isTree(g, root));
		});
	}

	@Test
	public void testIsTreeRootedNegativeCycle() {
		final long seed = 0xad27b6b0cb625eb3L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = n - 1;
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(false)
					.selfEdges(false).cycles(false).connected(true).build();
			int[] vs = g.vertices().toIntArray();
			int root = vs[rand.nextInt(n)];
			int u, v;
			do {
				u = vs[rand.nextInt(n)];
				v = vs[rand.nextInt(n)];
			} while (u == v);
			g.addEdge(u, v);

			assertFalse(Trees.isTree(g, root));
		});
	}

	@Test
	public void testIsForestPositive() {
		final long seed = 0xb63ccfd25f531281L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = n - 1;
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(false)
					.selfEdges(false).cycles(false).connected(true).build();
			// remove a few edges
			for (int i = 0; i < m / 10; i++) {
				int[] edges = g.edges().toIntArray();
				int e = edges[rand.nextInt(edges.length)];
				g.removeEdge(e);
			}
			assertTrue(Trees.isForest(g));
		});
	}

	@Test
	public void testIsForestNegative() {
		final long seed = 0xe1a9a20ecb9e816bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = n - 1;
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(false)
					.selfEdges(false).cycles(false).connected(true).build();
			// remove a few edges
			for (int i = 0; i < m / 10; i++) {
				int[] edges = g.edges().toIntArray();
				int e = edges[rand.nextInt(edges.length)];
				g.removeEdge(e);
			}
			// close a random cycle
			for (int[] vs = g.vertices().toIntArray();;) {
				int u = vs[rand.nextInt(n)];
				int v = vs[rand.nextInt(n)];
				if (u != v && Path.findPath(g, u, v) != null) {
					g.addEdge(u, v);
					break;
				}
			}
			assertFalse(Trees.isForest(g));
		});
	}

}
