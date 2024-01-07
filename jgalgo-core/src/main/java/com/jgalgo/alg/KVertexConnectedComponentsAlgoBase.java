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

interface KVertexConnectedComponentsAlgoBase extends KVertexConnectedComponentsAlgo {

	@SuppressWarnings("unchecked")
	@Override
	default <V, E> KVertexConnectedComponentsAlgo.Result<V, E> findKVertexConnectedComponents(Graph<V, E> g, int k) {
		if (g instanceof IndexGraph) {
			return (KVertexConnectedComponentsAlgo.Result<V, E>) findKVertexConnectedComponents((IndexGraph) g, k);
		} else {
			IndexGraph ig = g.indexGraph();
			KVertexConnectedComponentsAlgo.IResult indexRes = findKVertexConnectedComponents(ig, k);
			return resultFromIndexResult(g, indexRes);
		}
	}

	KVertexConnectedComponentsAlgo.IResult findKVertexConnectedComponents(IndexGraph g, int k);

	@SuppressWarnings("unchecked")
	static <V, E> KVertexConnectedComponentsAlgo.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			KVertexConnectedComponentsAlgo.IResult indexRes) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			KVertexConnectedComponentsAlgo.IResult res =
					new KVertexConnectedComponentsAlgos.IntResultFromIndexResult((IntGraph) g, indexRes);
			return (KVertexConnectedComponentsAlgo.Result<V, E>) res;
		} else {
			return new KVertexConnectedComponentsAlgos.ObjResultFromIndexResult<>(g, indexRes);
		}
	}

}
