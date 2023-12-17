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
package com.jgalgo.graph;

import java.util.Objects;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntSet;

class IdentityIndexIdMap implements IndexIntIdMap {

	/*
	 * TODO: instead of using this class, we can use an empty IndexIntIdMapImpl by modifying it to check if it is an
	 * identify map in case id/index is not in range (all ids/indices will be out of range in an empty map). By doing
	 * so, the number of implementations of IndexIntIdMap will be 1 and number of implementations of IndexIdMap will be
	 * 2. There is a 'cliff' of performance for more than two implementations for an interface in most JVMs. Consider
	 * this for performance.
	 */

	private final IntSet elementsSet;
	private final boolean isEdges;

	IdentityIndexIdMap(IntSet elementsSet, boolean isEdges) {
		this.elementsSet = Objects.requireNonNull(elementsSet);
		this.isEdges = isEdges;
	}

	@Override
	public int indexToIdInt(int index) {
		Assertions.Graphs.checkId(index, elementsSet.size(), isEdges);
		return index;
	}

	@Override
	public int indexToIdIfExistInt(int index) {
		return 0 <= index && index < elementsSet.size() ? index : -1;
	}

	@Override
	public int idToIndex(int id) {
		return indexToIdInt(id);
	}

	@Override
	public int idToIndexIfExist(int id) {
		return indexToIdIfExistInt(id);
	}

}
