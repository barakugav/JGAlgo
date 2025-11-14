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

package com.jgalgo.alg.euler;

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.common.Path;
import com.jgalgo.alg.common.VertexPartition;
import com.jgalgo.alg.connect.StronglyConnectedComponentsAlgo;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
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
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = randUGraph(n, m, allEvenVertices, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);
			validateEulerianTour(g, EulerianTourAlgo.newInstance());
		});
	}

	private static void testRandGraphDirected(boolean allEqualInOutDegree, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = randDiGraph(n, m, allEqualInOutDegree, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);
			validateEulerianTour(g, EulerianTourAlgo.newInstance());
		});
	}

	private static <V, E> void validateEulerianTour(Graph<V, E> g, EulerianTourAlgo algo) {
		Path<V, E> tour = algo.computeEulerianTour(g);
		assertTrue(EulerianTourAlgo.isEulerianTour(g, tour.edges()));
		Set<E> usedEdges = new ObjectOpenHashSet<>(g.edges().size());
		for (EdgeIter<V, E> it = tour.edgeIter(); it.hasNext();) {
			E e = it.next();
			boolean alreadyUsed = !usedEdges.add(e);
			assertFalse(alreadyUsed, "edge appear twice in tour: " + e);
		}

		for (E e : g.edges())
			assertTrue(usedEdges.contains(e), "edge was not used: " + e);
		assertTrue(algo.isEulerian(g));
	}

	private static Graph<Integer, Integer> randUGraph(int n, int m, boolean allEvenVertices, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		Graph<Integer, Integer> g = GraphsTestUtils.randConnectedGraph(n, m, false, seedGen.nextSeed());
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
			for (;;) {
				V u = Graphs.randVertex(g, rand);
				V v = Graphs.randVertex(g, rand);
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
		Graph<Integer, Integer> g = GraphsTestUtils.randConnectedGraph(n, m, true, seedGen.nextSeed());
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
			for (;;) {
				V u = Graphs.randVertex(g, rand);
				V v = Graphs.randVertex(g, rand);
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

		for (int V : range(1, N)) {
			g.addEdge(V2v.get(0), V2v.get(V), edgSupplier.get());
			g.addEdge(V2v.get(V), V2v.get(0), edgSupplier.get());
		}
	}

	@SuppressWarnings("boxing")
	@Test
	public void noEulerianTourUndirected() {
		foreachBoolConfig(intGraph -> {
			GraphFactory<Integer, Integer> factory =
					intGraph ? IntGraphFactory.undirected() : GraphFactory.undirected();
			Graph<Integer, Integer> g = factory.allowParallelEdges().newGraph();

			/* more than 2 vertices with odd degree */
			g.addVertex(0);
			g.addVertex(1);
			g.addVertex(2);
			g.addVertex(3);
			g.addEdge(0, 1, 0);
			g.addEdge(1, 2, 1);
			g.addEdge(2, 3, 2);
			g.addEdge(3, 0, 3);
			g.addEdge(0, 2, 4);
			g.addEdge(1, 3, 5);
			assertFalse(EulerianTourAlgo.newInstance().isEulerian(g));
			assertThrows(IllegalArgumentException.class, () -> EulerianTourAlgo.newInstance().computeEulerianTour(g));
			g.clear();

			/* unconnected graph */
			g.addVertex(0);
			g.addVertex(1);
			g.addVertex(2);
			g.addVertex(3);
			g.addEdge(0, 1, 0);
			g.addEdge(0, 1, 1);
			g.addEdge(2, 3, 2);
			g.addEdge(2, 3, 3);
			assertFalse(EulerianTourAlgo.newInstance().isEulerian(g));
			assertThrows(IllegalArgumentException.class, () -> EulerianTourAlgo.newInstance().computeEulerianTour(g));
			g.clear();
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void noEulerianTourDirected() {
		foreachBoolConfig(intGraph -> {
			GraphFactory<Integer, Integer> factory = intGraph ? IntGraphFactory.directed() : GraphFactory.directed();
			Graph<Integer, Integer> g = factory.allowParallelEdges().newGraph();

			/* more than one vertices with extra out-edge */
			g.addVertex(0);
			g.addVertex(1);
			g.addVertex(2);
			g.addVertex(3);
			g.addEdge(0, 1, 0);
			g.addEdge(0, 1, 1);
			g.addEdge(1, 2, 2);
			g.addEdge(2, 3, 3);
			g.addEdge(2, 3, 4);
			g.addEdge(3, 0, 5);
			assertFalse(EulerianTourAlgo.newInstance().isEulerian(g));
			assertThrows(IllegalArgumentException.class, () -> EulerianTourAlgo.newInstance().computeEulerianTour(g));
			g.clear();

			/* more than one vertices with missing in-edge */
			g.addVertex(0);
			g.addVertex(1);
			g.addVertex(2);
			g.addVertex(3);
			g.addEdge(0, 1, 0);
			g.addEdge(1, 2, 1);
			g.addEdge(1, 2, 2);
			g.addEdge(2, 3, 3);
			g.addEdge(3, 0, 4);
			g.addEdge(3, 0, 5);
			assertFalse(EulerianTourAlgo.newInstance().isEulerian(g));
			assertThrows(IllegalArgumentException.class, () -> EulerianTourAlgo.newInstance().computeEulerianTour(g));
			g.clear();

			/* difference greater than one between number of out edges to in edges */
			g.addVertex(0);
			g.addVertex(1);
			g.addVertex(2);
			g.addVertex(3);
			g.addEdge(0, 1, 0);
			g.addEdge(0, 1, 1);
			g.addEdge(0, 1, 2);
			g.addEdge(1, 2, 3);
			g.addEdge(2, 3, 4);
			g.addEdge(2, 3, 5);
			g.addEdge(2, 3, 6);
			g.addEdge(3, 0, 7);
			assertFalse(EulerianTourAlgo.newInstance().isEulerian(g));
			assertThrows(IllegalArgumentException.class, () -> EulerianTourAlgo.newInstance().computeEulerianTour(g));
			g.clear();

			/* not unconnected graph */
			g.addVertex(0);
			g.addVertex(1);
			g.addVertex(2);
			g.addVertex(3);
			g.addEdge(0, 1, 0);
			g.addEdge(1, 0, 3);
			g.addEdge(2, 3, 4);
			g.addEdge(3, 2, 7);
			assertFalse(EulerianTourAlgo.newInstance().isEulerian(g));
			assertThrows(IllegalArgumentException.class, () -> EulerianTourAlgo.newInstance().computeEulerianTour(g));
			g.clear();
		});
	}

	@Test
	public void isEulerianTourMissingEdges() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addEdge(0, 1, 0);
		g.addEdge(1, 2, 1);
		g.addEdge(2, 3, 2);
		g.addEdge(3, 0, 3);
		g.addEdge(0, 2, 4);

		assertTrue(EulerianTourAlgo.isEulerianTour(g, IntList.of(0, 1, 2, 3, 4)));
		assertFalse(EulerianTourAlgo.isEulerianTour(g, IntList.of(0, 1, 2, 3)));
		assertFalse(EulerianTourAlgo.isEulerianTour(g, IntList.of(1, 2, 3, 4)));
	}

	@Test
	public void isEulerianTourDuplicateEdges() {
		foreachBoolConfig(directed -> {
			IntGraph g = directed ? IntGraph.newUndirected() : IntGraph.newDirected();
			g.addVertex(0);
			g.addVertex(1);
			g.addVertex(2);
			g.addVertex(3);
			g.addEdge(0, 1, 0);
			g.addEdge(1, 2, 1);
			g.addEdge(2, 3, 2);
			g.addEdge(3, 0, 3);
			g.addEdge(0, 2, 4);

			assertTrue(EulerianTourAlgo.isEulerianTour(g, IntList.of(0, 1, 2, 3, 4)));
			assertFalse(EulerianTourAlgo.isEulerianTour(g, IntList.of(3, 0, 1, 2, 3)));
			assertFalse(EulerianTourAlgo.isEulerianTour(g, IntList.of(3, 4, 2, 3, 0)));
		});
	}

	@Test
	public void isEulerianTourNotAPath() {
		foreachBoolConfig(directed -> {
			IntGraph g = directed ? IntGraph.newUndirected() : IntGraph.newDirected();
			g.addVertex(0);
			g.addVertex(1);
			g.addVertex(2);
			g.addVertex(3);
			g.addEdge(0, 1, 0);
			g.addEdge(1, 2, 1);
			g.addEdge(2, 3, 2);
			g.addEdge(3, 0, 3);
			g.addEdge(0, 2, 4);

			assertTrue(EulerianTourAlgo.isEulerianTour(g, IntList.of(0, 1, 2, 3, 4)));
			assertFalse(EulerianTourAlgo.isEulerianTour(g, IntList.of(3, 0, 1, 2, 4)));
			assertFalse(EulerianTourAlgo.isEulerianTour(g, IntList.of(3, 4, 2, 0, 1)));
		});
	}

	@Test
	public void isEulerianTourEmptyGraph() {
		IntGraph g = IntGraph.newUndirected();
		assertTrue(EulerianTourAlgo.isEulerianTour(g, IntList.of()));
	}

}
