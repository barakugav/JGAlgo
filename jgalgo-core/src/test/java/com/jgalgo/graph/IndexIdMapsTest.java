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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.gen.GnpGraphGenerator;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class IndexIdMapsTest extends TestBase {

	@Test
	public void indexToIdIterator() {
		final long seed = 0xfd38baacf447aa9eL;
		final Random rand = new Random(seed);
		foreachBoolConfig((intGraph, edgesOrVertices) -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IntSet indicesSet = (IntSet) randElementsSubSet(g.indexGraph(), edgesOrVertices);
			IndexIdMap<Integer> map = edgesOrVertices ? g.indexGraphEdgesMap() : g.indexGraphVerticesMap();

			Set<Integer> iteratedIds = new HashSet<>();
			for (Iterator<Integer> it = IndexIdMaps.indexToIdIterator(indicesSet.iterator(), map); it.hasNext();)
				iteratedIds.add(it.next());
			assertEquals(indicesSet.intStream().mapToObj(map::indexToId).collect(Collectors.toSet()), iteratedIds);

			Set<Integer> removedIds = new HashSet<>();
			Set<Integer> nonRemovedIds = new HashSet<>();
			for (Iterator<Integer> it = IndexIdMaps.indexToIdIterator(indicesSet.iterator(), map); it.hasNext();) {
				Integer id = it.next();
				if (rand.nextBoolean()) {
					it.remove();
					removedIds.add(id);
				} else {
					nonRemovedIds.add(id);
				}
			}
			for (Integer removedId : removedIds)
				assertFalse(indicesSet.contains(map.idToIndex(removedId)));
			for (Integer nonRemovedId : nonRemovedIds)
				assertTrue(indicesSet.contains(map.idToIndex(nonRemovedId)));
		});
	}

	@Test
	public void idToIndexIterator() {
		final long seed = 0xe47e0556fb0eba74L;
		final Random rand = new Random(seed);
		foreachBoolConfig((intGraph, edgesOrVertices) -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			Set<Integer> idsSet = randElementsSubSet(g, edgesOrVertices);
			IndexIdMap<Integer> map = edgesOrVertices ? g.indexGraphEdgesMap() : g.indexGraphVerticesMap();

			IntSet iteratedIndices = new IntOpenHashSet();
			for (IntIterator it = IndexIdMaps.idToIndexIterator(idsSet.iterator(), map); it.hasNext();)
				iteratedIndices.add(it.nextInt());
			assertEquals(idsSet.stream().map(map::idToIndex).collect(Collectors.toSet()), iteratedIndices);

			IntSet removedIndices = new IntOpenHashSet();
			IntSet nonRemovedIndices = new IntOpenHashSet();
			for (IntIterator it = IndexIdMaps.idToIndexIterator(idsSet.iterator(), map); it.hasNext();) {
				int idx = it.nextInt();
				if (rand.nextBoolean()) {
					it.remove();
					removedIndices.add(idx);
				} else {
					nonRemovedIndices.add(idx);
				}
			}
			for (int removedIdx : removedIndices)
				assertFalse(idsSet.contains(map.indexToId(removedIdx)));
			for (int nonRemovedIdx : nonRemovedIndices)
				assertTrue(idsSet.contains(map.indexToId(nonRemovedIdx)));
		});
	}

	@Test
	public void indexToIdToIndexIterator() {
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			Iterator<Integer> idIter = g.vertices().iterator();
			IntIterator indexIter = IndexIdMaps.idToIndexIterator(idIter, viMap);
			Iterator<Integer> idIter2 = IndexIdMaps.indexToIdIterator(indexIter, viMap);
			assertTrue(idIter == idIter2);
		});
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			Iterator<Integer> idIter = g.vertices().iterator();
			IntIterator indexIter = IndexIdMaps.idToIndexIterator(idIter, viMap);
			Iterator<Integer> idIter2 = IndexIdMaps.indexToIdIterator(indexIter, wrapIndexIdMap(viMap));
			assertFalse(idIter == idIter2);
		});
	}

	@Test
	public void idToIndexToIdIterator() {
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IntIterator indexIter1 = g.indexGraph().vertices().iterator();
			Iterator<Integer> idIter = IndexIdMaps.indexToIdIterator(indexIter1, viMap);
			IntIterator indexIter2 = IndexIdMaps.idToIndexIterator(idIter, viMap);
			assertTrue(indexIter1 == indexIter2);
		});
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IntIterator indexIter1 = g.indexGraph().vertices().iterator();
			Iterator<Integer> idIter = IndexIdMaps.indexToIdIterator(indexIter1, viMap);
			IntIterator indexIter2 = IndexIdMaps.idToIndexIterator(idIter, wrapIndexIdMap(viMap));
			assertFalse(indexIter1 == indexIter2);
		});
	}

	@Test
	public void indexToIdEdgeIter() {
		final long seed = 0x7b422697871558a8L;
		final Random rand = new Random(seed);
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();

			Integer source;
			do {
				source = Graphs.randVertex(g, rand);
			} while (g.outEdges(source).isEmpty());
			int sourceIdx = viMap.idToIndex(source);
			IEdgeSet indexEdgesSet = g.indexGraph().outEdges(sourceIdx);
			Set<Integer> iteratedEdgesIds = new HashSet<>();
			for (EdgeIter<Integer, Integer> it = IndexIdMaps.indexToIdEdgeIter(g, indexEdgesSet.iterator()); it
					.hasNext();) {
				Integer peek = it.peekNext();
				Integer next = it.next();
				assertEquals(next, peek);

				assertEquals(source, it.source());
				assertEquals(g.edgeEndpoint(next, source), it.target());

				iteratedEdgesIds.add(next);
			}

			assertEquals(indexEdgesSet.intStream().mapToObj(eiMap::indexToId).collect(Collectors.toSet()),
					iteratedEdgesIds);
			assertEquals(g.outEdges(source), iteratedEdgesIds);

			Set<Integer> removedIds = new HashSet<>();
			Set<Integer> nonRemovedIds = new HashSet<>();
			for (EdgeIter<Integer, Integer> it = IndexIdMaps.indexToIdEdgeIter(g, indexEdgesSet.iterator()); it
					.hasNext();) {
				Integer id = it.next();
				if (rand.nextBoolean()) {
					it.remove();
					removedIds.add(id);
				} else {
					nonRemovedIds.add(id);
				}
			}
			Set<Integer> edges = g.edges();
			EdgeSet<Integer, Integer> outEdges = g.outEdges(source);
			for (Integer removedId : removedIds) {
				assertFalse(edges.contains(removedId));
				assertFalse(outEdges.contains(removedId));
			}
			for (Integer nonRemovedId : nonRemovedIds) {
				assertTrue(edges.contains(nonRemovedId));
				assertTrue(outEdges.contains(nonRemovedId));
			}
		});
	}

	@Test
	public void indexToIdCollection() {
		indexToIdCollectionAbstract(indicesSet -> (IntCollection) copyToRealCollection(indicesSet));
	}

	@Test
	public void indexToIdSet() {
		indexToIdCollectionAbstract(IntOpenHashSet::new);
	}

	@Test
	public void indexToIdList() {
		indexToIdCollectionAbstract(IntArrayList::new);

		final long seed = 0x136cebddbfbbff4bL;
		final Random rand = new Random(seed);
		foreachBoolConfig((intGraph, edgesOrVertices) -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			Set<Integer> allIds = edgesOrVertices ? g.edges() : g.vertices();
			IntSet indicesSet = (IntSet) randElementsSubSet(g.indexGraph(), edgesOrVertices);
			IndexIdMap<Integer> map = edgesOrVertices ? g.indexGraphEdgesMap() : g.indexGraphVerticesMap();

			/* addAll twice so indexOf() will be different than lastIndexOf() */
			IntList indexList = new IntArrayList();
			indexList.addAll(indicesSet);
			indexList.addAll(indicesSet);
			List<Integer> idList = IndexIdMaps.indexToIdList(indexList, map);

			/* indexOf() */
			List<Integer> idList2 = indexList.intStream().mapToObj(map::indexToId).collect(Collectors.toList());
			for (int i : range(indexList.size())) {
				int idx = indexList.getInt(i);
				Integer id = map.indexToId(idx);
				assertEquals(idList2.indexOf(id), idList.indexOf(id));
			}
			for (int i = 0; i < 10; i++) {
				Integer nonExistingId = Integer.valueOf(rand.nextInt());
				if (allIds.contains(nonExistingId))
					continue;
				assertEquals(-1, idList.indexOf(nonExistingId));
			}

			/* lastIndexOf() */
			for (int i : range(indexList.size())) {
				int idx = indexList.getInt(i);
				Integer id = map.indexToId(idx);
				assertEquals(idList2.lastIndexOf(id), idList.lastIndexOf(id));
			}
			for (int i = 0; i < 10; i++) {
				Integer nonExistingId = Integer.valueOf(rand.nextInt());
				if (allIds.contains(nonExistingId))
					continue;
				assertEquals(-1, idList.lastIndexOf(nonExistingId));
			}

			/* remove(index) */
			int sizeBeforeRemove = indexList.size();
			List<Integer> expected = indexList.intStream().mapToObj(map::indexToId).collect(Collectors.toList());
			int removeIdx = rand.nextInt(indexList.size());
			Integer removedIdExpected = expected.remove(removeIdx);
			Integer removedIdActual = idList.remove(removeIdx);
			assertEquals(removedIdExpected, removedIdActual);
			assertEquals(sizeBeforeRemove - 1, indexList.size());
			assertEquals(sizeBeforeRemove - 1, idList.size());
			assertEquals(indexList.intStream().mapToObj(map::indexToId).collect(Collectors.toList()),
					new ArrayList<>(idList));
		});
	}

	public void indexToIdCollectionAbstract(Function<IntSet, IntCollection> createIndexCollection) {
		final long seed = 0x112b0dd34340e8a8L;
		final Random rand = new Random(seed);
		foreachBoolConfig((intGraph, edgesOrVertices) -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			Set<Integer> allIds = edgesOrVertices ? g.edges() : g.vertices();
			IntSet indicesSet = (IntSet) randElementsSubSet(g.indexGraph(), edgesOrVertices);
			IndexIdMap<Integer> map = edgesOrVertices ? g.indexGraphEdgesMap() : g.indexGraphVerticesMap();

			IntCollection indexCollection = createIndexCollection.apply(indicesSet);
			Collection<Integer> idCollection;
			if (map instanceof IndexIntIdMap) {
				idCollection = IndexIdMaps.indexToIdCollection(indexCollection, (IndexIntIdMap) map);
			} else {
				idCollection = IndexIdMaps.indexToIdCollection(indexCollection, map);
			}

			/* size() */
			assertEquals(indexCollection.size(), idCollection.size());
			/* isEmpty() */
			assertEqualsBool(indexCollection.isEmpty(), idCollection.isEmpty());

			/* contains() */
			for (Integer id : allIds)
				assertEqualsBool(indexCollection.contains(map.idToIndex(id)), idCollection.contains(id));
			for (int i = 0; i < 10; i++) {
				Integer nonExistingId = Integer.valueOf(rand.nextInt());
				if (allIds.contains(nonExistingId))
					continue;
				assertFalse(idCollection.contains(nonExistingId));
			}

			/* iterator() */
			Set<Integer> iteratedIds = new HashSet<>();
			for (Iterator<Integer> it = idCollection.iterator(); it.hasNext();)
				iteratedIds.add(it.next());
			assertEquals(indicesSet.intStream().mapToObj(map::indexToId).collect(Collectors.toSet()), iteratedIds);

			/* remove() */
			int sizeBeforeRemove = indexCollection.size();
			idCollection.remove(idCollection.iterator().next());
			assertEquals(sizeBeforeRemove - 1, indexCollection.size());
			assertEquals(sizeBeforeRemove - 1, idCollection.size());
			assertEquals(indexCollection.intStream().mapToObj(map::indexToId).collect(Collectors.toSet()),
					new HashSet<>(idCollection));

			/* clear() */
			idCollection.clear();
			assertTrue(idCollection.isEmpty());
			assertTrue(indexCollection.isEmpty());
			indexCollection = createIndexCollection.apply(indicesSet);
			idCollection = IndexIdMaps.indexToIdCollection(indexCollection, map);
			assertFalse(indexCollection.isEmpty());
			indexCollection.clear();
			assertTrue(idCollection.isEmpty());
			assertTrue(indexCollection.isEmpty());
		});
	}

	@Test
	public void idToIndexCollection() {
		idToIndexCollectionOrSet(IndexIdMapsTest::copyToRealCollection);
	}

	@Test
	public void idToIndexSet() {
		idToIndexCollectionOrSet(HashSet::new);

		Graph<Integer, Integer> g = createGraph(false);
		Set<Integer> edgeSet = g.edges();
		IndexIdMap<Integer> map = g.indexGraphEdgesMap();
		IntCollection indexSet = IndexIdMaps.idToIndexCollection(edgeSet, map);
		assertTrue(indexSet instanceof IntSet);
	}

	@SuppressWarnings("boxing")
	@Test
	public void idToIndexList() {
		idToIndexCollectionOrSet(ArrayList::new);

		final long seed = 0x58c559c21cf688a8L;
		final Random rand = new Random(seed);
		foreachBoolConfig((intGraph, edgesOrVertices) -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IntSet allIndices = edgesOrVertices ? g.indexGraph().edges() : g.indexGraph().vertices();
			Set<Integer> idsSet = randElementsSubSet(g, edgesOrVertices);
			IndexIdMap<Integer> map = edgesOrVertices ? g.indexGraphEdgesMap() : g.indexGraphVerticesMap();

			/* addAll twice so indexOf() will be different than lastIndexOf() */
			List<Integer> idList = new ArrayList<>();
			idList.addAll(idsSet);
			idList.addAll(idsSet);
			IntList indexList = IndexIdMaps.idToIndexList(idList, map);

			/* get(index) */
			for (int i : range(idList.size())) {
				Integer id = idList.get(i);
				assertEquals(map.idToIndex(id), indexList.getInt(i));
			}

			/* indexOf() */
			List<Integer> indexList2 = idList.stream().map(map::idToIndex).collect(Collectors.toList());
			for (int i : range(idList.size())) {
				Integer id = idList.get(i);
				int idx = map.idToIndex(id);
				assertEquals(indexList2.indexOf(idx), indexList.indexOf(idx));
			}
			for (int i = 0; i < 10; i++) {
				int nonExistingIdx = rand.nextInt();
				if (allIndices.contains(nonExistingIdx))
					continue;
				assertEquals(-1, indexList.indexOf(nonExistingIdx));
			}

			/* lastIndexOf() */
			for (int i : range(idList.size())) {
				Integer id = idList.get(i);
				int idx = map.idToIndex(id);
				assertEquals(indexList2.lastIndexOf(idx), indexList.lastIndexOf(idx));
			}
			for (int i = 0; i < 10; i++) {
				int nonExistingIdx = rand.nextInt();
				if (allIndices.contains(nonExistingIdx))
					continue;
				assertEquals(-1, indexList.lastIndexOf(nonExistingIdx));
			}

			/* remove(element) */
			int sizeBeforeRemove = idList.size();
			IntList expected = new IntArrayList(idList.stream().map(map::idToIndex).collect(Collectors.toList()));
			int removeElement = expected.getInt(expected.indexOf(expected.getInt(rand.nextInt(idList.size()))));
			boolean expectedModified = expected.rem(removeElement);
			boolean actualModified = indexList.rem(removeElement);
			assertEqualsBool(expectedModified, actualModified);
			assertEquals(sizeBeforeRemove - 1, idList.size());
			assertEquals(sizeBeforeRemove - 1, indexList.size());
			assertEquals(idList.stream().map(map::idToIndex).collect(Collectors.toList()), new ArrayList<>(indexList));
			for (int i = 0; i < 10; i++) {
				int nonExistingIdx = rand.nextInt();
				if (allIndices.contains(nonExistingIdx))
					continue;
				assertFalse(indexList.rem(nonExistingIdx));
			}
			for (int i = 0; i < 10; i++) {
				int nonExistingIdx = allIndices.toIntArray()[rand.nextInt(allIndices.size())];
				if (indexList.contains(nonExistingIdx))
					continue;
				assertFalse(indexList.rem(nonExistingIdx));
			}

			/* remove(index) */
			sizeBeforeRemove = idList.size();
			expected = new IntArrayList(idList.stream().map(map::idToIndex).collect(Collectors.toList()));
			int removeIdx = rand.nextInt(idList.size());
			int removedIdxExpected = expected.removeInt(removeIdx);
			int removedIdxActual = indexList.removeInt(removeIdx);
			assertEquals(removedIdxExpected, removedIdxActual);
			assertEquals(sizeBeforeRemove - 1, idList.size());
			assertEquals(sizeBeforeRemove - 1, indexList.size());
			assertEquals(idList.stream().map(map::indexToId).collect(Collectors.toList()), new ArrayList<>(indexList));

			/* listIterator() */
			IntListIterator it = indexList.listIterator();
			for (int i : range(indexList.size())) {
				assertTrue(it.hasNext());
				assertEquals(i, it.nextIndex());
				int element = it.nextInt();
				assertEquals(indexList.getInt(i), element);
			}
			for (int i = indexList.size() - 1; i >= 0; i--) {
				assertTrue(it.hasPrevious());
				assertEquals(i, it.previousIndex());
				int element = it.previousInt();
				assertEquals(indexList.getInt(i), element);
			}
		});

		Graph<Integer, Integer> g = createGraph(false);
		List<Integer> listSet = new ArrayList<>(g.edges());
		IndexIdMap<Integer> map = g.indexGraphEdgesMap();
		IntCollection indexList = IndexIdMaps.idToIndexCollection(listSet, map);
		assertTrue(indexList instanceof IntList);
	}

	public void idToIndexCollectionOrSet(Function<Set<Integer>, Collection<Integer>> createIdCollection) {
		final long seed = 0xd71f63773c813234L;
		final Random rand = new Random(seed);
		foreachBoolConfig((intGraph, edgesOrVertices) -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IntSet allIndices = edgesOrVertices ? g.indexGraph().edges() : g.indexGraph().vertices();
			Set<Integer> idSet = randElementsSubSet(g, edgesOrVertices);
			IndexIdMap<Integer> map = edgesOrVertices ? g.indexGraphEdgesMap() : g.indexGraphVerticesMap();

			Collection<Integer> idCollection = createIdCollection.apply(idSet);
			IntCollection indexCollection;
			if (idCollection instanceof Set) {
				indexCollection = IndexIdMaps.idToIndexSet((Set<Integer>) idCollection, map);
			} else if (map instanceof IndexIntIdMap) {
				indexCollection = IndexIdMaps.idToIndexCollection(idCollection, (IndexIntIdMap) map);
			} else {
				indexCollection = IndexIdMaps.idToIndexCollection(idCollection, map);
			}

			/* size() */
			assertEquals(indexCollection.size(), idCollection.size());
			/* isEmpty() */
			assertEqualsBool(indexCollection.isEmpty(), idCollection.isEmpty());

			/* contains() */
			for (int idx : allIndices)
				assertEqualsBool(idCollection.contains(map.indexToId(idx)), indexCollection.contains(idx));
			for (int i = 0; i < 10; i++) {
				int nonExistingIdx = rand.nextInt();
				if (allIndices.contains(nonExistingIdx))
					continue;
				assertFalse(indexCollection.contains(nonExistingIdx));
			}

			/* iterator() */
			IntSet iteratedIndices = new IntOpenHashSet();
			for (IntIterator it = indexCollection.iterator(); it.hasNext();)
				iteratedIndices.add(it.nextInt());
			assertEquals(idSet.stream().map(map::idToIndex).collect(Collectors.toSet()), iteratedIndices);

			/* remove() */
			int sizeBeforeRemove = indexCollection.size();
			indexCollection.rem(indexCollection.iterator().nextInt());
			assertEquals(sizeBeforeRemove - 1, indexCollection.size());
			assertEquals(sizeBeforeRemove - 1, idCollection.size());
			assertEquals(idCollection.stream().map(map::idToIndex).collect(Collectors.toSet()),
					new HashSet<>(indexCollection));

			/* clear() */
			indexCollection.clear();
			assertTrue(idCollection.isEmpty());
			assertTrue(indexCollection.isEmpty());
			idCollection = createIdCollection.apply(idSet);
			indexCollection = IndexIdMaps.idToIndexCollection(idCollection, map);
			assertFalse(indexCollection.isEmpty());
			idCollection.clear();
			assertTrue(idCollection.isEmpty());
			assertTrue(indexCollection.isEmpty());
		});
	}

	@Test
	public void indexToIdToIndexCollection() {
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			Collection<Integer> idCol1 = copyToRealCollection(g.vertices());
			IntCollection indexCol = IndexIdMaps.idToIndexCollection(idCol1, viMap);
			Collection<Integer> idCol2 = IndexIdMaps.indexToIdCollection(indexCol, viMap);
			assertTrue(idCol1 == idCol2);
		});
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			Collection<Integer> idCol1 = copyToRealCollection(g.vertices());
			IntCollection indexCol = IndexIdMaps.idToIndexCollection(idCol1, viMap);
			Collection<Integer> idCol2 = IndexIdMaps.indexToIdCollection(indexCol, wrapIndexIdMap(viMap));
			assertFalse(idCol1 == idCol2);
		});
	}

	@Test
	public void idToIndexToIdCollection() {
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IntCollection indexCol1 = (IntCollection) copyToRealCollection(g.indexGraph().vertices());
			Collection<Integer> idCol = IndexIdMaps.indexToIdCollection(indexCol1, viMap);
			IntCollection indexCol2 = IndexIdMaps.idToIndexCollection(idCol, viMap);
			assertTrue(indexCol1 == indexCol2);
		});
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IntCollection indexCol1 = (IntCollection) copyToRealCollection(g.indexGraph().vertices());
			Collection<Integer> idCol = IndexIdMaps.indexToIdCollection(indexCol1, viMap);
			IntCollection indexCol2 = IndexIdMaps.idToIndexCollection(idCol, wrapIndexIdMap(viMap));
			assertFalse(indexCol1 == indexCol2);
		});
	}

	@Test
	public void indexToIdToIndexSet() {
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			Set<Integer> idSet = g.vertices();
			IntSet indexSet = IndexIdMaps.idToIndexSet(idSet, viMap);
			Set<Integer> idSet2 = IndexIdMaps.indexToIdSet(indexSet, viMap);
			assertTrue(idSet == idSet2);
		});
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			Set<Integer> idSet = g.vertices();
			IntSet indexSet = IndexIdMaps.idToIndexSet(idSet, viMap);
			Set<Integer> idSet2 = IndexIdMaps.indexToIdSet(indexSet, wrapIndexIdMap(viMap));
			assertFalse(idSet == idSet2);
		});
	}

	@Test
	public void idToIndexToIdSet() {
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IntSet indexSet1 = g.indexGraph().vertices();
			Set<Integer> idSet = IndexIdMaps.indexToIdSet(indexSet1, viMap);
			IntSet indexSet2 = IndexIdMaps.idToIndexSet(idSet, viMap);
			assertTrue(indexSet1 == indexSet2);
		});
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IntSet indexSet1 = g.indexGraph().vertices();
			Set<Integer> idSet = IndexIdMaps.indexToIdSet(indexSet1, viMap);
			IntSet indexSet2 = IndexIdMaps.idToIndexSet(idSet, wrapIndexIdMap(viMap));
			assertFalse(indexSet1 == indexSet2);
		});
	}

	@Test
	public void indexToIdToIndexList() {
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			List<Integer> idList = new IntArrayList(g.vertices());
			IntList indexList = IndexIdMaps.idToIndexList(idList, viMap);
			List<Integer> idList2 = IndexIdMaps.indexToIdList(indexList, viMap);
			assertTrue(idList == idList2);
		});
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			List<Integer> idList = new IntArrayList(g.vertices());
			IntList indexList = IndexIdMaps.idToIndexList(idList, viMap);
			List<Integer> idList2 = IndexIdMaps.indexToIdList(indexList, wrapIndexIdMap(viMap));
			assertFalse(idList == idList2);
		});
	}

	@Test
	public void idToIndexToIdList() {
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IntList indexList1 = new IntArrayList(g.indexGraph().vertices());
			List<Integer> idList = IndexIdMaps.indexToIdList(indexList1, viMap);
			IntList indexList2 = IndexIdMaps.idToIndexList(idList, viMap);
			assertTrue(indexList1 == indexList2);
		});
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = createGraph(intGraph);
			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
			IntList indexList1 = new IntArrayList(g.indexGraph().vertices());
			List<Integer> idList = IndexIdMaps.indexToIdList(indexList1, viMap);
			IntList indexList2 = IndexIdMaps.idToIndexList(idList, wrapIndexIdMap(viMap));
			assertFalse(indexList1 == indexList2);
		});
	}

	@Test
	public void indexToIdEdgeSetOfIntGraph() {
		Graph<Integer, Integer> g = createGraph(true);
		EdgeSet<Integer, Integer> edges = IndexIdMaps.indexToIdEdgeSet(g.indexGraph().outEdges(0), g);
		assertTrue(edges instanceof IEdgeSet);
	}

	private static Graph<Integer, Integer> createGraph(boolean intGraph) {
		final long seed = 0x97fa28ae01bfaf23L;
		GnpGraphGenerator<Integer, Integer> g =
				intGraph ? new GnpGraphGenerator<>(IntGraphFactory.undirected()) : new GnpGraphGenerator<>();
		g.seed(seed);
		g.vertices(range(24));
		g.edges(IdBuilderInt.defaultBuilder());
		g.edgeProbability(0.1);
		return g.generateMutable();
	}

	private static Set<Integer> randElementsSubSet(Graph<Integer, Integer> g, boolean edgesOrVertices) {
		final long seed = 0xd024886f76b43792L;
		final Random rand = new Random(seed);
		Set<Integer> elements = edgesOrVertices ? g.edges() : g.vertices();
		int s = 1 + rand.nextInt(elements.size() / 2 - 1);
		IntSet subSet = new IntOpenHashSet(s);
		for (IntList elements0 = new IntArrayList(elements); subSet.size() < s;)
			subSet.add(randElement(elements0, rand));
		return subSet;
	}

	@SuppressWarnings({ "unchecked", "boxing" })
	private static <T> Collection<T> copyToRealCollection(Collection<T> collection) {
		Map<Integer, T> map;
		if (collection instanceof IntCollection) {
			map = (Map<Integer, T>) new Int2IntOpenHashMap();
		} else {
			map = new Int2ObjectOpenHashMap<>();
		}
		for (T t : collection)
			map.put(map.size() + 1, t);
		return map.values();
	}

	@SuppressWarnings("unchecked")
	private static <K> IndexIdMap<K> wrapIndexIdMap(IndexIdMap<K> map) {
		if (map instanceof IndexIntIdMap) {
			IndexIntIdMap map0 = (IndexIntIdMap) map;
			return (IndexIdMap<K>) new IndexIntIdMap() {
				@Override
				public int indexToIdInt(int index) {
					return map0.indexToIdInt(index);
				}

				@Override
				public int indexToIdIfExistInt(int index) {
					return map0.indexToIdIfExistInt(index);
				}

				@Override
				public int idToIndex(int id) {
					return map0.idToIndex(id);
				}

				@Override
				public int idToIndexIfExist(int id) {
					return map0.idToIndexIfExist(id);
				}
			};
		} else {
			return new IndexIdMap<>() {
				@Override
				public K indexToId(int index) {
					return map.indexToId(index);
				}

				@Override
				public K indexToIdIfExist(int index) {
					return map.indexToIdIfExist(index);
				}

				@Override
				public int idToIndex(K id) {
					return map.idToIndex(id);
				}

				@Override
				public int idToIndexIfExist(K id) {
					return map.idToIndexIfExist(id);
				}
			};
		}
	}

}
