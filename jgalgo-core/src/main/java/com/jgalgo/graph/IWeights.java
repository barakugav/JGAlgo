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

import com.jgalgo.alg.MatchingAlgo;
import com.jgalgo.alg.ShortestPathSingleSource;

/**
 * Weights of graph vertices or edges.
 * <p>
 * A weights object associated with the edges (vertices) of a graph support getting and setting a weight value for each
 * edge (vertex). Such weights are useful for various algorithms such as {@link ShortestPathSingleSource} or
 * {@link MatchingAlgo} to assigned the <i>cost</i> of edges. Another example is boolean weights used to represent the
 * partition of vertices in bipartite graphs, which is used by algorithms such as Hopcroft-Karp algorithm for
 * cardinality maximum matching in bipartite graphs.
 * <p>
 * An exiting graph expose two methods to add new type of weights associated with its vertices or edges:
 * {@link IntGraph#addVerticesWeights(String, Class)} and {@link IntGraph#addEdgesWeights(String, Class)}. Weights of
 * primitive types can be created by passing a primitive class to these methods, for example this snippet demonstrate
 * how a {@code double} weights type can be added to a graph, and then passed to {@link ShortestPathSingleSource}
 * algorithm:
 *
 * <pre> {@code
 * // Create a directed graph with three vertices and edges between them
 * Graph g = Graph.newDirected();
 * int v1 = g.addVertex();
 * int v2 = g.addVertex();
 * int v3 = g.addVertex();
 * int e1 = g.addEdge(v1, v2);
 * int e2 = g.addEdge(v2, v3);
 * int e3 = g.addEdge(v1, v3);
 *
 * // Assign some weights to the edges
 * IWeightsDouble w = g.addEdgesWeights("weightsKey", double.class);
 * w.set(e1, 1.2);
 * w.set(e2, 3.1);
 * w.set(e3, 15.1);
 *
 * // Calculate the shortest paths from v1 to all other vertices
 * ShortestPathSingleSource ssspAlgo = ShortestPathSingleSource.newInstance();
 * ShortestPathSingleSource.Result ssspRes = ssspAlgo.computeShortestPaths(g, w, v1);
 *
 * // Print the shortest path from v1 to v3
 * assert ssspRes.distance(v3) == 4.3;
 * assert ssspRes.getPath(v3).equals(IntList.of(e1, e2));
 * System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));
 * System.out.println("The shortest path from v1 to v3 is:");
 * for (int e : ssspRes.getPath(v3)) {
 * 	int u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * <p>
 * A default weight can be provided in the time of the weights container. The default weight will be returned for every
 * edge (vertex) that was not explicitly set another value.
 * <p>
 * If the weights container is associated with the edges of an index graph, and the graph implementation chooses to
 * perform some swaps and renames to the edges, the weights container will update automatically (see
 * {@link IndexGraph#addEdgeSwapListener(IndexSwapListener)}).
 *
 * @param  <T> the weights type
 * @author     Barak Ugav
 */
public interface IWeights<T> {

	/**
	 * Get the weight associated with the given id.
	 *
	 * @param  id an id of edge/vertex
	 * @return    the weight associated with the given id
	 */
	public T getAsObj(int id);

	/**
	 * Set the weight associated with the given id.
	 *
	 * @param id     an id of edge/vertex
	 * @param weight new weight that will be associated with the given id
	 */
	public void setAsObj(int id, T weight);

	/**
	 * Get the default weight of this weights container.
	 * <p>
	 * The default weight is the weight associated with all ids that were not explicitly set.
	 *
	 * @return the default weight of this weights container.
	 */
	public T defaultWeightAsObj();

	/**
	 * Create an external vertex weights container.
	 * <p>
	 * An external weights container is a container that associate a weight to each vertex in the graph, but does not
	 * update when the graph is updated. This method should be used only in cases where the graph is immutable.
	 *
	 * @param  g          a graph
	 * @param  type       the type of the weights, used for primitive types weights
	 * @return            a new weights container
	 * @param  <T>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 */
	public static <T, WeightsT extends IWeights<T>> WeightsT createExternalVerticesWeights(IntGraph g,
			Class<? super T> type) {
		return createExternalVerticesWeights(g, type, null);
	}

	/**
	 * Create an external vertex weights container with default values.
	 * <p>
	 * An external weights container is a container that associate a weight to each vertex in the graph, but does not
	 * update when the graph is updated. This method should be used only in cases where the graph is immutable.
	 *
	 * @param  g          a graph
	 * @param  type       the type of the weights, used for primitive types weights
	 * @param  defVal     default value use for the weights container
	 * @return            a new weights container
	 * @param  <T>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 */
	@SuppressWarnings("unchecked")
	public static <T, WeightsT extends IWeights<T>> WeightsT createExternalVerticesWeights(IntGraph g,
			Class<? super T> type, T defVal) {
		GraphElementSet vertices = ((IndexGraphImpl) g.indexGraph()).vertices();
		WeightsImpl.IndexMutable<T> weights = WeightsImpl.IndexMutable.newInstance(vertices, type, defVal);
		if (vertices.size() > 0)
			weights.expand(vertices.size());
		if (g instanceof IndexGraph) {
			return (WeightsT) weights;
		} else {
			return (WeightsT) WeightsImpl.IntMapped.newInstance(weights, g.indexGraphVerticesMap());
		}
	}

	/**
	 * Create an external edge weights container.
	 * <p>
	 * An external weights container is a container that associate a weight to each edge in the graph, but does not
	 * update when the graph is updated. This method should be used only in cases where the graph is immutable.
	 *
	 * @param  g          a graph
	 * @param  type       the type of the weights, used for primitive types weights
	 * @return            a new weights container
	 * @param  <T>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 */
	public static <T, WeightsT extends IWeights<T>> WeightsT createExternalEdgesWeights(IntGraph g,
			Class<? super T> type) {
		return createExternalEdgesWeights(g, type, null);
	}

	/**
	 * Create an external edge weights container with default values.
	 * <p>
	 * An external weights container is a container that associate a weight to each edge in the graph, but does not
	 * update when the graph is updated. This method should be used only in cases where the graph is immutable.
	 *
	 * @param  g          a graph
	 * @param  type       the type of the weights, used for primitive types weights
	 * @param  defVal     default value use for the weights container
	 * @return            a new weights container
	 * @param  <T>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link IWeightsInt}, {@link IWeightsDouble} ect.
	 */
	@SuppressWarnings("unchecked")
	public static <T, WeightsT extends IWeights<T>> WeightsT createExternalEdgesWeights(IntGraph g,
			Class<? super T> type, T defVal) {
		GraphElementSet edges = ((IndexGraphImpl) g.indexGraph()).edges();
		WeightsImpl.IndexMutable<T> weights = WeightsImpl.IndexMutable.newInstance(edges, type, defVal);
		if (edges.size() > 0)
			weights.expand(edges.size());
		if (g instanceof IndexGraph) {
			return (WeightsT) weights;
		} else {
			return (WeightsT) WeightsImpl.IntMapped.newInstance(weights, g.indexGraphEdgesMap());
		}
	}

}
