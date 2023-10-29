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
 * Graphs object are the fundamental building blocks of the JGAlgo library.
 * <p>
 * A graph is a collection of vertices (nodes) and edges (links) between them. The edges may be directed or undirected.
 * The {@link com.jgalgo.graph.IntGraph} object represent a graph, in which vertices and edges have {@code int}
 * identifiers. Vertices can be added or removed to/from a graph, and edges can be added or removed between vertices.
 * The {@link com.jgalgo.graph.IntGraph} object also provides methods to query the graph, such as the number of vertices
 * and edges, and whether a vertex is connected to another vertex, the out-edges or in-edges of a vertex, etc.
 * <p>
 * Weights can be associated with the vertices or edges of a graph, and the {@link com.jgalgo.graph.IntGraph} object
 * provides methods to add or remove weights to it. When a weight is added to the vertices, it is added to <i>all</i>
 * vertices of the graph. The weights can be an {@code Object} or any other primitive such as {@code int},
 * {@code double}, etc. When weights are added to a graph via the
 * {@link com.jgalgo.graph.IntGraph#addVerticesWeights(String, Class)} or
 * {@link com.jgalgo.graph.IntGraph#addEdgesWeights(String, Class)} methods, a {@link com.jgalgo.graph.IWeights} object
 * is created and return, which allow setting and getting the weights values by vertex/edge identifier. If the weights
 * type is primitive, a specific subclass of {@link com.jgalgo.graph.IWeights} such as
 * {@link com.jgalgo.graph.IWeightsInt}, {@link com.jgalgo.graph.IWeightsDouble}, etc. is returned. These specific
 * primitive weights containers avoid the overhead of boxing and unboxing primitive values.
 * <p>
 * Each {@link com.jgalgo.graph.IntGraph} object expose an {@link com.jgalgo.graph.IndexGraph} via the
 * {@link com.jgalgo.graph.IntGraph#indexGraph()} method. An index graph is a graph in which the identifiers of the
 * vertices are always in the range {@code [0, n)}, where {@code n} is the number of vertices in the graph, and the
 * identifiers of the edges are always in the range {@code [0, m)}, where {@code m} is the number of edges in the graph.
 * Index graphs are much more efficient than regular graphs, as simple arrays are enough to store the data (internal and
 * additional weights) associated with the vertices/edges. When a vertex or edge is removed from an index graph, some
 * identifiers may change, as it must maintain the invariant that the identifiers are always in the range {@code [0, n)}
 * and {@code [0, m)}. This makes the index graph less convenient to use, but much more efficient. The default graph
 * implementation uses an index graph internally, and expose it via the {@link com.jgalgo.graph.IntGraph#indexGraph()}
 * method. Good written algorithms should accept a regular graph, retrieve its index graph, perform the heavy
 * computation on the index graph, and translate back the result to the regular graph identifiers. Index graphs should
 * rarely be used directly by a user.
 */
package com.jgalgo.graph;
