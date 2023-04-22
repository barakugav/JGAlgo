package com.jgalgo;

/**
 * {@link DynamicTree} builder.
 *
 * @author Barak Ugav
 */
public class DynamicTreeBuilder {

	private boolean intWeights = false;
	private boolean supportNodeSize = false;
	private double weightLimit;

	/**
	 * Create a new builder.
	 * <p>
	 * By default, built {@link DynamicTree}s will support {@code double} weights
	 * and will not support the
	 * {@link DynamicTree#size(com.jgalgo.DynamicTree.Node)} operation.
	 */
	public DynamicTreeBuilder() {
	}

	/**
	 * Enable/disable the use of {@code int} (instead of {@code double}) as weights
	 * type.
	 *
	 * @param enable if {@code true}, the built dynamic trees will use integers to
	 *               represent ints, else they will use {@code double}.
	 * @return this builder
	 */
	public DynamicTreeBuilder setIntWeightsEnable(boolean enable) {
		intWeights = enable;
		return this;
	}

	/**
	 * Enable/disable the implementation of
	 * {@link DynamicTree#size(com.jgalgo.DynamicTree.Node)} operation.
	 *
	 * @param enable if {@code true}, the built dynamic trees will support the
	 *               {@link DynamicTree#size(com.jgalgo.DynamicTree.Node)} method,
	 *               else they will throw {@link UnsupportedOperationException}.
	 * @return this builder
	 */
	public DynamicTreeBuilder setNodeSizeEnable(boolean enable) {
		supportNodeSize = enable;
		return this;
	}

	/**
	 * Set the weight limit used by the built dynamic trees.
	 *
	 * @param weightLimit a limit for the maximum edge weights that will be used.
	 *                    The limit should be greater than the sum of each edge
	 *                    weight and the additional weights assigned using
	 *                    {@link DynamicTree#addWeight(com.jgalgo.DynamicTree.Node, double)}.
	 * @return this builder
	 */
	public DynamicTreeBuilder setWeightLimit(double weightLimit) {
		if (weightLimit <= 0)
			throw new IllegalArgumentException("invalid weight limit: " + weightLimit);
		this.weightLimit = weightLimit;
		return this;
	}

	/**
	 * Build a new dynamic tree based on the builder options.
	 *
	 * @return a new empty dynamic tree
	 * @throws IllegalStateException if {@link #setWeightLimit(double)} was not
	 *                               called before this method call
	 */
	public DynamicTree build() {
		if (weightLimit == 0)
			throw new IllegalStateException("Weight limit was not set");
		if (!intWeights && !supportNodeSize)
			return new DynamicTreeSplay(weightLimit);
		if (intWeights && !supportNodeSize)
			return new DynamicTreeSplayInt((int) weightLimit);
		if (!intWeights && supportNodeSize)
			return new DynamicTreeSplaySized(weightLimit);
		if (intWeights && supportNodeSize)
			return new DynamicTreeSplaySizedInt((int) weightLimit);
		throw new IllegalStateException();
	}

}
