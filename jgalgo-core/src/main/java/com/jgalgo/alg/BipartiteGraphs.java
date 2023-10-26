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

import java.util.BitSet;
import java.util.Optional;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Static class for bipartite graphs.
 * <p>
 * A bipartite graph is a graph in which the vertices can be partitioned into two sets V1,V2 and there are no edges
 * between two vertices u,v if they are both in V1 or both in V2. Some algorithms expect a bipartite graph as an input,
 * and the partition V1,V2 is expected to be a vertex boolean weight keyed by
 * {@link BipartiteGraphs#VertexBiPartitionWeightKey}. See the static field documentation for more details.
 * <p>
 * This class provides functions to check whether a graph is bipartite, and to find a bipartite partition of a graph.
 * These functions can add the bipartite partition as a vertex boolean weight as an option, but the default is to treat
 * the graph as immutable.
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
	 * <p>
	 * If the computed partition is needed, use {@link #findPartition(Graph)}. This function does not have any side
	 * effects on the graph object itself, namely it does not add the partition (if one exist) as vertex weights.
	 *
	 * @param  g the graph
	 * @return   {@code true} if the graph is bipartite, {@code false} otherwise
	 */
	public static boolean isBipartite(Graph g) {
		return findBipartitePartition0(g.indexGraph()) != null;
	}

	/**
	 * Find a bipartite partition of the given graph (if one exists).
	 * <p>
	 * This function does not have any side effects on the graph object itself, namely it does not add the partition (if
	 * one exist) as vertex weights. To add the partition as vertex weights, use {@link #findPartition(Graph, boolean)}.
	 *
	 * @param  g the graph
	 * @return   the bipartite partition of the graph if one exists
	 */
	public static Optional<VertexBiPartition> findPartition(Graph g) {
		return findPartition(g, false);
	}

	/**
	 * Find a bipartite partition of the given graph (if one exists), and optionally add the partition as vertex
	 * weights.
	 *
	 * @param  g                        the graph
	 * @param  addPartitionWeight       if {@code true}, add the partition (if exist) will be added to the graph as
	 *                                      vertex weights. If no valid bipartite partition is found, the graph will not
	 *                                      be modified. If vertex weights already exists in the graph with the key
	 *                                      {@link #VertexBiPartitionWeightKey}, they will be used as the partition.
	 * @return                          the bipartite partition of the graph if one exists
	 * @throws IllegalArgumentException if {@code addPartitionWeight} is {@code true} and the graph already has a non
	 *                                      boolean vertex weights with key {@link #VertexBiPartitionWeightKey}
	 */
	public static Optional<VertexBiPartition> findPartition(Graph g, boolean addPartitionWeight) {
		IndexGraph ig = g instanceof IndexGraph ? (IndexGraph) g : g.indexGraph();

		BitSet partition0 = findBipartitePartition0(ig);
		if (partition0 == null)
			return Optional.empty();

		WeightsBool partition = getOrCreateBoolWeights(ig, addPartitionWeight);
		for (int n = ig.vertices().size(), v = 0; v < n; v++)
			partition.set(v, partition0.get(v));

		VertexBiPartition partitionRes = new VertexBiPartitions.FromWeights(ig, partition);
		if (!(g instanceof IndexGraph)) {
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			partitionRes = new VertexBiPartitions.BiPartitionFromIndexBiPartition(partitionRes, viMap, eiMap);
		}
		return Optional.of(partitionRes);
	}

	private static BitSet findBipartitePartition0(IndexGraph g) {
		final int n = g.vertices().size();
		BitSet partition = new BitSet(n);
		if (n > 0) {
			IntPriorityQueue queue = new FIFOQueueIntNoReduce();
			BitSet visited = new BitSet(n);
			for (int start = 0; start < n; start++) {
				if (visited.get(start))
					continue;
				visited.set(start);
				queue.enqueue(start);
				partition.set(start, true);
				while (!queue.isEmpty()) {
					final int u = queue.dequeueInt();
					final boolean uSide = partition.get(u);
					for (EdgeIter eit = g.outEdges(start).iterator(); eit.hasNext();) {
						eit.nextInt();
						int v = eit.target();
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
						for (EdgeIter eit = g.inEdges(start).iterator(); eit.hasNext();) {
							eit.nextInt();
							int v = eit.source();
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
		}
		return partition;
	}

	private static WeightsBool getOrCreateBoolWeights(IndexGraph g, boolean addPartitionWeight) {
		if (addPartitionWeight)
			return Weights.createExternalEdgesWeights(g, boolean.class);
		Object existingPartition = g.getVerticesWeights(VertexBiPartitionWeightKey);
		if (existingPartition == null)
			return g.addVerticesWeights(VertexBiPartitionWeightKey, boolean.class);
		if (!(existingPartition instanceof WeightsBool))
			throw new IllegalArgumentException("found vertex weights with key '" + VertexBiPartitionWeightKey
					+ "' but it is not a boolean weights");
		return (WeightsBool) existingPartition;
	}

	/**
	 * Get the existing bipartite partition of the given graph (if one exists).
	 * <p>
	 * If a bipartite partition was computed on the graph and boolean vertex weights were added to it, the partition
	 * will be returned. Otherwise, an empty optional will be returned. This function does not compute a partition if it
	 * doesn't find an existing one.
	 * <p>
	 * Note that if the graph was modified after the bipartite partition was computed and added, it might be invalid and
	 * no checks are performed in this function to verify that it is still valid.
	 *
	 * @param  g                        the graph
	 * @return                          the bipartite partition of the graph if one exists
	 * @throws IllegalArgumentException if the graph has a non boolean vertex weights with key
	 *                                      {@link #VertexBiPartitionWeightKey}
	 */
	public static Optional<VertexBiPartition> getExistingPartition(Graph g) {
		IndexGraph ig = g instanceof IndexGraph ? (IndexGraph) g : g.indexGraph();
		Object existingPartition = g.getVerticesWeights(VertexBiPartitionWeightKey);
		if (existingPartition == null)
			return Optional.empty();
		if (!(existingPartition instanceof WeightsBool))
			throw new IllegalArgumentException("found vertex weights with key '" + VertexBiPartitionWeightKey
					+ "' but it is not a boolean weights");
		WeightsBool partition = (WeightsBool) existingPartition;

		VertexBiPartition partitionRes = new VertexBiPartitions.FromWeights(ig, partition);
		if (!(g instanceof IndexGraph)) {
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			partitionRes = new VertexBiPartitions.BiPartitionFromIndexBiPartition(partitionRes, viMap, eiMap);
		}
		return Optional.of(partitionRes);
	}

	/**
	 * The vertices weight key of the bipartite property.
	 * <p>
	 * The bipartite partition is usually represented as a vertex boolean weight keyed by this key. The weight of each
	 * vertex indicates to which of the two partitions it belongs to. Functions such as
	 * {@link #findPartition(Graph, boolean)} may attempt to find a valid bipartition of a graph, and if one is found,
	 * to store it as vertex weights with this key. When an algorithm accept a graph to operate on, it may check for
	 * such vertex weights, and if they exists it may assume the graph is bipartite and use them.
	 * <p>
	 * If a graph contains vertex weights with this key, the partition can be retrieved by
	 * {@link #getExistingPartition(Graph)}. But note that a bipartite partition may become invalid if the graph is
	 * modified after the vertex weights were added. Consider removing the vertex weights to avoid misleading
	 * algorithms.
	 */
	public static final String VertexBiPartitionWeightKey = "_bipartite_partition";

}
