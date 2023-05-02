package com.jgalgo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

class MatchingUnweightedTestUtils extends TestUtils {

	private MatchingUnweightedTestUtils() {}

	static void randGraphs(MaximumMatching algo, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 16, 8), phase(128, 16, 16), phase(64, 32, 32), phase(32, 32, 64),
				phase(16, 64, 64), phase(12, 64, 128), phase(4, 256, 256), phase(4, 256, 512), phase(1, 1000, 2500));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			UGraph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			int expeced = calcExpectedMaxMatching(g);
			testAlgo(algo, g, expeced);
		});
	}

	private static void testAlgo(MaximumMatching algo, UGraph g, int expectedMatchSize) {
		IntCollection match = algo.computeMaximumMatching(g);
		validateMatching(g, match);
		assertEquals(expectedMatchSize, match.size(), "unexpected match size");
	}

	static <E> void validateMatching(Graph g, IntCollection matching) {
		Set<Integer> matched = new HashSet<>();
		for (IntIterator it = matching.iterator(); it.hasNext();) {
			int e = it.nextInt();
			for (int v : new int[] { g.edgeSource(e), g.edgeTarget(e) }) {
				boolean dup = matched.contains(Integer.valueOf(v));
				assertFalse(dup, "Invalid matching, clash: " + v + " " + e);
				matched.add(Integer.valueOf(v));
			}
		}
	}

	private static <E> int calcExpectedMaxMatching(Graph g) {
		int n = g.vertices().size();
		@SuppressWarnings("unchecked")
		List<Integer>[] graph = new List[n];
		for (int u = 0; u < n; u++) {
			graph[u] = new ArrayList<>();
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				int v = eit.v();
				graph[u].add(Integer.valueOf(v));
			}
		}
		return EdmondsMaximumCardinalityMatching.maxMatching(graph);
	}

	/* implementation of general graphs maximum matching from the Internet */

	private static class EdmondsMaximumCardinalityMatching {
		private static int lca(int[] match, int[] base, int[] p, int a, int b) {
			boolean[] used = new boolean[match.length];
			while (true) {
				a = base[a];
				used[a] = true;
				if (match[a] == -1)
					break;
				a = p[match[a]];
			}
			while (true) {
				b = base[b];
				if (used[b])
					return b;
				b = p[match[b]];
			}
		}

		private static void markPath(int[] match, int[] base, boolean[] blossom, int[] p, int v, int b, int children) {
			for (; base[v] != b; v = p[match[v]]) {
				blossom[base[v]] = blossom[base[match[v]]] = true;
				p[v] = children;
				children = match[v];
			}
		}

		private static int findPath(List<Integer>[] graph, int[] match, int[] p, int root) {
			int n = graph.length;
			boolean[] used = new boolean[n];
			Arrays.fill(p, -1);
			int[] base = new int[n];
			for (int i = 0; i < n; ++i)
				base[i] = i;
			used[root] = true;
			int qh = 0;
			int qt = 0;
			int[] q = new int[n];
			q[qt++] = root;
			while (qh < qt) {
				int v = q[qh++];
				for (int to : graph[v]) {
					if (base[v] == base[to] || match[v] == to)
						continue;
					if (to == root || match[to] != -1 && p[match[to]] != -1) {
						int curbase = lca(match, base, p, v, to);
						boolean[] blossom = new boolean[n];
						markPath(match, base, blossom, p, v, curbase, to);
						markPath(match, base, blossom, p, to, curbase, v);
						for (int i = 0; i < n; ++i)
							if (blossom[base[i]]) {
								base[i] = curbase;
								if (!used[i]) {
									used[i] = true;
									q[qt++] = i;
								}
							}
					} else if (p[to] == -1) {
						p[to] = v;
						if (match[to] == -1)
							return to;
						to = match[to];
						used[to] = true;
						q[qt++] = to;
					}
				}
			}
			return -1;
		}

		public static int maxMatching(List<Integer>[] graph) {
			int n = graph.length;
			int[] match = new int[n];
			Arrays.fill(match, -1);
			int[] p = new int[n];
			for (int i = 0; i < n; ++i) {
				if (match[i] == -1) {
					int v = findPath(graph, match, p, i);
					while (v != -1) {
						int pv = p[v];
						int ppv = match[pv];
						match[v] = pv;
						match[pv] = v;
						v = ppv;
					}
				}
			}
			int matches = 0;
			for (int i = 0; i < n; ++i)
				if (match[i] != -1)
					++matches;
			return matches / 2;
		}
	}

}
