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
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntList;

@SuppressWarnings("boxing")
public class IntersectionGraphGeneratorTest extends TestBase {

	@Test
	public void graphFactory() {
		GraphFactory<Integer, Integer> factory = GraphFactory.undirected();
		IntersectionGraphGenerator<Integer, Integer> gen = new IntersectionGraphGenerator<>(factory);
		assertTrue(factory == gen.graphFactory());
	}

	@Test
	public void missingGraphs() {
		IntersectionGraphGenerator<Integer, Integer> gen =
				new IntersectionGraphGenerator<>(IntGraphFactory.undirected());
		assertThrows(IllegalStateException.class, () -> gen.generate());
	}

	@Test
	public void onlyOneGraph() {
		IntersectionGraphGenerator<Integer, Integer> gen = new IntersectionGraphGenerator<>();
		assertThrows(IllegalArgumentException.class, () -> gen.graphs(IntGraph.newDirected()));
	}

	@Test
	public void intersectByEndpointsParallelEdges() {
		Graph<Integer, Integer> g1 = Graph.newDirected();
		g1.addVertices(range(2));
		g1.addEdge(0, 1, 5);
		g1.addEdge(0, 1, 6);
		IntersectionGraphGenerator<Integer, Integer> gen = new IntersectionGraphGenerator<>(IntGraphFactory.directed())
				.graphs(g1, IntGraph.newDirected())
				.edgeIntersectByEndpoints();
		assertThrows(IllegalArgumentException.class, () -> gen.generate());
	}

	@Test
	public void graphsWithWrongDirection() {
		IntersectionGraphGenerator<Integer, Integer> gen = new IntersectionGraphGenerator<>();
		assertThrows(IllegalArgumentException.class,
				() -> gen.graphs(IntGraph.newDirected(), IntGraph.newUndirected()));
	}

	@Test
	public void directed() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> union = new IntersectionGraphGenerator<Integer, Integer>()
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
		Graph<Integer, Integer> complement =
				new IntersectionGraphGenerator<Integer, Integer>().graphs(graph1, graph2).generate();
		assertEquals(range(5, 10), complement.vertices());
	}

	@Test
	public void intersectById() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertices(IntList.of(0, 1, 2, 11));
			graph1.addEdge(0, 11, 79);
			graph1.addEdge(0, 1, 10);
			graph1.addEdge(1, 2, 20);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addVertices(IntList.of(0, 1, 2, 8));
			graph2.addEdge(0, 8, 301);
			graph2.addEdge(1, 2, 20);

			Graph<Integer, Integer> intersection = new IntersectionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.edgeIntersectById()
					.generate();

			Graph<Integer, Integer> expected = IntGraphFactory.newInstance(directed).newGraph();
			expected.addVertices(range(3));
			expected.addEdge(1, 2, 20);
			assertEquals(expected, intersection);
		});
	}

	@Test
	public void intersectByIdDuplicateIdDifferentEndpoints() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertices(range(3));
			graph1.addEdge(0, 1, 10);
			graph1.addEdge(1, 2, 20);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addVertices(range(5));
			graph2.addEdge(0, 2, 10);

			IntersectionGraphGenerator<Integer, Integer> gen =
					new IntersectionGraphGenerator<>(IntGraphFactory.undirected())
							.graphs(graph1, graph2)
							.edgeIntersectById();

			assertThrows(IllegalArgumentException.class, () -> gen.generate());
		});
	}

	@Test
	public void graphsWithSelfEdges() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).allowSelfEdges().newGraph();
			graph1.addVertices(range(3));
			graph1.addEdge(0, 0, 30);
			graph1.addEdge(1, 2, 20);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).allowSelfEdges().newGraph();
			graph2.addVertices(range(5));
			graph2.addEdge(0, 0, 30);

			Graph<Integer, Integer> intersection =
					new IntersectionGraphGenerator<>(IntGraphFactory.undirected()).graphs(graph1, graph2).generate();

			Graph<Integer, Integer> expected = IntGraphFactory.newInstance(directed).allowSelfEdges().newGraph();
			expected.addVertices(range(3));
			expected.addEdge(0, 0, 30);
			assertEquals(expected, intersection);
		});
	}

	@Test
	public void graphsWithParallelEdges() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).allowParallelEdges().newGraph();
			graph1.addVertices(range(3));
			graph1.addEdge(0, 1, 20);
			graph1.addEdge(0, 1, 30);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).allowParallelEdges().newGraph();
			graph2.addVertices(range(5));
			graph2.addEdge(0, 1, 20);
			graph2.addEdge(0, 1, 30);

			Graph<Integer, Integer> intersection =
					new IntersectionGraphGenerator<>(IntGraphFactory.undirected()).graphs(graph1, graph2).generate();

			Graph<Integer, Integer> expected = IntGraphFactory.newInstance(directed).allowSelfEdges().newGraph();
			expected.addVertices(range(3));
			expected.addEdge(0, 1, 20);
			expected.addEdge(0, 1, 30);
			assertEquals(expected, intersection);
		});
	}

	@Test
	public void intersectByEndpoints() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertices(IntList.of(0, 1, 2, 11));
			graph1.addEdge(0, 1, 10);
			graph1.addEdge(1, 2, 20);
			graph1.addEdge(0, 11, 79);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addVertices(IntList.of(0, 1, 2, 8));
			graph2.addEdge(1, 2, 300);
			graph2.addEdge(0, 8, 301);

			Graph<Integer, Integer> intersection = new IntersectionGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.edgeIntersectByEndpoints()
					.generate();

			Graph<Integer, Integer> expected = IntGraphFactory.newInstance(directed).newGraph();
			expected.addVertices(range(3));
			expected.addEdge(1, 2, 20);
			assertEquals(expected, intersection);
		});
	}

}
