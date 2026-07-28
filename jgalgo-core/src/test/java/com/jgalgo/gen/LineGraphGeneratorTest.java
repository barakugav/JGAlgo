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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsObj;
import com.jgalgo.internal.util.SubSets;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntList;

public class LineGraphGeneratorTest extends TestBase {

	@Test
	public void directedUndirected() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> origGraph = new GnmGraphGenerator<>(IntGraphFactory.undirected())
					.vertices(10)
					.edges(20)
					.directed(directed)
					.selfEdges(true)
					.parallelEdges(true)
					.seed(0x7e6b7806df11bdebL)
					.generate();
			Graph<Integer, Integer> lineGraph = new LineGraphGenerator<>(IntGraphFactory.undirected())
					.graph(origGraph)
					.edges(IdBuilderInt.defaultBuilder())
					.generate();
			assertEqualsBool(directed, lineGraph.isDirected());
		});
	}

	@Test
	public void vertices() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> origGraph = new GnmGraphGenerator<>(IntGraphFactory.undirected())
					.vertices(10)
					.edges(20)
					.directed(directed)
					.selfEdges(true)
					.parallelEdges(true)
					.seed(0xcd9c012a451540ecL)
					.generate();
			Graph<Integer, Integer> lineGraph = new LineGraphGenerator<>(IntGraphFactory.undirected())
					.graph(origGraph)
					.edges(IdBuilderInt.defaultBuilder())
					.generate();

			assertEquals(origGraph.edges(), lineGraph.vertices());
		});
	}

	@Test
	public void edges() {
		foreachBoolConfig(directed -> {
			for (int edgesNum : IntList.of(20, 100, 300, 500)) {
				Graph<Integer, Integer> origGraph = new GnmGraphGenerator<>(IntGraphFactory.undirected())
						.vertices(10)
						.edges(edgesNum)
						.directed(directed)
						.selfEdges(true)
						.parallelEdges(true)
						.seed(0xe488b00e3be8baffL)
						.generate();
				Graph<Integer, Integer> lineGraph = new LineGraphGenerator<>(IntGraphFactory.undirected())
						.graph(origGraph)
						.edges(IdBuilderInt.defaultBuilder())
						.generate();

				List<Collection<Integer>> actualEdges0 = lineGraph.edges().stream().map(e -> {
					Integer e1 = lineGraph.edgeSource(e), e2 = lineGraph.edgeTarget(e);
					if (directed) {
						return List.of(e1, e2);
					} else {
						return Set.of(e1, e2);
					}
				}).collect(toList());
				Set<Collection<Integer>> actualEdges = new HashSet<>(actualEdges0);
				assertEquals(actualEdges.size(), actualEdges0.size());

				Set<Collection<Integer>> expectedEdges = expectedEdgesInLineGraph(origGraph);
				assertEquals(expectedEdges, actualEdges);
			}
		});
	}

	@Test
	public void generateWithoutInputGraph() {
		assertThrows(IllegalStateException.class,
				() -> new LineGraphGenerator<>(IntGraphFactory.undirected()).generate());
	}

	@Test
	public void generateWithoutEdgeBuilder() {
		Graph<Integer, Integer> origGraph = IntGraph.newDirected();
		assertThrows(IllegalStateException.class,
				() -> new LineGraphGenerator<Integer, Integer>().graph(origGraph).generate());
	}

	@Test
	public void graphFactory() {
		GraphFactory<Integer, Integer> factory = IntGraphFactory.undirected();
		LineGraphGenerator<Integer, Integer> generator = new LineGraphGenerator<>(factory);
		assertTrue(factory == generator.graphFactory());
	}

	@Test
	public void commonVertexWeights() {
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> origGraph = new GnmGraphGenerator<>(IntGraphFactory.undirected())
					.vertices(10)
					.edges(20)
					.directed(directed)
					.selfEdges(true)
					.parallelEdges(true)
					.seed(0xe93aa213c6bbe95cL)
					.generate();
			LineGraphGenerator<Integer, Integer> gen = new LineGraphGenerator<>(IntGraphFactory.undirected())
					.graph(origGraph)
					.edges(IdBuilderInt.defaultBuilder());

			assertEquals(Set.of(), gen.generate().edgesWeightsKeys());
			assertEquals(Set.of("key-name"), gen.commonVertexWeights("key-name").generate().edgesWeightsKeys());
			assertEquals(Set.of(), gen.commonVertexWeights(null).generate().edgesWeightsKeys());
		});
		foreachBoolConfig((directed, intGraph) -> {
			for (int edgesNum : IntList.of(20, 100, 300)) {
				Graph<Integer, Integer> origGraph =
						new GnmGraphGenerator<>(intGraph ? IntGraphFactory.undirected() : GraphFactory.undirected())
								.vertices(10, IdBuilderInt.defaultBuilder())
								.edges(edgesNum, IdBuilderInt.defaultBuilder())
								.directed(directed)
								.selfEdges(true)
								.parallelEdges(true)
								.seed(0xad664c9980072132L)
								.generate();
				Graph<Integer, Integer> lineGraph = new LineGraphGenerator<>(IntGraphFactory.undirected())
						.graph(origGraph)
						.edges(IdBuilderInt.defaultBuilder())
						.commonVertexWeights("common-vertex")
						.generate();

				assertEquals(Set.of("common-vertex"), lineGraph.edgesWeightsKeys());
				Weights<Integer, Integer> commonVertexWeights = lineGraph.edgesWeights("common-vertex");
				if (intGraph) {
					assertTrue(commonVertexWeights instanceof WeightsInt);
				} else {
					assertTrue(commonVertexWeights instanceof WeightsObj);
				}

				for (Integer lineEdge : lineGraph.edges()) {
					Integer e1 = lineGraph.edgeSource(lineEdge);
					Integer e2 = lineGraph.edgeTarget(lineEdge);
					Integer commonVertex = commonVertexWeights.getAsObj(lineEdge);
					assertNotNull(commonVertex);
					if (directed) {
						assertEquals(commonVertex, origGraph.edgeTarget(e1));
						assertEquals(commonVertex, origGraph.edgeSource(e2));
					} else {
						Integer u1 = origGraph.edgeSource(e1), v1 = origGraph.edgeTarget(e1);
						Integer u2 = origGraph.edgeSource(e2), v2 = origGraph.edgeTarget(e2);
						assertTrue(commonVertex.equals(u1) || commonVertex.equals(v1));
						assertTrue(commonVertex.equals(u2) || commonVertex.equals(v2));
					}
				}
			}
		});
	}

	private static Set<Collection<Integer>> expectedEdgesInLineGraph(Graph<Integer, Integer> origGraph) {
		return SubSets.stream(origGraph.edges(), 2).flatMap(es -> {
			Integer e1 = es.get(0), e2 = es.get(1);
			if (origGraph.isDirected()) {
				return Stream.of(Pair.of(e1, e2), Pair.of(e2, e1));
			} else {
				return Stream.of(Pair.of(e1, e2));
			}
		}).map(es -> {
			Integer e1 = es.first(), e2 = es.second();
			int u1 = origGraph.edgeSource(e1).intValue(), v1 = origGraph.edgeTarget(e1).intValue();
			int u2 = origGraph.edgeSource(e2).intValue(), v2 = origGraph.edgeTarget(e2).intValue();
			if (origGraph.isDirected()) {
				if (v1 == u2)
					return List.of(e1, e2);
			} else {
				if (u1 == u2 || v1 == v2 || u1 == v2 || v1 == u2)
					return Set.of(e1, e2);
			}
			return null;
		}).filter(Objects::nonNull).collect(toSet());
	}

}
