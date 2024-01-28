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

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.gen.GnmGraphGenerator;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class ImmutableGraphCopyTest extends TestBase {

	private static final String VerticesWeightsKey = "vWeights";
	private static final String EdgesWeightsKey = "eWeights";

	@SuppressWarnings("boxing")
	private static Graph<Integer, Integer> createGraph(boolean intGraph, boolean directed, boolean parallelEdges) {
		final long seed = 0x4ff62bb8f3a0b831L;
		final Random rand = new Random(seed);
		final int n = 47, m = parallelEdges ? 1345 : 120;
		GnmGraphGenerator<Integer, Integer> gen =
				intGraph ? new GnmGraphGenerator<>(IntGraphFactory.undirected()) : new GnmGraphGenerator<>();
		Graph<Integer, Integer> g = gen
				.seed(rand.nextLong())
				.directed(directed)
				.parallelEdges(parallelEdges)
				.vertices(n, IdBuilderInt.defaultBuilder())
				.edges(m, IdBuilderInt.defaultBuilder())
				.generateMutable();

		WeightsInt<Integer> vWeights = g.addVerticesWeights(VerticesWeightsKey, int.class);
		for (int v : g.vertices())
			vWeights.set(v, rand.nextInt(10000));

		WeightsInt<Integer> eWeights = g.addEdgesWeights(EdgesWeightsKey, int.class);
		for (int e : g.edges())
			eWeights.set(e, rand.nextInt(10000));
		return g;
	}

	private static IndexGraph immutableCopy(IndexGraph g, boolean fastLookup) {
		if (!fastLookup)
			return g.immutableCopy();
		return IndexGraphFactory
				.newInstance(g.isDirected())
				.addHint(GraphFactory.Hint.FastEdgeLookup)
				.newImmutableCopyOf(g);
	}

	private static IndexGraph immutableCopy(IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights,
			boolean fastLookup) {
		if (!fastLookup)
			return g.immutableCopy(copyVerticesWeights, copyEdgesWeights);
		return IndexGraphFactory
				.newInstance(g.isDirected())
				.addHint(GraphFactory.Hint.FastEdgeLookup)
				.newImmutableCopyOf(g, copyVerticesWeights, copyEdgesWeights);
	}

	private static <V, E> Graph<V, E> immutableCopy(Graph<V, E> g, boolean fastLookup) {
		if (!fastLookup)
			return g.immutableCopy();
		return factory(g).addHint(GraphFactory.Hint.FastEdgeLookup).newImmutableCopyOf(g);
	}

	private static <V, E> Graph<V, E> immutableCopy(Graph<V, E> g, boolean fastLookup, boolean fromBuilder) {
		if (fromBuilder) {
			GraphFactory<V, E> factory = factory(g);
			if (fastLookup)
				factory.addHint(GraphFactory.Hint.FastEdgeLookup);
			return factory.newBuilderCopyOf(g).build();
		}
		if (!fastLookup)
			return g.immutableCopy();
		return factory(g).addHint(GraphFactory.Hint.FastEdgeLookup).newImmutableCopyOf(g);
	}

	private static <V, E> Graph<V, E> immutableCopy(Graph<V, E> g, boolean copyVerticesWeights,
			boolean copyEdgesWeights, boolean fastLookup) {
		if (!fastLookup)
			return g.immutableCopy(copyVerticesWeights, copyEdgesWeights);
		return factory(g)
				.addHint(GraphFactory.Hint.FastEdgeLookup)
				.newImmutableCopyOf(g, copyVerticesWeights, copyEdgesWeights);
	}

	@SuppressWarnings("unchecked")
	private static <V, E> GraphFactory<V, E> factory(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return (GraphFactory<V, E>) IndexGraphFactory.newInstance(g.isDirected());
		} else if (g instanceof IntGraph) {
			return (GraphFactory<V, E>) IntGraphFactory.newInstance(g.isDirected());
		} else {
			return GraphFactory.newInstance(g.isDirected());
		}
	}

	@Test
	public void vertices() {
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);

				assertEquals(gOrig.vertices().size(), gImmutable.vertices().size());
				assertEquals(gOrig.vertices(), gImmutable.vertices());
			});
		});
	}

	@Test
	public void edges() {
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);
				assertEquals(gOrig.edges().size(), gImmutable.edges().size());
				assertEquals(gOrig.edges(), gImmutable.edges());
			});
		});
	}

	@Test
	public void addRemoveVertex() {
		final Random rand = new Random(0xaf48a73a63a773daL);
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);

				Integer nonExistingVertex =
						gImmutable instanceof IndexGraph ? Integer.valueOf(gImmutable.vertices().size())
								: GraphsTestUtils.nonExistingVertexNonNegative(gImmutable, rand);
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.addVertex());
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.addVertex(nonExistingVertex));

				Integer vertexToRemove = gImmutable.vertices().iterator().next();
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeVertex(vertexToRemove));
			});
		});
	}

	@Test
	public void removeVertices() {
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);

				assertThrows(UnsupportedOperationException.class,
						() -> gImmutable.removeVertices(List.of(gImmutable.vertices().iterator().next())));
			});
		});
	}

	@Test
	public void addVertices() {
		final Random rand = new Random(0x3d801f2f5536a92fL);
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);

				assertThrows(UnsupportedOperationException.class, () -> gImmutable
						.addVertices(List.of(GraphsTestUtils.nonExistingVertexNonNegative(gImmutable, rand))));
			});
		});
	}

	@Test
	public void renameVertex() {
		final Random rand = new Random(0x579e499ecda79793L);
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);

				Integer v = gImmutable.vertices().iterator().next();
				Integer vNew = GraphsTestUtils.nonExistingVertexNonNegative(gImmutable, rand);
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.renameVertex(v, vNew));
			});
		});
	}

	@Test
	public void addRemoveEdge() {
		final Random rand = new Random(0x721556d31186bb4eL);
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);

				Iterator<Integer> vit = gImmutable.vertices().iterator();
				Integer u = vit.next();
				Integer v = vit.next();

				Integer nonExistingEdge = gImmutable instanceof IndexGraph ? Integer.valueOf(gImmutable.edges().size())
						: GraphsTestUtils.nonExistingEdgeNonNegative(gImmutable, rand);
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.addEdge(u, v));
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.addEdge(u, v, nonExistingEdge));

				Integer edgeToRemove = gImmutable.edges().iterator().next();
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeEdge(edgeToRemove));
			});
		});
	}

	@Test
	public void addEdges() {
		final Random rand = new Random(0x21f6dfd8efe0a416L);
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);

				Iterator<Integer> vit = gImmutable.vertices().iterator();
				Integer u = vit.next();
				Integer v = vit.next();
				Integer nonExistingEdge = GraphsTestUtils.nonExistingEdgeNonNegative(gImmutable, rand);
				IntGraph g1 = IntGraph.newDirected();
				g1.addVertices(List.of(u, v));
				g1.addEdge(u.intValue(), v.intValue(), nonExistingEdge.intValue());
				IEdgeSet edges = IEdgeSet.allOf(g1);

				assertThrows(UnsupportedOperationException.class, () -> gImmutable.addEdges(edges));
				if (gImmutable instanceof IndexGraph)
					assertThrows(UnsupportedOperationException.class,
							() -> ((IndexGraph) gImmutable).addEdgesReassignIds(edges));
			});
		});
	}

	@Test
	public void renameEdge() {
		final Random rand = new Random(0x9f8be40df7323a78L);
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);

				Integer nonExistingEdge = GraphsTestUtils.nonExistingEdgeNonNegative(gImmutable, rand);
				assertThrows(UnsupportedOperationException.class,
						() -> gImmutable.renameEdge(gImmutable.edges().iterator().next(), nonExistingEdge));
			});
		});
	}

	@Test
	public void edgesOutIn() {
		final Random rand = new Random(0xa415bd8a8246c2e5L);
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);

				for (Integer u : gImmutable.vertices()) {
					EdgeSet<Integer, Integer> edges = gImmutable.outEdges(u);
					assertEquals(gOrig.outEdges(u).size(), edges.size());
					assertEqualsBool(gOrig.outEdges(u).isEmpty(), edges.isEmpty());
					assertEquals(gOrig.outEdges(u), edges);

					Set<Integer> iteratedEdges = new IntOpenHashSet();
					for (EdgeIter<Integer, Integer> eit = edges.iterator(); eit.hasNext();) {
						Integer peekNext = eit.peekNext();
						Integer e = eit.next();
						assertEquals(e, peekNext);

						assertEquals(u, eit.source());
						assertEquals(gOrig.edgeEndpoint(e, u), eit.target());
						assertEquals(gImmutable.edgeEndpoint(e, u), eit.target());
						assertEquals(u, gImmutable.edgeEndpoint(e, eit.target()));

						iteratedEdges.add(e);
					}

					assertEquals(edges.size(), iteratedEdges.size());
					for (Integer e : gOrig.edges()) {
						if (iteratedEdges.contains(e)) {
							assertTrue(edges.contains(e));
						} else {
							assertFalse(edges.contains(e));
						}
					}
				}
				for (Integer v : gImmutable.vertices()) {
					EdgeSet<Integer, Integer> edges = gImmutable.inEdges(v);
					assertEquals(gOrig.inEdges(v).size(), edges.size());
					assertEquals(gOrig.inEdges(v), edges);

					Set<Integer> iteratedEdges = new IntOpenHashSet();
					for (EdgeIter<Integer, Integer> eit = edges.iterator(); eit.hasNext();) {
						Integer peekNext = eit.peekNext();
						Integer e = eit.next();
						assertEquals(e, peekNext);

						assertEquals(v, eit.target());
						assertEquals(gOrig.edgeEndpoint(e, v), eit.source());
						assertEquals(gImmutable.edgeEndpoint(e, v), eit.source());
						assertEquals(v, gImmutable.edgeEndpoint(e, eit.source()));

						iteratedEdges.add(e);
					}

					assertEquals(edges.size(), iteratedEdges.size());
					for (Integer e : gOrig.edges()) {
						if (iteratedEdges.contains(e)) {
							assertTrue(edges.contains(e));
						} else {
							assertFalse(edges.contains(e));
						}
					}
				}

				for (int i = 0; i < 10; i++) {
					Integer nonExistingVertex = GraphsTestUtils.nonExistingVertex(gImmutable, rand);
					assertThrows(NoSuchVertexException.class, () -> gImmutable.outEdges(nonExistingVertex));
					assertThrows(NoSuchVertexException.class, () -> gImmutable.inEdges(nonExistingVertex));
				}
			});
		});
	}

	@Test
	public void getEdges() {
		final Random rand = new Random(0xdd41019ca74783d4L);
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges, fromBuilder) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup, fromBuilder);

				for (Integer u : gImmutable.vertices()) {
					for (Integer v : gImmutable.vertices()) {
						EdgeSet<Integer, Integer> edges = gImmutable.getEdges(u, v);
						if (gOrig.getEdges(u, v).size() != edges.size())
							assertEquals(gOrig.getEdges(u, v).size(), edges.size());
						assertEquals(gOrig.getEdges(u, v), edges);

						if (edges.isEmpty()) {
							assertFalse(gImmutable.containsEdge(u, v));
						} else {
							Integer e = gImmutable.getEdge(u, v);
							assertNotEquals(null, e);
							assertTrue(edges.contains(e));
						}
						assertFalse(edges.contains(GraphsTestUtils.nonExistingEdge(gOrig, rand)));
						for (int i = 0; i < 20; i++) {
							Integer e = Graphs.randEdge(gOrig, rand);
							Integer u1 = gOrig.edgeSource(e), v1 = gOrig.edgeTarget(e);
							boolean expectedContains;
							if (directed) {
								expectedContains = u.equals(u1) && v.equals(v1);
							} else {
								expectedContains = (u.equals(u1) && v.equals(v1)) || (u.equals(v1) && v.equals(u1));
							}
							assertEqualsBool(expectedContains, edges.contains(e));
						}

						for (EdgeIter<Integer, Integer> eit = edges.iterator(); eit.hasNext();) {
							Integer peekNext = eit.peekNext();
							Integer e = eit.next();
							assertEquals(e, peekNext);

							assertEquals(u, eit.source());
							assertEquals(v, eit.target());
							assertEquals(gOrig.edgeEndpoint(e, u), v);
							assertEquals(gOrig.edgeEndpoint(e, v), u);
							assertEquals(u, gImmutable.edgeEndpoint(e, v));
							assertEquals(v, gImmutable.edgeEndpoint(e, u));
						}
					}
				}

				assertThrows(NoSuchVertexException.class, () -> gImmutable
						.getEdges(GraphsTestUtils.nonExistingVertex(gOrig, rand), Graphs.randVertex(gOrig, rand)));
				assertThrows(NoSuchVertexException.class, () -> gImmutable
						.getEdges(Graphs.randVertex(gOrig, rand), GraphsTestUtils.nonExistingVertex(gOrig, rand)));
			});
		});
	}

	@Test
	public void removeEdges() {
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);

				assertThrows(UnsupportedOperationException.class,
						() -> gImmutable.removeEdges(List.of(gImmutable.edges().iterator().next())));
			});
		});
	}

	@Test
	public void removeEdgesOf() {
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);
				Integer v = gImmutable.vertices().iterator().next();
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeEdgesOf(v));
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeOutEdgesOf(v));
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeInEdgesOf(v));
			});
		});
	}

	@Test
	public void reverseEdge() {
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);
				Integer e = gImmutable.edges().iterator().next();
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.reverseEdge(e));
			});
		});
	}

	@Test
	public void moveEdge() {
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);
				Integer e = gImmutable.edges().iterator().next();
				Integer v = gImmutable.vertices().iterator().next();
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.moveEdge(e, v, v));
			});
		});
	}

	@Test
	public void edgeGetSourceTarget() {
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);
				for (Integer e : gImmutable.edges()) {
					assertEquals(gOrig.edgeSource(e), gImmutable.edgeSource(e));
					assertEquals(gOrig.edgeTarget(e), gImmutable.edgeTarget(e));
				}
			});
		});
	}

	@Test
	public void clear() {
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.clear());
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.clearEdges());
			});
		});
	}

	@Test
	public void verticesWeights() {
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, true, true, fastLookup);

				assertEquals(gOrig.verticesWeightsKeys(), gImmutable.verticesWeightsKeys());
				WeightsInt<Integer> wOrig = gOrig.verticesWeights(VerticesWeightsKey);
				WeightsInt<Integer> wImmutable = gImmutable.verticesWeights(VerticesWeightsKey);

				for (Integer v : gImmutable.vertices())
					assertEquals(wOrig.get(v), wImmutable.get(v));
				assertEquals(wOrig.defaultWeight(), wImmutable.defaultWeight());

				Integer vertex = gImmutable.vertices().iterator().next();
				assertThrows(UnsupportedOperationException.class, () -> wImmutable.set(vertex, 42));
				assertThrows(UnsupportedOperationException.class,
						() -> gImmutable.removeVerticesWeights(VerticesWeightsKey));
				assertThrows(UnsupportedOperationException.class,
						() -> gImmutable.addVerticesWeights("key", Object.class));
			});
		});
	}

	@Test
	public void edgesWeights() {
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, true, true, fastLookup);

				assertEquals(gOrig.edgesWeightsKeys(), gImmutable.edgesWeightsKeys());
				WeightsInt<Integer> wOrig = gOrig.edgesWeights(EdgesWeightsKey);
				WeightsInt<Integer> wImmutable = gImmutable.edgesWeights(EdgesWeightsKey);

				for (Integer e : gImmutable.edges())
					assertEquals(wOrig.get(e), wImmutable.get(e));
				assertEquals(wOrig.defaultWeight(), wImmutable.defaultWeight());

				Integer edge = gImmutable.edges().iterator().next();
				assertThrows(UnsupportedOperationException.class, () -> wImmutable.set(edge, 42));
				assertThrows(UnsupportedOperationException.class, () -> gImmutable.removeEdgesWeights(EdgesWeightsKey));
				assertThrows(UnsupportedOperationException.class,
						() -> gImmutable.addEdgesWeights("key", Object.class));
			});
		});
	}

	@Test
	public void graphCapabilities() {
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);

				assertEqualsBool(gOrig.isDirected(), gImmutable.isDirected());
				if (!gOrig.isAllowSelfEdges())
					assertFalse(gImmutable.isAllowSelfEdges());
				if (!gOrig.isAllowParallelEdges())
					assertFalse(gImmutable.isAllowParallelEdges());
			});
		});
	}

	@Test
	public void immutableViewOfImmutableCopy() {
		foreachBoolConfig((intGraph, directed, fastLookup, parallelEdges) -> {
			IndexGraph gOrig = createGraph(intGraph, directed, parallelEdges).indexGraph();
			IndexGraph gImmutable = immutableCopy(gOrig, fastLookup);

			assertTrue(gImmutable == gImmutable.immutableView());
		});
	}

	@Test
	public void immutableCopyOfImmutableCopy() {
		/* immutable copy index graph */
		foreachBoolConfig((intGraph, reindexing, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				IndexGraph gImmutable;
				if (reindexing) {
					Graph<Integer, Integer> gOrig = createGraph(intGraph, directed, parallelEdges);
					gImmutable = immutableCopy(gOrig, true, true, fastLookup).indexGraph();
				} else {
					IndexGraph gOrig = createGraph(intGraph, directed, parallelEdges).indexGraph();
					gImmutable = immutableCopy(gOrig, true, true, fastLookup);
				}

				foreachBoolConfig((copyVerticesWeights, copyEdgesWeights) -> {
					IndexGraph gImmutable2;
					if (!copyVerticesWeights && !copyEdgesWeights) {
						gImmutable2 = immutableCopy(gImmutable, fastLookup);
					} else {
						gImmutable2 = immutableCopy(gImmutable, copyVerticesWeights, copyEdgesWeights, fastLookup);
					}
					assertEquals(gImmutable.copy(copyVerticesWeights, copyEdgesWeights), gImmutable2);

					if (copyVerticesWeights) {
						assertEquals(gImmutable.verticesWeightsKeys(), gImmutable2.verticesWeightsKeys());
					} else {
						assertEquals(Set.of(), gImmutable2.verticesWeightsKeys());
					}
					if (copyEdgesWeights) {
						assertEquals(gImmutable.edgesWeightsKeys(), gImmutable2.edgesWeightsKeys());
					} else {
						assertEquals(Set.of(), gImmutable2.edgesWeightsKeys());
					}

					assertEquals(gImmutable2, immutableCopy(gImmutable2, true, true, fastLookup));
					assertEquals(gImmutable2, immutableCopy(gImmutable2, true, true, !fastLookup));
				});
			});
		});
		/* immutable copy regular graph */
		foreachBoolConfig((intGraph, reindexing, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, true, true, fastLookup);

				foreachBoolConfig((copyVerticesWeights, copyEdgesWeights) -> {
					Graph<Integer, Integer> gImmutable2;
					if (!copyVerticesWeights && !copyEdgesWeights) {
						gImmutable2 = immutableCopy(gImmutable, fastLookup);
					} else {
						gImmutable2 = immutableCopy(gImmutable, copyVerticesWeights, copyEdgesWeights, fastLookup);
					}
					assertEquals(gImmutable.copy(copyVerticesWeights, copyEdgesWeights), gImmutable2);

					if (copyVerticesWeights) {
						assertEquals(gImmutable.verticesWeightsKeys(), gImmutable2.verticesWeightsKeys());
					} else {
						assertEquals(Set.of(), gImmutable2.verticesWeightsKeys());
					}
					if (copyEdgesWeights) {
						assertEquals(gImmutable.edgesWeightsKeys(), gImmutable2.edgesWeightsKeys());
					} else {
						assertEquals(Set.of(), gImmutable2.edgesWeightsKeys());
					}

					assertEquals(gImmutable2, immutableCopy(gImmutable2, true, true, fastLookup));
				});
			});
		});
	}

	@Test
	public void removeListener() {
		foreachBoolConfig((intGraph, directed, fastLookup, parallelEdges) -> {
			IndexGraph gOrig = createGraph(intGraph, directed, parallelEdges).indexGraph();
			IndexGraph gImmutable = immutableCopy(gOrig, fastLookup);

			assertThrows(NullPointerException.class, () -> gImmutable.addVertexRemoveListener(null));
			assertThrows(NullPointerException.class, () -> gImmutable.addEdgeRemoveListener(null));
			IndexRemoveListener listener = new IndexRemoveListener() {
				@Override
				public void removeLast(int removedIdx) {}

				@Override
				public void swapAndRemove(int removedIdx, int swappedIdx) {}
			};

			/* these listeners should do nothing and never be called, graph is immutable */
			gImmutable.addVertexRemoveListener(listener);
			gImmutable.addEdgeRemoveListener(listener);
			gImmutable.removeVertexRemoveListener(listener);
			gImmutable.removeEdgeRemoveListener(listener);
		});
	}

	@Test
	public void ensureCapacity() {
		/* can't real test anything, just cover and see no exception is thrown */
		foreachBoolConfig((intGraph, index, fastLookup) -> {
			foreachBoolConfig((directed, parallelEdges) -> {
				Graph<Integer, Integer> gOrig0 = createGraph(intGraph, directed, parallelEdges);
				Graph<Integer, Integer> gOrig = index ? gOrig0.indexGraph() : gOrig0;
				Graph<Integer, Integer> gImmutable = immutableCopy(gOrig, fastLookup);

				gImmutable.ensureVertexCapacity(gImmutable.vertices().size() + 10);
				gImmutable.ensureEdgeCapacity(gImmutable.edges().size() + 10);
			});
		});
	}

	@Test
	public void isAllowSelfEdges() {
		foreachBoolConfig(directed -> {
			IntGraph g = IntGraphFactory.newInstance(directed).allowSelfEdges().allowParallelEdges().newGraph();
			g.addVertices(range(10));

			g.addEdge(0, 1);
			assertFalse(g.immutableCopy().isAllowSelfEdges());

			g.addEdge(3, 3);
			assertTrue(g.immutableCopy().isAllowSelfEdges());
		});
	}

	@Test
	public void isAllowParallelEdges() {
		foreachBoolConfig(directed -> {
			IntGraph g = IntGraphFactory.newInstance(directed).allowSelfEdges().allowParallelEdges().newGraph();
			g.addVertices(range(10));

			g.addEdge(0, 1);
			assertFalse(g.immutableCopy().isAllowParallelEdges());

			g.addEdge(0, 1);
			assertTrue(g.immutableCopy().isAllowParallelEdges());
		});
	}

}
