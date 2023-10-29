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

import java.util.Objects;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IWeightFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;

class VoronoiAlgos {

	static abstract class AbstractImpl implements VoronoiAlgo {

		@Override
		public VoronoiAlgo.Result computeVoronoiCells(IntGraph g, IntCollection sites, IWeightFunction w) {
			if (g instanceof IndexGraph)
				return computeVoronoiCells((IndexGraph) g, sites, w);

			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();
			IntCollection iSites = IndexIdMaps.idToIndexCollection(sites, viMap);
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);

			VoronoiAlgo.Result indexResult = computeVoronoiCells(iGraph, iSites, iw);
			return new ResultFromIndexResult(g, indexResult);
		}

		abstract VoronoiAlgo.Result computeVoronoiCells(IndexGraph g, IntCollection sites, IWeightFunction w);

	}

	static class ResultImpl extends VertexPartitions.Impl implements VoronoiAlgo.Result {

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
		public Path getPath(int target) {
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
			return new PathImpl(g, site, target, path);
		}

		@Override
		public int blockSite(int block) {
			return block < sites.length ? sites[block] : -1;
		}

		@Override
		public int vertexSite(int vertex) {
			int siteIdx = vertexBlock(vertex);
			return siteIdx < sites.length ? sites[siteIdx] : -1;
		}

	}

	static class ResultFromIndexResult extends VertexPartitions.PartitionFromIndexPartition
			implements VoronoiAlgo.Result {

		ResultFromIndexResult(IntGraph g, VoronoiAlgo.Result res) {
			super(g, res);
		}

		VoronoiAlgo.Result res() {
			return (VoronoiAlgo.Result) super.res;
		}

		@Override
		public double distance(int vertex) {
			return res().distance(viMap.idToIndex(vertex));
		}

		@Override
		public Path getPath(int target) {
			Path indexPath = res().getPath(viMap.idToIndex(target));
			return PathImpl.pathFromIndexPath(indexPath, viMap, eiMap);
		}

		@Override
		public int blockSite(int block) {
			int site = res().blockSite(block);
			return site != -1 ? viMap.indexToIdInt(site) : -1;
		}

		@Override
		public int vertexSite(int vertex) {
			int site = res().vertexSite(viMap.idToIndex(vertex));
			return site != -1 ? viMap.indexToIdInt(site) : -1;
		}

	}

}
