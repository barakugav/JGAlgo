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
import com.jgalgo.graph.IntGraph;

interface BiConnectedComponentsAlgoBase extends BiConnectedComponentsAlgo {

	@SuppressWarnings("unchecked")
	@Override
	default <V, E> BiConnectedComponentsAlgo.Result<V, E> findBiConnectedComponents(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return (BiConnectedComponentsAlgo.Result<V, E>) findBiConnectedComponents((IndexGraph) g);

		} else {
			IndexGraph iGraph = g.indexGraph();
			BiConnectedComponentsAlgo.IResult indexResult = findBiConnectedComponents(iGraph);
			return resultFromIndexResult(g, indexResult);
		}
	}

	BiConnectedComponentsAlgo.IResult findBiConnectedComponents(IndexGraph g);

	@SuppressWarnings("unchecked")
	private static <V, E> BiConnectedComponentsAlgo.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			BiConnectedComponentsAlgo.IResult indexRes) {
		if (g instanceof IntGraph) {
			return (BiConnectedComponentsAlgo.Result<V, E>) new BiConnectedComponentsAlgos.IntResultFromIndexResult(
					(IntGraph) g, indexRes);
		} else {
			return new BiConnectedComponentsAlgos.ObjResultFromIndexResult<>(g, indexRes);
		}
	}

}
