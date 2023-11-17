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
package com.jgalgo.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class GraphsTest extends TestBase {

	@Test
	public void testInducedSubGraph() {
		final long seed = 0x386ceb63106c599fL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(4);
		tester.addPhase().withArgs(64, 256).repeat(2);
		tester.addPhase().withArgs(512, 1024).repeat(1);
		tester.run((n, m) -> {
			for (boolean intGraph : BooleanList.of(false, true)) {
				for (boolean directed : BooleanList.of(false, true)) {
					for (boolean index : BooleanList.of(false, true)) {
						Graph<Integer, Integer> g0 = new RandomGraphBuilder(seedGen.nextSeed()).graphImpl(intGraph).n(n)
								.m(m).directed(directed).parallelEdges(true).selfEdges(true).cycles(true)
								.connected(false).build();
						Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;

						Set<Integer> subVs = new IntOpenHashSet();
						while (subVs.size() < n / 3)
							subVs.add(Graphs.randVertex(g, rand));
						Graph<Integer, Integer> subGraph = Graphs.subGraph(g, subVs);

						assertEquals(subVs, subGraph.vertices());
						assertEquals(g.edges().stream()
								.filter(e -> subVs.contains(g.edgeSource(e)) && subVs.contains(g.edgeTarget(e)))
								.collect(Collectors.toSet()), subGraph.edges());
					}
				}
			}
		});
	}

	@Test
	public void testEdgesSubGraph() {
		final long seed = 0x201602cb9ffc4d81L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(4);
		tester.addPhase().withArgs(64, 256).repeat(2);
		tester.addPhase().withArgs(512, 1024).repeat(1);
		tester.run((n, m) -> {
			for (boolean intGraph : BooleanList.of(false, true)) {
				for (boolean directed : BooleanList.of(false, true)) {
					for (boolean index : BooleanList.of(false, true)) {
						Graph<Integer, Integer> g0 = new RandomGraphBuilder(seedGen.nextSeed()).graphImpl(intGraph).n(n)
								.m(m).directed(directed).parallelEdges(true).selfEdges(true).cycles(true)
								.connected(false).build();
						Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;

						Set<Integer> subEs = new IntOpenHashSet();
						while (subEs.size() < m / 6)
							subEs.add(Graphs.randEdge(g, rand));
						Graph<Integer, Integer> subGraph = Graphs.subGraph(g, null, subEs);

						assertEquals(subEs.stream().flatMap(e -> Stream.of(g.edgeSource(e), g.edgeTarget(e))).distinct()
								.collect(Collectors.toSet()), subGraph.vertices());
						assertEquals(subEs, subGraph.edges());
					}
				}
			}
		});
	}

	@Test
	public void testSubGraphWithoutWeights() {
		final long seed = 0xc22a42ac5bf749a4L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(4);
		tester.addPhase().withArgs(64, 256).repeat(2);
		tester.addPhase().withArgs(512, 1024).repeat(1);
		tester.run((n, m) -> {
			for (boolean intGraph : BooleanList.of(false, true)) {
				for (boolean directed : BooleanList.of(false, true)) {
					for (boolean index : BooleanList.of(false, true)) {
						Graph<Integer, Integer> g0 = new RandomGraphBuilder(seedGen.nextSeed()).graphImpl(intGraph).n(n)
								.m(m).directed(directed).parallelEdges(true).selfEdges(true).cycles(true)
								.connected(false).build();
						Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;

						addWeights(g, rand);

						Set<Integer> subVs = new IntOpenHashSet();
						while (subVs.size() < n / 3)
							subVs.add(Graphs.randVertex(g, rand));
						Graph<Integer, Integer> subGraph = Graphs.subGraph(g, subVs, null, false, false);

						assertNull(subGraph.getVerticesWeights("weights1"));
						assertNull(subGraph.getVerticesWeights("weights2"));
						assertNull(subGraph.getVerticesWeights("weights3"));
						assertNull(subGraph.getVerticesWeights("weights4"));
						assertNull(subGraph.getVerticesWeights("weights5"));
						assertNull(subGraph.getVerticesWeights("weights6"));
						assertNull(subGraph.getVerticesWeights("weights7"));
						assertNull(subGraph.getVerticesWeights("weights8"));
						assertNull(subGraph.getVerticesWeights("weights9"));
						assertNull(subGraph.getEdgesWeights("weights1"));
						assertNull(subGraph.getEdgesWeights("weights2"));
						assertNull(subGraph.getEdgesWeights("weights3"));
						assertNull(subGraph.getEdgesWeights("weights4"));
						assertNull(subGraph.getEdgesWeights("weights5"));
						assertNull(subGraph.getEdgesWeights("weights6"));
						assertNull(subGraph.getEdgesWeights("weights7"));
						assertNull(subGraph.getEdgesWeights("weights8"));
						assertNull(subGraph.getEdgesWeights("weights9"));
					}
				}
			}
		});
	}

	@Test
	public void testSubGraphWithWeights() {
		final long seed = 0xf49ceea07e133a11L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(4);
		tester.addPhase().withArgs(64, 256).repeat(2);
		tester.addPhase().withArgs(512, 1024).repeat(1);
		tester.run((n, m) -> {
			for (boolean intGraph : BooleanList.of(false, true)) {
				for (boolean directed : BooleanList.of(false, true)) {
					for (boolean index : BooleanList.of(false, true)) {
						Graph<Integer, Integer> g0 = new RandomGraphBuilder(seedGen.nextSeed()).graphImpl(intGraph).n(n)
								.m(m).directed(directed).parallelEdges(true).selfEdges(true).cycles(true)
								.connected(false).build();
						Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;

						addWeights(g, rand);
						WeightsByte<Integer> vertexWeights1 = g.getVerticesWeights("weights1");
						WeightsShort<Integer> vertexWeights2 = g.getVerticesWeights("weights2");
						WeightsInt<Integer> vertexWeights3 = g.getVerticesWeights("weights3");
						WeightsLong<Integer> vertexWeights4 = g.getVerticesWeights("weights4");
						WeightsFloat<Integer> vertexWeights5 = g.getVerticesWeights("weights5");
						WeightsDouble<Integer> vertexWeights6 = g.getVerticesWeights("weights6");
						WeightsBool<Integer> vertexWeights7 = g.getVerticesWeights("weights7");
						WeightsChar<Integer> vertexWeights8 = g.getVerticesWeights("weights8");
						WeightsObj<Integer, Object> vertexWeights9 = g.getVerticesWeights("weights9");
						WeightsByte<Integer> edgeWeights1 = g.getEdgesWeights("weights1");
						WeightsShort<Integer> edgeWeights2 = g.getEdgesWeights("weights2");
						WeightsInt<Integer> edgeWeights3 = g.getEdgesWeights("weights3");
						WeightsLong<Integer> edgeWeights4 = g.getEdgesWeights("weights4");
						WeightsFloat<Integer> edgeWeights5 = g.getEdgesWeights("weights5");
						WeightsDouble<Integer> edgeWeights6 = g.getEdgesWeights("weights6");
						WeightsBool<Integer> edgeWeights7 = g.getEdgesWeights("weights7");
						WeightsChar<Integer> edgeWeights8 = g.getEdgesWeights("weights8");
						WeightsObj<Integer, Object> edgeWeights9 = g.getEdgesWeights("weights9");

						Set<Integer> subVs = new IntOpenHashSet();
						while (subVs.size() < n / 3)
							subVs.add(Graphs.randVertex(g, rand));
						Graph<Integer, Integer> subGraph = Graphs.subGraph(g, subVs, null, true, true);

						WeightsByte<Integer> vertexWeightsSub1 = subGraph.getVerticesWeights("weights1");
						WeightsShort<Integer> vertexWeightsSub2 = subGraph.getVerticesWeights("weights2");
						WeightsInt<Integer> vertexWeightsSub3 = subGraph.getVerticesWeights("weights3");
						WeightsLong<Integer> vertexWeightsSub4 = subGraph.getVerticesWeights("weights4");
						WeightsFloat<Integer> vertexWeightsSub5 = subGraph.getVerticesWeights("weights5");
						WeightsDouble<Integer> vertexWeightsSub6 = subGraph.getVerticesWeights("weights6");
						WeightsBool<Integer> vertexWeightsSub7 = subGraph.getVerticesWeights("weights7");
						WeightsChar<Integer> vertexWeightsSub8 = subGraph.getVerticesWeights("weights8");
						WeightsObj<Integer, Object> vertexWeightsSub9 = subGraph.getVerticesWeights("weights9");
						WeightsByte<Integer> edgeWeightsSub1 = subGraph.getEdgesWeights("weights1");
						WeightsShort<Integer> edgeWeightsSub2 = subGraph.getEdgesWeights("weights2");
						WeightsInt<Integer> edgeWeightsSub3 = subGraph.getEdgesWeights("weights3");
						WeightsLong<Integer> edgeWeightsSub4 = subGraph.getEdgesWeights("weights4");
						WeightsFloat<Integer> edgeWeightsSub5 = subGraph.getEdgesWeights("weights5");
						WeightsDouble<Integer> edgeWeightsSub6 = subGraph.getEdgesWeights("weights6");
						WeightsBool<Integer> edgeWeightsSub7 = subGraph.getEdgesWeights("weights7");
						WeightsChar<Integer> edgeWeightsSub8 = subGraph.getEdgesWeights("weights8");
						WeightsObj<Integer, Object> edgeWeightsSub9 = subGraph.getEdgesWeights("weights9");
						assertNotNull(vertexWeightsSub1);
						assertNotNull(vertexWeightsSub2);
						assertNotNull(vertexWeightsSub3);
						assertNotNull(vertexWeightsSub4);
						assertNotNull(vertexWeightsSub5);
						assertNotNull(vertexWeightsSub6);
						assertNotNull(vertexWeightsSub7);
						assertNotNull(vertexWeightsSub8);
						assertNotNull(vertexWeightsSub9);
						assertNotNull(edgeWeightsSub1);
						assertNotNull(edgeWeightsSub2);
						assertNotNull(edgeWeightsSub3);
						assertNotNull(edgeWeightsSub4);
						assertNotNull(edgeWeightsSub5);
						assertNotNull(edgeWeightsSub6);
						assertNotNull(edgeWeightsSub7);
						assertNotNull(edgeWeightsSub8);
						assertNotNull(edgeWeightsSub9);

						for (Integer v : subGraph.vertices()) {
							assertEquals(vertexWeights1.get(v), vertexWeightsSub1.get(v));
							assertEquals(vertexWeights2.get(v), vertexWeightsSub2.get(v));
							assertEquals(vertexWeights3.get(v), vertexWeightsSub3.get(v));
							assertEquals(vertexWeights4.get(v), vertexWeightsSub4.get(v));
							assertEquals(vertexWeights5.get(v), vertexWeightsSub5.get(v));
							assertEquals(vertexWeights6.get(v), vertexWeightsSub6.get(v));
							assertEqualsBool(vertexWeights7.get(v), vertexWeightsSub7.get(v));
							assertEquals(vertexWeights8.get(v), vertexWeightsSub8.get(v));
							assertEquals(vertexWeights9.get(v), vertexWeightsSub9.get(v));
						}
						for (Integer e : subGraph.edges()) {
							assertEquals(edgeWeights1.get(e), edgeWeightsSub1.get(e));
							assertEquals(edgeWeights2.get(e), edgeWeightsSub2.get(e));
							assertEquals(edgeWeights3.get(e), edgeWeightsSub3.get(e));
							assertEquals(edgeWeights4.get(e), edgeWeightsSub4.get(e));
							assertEquals(edgeWeights5.get(e), edgeWeightsSub5.get(e));
							assertEquals(edgeWeights6.get(e), edgeWeightsSub6.get(e));
							assertEqualsBool(edgeWeights7.get(e), edgeWeightsSub7.get(e));
							assertEquals(edgeWeights8.get(e), edgeWeightsSub8.get(e));
							assertEquals(edgeWeights9.get(e), edgeWeightsSub9.get(e));
						}
					}
				}
			}
		});
	}

	@Test
	public void testGraphEqualsWeights() {
		final long seed = 0xa9af376e90cd5845L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean directed : BooleanList.of(false, true)) {
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> g0 = new RandomGraphBuilder(seedGen.nextSeed()).graphImpl(intGraph).n(100)
							.m(400).directed(directed).parallelEdges(true).selfEdges(true).cycles(true).connected(false)
							.build();
					Graph<Integer, Integer> g1 = index ? g0.indexGraph() : g0;

					addWeights(g1, rand);
					WeightsByte<Integer> vertexWeights1g1 = g1.getVerticesWeights("weights1");
					WeightsShort<Integer> vertexWeights2g1 = g1.getVerticesWeights("weights2");
					WeightsInt<Integer> vertexWeights3g1 = g1.getVerticesWeights("weights3");
					WeightsLong<Integer> vertexWeights4g1 = g1.getVerticesWeights("weights4");
					WeightsFloat<Integer> vertexWeights5g1 = g1.getVerticesWeights("weights5");
					WeightsDouble<Integer> vertexWeights6g1 = g1.getVerticesWeights("weights6");
					WeightsBool<Integer> vertexWeights7g1 = g1.getVerticesWeights("weights7");
					WeightsChar<Integer> vertexWeights8g1 = g1.getVerticesWeights("weights8");
					WeightsObj<Integer, Object> vertexWeights9g1 = g1.getVerticesWeights("weights9");
					WeightsByte<Integer> edgeWeights1g1 = g1.getEdgesWeights("weights1");
					WeightsShort<Integer> edgeWeights2g1 = g1.getEdgesWeights("weights2");
					WeightsInt<Integer> edgeWeights3g1 = g1.getEdgesWeights("weights3");
					WeightsLong<Integer> edgeWeights4g1 = g1.getEdgesWeights("weights4");
					WeightsFloat<Integer> edgeWeights5g1 = g1.getEdgesWeights("weights5");
					WeightsDouble<Integer> edgeWeights6g1 = g1.getEdgesWeights("weights6");
					WeightsBool<Integer> edgeWeights7g1 = g1.getEdgesWeights("weights7");
					WeightsChar<Integer> edgeWeights8g1 = g1.getEdgesWeights("weights8");
					WeightsObj<Integer, Object> edgeWeights9g1 = g1.getEdgesWeights("weights9");

					Graph<Integer, Integer> g2 = g1.copy(true);
					WeightsByte<Integer> vertexWeights1g2 = g2.getVerticesWeights("weights1");
					WeightsShort<Integer> vertexWeights2g2 = g2.getVerticesWeights("weights2");
					WeightsInt<Integer> vertexWeights3g2 = g2.getVerticesWeights("weights3");
					WeightsLong<Integer> vertexWeights4g2 = g2.getVerticesWeights("weights4");
					WeightsFloat<Integer> vertexWeights5g2 = g2.getVerticesWeights("weights5");
					WeightsDouble<Integer> vertexWeights6g2 = g2.getVerticesWeights("weights6");
					WeightsBool<Integer> vertexWeights7g2 = g2.getVerticesWeights("weights7");
					WeightsChar<Integer> vertexWeights8g2 = g2.getVerticesWeights("weights8");
					WeightsObj<Integer, Object> vertexWeights9g2 = g2.getVerticesWeights("weights9");
					WeightsByte<Integer> edgeWeights1g2 = g2.getEdgesWeights("weights1");
					WeightsShort<Integer> edgeWeights2g2 = g2.getEdgesWeights("weights2");
					WeightsInt<Integer> edgeWeights3g2 = g2.getEdgesWeights("weights3");
					WeightsLong<Integer> edgeWeights4g2 = g2.getEdgesWeights("weights4");
					WeightsFloat<Integer> edgeWeights5g2 = g2.getEdgesWeights("weights5");
					WeightsDouble<Integer> edgeWeights6g2 = g2.getEdgesWeights("weights6");
					WeightsBool<Integer> edgeWeights7g2 = g2.getEdgesWeights("weights7");
					WeightsChar<Integer> edgeWeights8g2 = g2.getEdgesWeights("weights8");
					WeightsObj<Integer, Object> edgeWeights9g2 = g2.getEdgesWeights("weights9");

					Integer v = g1.vertices().iterator().next();
					Integer e = g1.edges().iterator().next();

					assertEquals(g1, g2);

					vertexWeights1g2.set(v, (byte) (vertexWeights1g2.get(v) + 1));
					assertNotEquals(g1, g2);
					vertexWeights1g2.set(v, vertexWeights1g1.get(v));
					assert g1.equals(g2);

					vertexWeights2g2.set(v, (short) (vertexWeights2g2.get(v) + 1));
					assertNotEquals(g1, g2);
					vertexWeights2g2.set(v, vertexWeights2g1.get(v));
					assert g1.equals(g2);

					vertexWeights3g2.set(v, vertexWeights3g2.get(v) + 1);
					assertNotEquals(g1, g2);
					vertexWeights3g2.set(v, vertexWeights3g1.get(v));
					assert g1.equals(g2);

					vertexWeights4g2.set(v, vertexWeights4g2.get(v) + 1);
					assertNotEquals(g1, g2);
					vertexWeights4g2.set(v, vertexWeights4g1.get(v));
					assert g1.equals(g2);

					vertexWeights5g2.set(v, vertexWeights5g2.get(v) + 1);
					assertNotEquals(g1, g2);
					vertexWeights5g2.set(v, vertexWeights5g1.get(v));
					assert g1.equals(g2);

					vertexWeights6g2.set(v, vertexWeights6g2.get(v) + 1);
					assertNotEquals(g1, g2);
					vertexWeights6g2.set(v, vertexWeights6g1.get(v));
					assert g1.equals(g2);

					vertexWeights7g2.set(v, !vertexWeights7g2.get(v));
					assertNotEquals(g1, g2);
					vertexWeights7g2.set(v, vertexWeights7g1.get(v));
					assert g1.equals(g2);

					vertexWeights8g2.set(v, (char) (vertexWeights8g2.get(v) + 1));
					assertNotEquals(g1, g2);
					vertexWeights8g2.set(v, vertexWeights8g1.get(v));
					assert g1.equals(g2);

					vertexWeights9g2.set(v, new Object());
					assertNotEquals(g1, g2);
					vertexWeights9g2.set(v, vertexWeights9g1.get(v));
					assert g1.equals(g2);

					edgeWeights1g2.set(e, (byte) (edgeWeights1g2.get(e) + 1));
					assertNotEquals(g1, g2);
					edgeWeights1g2.set(e, edgeWeights1g1.get(e));
					assert g1.equals(g2);

					edgeWeights2g2.set(e, (short) (edgeWeights2g2.get(e) + 1));
					assertNotEquals(g1, g2);
					edgeWeights2g2.set(e, edgeWeights2g1.get(e));
					assert g1.equals(g2);

					edgeWeights3g2.set(e, edgeWeights3g2.get(e) + 1);
					assertNotEquals(g1, g2);
					edgeWeights3g2.set(e, edgeWeights3g1.get(e));
					assert g1.equals(g2);

					edgeWeights4g2.set(e, edgeWeights4g2.get(e) + 1);
					assertNotEquals(g1, g2);
					edgeWeights4g2.set(e, edgeWeights4g1.get(e));
					assert g1.equals(g2);

					edgeWeights5g2.set(e, edgeWeights5g2.get(e) + 1);
					assertNotEquals(g1, g2);
					edgeWeights5g2.set(e, edgeWeights5g1.get(e));
					assert g1.equals(g2);

					edgeWeights6g2.set(e, edgeWeights6g2.get(e) + 1);
					assertNotEquals(g1, g2);
					edgeWeights6g2.set(e, edgeWeights6g1.get(e));
					assert g1.equals(g2);

					edgeWeights7g2.set(e, !edgeWeights7g2.get(e));
					assertNotEquals(g1, g2);
					edgeWeights7g2.set(e, edgeWeights7g1.get(e));
					assert g1.equals(g2);

					edgeWeights8g2.set(e, (char) (edgeWeights8g2.get(e) + 1));
					assertNotEquals(g1, g2);
					edgeWeights8g2.set(e, edgeWeights8g1.get(e));
					assert g1.equals(g2);

					edgeWeights9g2.set(e, new Object());
					assertNotEquals(g1, g2);
					edgeWeights9g2.set(e, edgeWeights9g1.get(e));
					assert g1.equals(g2);
				}
			}
		}
	}

	@Test
	public void testEqualNegativeDirected() {
		final long seed = 0x2a9b993ea2f19151L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean directed : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g1 =
						new RandomGraphBuilder(seedGen.nextSeed()).graphImpl(intGraph).n(100).m(400).directed(directed)
								.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();

				Graph<Integer, Integer> g2;
				if (g1 instanceof IntGraph) {
					g2 = directed ? IntGraph.newUndirected() : IntGraph.newDirected();
				} else {
					g2 = directed ? Graph.newUndirected() : Graph.newDirected();
				}
				for (Integer v : g1.vertices())
					g2.addVertex(v);
				for (Integer e : g1.edges())
					g2.addEdge(g1.edgeSource(e), g1.edgeTarget(e), e);

				assertNotEquals(g1, g2);
			}
		}
	}

	@Test
	public void testEqualNegativeDifferentVertices() {
		final long seed = 0xe68db99aa8e9bbf1L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean directed : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g1 =
						new RandomGraphBuilder(seedGen.nextSeed()).graphImpl(intGraph).n(100).m(400).directed(directed)
								.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();

				Graph<Integer, Integer> g2;
				if (g1 instanceof IntGraph) {
					g2 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				} else {
					g2 = directed ? Graph.newDirected() : Graph.newUndirected();
				}
				for (Integer v : g1.vertices())
					g2.addVertex(v);
				for (;;) {
					int v = rand.nextInt();
					if (v >= 1 && !g1.vertices().contains(Integer.valueOf(v))) {
						g2.addVertex(Integer.valueOf(v));
						break;
					}
				}
				for (Integer e : g1.edges())
					g2.addEdge(g1.edgeSource(e), g1.edgeTarget(e), e);

				assertNotEquals(g1, g2);
			}
		}
	}

	@Test
	public void testEqualNegativeDifferentEdges() {
		final long seed = 0x713602fa31b3e82aL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean directed : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g1 =
						new RandomGraphBuilder(seedGen.nextSeed()).graphImpl(intGraph).n(100).m(400).directed(directed)
								.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();

				Graph<Integer, Integer> g2;
				if (g1 instanceof IntGraph) {
					g2 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				} else {
					g2 = directed ? Graph.newDirected() : Graph.newUndirected();
				}
				for (Integer v : g1.vertices())
					g2.addVertex(v);
				for (Integer e : g1.edges())
					g2.addEdge(g1.edgeSource(e), g1.edgeTarget(e), e);
				for (;;) {
					int e = rand.nextInt();
					if (e >= 1 && !g1.edges().contains(Integer.valueOf(e))) {
						g2.addEdge(Graphs.randVertex(g2, rand), Graphs.randVertex(g2, rand), Integer.valueOf(e));
						break;
					}
				}

				assertNotEquals(g1, g2);
			}
		}
	}

	@Test
	public void testEqualNegativeDifferentEndpoints() {
		final long seed = 0x96bda5169a1e5e5cL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean directed : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g1 =
						new RandomGraphBuilder(seedGen.nextSeed()).graphImpl(intGraph).n(100).m(400).directed(directed)
								.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();

				Graph<Integer, Integer> g2;
				if (g1 instanceof IntGraph) {
					g2 = directed ? IntGraph.newDirected() : IntGraph.newUndirected();
				} else {
					g2 = directed ? Graph.newDirected() : Graph.newUndirected();
				}
				for (Integer v : g1.vertices())
					g2.addVertex(v);
				for (Integer e : g1.edges())
					g2.addEdge(g1.edgeSource(e), Graphs.randVertex(g2, rand), e);

				assertNotEquals(g1, g2);
			}
		}
	}

	@Test
	public void testEqualNegativeDifferentWeightsKeys() {
		final long seed = 0xd7a3e806dd5e50a1L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean directed : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g1 =
						new RandomGraphBuilder(seedGen.nextSeed()).graphImpl(intGraph).n(100).m(400).directed(directed)
								.parallelEdges(true).selfEdges(true).cycles(true).connected(false).build();

				Graph<Integer, Integer> g2 = g1.copy();
				assertEquals(g1, g2);
				g2.addVerticesWeights("6548949", char.class);
				assertNotEquals(g1, g2);

				g2 = g1.copy();
				assertEquals(g1, g2);
				g2.addEdgesWeights("dfgfdslk", int.class);
				assertNotEquals(g1, g2);
			}
		}
	}

	@Test
	public void testGraphHashCode() {
		final long seed = 0x1076bbfb8212b47eL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean directed : BooleanList.of(false, true)) {
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> g0 = new RandomGraphBuilder(seedGen.nextSeed()).graphImpl(intGraph).n(100)
							.m(400).directed(directed).parallelEdges(true).selfEdges(true).cycles(true).connected(false)
							.build();
					Graph<Integer, Integer> g1 = index ? g0.indexGraph() : g0;

					addWeights(g1, rand);
					Graph<Integer, Integer> g2 = g1.copy(true);

					assertEquals(g1.hashCode(), g2.hashCode());
				}
			}
		}
	}

	private static void addWeights(Graph<Integer, Integer> g, Random rand) {
		WeightsByte<Integer> vertexWeights1 = g.addVerticesWeights("weights1", byte.class);
		WeightsShort<Integer> vertexWeights2 = g.addVerticesWeights("weights2", short.class);
		WeightsInt<Integer> vertexWeights3 = g.addVerticesWeights("weights3", int.class);
		WeightsLong<Integer> vertexWeights4 = g.addVerticesWeights("weights4", long.class);
		WeightsFloat<Integer> vertexWeights5 = g.addVerticesWeights("weights5", float.class);
		WeightsDouble<Integer> vertexWeights6 = g.addVerticesWeights("weights6", double.class);
		WeightsBool<Integer> vertexWeights7 = g.addVerticesWeights("weights7", boolean.class);
		WeightsChar<Integer> vertexWeights8 = g.addVerticesWeights("weights8", char.class);
		WeightsObj<Integer, Object> vertexWeights9 = g.addVerticesWeights("weights9", Object.class);
		WeightsByte<Integer> edgeWeights1 = g.addEdgesWeights("weights1", byte.class);
		WeightsShort<Integer> edgeWeights2 = g.addEdgesWeights("weights2", short.class);
		WeightsInt<Integer> edgeWeights3 = g.addEdgesWeights("weights3", int.class);
		WeightsLong<Integer> edgeWeights4 = g.addEdgesWeights("weights4", long.class);
		WeightsFloat<Integer> edgeWeights5 = g.addEdgesWeights("weights5", float.class);
		WeightsDouble<Integer> edgeWeights6 = g.addEdgesWeights("weights6", double.class);
		WeightsBool<Integer> edgeWeights7 = g.addEdgesWeights("weights7", boolean.class);
		WeightsChar<Integer> edgeWeights8 = g.addEdgesWeights("weights8", char.class);
		WeightsObj<Integer, Object> edgeWeights9 = g.addEdgesWeights("weights9", Object.class);

		for (Integer v : g.vertices()) {
			vertexWeights1.set(v, (byte) rand.nextInt(100));
			vertexWeights2.set(v, (short) rand.nextInt(100));
			vertexWeights3.set(v, rand.nextInt(100));
			vertexWeights4.set(v, rand.nextInt(100));
			vertexWeights5.set(v, rand.nextInt(100) / 10.0f);
			vertexWeights6.set(v, rand.nextInt(100) / 10.0);
			vertexWeights7.set(v, rand.nextBoolean());
			vertexWeights8.set(v, (char) ('0' + rand.nextInt(10)));
			vertexWeights9.set(v, String.valueOf(rand.nextInt(100)));
		}
		for (Integer e : g.edges()) {
			edgeWeights1.set(e, (byte) rand.nextInt(100));
			edgeWeights2.set(e, (short) rand.nextInt(100));
			edgeWeights3.set(e, rand.nextInt(100));
			edgeWeights4.set(e, rand.nextInt(100));
			edgeWeights5.set(e, rand.nextInt(100) / 10.0f);
			edgeWeights6.set(e, rand.nextInt(100) / 10.0);
			edgeWeights7.set(e, rand.nextBoolean());
			edgeWeights8.set(e, (char) ('0' + rand.nextInt(10)));
			edgeWeights9.set(e, String.valueOf(rand.nextInt(100)));
		}
	}

}