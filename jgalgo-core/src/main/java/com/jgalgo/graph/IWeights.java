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

import com.jgalgo.alg.bipartite.BipartiteGraphs;
import com.jgalgo.alg.match.MatchingAlgo;
import com.jgalgo.alg.shortestpath.ShortestPathSingleSource;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Weights of {@linkplain IntGraph int graph} vertices or edges.
 *
 * <p>
 * This interface is a specification of {@link Weights} for {@link IntGraph}.
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
 * {@link IntGraph#addVerticesWeights(String, Class)} and {@link IntGraph#addEdgesWeights(String, Class)}. Weights of
 * primitive types can be created by passing a primitive class to these methods, for example this snippet demonstrate
 * how a {@code double} weights type can be added to a graph, and then passed to {@link ShortestPathSingleSource}
 * algorithm:
 *
 * <pre> {@code
 * // Create a directed graph with three vertices and edges between them
 * IntGraph g = IntGraph.newDirected();
 * int v1 = g.addVertex();
 * int v2 = g.addVertex();
 * int v3 = g.addVertex();
 * int e1 = g.addEdge(v1, v2);
 * int e2 = g.addEdge(v2, v3);
 * int e3 = g.addEdge(v1, v3);
 *
 * // Assign some weights to the edges
 * IWeightsDouble weights = g.addEdgesWeights("weightsKey", double.class);
 * weights.set(e1, 1.2);
 * weights.set(e2, 3.1);
 * weights.set(e3, 15.1);
 * IWeightFunction weightFunc = weights;
 *
 * // Calculate the shortest paths from v1 to all other vertices
 * ShortestPathSingleSource ssspAlgo = ShortestPathSingleSource.newInstance();
 * ShortestPathSingleSource.Result ssspRes = ssspAlgo.computeShortestPaths(g, weightFunc, v1);
 *
 * // Print the shortest path from v1 to v3
 * assert ssspRes.distance(v3) == 4.3;
 * assert ssspRes.getPath(v3).edges().equals(IntList.of(e1, e2));
 * System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));
 * System.out.println("The shortest path from v1 to v3 is:");
 * for (int e : ssspRes.getPath(v3).edges()) {
 * 	int u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * <p>
 * A default weight can be provided in the time of the weights container. The default weight will be returned for every
 * edge (vertex) that was not explicitly set another value.
 *
 * <p>
 * There are type specific weights interfaces for both primitives and objects, such as {@link IWeightsDouble},
 * {@link IWeightsInt}, {@link IWeightsObj}, etc. The super interface {@link IWeights} allow to get and set weights as
 * objects, and should not be used when the type of the weights is known. The sub interfaces expose methods to get and
 * set weights as the specific type, for example {@link IWeightsInt#get(int)}.
 *
 * <p>
 * If the weights container is associated with the edges of an index graph, and the graph implementation chooses to
 * perform some swaps and renames to the edges, the weights container will update automatically (see
 * {@link IndexGraph#addEdgeRemoveListener(IndexRemoveListener)}).
 *
 * <p>
 * The {@link IWeights} interface can be used for edges or vertices, depending on how it was created. In this
 * documentation we use the term 'element' to refer to either an edge or a vertex.
 *
 * @param  <T> the weights type
 * @author     Barak Ugav
 */
public interface IWeights<T> extends Weights<Integer, T> {

	/**
	 * Get the weight associated with the given id.
	 *
	 * @param  id an id of edge/vertex
	 * @return    the weight associated with the given id
	 */
	public T getAsObj(int id);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #getAsObj(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default T getAsObj(Integer id) {
		return getAsObj(id.intValue());
	}

	/**
	 * Set the weight associated with the given id.
	 *
	 * @param id     an id of edge/vertex
	 * @param weight new weight that will be associated with the given id
	 */
	public void setAsObj(int id, T weight);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #setAsObj(int, Object)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default void setAsObj(Integer id, T weight) {
		setAsObj(id.intValue(), weight);
	}

	/**
	 * Create an external vertex weights container.
	 *
	 * <p>
	 * An external weights container is a container that associate a weight to each vertex in the graph, but does not
	 * update when the graph is updated. This method should be used only in cases where the graph is immutable.
	 *
	 * @param  <T>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 * @param  g          a graph
	 * @param  type       the type of the weights, used for primitive types weights
	 * @return            a new weights container
	 */
	public static <T, WeightsT extends IWeights<T>> WeightsT createExternalVerticesWeights(IntGraph g,
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
	 * @param  <T>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 * @param  g          a graph
	 * @param  type       the type of the weights, used for primitive types weights
	 * @param  defVal     default value use for the weights container
	 * @return            a new weights container
	 */
	@SuppressWarnings("unchecked")
	public static <T, WeightsT extends IWeights<T>> WeightsT createExternalVerticesWeights(IntGraph g,
			Class<? super T> type, T defVal) {
		IntSet vertices = g.indexGraph().vertices();
		WeightsImpl.IndexMutable<T> weights = WeightsImpl.IndexMutable.newInstance(vertices, true, type, defVal);
		if (g instanceof IndexGraph) {
			return (WeightsT) weights;
		} else {
			return (WeightsT) WeightsImpl.IntMapped.newInstance(weights, g.indexGraphVerticesMap());
		}
	}

	/**
	 * Create an external edge weights container.
	 *
	 * <p>
	 * An external weights container is a container that associate a weight to each edge in the graph, but does not
	 * update when the graph is updated. This method should be used only in cases where the graph is immutable.
	 *
	 * @param  <T>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 * @param  g          a graph
	 * @param  type       the type of the weights, used for primitive types weights
	 * @return            a new weights container
	 */
	public static <T, WeightsT extends IWeights<T>> WeightsT createExternalEdgesWeights(IntGraph g,
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
	 * @param  <T>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 * @param  g          a graph
	 * @param  type       the type of the weights, used for primitive types weights
	 * @param  defVal     default value use for the weights container
	 * @return            a new weights container
	 */
	@SuppressWarnings("unchecked")
	public static <T, WeightsT extends IWeights<T>> WeightsT createExternalEdgesWeights(IntGraph g,
			Class<? super T> type, T defVal) {
		IntSet edges = g.indexGraph().edges();
		WeightsImpl.IndexMutable<T> weights = WeightsImpl.IndexMutable.newInstance(edges, false, type, defVal);
		if (g instanceof IndexGraph) {
			return (WeightsT) weights;
		} else {
			return (WeightsT) WeightsImpl.IntMapped.newInstance(weights, g.indexGraphEdgesMap());
		}
	}

}
