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
package com.jgalgo.alg.path;

import java.util.List;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;

class ShortestPathStTestUtils {

	static ShortestPathSingleSource ssspFromSpst(ShortestPathSt spst) {
		return new ShortestPathSingleSource() {

			@Override
			public <V, E> ShortestPathSingleSource.Result<V, E> computeShortestPaths(Graph<V, E> g, WeightFunction<E> w,
					V source) {
				Object2ObjectMap<V, ObjectDoublePair<Path<V, E>>> paths =
						new Object2ObjectOpenHashMap<>(g.vertices().size());
				for (V target : g.vertices())
					paths.put(target, spst.computeShortestPathAndWeight(g, w, source, target));
				return new ShortestPathSingleSource.Result<>() {

					@Override
					public double distance(V target) {
						if (!g.vertices().contains(target))
							throw NoSuchVertexException.ofVertex(target);
						if (!paths.containsKey(target))
							throw new IllegalArgumentException("Target vertex " + target + " is not in the graph");
						ObjectDoublePair<Path<V, E>> path = paths.get(target);
						return path != null ? path.secondDouble() : Double.POSITIVE_INFINITY;
					}

					@Override
					public Path<V, E> getPath(V target) {
						if (!g.vertices().contains(target))
							throw NoSuchVertexException.ofVertex(target);
						if (!paths.containsKey(target))
							throw new IllegalArgumentException("Target vertex " + target + " is not in the graph");
						ObjectDoublePair<Path<V, E>> path = paths.get(target);
						return path != null ? path.first() : null;
					}

					@Override
					public E backtrackEdge(V target) {
						Path<V, E> path = getPath(target);
						if (path != null) {
							List<E> edges = path.edges();
							if (!edges.isEmpty())
								return edges.get(edges.size() - 1);
						}
						return null;
					}

					@Override
					public V source() {
						return source;
					}

					@Override
					public Graph<V, E> graph() {
						return g;
					}
				};
			}
		};

	}

}
