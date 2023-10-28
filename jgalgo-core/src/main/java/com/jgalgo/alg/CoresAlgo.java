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
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Cores computing algorithm.
 * <p>
 * Given a graph \(G=(V,E)\), a subgraph \(H\) induced by a subset of vertices \(W\) is a \(k\)-core or a core of order
 * \(k\) if \(\forall v \in W : deg_H(v) \geq k\) and \(H\) is a maximum subgraph with this property. The core number of
 * vertex is the highest order of a core that contains this vertex. The degree \(deg(v)\) can be: in-degree, out-degree,
 * in-degree + out-degree, determining different types of cores.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @see    CoresAlgo.DegreeType
 * @author Barak Ugav
 */
public interface CoresAlgo {

	/**
	 * Compute the cores of the graph with respect to both in and out degree of the vertices.
	 * <p>
	 * For a detail description of the cores definition, see the interface documentation {@link CoresAlgo}.
	 *
	 * @param  g a graph
	 * @return   the cores of the graph
	 */
	default CoresAlgo.Result computeCores(Graph g) {
		return computeCores(g, DegreeType.OutAndInDegree);
	}

	/**
	 * Compute the cores of the graph with respect the given degree type.
	 * <p>
	 * Cores are defined with respect to either the out edges, in edges, or both. For undirected graphs the degree type
	 * is ignored.
	 * <p>
	 * For a detail description of the cores definition, see the interface documentation {@link CoresAlgo}.
	 *
	 * @param  g          a graph
	 * @param  degreeType the degree type the cores are computed with respect to
	 * @return            the cores of the graph
	 */
	CoresAlgo.Result computeCores(Graph g, DegreeType degreeType);

	/**
	 * The degree type the cores are defined with respect to.
	 * <p>
	 * A \(k\)-core is a maximal set of vertices such that the graph induced by the set has minimum degree \(k\).
	 * Different types of degrees can be considered, yielding different types of cores. For undirected graphs the degree
	 * type has no effect.
	 *
	 * @see    CoresAlgo
	 * @see    CoresAlgo#computeCores(Graph, DegreeType)
	 * @author Barak Ugav
	 */
	static enum DegreeType {
		/**
		 * Cores will be computed with respect to the out-degree of the vertices, namely the number of outgoing edges.
		 */
		OutDegree,

		/**
		 * Cores will be computed with respect to the in-degree of the vertices, namely the number of incoming edges.
		 */
		InDegree,

		/**
		 * Cores will be computed with respect to the sum of the in and out degrees of the vertices.
		 */
		OutAndInDegree
	}

	/**
	 * The result of the cores computation.
	 *
	 * @author Barak Ugav
	 */
	static interface Result {

		/**
		 * The core number of the given vertex.
		 * <p>
		 * The core number of a vertex is the highest order of a core that contains this vertex.
		 *
		 * @param  v a vertex in the graph
		 * @return   the core number of the vertex
		 */
		int vertexCoreNum(int v);

		/**
		 * The maximum core number of the graph.
		 *
		 * @return the maximum core number of the graph
		 */
		int maxCore();

		/**
		 * The vertices of the given core.
		 * <p>
		 * A vertex is in the core if its core number is at least the given core number.
		 *
		 * @param  k the core number (order)
		 * @return   the vertices of the core
		 */
		IntSet coreVertices(int k);

		/**
		 * The vertices in the shell of the given core.
		 * <p>
		 * A vertex is in the shell of the core if its core number is exactly the given core number. Namely it is in the
		 * k core but not in the (k+1) core.
		 *
		 * @param  core the core number (order)
		 * @return      the vertices in the shell of the core
		 */
		IntSet coreShell(int core);

		/**
		 * The vertices in the crust of the given core.
		 * <p>
		 * A vertex is in the crust of the core if its core number is less than the given core. The crust is the
		 * complement of the core vertices set.
		 *
		 * @param  core the core number (order)
		 * @return      the vertices in the crust of the core
		 */
		IntSet coreCrust(int core);

	}

	/**
	 * Create a new cores algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link CoresAlgo} object. The {@link CoresAlgo.Builder} might
	 * support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link CoresAlgo}
	 */
	static CoresAlgo newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new builder for core algorithms.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder for core algorithms
	 */
	static CoresAlgo.Builder newBuilder() {
		return CoresAlgoImpl::new;
	}

	/**
	 * A builder for {@link CoresAlgo} objects.
	 *
	 * @see    CoresAlgo#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Build a new {@link CoresAlgo} object.
		 *
		 * @return a new {@link CoresAlgo} object
		 */
		CoresAlgo build();

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
		default CoresAlgo.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
