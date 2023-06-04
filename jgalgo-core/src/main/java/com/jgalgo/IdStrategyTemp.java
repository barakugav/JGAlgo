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
 * A strategy used by {@link IndexGraph} to maintain IDs invariants.
 * <p>
 * An {@link IndexGraph} is a {@link Graph} in which the vertices and edges identifiers are <b>always</b>
 * {@code (0,1,2, ...,verticesNum-1)} and {@code (0,1,2, ...,edgesNum-1)}. This invariants allow for a great performance
 * boost, as a simple array or bitmap can be used to associate a value/weight/flag with each vertex/edge. But it does
 * come with a cost: to maintain the invariants, implementations may rename existing vertices or edges along the graph
 * lifetime. These renames are managed by a {@link IdStrategy} that can be accessed using
 * {@link IndexGraph#getVerticesIdStrategy()} or {@link IndexGraph#getEdgesIdStrategy()} which allow for a subscription
 * to these renames via {@link IdStrategy#addIdSwapListener(com.jgalgo.IdStrategy.IdSwapListener)}.
 *
 * @see    IndexGraph
 * @author Barak Ugav
 */
public interface IdStrategy {

	/**
	 * Add a listener that will be notified each time the strategy chooses to swap two IDs.
	 * <p>
	 * The strategy implementation might swap IDs to maintain its invariants. These swaps can be subscribed using this
	 * function, and the listener {@code IDSwapListener#idSwap(int, int)} will be called when a swap occur.
	 *
	 * @param listener an ID swap listener that will be called each time the strategy wap two IDs.
	 */
	public void addIdSwapListener(IdSwapListener listener);

	/**
	 * Remove an ID swap listener.
	 *
	 * @param listener the listener to remove
	 * @see            #addIdSwapListener(IdSwapListener)
	 */
	public void removeIdSwapListener(IdSwapListener listener);

	/**
	 * A listener that will be notified each time a strategy chooses to swap two IDs.
	 * <p>
	 * The strategy implementation might swap IDs to maintain its invariants. These swaps can be subscribed using this
	 * {@link IdStrategy#addIdSwapListener(IdSwapListener)}, and the listener will be called when a swap occur.
	 *
	 * @author Barak Ugav
	 */
	@FunctionalInterface
	public static interface IdSwapListener {

		/**
		 * A callback that is called when {@code id1} and {@code id2} are swapped.
		 *
		 * @param id1 the first id
		 * @param id2 the second id
		 */
		void idSwap(int id1, int id2);
	}

}
