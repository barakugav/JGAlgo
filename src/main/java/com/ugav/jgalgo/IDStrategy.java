package com.ugav.jgalgo;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A strategy that determine for each new graph vertex/edge its unique
 * identifier, and maintain its invariants on removals.
 *
 * <p>
 * Each vertex/edges in the graph is identified by a unique int ID, which is
 * determined by a strategy. For example,
 * {@link com.ugav.jgalgo.IDStrategy.Continues} ensure that at all times the
 * vertices IDs are {@code 0,1,..., verticesNum-1}, and it might rename some
 * vertices when a vertex is removed to maintain this invariant. This rename can
 * be subscribed using {@link com.ugav.jgalgo.IDStrategy#addIDSwapListener}.
 * Another option for an ID strategy is {@link com.ugav.jgalgo.IDStrategy.Fixed}
 * which ensure once a vertex is assigned an ID, it will not change. All the
 * stated above about vertices hold for edges as well. There might be some
 * performance differences between different ID strategies.
 *
 * @author ugav
 */
public abstract class IDStrategy {

	private final List<IDSwapListener> idSwapListeners = new CopyOnWriteArrayList<>();

	IDStrategy() {
	}

	/**
	 * An ID strategy that ensure the IDs of a graph vertices/edges are always
	 * {@code 0,1,..., n-1}.
	 *
	 * <p>
	 * When a graph is using this strategy, the graph implementation is allowed to
	 * store information of vertices/edges in a simple continues array, which is
	 * best for performance. In case no vertices/edges removal are required, the is
	 * no disadvantage of using this strategy, therefore its the default one.
	 *
	 * <p>
	 * To ensure the invariants of this class, the implementation might rename some
	 * vertices/edges. These renames can be subscribed using
	 * {@link com.ugav.jgalgo.IDStrategy#addIDSwapListener}.
	 *
	 * @author ugav
	 */
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

	/**
	 * An ID strategy that ensure the IDs of a graph vertices/edges never change
	 * once assigned.
	 *
	 * <p>
	 * When a graph is using this strategy, some sort of map is required to 'random'
	 * access information of vertices/edges, which is slightly less efficient than
	 * {@link IDStrategy.Continues} but may be more convenient when vertices/edges
	 * removal are required.
	 *
	 * @author ugav
	 */
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

	/**
	 * Add a listener that will be notified each time the strategy chooses to swap
	 * two IDs.
	 *
	 * <p>
	 * The strategy implementation might swap IDs to maintain its invariants. These
	 * swaps can be subscribed using this function, and the listener
	 * {@code IDSwapListener#idSwap(int, int)} will be called when a swap occur.
	 *
	 * @param listener an ID swap listener that will be called each time the
	 *                 strategy wap two IDs.
	 */
	public void addIDSwapListener(IDSwapListener listener) {
		idSwapListeners.add(Objects.requireNonNull(listener));
	}

	/**
	 * Remove an ID swap listener.
	 *
	 * @see #addIDSwapListener(IDSwapListener)
	 *
	 * @param listener the listener to remove
	 */
	public void removeIDSwapListener(IDSwapListener listener) {
		idSwapListeners.remove(listener);
	}

	@FunctionalInterface
	public static interface IDSwapListener {
		public void idSwap(int e1, int e2);
	}

}
