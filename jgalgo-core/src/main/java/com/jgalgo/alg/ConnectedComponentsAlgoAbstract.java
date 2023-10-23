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
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;

abstract class ConnectedComponentsAlgoAbstract implements ConnectedComponentsAlgo {

	@Override
	public VertexPartition findConnectedComponents(Graph g) {
		if (g instanceof IndexGraph)
			return findConnectedComponents((IndexGraph) g);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();

		VertexPartition indexResult = findConnectedComponents(iGraph);
		return new VertexPartitions.ResultFromIndexResult(indexResult, viMap, eiMap);
	}

	@Override
	public VertexPartition findWeaklyConnectedComponents(Graph g) {
		if (g instanceof IndexGraph)
			return findWeaklyConnectedComponents((IndexGraph) g);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();

		VertexPartition indexResult = findWeaklyConnectedComponents(iGraph);
		return new VertexPartitions.ResultFromIndexResult(indexResult, viMap, eiMap);
	}

	abstract VertexPartition findConnectedComponents(IndexGraph g);

	abstract VertexPartition findWeaklyConnectedComponents(IndexGraph g);

}
