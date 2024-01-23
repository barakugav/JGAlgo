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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.TestBase;

public class ComplementGraphGeneratorTest extends TestBase {

	@Test
	public void graphFactory() {
		GraphFactory<Integer, Integer> factory = GraphFactory.undirected();
		ComplementGraphGenerator<Integer, Integer> gen = new ComplementGraphGenerator<>(factory);
		assertTrue(factory == gen.graphFactory());
	}

	@Test
	public void missingGraph() {
		ComplementGraphGenerator<Integer, Integer> gen = new ComplementGraphGenerator<>(IntGraphFactory.undirected());
		assertThrows(IllegalStateException.class, () -> gen.generate());
	}

	@Test
	public void missingEdgeBuilder() {
		Graph<Integer, Integer> origGraph = createGraph(true, true);
		ComplementGraphGenerator<Integer, Integer> gen = new ComplementGraphGenerator<>();
		gen.graph(origGraph);
		assertThrows(IllegalStateException.class, () -> gen.generate());
	}

	@Test
	public void directed() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> origGraph = createGraph(directed, true);
			Graph<Integer, Integer> complement = new ComplementGraphGenerator<Integer, Integer>()
					.graph(origGraph)
					.edges(IdBuilderInt.defaultBuilder())
					.generate();
			assertEqualsBool(origGraph.isDirected(), complement.isDirected());
		});
	}

	@Test
	public void vertices() {
		Graph<Integer, Integer> origGraph = createGraph(true, true);
		Graph<Integer, Integer> complement = new ComplementGraphGenerator<Integer, Integer>()
				.graph(origGraph)
				.edges(IdBuilderInt.defaultBuilder())
				.generate();
		assertEquals(origGraph.vertices(), complement.vertices());
	}

	@Test
	public void generate() {
		foreachBoolConfig((directed, selfEdges) -> {
			Graph<Integer, Integer> origGraph = createGraph(directed, true);
			Graph<Integer, Integer> complement = new ComplementGraphGenerator<Integer, Integer>()
					.graph(origGraph)
					.edges(IdBuilderInt.defaultBuilder())
					.selfEdges(selfEdges)
					.generate();
			for (Integer u : origGraph.vertices()) {
				for (Integer v : origGraph.vertices()) {
					boolean origContains = origGraph.containsEdge(u, v);
					boolean expectedContains = !origContains && (selfEdges || !u.equals(v));
					assertEqualsBool(expectedContains, complement.containsEdge(u, v));
				}
			}
		});
	}

	private static Graph<Integer, Integer> createGraph(boolean directed, boolean selfEdges) {
		return new GnmGraphGenerator<Integer, Integer>()
				.directed(directed)
				.vertices(10, IdBuilderInt.defaultBuilder())
				.edges(20, IdBuilderInt.defaultBuilder())
				.selfEdges(selfEdges)
				.seed(0x116fbdf1fca7503L)
				.generate();
	}

}
