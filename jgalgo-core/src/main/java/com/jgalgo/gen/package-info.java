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
 * Graph generators used to generate (possibly random) graphs with different distributions and structures.
 *
 * <p>
 * Graph generators are essential for proper testing/benchmarking of algorithms. They can be used to generate with
 * different distributions and structures. A generator may be used to generate only trees, only bipartite graphs, dense
 * or sparse graphs, etc. It is not a trivial task to generate a graph with real world properties, and therefore
 * multiple generators exists, each with different properties.
 */
package com.jgalgo.gen;
