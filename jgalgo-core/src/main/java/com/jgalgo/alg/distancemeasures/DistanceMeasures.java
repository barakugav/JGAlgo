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
package com.jgalgo.alg.distancemeasures;

import java.util.Set;
import com.jgalgo.alg.shortestpath.ShortestPathAllPairs;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightFunction;

/**
 * A set of graph distance measures.
 *
 * <p>
 * Given a graph \(G=(V,E)\), and an edge weight function \(w:E \rightarrow \mathbb{R}\), the eccentricity of a vertex
 * \(v \in V\) is the maximum distance from \(v\) to any other vertex in the graph. Multiple measures can be derived
 * from the eccentricity of the vertices, such as {@linkplain #radius() radius}, {@linkplain #diameter() diameter},
 * {@linkplain #center() center}, and {@linkplain #periphery() periphery}. A {@link DistanceMeasures} object can be
 * created for a graph using {@link #of(Graph, WeightFunction)}, and all these measures will be computed (possible
 * lazily).
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
@SuppressWarnings("unused")
public interface DistanceMeasures<V, E> {

	/**
	 * Get the radius of the graph.
	 *
	 * <p>
	 * Given a graph \(G=(V,E)\), and an edge weight function \(w:E \rightarrow \mathbb{R}\), the eccentricity of a
	 * vertex \(v \in V\) is the maximum distance from \(v\) to any other vertex in the graph. The raidus of the graph
	 * is the minimum eccentricity of all vertices in the graph. If the graph is directed, the eccentricity of a vertex
	 * \(v \in V\) is the maximum <i>directed</i> distance from \(v\) to any other vertex in the graph.
	 *
	 * <p>
	 * If the graph is not (strongly) connected, the radius is {@link Double#POSITIVE_INFINITY}.
	 *
	 * @return the radius of the graph
	 */
	double radius();

	/**
	 * Get the diameter of the graph.
	 *
	 * <p>
	 * Given a graph \(G=(V,E)\), and an edge weight function \(w:E \rightarrow \mathbb{R}\), the eccentricity of a
	 * vertex \(v \in V\) is the maximum distance from \(v\) to any other vertex in the graph. The diameter of the graph
	 * is the maximum eccentricity of all vertices in the graph. In other words, it is the maximum distance between two
	 * vertices. If the graph is directed, the eccentricity of a vertex \(v \in V\) is the maximum <i>directed</i>
	 * distance from \(v\) to any other vertex in the graph.
	 *
	 * <p>
	 * If the graph is not (strongly) connected, the diameter is {@link Double#POSITIVE_INFINITY}.
	 *
	 * @return the diameter of the graph
	 */
	double diameter();

	/**
	 * Get the eccentricity of a vertex.
	 *
	 * <p>
	 * Given a graph \(G=(V,E)\), and an edge weight function \(w:E \rightarrow \mathbb{R}\), the eccentricity of a
	 * vertex \(v \in V\) is the maximum distance from \(v\) to any other vertex in the graph. If the graph is directed,
	 * the eccentricity of a vertex \(v \in V\) is the maximum <i>directed</i> distance from \(v\) to any other vertex
	 * in the graph.
	 *
	 * <p>
	 * If there is a vertex \(u \in V\) that is not reachable from \(v\), the eccentricity of \(v\) is
	 * {@link Double#POSITIVE_INFINITY}.
	 *
	 * @param  v                     a vertex
	 * @return                       the eccentricity of the vertex
	 * @throws NoSuchVertexException if {@code v} is not a valid vertex identifier in the graph
	 */
	double eccentricity(V v);

	/**
	 * Get the center of the graph.
	 *
	 * <p>
	 * Given a graph \(G=(V,E)\), and an edge weight function \(w:E \rightarrow \mathbb{R}\), the eccentricity of a
	 * vertex \(v \in V\) is the maximum distance from \(v\) to any other vertex in the graph. The raidus of the graph
	 * is the minimum eccentricity of all vertices in the graph. The center of the graph is the set of vertices with
	 * eccentricity equal to the radius.
	 *
	 * @return the set of vertices forming the center of the graph
	 */
	Set<V> center();

	/**
	 * Get the periphery of the graph.
	 *
	 * <p>
	 * Given a graph \(G=(V,E)\), and an edge weight function \(w:E \rightarrow \mathbb{R}\), the eccentricity of a
	 * vertex \(v \in V\) is the maximum distance from \(v\) to any other vertex in the graph. The diameter of the graph
	 * is the maximum eccentricity of all vertices in the graph. The periphery of the graph is the set of vertices with
	 * eccentricity equal to the diameter.
	 *
	 * <p>
	 * If the graph is not (strongly) connected, the periphery is the set of all the vertices as all the vertices
	 * eccentricity and the diameter are {@link Double#POSITIVE_INFINITY}.
	 *
	 * @return the set of vertices forming the periphery of the graph
	 */
	Set<V> periphery();

	/**
	 * Get a distance measures object of a graph.
	 *
	 * <p>
	 * If {@code g} is and {@link IntGraph}, the return object is of type {@link IDistanceMeasures}.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   the graph
	 * @param  w   the edge weight function
	 * @return     a distance measures object of the graph
	 */
	@SuppressWarnings("unchecked")
	static <V, E> DistanceMeasures<V, E> of(Graph<V, E> g, WeightFunction<E> w) {
		ShortestPathAllPairs apspAlgo = ShortestPathAllPairs.newInstance();
		if (g instanceof IndexGraph) {
			ShortestPathAllPairs.IResult sp = (ShortestPathAllPairs.IResult) apspAlgo.computeAllShortestPaths(g, w);
			return (DistanceMeasures<V, E>) new DistanceMeasuresImpl.IndexImpl((IndexGraph) g, sp);

		} else {
			IndexGraph ig = g.indexGraph();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			ShortestPathAllPairs.IResult sp = (ShortestPathAllPairs.IResult) apspAlgo.computeAllShortestPaths(ig, iw);
			IDistanceMeasures indexMeasures = new DistanceMeasuresImpl.IndexImpl(ig, sp);
			return DistanceMeasuresImpl.measuresFromIndexMeasures(g, indexMeasures);
		}
	}

}
