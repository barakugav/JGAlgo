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

public interface GraphBuilderFixedUnmapped {

	int addVertex();

	int addEdge(int source, int target);

	int verticesNum();

	int edgesNum();

	IndexGraph build();

	static GraphBuilderFixedUnmapped newUndirected() {
		return new GraphCSRUnmappedUndirected.Builder();
	}

	static GraphBuilderFixedUnmapped newDirected() {
		return new GraphCSRUnmappedDirected.Builder();
	}

	static GraphBuilderFixedUnmapped newFrom(IndexGraph g) {
		GraphBuilderFixedUnmapped builder = g.getCapabilities().directed() ? newDirected() : newUndirected();
		final int n = g.vertices().size();
		final int m = g.edges().size();
		for (int v = 0; v < n; v++) {
			int vFixed = builder.addVertex();
			assert vFixed == v;
		}
		for (int e = 0; e < m; e++) {
			int eFixed = builder.addEdge(g.edgeSource(e), g.edgeTarget(e));
			assert eFixed == e;
		}
		return builder;
	}

}
