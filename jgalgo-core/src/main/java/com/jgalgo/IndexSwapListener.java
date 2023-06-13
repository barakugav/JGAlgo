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
package com.jgalgo;

/**
 * A listener that will be notified when a {@link IndexGraph} chooses to swap two IDs (vertices/edges).
 * <p>
 * The {@code int} identifiers an {@link IndexGraph} uses for its vertices are always {@code 0,1,2,...,verticesNum} (and
 * similarly for its edges). To maintain this invariant the graph implementations way swap and rename its vertices
 * (edges) during its lifetime. It's possible to register to these swaps using
 * {@link IndexGraph#addVertexSwapListener(IndexSwapListener)} and
 * {@link IndexGraph#addEdgeSwapListener(IndexSwapListener)}, which ensure a callback listener will be notified when
 * such swaps occur.
 * <p>
 * The same swap listener interface is used for both vertices and edges (a specific instance is only used to one of
 * them, which can be determined by the context), and we use a unified term <i>element</i> in the documentation to refer
 * to either of them.
 *
 * @author Barak Ugav
 */
@FunctionalInterface
public interface IndexSwapListener {

	/**
	 * A callback that is called when {@code idx1} and {@code idx2} are swapped.
	 * <p>
	 * The same swap listener interface is used for both vertices and edges (a specific instance is only used to one of
	 * them, which can be determined by the context), and we use a unified term <i>element</i> in the documentation to
	 * refer to either of them.
	 *
	 * @param idx1 the first element index
	 * @param idx2 the second element index
	 */
	void swap(int idx1, int idx2);
}
