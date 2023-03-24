package com.ugav.jgalgo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

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
	 * best for performance. In case no vertices/edges removal are required, the the
	 * IDs do not change and its equivalent to {@link Fixed}, therefore its the
	 * default one.
	 *
	 * <p>
	 * To ensure the invariants of this class, the implementation might rename some
	 * vertices/edges. These renames can be subscribed using
	 * {@link com.ugav.jgalgo.IDStrategy#addIDSwapListener}.
	 *
	 * @author ugav
	 */
	public static class Continues extends IDStrategy {

		private int size;
		private final IntSet idSet;

		public Continues() {
			idSet = new AbstractIntSet() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public boolean contains(int key) {
					return key >= 0 && key < size();
				}

				@Override
				public IntIterator iterator() {
					return new IntIterator() {
						int u = 0;

						@Override
						public boolean hasNext() {
							return u < size();
						}

						@Override
						public int nextInt() {
							if (!hasNext())
								throw new NoSuchElementException();
							return u++;
						}
					};
				}
			};
		}

		@Override
		int newID() {
			return size++;
		}

		@Override
		void removeID(int id) {
			assert id == size - 1;
			size--;
		}

		@Override
		void clear() {
			size = 0;
		}

		@Override
		IntSet idSet() {
			return idSet;
		}

		@Override
		void ensureSize(int n) {
		}

		@Override
		int swapBeforeRemove(int id) {
			return size - 1;
		}
	}

	private abstract static class FixedAbstract extends IDStrategy {

		private final IntOpenHashSet ids;
		private final IntSet idsView;

		FixedAbstract() {
			ids = new IntOpenHashSet();
			idsView = IntSets.unmodifiable(ids);
		}

		@Override
		int newID() {
			int id = nextID();
			ids.add(id);
			return id;
		}

		abstract int nextID();

		@Override
		void removeID(int id) {
			ids.remove(id);
		}

		@Override
		void clear() {
			ids.clear();
		}

		@Override
		IntSet idSet() {
			return idsView;
		}

		@Override
		void ensureSize(int n) {
			ids.ensureCapacity(n);
		}

		@Override
		int swapBeforeRemove(int id) {
			return id;
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
	public static class Fixed extends FixedAbstract {

		private int counter;

		Fixed() {
			counter = 0;
		}

		@Override
		int nextID() {
			return counter++;
		}

	}

	static class Rnad extends FixedAbstract {

		private final Random rand = new Random();

		Rnad() {
		}

		@Override
		int nextID() {
			int id;
			do {
				id = rand.nextInt();
			} while (idSet().contains(id));
			return id;
		}
	}

	abstract int newID();

	abstract void removeID(int id);

	abstract void clear();

	abstract IntSet idSet();

	abstract void ensureSize(int n);

	abstract int swapBeforeRemove(int id);

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
