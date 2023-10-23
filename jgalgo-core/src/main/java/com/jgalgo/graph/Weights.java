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
import com.jgalgo.internal.util.JGAlgoUtils;

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
 * {@link Graph#addVerticesWeights(Object, Class)} and {@link Graph#addEdgesWeights(Object, Class)}. Weights of
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
 * Weights.Double w = g.addEdgesWeights("weightsKey", double.class);
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
 * @param  <W> the weights type
 * @author     Barak Ugav
 */
public interface Weights<W> {

	/**
	 * Get the weight associated with the given id.
	 *
	 * @param  id an id of edge/vertex
	 * @return    the weight associated with the given id
	 */
	public W getAsObj(int id);

	/**
	 * Set the weight associated with the given id.
	 *
	 * @param id     an id of edge/vertex
	 * @param weight new weight that will be associated with the given id
	 */
	public void setAsObj(int id, W weight);

	/**
	 * Get the default weight of this weights container.
	 * <p>
	 * The default weight is the weight associated with all ids that were not explicitly set using
	 * {@link #set(int, Object)}.
	 *
	 * @return the default weight of this weights container.
	 */
	public W defaultWeightAsObj();

	/**
	 * Specific weights of objects.
	 *
	 * @author Barak Ugav
	 */
	public static interface Obj<W> extends Weights<W> {

		/**
		 * Get the weight associated with the given id.
		 *
		 * @param  id an id of edge/vertex.
		 * @return    the weight associated with the given id.
		 */
		public W get(int id);

		@Deprecated
		@Override
		default W getAsObj(int id) {
			return get(id);
		}

		/**
		 * Set the weight associated with the given id.
		 *
		 * @param id     an id of edge/vertex
		 * @param weight new weight that will be associated with the given id
		 */
		public void set(int id, W weight);

		@Deprecated
		@Override
		default void setAsObj(int id, W weight) {
			set(id, weight);
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not explicitly set using
		 * {@link #set(int, Object)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public W defaultWeight();

		@Deprecated
		@Override
		default W defaultWeightAsObj() {
			return defaultWeight();
		}
	}

	/**
	 * Specific weights of primitive type {@code byte}.
	 *
	 * @author Barak Ugav
	 */
	public static interface Byte extends Weights<java.lang.Byte>, WeightFunction.Int {

		/**
		 * Get the weight associated with the given id.
		 *
		 * @param  id an id of edge/vertex.
		 * @return    the weight associated with the given id.
		 */
		public byte get(int id);

		@Deprecated
		@Override
		default java.lang.Byte getAsObj(int id) {
			return java.lang.Byte.valueOf(get(id));
		}

		/**
		 * Set the weight associated with the given id.
		 *
		 * @param id     an id of edge/vertex
		 * @param weight new weight that will be associated with the given id
		 */
		public void set(int id, byte weight);

		@Deprecated
		@Override
		default void setAsObj(int id, java.lang.Byte weight) {
			set(id, weight.byteValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not explicitly set using
		 * {@link #set(int, byte)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public byte defaultWeight();

		@Deprecated
		@Override
		default java.lang.Byte defaultWeightAsObj() {
			return java.lang.Byte.valueOf(defaultWeight());
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Implement the {@link WeightFunction.Int} interface by using the weights of the container.
		 */
		@Override
		default int weightInt(int id) {
			return get(id);
		}
	}

	/**
	 * Specific weights of primitive type {@code short}.
	 *
	 * @author Barak Ugav
	 */
	public static interface Short extends Weights<java.lang.Short>, WeightFunction.Int {

		/**
		 * Get the weight associated with the given id.
		 *
		 * @param  id an id of edge/vertex.
		 * @return    the weight associated with the given id.
		 */
		public short get(int id);

		@Deprecated
		@Override
		default java.lang.Short getAsObj(int id) {
			return java.lang.Short.valueOf(get(id));
		}

		/**
		 * Set the weight associated with the given id.
		 *
		 * @param id     an id of edge/vertex
		 * @param weight new weight that will be associated with the given id
		 */
		public void set(int id, short weight);

		@Deprecated
		@Override
		default void setAsObj(int id, java.lang.Short weight) {
			set(id, weight.shortValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not explicitly set using
		 * {@link #set(int, short)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public short defaultWeight();

		@Deprecated
		@Override
		default java.lang.Short defaultWeightAsObj() {
			return java.lang.Short.valueOf(defaultWeight());
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Implement the {@link WeightFunction.Int} interface by using the weights of the container.
		 */
		@Override
		default int weightInt(int id) {
			return get(id);
		}
	}

	/**
	 * Specific weights of primitive type {@code int}.
	 *
	 * @author Barak Ugav
	 */
	public static interface Int extends Weights<Integer>, WeightFunction.Int {

		/**
		 * Get the weight associated with the given id.
		 *
		 * @param  id an id of edge/vertex.
		 * @return    the weight associated with the given id.
		 */
		public int get(int id);

		@Deprecated
		@Override
		default Integer getAsObj(int id) {
			return Integer.valueOf(get(id));
		}

		/**
		 * Set the weight associated with the given id.
		 *
		 * @param id     an id of edge/vertex
		 * @param weight new weight that will be associated with the given id
		 */
		public void set(int id, int weight);

		@Deprecated
		@Override
		default void setAsObj(int id, Integer weight) {
			set(id, weight.intValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not explicitly set using
		 * {@link #set(int, int)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public int defaultWeight();

		@Deprecated
		@Override
		default Integer defaultWeightAsObj() {
			return Integer.valueOf(defaultWeight());
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Implement the {@link WeightFunction.Int} interface by using the weights of the container.
		 */
		@Override
		default int weightInt(int id) {
			return get(id);
		}
	}

	/**
	 * Specific weights of primitive type {@code long}.
	 *
	 * @author Barak Ugav
	 */
	public static interface Long extends Weights<java.lang.Long>, WeightFunction {

		/**
		 * Get the weight associated with the given id.
		 *
		 * @param  id an id of edge/vertex.
		 * @return    the weight associated with the given id.
		 */
		public long get(int id);

		@Deprecated
		@Override
		default java.lang.Long getAsObj(int id) {
			return java.lang.Long.valueOf(get(id));
		}

		/**
		 * Set the weight associated with the given id.
		 *
		 * @param id     an id of edge/vertex
		 * @param weight new weight that will be associated with the given id
		 */
		public void set(int id, long weight);

		@Deprecated
		@Override
		default void setAsObj(int id, java.lang.Long weight) {
			set(id, weight.longValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not explicitly set using
		 * {@link #set(int, long)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public long defaultWeight();

		@Deprecated
		@Override
		default java.lang.Long defaultWeightAsObj() {
			return java.lang.Long.valueOf(defaultWeight());
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Implement the {@link WeightFunction} interface by using the weights of the container.
		 */
		@Override
		default double weight(int id) {
			return get(id);
		}
	}

	/**
	 * Specific weights of primitive type {@code float}.
	 *
	 * @author Barak Ugav
	 */
	public static interface Float extends Weights<java.lang.Float>, WeightFunction {

		/**
		 * Get the weight associated with the given id.
		 *
		 * @param  id an id of edge/vertex.
		 * @return    the weight associated with the given id.
		 */
		public float get(int id);

		@Deprecated
		@Override
		default java.lang.Float getAsObj(int id) {
			return java.lang.Float.valueOf(get(id));
		}

		/**
		 * Set the weight associated with the given id.
		 *
		 * @param id     an id of edge/vertex
		 * @param weight new weight that will be associated with the given id
		 */
		public void set(int id, float weight);

		@Deprecated
		@Override
		default void setAsObj(int id, java.lang.Float weight) {
			set(id, weight.floatValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not explicitly set using
		 * {@link #set(int, float)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public float defaultWeight();

		@Deprecated
		@Override
		default java.lang.Float defaultWeightAsObj() {
			return java.lang.Float.valueOf(defaultWeight());
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Implement the {@link WeightFunction} interface by using the weights of the container.
		 */
		@Override
		default double weight(int id) {
			return get(id);
		}
	}

	/**
	 * Specific weights of primitive type {@code double}.
	 *
	 * @author Barak Ugav
	 */
	public static interface Double extends Weights<java.lang.Double>, WeightFunction {

		/**
		 * Get the weight associated with the given id.
		 *
		 * @param  id an id of edge/vertex.
		 * @return    the weight associated with the given id.
		 */
		public double get(int id);

		@Deprecated
		@Override
		default java.lang.Double getAsObj(int id) {
			return java.lang.Double.valueOf(get(id));
		}

		/**
		 * Set the weight associated with the given id.
		 *
		 * @param id     an id of edge/vertex
		 * @param weight new weight that will be associated with the given id
		 */
		public void set(int id, double weight);

		@Deprecated
		@Override
		default void setAsObj(int id, java.lang.Double weight) {
			set(id, weight.doubleValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not explicitly set using
		 * {@link #set(int, double)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public double defaultWeight();

		@Deprecated
		@Override
		default java.lang.Double defaultWeightAsObj() {
			return java.lang.Double.valueOf(defaultWeight());
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Implement the {@link WeightFunction} interface by using the weights of the container.
		 */
		@Override
		default double weight(int id) {
			return get(id);
		}
	}

	/**
	 * Specific weights of primitive type {@code boolean}.
	 *
	 * @author Barak Ugav
	 */
	public static interface Bool extends Weights<Boolean> {

		/**
		 * Get the weight associated with the given id.
		 *
		 * @param  id an id of edge/vertex.
		 * @return    the weight associated with the given id.
		 */
		public boolean get(int id);

		@Deprecated
		@Override
		default Boolean getAsObj(int id) {
			return Boolean.valueOf(get(id));
		}

		/**
		 * Set the weight associated with the given id.
		 *
		 * @param id     an id of edge/vertex
		 * @param weight new weight that will be associated with the given id
		 */
		public void set(int id, boolean weight);

		@Deprecated
		@Override
		default void setAsObj(int id, Boolean weight) {
			set(id, weight.booleanValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not explicitly set using
		 * {@link #set(int, boolean)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public boolean defaultWeight();

		@Deprecated
		@Override
		default Boolean defaultWeightAsObj() {
			return Boolean.valueOf(defaultWeight());
		}
	}

	/**
	 * Specific weights of primitive type {@code char}.
	 *
	 * @author Barak Ugav
	 */
	public static interface Char extends Weights<Character> {

		/**
		 * Get the weight associated with the given id.
		 *
		 * @param  id an id of edge/vertex.
		 * @return    the weight associated with the given id.
		 */
		public char get(int id);

		@Deprecated
		@Override
		default Character getAsObj(int id) {
			return Character.valueOf(get(id));
		}

		/**
		 * Set the weight associated with the given id.
		 *
		 * @param id     an id of edge/vertex
		 * @param weight new weight that will be associated with the given id
		 */
		public void set(int id, char weight);

		@Deprecated
		@Override
		default void setAsObj(int id, Character weight) {
			set(id, weight.charValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not explicitly set using
		 * {@link #set(int, char)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public char defaultWeight();

		@Deprecated
		@Override
		default Character defaultWeightAsObj() {
			return Character.valueOf(defaultWeight());
		}
	}

	/**
	 * Create an external vertex weights container.
	 * <p>
	 * An external weights container is a container that associate a weight to each vertex in the graph, but does not
	 * update when the graph is updated. This method should be used only in cases where the graph is immutable.
	 *
	 * @param  g          a graph
	 * @param  type       the type of the weights, used for primitive types weights
	 * @return            a new weights container
	 * @param  <E>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types
	 */
	public static <E, WeightsT extends Weights<E>> WeightsT createExternalVerticesWeights(Graph g,
			Class<? super E> type) {
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
	 * @param  <E>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types
	 */
	@SuppressWarnings("unchecked")
	public static <E, WeightsT extends Weights<E>> WeightsT createExternalVerticesWeights(Graph g,
			Class<? super E> type, E defVal) {
		GraphElementSet vertices = ((IndexGraphImpl) g.indexGraph()).vertices();
		WeightsImpl.IndexMutable<E> weights = WeightsImpl.IndexMutable.newInstance(vertices, type, defVal);
		if (vertices.size() > 0)
			weights.expand(vertices.size());
		if (g instanceof IndexGraph) {
			return (WeightsT) weights;
		} else {
			return (WeightsT) WeightsImpl.Mapped.newInstance(weights, g.indexGraphVerticesMap());
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
	 * @param  <E>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types
	 */
	public static <E, WeightsT extends Weights<E>> WeightsT createExternalEdgesWeights(Graph g, Class<? super E> type) {
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
	 * @param  <E>        the weights type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types
	 */
	@SuppressWarnings("unchecked")
	public static <E, WeightsT extends Weights<E>> WeightsT createExternalEdgesWeights(Graph g, Class<? super E> type,
			E defVal) {
		GraphElementSet edges = ((IndexGraphImpl) g.indexGraph()).edges();
		WeightsImpl.IndexMutable<E> weights = WeightsImpl.IndexMutable.newInstance(edges, type, defVal);
		if (edges.size() > 0)
			weights.expand(edges.size());
		if (g instanceof IndexGraph) {
			return (WeightsT) weights;
		} else {
			return (WeightsT) WeightsImpl.Mapped.newInstance(weights, g.indexGraphEdgesMap());
		}
	}

	/**
	 * The default vertices weight key of the bipartite property.
	 * <p>
	 * A bipartite graph is a graph in which the vertices are partitioned into two sets V1,V2 and there are no edges
	 * between two vertices u,v if they are both in V1 or both in V2. Some algorithms expect a bipartite graph as an
	 * input, and the partition V1,V2 is expected to be a vertex boolean weight keyed by
	 * {@link #DefaultBipartiteWeightKey}. To use a different key, the algorithms expose a
	 * {@code setBipartiteVerticesWeightKey(Object)} function.
	 */
	public static final Object DefaultBipartiteWeightKey = JGAlgoUtils.labeledObj("DefaultBipartiteVerticesWeightKey");

}
