package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.ugav.algo.Graph.EdgeIter;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

@SuppressWarnings("boxing")
class MatchingUnweightedTestUtils extends TestUtils {

	private MatchingUnweightedTestUtils() {
		throw new InternalError();
	}

	static void randGraphs(Supplier<? extends Matching> builder) {
		List<Phase> phases = List.of(phase(256, 16, 8), phase(256, 16, 16), phase(128, 32, 32), phase(128, 32, 64),
				phase(64, 64, 64), phase(64, 64, 128), phase(16, 256, 256), phase(16, 256, 512), phase(1, 2048, 2048),
				phase(1, 2048, 3249));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			Graph g = GraphsTestUtils.randGraph(n, m);
			Matching algo = builder.get();
			int expeced = calcExpectedMaxMatching(g);
			testAlgo(algo, g, expeced);
		});
	}

	private static void testAlgo(Matching algo, Graph g, int expectedMatchSize) {
		IntCollection match = algo.calcMaxMatching(g);
		validateMatching(g, match);
		assertEq(expectedMatchSize, match.size(), "unexpected match size");
	}

	static <E> void validateMatching(Graph g, IntCollection matching) {
		Map<Integer, Integer> matched = new HashMap<>();
		for (IntIterator it = matching.iterator(); it.hasNext();) {
			int e = it.nextInt();
			for (int v : new int[] { g.getEdgeSource(e), g.getEdgeTarget(e) }) {
				Integer dup = matched.get(Integer.valueOf(v));
				assertNull(dup, "Invalid matching, clash: ", dup, " ", e, " \n");
				matched.put(Integer.valueOf(v), e);
			}
		}
	}

	private static <E> int calcExpectedMaxMatching(Graph g) {
		int n = g.vertices();
		@SuppressWarnings("unchecked")
		List<Integer>[] graph = new List[n];
		for (int u = 0; u < n; u++) {
			graph[u] = new ArrayList<>();
			for (EdgeIter eit = g.edges(u); eit.hasNext();) {
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
