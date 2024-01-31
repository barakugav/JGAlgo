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
package com.jgalgo.gen;

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.graph.WeightsByte;
import com.jgalgo.graph.WeightsChar;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsFloat;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsLong;
import com.jgalgo.graph.WeightsObj;
import com.jgalgo.graph.WeightsShort;
import com.jgalgo.internal.util.TestBase;

@SuppressWarnings("boxing")
public class UnionGraphGeneratorTest extends TestBase {

	@Test
	public void graphFactory() {
		GraphFactory<Integer, Integer> factory = GraphFactory.undirected();
		UnionGraphGenerator<Integer, Integer> gen = new UnionGraphGenerator<>(factory);
		assertTrue(factory == gen.graphFactory());
	}

	@Test
	public void missingGraphs() {
		UnionGraphGenerator<Integer, Integer> gen = new UnionGraphGenerator<>(IntGraphFactory.undirected());
		assertThrows(IllegalStateException.class, () -> gen.generate());
	}

	@Test
	public void onlyOneGraph() {
		UnionGraphGenerator<Integer, Integer> gen = new UnionGraphGenerator<>();
		assertThrows(IllegalArgumentException.class, () -> gen.graphs(IntGraph.newDirected()));
	}

	@Test
	public void graphsWithWrongDirection() {
		UnionGraphGenerator<Integer, Integer> gen = new UnionGraphGenerator<>();
		assertThrows(IllegalArgumentException.class,
				() -> gen.graphs(IntGraph.newDirected(), IntGraph.newUndirected()));
	}

	@Test
	public void unionByEndpointsParallelEdges() {
		IntGraph g1 = IntGraph.newDirected();
		g1.addVertices(range(2));
		g1.addEdge(0, 1);
		g1.addEdge(0, 1);
		UnionGraphGenerator<Integer, Integer> gen = new UnionGraphGenerator<>(IntGraphFactory.directed())
				.graphs(g1, IntGraph.newDirected())
				.edgeUnionByEndpoints();
		assertThrows(IllegalArgumentException.class, () -> gen.generate());
	}

