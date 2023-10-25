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

import com.jgalgo.internal.util.JGAlgoUtils;

class EdgeEndpointsContainer {

	private static final int None = -1;
	static final long DefVal = sourceTarget2Endpoints(None, None);

	static void reverseEdge(long[] edgeEndpoints, int edge) {
		long endpoints = edgeEndpoints[edge];
		int u = endpoints2Source(endpoints);
		int v = endpoints2Target(endpoints);
		endpoints = sourceTarget2Endpoints(v, u);
		edgeEndpoints[edge] = endpoints;
	}

	static long sourceTarget2Endpoints(int source, int target) {
		return JGAlgoUtils.longCompose(source, target);
	}

	private static int endpoints2Source(long endpoints) {
		return JGAlgoUtils.long2low(endpoints);
	}

	private static int endpoints2Target(long endpoints) {
		return JGAlgoUtils.long2high(endpoints);
	}

	static interface GraphWithEdgeEndpointsContainer extends IndexGraphImpl {

		long[] edgeEndpoints();

		default void setEndpoints(int edge, int source, int target) {
			edgeEndpoints()[edge] = sourceTarget2Endpoints(source, target);
		}

		@Override
		default int edgeEndpoint(int edge, int endpoint) {
			edges().checkIdx(edge);
			long endpoints = edgeEndpoints()[edge];
			int u = endpoints2Source(endpoints);
			int v = endpoints2Target(endpoints);
			if (endpoint == u) {
				return v;
			} else if (endpoint == v) {
				return u;
			} else {
				throw new IllegalArgumentException("The given vertex (idx=" + endpoint
						+ ") is not an endpoint of the edge (idx=" + u + ", idx=" + v + ")");
			}
		}

		@Override
		default int edgeSource(int edge) {
			edges().checkIdx(edge);
			return endpoints2Source(edgeEndpoints()[edge]);
		}

		@Override
		default int edgeTarget(int edge) {
			edges().checkIdx(edge);
			return endpoints2Target(edgeEndpoints()[edge]);
		}

		default void replaceEdgeSource(int edge, int newSource) {
			long endpoints = edgeEndpoints()[edge];
			int target = endpoints2Target(endpoints);
			edgeEndpoints()[edge] = sourceTarget2Endpoints(newSource, target);
		}

		default void replaceEdgeTarget(int edge, int newTarget) {
			long endpoints = edgeEndpoints()[edge];
			int source = endpoints2Source(endpoints);
			edgeEndpoints()[edge] = sourceTarget2Endpoints(source, newTarget);
		}

		default void replaceEdgeEndpoint(int edge, int oldEndpoint, int newEndpoint) {
			long endpoints = edgeEndpoints()[edge];
			int source = endpoints2Source(endpoints);
			int target = endpoints2Target(endpoints);
			if (source == oldEndpoint)
				source = newEndpoint;
			if (target == oldEndpoint)
				target = newEndpoint;
			edgeEndpoints()[edge] = sourceTarget2Endpoints(source, target);
		}

	}

}
