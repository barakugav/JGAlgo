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
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

class ShortestPathSTTestUtils {

	@SuppressWarnings("unchecked")
	static ShortestPathSingleSource ssspFromSpst(ShortestPathST spst) {
		return new ShortestPathSingleSource() {

			@Override
			public <V, E> ShortestPathSingleSource.Result<V, E> computeShortestPaths(Graph<V, E> g, WeightFunction<E> w,
					V source) {
				if (!(g instanceof IntGraph))
					throw new IllegalArgumentException("only int graphs are supported");
				IntGraph g0 = (IntGraph) g;
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				int source0 = ((Integer) source).intValue();
				Int2ObjectMap<IPath> paths = new Int2ObjectOpenHashMap<>(g.vertices().size());
				for (int v : g0.vertices())
					paths.put(v, (IPath) spst.computeShortestPath(g0, w0, Integer.valueOf(source0), Integer.valueOf(v)));
				return (ShortestPathSingleSource.Result<V, E>) new ShortestPathSingleSource.IResult() {

					@Override
					public double distance(int target) {
						IPath path = getPath(target);
						return path == null ? Double.POSITIVE_INFINITY : IWeightFunction.weightSum(w0, path.edges());
					}

					@Override
					public IPath getPath(int target) {
						return paths.get(target);
					}

					@Override
					public boolean foundNegativeCycle() {
						return false;
					}

					@Override
					public IPath getNegativeCycle() {
						throw new IllegalStateException();
					}
				};
			}
		};

	}

}
