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
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

class ShortestPathSTTestUtils {

	static ShortestPathSingleSource ssspFromSpst(ShortestPathST spst) {
		return new ShortestPathSingleSource() {

			@Override
			public <V, E> ShortestPathSingleSource.Result<V, E> computeShortestPaths(Graph<V, E> g, WeightFunction<E> w,
					V source) {
				Object2ObjectMap<V, Path<V, E>> paths = new Object2ObjectOpenHashMap<>(g.vertices().size());
				for (V target : g.vertices())
					paths.put(target, spst.computeShortestPath(g, w, source, target));
				return new ShortestPathSingleSource.Result<>() {

					@Override
					public double distance(V target) {
						Path<V, E> path = getPath(target);
						return path == null ? Double.POSITIVE_INFINITY : WeightFunction.weightSum(w, path.edges());
					}

					@Override
					public Path<V, E> getPath(V target) {
						return paths.get(target);
					}

					@Override
					public boolean foundNegativeCycle() {
						return false;
					}

					@Override
					public Path<V, E> getNegativeCycle() {
						throw new IllegalStateException();
					}
				};
			}
		};

	}

}
