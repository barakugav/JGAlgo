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

/**
 * Algorithms for solving graph problems.
 *
 * <p>
 * Most algorithms accept a {@link com.jgalgo.graph.Graph} object as input, and perform some computation on it.
 * Algorithms in this package and its sub packages follow a common pattern: an interface is defined for the
 * functionality only (e.g. {@link com.jgalgo.alg.shortestpath.ShortestPathSingleSource}), a result object is defined
 * within the interface (e.g. {@link com.jgalgo.alg.shortestpath.ShortestPathSingleSource.Result}), a default
 * implementation is provided via a {@code newInstance()} method (e.g.
 * {@link com.jgalgo.alg.shortestpath.ShortestPathSingleSource#newInstance()}), and a builder that allow more control
 * over the algorithm may be provided via {@code builder()} method (e.g.
 * {@link com.jgalgo.alg.shortestpath.ShortestPathSingleSource#builder()}). In addition, algorithm interfaces define a
 * result object specifically for {@linkplain com.jgalgo.graph.IntGraph int graphs} in which the vertices and edges are
 * integers (e.g. {@link com.jgalgo.alg.shortestpath.ShortestPathSingleSource.IResult}). In the common use case, there
 * is no need to use {@link com.jgalgo.graph.IntGraph} and the result objects that are specific for it.
 *
 * <p>
 * None of the algorithms or result objects in this package are thread safe. Result objects should not be used once the
 * graph on which the result was computed is modified, as the result object may relay on the graph for information and
 * may not be able to detect the modification.
 */
package com.jgalgo.alg;
