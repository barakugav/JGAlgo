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
 * Adapters from JGraphT to JGalgo, and from JGalgo to JGraphT.
 *
 * <p>
 * Classes that accept a {@linkplain com.jgalgo.graph.Graph JGAlgo Graph} and implement other library graph interface
 * are called <i>adapters</i>. Classes that accept other library graph and implement the JGAlgo graph interface are
 * called <i>wrappers</i>. For JGraphT, these are {@link com.jgalgo.adapt.jgrapht.JGraphTAdapter} and
 * {@link com.jgalgo.adapt.jgrapht.JGraphTWrapper}, respectively.
 */
package com.jgalgo.adapt.jgrapht;
