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
package com.jgalgo.alg.path;

import java.util.Collection;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.IVertexPartition;
import com.jgalgo.alg.common.Path;
import com.jgalgo.alg.common.VertexPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Voronoi cells algorithm.
 *
 * <p>
 * Given a graph \(G=(V,E)\) and a set of sites \(S \subseteq V\), the Voronoi cells of \(S\) are the sets of vertices
 * that are closer to a site than to any other site. The Voronoi cells are a partition of the graph vertices, namely
 * each vertex is assigned to exactly one site.
 *
 * <p>
 * The distances and paths are directed from the sites to the vertices. If the other direction is needed, consider
 * passing a reversed view of the original graph by using {@link Graph#reverseView()}.
 *
 * <p>
 * If there are some vertices that are unreachable from any sites, the partition will contain an addition block with
 * index {@code siteNumber+1} that contains all these vertices.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    ShortestPathSingleSource
 * @see    IVertexPartition
 * @author Barak Ugav
 */
public interface VoronoiAlgo {

	/**
	 * Compute the Voronoi cells of a graph with respect to a set of sites and an edge weight function.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link VoronoiAlgo.IResult} object will be returned. In that case, its
	 * better to pass a {@link IntCollection} as {@code sites} and {@link IWeightFunction} as {@code w} to avoid
	 * boxing/unboxing.
	 *
	 * @param  <V>   the vertices type
	 * @param  <E>   the edges type
	 * @param  g     a graph
	 * @param  sites a set of sites
	 * @param  w     an edge weight function
	 * @return       the Voronoi cells of the sites
	 */
	<V, E> VoronoiAlgo.Result<V, E> computeVoronoiCells(Graph<V, E> g, Collection<V> sites, WeightFunction<E> w);

	/**
	 * A result object of {@link VoronoiAlgo} computation.
	 *
	 * <p>
	 * The result object is firstly a valid {@link IVertexPartition} of the graph. The partition is defined by the
	 * sites. Each 'block' contains all the vertices that are closer to the site of the block than to any other site. If
	 * some vertices are unreachable from any sites, the partition will contain an addition block with index
	 * {@code siteNumber+1} that contains all these vertices. The result object itself does not extends the
	 * {@link VertexPartition} interface, but the partition is accessible by the {@link #partition()} method.
	 *
	 * <p>
	 * In addition to holding a partition, the result object also contains the distance of each vertex from its site,
	 * and the shortest path from the sites to the vertices. Note that the direction of the distances and paths (in case
	 * of a directed graph) is from the sites to the vertices. If the other direction is needed, consider passing a
	 * reversed view of the original graph by using {@link Graph#reverseView()}.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @see        Path
	 * @author     Barak Ugav
	 */
	static interface Result<V, E> {

		/**
		 * Get the partition of the graph to blocks where each block contains the vertices which are closest to a single
		 * site.
		 *
		 * @return the partition of the graph to sites blocks
		 */
		VertexPartition<V, E> partition();

		/**
		 * Get the distance of a vertex from its site.
		 *
		 * <p>
		 * Note that the direction of the distances and paths (in case of a directed graph) is from the sites to the
		 * vertices.
		 *
		 * @param  vertex a target vertex
		 * @return        the shortest distance from any site to the target vertex, or {@link Double#POSITIVE_INFINITY}
		 *                if the target vertex is unreachable from any site
		 */
		double distance(V vertex);

		/**
		 * Get the shortest path of a vertex from its site.
		 *
		 * <p>
		 * Note that the direction of the distances and paths (in case of a directed graph) is from the sites to the
		 * vertices.
		 *
		 * @param  target a target vertex
		 * @return        the shortest path from any site to the target vertex, or {@code null} if the target vertex is
		 *                unreachable from any site
		 */
		Path<V, E> getPath(V target);

		/**
		 * Get the site vertex of a block.
		 *
		 * <p>
		 * The Voronoi cells are defined by the sites. Each 'block' contains all the vertices that are closer to the
		 * site of the block than to any other site. This method return the site vertex of one of the blocks
		 * {@code [0, numberOfBlocks())}.
		 *
		 * <p>
		 * In case some vertices are unreachable from any sites, the partition will contain an addition block with index
		 * {@code siteNumber+1} that contains all these vertices. This method will return {@code null} for this block.
		 *
		 * @param  block index of a block
		 * @return       the site vertex of the block, or {@code null} if the block is the unreachable block
		 */
		V blockSite(int block);

		/**
		 * Get the site vertex of a vertex.
		 *
		 * <p>
		 * The Voronoi cells are defined by the sites. Each 'block' contains all the vertices that are closer to the
		 * site of the block than to any other site. This method return the site vertex of the block that contains the
		 * vertex, namely the site with the shortest path from any site to the vertex.
		 *
		 * @param  vertex a vertex
		 * @return        the site vertex with the shortest path from any site to the vertex, or {@code null} if the
		 *                block is the unreachable block
		 */
		V vertexSite(V vertex);
	}

	/**
	 * A result object of {@link VoronoiAlgo} computation for {@link IntGraph}.
	 *
	 * <p>
	 * See {@link VoronoiAlgo.Result} for the result object documentation.
	 *
	 * @author Barak Ugav
	 */
	static interface IResult extends VoronoiAlgo.Result<Integer, Integer> {

		@Override
		IVertexPartition partition();

		/**
		 * Get the distance of a vertex from its site.
		 *
		 * <p>
		 * Note that the direction of the distances and paths (in case of a directed graph) is from the sites to the
		 * vertices.
		 *
		 * @param  vertex a target vertex
		 * @return        the shortest distance from any site to the target vertex, or {@link Double#POSITIVE_INFINITY}
		 *                if the target vertex is unreachable from any site
		 */
		double distance(int vertex);

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #distance(int)} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default double distance(Integer vertex) {
			return distance(vertex.intValue());
		}

		/**
		 * Get the shortest path of a vertex from its site.
		 *
		 * <p>
		 * Note that the direction of the distances and paths (in case of a directed graph) is from the sites to the
		 * vertices.
		 *
		 * @param  target a target vertex
		 * @return        the shortest path from any site to the target vertex, or {@code null} if the target vertex is
		 *                unreachable from any site
		 */
		IPath getPath(int target);

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #getPath(int)} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default IPath getPath(Integer target) {
			return getPath(target.intValue());
		}

		/**
		 * Get the site vertex of a block.
		 *
		 * <p>
		 * The Voronoi cells are defined by the sites. Each 'block' contains all the vertices that are closer to the
		 * site of the block than to any other site. This method return the site vertex of one of the blocks
		 * {@code [0, numberOfBlocks())}.
		 *
		 * <p>
		 * In case some vertices are unreachable from any sites, the partition will contain an addition block with index
		 * {@code siteNumber+1} that contains all these vertices. This method will return {@code -1} for this block.
		 *
		 * @param  block index of a block
		 * @return       the site vertex of the block, or {@code -1} if the block is the unreachable block
		 */
		int blockSiteInt(int block);

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #blockSiteInt(int)} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default Integer blockSite(int block) {
			int site = blockSiteInt(block);
			return site < 0 ? null : Integer.valueOf(site);
		}

		/**
		 * Get the site vertex of a vertex.
		 *
		 * <p>
		 * The Voronoi cells are defined by the sites. Each 'block' contains all the vertices that are closer to the
		 * site of the block than to any other site. This method return the site vertex of the block that contains the
		 * vertex, namely the site with the shortest path from any site to the vertex.
		 *
		 * @param  vertex a vertex
		 * @return        the site vertex with the shortest path from any site to the vertex
		 */
		int vertexSite(int vertex);

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #vertexSite(int)} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default Integer vertexSite(Integer vertex) {
			int site = vertexSite(vertex.intValue());
			return site < 0 ? null : Integer.valueOf(site);
		}
	}

	/**
	 * Create a new Voronoi cells algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link VoronoiAlgo} object.
	 *
	 * @return a default implementation of {@link VoronoiAlgo}
	 */
	static VoronoiAlgo newInstance() {
		return new VoronoiAlgoDijkstra();
	}

}
