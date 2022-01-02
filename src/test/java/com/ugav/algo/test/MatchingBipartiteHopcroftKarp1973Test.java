package com.ugav.algo.test;

import java.util.Collection;
import java.util.Iterator;

import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.GraphBipartite;
import com.ugav.algo.GraphBipartiteArray;
import com.ugav.algo.MatchingBipartite;
import com.ugav.algo.MatchingBipartiteHopcroftKarp1973;
import com.ugav.algo.test.GraphsTestUtils.RandomGraphBuilder;

public class MatchingBipartiteHopcroftKarp1973Test {

	private static <E> GraphBipartite<E> randGraphBipartite(int sn, int tn, int m) {
		return (GraphBipartite<E>) new RandomGraphBuilder().sn(sn).tn(tn).m(m).bipartite(true).directed(false)
				.doubleEdges(false).selfEdges(false).cycles(true).connected(false).<E>build();
	}

	@SuppressWarnings("unused")
	private static GraphBipartite<Void> createGraphBipartiteFromAdjacencyMatrix(int sSize, int[][] m) {
		int n = m.length;
		GraphBipartite<Void> g = new GraphBipartiteArray<>(DirectedType.Undirected, sSize, n - sSize);
		for (int u = 0; u < n; u++) {
			for (int v = u + 1; v < n; v++) {
				if (m[u][v] == 0)
					continue;
				g.addEdge(u, v);
			}
		}
		return g;
	}

	private static boolean testAlgo(MatchingBipartite algo) {
		int[][] phases = { { 256, 8, 8, 16 }, { 128, 16, 16, 64 }, { 64, 32, 32, 128 }, { 16, 128, 128, 512 },
				{ 4, 1024, 1024, 8192 } };
		for (int phase = 0; phase < phases.length; phase++) {
			int repeat = phases[phase][0];
			int sn = phases[phase][1];
			int tn = phases[phase][2];
			int m = phases[phase][3];
			for (int i = 0; i < repeat; i++) {
				GraphBipartite<Void> g = randGraphBipartite(sn, tn, m);
				int expeced = calcExpectedMaxMatching(g);
				if (!testAlgo(algo, g, expeced)) {
					System.out.println(i);
					return false;
				}
			}
		}
		return true;
	}

	private static <E> boolean testAlgo(MatchingBipartite algo, GraphBipartite<E> g, int expectedMatchSize) {
		Collection<Edge<E>> match = algo.calcMaxMatching(g);

		int n = g.vertices();
		@SuppressWarnings("unchecked")
		Edge<E>[] matched = new Edge[n];
		for (Edge<E> e : match) {
			for (int v : new int[] { e.u(), e.v() }) {
				if (matched[v] != null) {
					TestUtils.printTestStr("Vertex " + v + " is matched twice: " + matched[v] + ", " + e + "\n");
					return false;
				}
			}
		}

		if (match.size() != expectedMatchSize) {
			TestUtils.printTestStr("unexpected match size: " + match.size() + " != " + expectedMatchSize + "\n");
			return false;
		}
		return true;
	}

	@Test
	public static boolean test() {
		TestUtils.initTestRand(TestUtils.getTestFullname(), -4289612609209232546L);
		return testAlgo(MatchingBipartiteHopcroftKarp1973.getInstance());
	}

	private static <E> int calcExpectedMaxMatching(GraphBipartite<E> g) {
		int sn = g.svertices(), tn = g.tvertices();
		boolean m[][] = new boolean[sn][tn];
		for (int u = 0; u < sn; u++)
			for (Iterator<Edge<E>> it = g.edges(u); it.hasNext();)
				m[u][it.next().v() - sn] = true;

		return maxBPM(m);
	}

	/*
	 * Maximum Bipartite Matching implementation of Ford-Fulkerson algorithm from
	 * the Internet
	 */
	private static int maxBPM(boolean g[][]) {
		int sn = g.length, tn = g[0].length;
		int matchR[] = new int[tn];

		for (int i = 0; i < tn; ++i)
			matchR[i] = -1;

		int result = 0;
		for (int u = 0; u < sn; u++) {
			boolean visited[] = new boolean[tn];
			for (int i = 0; i < tn; ++i)
				visited[i] = false;

			if (bpm(g, u, visited, matchR))
				result++;
		}
		return result;
	}

	private static boolean bpm(boolean g[][], int u, boolean visited[], int matchR[]) {
		int tn = g[0].length;
		for (int v = 0; v < tn; v++) {
			if (g[u][v] && !visited[v]) {
				visited[v] = true;
				if (matchR[v] < 0 || bpm(g, matchR[v], visited, matchR)) {
					matchR[v] = u;
					return true;
				}
			}
		}
		return false;
	}

}
