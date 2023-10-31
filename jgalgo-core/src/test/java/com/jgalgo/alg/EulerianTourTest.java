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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class EulerianTourTest extends TestBase {

	@Test
	public void testRandGraphUndirectedAllEvenDegree() {
		final long seed = 0x92d17aaf1fe75a3fL;
		testRandGraphUndirected(true, seed);
	}

	@Test
	public void testRandGraphUndirectedTwoOddDegree() {
		final long seed = 0xfe1af8c840dbcd5bL;
		testRandGraphUndirected(false, seed);
	}

	@Test
	public void testRandGraphDirectedAllEqualInOutDegree() {
		final long seed = 0xd6802511f6b16d27L;
		testRandGraphDirected(true, seed);
	}

	@Test
	public void testRandGraphDirectedOneExtraInDegreeOneExtraOutDegree() {
		final long seed = 0x59a5e2af6122a61fL;
		testRandGraphDirected(false, seed);
	}

	private static void testRandGraphUndirected(boolean allEvenVertices, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			IntGraph g = randUGraph(n, m, allEvenVertices, seedGen.nextSeed());
			IPath tour = (IPath) EulerianTourAlgo.newInstance().computeEulerianTour(g);
			validateEulerianTour(g, tour);
		});
	}

	private static void testRandGraphDirected(boolean allEqualInOutDegree, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			IntGraph g = randDiGraph(n, m, allEqualInOutDegree, seedGen.nextSeed());
			IPath tour = (IPath) (IPath) EulerianTourAlgo.newInstance().computeEulerianTour(g);
			validateEulerianTour(g, tour);
		});
	}

	private static void validateEulerianTour(IntGraph g, IPath tour) {
		IntSet usedEdges = new IntOpenHashSet(g.edges().size());
		for (IEdgeIter it = tour.edgeIter(); it.hasNext();) {
			int e = it.nextInt();
			boolean alreadyUsed = !usedEdges.add(e);
			assertFalse(alreadyUsed, "edge appear twice in tour: " + e);
		}

		for (int e : g.edges())
			assertTrue(usedEdges.contains(e), "edge was not used: " + e);
	}

	private static IntGraph randUGraph(int n, int m, boolean allEvenVertices, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());

		IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
				.selfEdges(true).cycles(true).connected(true).build();

		IntList oddVertices = new IntArrayList();
		for (int u : g.vertices())
			if (degreeWithoutSelfLoops(g, u) % 2 != 0)
				oddVertices.add(u);
		assert oddVertices.size() % 2 == 0;

		while (!oddVertices.isEmpty()) {
			int uIdx = rand.nextInt(oddVertices.size());
			int vIdx = rand.nextInt(oddVertices.size());
			int u = oddVertices.getInt(uIdx);
			int v = oddVertices.getInt(vIdx);
			if (u == v)
				continue;
			g.addEdge(u, v);
			assert degreeWithoutSelfLoops(g, u) % 2 == 0;
			assert degreeWithoutSelfLoops(g, v) % 2 == 0;

			if (uIdx < vIdx) {
				int temp = uIdx;
				uIdx = vIdx;
				vIdx = temp;
			}

			/* remove u and v from oddVertices */
			/* assume uIdx > vIdx */
			swapAndRemove(oddVertices, uIdx);
			swapAndRemove(oddVertices, vIdx);
		}

		for (int u : g.vertices())
			assert degreeWithoutSelfLoops(g, u) % 2 == 0;
		if (!allEvenVertices) {
			/* Add another edge resulting in two vertices with odd degree */
			if (n <= 1)
				throw new IllegalArgumentException();
			int[] vs = g.vertices().toIntArray();
			for (;;) {
				int u = vs[rand.nextInt(vs.length)];
				int v = vs[rand.nextInt(vs.length)];
				if (u == v)
					continue;
				g.addEdge(u, v);
				break;
			}
		}

		return g;
	}

	private static IntGraph randDiGraph(int n, int m, boolean allEqualInOutDegree, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());

		IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
				.selfEdges(true).cycles(true).connected(true).build();
		addEdgesUntilStronglyConnected(g);

		IntList lackingOutEdgesVertices = new IntArrayList();
		IntList lackingInEdgesVertices = new IntArrayList();
		IntList lackingOutEdgesNum = new IntArrayList();
		IntList lackingInEdgesNum = new IntArrayList();
		for (int u : g.vertices()) {
			int outD = g.outEdges(u).size();
			int inD = g.inEdges(u).size();
			if (outD == inD)
				continue;
			if (outD > inD) {
				lackingInEdgesVertices.add(u);
				lackingInEdgesNum.add(outD - inD);
			} else {
				lackingOutEdgesVertices.add(u);
				lackingOutEdgesNum.add(inD - outD);
			}
		}

		for (;;) {
			if (lackingOutEdgesVertices.isEmpty()) {
				assert lackingInEdgesVertices.isEmpty();
				break;
			}
			int uIdx = rand.nextInt(lackingOutEdgesVertices.size());
			int vIdx = rand.nextInt(lackingInEdgesVertices.size());
			int u = lackingOutEdgesVertices.getInt(uIdx);
			int v = lackingInEdgesVertices.getInt(vIdx);
			if (u == v)
				continue;
			g.addEdge(u, v);

			/* remove u and v if they have enough out/in edges */
			/* assume uIdx > vIdx */
			int uLackingOutNum = lackingOutEdgesNum.getInt(uIdx);
			if (--uLackingOutNum > 0) {
				assert g.inEdges(u).size() - g.outEdges(u).size() == uLackingOutNum;
				lackingOutEdgesNum.set(uIdx, uLackingOutNum);
			} else {
				assert g.inEdges(u).size() - g.outEdges(u).size() == uLackingOutNum;
				swapAndRemove(lackingOutEdgesNum, uIdx);
				swapAndRemove(lackingOutEdgesVertices, uIdx);
			}
			int vLackingInNum = lackingInEdgesNum.getInt(vIdx);
			if (--vLackingInNum > 0) {
				assert g.outEdges(v).size() - g.inEdges(v).size() == vLackingInNum;
				lackingInEdgesNum.set(vIdx, vLackingInNum);
			} else {
				assert g.outEdges(v).size() - g.inEdges(v).size() == vLackingInNum;
				swapAndRemove(lackingInEdgesNum, vIdx);
				swapAndRemove(lackingInEdgesVertices, vIdx);
			}
		}

		for (int u : g.vertices())
			assert g.outEdges(u).size() == g.inEdges(u).size();
		if (!allEqualInOutDegree) {
			/*
			 * Add another edge resulting in one vertex with extra out degree, and one vertex with extra in degree
			 */
			if (n <= 1)
				throw new IllegalArgumentException();
			int[] vs = g.vertices().toIntArray();
			for (;;) {
				int u = vs[rand.nextInt(vs.length)];
				int v = vs[rand.nextInt(vs.length)];
				if (u == v)
					continue;
				g.addEdge(u, v);
				break;
			}
		}
		assert StronglyConnectedComponentsAlgo.newInstance().isStronglyConnected(g);
		return g;
	}

	private static int degreeWithoutSelfLoops(IntGraph g, int u) {
		int d = 0;
		for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
			eit.nextInt();
			if (eit.targetInt() != u)
				d++;
		}
		return d;
	}

	private static void swapAndRemove(IntList list, int idx) {
		list.set(idx, list.getInt(list.size() - 1));
		list.removeInt(list.size() - 1);
	}

	private static void addEdgesUntilStronglyConnected(IntGraph g) {
		IVertexPartition connectivityRes =
				(IVertexPartition) StronglyConnectedComponentsAlgo.newInstance().findStronglyConnectedComponents(g);
		int N = connectivityRes.numberOfBlocks();
		if (N <= 1)
			return;

		int[] V2v = new int[N];
		Arrays.fill(V2v, -1);
		for (int v : g.vertices()) {
			int V = connectivityRes.vertexBlock(v);
			if (V2v[V] == -1)
				V2v[V] = v;
		}

		for (int V = 1; V < N; V++) {
			g.addEdge(V2v[0], V2v[V]);
			g.addEdge(V2v[V], V2v[0]);
		}
	}

}
