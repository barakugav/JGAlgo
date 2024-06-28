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
package com.jgalgo.alg.shortestpath;

import java.util.Collection;
import java.util.Objects;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.IVertexPartition;
import com.jgalgo.alg.common.Path;
import com.jgalgo.alg.common.VertexPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Abstract class for computing the Voronoi cells in a graph given a set of site vertices.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class VoronoiAlgoAbstract implements VoronoiAlgo {

	/**
	 * Default constructor.
	 */
	public VoronoiAlgoAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> VoronoiAlgo.Result<V, E> computeVoronoiCells(Graph<V, E> g, Collection<V> sites,
			WeightFunction<E> w) {
		if (g instanceof IndexGraph) {
			IntCollection sites0 = IntAdapters.asIntCollection((Collection<Integer>) sites);
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			return (VoronoiAlgo.Result<V, E>) computeVoronoiCells((IndexGraph) g, sites0, w0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IntCollection iSites = IndexIdMaps.idToIndexCollection(sites, viMap);
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			VoronoiAlgo.IResult indexResult = computeVoronoiCells(iGraph, iSites, iw);
			return resultFromIndexResult(g, indexResult);
		}
	}

	protected abstract VoronoiAlgo.IResult computeVoronoiCells(IndexGraph g, IntCollection sites, IWeightFunction w);

	/**
	 * A result object for the Voronoi cells computation in an index graph.
	 *
	 * @author Barak Ugav
	 */
	protected static class IndexResult implements VoronoiAlgo.IResult {

		private final IVertexPartition partition;
		private final double[] distance;
		private final int[] backtrack;
		private final int[] sites;

		/**
		 * Create a new result object for the Voronoi cells computation in an index graph.
		 *
		 * <p>
		 * All the arguments are NOT CLONED and are used as is. The caller should not use these arrays after creating
		 * the result object.
		 *
		 * @param g             the index graph
		 * @param vertexToBlock an array mapping each vertex (index) to its block index
		 * @param blockNum      the number of blocks
		 * @param distance      an array mapping vertices (indices) to their distance from their closest site
		 * @param backtrack     an array mapping vertices (indices) to the edge that led their closest site to them
		 * @param sites         an array mapping block indices to their site vertex (index)
		 */
		public IndexResult(IndexGraph g, int[] vertexToBlock, int blockNum, double[] distance, int[] backtrack,
				int[] sites) {
			partition = IVertexPartition.fromArray(g, vertexToBlock, blockNum);
			this.distance = Objects.requireNonNull(distance);
			this.backtrack = Objects.requireNonNull(backtrack);
			this.sites = Objects.requireNonNull(sites);
		}

		@Override
		public IVertexPartition partition() {
			return partition;
		}

		@Override
		public double distance(int vertex) {
			return distance[vertex];
		}

		@Override
		public IPath getPath(int target) {
			if (distance[target] == Double.POSITIVE_INFINITY)
				return null;
			IntArrayList path = new IntArrayList();
			int site;
			IndexGraph g = (IndexGraph) partition.graph();
			if (g.isDirected()) {
				for (int v = target;;) {
					int e = backtrack[v];
					if (e < 0) {
						site = v;
						break;
					}
					path.add(e);
					v = g.edgeSource(e);
				}
			} else {
				for (int v = target;;) {
					int e = backtrack[v];
					if (e < 0) {
						site = v;
						break;
					}
					path.add(e);
					v = g.edgeEndpoint(e, v);
				}
			}
			IntArrays.reverse(path.elements(), 0, path.size());
			return IPath.valueOf(g, site, target, path);
		}

		@Override
		public int blockSiteInt(int block) {
			return block < sites.length ? sites[block] : -1;
		}

		@Override
		public int vertexSite(int vertex) {
			int siteIdx = partition.vertexBlock(vertex);
			return siteIdx < sites.length ? sites[siteIdx] : -1;
		}
	}

	private static class ObjResultFromIndexResult<V, E> implements VoronoiAlgo.Result<V, E> {

		private final VoronoiAlgo.IResult indexRes;
		private final IndexIdMap<V> viMap;
		private final VertexPartition<V, E> partition;

		ObjResultFromIndexResult(Graph<V, E> g, VoronoiAlgo.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			viMap = g.indexGraphVerticesMap();
			partition = VertexPartition.partitionFromIndexPartition(g, indexRes.partition());
		}

		@Override
		public VertexPartition<V, E> partition() {
			return partition;
		}

		@Override
		public double distance(V vertex) {
			return indexRes.distance(viMap.idToIndex(vertex));
		}

		@Override
		public Path<V, E> getPath(V target) {
			IPath indexPath = indexRes.getPath(viMap.idToIndex(target));
			return Path.pathFromIndexPath(partition.graph(), indexPath);
		}

		@Override
		public V blockSite(int block) {
			int site = indexRes.blockSiteInt(block);
			return viMap.indexToIdIfExist(site);
		}

		@Override
		public V vertexSite(V vertex) {
			int site = indexRes.vertexSite(viMap.idToIndex(vertex));
			return viMap.indexToIdIfExist(site);
		}
	}

	private static class IntResultFromIndexResult implements VoronoiAlgo.IResult {

		private final VoronoiAlgo.IResult indexRes;
		private final IndexIntIdMap viMap;
		private final IVertexPartition partition;

		IntResultFromIndexResult(IntGraph g, VoronoiAlgo.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			viMap = g.indexGraphVerticesMap();
			partition = (IVertexPartition) VertexPartition.partitionFromIndexPartition(g, indexRes.partition());
		}

		@Override
		public IVertexPartition partition() {
			return partition;
		}

		@Override
		public double distance(int vertex) {
			return indexRes.distance(viMap.idToIndex(vertex));
		}

		@Override
		public IPath getPath(int target) {
			IPath indexPath = indexRes.getPath(viMap.idToIndex(target));
			return (IPath) Path.pathFromIndexPath(partition.graph(), indexPath);
		}

		@Override
		public int blockSiteInt(int block) {
			int site = indexRes.blockSiteInt(block);
			return viMap.indexToIdIfExistInt(site);
		}

		@Override
		public int vertexSite(int vertex) {
			int site = indexRes.vertexSite(viMap.idToIndex(vertex));
			return viMap.indexToIdIfExistInt(site);
		}
	}

	@SuppressWarnings("unchecked")
	private static <V, E> VoronoiAlgo.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			VoronoiAlgo.IResult indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (VoronoiAlgo.Result<V, E>) new IntResultFromIndexResult((IntGraph) g, indexResult);
		} else {
			return new ObjResultFromIndexResult<>(g, indexResult);
		}
	}

}
