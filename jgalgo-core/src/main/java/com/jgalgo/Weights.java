package com.jgalgo;

/**
 * Weights of graph vertices or edges.
 * <p>
 * A weights object associated with the edges (vertices) of a graph support
 * getting and setting a weight value for each edge (vertex) using the
 * {@link #get(int)} and {@link #set(int, Object)} methods. Such weights are
 * useful for various algorithms such as {@link SSSP} or
 * {@link MaximumMatchingWeighted} to assigned the <i>cost</i> of edges. Another
 * example is boolean weights used to represent the partition of vertices in
 * bipartite graphs, which is used by algorithms such as
 * {@link MaximumMatchingBipartiteHopcroftKarp}.
 * <p>
 * An exiting graph expose two methods to add new type of weights associated
 * with its vertices or edges: {@link Graph#addVerticesWeights(Object, Class)}
 * and {@link Graph#addEdgesWeights(Object, Class)}. Weights of primitive types
 * can be created by passing a primitive class to these methods, for example
 * this snippet demonstrate how a {@code double} weights type can be added to a
 * graph, and then passed to {@link SSSPDijkstra}:
 *
 * <pre> {@code
 * // Create a directed graph with three vertices and edges between them
 * DiGraph g = new GraphArrayDirected();
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
 * SSSP ssspAlgo = new SSSPDijkstra();
 * SSSP.Result ssspRes = ssspAlgo.computeShortestPaths(g, w, v1);
 *
 * // Print the shortest path from v1 to v3
 * assert ssspRes.distance(v3) == 4.3;
 * assert ssspRes.getPathTo(v3).equals(IntList.of(e1, e2));
 * System.out.println("Distance from v1 to v3 is: " + ssspRes.distance(v3));
 * System.out.println("The shortest path from v1 to v3 is:");
 * for (IntIterator it = ssspRes.getPathTo(v3).iterator(); it.hasNext();) {
 * 	int e = it.nextInt();
 * 	int u = g.edgeSource(e), v = g.edgeTarget(e);
 * 	System.out.println(" " + e + "(" + u + ", " + v + ")");
 * }
 * }</pre>
 *
 * <p>
 * A default weight can be provided in the time of the weights container. The
 * default weight will be returned from the {@link #get(int)} method for every
 * edge (vertex) that was not explicitly set another value using
 * {@link #set(int, Object)}.
 * <p>
 * If the weights container is associated with the edges of the graph, and the
 * {@link IDStrategy} of the edges performed some swaps to maintain its
 * invariant (see
 * {@link IDStrategy#addIDSwapListener(IDStrategy.IDSwapListener)}) the weights
 * container will be updated to the edges ids automatically.
 *
 * @author Barak Ugav
 */
public interface Weights<W> {

	/**
	 * Get the weight associated with the given id.
	 *
	 * @param id an id of edge/vertex
	 * @return the weight associated with the given id
	 */
	public W get(int id);

	/**
	 * Set the weight associated with the given id.
	 *
	 * @param id     an id of edge/vertex
	 * @param weight new weight that will be associated with the given id
	 */
	public void set(int id, W weight);

	/**
	 * Get the default weight of this weights container.
	 * <p>
	 * The default weight is the weight associated with all ids that were not
	 * explicitly set using {@link #set(int, Object)}.
	 *
	 * @return the default weight of this weights container.
	 */
	public W defaultWeight();

	/**
	 * Specific weights of primitive type {@code byte}.
	 *
	 * @author Barak Ugav
	 */
	public static interface Byte extends Weights<java.lang.Byte>, EdgeWeightFunc.Int {

		/**
		 * Get the weight associated with the given id.
		 *
		 * @param id an id of edge/vertex.
		 * @return the weight associated with the given id.
		 */
		public byte getByte(int id);

