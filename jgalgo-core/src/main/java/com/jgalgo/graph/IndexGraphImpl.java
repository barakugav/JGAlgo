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
	default void addVertexSwapListener(IndexSwapListener listener) {
		vertices().addIdSwapListener(listener);
	}

	@Override
	default void removeVertexSwapListener(IndexSwapListener listener) {
		vertices().removeIdSwapListener(listener);
	}

	@Override
	default void addEdgeSwapListener(IndexSwapListener listener) {
		edges().addIdSwapListener(listener);
	}

	@Override
	default void removeEdgeSwapListener(IndexSwapListener listener) {
		edges().removeIdSwapListener(listener);
	}

}
