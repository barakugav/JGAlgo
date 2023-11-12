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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;

/**
 * Eulerian tour calculation algorithm.
 *
 * <p>
 * An Eulerian tour is a tour that visits every edge exactly once (allowing for revisiting vertices). For a connected
 * undirected graph, if all vertices have an even degree, an Eulerian cycle will be found. If exactly two vertices have
 * an odd degree, called \(s,t\), an Eulerian tour that start at \(s\) and ends at \(t\) exists. For any other vertices
 * degrees an Eulerian tour does not exists. For a strongly connected directed graph, the in-degree and out-degree of
 * each vertex must be equal for an Eulerian cycle to exists. If exactly one vertex \(s\) has one more out-edge than
 * in-edges, and one vertex \(t\) has one more in-edge than out-edges, an Eulerian tour that start at \(s\) and ends at
 * \(t\) exists.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Eulerian_path">Wikipedia</a>
 * @author Barak Ugav
 */
public interface EulerianTourAlgo {

	/**
	 * Compute an Eulerian tour in the graph that visit all edges exactly once.
	 *
	 * <p>
	 * The graph is assumed to be (strongly) connected. Either a cycle or tour will be found, depending on the vertices
	 * degrees.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned object is {@link IPath}.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a graph
	 * @return                          an Eulerian tour that visit all edges of the graph exactly once
	 * @throws IllegalArgumentException if there is no Eulerian tour in the graph
	 */
	public <V, E> Path<V, E> computeEulerianTour(Graph<V, E> g);

	/**
	 * Create a new Eulerian tour computation algorithm.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link EulerianTourAlgo} object. The
	 * {@link EulerianTourAlgo.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link EulerianTourAlgo}
	 */
	static EulerianTourAlgo newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new Eulerian tour algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link EulerianTourAlgo} objects
	 */
	static EulerianTourAlgo.Builder newBuilder() {
		return EulerianTourImpl::new;
	}

	/**
	 * A builder for {@link EulerianTourAlgo} objects.
	 *
	 * @see    EulerianTourAlgo#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new algorithm object for Eulerian tours computation.
		 *
		 * @return a new Eulerian tour algorithm
		 */
		EulerianTourAlgo build();
	}

}