	@Test
	public void directed() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> union = new UnionGraphGenerator<Integer, Integer>()
					.graphs(directed ? IntGraph.newDirected() : IntGraph.newUndirected(),
							directed ? IntGraph.newDirected() : IntGraph.newUndirected())
					.generate();
			assertEqualsBool(directed, union.isDirected());
		});
	}

	@Test
	public void vertices() {
		Graph<Integer, Integer> graph1 = IntGraph.newDirected();
		graph1.addVertices(range(0, 10));
		Graph<Integer, Integer> graph2 = IntGraph.newDirected();
		graph2.addVertices(range(5, 15));
		Graph<Integer, Integer> union = new UnionGraphGenerator<Integer, Integer>().graphs(graph1, graph2).generate();
		assertEquals(range(15), union.vertices());
	}

	@Test
	public void unionById() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertices(range(3));
			graph1.addEdge(0, 1, 10);
			graph1.addEdge(1, 2, 20);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addVertices(range(3));
			graph2.addEdge(0, 1, 30);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.edgeUnionById()
					.generate();

			Graph<Integer, Integer> expected = IntGraphFactory.newInstance(directed).newGraph();
			expected.addVertices(range(3));
			expected.addEdge(0, 1, 10);
			expected.addEdge(1, 2, 20);
			expected.addEdge(0, 1, 30);
			assertEquals(expected, union);
		});
	}

	@Test
	public void unionByIdDuplicateId() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertices(range(3));
			graph1.addEdge(0, 1, 10);
			graph1.addEdge(1, 2, 20);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addVertices(range(3));
			graph2.addEdge(0, 1, 10);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.edgeUnionById()
					.generate();

			Graph<Integer, Integer> expected = IntGraphFactory.newInstance(directed).newGraph();
			expected.addVertices(range(3));
			expected.addEdge(0, 1, 10);
			expected.addEdge(1, 2, 20);
			assertEquals(expected, union);
		});
	}

	@Test
	public void unionByIdDuplicateIdDifferentEndpoints() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertices(range(3));
			graph1.addEdge(0, 1, 10);
			graph1.addEdge(1, 2, 20);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addVertices(range(3));
			graph2.addEdge(0, 2, 10);

			UnionGraphGenerator<Integer, Integer> gen =
					new UnionGraphGenerator<>(IntGraphFactory.undirected()).graphs(graph1, graph2).edgeUnionById();

			assertThrows(IllegalArgumentException.class, () -> gen.generate());
		});
	}

	@Test
	public void graphsWithSelfEdges() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertices(range(3));
			graph1.addEdge(0, 1, 10);
			graph1.addEdge(1, 2, 20);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).allowSelfEdges().newGraph();
			graph2.addVertices(range(3));
			graph2.addEdge(0, 0, 30);

			Graph<Integer, Integer> union =
					new UnionGraphGenerator<>(IntGraphFactory.undirected()).graphs(graph1, graph2).generate();

			Graph<Integer, Integer> expected = IntGraphFactory.newInstance(directed).allowSelfEdges().newGraph();
			expected.addVertices(range(3));
			expected.addEdge(0, 1, 10);
			expected.addEdge(1, 2, 20);
			expected.addEdge(0, 0, 30);
			assertEquals(expected, union);
		});
	}

	@Test
	public void unionByEndpoints() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertices(range(3));
			graph1.addEdge(0, 1, 10);
			graph1.addEdge(1, 2, 20);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addVertices(range(3));
			graph2.addEdge(0, 2, 30);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.edgeUnionByEndpoints()
					.generate();

			Graph<Integer, Integer> expected = IntGraphFactory.newInstance(directed).newGraph();
			expected.addVertices(range(3));
			expected.addEdge(0, 1, 10);
			expected.addEdge(1, 2, 20);
			expected.addEdge(0, 2, 30);
			assertEquals(expected, union);
		});
	}

	@Test
	public void unionByEndpointsDupEdge() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertices(range(3));
			graph1.addEdge(0, 1, 10);
			graph1.addEdge(1, 2, 20);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addVertices(range(3));
			if (directed) {
				graph2.addEdge(0, 1, 30);
			} else {
				graph2.addEdge(1, 0, 30);
			}

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.edgeUnionByEndpoints()
					.generate();

			Graph<Integer, Integer> expected = IntGraphFactory.newInstance(directed).newGraph();
			expected.addVertices(range(3));
			expected.addEdge(0, 1, 10);
			expected.addEdge(1, 2, 20);
			assertEquals(expected, union);
		});
	}

	@Test
	public void copyVerticesWeights() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			WeightsInt<Integer> weights11 = graph1.addVerticesWeights("w1", int.class, -1);
			WeightsInt<Integer> weights12 = graph1.addVerticesWeights("w2", int.class, -2);
			graph1.addEdgesWeights("edge-weights", int.class);
			graph1.addVertex(0);
			graph1.addVertex(1);
			weights11.set(0, 10);
			weights12.set(1, 20);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			WeightsInt<Integer> weights21 = graph2.addVerticesWeights("w1", int.class, -10);
			WeightsInt<Integer> weights23 = graph2.addVerticesWeights("w3", int.class, -30);
			graph2.addEdgesWeights("edge-weights", int.class);
			graph2.addVertex(0);
			graph2.addVertex(2);
			weights21.set(0, 100);
			weights23.set(2, 300);

			Graph<Integer, Integer> unionNoCopy = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.copyWeights(false, false)
					.generate();
			assertEquals(Set.of(), unionNoCopy.verticesWeightsKeys());
			assertEquals(Set.of(), unionNoCopy.edgesWeightsKeys());

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.copyWeights(true, false)
					.generate();
			assertEquals(Set.of("w1", "w2", "w3"), union.verticesWeightsKeys());
			assertEquals(Set.of(), union.edgesWeightsKeys());

			WeightsInt<Integer> unionWeights1 = union.verticesWeights("w1");
			WeightsInt<Integer> unionWeights2 = union.verticesWeights("w2");
			WeightsInt<Integer> unionWeights3 = union.verticesWeights("w3");
			assertEquals(-1, unionWeights1.defaultWeight());
			assertEquals(-2, unionWeights2.defaultWeight());
			assertEquals(-30, unionWeights3.defaultWeight());
			assertEquals(10, unionWeights1.get(0));
			assertEquals(-1, unionWeights1.get(1));
			assertEquals(-10, unionWeights1.get(2));
			assertEquals(-2, unionWeights2.get(0));
			assertEquals(20, unionWeights2.get(1));
			assertEquals(-2, unionWeights2.get(2));
			assertEquals(-30, unionWeights3.get(0));
			assertEquals(-30, unionWeights3.get(1));
			assertEquals(300, unionWeights3.get(2));

			assertEquals(Set.of("w1", "w3"),
					new UnionGraphGenerator<>(IntGraphFactory.undirected())
							.graphs(graph1, graph2)
							.copyWeights(true, false)
							.verticesWeightsKeys(Set.of("w1", "w3"))
							.generate()
							.verticesWeightsKeys());
			assertEquals(Set.of("w1", "w2", "w3"),
					new UnionGraphGenerator<>(IntGraphFactory.undirected())
							.graphs(graph1, graph2)
							.copyWeights(true, false)
							.verticesWeightsKeys(Set.of("w1", "w3"))
							.verticesWeightsKeys(null)
							.generate()
							.verticesWeightsKeys());
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVerticesWeights("w1", int.class);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addVerticesWeights("w1", double.class);

			UnionGraphGenerator<Integer, Integer> gen = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.copyWeights(true, false);
			assertThrows(IllegalArgumentException.class, () -> gen.generate());
		});

		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(0);
			WeightsByte<Integer> w = graph1.addVerticesWeights("w1", byte.class);
			w.set(0, (byte) 10);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(true, false)
					.generate();
			WeightsByte<Integer> unionWeights = union.verticesWeights("w1");
			assertEquals(10, unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(0);
			WeightsShort<Integer> w = graph1.addVerticesWeights("w1", short.class);
			w.set(0, (short) 10);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(true, false)
					.generate();
			WeightsShort<Integer> unionWeights = union.verticesWeights("w1");
			assertEquals(10, unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(0);
			WeightsInt<Integer> w = graph1.addVerticesWeights("w1", int.class);
			w.set(0, 10);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(true, false)
					.generate();
			WeightsInt<Integer> unionWeights = union.verticesWeights("w1");
			assertEquals(10, unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(0);
			WeightsLong<Integer> w = graph1.addVerticesWeights("w1", long.class);
			w.set(0, 10);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(true, false)
					.generate();
			WeightsLong<Integer> unionWeights = union.verticesWeights("w1");
			assertEquals(10, unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(0);
			WeightsFloat<Integer> w = graph1.addVerticesWeights("w1", float.class);
			w.set(0, 10.1f);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(true, false)
					.generate();
			WeightsFloat<Integer> unionWeights = union.verticesWeights("w1");
			assertEquals(10.1f, unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(0);
			WeightsDouble<Integer> w = graph1.addVerticesWeights("w1", double.class);
			w.set(0, 10.1);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(true, false)
					.generate();
			WeightsDouble<Integer> unionWeights = union.verticesWeights("w1");
			assertEquals(10.1, unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(0);
			WeightsBool<Integer> w = graph1.addVerticesWeights("w1", boolean.class);
			w.set(0, true);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(true, false)
					.generate();
			WeightsBool<Integer> unionWeights = union.verticesWeights("w1");
			assertEquals(true, unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(0);
			WeightsChar<Integer> w = graph1.addVerticesWeights("w1", char.class);
			w.set(0, 'a');

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(true, false)
					.generate();
			WeightsChar<Integer> unionWeights = union.verticesWeights("w1");
			assertEquals('a', unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(0);
			WeightsObj<Integer, String> w = graph1.addVerticesWeights("w1", Object.class);
			w.set(0, "10");

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(true, false)
					.generate();
			WeightsObj<Integer, String> unionWeights = union.verticesWeights("w1");
			assertEquals("10", unionWeights.get(0));
		});
	}

	@Test
	public void copyEdgesWeights() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVerticesWeights("vertex-weights", int.class);
			WeightsInt<Integer> weights11 = graph1.addEdgesWeights("w1", int.class, -1);
			WeightsInt<Integer> weights12 = graph1.addEdgesWeights("w2", int.class, -2);
			graph1.addVertex(1000);
			graph1.addVertex(1001);
			graph1.addVertex(1002);
			graph1.addEdge(1000, 1001, 0);
			graph1.addEdge(1001, 1002, 1);
			weights11.set(0, 10);
			weights12.set(1, 20);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addVerticesWeights("vertex-weights", int.class);
			WeightsInt<Integer> weights21 = graph2.addEdgesWeights("w1", int.class, -10);
			WeightsInt<Integer> weights23 = graph2.addEdgesWeights("w3", int.class, -30);
			graph2.addVertex(1000);
			graph2.addVertex(1001);
			graph2.addVertex(1002);
			graph2.addEdge(1000, 1001, 0);
			graph2.addEdge(1002, 1000, 2);
			weights21.set(0, 100);
			weights23.set(2, 300);

			Graph<Integer, Integer> unionNoCopy = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.copyWeights(false, false)
					.generate();
			assertEquals(Set.of(), unionNoCopy.verticesWeightsKeys());
			assertEquals(Set.of(), unionNoCopy.edgesWeightsKeys());

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.copyWeights(false, true)
					.generate();
			assertEquals(Set.of(), union.verticesWeightsKeys());
			assertEquals(Set.of("w1", "w2", "w3"), union.edgesWeightsKeys());

			WeightsInt<Integer> unionWeights1 = union.edgesWeights("w1");
			WeightsInt<Integer> unionWeights2 = union.edgesWeights("w2");
			WeightsInt<Integer> unionWeights3 = union.edgesWeights("w3");
			assertEquals(-1, unionWeights1.defaultWeight());
			assertEquals(-2, unionWeights2.defaultWeight());
			assertEquals(-30, unionWeights3.defaultWeight());
			assertEquals(10, unionWeights1.get(0));
			assertEquals(-1, unionWeights1.get(1));
			assertEquals(-10, unionWeights1.get(2));
			assertEquals(-2, unionWeights2.get(0));
			assertEquals(20, unionWeights2.get(1));
			assertEquals(-2, unionWeights2.get(2));
			assertEquals(-30, unionWeights3.get(0));
			assertEquals(-30, unionWeights3.get(1));
			assertEquals(300, unionWeights3.get(2));

			assertEquals(Set.of("w1", "w3"),
					new UnionGraphGenerator<>(IntGraphFactory.undirected())
							.graphs(graph1, graph2)
							.copyWeights(false, true)
							.edgesWeightsKeys(Set.of("w1", "w3"))
							.generate()
							.edgesWeightsKeys());
			assertEquals(Set.of("w1", "w2", "w3"),
					new UnionGraphGenerator<>(IntGraphFactory.undirected())
							.graphs(graph1, graph2)
							.copyWeights(false, true)
							.edgesWeightsKeys(Set.of("w1", "w3"))
							.edgesWeightsKeys(null)
							.generate()
							.edgesWeightsKeys());
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVerticesWeights("vertex-weights", int.class);
			WeightsInt<Integer> weights11 = graph1.addEdgesWeights("w1", int.class, -1);
			WeightsInt<Integer> weights12 = graph1.addEdgesWeights("w2", int.class, -2);
			graph1.addVertex(1000);
			graph1.addVertex(1001);
			graph1.addVertex(1002);
			graph1.addEdge(1000, 1001, 0);
			graph1.addEdge(1001, 1002, 1);
			weights11.set(0, 10);
			weights12.set(1, 20);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addVerticesWeights("vertex-weights", int.class);
			WeightsInt<Integer> weights21 = graph2.addEdgesWeights("w1", int.class, -10);
			WeightsInt<Integer> weights23 = graph2.addEdgesWeights("w3", int.class, -30);
			graph2.addVertex(1000);
			graph2.addVertex(1001);
			graph2.addVertex(1002);
			graph2.addEdge(1000, 1001, 11);
			graph2.addEdge(1002, 1000, 2);
			weights21.set(11, 100);
			weights23.set(2, 300);
			weights23.set(11, 500);

			Graph<Integer, Integer> unionNoCopy = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.edgeUnionByEndpoints()
					.copyWeights(false, false)
					.generate();
			assertEquals(Set.of(), unionNoCopy.verticesWeightsKeys());
			assertEquals(Set.of(), unionNoCopy.edgesWeightsKeys());

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.edgeUnionByEndpoints()
					.copyWeights(false, true)
					.generate();
			assertEquals(Set.of(), union.verticesWeightsKeys());
			assertEquals(Set.of("w1", "w2", "w3"), union.edgesWeightsKeys());

			WeightsInt<Integer> unionWeights1 = union.edgesWeights("w1");
			WeightsInt<Integer> unionWeights2 = union.edgesWeights("w2");
			WeightsInt<Integer> unionWeights3 = union.edgesWeights("w3");
			assertEquals(-1, unionWeights1.defaultWeight());
			assertEquals(-2, unionWeights2.defaultWeight());
			assertEquals(-30, unionWeights3.defaultWeight());
			assertEquals(10, unionWeights1.get(0));
			assertEquals(-1, unionWeights1.get(1));
			assertEquals(-10, unionWeights1.get(2));
			assertEquals(-2, unionWeights2.get(0));
			assertEquals(20, unionWeights2.get(1));
			assertEquals(-2, unionWeights2.get(2));
			assertEquals(500, unionWeights3.get(0));
			assertEquals(-30, unionWeights3.get(1));
			assertEquals(300, unionWeights3.get(2));

			assertEquals(Set.of("w1", "w3"),
					new UnionGraphGenerator<>(IntGraphFactory.undirected())
							.graphs(graph1, graph2)
							.edgeUnionByEndpoints()
							.copyWeights(false, true)
							.edgesWeightsKeys(Set.of("w1", "w3"))
							.generate()
							.edgesWeightsKeys());
			assertEquals(Set.of("w1", "w2", "w3"),
					new UnionGraphGenerator<>(IntGraphFactory.undirected())
							.graphs(graph1, graph2)
							.edgeUnionByEndpoints()
							.copyWeights(false, true)
							.edgesWeightsKeys(Set.of("w1", "w3"))
							.edgesWeightsKeys(null)
							.generate()
							.edgesWeightsKeys());
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addEdgesWeights("w1", int.class);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addEdgesWeights("w1", double.class);

			UnionGraphGenerator<Integer, Integer> gen = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.copyWeights(false, true);
			assertThrows(IllegalArgumentException.class, () -> gen.generate());
		});

		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(1000);
			graph1.addVertex(1001);
			graph1.addEdge(1000, 1001, 0);
			WeightsByte<Integer> w = graph1.addEdgesWeights("w1", byte.class);
			w.set(0, (byte) 10);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(false, true)
					.generate();
			WeightsByte<Integer> unionWeights = union.edgesWeights("w1");
			assertEquals(10, unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(1000);
			graph1.addVertex(1001);
			graph1.addEdge(1000, 1001, 0);
			WeightsShort<Integer> w = graph1.addEdgesWeights("w1", short.class);
			w.set(0, (short) 10);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(false, true)
					.generate();
			WeightsShort<Integer> unionWeights = union.edgesWeights("w1");
			assertEquals(10, unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(1000);
			graph1.addVertex(1001);
			graph1.addEdge(1000, 1001, 0);
			WeightsInt<Integer> w = graph1.addEdgesWeights("w1", int.class);
			w.set(0, 10);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(false, true)
					.generate();
			WeightsInt<Integer> unionWeights = union.edgesWeights("w1");
			assertEquals(10, unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(1000);
			graph1.addVertex(1001);
			graph1.addEdge(1000, 1001, 0);
			WeightsLong<Integer> w = graph1.addEdgesWeights("w1", long.class);
			w.set(0, 10);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(false, true)
					.generate();
			WeightsLong<Integer> unionWeights = union.edgesWeights("w1");
			assertEquals(10, unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(1000);
			graph1.addVertex(1001);
			graph1.addEdge(1000, 1001, 0);
			WeightsFloat<Integer> w = graph1.addEdgesWeights("w1", float.class);
			w.set(0, 10.1f);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(false, true)
					.generate();
			WeightsFloat<Integer> unionWeights = union.edgesWeights("w1");
			assertEquals(10.1f, unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(1000);
			graph1.addVertex(1001);
			graph1.addEdge(1000, 1001, 0);
			WeightsDouble<Integer> w = graph1.addEdgesWeights("w1", double.class);
			w.set(0, 10.1);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(false, true)
					.generate();
			WeightsDouble<Integer> unionWeights = union.edgesWeights("w1");
			assertEquals(10.1, unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(1000);
			graph1.addVertex(1001);
			graph1.addEdge(1000, 1001, 0);
			WeightsBool<Integer> w = graph1.addEdgesWeights("w1", boolean.class);
			w.set(0, true);

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(false, true)
					.generate();
			WeightsBool<Integer> unionWeights = union.edgesWeights("w1");
			assertEquals(true, unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(1000);
			graph1.addVertex(1001);
			graph1.addEdge(1000, 1001, 0);
			WeightsChar<Integer> w = graph1.addEdgesWeights("w1", char.class);
			w.set(0, 'a');

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(false, true)
					.generate();
			WeightsChar<Integer> unionWeights = union.edgesWeights("w1");
			assertEquals('a', unionWeights.get(0));
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertex(1000);
			graph1.addVertex(1001);
			graph1.addEdge(1000, 1001, 0);
			WeightsObj<Integer, String> w = graph1.addEdgesWeights("w1", Object.class);
			w.set(0, "10");

			Graph<Integer, Integer> union = new UnionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, IntGraphFactory.newInstance(directed).newGraph())
					.copyWeights(false, true)
					.generate();
			WeightsObj<Integer, String> unionWeights = union.edgesWeights("w1");
			assertEquals("10", unionWeights.get(0));
		});
	}

}
