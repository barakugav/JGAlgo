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

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntSet;

public class IndexIntIdMapImplTest extends TestBase {

	@Test
	public void newCopyOf() {
		final Random rand = new Random(0xe0260dea11489d64L);
		final int n = 157;
		Int2IntMap idToIndex = new Int2IntOpenHashMap();
		while (idToIndex.size() < n) {
			int id = rand.nextInt(1000);
			if (idToIndex.containsKey(id))
				continue;
			idToIndex.put(id, idToIndex.size());
		}
		IntSet indices = range(n);
		IntSet ids = idToIndex.keySet();

		IndexIntIdMap map1 = fromMap(idToIndex);
		IndexIntIdMap map2 = IndexIntIdMapImpl.newCopyOf(map1, Optional.empty(), indices, false, false);
		for (int idx : indices) {
			assertEquals(map1.indexToIdInt(idx), map2.indexToIdInt(idx));
			assertEquals(map1.indexToIdInt(idx), map2.indexToIdIfExistInt(idx));
		}
		for (int id : ids) {
			assertEquals(map1.idToIndex(id), map2.idToIndex(id));
			assertEquals(map1.idToIndex(id), map2.idToIndexIfExist(id));
		}
		for (int idx = -15; idx < 0; idx++)
			assertEquals(-1, map2.indexToIdIfExistInt(idx));
		for (int idx = n; idx < n + 15; idx++)
			assertEquals(-1, map2.indexToIdIfExistInt(idx));
		for (int id = 0; id < 100; id++)
			assertEquals(map1.idToIndexIfExist(id), map2.idToIndexIfExist(id));
	}

	@Test
	public void newCopyOfNegativeId() {
		Int2IntMap idToIndex = new Int2IntOpenHashMap();
		idToIndex.put(-8, 0);
		IntSet indices = range(1);
		IndexIntIdMap map1 = fromMap(idToIndex);
		assertThrows(IllegalArgumentException.class,
				() -> IndexIntIdMapImpl.newCopyOf(map1, Optional.empty(), indices, false, false));
	}

	@Test
	public void newCopyOfDuplicateId() {
		IntSet indices = range(2);
		IndexIntIdMap map1 = new IndexIntIdMap() {
			@Override
			public int indexToIdInt(int index) {
				if (indices.contains(index))
					return 17;
				throw new IllegalArgumentException();
			}

			@Override
			public int indexToIdIfExistInt(int index) {
				if (indices.contains(index))
					return 17;
				return -1;
			}

			@Override
			public int idToIndex(int id) {
				if (id == 17)
					return 0;
				throw new IllegalArgumentException();
			}

			@Override
			public int idToIndexIfExist(int id) {
				if (id == 17)
					return 0;
				return -1;
			}
		};
		assertThrows(IllegalArgumentException.class,
				() -> IndexIntIdMapImpl.newCopyOf(map1, Optional.empty(), indices, false, false));
	}

	@Test
	public void newCopyOfWithReIndexing() {
		final Random rand = new Random(0xe0260dea11489d64L);
		final int n = 157;
		Int2IntMap idToIndex = new Int2IntOpenHashMap();
		while (idToIndex.size() < n) {
			int id = rand.nextInt(1000);
			if (idToIndex.containsKey(id))
				continue;
			idToIndex.put(id, idToIndex.size());
		}
		IntSet indices = range(n);
		IntSet ids = idToIndex.keySet();

		int[] origToReIndexed = indices.toIntArray();
		IntArrays.shuffle(origToReIndexed, rand);
		@SuppressWarnings("boxing")
		int[] reIndexedToOrig = range(n).mapToObj(x -> Pair.of(x, origToReIndexed[x]))
				.sorted((p1, p2) -> Integer.compare(p1.second(), p2.second())).mapToInt(p -> p.first()).toArray();

		IndexGraphBuilderImpl.ReIndexingMapImpl reindexing =
				new IndexGraphBuilderImpl.ReIndexingMapImpl(origToReIndexed, reIndexedToOrig);

		IndexIntIdMap map1 = fromMap(idToIndex);
		IndexIntIdMap map2 = IndexIntIdMapImpl.newCopyOf(map1, Optional.of(reindexing), indices, false, false);
		for (int idx : indices) {
			assertEquals(map1.indexToIdInt(reindexing.reIndexedToOrig(idx)), map2.indexToIdInt(idx));
			assertEquals(map1.indexToIdInt(reindexing.reIndexedToOrig(idx)), map2.indexToIdIfExistInt(idx));
		}
		for (int id : ids) {
			assertEquals(reindexing.origToReIndexed(map1.idToIndex(id)), map2.idToIndex(id));
			assertEquals(reindexing.origToReIndexed(map1.idToIndex(id)), map2.idToIndexIfExist(id));
		}
		for (int idx = -15; idx < 0; idx++)
			assertEquals(-1, map2.indexToIdIfExistInt(idx));
		for (int idx = n; idx < n + 15; idx++)
			assertEquals(-1, map2.indexToIdIfExistInt(idx));
		for (int id = 0; id < 100; id++) {
			int expected = map1.idToIndexIfExist(id);
			if (expected != -1)
				expected = reindexing.origToReIndexed(expected);
			assertEquals(expected, map2.idToIndexIfExist(id));
		}
	}

