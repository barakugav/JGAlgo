package com.ugav.jgalgo;

import it.unimi.dsi.fastutil.ints.IntComparator;

@FunctionalInterface
public interface EdgeWeightFunc extends IntComparator {

	public double weight(int e);

	@Override
	default int compare(int e1, int e2) {
		return Utils.compare(weight(e1), weight(e2));
	}

	@FunctionalInterface
	public static interface Int extends EdgeWeightFunc, IntComparator {

		@Override
		default double weight(int e) {
			return weightInt(e);
		}

		public int weightInt(int e);

		@Override
		default int compare(int e1, int e2) {
			return Integer.compare(weightInt(e1), weightInt(e2));
		}

	}

}
