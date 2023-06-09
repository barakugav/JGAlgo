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

class EdgeEndpointsContainer {

	private static final int None = -1;
	static final long DefVal = sourceTarget2Endpoints(None, None);

	static void setEndpoints(long[] edgeEndpoints, int edge, int source, int target) {
		edgeEndpoints[edge] = sourceTarget2Endpoints(source, target);
	}

	static int edgeSource(long[] edgeEndpoints, int edge) {
		return endpoints2Source(edgeEndpoints[edge]);
	}

	static int edgeTarget(long[] edgeEndpoints, int edge) {
		return endpoints2Target(edgeEndpoints[edge]);
	}

	static int edgeEndpoint(long[] edgeEndpoints, int edge, int endpoint) {
		long endpoints = edgeEndpoints[edge];
		int u = endpoints2Source(endpoints);
		int v = endpoints2Target(endpoints);
		if (endpoint == u) {
			return v;
		} else if (endpoint == v) {
			return u;
		} else {
			throw new IllegalArgumentException(
					"The given vertex (" + endpoint + ") is not an endpoint of the edge (" + u + ", " + v + ")");
		}
	}

	static void reverseEdge(long[] edgeEndpoints, int edge) {
		long endpoints = edgeEndpoints[edge];
		int u = endpoints2Source(endpoints);
		int v = endpoints2Target(endpoints);
		endpoints = sourceTarget2Endpoints(v, u);
		edgeEndpoints[edge] = endpoints;
	}

	static void replaceEdgeSource(long[] edgeEndpoints, int edge, int newSource) {
		long endpoints = edgeEndpoints[edge];
		int target = endpoints2Target(endpoints);
		edgeEndpoints[edge] = sourceTarget2Endpoints(newSource, target);
	}

	static void replaceEdgeTarget(long[] edgeEndpoints, int edge, int newTarget) {
		long endpoints = edgeEndpoints[edge];
		int source = endpoints2Source(endpoints);
		edgeEndpoints[edge] = sourceTarget2Endpoints(source, newTarget);
	}

	static void replaceEdgeEndpoint(long[] edgeEndpoints, int edge, int oldEndpoint, int newEndpoint) {
		long endpoints = edgeEndpoints[edge];
		int source = endpoints2Source(endpoints);
		int target = endpoints2Target(endpoints);
		if (source == oldEndpoint)
			source = newEndpoint;
		if (target == oldEndpoint)
			target = newEndpoint;
		edgeEndpoints[edge] = sourceTarget2Endpoints(source, target);
	}

	private static long sourceTarget2Endpoints(int source, int target) {
		return ((source & 0xffffffffL) << 32) | ((target & 0xffffffffL) << 0);
	}

	private static int endpoints2Source(long endpoints) {
		return (int) ((endpoints >> 32) & 0xffffffffL);
	}

	private static int endpoints2Target(long endpoints) {
		return (int) ((endpoints >> 0) & 0xffffffffL);
	}

	static interface GraphWithEdgeEndpointsContainer extends IndexGraph {

		long[] edgeEndpoints();

		@Override
		default int edgeEndpoint(int edge, int endpoint) {
			return EdgeEndpointsContainer.edgeEndpoint(edgeEndpoints(), edge, endpoint);
		}

		@Override
		default int edgeSource(int edge) {
			return EdgeEndpointsContainer.edgeSource(edgeEndpoints(), edge);
		}

		@Override
		default int edgeTarget(int edge) {
			return EdgeEndpointsContainer.edgeTarget(edgeEndpoints(), edge);
		}

		default void replaceEdgeSource(int edge, int newSource) {
			EdgeEndpointsContainer.replaceEdgeSource(edgeEndpoints(), edge, newSource);
		}

		default void replaceEdgeTarget(int edge, int newTarget) {
			EdgeEndpointsContainer.replaceEdgeTarget(edgeEndpoints(), edge, newTarget);
		}

		default void replaceEdgeEndpoint(int edge, int oldEndpoint, int newEndpoint) {
			EdgeEndpointsContainer.replaceEdgeEndpoint(edgeEndpoints(), edge, oldEndpoint, newEndpoint);
		}

	}

}
