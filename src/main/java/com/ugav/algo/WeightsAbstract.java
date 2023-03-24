package com.ugav.algo;

import java.util.Collection;

import it.unimi.dsi.fastutil.ints.IntSet;

abstract class WeightsAbstract<E> implements Weights<E> {

	final boolean isEdges;
	boolean forceAdd;

	WeightsAbstract(boolean isEdges) {
		this.isEdges = isEdges;
	}

	abstract void keyAdd(int key);

	abstract void keyRemove(int key);

	abstract void keySwap(int k1, int k2);

	abstract IntSet keysSet();

	abstract Collection<E> values();

}
