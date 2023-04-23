package com.jgalgo.test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.jgalgo.Graph;
import com.jgalgo.Path;
import com.jgalgo.Trees;
import com.jgalgo.UGraph;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

public class TreesTest extends TestBase {

	@Test
	public void testIsTreeUnrootedPositive() {
		final long seed = 0xb83f3ebfa35ba7a8L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 16), phase(128, 32), phase(4, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = n - 1;
			UGraph g = (UGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false)
					.parallelEdges(false).selfEdges(false).cycles(false).connected(true).build();

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
			UGraph g = (UGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false)
					.parallelEdges(false).selfEdges(false).cycles(false).connected(true).build();
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
			UGraph g = (UGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false)
					.parallelEdges(false).selfEdges(false).cycles(false).connected(true).build();
			int u, v;
			do {
				u = rand.nextInt(n);
				v = rand.nextInt(n);
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
			int root = rand.nextInt(n);

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
			int root = rand.nextInt(n);
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
			int root = rand.nextInt(n);
			int u, v;
			do {
				u = rand.nextInt(n);
				v = rand.nextInt(n);
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
			for (;;) {
				int u = rand.nextInt(n);
				int v = rand.nextInt(n);
				if (u != v && Path.findPath(g, u, v) != null) {
					g.addEdge(u, v);
					break;
				}
			}
			assertFalse(Trees.isForest(g));
		});
	}

}
