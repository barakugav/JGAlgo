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

import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IWeightFunction;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Voronoi cells algorithm.
 * <p>
 * Given a graph \(G=(V,E)\) and a set of sites \(S \subseteq V\), the Voronoi cells of \(S\) are the sets of vertices
 * that are closer to a site than to any other site. The Voronoi cells are a partition of the graph vertices, namely
 * each vertex is assigned to exactly one site.
 * <p>
 * The distances and paths are directed from the sites to the vertices. If the other direction is needed, consider
 * passing a reversed view of the original graph by using {@link IntGraph#reverseView()}.
 * <p>
 * If there are some vertices that are unreachable from any sites, the partition will contain an addition block with
 * index {@code siteNumber+1} that contains all these vertices.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @see    ShortestPathSingleSource
 * @see    VertexPartition
 * @author Barak Ugav
 */
public interface VoronoiAlgo {

	/**
	 * Compute the Voronoi cells of a graph with respect to a set of sites and an edge weight function.
	 *
	 * @param  g     a graph
	 * @param  sites a set of sites
	 * @param  w     an edge weight function
	 * @return       the Voronoi cells of the sites
	 */
	VoronoiAlgo.Result computeVoronoiCells(IntGraph g, IntCollection sites, IWeightFunction w);

	/**
	 * A result object of {@link VoronoiAlgo} computation.
	 * <p>
	 * The result object is firstly a valid {@link VertexPartition} of the graph. The partition is defined by the sites.
	 * Each 'block' contains all the vertices that are closer to the site of the block than to any other site. If some
	 * vertices are unreachable from any sites, the partition will contain an addition block with index
	 * {@code siteNumber+1} that contains all these vertices.
	 * <p>
	 * In addition to being a partition, the result object also contains the distance of each vertex from its site, and
	 * the shortest path from the sites to the vertices. Note that the direction of the distances and paths (in case of
	 * a directed graph) is from the sites to the vertices. If the other direction is needed, consider passing a
	 * reversed view of the original graph by using {@link IntGraph#reverseView()}.
	 *
	 * @see    Path
	 * @author Barak Ugav
	 */
	static interface Result extends VertexPartition {

		/**
		 * Get the distance of a vertex from its site.
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
		 * Get the shortest path of a vertex from its site.
		 * <p>
		 * Note that the direction of the distances and paths (in case of a directed graph) is from the sites to the
		 * vertices.
		 *
		 * @param  target a target vertex
		 * @return        the shortest path from any site to the target vertex, or {@code null} if the target vertex is
		 *                unreachable from any site
		 */
		Path getPath(int target);

		/**
		 * Get the site vertex of a block.
		 * <p>
		 * The Voronoi cells are defined by the sites. Each 'block' contains all the vertices that are closer to the
		 * site of the block than to any other site. This method return the site vertex of one of the blocks
		 * {@code [0, numberOfBlocks())}.
		 * <p>
		 * In case some vertices are unreachable from any sites, the partition will contain an addition block with index
		 * {@code siteNumber+1} that contains all these vertices. This method will return {@code -1} for this block.
		 *
		 * @param  block index of a block
		 * @return       the site vertex of the block, or {@code -1} if the block is the unreachable block
		 */
		int blockSite(int block);

		/**
		 * Get the site vertex of a vertex.
		 * <p>
		 * The Voronoi cells are defined by the sites. Each 'block' contains all the vertices that are closer to the
		 * site of the block than to any other site. This method return the site vertex of the block that contains the
		 * vertex, namely the site with the shortest path from any site to the vertex.
		 *
		 * @param  vertex a vertex
		 * @return        the site vertex with the shortest path from any site to the vertex
		 */
		int vertexSite(int vertex);

	}

	/**
	 * Create a new Voronoi cells algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link VoronoiAlgo} object. The {@link VoronoiAlgo.Builder}
	 * might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link VoronoiAlgo}
	 */
	static VoronoiAlgo newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new Voronoi cells algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link VoronoiAlgo} objects
	 */
	static VoronoiAlgo.Builder newBuilder() {
		return VoronoiAlgoDijkstra::new;
	}

	/**
	 * A builder for {@link VoronoiAlgo} algorithms.
	 *
	 * @see    VoronoiAlgo#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for Voronoi cells computation.
		 *
		 * @return a new Voronoi cells algorithm
		 */
		VoronoiAlgo build();

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default VoronoiAlgo.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
