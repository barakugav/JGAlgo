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
package com.jgalgo.graph;

interface IndexGraphImpl extends IndexGraph {

	@Override
	GraphElementSet vertices();

	@Override
	GraphElementSet edges();

	@Override
	default void addVertexRemoveListener(IndexRemoveListener listener) {
		vertices().addRemoveListener(listener);
	}

	@Override
	default void removeVertexSwapRemoveListener(IndexRemoveListener listener) {
		vertices().removeRemoveListener(listener);
	}

	@Override
	default void addEdgeRemoveListener(IndexRemoveListener listener) {
		edges().addRemoveListener(listener);
	}

	@Override
	default void removeEdgeSwapRemoveListener(IndexRemoveListener listener) {
		edges().removeRemoveListener(listener);
	}

}
