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
import java.util.Arrays;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;

/**
 * Generate a graph that contains the edges that exist in one graph but not in the other.
 *
 * <p>
 * Given two graphs with the same vertex set \(G_1=(V,E_1)\) and \(G_2=(V,E_2)\), the difference graph \(G=(V,E)\) is a
 * graph with the same vertex set that contains every edge that exists in \(E_1\) but not in \(E_2\). Saying one edge
 * from \(E_1\) is the same edge as another from \(E_2\) can be defined by the edge id or by the edge endpoints. See
 * {@link #edgeDifferenceById()} and {@link #edgeDifferenceByEndpoints()}.
 *
 * <p>
 * By default, the edges are compared by their id. Use {@link #edgeDifferenceByEndpoints()} to compare edges by their
 * endpoints.
 *
 * <p>
 * Weights are not copied from the input graphs to the generated graph(s). In the future there might be an option to do
 * so.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class DifferenceGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final GraphFactory<V, E> factory;
	private Graph<V, E> graph1, graph2;
	private boolean differenceById = true;

	/**
	 * Create a new difference graph generator that will use the default graph factory.
	 */
	public DifferenceGraphGenerator() {
		this(GraphFactory.undirected());
	}

	/**
	 * Create a new difference graph generator that will use the given graph factory.
	 *
	 * <p>
	 * The graph factory will be used to create the generated graph(s).
	 *
	 * <p>
	 * To generate {@linkplain IntGraph int graphs}, pass an instance of {@linkplain IntGraphFactory} to this
	 * constructor.
	 *
	 * @param factory the graph factory that will be used to create the generated graph(s)
	 */
	public DifferenceGraphGenerator(GraphFactory<V, E> factory) {
		this.factory = Objects.requireNonNull(factory);
	}

	/**
	 * Get the graph factory that will be used to create the generated graph(s).
	 *
	 * <p>
	 * It's possible to customize the factory before generating the graph(s), for example by using
	 * {@link GraphFactory#addHint(GraphFactory.Hint)} to optimize the generated graph(s) for a specific algorithm.
	 *
	 * <p>
	 * During the graph(s) generation, the method {@link GraphFactory#setDirected(boolean)} of the given factory will be
	 * called to align the created graph with the generator configuration. If the first graph has self edges, the method
	 * {@link GraphFactory#allowSelfEdges()} will be called. If the first graph has parallel edges, the method
	 * {@link GraphFactory#allowParallelEdges()} will be called.
	 *
	 * @return the graph factory that will be used to create the generated graph(s)
	 */
	public GraphFactory<V, E> graphFactory() {
		return factory;
	}

	/**
	 * Set the input graphs whose difference graph will be generated.
	 *
	 * <p>
	 * Given two graphs with the same vertex set, the difference graph is a graph with the same vertex set that contains
	 * every edge that exists in the first graph but not in the second graph. Saying one edge from the first graph is
	 * the same edge as another from the second graph can be defined by the edge id or by the edge endpoints. See
	 * {@link #edgeDifferenceById()} and {@link #edgeDifferenceByEndpoints()}.
	 *
	 * @param  graph1                   the first graph
	 * @param  graph2                   the second graph
	 * @return                          this generator
	 * @throws IllegalArgumentException if the graphs have different directionality or if their vertex sets are not
	 *                                      equal
	 */
	public DifferenceGraphGenerator<V, E> graphs(Graph<V, E> graph1, Graph<V, E> graph2) {
		if (graph1.isDirected() != graph2.isDirected())
			throw new IllegalArgumentException("Input graphs must have the same directionality");
		if (!graph1.vertices().equals(graph2.vertices()))
			throw new IllegalArgumentException("Input graphs must have the same set of vertices");
		this.graph1 = graph1;
		this.graph2 = graph2;
		return this;
	}

	/**
	 * Set the difference of edges to be by their id.
	 *
	 * <p>
	 * Given two graphs with the same vertex set, the difference graph is a graph with the same vertex set that contains
	 * every edge that exists in the first graph but not in the second graph. This method defines that one edge from the
	 * first graph is the same edge as another from the second graph if they have the same identifier. See
	 * {@link #edgeDifferenceByEndpoints()} for an alternative definition.
	 *
	 * <p>
	 * By default, the edges are compared by their id.
	 *
	 * <p>
	 * When difference of edges is done by their id, if an edge from the first graph and an edge from the second graph
	 * have the same identifier, they also must have the same endpoints, otherwise an exception will be thrown during
	 * generation.
	 *
	 * <p>
	 * In the following example two directed graphs are created, and the difference graph of the two graphs is generated
	 * by the edges identifiers:
	 *
	 * <pre> {@code
	 * Graph<String, Integer> graph1 = Graph.newDirected();
	 * graph1.addVertices(Set.of("A", "B", "C", "D"));
	 * graph1.addEdge("A", "B", 1);
	 * graph1.addEdge("A", "C", 2);
	 *
	 * Graph<String, Integer> graph2 = Graph.newDirected();
	 * graph2.addVertices(Set.of("A", "B", "C", "D"));
	 * graph2.addEdge("A", "B", 10);
	 * graph2.addEdge("A", "C", 2);
	 *
	 * Graph<String, Integer> difference = new DifferenceGraphGenerator<String, Integer>()
	 * 		.graphs(graph1, graph2)
	 * 		.edgeDifferenceById() // default, not really needed
	 * 		.generate();
	 * assert Set.of("A", "B", "C", "D").equals(difference.vertices());
	 * assert Set.of(1).equals(difference.edges());
	 * assert difference.edgeSource(1).equals("A") && difference.edgeTarget(1).equals("B");
	 * }</pre>
	 *
	 * @return this generator
	 */
	public DifferenceGraphGenerator<V, E> edgeDifferenceById() {
		differenceById = true;
		return this;
	}

	/**
	 * Set the difference of edges to be by their endpoints.
	 *
	 * <p>
	 * Given two graphs with the same vertex set, the difference graph is a graph with the same vertex set that contains
	 * every edge that exists in the first graph but not in the second graph. This method defines that one edge from the
	 * first graph is the same edge as another from the second graph if they have the same endpoints. See
	 * {@link #edgeDifferenceById()} for an alternative definition.
	 *
	 * <p>
	 * By default, the edges are compared by their id.
	 *
	 * <p>
	 * In the following example two directed graphs are created, and the difference graph of the two graphs is generated
	 * by the edges endpoints:
	 *
	 * <pre> {@code
	 * Graph<String, Integer> graph1 = Graph.newDirected();
	 * graph1.addVertices(Set.of("A", "B", "C", "D"));
	 * graph1.addEdge("A", "B", 1);
	 * graph1.addEdge("A", "C", 2);
	 *
	 * Graph<String, Integer> graph2 = Graph.newDirected();
	 * graph2.addVertices(Set.of("A", "B", "C", "D"));
	 * graph2.addEdge("A", "B", 10);
	 * graph2.addEdge("A", "D", 20);
	 *
	 * Graph<String, Integer> difference = new DifferenceGraphGenerator<String, Integer>()
	 * 		.graphs(graph1, graph2)
	 * 		.edgeDifferenceByEndpoints()
	 * 		.generate();
	 * assert Set.of("A", "B", "C", "D").equals(difference.vertices());
	 * assert Set.of(2).equals(difference.edges());
	 * assert difference.edgeSource(2).equals("A") && difference.edgeTarget(2).equals("C");
	 * }</pre>
	 *
	 * @return this generator
	 */
	public DifferenceGraphGenerator<V, E> edgeDifferenceByEndpoints() {
		differenceById = false;
		return this;
	}

	@Override
	public GraphBuilder<V, E> generateIntoBuilder() {
		if (graph1 == null)
			throw new IllegalStateException("Input graphs not provided");

		final boolean directed = graph1.isDirected();
		factory.setDirected(directed);
		if (!Graphs.selfEdges(graph1).isEmpty())
			factory.allowSelfEdges();
		if (Graphs.containsParallelEdges(graph1))
			factory.allowParallelEdges();
		GraphBuilder<V, E> difference = factory.newBuilder();

		assert graph1.vertices().equals(graph2.vertices());
		difference.addVertices(graph1.vertices());

		IndexGraph g1 = graph1.indexGraph();
		IndexGraph g2 = graph2.indexGraph();
		IndexIdMap<V> viMap1 = graph1.indexGraphVerticesMap();
		IndexIdMap<V> viMap2 = graph2.indexGraphVerticesMap();
		IndexIdMap<E> eiMap1 = graph1.indexGraphEdgesMap();
		IndexIdMap<E> eiMap2 = graph2.indexGraphEdgesMap();

		if (differenceById) {
			for (int eIdx1 : range(graph1.edges().size())) {
				E e = eiMap1.indexToId(eIdx1);
				V u1 = viMap1.indexToId(g1.edgeSource(eIdx1));
				V v1 = viMap1.indexToId(g1.edgeTarget(eIdx1));

				int eIdx2 = eiMap2.idToIndexIfExist(e);
				if (eIdx2 >= 0) {
					V u2 = viMap2.indexToId(g2.edgeSource(eIdx2));
					V v2 = viMap2.indexToId(g2.edgeTarget(eIdx2));
					boolean sameEndpoints =
							(u1.equals(u2) && v1.equals(v2)) || (!directed && u1.equals(v2) && v1.equals(u2));
					if (!sameEndpoints)
						throw new IllegalArgumentException("Input graphs have the same edge with difference endpoints");
					continue;
				}
				difference.addEdge(u1, v1, e);
			}
		} else {
			final int n = g1.vertices().size();
			int[] visit = new int[n];
			Arrays.fill(visit, -1);

			for (int uIdx2 : range(n)) {
				final int visitIdx = uIdx2;
				for (IEdgeIter eit = g2.outEdges(uIdx2).iterator(); eit.hasNext();) {
					eit.nextInt();
					int vIdx2 = eit.targetInt();
					V v = viMap2.indexToId(vIdx2);
					int vIdx1 = viMap1.idToIndex(v);
					visit[vIdx1] = visitIdx;
				}
				V u = viMap2.indexToId(uIdx2);
				int uIdx1 = viMap1.idToIndex(u);
				for (IEdgeIter eit = g1.outEdges(uIdx1).iterator(); eit.hasNext();) {
					int eIdx = eit.nextInt();
					int vIdx1 = eit.targetInt();
					if (!directed && uIdx1 > vIdx1)
						continue; /* avoid adding undirected edges twice */
					if (visit[vIdx1] == visitIdx)
						continue; /* edge exist in g2 */
					V v = viMap1.indexToId(vIdx1);
					E e = eiMap1.indexToId(eIdx);
					difference.addEdge(u, v, e);
				}
			}
		}
		return difference;
	}

}
