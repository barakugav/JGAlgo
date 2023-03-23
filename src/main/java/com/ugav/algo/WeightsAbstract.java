package com.ugav.algo;

abstract class WeightsAbstract<E> implements Weights<E> {

	final boolean isEdges;

	WeightsAbstract(boolean isEdges) {
		this.isEdges = isEdges;
	}

	abstract void keyAdd(int key);

	abstract void keyRemove(int key);

	abstract void keySwap(int k1, int k2);

}
