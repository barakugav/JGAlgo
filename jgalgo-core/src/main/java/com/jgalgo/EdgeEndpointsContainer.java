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

class EdgeEndpointsContainer extends WeightsImpl.Index.Long {

	private static final int None = -1;

	EdgeEndpointsContainer(IDStrategyImpl idStrat) {
		super(idStrat, sourceTarget2Endpoints(None, None));
	}

	EdgeEndpointsContainer(EdgeEndpointsContainer orig, IDStrategyImpl idStrat) {
		super(orig, idStrat);
	}

	void setEndpoints(int edge, int source, int target) {
		set(edge, sourceTarget2Endpoints(source, target));
	}

	int edgeSource(int edge) {
		return endpoints2Source(getLong(edge));
	}

	int edgeTarget(int edge) {
		return endpoints2Target(getLong(edge));
	}

	int edgeEndpoint(int edge, int endpoint) {
		long endpoints = getLong(edge);
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

	void reverseEdge(int edge) {
		long endpoints = getLong(edge);
		int u = endpoints2Source(endpoints);
		int v = endpoints2Target(endpoints);
		endpoints = sourceTarget2Endpoints(v, u);
		set(edge, endpoints);
	}

	void replaceEdgeSource(int edge, int newSource) {
		long endpoints = getLong(edge);
		int target = endpoints2Target(endpoints);
		set(edge, sourceTarget2Endpoints(newSource, target));
	}

	void replaceEdgeTarget(int edge, int newTarget) {
		long endpoints = getLong(edge);
		int source = endpoints2Source(endpoints);
		set(edge, sourceTarget2Endpoints(source, newTarget));
	}

	void replaceEdgeEndpoint(int edge, int oldEndpoint, int newEndpoint) {
		long endpoints = getLong(edge);
		int source = endpoints2Source(endpoints);
		int target = endpoints2Target(endpoints);
		if (source == oldEndpoint)
			source = newEndpoint;
		if (target == oldEndpoint)
			target = newEndpoint;
		set(edge, sourceTarget2Endpoints(source, target));
	}

	@Override
	public EdgeEndpointsContainer copy(IDStrategyImpl idStrat) {
		return new EdgeEndpointsContainer(this, idStrat);
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

		EdgeEndpointsContainer edgeEndpoints();

		@Override
		default int edgeEndpoint(int edge, int endpoint) {
			return edgeEndpoints().edgeEndpoint(edge, endpoint);
		}

		@Override
		default int edgeSource(int edge) {
			return edgeEndpoints().edgeSource(edge);
		}

		@Override
		default int edgeTarget(int edge) {
			return edgeEndpoints().edgeTarget(edge);
		}

		default void replaceEdgeSource(int edge, int newSource) {
			edgeEndpoints().replaceEdgeSource(edge, newSource);
		}

		default void replaceEdgeTarget(int edge, int newTarget) {
			edgeEndpoints().replaceEdgeTarget(edge, newTarget);
		}

		default void replaceEdgeEndpoint(int edge, int oldEndpoint, int newEndpoint) {
			edgeEndpoints().replaceEdgeEndpoint(edge, oldEndpoint, newEndpoint);
		}

	}

}
