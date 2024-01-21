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

import java.util.Collection;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;

class VoronoiAlgos {

	abstract static class AbstractImpl implements VoronoiAlgo {

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

		abstract VoronoiAlgo.IResult computeVoronoiCells(IndexGraph g, IntCollection sites, IWeightFunction w);

	}

	static class ResultImpl extends VertexPartitions.Impl implements VoronoiAlgo.IResult {

		private final double[] distance;
		private final int[] backtrack;
		private final int[] sites;

		ResultImpl(IndexGraph g, int blockNum, int[] vertexToBlock, double[] distance, int[] backtrack, int[] sites) {
			super(g, blockNum, vertexToBlock);
			this.distance = Objects.requireNonNull(distance);
			this.backtrack = Objects.requireNonNull(backtrack);
			this.sites = Objects.requireNonNull(sites);
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
			if (g.isDirected()) {
				for (int v = target;;) {
					int e = backtrack[v];
					if (e == -1) {
						site = v;
						break;
					}
					path.add(e);
					v = g.edgeSource(e);
				}
			} else {
				for (int v = target;;) {
					int e = backtrack[v];
					if (e == -1) {
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
			int siteIdx = vertexBlock(vertex);
			return siteIdx < sites.length ? sites[siteIdx] : -1;
		}

	}

	static class ObjResultFromIndexResult<V, E> extends VertexPartitions.ObjPartitionFromIndexPartition<V, E>
			implements VoronoiAlgo.Result<V, E> {

		ObjResultFromIndexResult(Graph<V, E> g, VoronoiAlgo.IResult indexRes) {
			super(g, indexRes);
		}

		VoronoiAlgo.IResult indexRes() {
			return (VoronoiAlgo.IResult) super.indexPartition;
		}

		@Override
		public double distance(V vertex) {
			return indexRes().distance(viMap.idToIndex(vertex));
		}

		@Override
		public Path<V, E> getPath(V target) {
			IPath indexPath = indexRes().getPath(viMap.idToIndex(target));
			return Paths.objPathFromIndexPath(graph(), indexPath);
		}

		@Override
		public V blockSite(int block) {
			int site = indexRes().blockSiteInt(block);
			return viMap.indexToIdIfExist(site);
		}

		@Override
		public V vertexSite(V vertex) {
			int site = indexRes().vertexSite(viMap.idToIndex(vertex));
			return viMap.indexToIdIfExist(site);
		}
	}

	static class IntResultFromIndexResult extends VertexPartitions.IntPartitionFromIndexPartition
			implements VoronoiAlgo.IResult {

		IntResultFromIndexResult(IntGraph g, VoronoiAlgo.IResult indexRes) {
			super(g, indexRes);
		}

		VoronoiAlgo.IResult indexRes() {
			return (VoronoiAlgo.IResult) super.indexPartition;
		}

		@Override
		public double distance(int vertex) {
			return indexRes().distance(viMap.idToIndex(vertex));
		}

		@Override
		public IPath getPath(int target) {
			IPath indexPath = indexRes().getPath(viMap.idToIndex(target));
			return Paths.intPathFromIndexPath(graph(), indexPath);
		}

		@Override
		public int blockSiteInt(int block) {
			int site = indexRes().blockSiteInt(block);
			return viMap.indexToIdIfExistInt(site);
		}

		@Override
		public int vertexSite(int vertex) {
			int site = indexRes().vertexSite(viMap.idToIndex(vertex));
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
