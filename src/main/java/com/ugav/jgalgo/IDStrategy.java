package com.ugav.jgalgo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
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
 * @author Barak Ugav
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
	 * @author Barak Ugav
	 */
	public static class Continues extends IDStrategy {

		private int size;
		private final IntSet idSet;

		Continues(int initSize) {
			if (initSize < 0)
				throw new IllegalArgumentException();
			size = initSize;
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
		int newIdx() {
			return size++;
		}

		@Override
		void removeIdx(int idx) {
			assert idx == size - 1;
			assert size > 0;
			size--;
		}

		@Override
		void clear() {
			size = 0;
		}

		@Override
		int idToIdx(int id) {
			if (!(0 <= id && id < size))
				throw new IndexOutOfBoundsException(id);
			return id;
		}

		@Override
		int idxToId(int idx) {
			checkIdx(idx);
			return idx;
		}

		@Override
		IntSet idSet() {
			return idSet;
		}

		@Override
		void ensureSize(int n) {
		}

		@Override
		void idxSwap(int idx1, int idx2) {
			int id1 = idxToId(idx1), id2 = idxToId(idx2);
			notifyIDSwap(id1, id2);
		}

		private void checkIdx(int idx) {
			if (!(0 <= idx && idx < size))
				throw new IndexOutOfBoundsException(idx);
		}
	}

	private abstract static class FixedAbstract extends IDStrategy {

		private final Int2IntOpenHashMap idToIdx;
		private final DataContainer.Int idxToId;
		private final IntSet idsView; // move to graph abstract implementation

		FixedAbstract() {
			idToIdx = new Int2IntOpenHashMap();
			idsView = IntSets.unmodifiable(idToIdx.keySet());
			idxToId = new DataContainer.Int(0, 0);
		}

		@Override
		int newIdx() {
			int idx = idToIdx.size();
			int id = nextID();
			assert id >= 0;
			idToIdx.put(id, idx);
			idxToId.add(idx);
			idxToId.set(idx, id);
			return idx;
		}

		abstract int nextID();

		@Override
		void removeIdx(int idx) {
			assert idx == idxToId.size - 1;
			assert idxToId.size > 0;
			final int id = idxToId.getInt(idx);
			idxToId.remove(idx);
			idToIdx.remove(id);
		}

		@Override
		void clear() {
			idToIdx.clear();
			idxToId.clear();
		}

		@Override
		int idToIdx(int id) {
			if (!idToIdx.containsKey(id))
				throw new IndexOutOfBoundsException(id);
			return idToIdx.get(id);
		}

		@Override
		int idxToId(int idx) {
			return idxToId.getInt(idx);
		}

		@Override
		IntSet idSet() {
			return idsView;
		}

		@Override
		void ensureSize(int n) {
			idToIdx.ensureCapacity(n);
		}

		@Override
		void idxSwap(int idx1, int idx2) {
			int id1 = idxToId.getInt(idx1);
			int id2 = idxToId.getInt(idx2);
			idxToId.set(idx1, id2);
			idxToId.set(idx2, id1);
			int oldIdx1 = idToIdx.put(id1, idx2);
			int oldIdx2 = idToIdx.put(id2, idx1);
			assert idx1 == oldIdx1;
			assert idx2 == oldIdx2;

			// The user IDs were not changed, no need to call notifyIDSwap
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
	 * @author Barak Ugav
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

	static class Rand extends FixedAbstract {

		private final Random rand = new Random();

		Rand() {
		}

		@Override
		int nextID() {
			for (;;) {
				int id = rand.nextInt();
				if (id >= 0 && !idSet().contains(id))
					return id;
			}
		}
	}

	abstract int newIdx();

	abstract void removeIdx(int idx);

	abstract void clear();

	abstract int idToIdx(int id);

	abstract int idxToId(int idx);

	abstract IntSet idSet();

	int isSwapNeededBeforeRemove(int idx) {
		int size = idSet().size();
		if (!(0 <= idx && idx < size))
			throw new IndexOutOfBoundsException(idx);
		return size - 1;
	}

	abstract void idxSwap(int idx1, int idx2);

	void notifyIDSwap(int id1, int id2) {
		for (IDSwapListener listener : idSwapListeners)
			listener.idSwap(id1, id2);
	}

	abstract void ensureSize(int n);

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
