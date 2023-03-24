package com.ugav.jgalgo;

@FunctionalInterface
public interface EdgeWeightFunc {

	public double weight(int e);

	@FunctionalInterface
	public static interface Int extends EdgeWeightFunc {

		@Override
		default double weight(int e) {
			return weightInt(e);
		}

		public int weightInt(int e);

	}

}
