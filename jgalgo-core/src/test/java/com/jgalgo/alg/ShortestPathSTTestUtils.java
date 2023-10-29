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

import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IWeightFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

class ShortestPathSTTestUtils {

	static ShortestPathSingleSource ssspFromSpst(ShortestPathST spst) {
		return new ShortestPathSingleSource() {

			@Override
			public ShortestPathSingleSource.Result computeShortestPaths(IntGraph g, IWeightFunction w, int source) {
				Int2ObjectMap<Path> paths = new Int2ObjectOpenHashMap<>(g.vertices().size());
				for (int v : g.vertices())
					paths.put(v, spst.computeShortestPath(g, w, source, v));
				return new ShortestPathSingleSource.Result() {

					@Override
					public double distance(int target) {
						Path path = getPath(target);
						return path == null ? Double.POSITIVE_INFINITY : IWeightFunction.weightSum(w, path.edges());
					}

					@Override
					public Path getPath(int target) {
						return paths.get(target);
					}

					@Override
					public boolean foundNegativeCycle() {
						return false;
					}

					@Override
					public Path getNegativeCycle() {
						throw new IllegalStateException();
					}
				};
			}
		};

	}

}
