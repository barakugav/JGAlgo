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
package com.jgalgo.alg.connect;

import java.util.Iterator;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Abstract class for computing all global minimum vertex cuts in a graph.
 *
 * <p>
 * The class implements the interface by solving the problem on the index graph and then maps the results back to the
 * original graph. The implementation for the index graph is abstract and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class MinimumVertexCutAllGlobalAbstract implements MinimumVertexCutAllGlobal {

	/**
	 * Default constructor.
	 */
	public MinimumVertexCutAllGlobalAbstract() {}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V, E> Iterator<Set<V>> minimumCutsIter(Graph<V, E> g, WeightFunction<V> w) {
		if (g instanceof IndexGraph) {
			IWeightFunction iw = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			return (Iterator) minimumCutsIter((IndexGraph) g, iw);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, viMap);
			Iterator<IntSet> indexCuts = minimumCutsIter(iGraph, iw);
			return IterTools.map(indexCuts, c -> IndexIdMaps.indexToIdSet(c, viMap));
		}
	}

	/**
	 * Iterate over all the minimum vertex-cuts in a graph.
	 *
	 * @see      #minimumCutsIter(Graph, WeightFunction)
	 * @param  g the graph
	 * @param  w a vertex weight function
	 * @return   an iterator over all the minimum vertex-cuts in a graph
	 */
	protected abstract Iterator<IntSet> minimumCutsIter(IndexGraph g, IWeightFunction w);

}