	@Test
	public void newCopyOfWithReIndexingNegativeId() {
		Int2IntMap idToIndex = new Int2IntOpenHashMap();
		idToIndex.put(8, 0);
		idToIndex.put(-8, 1);
		IntSet indices = range(1);
		IndexGraphBuilder.ReIndexingMap reindexing = new IndexGraphBuilder.ReIndexingMap() {
			@Override
			public int origToReIndexed(int orig) {
				return 1 - orig;
			}

			@Override
			public int reIndexedToOrig(int reindexed) {
				return 1 - reindexed;
			}
		};

		IndexIntIdMap map1 = fromMap(idToIndex);
		assertThrows(IllegalArgumentException.class,
				() -> IndexIntIdMapImpl.newCopyOf(map1, Optional.of(reindexing), indices, false, false));
	}

	@Test
	public void newCopyOfWithReIndexingDuplicateId() {
		IntSet indices = range(2);
		IndexIntIdMap map1 = new IndexIntIdMap() {
			@Override
			public int indexToIdInt(int index) {
				if (indices.contains(index))
					return 17;
				throw new IllegalArgumentException();
			}

			@Override
			public int indexToIdIfExistInt(int index) {
				if (indices.contains(index))
					return 17;
				return -1;
			}

			@Override
			public int idToIndex(int id) {
				if (id == 17)
					return 0;
				throw new IllegalArgumentException();
			}

			@Override
			public int idToIndexIfExist(int id) {
				if (id == 17)
					return 0;
				return -1;
			}
		};
		IndexGraphBuilder.ReIndexingMap reindexing = new IndexGraphBuilder.ReIndexingMap() {
			@Override
			public int origToReIndexed(int orig) {
				return 1 - orig;
			}

			@Override
			public int reIndexedToOrig(int reindexed) {
				return 1 - reindexed;
			}
		};

		assertThrows(IllegalArgumentException.class,
				() -> IndexIntIdMapImpl.newCopyOf(map1, Optional.of(reindexing), indices, false, false));
	}

	private static IndexIntIdMap fromMap(Int2IntMap map) {
		return new IndexIntIdMap() {

			int[] indexToId = map.int2IntEntrySet().stream()
					.sorted((e1, e2) -> Integer.compare(e1.getIntValue(), e2.getIntValue()))
					.mapToInt(Int2IntMap.Entry::getIntKey).toArray();

			@Override
			public int indexToIdInt(int index) {
				if (!(0 <= index && index < indexToId.length))
					throw new IllegalArgumentException();
				return indexToId[index];
			}

			@Override
			public int indexToIdIfExistInt(int index) {
				if (!(0 <= index && index < indexToId.length))
					return -1;
				return indexToId[index];
			}

			@Override
			public int idToIndex(int id) {
				if (!map.containsKey(id))
					throw new IllegalArgumentException();
				return map.get(id);
			}

			@Override
			public int idToIndexIfExist(int id) {
				if (!map.containsKey(id))
					return -1;
				return map.get(id);
			}

		};
	}

}
