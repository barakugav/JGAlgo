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
 * Adapters from Guava to JGalgo, and from JGalgo to Guava.
 *
 * <p>
 * Classes that accept a {@linkplain com.jgalgo.graph.Graph JGAlgo Graph} and implement other library graph interface
 * are called <i>adapters</i>. Classes that accept other library graph and implement the JGAlgo graph interface are
 * called <i>wrappers</i>. In Guava there are more than one graph interfaces, {@link com.google.common.graph.Graph},
 * {@link com.google.common.graph.ValueGraph} and {@link com.google.common.graph.Network}. These three interfaces are
 * immutable, and each of them has a mutable counterpart, {@link com.google.common.graph.MutableGraph},
 * {@link com.google.common.graph.MutableValueGraph} and {@link com.google.common.graph.MutableNetwork}. There is an
 * adapter from any of the above to JGAlgo. Wrappers are not implemented for {@linkplain com.google.common.graph.Graph
 * basic graphs} and {@linkplain com.google.common.graph.ValueGraph value graphs}, but only for
 * {@link com.google.common.graph.Network Networks}, see {@link com.jgalgo.adapt.guava.GuavaNetworkWrapper}.
 */
package com.jgalgo.adapt.guava;
