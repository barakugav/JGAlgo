package com.ugav.jgalgo;

import java.util.Collection;

abstract class WeightsAbstract<E> implements Weights<E> {

	final boolean isEdges;

	WeightsAbstract(boolean isEdges) {
		this.isEdges = isEdges;
	}

	abstract void keyAdd(int key);

	abstract void keyRemove(int key);

	abstract void keySwap(int k1, int k2);

	abstract Collection<E> values();

}
