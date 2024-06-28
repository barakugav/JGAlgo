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
package com.jgalgo.alg.shortestpath;

import java.util.Iterator;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.internal.util.IterTools;

/**
 * Abstract class for enumerating all simple paths between a source and target vertices.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class SimplePathsEnumeratorAbstract implements SimplePathsEnumerator {

	/**
	 * Default constructor.
	 */
	public SimplePathsEnumeratorAbstract() {}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V, E> Iterator<Path<V, E>> simplePathsIter(Graph<V, E> g, V source, V target) {
		if (g instanceof IndexGraph) {
			int source0 = ((Integer) source).intValue(), target0 = ((Integer) target).intValue();
			return (Iterator) simplePathsIter((IndexGraph) g, source0, target0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			int iSource = viMap.idToIndex(source);
			int iTarget = viMap.idToIndex(target);
			Iterator<IPath> indexResult = simplePathsIter(iGraph, iSource, iTarget);
			return IterTools.map(indexResult, iPath -> Path.pathFromIndexPath(g, iPath));
		}
	}

	protected abstract Iterator<IPath> simplePathsIter(IndexGraph g, int source, int target);

}
