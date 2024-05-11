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
package com.jgalgo.alg.cores;

import com.jgalgo.alg.EdgeDirection;
import com.jgalgo.alg.cores.CoresAlgos.IntResultFromIndexResult;
import com.jgalgo.alg.cores.CoresAlgos.ObjResultFromIndexResult;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IntGraph;

/**
 * Abstract class for computing the cores of a graph.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class CoresAlgoAbstract implements CoresAlgo {

	/**
	 * Default constructor.
	 */
	public CoresAlgoAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> CoresAlgo.Result<V, E> computeCores(Graph<V, E> g, EdgeDirection degreeType) {
		if (g instanceof IndexGraph) {
			return (CoresAlgo.Result<V, E>) computeCores((IndexGraph) g, degreeType);

		} else {
			IndexGraph iGraph = g.indexGraph();
			CoresAlgo.IResult iResult = computeCores(iGraph, degreeType);
			return resultFromIndexResult(g, iResult);
		}
	}

	protected abstract CoresAlgo.IResult computeCores(IndexGraph g, EdgeDirection degreeType);

	@SuppressWarnings("unchecked")
	private static <V, E> CoresAlgo.Result<V, E> resultFromIndexResult(Graph<V, E> g, CoresAlgo.IResult indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (CoresAlgo.Result<V, E>) new IntResultFromIndexResult((IntGraph) g, indexResult);
		} else {
			return new ObjResultFromIndexResult<>(g, indexResult);
		}
	}

}
