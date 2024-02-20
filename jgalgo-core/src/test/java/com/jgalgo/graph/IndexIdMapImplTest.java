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
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class IndexIdMapImplTest extends TestBase {

	@Test
	public void newCopyOf() {
		final Random rand = new Random(0x12735e945bc549aL);
		final int n = 157;
		Object2IntMap<String> idToIndex = new Object2IntOpenHashMap<>();
		while (idToIndex.size() < n) {
			String id = String.valueOf(rand.nextInt(1000));
			if (idToIndex.containsKey(id))
				continue;
			idToIndex.put(id, idToIndex.size());
		}
		IntSet indices = range(n);
		Set<String> ids = idToIndex.keySet();

		IndexIdMap<String> map1 = fromMap(idToIndex);
		IndexIdMap<String> map2 = IndexIdMapImpl.newCopyOf(map1, Optional.empty(), indices, false, false);
		for (int idx : indices) {
			assertEquals(map1.indexToId(idx), map2.indexToId(idx));
			assertEquals(map1.indexToId(idx), map2.indexToIdIfExist(idx));
		}
		for (String id : ids) {
			assertEquals(map1.idToIndex(id), map2.idToIndex(id));
			assertEquals(map1.idToIndex(id), map2.idToIndexIfExist(id));
		}
		for (int idx : range(-15, 0))
			assertEquals(null, map2.indexToIdIfExist(idx));
		for (int idx : range(n, n + 15))
			assertEquals(null, map2.indexToIdIfExist(idx));
		for (int id0 : range(100)) {
			String id = String.valueOf(id0);
			assertEquals(map1.idToIndexIfExist(id), map2.idToIndexIfExist(id));
		}
	}

	@Test
	public void newCopyOfNullId() {
		Object2IntMap<String> idToIndex = new Object2IntOpenHashMap<>();
		idToIndex.put(null, 0);
		IntSet indices = range(1);
		IndexIdMap<String> map1 = fromMap(idToIndex);
		assertThrows(NullPointerException.class,
				() -> IndexIdMapImpl.newCopyOf(map1, Optional.empty(), indices, false, false));
	}

	@Test
	public void newCopyOfDuplicateId() {
		IntSet indices = range(2);
		IndexIdMap<String> map1 = new IndexIdMap<>() {
			@Override
			public String indexToId(int index) {
				if (indices.contains(index))
					return String.valueOf(17);
				throw new IllegalArgumentException();
			}

			@Override
			public String indexToIdIfExist(int index) {
				if (indices.contains(index))
					return String.valueOf(17);
				return null;
			}

			@Override
			public int idToIndex(String id) {
				if (id.equals("17"))
					return 0;
				throw new IllegalArgumentException();
			}

			@Override
			public int idToIndexIfExist(String id) {
				if (id.equals("17"))
					return 0;
				return -1;
			}
		};
		assertThrows(IllegalArgumentException.class,
				() -> IndexIdMapImpl.newCopyOf(map1, Optional.empty(), indices, false, false));
	}

	@Test
	public void newCopyOfWithReIndexing() {
		final Random rand = new Random(0x8823e12172c8fbc4L);
		final int n = 157;
		Object2IntMap<String> idToIndex = new Object2IntOpenHashMap<>();
		while (idToIndex.size() < n) {
			String id = String.valueOf(rand.nextInt(1000));
			if (idToIndex.containsKey(id))
				continue;
			idToIndex.put(id, idToIndex.size());
		}
		IntSet indices = range(n);
		Set<String> ids = idToIndex.keySet();

		int[] origToReIndexed = indices.toIntArray();
		IntArrays.shuffle(origToReIndexed, rand);

		IndexGraphBuilder.ReIndexingMap reindexing = new IndexGraphBuilder.ReIndexingMap(origToReIndexed);

		IndexIdMap<String> map1 = fromMap(idToIndex);
		IndexIdMap<String> map2 = IndexIdMapImpl.newCopyOf(map1, Optional.of(reindexing), indices, false, false);
		for (int idx : indices) {
			assertEquals(map1.indexToId(reindexing.inverse().map(idx)), map2.indexToId(idx));
			assertEquals(map1.indexToId(reindexing.inverse().map(idx)), map2.indexToIdIfExist(idx));
		}
		for (String id : ids) {
			assertEquals(reindexing.map(map1.idToIndex(id)), map2.idToIndex(id));
			assertEquals(reindexing.map(map1.idToIndex(id)), map2.idToIndexIfExist(id));
		}
		for (int idx : range(-15, 0))
			assertEquals(null, map2.indexToIdIfExist(idx));
		for (int idx : range(n, n + 15))
			assertEquals(null, map2.indexToIdIfExist(idx));
		for (int id0 : range(100)) {
			String id = String.valueOf(id0);
			int expected = map1.idToIndexIfExist(id);
			if (expected >= 0)
				expected = reindexing.map(expected);
			assertEquals(expected, map2.idToIndexIfExist(id));
		}
	}

	@Test
	public void newCopyOfWithReIndexingNullId() {
		Object2IntMap<String> idToIndex = new Object2IntOpenHashMap<>();
		idToIndex.put(String.valueOf(8), 0);
		idToIndex.put(null, 1);
		IntSet indices = range(2);
		IndexGraphBuilder.ReIndexingMap reindexing = new IndexGraphBuilder.ReIndexingMap(new int[] { 1, 0 });

		IndexIdMap<String> map1 = fromMap(idToIndex);
		assertThrows(NullPointerException.class,
				() -> IndexIdMapImpl.newCopyOf(map1, Optional.of(reindexing), indices, false, false));
	}

	@Test
	public void newCopyOfWithReIndexingDuplicateId() {
		IntSet indices = range(2);
		IndexIdMap<String> map1 = new IndexIdMap<>() {
			@Override
			public String indexToId(int index) {
				if (indices.contains(index))
					return "17";
				throw new IllegalArgumentException();
			}

			@Override
			public String indexToIdIfExist(int index) {
				if (indices.contains(index))
					return "17";
				return null;
			}

			@Override
			public int idToIndex(String id) {
				if (id.equals("17"))
					return 0;
				throw new IllegalArgumentException();
			}

			@Override
			public int idToIndexIfExist(String id) {
				if (id.equals("17"))
					return 0;
				return -1;
			}
		};
		IndexGraphBuilder.ReIndexingMap reindexing = new IndexGraphBuilder.ReIndexingMap(new int[] { 1, 0 });

		assertThrows(IllegalArgumentException.class,
				() -> IndexIdMapImpl.newCopyOf(map1, Optional.of(reindexing), indices, false, false));
	}

	private static IndexIdMap<String> fromMap(Object2IntMap<String> map) {
		return new IndexIdMap<>() {

			String[] indexToId = map
					.object2IntEntrySet()
					.stream()
					.sorted((e1, e2) -> Integer.compare(e1.getIntValue(), e2.getIntValue()))
					.map(Object2IntMap.Entry::getKey)
					.toArray(String[]::new);

			@Override
			public String indexToId(int index) {
				if (!(0 <= index && index < indexToId.length))
					throw new IllegalArgumentException();
				return indexToId[index];
			}

			@Override
			public String indexToIdIfExist(int index) {
				if (!(0 <= index && index < indexToId.length))
					return null;
				return indexToId[index];
			}

			@Override
			public int idToIndex(String id) {
				if (!map.containsKey(id))
					throw new IllegalArgumentException();
				return map.getInt(id);
			}

			@Override
			public int idToIndexIfExist(String id) {
				if (!map.containsKey(id))
					return -1;
				return map.getInt(id);
			}

		};
	}

}
