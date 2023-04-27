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
