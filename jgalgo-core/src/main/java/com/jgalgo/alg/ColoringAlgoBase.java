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

interface ColoringAlgoBase extends ColoringAlgo {

	@SuppressWarnings("unchecked")
	@Override
	default <V, E> VertexPartition<V, E> computeColoring(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return (VertexPartition<V, E>) computeColoring((IndexGraph) g);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IVertexPartition indexResult = computeColoring(iGraph);
			return VertexPartitions.partitionFromIndexPartition(g, indexResult);
		}
	}

	IVertexPartition computeColoring(IndexGraph g);

}
