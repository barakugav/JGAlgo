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

import com.jgalgo.alg.BipartiteGraphs;
import com.jgalgo.alg.MatchingAlgo;
import com.jgalgo.alg.ShortestPathSingleSource;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Weights of graph vertices or edges.
 *
 * <p>
 * A weights object associated with the edges (vertices) of a graph support getting and setting a weight value for each
 * edge (vertex). Such weights are useful for various algorithms such as {@link ShortestPathSingleSource} or
 * {@link MatchingAlgo} to assigned the <i>cost</i> of edges. Another example is boolean weights used to represent the
 * partition of vertices in {@linkplain BipartiteGraphs bipartite graphs}, which is used by algorithms such as
 * Hopcroft-Karp algorithm for cardinality maximum matching.
 *
 * <p>
 * An exiting graph expose two methods to add new type of weights associated with its vertices or edges:
 * {@link Graph#addVerticesWeights(String, Class)} and {@link Graph#addEdgesWeights(String, Class)}. When a new weights
 * is added to the edges (vertices) of a graph, it is added to ALL edges (vertices) of the graph. Weights of primitive
 * types can be created by passing a primitive class to these methods, for example this snippet demonstrate how a
 * {@code double} weights type can be added to a graph, and then passed to {@link ShortestPathSingleSource} algorithm:
 *
 * <pre> {@code
 * // Create an undirected graph with three vertices and edges between them
 * Graph<String, Integer> g = Graph.newUndirected();
 * g.addVertex("Berlin");
 * g.addVertex("Leipzig");
 * g.addVertex("Dresden");
 * g.addEdge("Berlin", "Leipzig", 9);
 * g.addEdge("Berlin", "Dresden", 13);
 * g.addEdge("Dresden", "Leipzig", 14);
 *
 * // Assign some weights to the edges
 * WeightsDouble<Integer> w = g.addEdgesWeights("distance-km", double.class);
 * w.set(9, 191.1);
 * w.set(13, 193.3);
 * w.set(14, 121.3);
 *
 * // Calculate the shortest paths from Berlin to all other cities
 * ShortestPathSingleSource ssspAlgo = ShortestPathSingleSource.newInstance();
 * ShortestPathSingleSource.Result<String, Integer> ssspRes = ssspAlgo.computeShortestPaths(g, w, "Berlin");
 *
 * // Print the shortest path from Berlin to Leipzig
 * System.out.println("Distance from Berlin to Leipzig is: " + ssspRes.distance("Leipzig"));
 * System.out.println("The shortest path from Berlin to Leipzig is:");
 * for (Integer e : ssspRes.getPath("Leipzig").edges()) {
 * 	String u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * <p>
 * A default weight can be provided in the time of the weights container. The default weight will be returned for every
 * edge (vertex) that was not explicitly set another value.
 *
 * <p>
 * There are type specific weights interfaces for both primitives and objects, such as {@link WeightsDouble},
 * {@link WeightsInt}, {@link WeightsObj}, etc. The super interface {@link Weights} allow to get and set weights as
 * objects, and should not be used when the type of the weights is known. The sub interfaces expose methods to get and
 * set weights as the specific type, for example {@link WeightsInt#get(Object)}.
 *
 * <p>
 * If the weights container is associated with the edges of an index graph, and the graph implementation chooses to
 * perform some swaps and renames to the edges, the weights container will update automatically (see
 * {@link IndexGraph#addEdgeRemoveListener(IndexRemoveListener)}).
 *
 * <p>
 * The {@link Weights} interface can be used for edges or vertices, depending on how it was created. In this
 * documentation we use the term 'element' to refer to either an edge or a vertex.
 *
 * @param  <K> the elements (vertices/edges) type
 * @param  <T> the weights type
 * @author     Barak Ugav
 */
public interface Weights<K, T> {

	/**
	 * Get the weight associated with the given element.
	 *
	 * <p>
	 * This method return the weight as an object, and should not be used when its known what type the weights are.
	 *
	 * @param  element an element (edge/vertex)
	 * @return         the weight associated with the given element
	 */
	public T getAsObj(K element);

	/**
	 * Set the weight associated with the given element.
	 *
	 * <p>
	 * This method accept the weight as an object, and should not be used when its known what type the weights are.
	 *
	 * @param element an element (edge/vertex)
	 * @param weight  new weight that will be associated with the given element
	 */
	public void setAsObj(K element, T weight);

	/**
	 * Get the default weight of this weights container.
	 *
	 * <p>
	 * The default weight is the weight associated with all ids that were not explicitly set. This method return the
	 * default weight as an object, and should not be used when its known what type the weights are.
	 *
	 * @return the default weight of this weights container
	 */
	public T defaultWeightAsObj();

	/**
	 * Create an external vertex weights container.
	 *
	 * <p>
	 * An external weights container is a container that associate a weight to each vertex in the graph, but does not
	 * update when the graph is updated. This method should be used only in cases where the graph is immutable.
	 *
	 * <p>
	 * The created weights container will have a default weight of {@code null} or {@code 0} for primitives.
	 *
	 * @param  <V>        the vertices type
	 * @param  <T>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link WeightsInt}, {@link WeightsDouble} ect.
	 * @param  g          a graph
	 * @param  type       the type of the weights, used for primitive types weights
	 * @return            a new weights container
	 */
	public static <V, T, WeightsT extends Weights<V, T>> WeightsT createExternalVerticesWeights(Graph<V, ?> g,
			Class<? super T> type) {
		return createExternalVerticesWeights(g, type, null);
	}

	/**
	 * Create an external vertex weights container with default values.
	 *
	 * <p>
	 * An external weights container is a container that associate a weight to each vertex in the graph, but does not
	 * update when the graph is updated. This method should be used only in cases where the graph is immutable.
	 *
	 * @param  <V>        the vertices type
	 * @param  <T>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link WeightsInt}, {@link WeightsDouble} ect.
	 * @param  g          a graph
	 * @param  type       the type of the weights, used for primitive types weights
	 * @param  defVal     default value use for the weights container
	 * @return            a new weights container
	 */
	@SuppressWarnings("unchecked")
	public static <V, T, WeightsT extends Weights<V, T>> WeightsT createExternalVerticesWeights(Graph<V, ?> g,
			Class<? super T> type, T defVal) {
		IntSet vertices = g.indexGraph().vertices();
		WeightsImpl.IndexMutable<T> weights = WeightsImpl.IndexMutable.newInstance(vertices, false, type, defVal);
		if (vertices.size() > 0)
			weights.expand(vertices.size());
		if (g instanceof IndexGraph) {
			return (WeightsT) weights;
		} else if (g instanceof IntGraph) {
			IntGraph g0 = (IntGraph) g;
			return (WeightsT) WeightsImpl.IntMapped.newInstance(weights, g0.indexGraphVerticesMap());
		} else {
			return (WeightsT) WeightsImpl.ObjMapped.newInstance(weights, g.indexGraphVerticesMap());
		}
	}

	/**
	 * Create an external edge weights container.
	 *
	 * <p>
	 * An external weights container is a container that associate a weight to each edge in the graph, but does not
	 * update when the graph is updated. This method should be used only in cases where the graph is immutable.
	 *
	 * <p>
	 * The created weights container will have a default weight of {@code null} or {@code 0} for primitives.
	 *
	 * @param  <E>        the edges type
	 * @param  <T>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 * @param  g          a graph
	 * @param  type       the type of the weights, used for primitive types weights
	 * @return            a new weights container
	 */
	public static <E, T, WeightsT extends Weights<E, T>> WeightsT createExternalEdgesWeights(Graph<?, E> g,
			Class<? super T> type) {
		return createExternalEdgesWeights(g, type, null);
	}

	/**
	 * Create an external edge weights container with default values.
	 *
	 * <p>
	 * An external weights container is a container that associate a weight to each edge in the graph, but does not
	 * update when the graph is updated. This method should be used only in cases where the graph is immutable.
	 *
	 * @param  <E>        the edges type
	 * @param  <T>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 * @param  g          a graph
	 * @param  type       the type of the weights, used for primitive types weights
	 * @param  defVal     default value use for the weights container
	 * @return            a new weights container
	 */
	@SuppressWarnings("unchecked")
	public static <E, T, WeightsT extends Weights<E, T>> WeightsT createExternalEdgesWeights(Graph<?, E> g,
			Class<? super T> type, T defVal) {
		IntSet edges = g.indexGraph().edges();
		WeightsImpl.IndexMutable<T> weights = WeightsImpl.IndexMutable.newInstance(edges, true, type, defVal);
		if (edges.size() > 0)
			weights.expand(edges.size());
		if (g instanceof IndexGraph) {
			return (WeightsT) weights;
		} else if (g instanceof IntGraph) {
			IntGraph g0 = (IntGraph) g;
			return (WeightsT) WeightsImpl.IntMapped.newInstance(weights, g0.indexGraphEdgesMap());
		} else {
			return (WeightsT) WeightsImpl.ObjMapped.newInstance(weights, g.indexGraphEdgesMap());
		}
	}

}
