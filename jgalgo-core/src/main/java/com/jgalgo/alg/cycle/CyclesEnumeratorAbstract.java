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
import com.jgalgo.alg.path.IPath;
import com.jgalgo.alg.path.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.IterTools;

abstract class CyclesEnumeratorAbstract implements CyclesEnumerator {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V, E> Iterator<Path<V, E>> cyclesIter(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return (Iterator) cyclesIter((IndexGraph) g);

		} else {
			IndexGraph iGraph = g.indexGraph();
			Iterator<IPath> indexResult = cyclesIter(iGraph);
			return IterTools.map(indexResult, iPath -> Path.pathFromIndexPath(g, iPath));
		}
	}

	abstract Iterator<IPath> cyclesIter(IndexGraph g);

}
