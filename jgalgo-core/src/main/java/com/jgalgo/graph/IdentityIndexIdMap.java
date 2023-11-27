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

import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntSet;

class IdentityIndexIdMap implements IndexIntIdMap {

	private final IntSet elementsSet;
	private final boolean isEdges;

	IdentityIndexIdMap(IntSet elementsSet, boolean isEdges) {
		this.elementsSet = elementsSet;
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
