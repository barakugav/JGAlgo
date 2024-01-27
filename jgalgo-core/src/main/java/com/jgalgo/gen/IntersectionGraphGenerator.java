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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToIntFunction;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * Generate the intersection graph of two or more given graphs.
 *
 * <p>
 * The intersection graph of two graphs is a graph with the vertex set which is the intersection of the vertex sets of
 * the two graphs, and the edge set which is the intersection of the edge sets of the two graphs. There are two
 * different ways to intersect the edges of the graphs:
 * <ul>
 * <li>Intersect by id: the edges are considered the same if they have the same id. Two edges with the same id in the
 * two graphs are considered the same edge. If they do not have the same endpoints, an exception is thrown. This is the
 * default.</li>
 * <li>Intersect by endpoints: the edges are considered the same if they have the same endpoints. Two edges with the
 * same endpoints in the two graphs are considered the same edge. The id of the edge in the first graph is used as the
 * id of the edge in the intersection graph.</li>
 * </ul>
 * The above rules generalize to intersection of more than two graphs.
 *
 * <p>
 * By default, the edges intersection is by id. Use {@link #edgeIntersectByEndpoints()} and {@link #edgeIntersectById()}
 * to change the intersection of edges.
 *
 * <p>
 * Weights are not copied from the input graphs to the generated graph(s). In the future there might be an option to do
 * so.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class IntersectionGraphGenerator<V, E> implements GraphGenerator<V, E> {

	private final GraphFactory<V, E> factory;
	private final List<Graph<V, E>> graphs;
	private boolean intersectById = true;
	// private boolean verticesWeights, edgesWeights;
	// private Set<String> verticesWeightsKeys, edgesWeightsKeys;

	/**
	 * Create a new intersection graph generator that will use the default graph factory.
	 */
	public IntersectionGraphGenerator() {
		this(GraphFactory.undirected());
	}

	/**
	 * Create a new intersection graph generator that will use the given graph factory.
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
	public IntersectionGraphGenerator(GraphFactory<V, E> factory) {
		this.factory = Objects.requireNonNull(factory);
		graphs = new ArrayList<>();
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
	 * called to align the created graph with the generator configuration. If all of of the input graphs have self
	 * edges, the method {@link GraphFactory#allowSelfEdges()} will be called. If all of the input graphs have parallel
	 * edges, the method {@link GraphFactory#allowParallelEdges()} will be called. Note that parallel edges are not
	 * allowed (in each input graph independently) when intersecting by endpoints is used (see
	 * {@link #edgeIntersectByEndpoints()}).
	 *
	 * @return the graph factory that will be used to create the generated graph(s)
	 */
	public GraphFactory<V, E> graphFactory() {
		return factory;
	}

	/**
	 * Set the input graphs whose intersection graph will be generated.
	 *
	 * <p>
	 * Given two or more graphs, the intersection graph of the graphs is a graph with the vertex set which is the
	 * intersection of the vertex sets of the graphs, and the edge set which is the intersection of the edge sets of the
	 * graphs. There are two different ways to intersect the edges of the graphs, see {@link #edgeIntersectById()} and
	 * {@link #edgeIntersectByEndpoints()} for more details.
	 *
	 * @param  graphs                   the input graphs
	 * @return                          this generator
	 * @throws IllegalArgumentException if less than two input graphs are provided, or if the graphs have different
	 *                                      directionality
	 */
	public IntersectionGraphGenerator<V, E> graphs(Collection<? extends Graph<V, E>> graphs) {
		if (graphs.size() < 2)
			throw new IllegalArgumentException("At least two graphs must be provided");
		if (graphs.stream().map(Graph::isDirected).distinct().count() != 1)
			throw new IllegalArgumentException("Input graphs must have the same directionality");
		this.graphs.clear();
		this.graphs.addAll(graphs);
		return this;
	}

	/**
	 * Set the input graphs whose intersection graph will be generated.
	 *
	 * <p>
	 * Given two or more graphs, the intersection graph of the graphs is a graph with the vertex set which is the
	 * intersection of the vertex sets of the graphs, and the edge set which is the intersection of the edge sets of the
	 * graphs. There are two different ways to intersect the edges of the graphs, see {@link #edgeIntersectById()} and
	 * {@link #edgeIntersectByEndpoints()} for more details.
	 *
	 * @param  graphs                   the input graphs
	 * @return                          this generator
	 * @throws IllegalArgumentException if less than two input graphs are provided, or if the graphs have different
	 *                                      directionality
	 */
	@SafeVarargs
	public final IntersectionGraphGenerator<V, E> graphs(Graph<V, E>... graphs) {
		return graphs(ObjectList.of(graphs));
	}

	/**
	 * Set the intersection of edges to by the edges identifiers.
	 *
	 * <p>
	 * The intersection graph of two or more graphs using this method is a graph with the vertex set which is the
	 * intersection of the vertex sets of the graphs, and the edge set constructed as follows: for each edge in the
	 * first graph, if the edge (identifier) exists in all of the graphs, it is added to the intersection graph. The
	 * endpoints of the edge must be the same for all graphs, otherwise an exception is thrown.
	 *
	 * <p>
	 * A call to this method override a previous call to {@link #edgeIntersectByEndpoints()}. By default, the
	 * intersection of edges is by id.
	 *
	 * <p>
	 * Both self and parallel edges may be generated if such edges exist in all the input graphs.
	 *
	 * <p>
	 * In the following example two directed graphs are created, and the intersection graph of the two graphs is
	 * generated by the edges identifiers:
	 *
	 * <pre> {@code
	 * Graph<String, Integer> graph1 = Graph.newDirected();
	 * graph1.addVertices(List.of("A", "B", "C"));
	 * graph1.addEdge("A", "B", 1);
	 * graph1.addEdge("A", "C", 2);
	 *
	 * Graph<String, Integer> graph2 = Graph.newDirected();
	 * graph2.addVertices(List.of("A", "B", "D"));
	 * graph2.addEdge("A", "B", 1);
	 * graph2.addEdge("A", "D", 5);
	 *
	 * Graph<String, Integer> intersectionGraph = new IntersectionGraphGenerator<String, Integer>()
	 * 		.graphs(graph1, graph2)
	 * 		.edgeIntersectById() // override previous call to edgeIntersectByEndpoints() if have been called
	 * 		.generate();
	 * assert Set.of("A", "B").equals(intersectionGraph.vertices());
	 * assert Set.of(1).equals(intersectionGraph.edges());
	 * assert intersectionGraph.edgeSource(1).equals("A") && intersectionGraph.edgeTarget(1).equals("B");
	 * }</pre>
	 *
	 * @return this generator
	 */
	public IntersectionGraphGenerator<V, E> edgeIntersectById() {
		intersectById = true;
		return this;
	}

	/**
	 * Set the intersection of edges to be by the edges endpoints.
	 *
	 * <p>
	 * The intersection graph of two or more graphs using this method is a graph with the vertex set which is the
	 * intersection of the vertex sets of the graphs, and the edge set constructed as follows: for each edge in the
	 * first graph, if an edge (same endpoints, identifier may differ) exists in all of the graphs, it is added to the
	 * intersection graph. The identifier of the edge in the first graph is used as the identifier of the edge in the
	 * intersection graph.
	 *
	 * <p>
	 * The input graphs must not contain parallel edges. If one of the input graphs contains parallel edges, an
	 * exception is thrown during the graph(s) generation. Self edges may be generated if such edges exist in all the
	 * input graphs. Parallel edges are never generated.
	 *
	 * <p>
	 * A call to this method override a previous call to {@link #edgeIntersectById()}. By default, the intersection of
	 * edges is by id.
	 *
	 * <p>
	 * In the following example two directed graphs are created, and the intersection graph of the two graphs is
	 * generated by the edges endpoints. Note that the edge \(A, B\) exists in both graphs, but with different
	 * identifiers. In the generated graph, the edge \(A, B\) will appear only once, with identifier from the first
	 * graph:
	 *
	 * <pre> {@code
	 * Graph<String, Integer> graph1 = Graph.newDirected();
	 * graph1.addVertices(List.of("A", "B", "C"));
	 * graph1.addEdge("A", "B", 10);
	 * graph1.addEdge("A", "C", 20);
	 *
	 * Graph<String, Integer> graph2 = Graph.newDirected();
	 * graph2.addVertices(List.of("A", "B", "D"));
	 * graph2.addEdge("A", "B", 50);
	 * graph2.addEdge("A", "D", 100);
	 *
	 * Graph<String, Integer> intersectionGraph = new IntersectionGraphGenerator<String, Integer>()
	 * 		.graphs(graph1, graph2)
	 * 		.edgeIntersectByEndpoints() // override previous call to edgeIntersectById() if have been called
	 * 		.generate();
	 * assert Set.of("A", "B").equals(intersectionGraph.vertices());
	 * assert Set.of(10).equals(intersectionGraph.edges());
	 * assert intersectionGraph.edgeSource(10).equals("A") && intersectionGraph.edgeTarget(10).equals("B");
	 * }</pre>
	 *
	 * @return this generator
	 */
	public IntersectionGraphGenerator<V, E> edgeIntersectByEndpoints() {
		intersectById = false;
		return this;
	}

	// public IntersectionGraphGenerator<V, E> copyWeights(boolean verticesWeights, boolean edgesWeights) {
	// this.verticesWeights = verticesWeights;
	// this.edgesWeights = edgesWeights;
	// return this;
	// }

	// public IntersectionGraphGenerator<V, E> verticesWeightsKeys(Set<String> keys) {
	// verticesWeightsKeys = keys == null ? null : new HashSet<>(keys);
	// return this;
	// }

	// public IntersectionGraphGenerator<V, E> edgesWeightsKeys(Set<String> keys) {
	// edgesWeightsKeys = keys == null ? null : new HashSet<>(keys);
	// return this;
	// }

	@Override
	public GraphBuilder<V, E> generateIntoBuilder() {
		if (graphs.isEmpty())
			throw new IllegalStateException("Input graphs not provided");

		final Graph<V, E> firstGraph = graphs.get(0);
		final List<Graph<V, E>> remainingGraphs = graphs.subList(1, graphs.size());
		final boolean directed = firstGraph.isDirected();
		factory.setDirected(directed);
		if (graphs.stream().allMatch(g -> !Graphs.selfEdges(g).isEmpty()))
			factory.allowSelfEdges();
		if (intersectById && graphs.stream().allMatch(Graphs::containsParallelEdges))
			factory.allowParallelEdges();
		GraphBuilder<V, E> intersection = factory.newBuilder();

		Set<V> vertices;
		if (graphs.stream().allMatch(g -> g instanceof IntGraph)) {
			@SuppressWarnings("unchecked")
			Set<V> vertices0 = (Set<V>) new IntOpenHashSet();
			vertices = vertices0;
		} else {
			vertices = new ObjectOpenHashSet<>();
		}
		for (V v : firstGraph.vertices())
			if (remainingGraphs.stream().allMatch(g -> g.vertices().contains(v)))
				vertices.add(v);
		intersection.addVertices(vertices);
		vertices = intersection.vertices();

		final IndexIdMap<V> firstGraphVIdMap = firstGraph.indexGraphVerticesMap();
		final ToIntFunction<V> vIndex = firstGraphVIdMap::idToIndex;
		final Comparator<V> vComparator = Comparator.comparingInt(vIndex);

		if (intersectById) {
			for (V u : vertices) {
				for (EdgeIter<V, E> eit = firstGraph.outEdges(u).iterator(); eit.hasNext();) {
					E e = eit.next();
					V v = eit.target();
					if (!vertices.contains(v))
						continue;
					if (!directed && vComparator.compare(u, v) > 0)
						continue; /* avoid processing undirected edges twice */
					if (!remainingGraphs.stream().allMatch(g -> g.edges().contains(e)))
						continue;
					for (Graph<V, E> g : remainingGraphs) {
						V u2 = g.edgeSource(e);
						V v2 = g.edgeTarget(e);
						boolean sameEndpoints =
								(u.equals(u2) && v.equals(v2)) || (!directed && u.equals(v2) && v.equals(u2));
						if (!sameEndpoints)
							throw new IllegalArgumentException(
									"Input graphs have the same edge with different endpoints");
					}
					intersection.addEdge(u, v, e);
				}
			}

		} else { /* intersect by endpoints */
			if (graphs.stream().anyMatch(Graphs::containsParallelEdges))
				throw new IllegalArgumentException(
						"Cannot intersect by endpoints when input graphs contain parallel edges");

			// TODO can be implemented in array of size firstGraph.vertices().size()
			Int2IntOpenHashMap edges = new Int2IntOpenHashMap();

			for (V u : vertices) {
				for (Graph<V, E> graph : remainingGraphs) {
					for (EdgeIter<V, E> eit = graph.outEdges(u).iterator(); eit.hasNext();) {
						eit.next();
						V v = eit.target();
						if (!vertices.contains(v))
							continue;
						if (!directed && vComparator.compare(u, v) > 0)
							continue; /* avoid processing undirected edges twice */
						edges.addTo(vIndex.applyAsInt(v), 1);
					}
				}
				for (EdgeIter<V, E> eit = firstGraph.outEdges(u).iterator(); eit.hasNext();) {
					E e = eit.next();
					V v = eit.target();
					if (!vertices.contains(v))
						continue;
					if (!directed && vComparator.compare(u, v) > 0)
						continue; /* avoid processing undirected edges twice */
					boolean existInAllGraphs = edges.get(vIndex.applyAsInt(v)) == graphs.size() - 1;
					if (existInAllGraphs)
						intersection.addEdge(u, v, e);
				}
				edges.clear();
			}
		}
		return intersection;
	}

}
