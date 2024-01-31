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

@SuppressWarnings("boxing")
public class DifferenceGraphGeneratorTest extends TestBase {

	@Test
	public void graphFactory() {
		GraphFactory<Integer, Integer> factory = GraphFactory.undirected();
		DifferenceGraphGenerator<Integer, Integer> gen = new DifferenceGraphGenerator<>(factory);
		assertTrue(factory == gen.graphFactory());
	}

	@Test
	public void missingGraphs() {
		DifferenceGraphGenerator<Integer, Integer> gen = new DifferenceGraphGenerator<>(IntGraphFactory.undirected());
		assertThrows(IllegalStateException.class, () -> gen.generate());
	}

	@Test
	public void graphsWithWrongDirection() {
		DifferenceGraphGenerator<Integer, Integer> gen = new DifferenceGraphGenerator<>();
		assertThrows(IllegalArgumentException.class,
				() -> gen.graphs(IntGraph.newDirected(), IntGraph.newUndirected()));
	}

	@Test
	public void directed() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> difference = new DifferenceGraphGenerator<Integer, Integer>()
					.graphs(directed ? IntGraph.newDirected() : IntGraph.newUndirected(),
							directed ? IntGraph.newDirected() : IntGraph.newUndirected())
					.generate();
			assertEqualsBool(directed, difference.isDirected());
		});
	}

	@Test
	public void verticesN() {
		Graph<Integer, Integer> graph1 = IntGraph.newDirected();
		graph1.addVertices(range(10));
		Graph<Integer, Integer> graph2 = IntGraph.newDirected();
		graph2.addVertices(range(10));
		Graph<Integer, Integer> difference =
				new DifferenceGraphGenerator<Integer, Integer>().graphs(graph1, graph2).generate();
		assertEquals(range(10), difference.vertices());
	}

	@Test
	public void verticesNotSame() {
		Graph<Integer, Integer> graph1 = IntGraph.newDirected();
		graph1.addVertices(range(0, 10));
		Graph<Integer, Integer> graph2 = IntGraph.newDirected();
		graph2.addVertices(range(5, 15));
		DifferenceGraphGenerator<Integer, Integer> gen = new DifferenceGraphGenerator<>();
		assertThrows(IllegalArgumentException.class, () -> gen.graphs(graph1, graph2));
	}

	@Test
	public void differenceById() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertices(range(3));
			graph1.addEdge(0, 1, 10);
			graph1.addEdge(1, 2, 20);
			graph1.addEdge(2, 1, 100);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addVertices(range(3));
			graph2.addEdge(1, 2, 20);
			if (directed) {
				graph2.addEdge(2, 1, 100);
			} else {
				graph2.addEdge(1, 2, 100);
			}

			Graph<Integer, Integer> difference = new DifferenceGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.edgeDifferenceById()
					.generate();

			Graph<Integer, Integer> expected = IntGraphFactory.newInstance(directed).newGraph();
			expected.addVertices(range(3));
			expected.addEdge(0, 1, 10);
			assertEquals(expected, difference);
		});
		for (int variant : range(4)) {
			foreachBoolConfig(directed -> {
				Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
				graph1.addVertices(range(4));
				graph1.addEdge(0, 1, 10);
				graph1.addEdge(1, 2, 20);

				Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
				graph2.addVertices(range(4));
				if (variant == 0) {
					/* different source */
					graph2.addEdge(0, 2, 20);
				} else if (variant == 1) {
					/* different target */
					graph2.addEdge(1, 0, 20);
				} else if (variant == 2) {
					/* different source, target=orig_source */
					graph2.addEdge(3, 1, 20);
				} else {
					assert variant == 3;
					/* different source and target */
					graph2.addEdge(0, 3, 20);
				}

				DifferenceGraphGenerator<Integer, Integer> gen =
						new DifferenceGraphGenerator<>(IntGraphFactory.undirected())
								.graphs(graph1, graph2)
								.edgeDifferenceById();
				/* edge exist in both graphs with same id but difference endpoints */
				assertThrows(IllegalArgumentException.class, () -> gen.generate());
			});
		}
	}

	@Test
	public void differenceByEndpoints() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).newGraph();
			graph1.addVertices(range(3));
			graph1.addEdge(0, 1, 10);
			graph1.addEdge(1, 2, 20);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addVertices(range(3));
			graph2.addEdge(1, 2, 30);

			Graph<Integer, Integer> difference = new DifferenceGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.edgeDifferenceByEndpoints()
					.generate();

			Graph<Integer, Integer> expected = IntGraphFactory.newInstance(directed).newGraph();
			expected.addVertices(range(3));
			expected.addEdge(0, 1, 10);
			assertEquals(expected, difference);
		});
	}

	@Test
	public void selfEdges() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).allowSelfEdges().newGraph();
			graph1.addVertices(range(3));
			graph1.addEdge(0, 0, 10);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addVertices(range(3));

			Graph<Integer, Integer> difference = new DifferenceGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.edgeDifferenceByEndpoints()
					.generateMutable();

			assertTrue(difference.isAllowSelfEdges());
		});
	}

	@Test
	public void parallelEdges() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> graph1 = IntGraphFactory.newInstance(directed).allowParallelEdges().newGraph();
			graph1.addVertices(range(3));
			graph1.addEdge(0, 1, 10);
			graph1.addEdge(0, 1, 20);

			Graph<Integer, Integer> graph2 = IntGraphFactory.newInstance(directed).newGraph();
			graph2.addVertices(range(3));

			Graph<Integer, Integer> difference = new DifferenceGraphGenerator<>(IntGraphFactory.undirected())
					.graphs(graph1, graph2)
					.edgeDifferenceByEndpoints()
					.generateMutable();

			assertTrue(difference.isAllowParallelEdges());
		});
	}

}
