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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

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
			Graph<Integer, Integer> g = randUGraph(n, m, allEvenVertices, seedGen.nextSeed());
			Path<Integer, Integer> tour = EulerianTourAlgo.newInstance().computeEulerianTour(g);
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
			Graph<Integer, Integer> g = randDiGraph(n, m, allEqualInOutDegree, seedGen.nextSeed());
			Path<Integer, Integer> tour = EulerianTourAlgo.newInstance().computeEulerianTour(g);
			validateEulerianTour(g, tour);
		});
	}

	private static <V, E> void validateEulerianTour(Graph<V, E> g, Path<V, E> tour) {
		Set<E> usedEdges = new ObjectOpenHashSet<>(g.edges().size());
		for (EdgeIter<V, E> it = tour.edgeIter(); it.hasNext();) {
			E e = it.next();
			boolean alreadyUsed = !usedEdges.add(e);
			assertFalse(alreadyUsed, "edge appear twice in tour: " + e);
		}

		for (E e : g.edges())
			assertTrue(usedEdges.contains(e), "edge was not used: " + e);
	}

	private static Graph<Integer, Integer> randUGraph(int n, int m, boolean allEvenVertices, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false)
				.parallelEdges(true).selfEdges(true).cycles(true).connected(true).build();
		Supplier<Integer> edgeSupplier = () -> {
			for (;;) {
				Integer e = Integer.valueOf(rand.nextInt());
				if (e.intValue() >= 1 && !g.edges().contains(e))
					return e;
			}
		};
		addEdgesUntilEulerianUndirected(g, edgeSupplier, allEvenVertices, seedGen.nextSeed());
		return g;
	}

	private static <V, E> void addEdgesUntilEulerianUndirected(Graph<V, E> g, Supplier<E> edgeSupplier,
			boolean allEvenVertices, long seed) {
		Random rand = new Random(seed);
		List<V> oddVertices = new ArrayList<>();
		for (V u : g.vertices())
			if (degreeWithoutSelfLoops(g, u) % 2 != 0)
				oddVertices.add(u);
		assert oddVertices.size() % 2 == 0;

		while (!oddVertices.isEmpty()) {
			int uIdx = rand.nextInt(oddVertices.size());
			int vIdx = rand.nextInt(oddVertices.size());
			V u = oddVertices.get(uIdx);
			V v = oddVertices.get(vIdx);
			if (u.equals(v))
				continue;
			g.addEdge(u, v, edgeSupplier.get());
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

		for (V u : g.vertices())
			assert degreeWithoutSelfLoops(g, u) % 2 == 0;
		if (!allEvenVertices) {
			/* Add another edge resulting in two vertices with odd degree */
			if (g.vertices().size() <= 1)
				throw new IllegalArgumentException();
			List<V> vs = new ArrayList<>(g.vertices());
			for (;;) {
				V u = vs.get(rand.nextInt(vs.size()));
				V v = vs.get(rand.nextInt(vs.size()));
				if (u.equals(v))
					continue;
				g.addEdge(u, v, edgeSupplier.get());
				break;
			}
		}
	}

	private static Graph<Integer, Integer> randDiGraph(int n, int m, boolean allEqualInOutDegree, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true)
				.parallelEdges(true).selfEdges(true).cycles(true).connected(true).build();
		Supplier<Integer> edgeSupplier = () -> {
			for (;;) {
				Integer e = Integer.valueOf(rand.nextInt());
				if (e.intValue() >= 1 && !g.edges().contains(e))
					return e;
			}
		};
		addEdgesUntilStronglyConnected(g, edgeSupplier);
		addEdgesUntilEulerianDirected(g, edgeSupplier, allEqualInOutDegree, seedGen.nextSeed());
		return g;
	}

	private static <V, E> void addEdgesUntilEulerianDirected(Graph<V, E> g, Supplier<E> edgeSupplier,
			boolean allEqualInOutDegree, long seed) {
		Random rand = new Random(seed);

		List<V> lackingOutEdgesVertices = new ArrayList<>();
		List<V> lackingInEdgesVertices = new ArrayList<>();
		IntList lackingOutEdgesNum = new IntArrayList();
		IntList lackingInEdgesNum = new IntArrayList();
		for (V u : g.vertices()) {
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
			V u = lackingOutEdgesVertices.get(uIdx);
			V v = lackingInEdgesVertices.get(vIdx);
			if (u.equals(v))
				continue;
			g.addEdge(u, v, edgeSupplier.get());

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

		for (V u : g.vertices())
			assert g.outEdges(u).size() == g.inEdges(u).size();
		if (!allEqualInOutDegree) {
			/*
			 * Add another edge resulting in one vertex with extra out degree, and one vertex with extra in degree
			 */
			if (g.vertices().size() <= 1)
				throw new IllegalArgumentException();
			List<V> vs = new ArrayList<>(g.vertices());
			for (;;) {
				V u = vs.get(rand.nextInt(vs.size()));
				V v = vs.get(rand.nextInt(vs.size()));
				if (u.equals(v))
					continue;
				g.addEdge(u, v, edgeSupplier.get());
				break;
			}
		}
		assert StronglyConnectedComponentsAlgo.newInstance().isStronglyConnected(g);
	}

	private static <V, E> int degreeWithoutSelfLoops(Graph<V, E> g, V u) {
		int d = 0;
		for (EdgeIter<V, E> eit = g.outEdges(u).iterator(); eit.hasNext();) {
			eit.next();
			if (!eit.target().equals(u))
				d++;
		}
		return d;
	}

	private static <K> void swapAndRemove(List<K> list, int idx) {
		list.set(idx, list.get(list.size() - 1));
		list.remove(list.size() - 1);
	}

	private static <V, E> void addEdgesUntilStronglyConnected(Graph<V, E> g, Supplier<E> edgSupplier) {
		VertexPartition<V, E> connectivityRes =
				StronglyConnectedComponentsAlgo.newInstance().findStronglyConnectedComponents(g);
		int N = connectivityRes.numberOfBlocks();
		if (N <= 1)
			return;

		List<V> V2v = new ArrayList<>(N);
		for (int i = 0; i < N; i++)
			V2v.add(null);
		for (V v : g.vertices()) {
			int V = connectivityRes.vertexBlock(v);
			if (V2v.get(V) == null)
				V2v.set(V, v);
		}

		for (int V = 1; V < N; V++) {
			g.addEdge(V2v.get(0), V2v.get(V), edgSupplier.get());
			g.addEdge(V2v.get(V), V2v.get(0), edgSupplier.get());
		}
	}

}
