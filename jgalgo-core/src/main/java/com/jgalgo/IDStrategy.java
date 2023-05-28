/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo;

/**
 * A strategy used by graphs to determine for each new vertex/edge its unique identifier, and maintain invariants on
 * removals.
 * <p>
 * Each vertex/edges in the graph is identified by a unique {@code int} ID, which is determined by a strategy. For
 * example, {@link com.jgalgo.IDStrategy.Continues} ensure that at all times the vertices IDs are
 * {@code 0,1,..., verticesNum-1}, and it might rename some vertices when a vertex is removed to maintain this
 * invariant. This rename can be subscribed using {@link com.jgalgo.IDStrategy#addIDSwapListener}. Another option for an
 * ID strategy is {@link com.jgalgo.IDStrategy.Fixed} which ensure once a vertex is assigned an ID, it will not change.
 * All the above hold for edges as well, not only vertices. There might be some performance differences between
 * different ID strategies.
 *
 * @author Barak Ugav
 */
public interface IDStrategy {

	/**
	 * An ID strategy that ensure the IDs of a graph vertices/edges are always {@code 0,1,..., n-1}.
	 * <p>
	 * When a graph is using this strategy, the graph implementation is allowed to store information of vertices/edges
	 * in a simple continues array, which is best for performance. In case no vertices/edges removal are required, the
	 * the IDs do not change and its equivalent to {@link Fixed}, therefore its the default one.
	 * <p>
	 * To ensure the invariants of this class, the implementation might rename some vertices/edges. These renames can be
	 * subscribed using {@link com.jgalgo.IDStrategy#addIDSwapListener}.
	 *
	 * @author Barak Ugav
	 */
	public static interface Continues extends IDStrategy {
	}

	/**
	 * An ID strategy that ensure the IDs of a graph vertices/edges never change once assigned.
	 * <p>
	 * When a graph is using this strategy, some sort of map is required to 'random' access information of
	 * vertices/edges, which is slightly less efficient than {@link IDStrategy.Continues} but may be more convenient
	 * when vertices/edges removal are required.
	 *
	 * @author Barak Ugav
	 */
	public static interface Fixed extends IDStrategy {
	}

	/**
	 * Add a listener that will be notified each time the strategy chooses to swap two IDs.
	 * <p>
	 * The strategy implementation might swap IDs to maintain its invariants. These swaps can be subscribed using this
	 * function, and the listener {@code IDSwapListener#idSwap(int, int)} will be called when a swap occur.
	 *
	 * @param listener an ID swap listener that will be called each time the strategy wap two IDs.
	 */
	public void addIDSwapListener(IDSwapListener listener);

	/**
	 * Remove an ID swap listener.
	 *
	 * @param listener the listener to remove
	 * @see            #addIDSwapListener(IDSwapListener)
	 */
	public void removeIDSwapListener(IDSwapListener listener);

	/**
	 * Add a listener that will be notified each time the strategy add or remove an id.
	 *
	 * @param listener a listener object that will be called when the strategy add or remove an id
	 */
	public void addIDAddRemoveListener(IDAddRemoveListener listener);

	/**
	 * Remove an ID add/remove listener.
	 *
	 * @param listener the listener to remove
	 */
	public void removeIDAddRemoveListener(IDAddRemoveListener listener);

	/**
	 * A listener that will be notified each time a strategy chooses to swap two IDs.
	 * <p>
	 * The strategy implementation might swap IDs to maintain its invariants. These swaps can be subscribed using this
	 * {@link IDStrategy#addIDSwapListener(IDSwapListener)}, and the listener will be called when a swap occur.
	 *
	 * @author Barak Ugav
	 */
	@FunctionalInterface
	public static interface IDSwapListener {

		/**
		 * A callback that is called when {@code id1} and {@code id2} are swapped.
		 *
		 * @param id1 the first id
		 * @param id2 the second id
		 */
		void idSwap(int id1, int id2);
	}

	/**
	 * A listener that will be notified each time a strategy add or remove an id.
	 *
	 * @author Barak Ugav
	 */
	public static interface IDAddRemoveListener {
		/**
		 * A callback that is called when {@code id} is added by the strategy.
		 *
		 * @param id the new id
		 */
		void idAdd(int id);

		/**
		 * A callback that is called when {@code id} is removed by the strategy.
		 *
		 * @param id the removed id
		 */
		void idRemove(int id);

		/**
		 * A callback that is called when all ids are removed from the strategy.
		 */
		void idsClear();
	}

}
