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

public interface GraphBuilderFixedRemapped {

	int addVertex();

	int addEdge(int source, int target);

	BuilderResult build();

	static GraphBuilderFixedRemapped newDirected() {
		return new GraphCSRRemappedDirected.Builder();
	}

	// static GraphBuilderFixedRemmaped newUndirected() {
	// return new GraphCSRRemappedUndirected.Builder();
	// }

	static class BuilderResult {
		public final IndexGraph graph;
		public final int[] edgesFixedToInsertIdx;
		public final int[] edgesInsertIdxToFixed;

		BuilderResult(IndexGraph g, int[] edgesFixedToInsertIdx, int[] edgesInsertIdxToFixed) {
			this.graph = g;
			this.edgesFixedToInsertIdx = edgesFixedToInsertIdx;
			this.edgesInsertIdxToFixed = edgesInsertIdxToFixed;
		}
	}

}
