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

import com.jgalgo.SSSP.Builder;

class SSSPBuilderImpl implements SSSP.Builder {

	private boolean intWeights;
	private boolean negativeWeights;
	// private double minWeight;
	// private double maxWeight;
	// private double maxDistance;
	private boolean dagGraphs;

	@Override
	public SSSP build() {
		if (dagGraphs)
			return new SSSPDag();
		if (negativeWeights) {
			if (intWeights) {
				return new SSSPGoldberg();
			} else {
				return new SSSPBellmanFord();
			}
		} else {
			return new SSSPDijkstra();
		}
	}

	@Override
	public Builder setIntWeights(boolean enable) {
		intWeights = enable;
		return this;
	}

	@Override
	public Builder setNegativeWeights(boolean enable) {
		negativeWeights = enable;
		return this;
	}

	@Override
	public Builder setMinWeight(double minWeight) {
		// this.minWeight = minWeight;
		return this;
	}

	@Override
	public Builder setMaxWeight(double maxWeight) {
		// this.maxWeight = maxWeight;
		return this;
	}

	@Override
	public Builder setMaxDistance(double maxDistance) {
		// this.maxDistance = maxDistance;
		return this;
	}

	@Override
	public SSSP.Builder setDag(boolean dagGraphs) {
		this.dagGraphs = dagGraphs;
		return this;
	}

}
