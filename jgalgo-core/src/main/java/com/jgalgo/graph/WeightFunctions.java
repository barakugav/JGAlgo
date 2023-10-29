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
package com.jgalgo.graph;

/**
 * Static methods class for {@linkplain IWeightFunction weight functions}.
 *
 * @author Barak Ugav
 */
public class WeightFunctions {

	private WeightFunctions() {}

	/**
	 * Get a 'local' version of a given weight function.
	 * <p>
	 * A {@link IWeightFunction} is a functional interface, and may be implemented in many mays, and querying the weight
	 * of a single edge may be arbitrarily costly. In scenarios in which the weights of the edges are needed multiple
	 * times, it may be more efficient to query the weights of each edge once and store the result <i>locally</i>, to
	 * ensure the potentially heavy computations occur few as possible.
	 * <p>
	 * This method accept a weight function, and return a 'local' version of it. If the original weight function is
	 * already some 'local' implementation of a weight function, it will be returned as is without duplication. In other
	 * cases, in which the implementation decide to, the weights of the edges are computed onces and stored in a local
	 * weight function, which is than returned. In all cases, the returned weight function is local.
	 * <p>
	 * This function is used only for performance boost, it does not change any functionally over using the original
	 * weight function.
	 *
	 * @param  g a graph
	 * @param  w a weight function
	 * @return   a local version of the weight function
	 */
	public static IWeightFunction localEdgeWeightFunction(IndexGraph g, IWeightFunction w) {
		if (w == null || w == IWeightFunction.CardinalityWeightFunction)
			return w;
		if (w instanceof WeightsImpl.Index)
			return w;
		if (w instanceof IWeightFunctionInt) {
			IWeightFunctionInt wInt = (IWeightFunctionInt) w;
			IWeightsInt wLocal = IWeights.createExternalEdgesWeights(g, int.class);
			for (int m = g.edges().size(), e = 0; e < m; e++)
				wLocal.set(e, wInt.weightInt(e));
			return wLocal;
		} else {
			IWeightsDouble wLocal = IWeights.createExternalEdgesWeights(g, double.class);
			for (int m = g.edges().size(), e = 0; e < m; e++)
				wLocal.set(e, w.weight(e));
			return wLocal;
		}
	}

	/**
	 * Get a 'local' version of a given weight function.
	 * <p>
	 * A {@link IWeightFunction} is a functional interface, and may be implemented in many mays, and querying the weight
	 * of a single edge may be arbitrarily costly. In scenarios in which the weights of the edges are needed multiple
	 * times, it may be more efficient to query the weights of each edge once and store the result <i>locally</i>, to
	 * ensure the potentially heavy computations occur few as possible.
	 * <p>
	 * This method accept a weight function, and return a 'local' version of it. If the original weight function is
	 * already some 'local' implementation of a weight function, it will be returned as is without duplication. In other
	 * cases, in which the implementation decide to, the weights of the edges are computed onces and stored in a local
	 * weight function, which is than returned. In all cases, the returned weight function is local.
	 * <p>
	 * This function is used only for performance boost, it does not change any functionally over using the original
	 * weight function.
	 *
	 * @param  g a graph
	 * @param  w a weight function
	 * @return   a local version of the weight function
	 */
	public static IWeightFunctionInt localEdgeWeightFunction(IndexGraph g, IWeightFunctionInt w) {
		if (w == null || w == IWeightFunction.CardinalityWeightFunction)
			return w;
		if (w instanceof WeightsImpl.Index)
			return w;
		IWeightsInt wLocal = IWeights.createExternalEdgesWeights(g, int.class);
		for (int m = g.edges().size(), e = 0; e < m; e++)
			wLocal.set(e, w.weightInt(e));
		return wLocal;
	}

}
