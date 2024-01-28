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
package com.jgalgo.alg;

import static com.jgalgo.internal.util.Range.range;
import java.util.Optional;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsBool;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Static class for bipartite graphs.
 *
 * <p>
 * A bipartite graph is a graph in which the vertices can be partitioned into two sets V1,V2 and there are no edges
 * between two vertices u,v if they are both in V1 or both in V2. Some algorithms expect a bipartite graph as an input,
 * and the partition V1,V2 is expected to be a vertex boolean weight keyed by
 * {@link BipartiteGraphs#VertexBiPartitionWeightKey}. See the static field documentation for more details.
 *
 * <p>
 * This class provides functions to check whether a graph is bipartite, and to find a bipartite partition of a graph.
 * These functions can add the bipartite partition as a vertex boolean weight as an option, but the default is to treat
 * the graph as immutable.
 *
 * <p>
 * Note that algorithms might test for the existent of a bipartite partition as vertex weights, and if it does exists,
 * to assume the graph is bipartite and use it. If the graph is modified after the bipartite partition was computed and
 * added, it might be invalid, so consider removing the vertex weights to avoid misleading algorithms.
 *
 * @author Barak Ugav
 */
public class BipartiteGraphs {

	private BipartiteGraphs() {}

	/**
	 * Check whether the given graph is bipartite or not.
	 *
	 * <p>
	 * If the computed partition is needed, use {@link #findPartition(Graph)}. This function does not have any side
	 * effects on the graph object itself, namely it does not add the partition (if one exist) as vertex weights.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   the graph
	 * @return     {@code true} if the graph is bipartite, {@code false} otherwise
	 */
	public static <V, E> boolean isBipartite(Graph<V, E> g) {
		return findBipartitePartition0(g.indexGraph()) != null;
	}

	/**
	 * Find a bipartite partition of the given graph (if one exists).
	 *
	 * <p>
	 * This function does not have any side effects on the graph object itself, namely it does not add the partition (if
	 * one exist) as vertex weights. To add the partition as vertex weights, use {@link #findPartition(Graph, boolean)}.
	 *
	 * <p>
	 * If an {@link IntGraph} is passed as an argument, {@link IVertexBiPartition} will be returned (if a partition
	 * exist).
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   the graph
	 * @return     the bipartite partition of the graph if one exists
	 */
	public static <V, E> Optional<VertexBiPartition<V, E>> findPartition(Graph<V, E> g) {
		return findPartition(g, false);
	}

	/**
	 * Find a bipartite partition of the given graph (if one exists), and optionally add the partition as vertex
	 * weights.
	 *
	 * <p>
	 * If an {@link IntGraph} is passed as an argument, {@link IVertexBiPartition} will be returned (if a partition
	 * exist).
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        the graph
	 * @param  addPartitionWeight       if {@code true}, add the partition (if exist) will be added to the graph as
	 *                                      vertex weights. If no valid bipartite partition is found, the graph will not
	 *                                      be modified. If vertex weights already exists in the graph with the key
	 *                                      {@link #VertexBiPartitionWeightKey}, they will be used as the partition.
	 * @return                          the bipartite partition of the graph if one exists
	 * @throws IllegalArgumentException if {@code addPartitionWeight} is {@code true} and the graph already has a non
	 *                                      boolean vertex weights with key {@link #VertexBiPartitionWeightKey}
	 */
	@SuppressWarnings("unchecked")
	public static <V, E> Optional<VertexBiPartition<V, E>> findPartition(Graph<V, E> g, boolean addPartitionWeight) {
		IndexGraph ig = g instanceof IndexGraph ? (IndexGraph) g : g.indexGraph();

		Bitmap partition0 = findBipartitePartition0(ig);
		if (partition0 == null)
			return Optional.empty();

		IWeightsBool partition = getOrCreateBoolWeights(ig, addPartitionWeight);
		for (int v : range(ig.vertices().size()))
			partition.set(v, partition0.get(v));

		IVertexBiPartition indexPartition = new VertexBiPartitions.FromWeights(ig, partition);
		VertexBiPartition<V, E> resultPartition;
		if (g instanceof IndexGraph) {
			resultPartition = (VertexBiPartition<V, E>) indexPartition;
		} else {
			resultPartition = VertexBiPartitions.partitionFromIndexPartition(g, indexPartition);
		}
		return Optional.of(resultPartition);
	}

	private static Bitmap findBipartitePartition0(IndexGraph g) {
		final int n = g.vertices().size();
		Bitmap partition = new Bitmap(n);
		IntPriorityQueue queue = new FIFOQueueIntNoReduce();
		Bitmap visited = new Bitmap(n);
		for (int start : range(n)) {
			if (visited.get(start))
				continue;
			visited.set(start);
			queue.enqueue(start);
			partition.set(start, true);
			while (!queue.isEmpty()) {
				final int u = queue.dequeueInt();
				final boolean uSide = partition.get(u);
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.targetInt();
					if (visited.get(v)) {
						if (partition.get(v) == uSide)
							return null;
						continue;
					}
					partition.set(v, !uSide);
					visited.set(v);
					queue.enqueue(v);
				}
				if (g.isDirected()) {
					for (IEdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
						eit.nextInt();
						int v = eit.sourceInt();
						if (visited.get(v)) {
							if (partition.get(v) == uSide)
								return null;
							continue;
						}
						partition.set(v, !uSide);
						visited.set(v);
						queue.enqueue(v);
					}
				}
			}
		}
		return partition;
	}

	private static IWeightsBool getOrCreateBoolWeights(IndexGraph g, boolean addPartitionWeight) {
		if (!addPartitionWeight)
			return IWeights.createExternalEdgesWeights(g, boolean.class);
		Object existingPartition = g.verticesWeights(VertexBiPartitionWeightKey);
		if (existingPartition == null)
			return g.addVerticesWeights(VertexBiPartitionWeightKey, boolean.class);
		if (!(existingPartition instanceof IWeightsBool))
			throw new IllegalArgumentException("found vertex weights with key '" + VertexBiPartitionWeightKey
					+ "' but it is not a boolean weights");
		return (IWeightsBool) existingPartition;
	}

	/**
	 * Get the existing bipartite partition of the given graph (if one exists).
	 *
	 * <p>
	 * If a bipartite partition was computed on the graph and boolean vertex weights were added to it, the partition
	 * will be returned. Otherwise, an empty optional will be returned. This function does not compute a partition if it
	 * doesn't find an existing one.
	 *
	 * <p>
	 * Note that if the graph was modified after the bipartite partition was computed and added, it might be invalid and
	 * no checks are performed in this function to verify that it is still valid.
	 *
	 * <p>
	 * If an {@link IntGraph} is passed as an argument, {@link IVertexBiPartition} will be returned (if a partition
	 * exist).
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        the graph
	 * @return                          the bipartite partition of the graph if one exists
	 * @throws IllegalArgumentException if the graph has a non boolean vertex weights with key
	 *                                      {@link #VertexBiPartitionWeightKey}
	 */
	@SuppressWarnings("unchecked")
	public static <V, E> Optional<VertexBiPartition<V, E>> getExistingPartition(Graph<V, E> g) {
		IndexGraph ig = g instanceof IndexGraph ? (IndexGraph) g : g.indexGraph();
		Object existingPartition = ig.verticesWeights(VertexBiPartitionWeightKey);
		if (existingPartition == null)
			return Optional.empty();
		if (!(existingPartition instanceof IWeightsBool))
			throw new IllegalArgumentException("found vertex weights with key '" + VertexBiPartitionWeightKey
					+ "' but it is not a boolean weights");
		IWeightsBool partition = (IWeightsBool) existingPartition;

		IVertexBiPartition indexPartition = new VertexBiPartitions.FromWeights(ig, partition);
		VertexBiPartition<V, E> resultPartition;
		if (g instanceof IndexGraph) {
			resultPartition = (VertexBiPartition<V, E>) indexPartition;
		} else {
			resultPartition = VertexBiPartitions.partitionFromIndexPartition(g, indexPartition);
		}
		return Optional.of(resultPartition);
	}

	/**
	 * Get the existing bipartite partition of the given {@link IntGraph} (if one exists).
	 *
	 * <p>
	 * If a bipartite partition was computed on the graph and boolean vertex weights were added to it, the partition
	 * will be returned. Otherwise, an empty optional will be returned. This function does not compute a partition if it
	 * doesn't find an existing one.
	 *
	 * <p>
	 * Note that if the graph was modified after the bipartite partition was computed and added, it might be invalid and
	 * no checks are performed in this function to verify that it is still valid.
	 *
	 * @param  g                        the graph
	 * @return                          the bipartite partition of the graph if one exists
	 * @throws IllegalArgumentException if the graph has a non boolean vertex weights with key
	 *                                      {@link #VertexBiPartitionWeightKey}
	 */
	public static Optional<IVertexBiPartition> getExistingPartition(IntGraph g) {
		return getExistingPartition((Graph<Integer, Integer>) g).map(p -> (IVertexBiPartition) p);
	}

	/**
	 * The vertices weight key of the bipartite property.
	 *
	 * <p>
	 * The bipartite partition is usually represented as a vertex boolean weight keyed by this key. The weight of each
	 * vertex indicates to which of the two partitions it belongs to. Functions such as
	 * {@link #findPartition(Graph, boolean)} may attempt to find a valid bipartition of a graph, and if one is found,
	 * to store it as vertex weights with this key. When an algorithm accept a graph to operate on, it may check for
	 * such vertex weights, and if they exists it may assume the graph is bipartite and use them.
	 *
	 * <p>
	 * If a graph contains vertex weights with this key, the partition can be retrieved by
	 * {@link #getExistingPartition(Graph)}. But note that a bipartite partition may become invalid if the graph is
	 * modified after the vertex weights were added. Consider removing the vertex weights to avoid misleading
	 * algorithms.
	 */
	public static final String VertexBiPartitionWeightKey = "_bipartite_partition";

}
