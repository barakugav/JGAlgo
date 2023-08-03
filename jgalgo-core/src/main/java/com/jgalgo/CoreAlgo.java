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
package com.jgalgo;

import com.jgalgo.graph.Graph;
import com.jgalgo.internal.util.BuilderAbstract;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Cores computing algorithm.
 * <p>
 * Given a graph \(G=(V,E)\), a subgraph \(H\) induced by a subset of vertices \(W\) is a \(k\)-core or a core of order
 * \(k\) if \(\forall v \in W : deg_H(v) \geq k\) and \(H\) is a maximum subgraph with this property. The core number of
 * vertex is the highest order of a core that contains this vertex. The degree \(deg(v)\) can be: in-degree, out-degree,
 * in-degree + out-degree, determining different types of cores.
 *
 * @see    CoreAlgo.DegreeType
 * @author Barak Ugav
 */
public interface CoreAlgo {

	/**
	 * Compute the cores of the graph with respect to both in and out degree of the vertices.
	 * <p>
	 * For a detail description of the cores definition, see the interface documentation {@link CoreAlgo}.
	 *
	 * @param  g a graph
	 * @return   the cores of the graph
	 */
	default CoreAlgo.Result computeCores(Graph g) {
		return computeCores(g, DegreeType.OutAndInDegree);
	}

	/**
	 * Compute the cores of the graph with respect the given degree type.
	 * <p>
	 * Cores are defined with respect to either the out edges, in edges, or both. For undirected graphs the degree type
	 * is ignored.
	 * <p>
	 * For a detail description of the cores definition, see the interface documentation {@link CoreAlgo}.
	 *
	 * @param  g          a graph
	 * @param  degreeType the degree type the cores are computed with respect to
	 * @return            the cores of the graph
	 */
	CoreAlgo.Result computeCores(Graph g, DegreeType degreeType);

	/**
	 * The degree type the cores are defined with respect to.
	 * <p>
	 * A \(k\)-core is a maximal set of vertices such that the graph induced by the set has minimum degree \(k\).
	 * Different types of degrees can be considered, yielding different types of cores. For undirected graphs the degree
	 * type has no effect.
	 *
	 * @see    CoreAlgo
	 * @see    CoreAlgo#computeCores(Graph, DegreeType)
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
		 *
		 * @param  core the core number (order)
		 * @return      the vertices of the core
		 */
		IntCollection coreVertices(int core);

	}

	/**
	 * Create a new builder for core algorithms.
	 *
	 * @return a new builder for core algorithms
	 */
	static CoreAlgo.Builder newBuilder() {
		return CoreAlgoImpl::new;
	}

	/**
	 * A builder for {@link CoreAlgo} objects.
	 *
	 * @see    CoreAlgo#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<CoreAlgo.Builder> {

		/**
		 * Build a new {@link CoreAlgo} object.
		 *
		 * @return a new {@link CoreAlgo} object
		 */
		CoreAlgo build();

	}

}
