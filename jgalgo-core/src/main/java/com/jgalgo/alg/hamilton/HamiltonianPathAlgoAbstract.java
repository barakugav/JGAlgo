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
package com.jgalgo.alg.hamilton;

import java.util.Iterator;
import com.jgalgo.alg.path.IPath;
import com.jgalgo.alg.path.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.internal.util.IterTools;

/**
 * Abstract class for computing Hamiltonian cycles/paths in graphs.
 *
 * <p>
 * The class implements the interface by solving the problem on the index graph and then maps the results back to the
 * original graph. The implementation for the index graph is abstract and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class HamiltonianPathAlgoAbstract implements HamiltonianPathAlgo {

	/**
	 * Default constructor.
	 */
	public HamiltonianPathAlgoAbstract() {}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V, E> Iterator<Path<V, E>> hamiltonianPathsIter(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return (Iterator) hamiltonianPathsIter((IndexGraph) g);
		} else {
			IndexGraph ig = g.indexGraph();
			Iterator<IPath> indexIter = hamiltonianPathsIter(ig);
			return IterTools.map(indexIter, iPath -> Path.pathFromIndexPath(g, iPath));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V, E> Iterator<Path<V, E>> hamiltonianPathsIter(Graph<V, E> g, V source, V target) {
		if (g instanceof IndexGraph) {
			int src = ((Integer) source).intValue(), trg = ((Integer) target).intValue();
			return (Iterator) hamiltonianPathsIter((IndexGraph) g, src, trg);
		} else {
			IndexGraph ig = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			int src = viMap.idToIndex(source), trg = viMap.idToIndex(target);
			Iterator<IPath> indexIter = hamiltonianPathsIter(ig, src, trg);
			return IterTools.map(indexIter, iPath -> Path.pathFromIndexPath(g, iPath));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V, E> Iterator<Path<V, E>> hamiltonianCyclesIter(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return (Iterator) hamiltonianCyclesIter((IndexGraph) g);
		} else {
			IndexGraph ig = g.indexGraph();
			Iterator<IPath> indexIter = hamiltonianCyclesIter(ig);
			return IterTools.map(indexIter, iPath -> Path.pathFromIndexPath(g, iPath));
		}
	}

	protected abstract Iterator<IPath> hamiltonianPathsIter(IndexGraph g);

	protected abstract Iterator<IPath> hamiltonianPathsIter(IndexGraph g, int source, int target);

	protected abstract Iterator<IPath> hamiltonianCyclesIter(IndexGraph g);

}
