package com.ugav.algo;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public abstract class IDStrategy {

	private final List<IDSwapListener> idSwapListeners = new CopyOnWriteArrayList<>();

	IDStrategy() {
	}

	public static class Continues extends IDStrategy {

		@Override
		int nextID(int numOfExisting) {
			return numOfExisting;
		}

		@Override
		int swapBeforeRemove(int numOfExisting, int id) {
			return numOfExisting - 1;
		}
	}

	public static class Fixed extends IDStrategy {

		private int counter;

		Fixed() {
			counter = 0;
		}

		@Override
		int nextID(int numOfExisting) {
			return counter++;
		}

		@Override
		int swapBeforeRemove(int numOfExisting, int id) {
			return id;
		}
	}

	static class Rnad extends IDStrategy {

		private final IntSet usedIDs = new IntOpenHashSet();
		private final Random rand = new Random();

		Rnad() {
		}

		@Override
		int nextID(int numOfExisting) {
			int id;
			do {
				id = rand.nextInt();
			} while (usedIDs.contains(id));
			usedIDs.add(id);
			return id;
		}

		@Override
		int swapBeforeRemove(int numOfExisting, int id) {
			return id;
		}
	}

	abstract int nextID(int numOfExisting);

	abstract int swapBeforeRemove(int numOfExisting, int id);

	void afterSwap(int id1, int id2) {
		for (IDSwapListener listener : idSwapListeners)
			listener.idSwap(id1, id2);
	}

	public void addIDSwapListener(IDSwapListener listener) {
		idSwapListeners.add(Objects.requireNonNull(listener));
	}

	public void removeIDSwapListener(IDSwapListener listener) {
		idSwapListeners.remove(listener);
	}

	@FunctionalInterface
	public static interface IDSwapListener {
		public void idSwap(int e1, int e2);
	}

}
