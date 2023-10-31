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
 * <p>
 * Most algorithms accept a {@link com.jgalgo.graph.IntGraph} object as input, and perform some computation on it.
 * Algorithms in this package follow a common pattern: an interface is defined for the functionality only (e.g.
 * {@link com.jgalgo.alg.ShortestPathSingleSource}), a result object is defined within the interface (e.g.
 * {@link com.jgalgo.alg.ShortestPathSingleSource.IResult}), a default implementation is provided via
 * {@code newInstance()} method (e.g. {@link com.jgalgo.alg.ShortestPathSingleSource#newInstance()}), and a builder that
 * allow more control over the algorithm is provided via {@code newBuilder()} method (e.g.
 * {@link com.jgalgo.alg.ShortestPathSingleSource#newBuilder()}).
 * <p>
 * Most algorithm implementations are not expose as public API.
 */
package com.jgalgo.alg;
