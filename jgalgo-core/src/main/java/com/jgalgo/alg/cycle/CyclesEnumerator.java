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

package com.jgalgo.alg.cycle;

import java.util.Iterator;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;

/**
 * An algorithm that enumerate all cycles in a graph.
 *
 * <p>
 * Given a graph \(G = (V, E)\), a cycle is a path \(P = (v_1, v_2, \ldots, v_k), v_i \in V, (v_i,v_{i+1}) \in E\) such
 * that \(v_1 = v_k\). There might be exponentially many cycles in a graph, and algorithms implementing this interface
 * enumerate over all of them (use an {@link Iterator} avoiding storing all of them in memory).
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @author Barak Ugav
 */
public interface CyclesEnumerator {

	/**
	 * Iterate over all cycles in the given graph.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned iterator will iterate over {@link IPath} objects.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     an iterator that iteration over all cycles in the graph
	 */
	public <V, E> Iterator<Path<V, E>> cyclesIter(Graph<V, E> g);

	/**
	 * Create a new algorithm for cycles enumerating.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link CyclesEnumerator} object.
	 *
	 * @return a default implementation of {@link CyclesEnumerator}
	 */
	static CyclesEnumerator newInstance() {
		return new CyclesEnumeratorTarjan();
	}

}
