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

import java.util.Iterator;
import java.util.List;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * An algorithm that enumerate over simple paths between a source and a target.
 *
 * <p>
 * Given a graph \(G=(V,E)\), a path is a sequence of edges \(e_1,e_2,\ldots,e_k\) such that \(e_i=(v_{i-1},v_i)\) and
 * \(v_i\neq v_j\) for \(i\neq j\). A simple path is a path that does not contain a cycle, namely the vertices visited
 * by the path are distinct. Algorithms implementing this interface find all simple paths between a source and a target
 * vertices in a given graph. Note that there may be exponentially many simple paths between two vertices in a graph.
 *
 * @author Barak Ugav
 */
public interface SimplePathsEnumerator {

	/**
	 * Iterate over all the simple paths between a source and a target vertices in the given graph.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, an iterator of {@link IPath} objects will be returned.
	 *
	 * @param  <V>    the vertices type
	 * @param  <E>    the edges type
	 * @param  g      a graph
	 * @param  source the source vertex
	 * @param  target the target vertex
	 * @return        an iterator that iteration over all simple paths between the two vertices in the graph
	 */
	<V, E> Iterator<Path<V, E>> simplePathsIter(Graph<V, E> g, V source, V target);

	/**
	 * Find all the simple paths between a source and a target vertices in the given graph.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a list of {@link IPath} objects will be returned.
	 *
	 * @param  <V>    the vertices type
	 * @param  <E>    the edges type
	 * @param  g      a graph
	 * @param  source the source vertex
	 * @param  target the target vertex
	 * @return        a list of all simple paths between the two vertices in the graph
	 */
	default <V, E> List<Path<V, E>> allSimplePaths(Graph<V, E> g, V source, V target) {
		return new ObjectArrayList<>(simplePathsIter(g, source, target));
	}

	/**
	 * Create a new algorithm for simple paths enumeration.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link SimplePathsEnumerator} object. The
	 * {@link SimplePathsEnumerator.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link SimplePathsEnumerator}
	 */
	static SimplePathsEnumerator newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new simple paths enumerator algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link SimplePathsEnumerator} objects
	 */
	static SimplePathsEnumerator.Builder newBuilder() {
		return SimplePathsEnumeratorSedgewick::new;
	}

	/**
	 * A builder for {@link SimplePathsEnumerator} objects.
	 *
	 * @see    SimplePathsEnumerator#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new algorithm object for simple paths computation.
		 *
		 * @return a new simple paths enumerator algorithm
		 */
		SimplePathsEnumerator build();
	}

}