		@Deprecated
		@Override
		default java.lang.Byte get(int id) {
			return java.lang.Byte.valueOf(getByte(id));
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
		default void set(int id, java.lang.Byte weight) {
			set(id, weight.byteValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not
		 * explicitly set using {@link #set(int, byte)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public byte defaultWeightByte();

		@Deprecated
		@Override
		default java.lang.Byte defaultWeight() {
			return java.lang.Byte.valueOf(defaultWeightByte());
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Implement the {@link EdgeWeightFunc.Int} interface by using the weights of
		 * the container.
		 */
		@Override
		default int weightInt(int id) {
			return getByte(id);
		}
	}

	/**
	 * Specific weights of primitive type {@code short}.
	 *
	 * @author Barak Ugav
	 */
	public static interface Short extends Weights<java.lang.Short>, EdgeWeightFunc.Int {

		/**
		 * Get the weight associated with the given id.
		 *
		 * @param id an id of edge/vertex.
		 * @return the weight associated with the given id.
		 */
		public short getShort(int id);

		@Deprecated
		@Override
		default java.lang.Short get(int id) {
			return java.lang.Short.valueOf(getShort(id));
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
		default void set(int id, java.lang.Short weight) {
			set(id, weight.shortValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not
		 * explicitly set using {@link #set(int, short)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public short defaultWeightShort();

		@Deprecated
		@Override
		default java.lang.Short defaultWeight() {
			return java.lang.Short.valueOf(defaultWeightShort());
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Implement the {@link EdgeWeightFunc.Int} interface by using the weights of
		 * the container.
		 */
		@Override
		default int weightInt(int id) {
			return getShort(id);
		}
	}

	/**
	 * Specific weights of primitive type {@code int}.
	 *
	 * @author Barak Ugav
	 */
	public static interface Int extends Weights<Integer>, EdgeWeightFunc.Int {

		/**
		 * Get the weight associated with the given id.
		 *
		 * @param id an id of edge/vertex.
		 * @return the weight associated with the given id.
		 */
		public int getInt(int id);

		@Deprecated
		@Override
		default Integer get(int id) {
			return Integer.valueOf(getInt(id));
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
		default void set(int id, Integer weight) {
			set(id, weight.intValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not
		 * explicitly set using {@link #set(int, int)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public int defaultWeightInt();

		@Deprecated
		@Override
		default Integer defaultWeight() {
			return Integer.valueOf(defaultWeightInt());
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Implement the {@link EdgeWeightFunc.Int} interface by using the weights of
		 * the container.
		 */
		@Override
		default int weightInt(int id) {
			return getInt(id);
		}
	}

	/**
	 * Specific weights of primitive type {@code long}.
	 *
	 * @author Barak Ugav
	 */
	public static interface Long extends Weights<java.lang.Long>, EdgeWeightFunc {

		/**
		 * Get the weight associated with the given id.
		 *
		 * @param id an id of edge/vertex.
		 * @return the weight associated with the given id.
		 */
		public long getLong(int id);

		@Deprecated
		@Override
		default java.lang.Long get(int id) {
			return java.lang.Long.valueOf(getLong(id));
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
		default void set(int id, java.lang.Long weight) {
			set(id, weight.longValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not
		 * explicitly set using {@link #set(int, long)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public long defaultWeightLong();

		@Deprecated
		@Override
		default java.lang.Long defaultWeight() {
			return java.lang.Long.valueOf(defaultWeightLong());
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Implement the {@link EdgeWeightFunc} interface by using the weights of the
		 * container.
		 */
		@Override
		default double weight(int id) {
			return getLong(id);
		}
	}

	/**
	 * Specific weights of primitive type {@code float}.
	 *
	 * @author Barak Ugav
	 */
	public static interface Float extends Weights<java.lang.Float>, EdgeWeightFunc {

		/**
		 * Get the weight associated with the given id.
		 *
		 * @param id an id of edge/vertex.
		 * @return the weight associated with the given id.
		 */
		public float getFloat(int id);

		@Deprecated
		@Override
		default java.lang.Float get(int id) {
			return java.lang.Float.valueOf(getFloat(id));
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
		default void set(int id, java.lang.Float weight) {
			set(id, weight.floatValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not
		 * explicitly set using {@link #set(int, float)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public float defaultWeightFloat();

		@Deprecated
		@Override
		default java.lang.Float defaultWeight() {
			return java.lang.Float.valueOf(defaultWeightFloat());
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Implement the {@link EdgeWeightFunc} interface by using the weights of the
		 * container.
		 */
		@Override
		default double weight(int id) {
			return getFloat(id);
		}
	}

	/**
	 * Specific weights of primitive type {@code double}.
	 *
	 * @author Barak Ugav
	 */
	public static interface Double extends Weights<java.lang.Double>, EdgeWeightFunc {

		/**
		 * Get the weight associated with the given id.
		 *
		 * @param id an id of edge/vertex.
		 * @return the weight associated with the given id.
		 */
		public double getDouble(int id);

		@Deprecated
		@Override
		default java.lang.Double get(int id) {
			return java.lang.Double.valueOf(getDouble(id));
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
		default void set(int id, java.lang.Double weight) {
			set(id, weight.doubleValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not
		 * explicitly set using {@link #set(int, double)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public double defaultWeightDouble();

		@Deprecated
		@Override
		default java.lang.Double defaultWeight() {
			return java.lang.Double.valueOf(defaultWeightDouble());
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Implement the {@link EdgeWeightFunc} interface by using the weights of the
		 * container.
		 */
		@Override
		default double weight(int id) {
			return getDouble(id);
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
		 * @param id an id of edge/vertex.
		 * @return the weight associated with the given id.
		 */
		public boolean getBool(int id);

		@Deprecated
		@Override
		default Boolean get(int id) {
			return Boolean.valueOf(getBool(id));
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
		default void set(int id, Boolean weight) {
			set(id, weight.booleanValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not
		 * explicitly set using {@link #set(int, boolean)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public boolean defaultWeightBool();

		@Deprecated
		@Override
		default Boolean defaultWeight() {
			return Boolean.valueOf(defaultWeightBool());
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
		 * @param id an id of edge/vertex.
		 * @return the weight associated with the given id.
		 */
		public char getChar(int id);

		@Deprecated
		@Override
		default Character get(int id) {
			return Character.valueOf(getChar(id));
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
		default void set(int id, Character weight) {
			set(id, weight.charValue());
		}

		/**
		 * Get the default weight of this weights container.
		 * <p>
		 * The default weight is the weight associated with all ids that were not
		 * explicitly set using {@link #set(int, char)}.
		 *
		 * @return the default weight of this weights container.
		 */
		public char defaultWeightChar();

		@Deprecated
		@Override
		default Character defaultWeight() {
			return Character.valueOf(defaultWeightChar());
		}
	}

	/**
	 * Create an external vertex weights container.
	 * <p>
	 * An external weights container is a container that associate a weight to each
	 * vertex in the graph, but does not update when the graph is updated. This
	 * method should be used only in cases where the graph is unmodifiable.
	 *
	 * @param g    a graph
	 * @param type the type of the weights, used for primitive types weights
	 * @return a new weights container
	 * @param <E>        the weights type
	 * @param <WeightsT> the weights container, used to avoid casts of containers of
	 *                   primitive types
	 */
	public static <E, WeightsT extends Weights<E>> WeightsT createExternalVerticesWeights(Graph g,
			Class<? super E> type) {
		return createExternalVerticesWeights(g, type, null);
	}

	/**
	 * Create an external vertex weights container with default values.
	 * <p>
	 * An external weights container is a container that associate a weight to each
	 * vertex in the graph, but does not update when the graph is updated. This
	 * method should be used only in cases where the graph is unmodifiable.
	 *
	 * @param g      a graph
	 * @param type   the type of the weights, used for primitive types weights
	 * @param defVal default value use for the weights container
	 * @return a new weights container
	 * @param <E>        the weights type
	 * @param <WeightsT> the weights container, used to avoid casts of containers of
	 *                   primitive types
	 */
	public static <E, WeightsT extends Weights<E>> WeightsT createExternalVerticesWeights(Graph g,
			Class<? super E> type, E defVal) {
		return WeightsImpl.newInstance(g.getVerticesIDStrategy(), type, defVal);
	}

	/**
	 * Create an external edge weights container.
	 * <p>
	 * An external weights container is a container that associate a weight to each
	 * edge in the graph, but does not update when the graph is updated. This method
	 * should be used only in cases where the graph is unmodifiable.
	 *
	 * @param g    a graph
	 * @param type the type of the weights, used for primitive types weights
	 * @return a new weights container
	 * @param <E>        the weights type
	 * @param <WeightsT> the weights container, used to avoid casts of containers of
	 *                   primitive types
	 */
	public static <E, WeightsT extends Weights<E>> WeightsT createExternalEdgesWeights(Graph g, Class<? super E> type) {
		return createExternalEdgesWeights(g, type, null);
	}

	/**
	 * Create an external edge weights container with default values.
	 * <p>
	 * An external weights container is a container that associate a weight to each
	 * edge in the graph, but does not update when the graph is updated. This method
	 * should be used only in cases where the graph is unmodifiable.
	 *
	 * @param g      a graph
	 * @param type   the type of the weights, used for primitive types weights
	 * @param defVal default value use for the weights container
	 * @return a new weights container
	 * @param <E>        the weights type
	 * @param <WeightsT> the weights container, used to avoid casts of containers of
	 *                   primitive types
	 */
	public static <E, WeightsT extends Weights<E>> WeightsT createExternalEdgesWeights(Graph g, Class<? super E> type,
			E defVal) {
		return WeightsImpl.newInstance(g.getEdgesIDStrategy(), type, defVal);
	}

	/**
	 * The default vertices weight key of the bipartite property.
	 * <p>
	 * A bipartite graph is a graph in which the vertices are partitioned into two
	 * sets V1,V2 and there are no edges between two vertices u,v if they are both
	 * in V1 or both in V2. Some algorithms expect a bipartite graph as an input,
	 * and the partition V1,V2 is expected to be a vertex boolean weight keyed by
	 * {@link #DefaultBipartiteWeightKey}. To use a different key, the algorithms
	 * expose a {@code setBipartiteVerticesWeightKey(Object)} function.
	 */
	public static final Object DefaultBipartiteWeightKey = new Object() {
		@Override
		public String toString() {
			return "DefaultBipartiteVerticesWeightKey";
		}
	};

}