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

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Static methods class for {@linkplain IndexIntIdMap index-id maps}.
 *
 * @author Barak Ugav
 */
public class IndexIdMaps {

	private IndexIdMaps() {}

	/**
	 * Create an IDs iterator from an iterator of indices.
	 *
	 * @param  <K>       the type of IDs
	 * @param  indexIter an iterator of indices
	 * @param  map       index-id mapping
	 * @return           an iterator that iterate over the IDs matching the indices iterated by the original
	 *                   index-iterator
	 */
	@SuppressWarnings("unchecked")
	public static <K> Iterator<K> indexToIdIterator(IntIterator indexIter, IndexIdMap<K> map) {
		if (map instanceof IndexIntIdMap) {
			return (Iterator<K>) new IndexToIntIdIterator(indexIter, (IndexIntIdMap) map);
		} else {
			return new IndexToIdIterator<>(indexIter, map);
		}
	}

	/**
	 * Create an IDs iterator from an iterator of indices.
	 *
	 * @param  indexIter an iterator of indices
	 * @param  map       index-id mapping
	 * @return           an iterator that iterate over the IDs matching the indices iterated by the original
	 *                   index-iterator
	 */
	public static IntIterator indexToIdIterator(IntIterator indexIter, IndexIntIdMap map) {
		return new IndexToIntIdIterator(indexIter, map);
	}

	/**
	 * Create an indices iterator from an iterator of IDs.
	 *
	 * @param  <K>    the type of IDs
	 * @param  idIter an iterator of IDs
	 * @param  map    index-id mapping
	 * @return        an iterator that iterate over the indices matching the IDs iterated by the original ID-iterator
	 */
	@SuppressWarnings("unchecked")
	public static <K> IntIterator idToIndexIterator(Iterator<K> idIter, IndexIdMap<K> map) {
		if (map instanceof IndexIntIdMap) {
			return new IntIdToIndexIterator(IntAdapters.asIntIterator((Iterator<Integer>) idIter), (IndexIntIdMap) map);
		} else {
			return new IdToIndexIterator<>(idIter, map);
		}
	}

	private static class IndexToIdIterator<K> implements java.util.Iterator<K> {

		private final IntIterator indexIt;
		private final IndexIdMap<K> map;

		IndexToIdIterator(IntIterator idxIt, IndexIdMap<K> map) {
			this.indexIt = Objects.requireNonNull(idxIt);
			this.map = Objects.requireNonNull(map);
		}

		IntIterator indexIt() {
			return indexIt;
		}

		IndexIdMap<K> map() {
			return map;
		}

		@Override
		public boolean hasNext() {
			return indexIt.hasNext();
		}

		@Override
		public K next() {
			return map.indexToId(indexIt.nextInt());
		}

		@Override
		public void remove() {
			indexIt.remove();
		}
	}

	private static class IndexToIntIdIterator implements IntIterator {

		private final IntIterator indexIt;
		private final IndexIntIdMap map;

		IndexToIntIdIterator(IntIterator idxIt, IndexIntIdMap map) {
			this.indexIt = Objects.requireNonNull(idxIt);
			this.map = Objects.requireNonNull(map);
		}

		IntIterator indexIt() {
			return indexIt;
		}

		IndexIntIdMap map() {
			return map;
		}

		@Override
		public boolean hasNext() {
			return indexIt.hasNext();
		}

		@Override
		public int nextInt() {
			return map.indexToIdInt(indexIt.nextInt());
		}

		@Override
		public void remove() {
			indexIt.remove();
		}
	}

	private static class IdToIndexIterator<K> implements IntIterator {

		private final Iterator<K> idIt;
		private final IndexIdMap<K> map;

		IdToIndexIterator(Iterator<K> idIt, IndexIdMap<K> map) {
			this.idIt = Objects.requireNonNull(idIt);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public boolean hasNext() {
			return idIt.hasNext();
		}

		@Override
		public int nextInt() {
			return map.idToIndex(idIt.next());
		}

		@Override
		public void remove() {
			idIt.remove();
		}
	}

	private static class IntIdToIndexIterator implements IntIterator {

		private final IntIterator idIt;
		private final IndexIntIdMap map;

		IntIdToIndexIterator(IntIterator idIt, IndexIntIdMap map) {
			this.idIt = Objects.requireNonNull(idIt);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public boolean hasNext() {
			return idIt.hasNext();
		}

		@Override
		public int nextInt() {
			return map.idToIndex(idIt.nextInt());
		}

		@Override
		public void remove() {
			idIt.remove();
		}
	}

	/**
	 * Create an {@link IEdgeIter} that return IDs of vertices and edges from an {@link IEdgeIter} that return indices
	 * of vertices and edges.
	 *
	 * @param  <V>       the vertices type
	 * @param  <E>       the edges type
	 * @param  g         the graph
	 * @param  indexIter an {@link IEdgeIter} that return indices of vertices and edges
	 * @return           {@link IEdgeIter} that return IDs of vertices and edges matching the indices of vertices and
	 *                   edges returned by the original index-iterator
	 */
	@SuppressWarnings("unchecked")
	public static <V, E> EdgeIter<V, E> indexToIdEdgeIter(Graph<V, E> g, IEdgeIter indexIter) {
		if (g instanceof IntGraph) {
			return (EdgeIter<V, E>) indexToIdEdgeIter((IntGraph) g, indexIter);
		} else {
			return new IndexToIdEdgeIter<>(g, indexIter);
		}
	}

	/**
	 * Create an {@link IEdgeIter} that return IDs of vertices and edges from an {@link IEdgeIter} that return indices
	 * of vertices and edges.
	 *
	 * @param  g         the graph
	 * @param  indexIter an {@link IEdgeIter} that return indices of vertices and edges
	 * @return           {@link IEdgeIter} that return IDs of vertices and edges matching the indices of vertices and
	 *                   edges returned by the original index-iterator
	 */
	public static IEdgeIter indexToIdEdgeIter(IntGraph g, IEdgeIter indexIter) {
		return new IndexToIntIdEdgeIter(g, indexIter);
	}

	private static class IndexToIdEdgeIter<V, E> extends IndexToIdIterator<E> implements EdgeIter<V, E> {
		private final IndexIdMap<V> viMap;

		IndexToIdEdgeIter(Graph<V, E> g, IEdgeIter indexIt) {
			super(indexIt, g.indexGraphEdgesMap());
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		IEdgeIter indexIt() {
			return (IEdgeIter) super.indexIt();
		}

		IndexIdMap<E> eiMap() {
			return super.map();
		}

		@Override
		public E peekNext() {
			return eiMap().indexToId(indexIt().peekNextInt());
		}

		@Override
		public V source() {
			return viMap.indexToId(indexIt().sourceInt());
		}

		@Override
		public V target() {
			return viMap.indexToId(indexIt().targetInt());
		}
	}

	private static class IndexToIntIdEdgeIter extends IndexToIntIdIterator implements IEdgeIter {
		private final IndexIntIdMap viMap;

		IndexToIntIdEdgeIter(IntGraph g, IEdgeIter indexIt) {
			super(indexIt, g.indexGraphEdgesMap());
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		IEdgeIter indexIt() {
			return (IEdgeIter) super.indexIt();
		}

		IndexIntIdMap eiMap() {
			return super.map();
		}

		@Override
		public int peekNextInt() {
			return eiMap().indexToIdInt(indexIt().peekNextInt());
		}

		@Override
		public int sourceInt() {
			return viMap.indexToIdInt(indexIt().sourceInt());
		}

		@Override
		public int targetInt() {
			return viMap.indexToIdInt(indexIt().targetInt());
		}
	}

	/**
	 * Create an IDs collection from a collection of indices.
	 *
	 * @param  <K>             the type of IDs
	 * @param  indexCollection a collection of indices
	 * @param  map             index-id mapping
	 * @return                 a collection that contain IDs matching the indices contained in the original
	 *                         index-collection
	 */
	@SuppressWarnings("unchecked")
	public static <K> Collection<K> indexToIdCollection(IntCollection indexCollection, IndexIdMap<K> map) {
		if (indexCollection instanceof IntSet) {
			return indexToIdSet((IntSet) indexCollection, map);
		} else if (indexCollection instanceof IntList) {
			return indexToIdList((IntList) indexCollection, map);
		} else if (map instanceof IndexIntIdMap) {
			return (Collection<K>) new IndexToIntIdCollection(indexCollection, (IndexIntIdMap) map);
		} else {
			return new IndexToIdCollection<>(indexCollection, map);
		}
	}

	/**
	 * Create an int IDs collection from a collection of indices.
	 *
	 * @param  indexCollection a collection of indices
	 * @param  map             index-id mapping
	 * @return                 a collection that contain IDs matching the indices contained in the original
	 *                         index-collection
	 */
	public static IntCollection indexToIdCollection(IntCollection indexCollection, IndexIntIdMap map) {
		return (IntCollection) indexToIdCollection(indexCollection, (IndexIdMap<Integer>) map);
	}

	private static class IndexToIdCollection<K> extends AbstractCollection<K> {

		final IntCollection indexC;
		final IndexIdMap<K> map;

		IndexToIdCollection(IntCollection indexC, IndexIdMap<K> map) {
			this.indexC = Objects.requireNonNull(indexC);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public int size() {
			return indexC.size();
		}

		@Override
		public boolean isEmpty() {
			return indexC.isEmpty();
		}

		@Override
		public void clear() {
			indexC.clear();
		}

		@Override
		public Iterator<K> iterator() {
			return new IndexToIdIterator<>(indexC.iterator(), map);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean contains(Object key) {
			return indexC.contains(map.idToIndexIfExist((K) key));
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove(Object key) {
			return indexC.rem(map.idToIndexIfExist((K) key));
		}
	}

	private static class IndexToIntIdCollection extends AbstractIntCollection {

		final IntCollection indexC;
		final IndexIntIdMap map;

		IndexToIntIdCollection(IntCollection indexC, IndexIntIdMap map) {
			this.indexC = Objects.requireNonNull(indexC);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public int size() {
			return indexC.size();
		}

		@Override
		public boolean isEmpty() {
			return indexC.isEmpty();
		}

		@Override
		public void clear() {
			indexC.clear();
		}

		@Override
		public IntIterator iterator() {
			return new IndexToIntIdIterator(indexC.iterator(), map);
		}

		@Override
		public boolean contains(int key) {
			return indexC.contains(map.idToIndexIfExist(key));
		}

		@Override
		public boolean rem(int key) {
			return indexC.rem(map.idToIndexIfExist(key));
		}
	}

	/**
	 * Create an IDs set from a set of indices.
	 *
	 * @param  <K>      the type of IDs
	 * @param  indexSet a set of indices
	 * @param  map      index-id mapping
	 * @return          a set that contain IDs matching the indices contained in the original index-set
	 */
	@SuppressWarnings("unchecked")
	public static <K> Set<K> indexToIdSet(IntSet indexSet, IndexIdMap<K> map) {
		if (map instanceof IndexIntIdMap) {
			return (Set<K>) indexToIdSet(indexSet, (IndexIntIdMap) map);
		} else {
			return new IndexToIdSet<>(indexSet, map);
		}
	}

	/**
	 * Create an IDs set from a set of indices.
	 *
	 * @param  indexSet a set of indices
	 * @param  map      index-id mapping
	 * @return          a set that contain IDs matching the indices contained in the original index-set
	 */
	public static IntSet indexToIdSet(IntSet indexSet, IndexIntIdMap map) {
		return new IndexToIntIdSet(indexSet, map);
	}

	private static class IndexToIdSet<K> extends AbstractSet<K> {

		final IntCollection idxSet;
		final IndexIdMap<K> map;

		IndexToIdSet(IntSet idxSet, IndexIdMap<K> map) {
			this.idxSet = Objects.requireNonNull(idxSet);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public int size() {
			return idxSet.size();
		}

		@Override
		public boolean isEmpty() {
			return idxSet.isEmpty();
		}

		@Override
		public void clear() {
			idxSet.clear();
		}

		@Override
		public Iterator<K> iterator() {
			return new IndexToIdIterator<>(idxSet.iterator(), map);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean contains(Object key) {
			return idxSet.contains(map.idToIndexIfExist((K) key));
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove(Object key) {
			return idxSet.rem(map.idToIndexIfExist((K) key));
		}
	}

	private static class IndexToIntIdSet extends AbstractIntSet {

		final IntCollection idxSet;
		final IndexIntIdMap map;

		IndexToIntIdSet(IntSet idxSet, IndexIntIdMap map) {
			this.idxSet = Objects.requireNonNull(idxSet);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public int size() {
			return idxSet.size();
		}

		@Override
		public boolean isEmpty() {
			return idxSet.isEmpty();
		}

		@Override
		public void clear() {
			idxSet.clear();
		}

		@Override
		public IntIterator iterator() {
			return new IndexToIntIdIterator(idxSet.iterator(), map);
		}

		@Override
		public boolean contains(int key) {
			return idxSet.contains(map.idToIndexIfExist(key));
		}
	}

	/**
	 * Create an indices collection from a collection of IDs.
	 *
	 * @param  <K>          the type of IDs
	 * @param  idCollection a collection of IDs
	 * @param  map          index-id mapping
	 * @return              a collection that contain indices matching the IDs contained in the original ID-collection
	 */
	@SuppressWarnings("unchecked")
	public static <K> IntCollection idToIndexCollection(Collection<K> idCollection, IndexIdMap<K> map) {
		if (idCollection instanceof Set) {
			// TODO
			// return new IdToIndexSet<>((Set<K>) idCollection, map);
			return new IdToIndexCollection<>(idCollection, map);

		} else if (idCollection instanceof List) {
			// TODO
			// return new IdToIndexList<>((List<K>) idCollection, map);
			return new IdToIndexCollection<>(idCollection, map);

		} else if (map instanceof IndexIntIdMap) {
			return new IntIdToIndexCollection(IntAdapters.asIntCollection((Collection<Integer>) idCollection),
					(IndexIntIdMap) map);
		} else {
			return new IdToIndexCollection<>(idCollection, map);
		}
	}

	private static class IdToIndexCollection<K> extends AbstractIntCollection {

		private final Collection<K> idC;
		private final IndexIdMap<K> map;

		IdToIndexCollection(Collection<K> idC, IndexIdMap<K> map) {
			this.idC = Objects.requireNonNull(idC);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public int size() {
			return idC.size();
		}

		@Override
		public boolean isEmpty() {
			return idC.isEmpty();
		}

		@Override
		public void clear() {
			idC.clear();
		}

		@Override
		public IntIterator iterator() {
			return new IdToIndexIterator<>(idC.iterator(), map);
		}

		@Override
		public boolean contains(int key) {
			return idC.contains(map.indexToId(key));
		}

		@Override
		public boolean rem(int key) {
			return idC.remove(map.indexToId(key));
		}
	}

	private static class IntIdToIndexCollection extends AbstractIntCollection {

		private final IntCollection idC;
		private final IndexIntIdMap map;

		IntIdToIndexCollection(IntCollection idC, IndexIntIdMap map) {
			this.idC = Objects.requireNonNull(idC);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public int size() {
			return idC.size();
		}

		@Override
		public boolean isEmpty() {
			return idC.isEmpty();
		}

		@Override
		public void clear() {
			idC.clear();
		}

		@Override
		public IntIterator iterator() {
			return new IntIdToIndexIterator(idC.iterator(), map);
		}

		@Override
		public boolean contains(int key) {
			return idC.contains(map.indexToIdInt(key));
		}

		@Override
		public boolean rem(int key) {
			return idC.rem(map.indexToIdInt(key));
		}
	}

	/**
	 * Create an IDs list from a list of indices.
	 *
	 * @param  <K>       the type of IDs
	 * @param  indexList a list of indices
	 * @param  map       index-id mapping
	 * @return           a list that contain IDs matching the indices contained in the original index-list
	 */
	@SuppressWarnings("unchecked")
	public static <K> List<K> indexToIdList(IntList indexList, IndexIdMap<K> map) {
		if (map instanceof IndexIntIdMap) {
			return (List<K>) indexToIdList(indexList, (IndexIntIdMap) map);
		} else {
			return new IndexToIdList<>(indexList, map);
		}
	}

	/**
	 * Create an IDs list from a list of indices.
	 *
	 * @param  indexList a list of indices
	 * @param  map       index-id mapping
	 * @return           a list that contain IDs matching the indices contained in the original index-list
	 */
	public static IntList indexToIdList(IntList indexList, IndexIntIdMap map) {
		return new IndexToIntIdList(indexList, map);
	}

	@SuppressWarnings("unchecked")
	private static class IndexToIdList<K> extends AbstractList<K> {

		private final IntList indexList;
		private final IndexIdMap<K> map;

		IndexToIdList(IntList indexList, IndexIdMap<K> map) {
			this.indexList = Objects.requireNonNull(indexList);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public int size() {
			return indexList.size();
		}

		@Override
		public boolean isEmpty() {
			return indexList.isEmpty();
		}

		@Override
		public void clear() {
			indexList.clear();
		}

		@Override
		public boolean contains(Object key) {
			return indexList.contains(map.idToIndexIfExist((K) key));
		}

		@Override
		public boolean remove(Object key) {
			return indexList.rem(map.idToIndexIfExist((K) key));
		}

		@Override
		public K get(int index) {
			return map.indexToId(indexList.getInt(index));
		}

		@Override
		public int indexOf(Object k) {
			return indexList.indexOf(map.idToIndex((K) k));
		}

		@Override
		public int lastIndexOf(Object k) {
			return indexList.lastIndexOf(map.idToIndex((K) k));
		}

		@Override
		public K remove(int index) {
			return map.indexToId(indexList.removeInt(index));
		}
	}

	private static class IndexToIntIdList extends AbstractIntList {

		private final IntList indexList;
		private final IndexIntIdMap map;

		IndexToIntIdList(IntList indexList, IndexIntIdMap map) {
			this.indexList = Objects.requireNonNull(indexList);
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public int size() {
			return indexList.size();
		}

		@Override
		public boolean isEmpty() {
			return indexList.isEmpty();
		}

		@Override
		public void clear() {
			indexList.clear();
		}

		@Override
		public boolean contains(int key) {
			return indexList.contains(map.idToIndexIfExist(key));
		}

		@Override
		public boolean rem(int key) {
			return indexList.rem(map.idToIndexIfExist(key));
		}

		@Override
		public int getInt(int index) {
			return map.indexToIdInt(indexList.getInt(index));
		}

		@Override
		public int indexOf(int k) {
			return indexList.indexOf(map.idToIndex(k));
		}

		@Override
		public int lastIndexOf(int k) {
			return indexList.lastIndexOf(map.idToIndex(k));
		}

		@Override
		public int removeInt(int index) {
			return map.indexToIdInt(indexList.removeInt(index));
		}
	}

	private abstract static class IdToIndexWeights<K, T> implements IWeights<T> {

		private final Weights<K, T> idxWeights;
		private final IndexIdMap<K> map;

		IdToIndexWeights(Weights<K, T> idxWeights, IndexIdMap<K> map) {
			this.idxWeights = Objects.requireNonNull(idxWeights);
			this.map = Objects.requireNonNull(map);
		}

		Weights<K, T> idxWeights() {
			return idxWeights;
		}

		K id(int index) {
			return map.indexToId(index);
		}

		static class Obj<K, T> extends IdToIndexWeights<K, T> implements IWeightsObj<T> {

			Obj(WeightsObj<K, T> idxWeights, IndexIdMap<K> map) {
				super(idxWeights, map);
			}

			@Override
			WeightsObj<K, T> idxWeights() {
				return (WeightsObj<K, T>) super.idxWeights();
			}

			@Override
			public T get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, T weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public T defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Byte<K> extends IdToIndexWeights<K, java.lang.Byte> implements IWeightsByte {

			Byte(WeightsByte<K> idxWeights, IndexIdMap<K> map) {
				super(idxWeights, map);
			}

			@Override
			WeightsByte<K> idxWeights() {
				return (WeightsByte<K>) super.idxWeights();
			}

			@Override
			public byte get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, byte weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public byte defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Short<K> extends IdToIndexWeights<K, java.lang.Short> implements IWeightsShort {

			Short(WeightsShort<K> idxWeights, IndexIdMap<K> map) {
				super(idxWeights, map);
			}

			@Override
			WeightsShort<K> idxWeights() {
				return (WeightsShort<K>) super.idxWeights();
			}

			@Override
			public short get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, short weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public short defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Int<K> extends IdToIndexWeights<K, Integer> implements IWeightsInt {

			Int(WeightsInt<K> idxWeights, IndexIdMap<K> map) {
				super(idxWeights, map);
			}

			@Override
			WeightsInt<K> idxWeights() {
				return (WeightsInt<K>) super.idxWeights();
			}

			@Override
			public int get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, int weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public int defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Long<K> extends IdToIndexWeights<K, java.lang.Long> implements IWeightsLong {

			Long(WeightsLong<K> idxWeights, IndexIdMap<K> map) {
				super(idxWeights, map);
			}

			@Override
			WeightsLong<K> idxWeights() {
				return (WeightsLong<K>) super.idxWeights();
			}

			@Override
			public long get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, long weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public long defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Float<K> extends IdToIndexWeights<K, java.lang.Float> implements IWeightsFloat {

			Float(WeightsFloat<K> idxWeights, IndexIdMap<K> map) {
				super(idxWeights, map);
			}

			@Override
			WeightsFloat<K> idxWeights() {
				return (WeightsFloat<K>) super.idxWeights();
			}

			@Override
			public float get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, float weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public float defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Double<K> extends IdToIndexWeights<K, java.lang.Double> implements IWeightsDouble {

			Double(WeightsDouble<K> idxWeights, IndexIdMap<K> map) {
				super(idxWeights, map);
			}

			@Override
			WeightsDouble<K> idxWeights() {
				return (WeightsDouble<K>) super.idxWeights();
			}

			@Override
			public double get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, double weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public double defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Bool<K> extends IdToIndexWeights<K, Boolean> implements IWeightsBool {

			Bool(WeightsBool<K> idxWeights, IndexIdMap<K> map) {
				super(idxWeights, map);
			}

			@Override
			WeightsBool<K> idxWeights() {
				return (WeightsBool<K>) super.idxWeights();
			}

			@Override
			public boolean get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, boolean weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public boolean defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Char<K> extends IdToIndexWeights<K, Character> implements IWeightsChar {

			Char(WeightsChar<K> idxWeights, IndexIdMap<K> map) {
				super(idxWeights, map);
			}

			@Override
			WeightsChar<K> idxWeights() {
				return (WeightsChar<K>) super.idxWeights();
			}

			@Override
			public char get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, char weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public char defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

	}

	private abstract static class IntIdToIndexWeights<T> implements IWeights<T> {

		private final IWeights<T> idxWeights;
		private final IndexIntIdMap map;

		IntIdToIndexWeights(IWeights<T> idxWeights, IndexIntIdMap map) {
			this.idxWeights = Objects.requireNonNull(idxWeights);
			this.map = Objects.requireNonNull(map);
		}

		IWeights<T> idxWeights() {
			return idxWeights;
		}

		int id(int index) {
			return map.indexToIdInt(index);
		}

		static class Obj<T> extends IntIdToIndexWeights<T> implements IWeightsObj<T> {

			Obj(IWeightsObj<T> idxWeights, IndexIntIdMap map) {
				super(idxWeights, map);
			}

			@Override
			IWeightsObj<T> idxWeights() {
				return (IWeightsObj<T>) super.idxWeights();
			}

			@Override
			public T get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, T weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public T defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Byte extends IntIdToIndexWeights<java.lang.Byte> implements IWeightsByte {

			Byte(IWeightsByte idxWeights, IndexIntIdMap map) {
				super(idxWeights, map);
			}

			@Override
			IWeightsByte idxWeights() {
				return (IWeightsByte) super.idxWeights();
			}

			@Override
			public byte get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, byte weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public byte defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Short extends IntIdToIndexWeights<java.lang.Short> implements IWeightsShort {

			Short(IWeightsShort idxWeights, IndexIntIdMap map) {
				super(idxWeights, map);
			}

			@Override
			IWeightsShort idxWeights() {
				return (IWeightsShort) super.idxWeights();
			}

			@Override
			public short get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, short weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public short defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Int extends IntIdToIndexWeights<Integer> implements IWeightsInt {

			Int(IWeightsInt idxWeights, IndexIntIdMap map) {
				super(idxWeights, map);
			}

			@Override
			IWeightsInt idxWeights() {
				return (IWeightsInt) super.idxWeights();
			}

			@Override
			public int get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, int weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public int defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Long extends IntIdToIndexWeights<java.lang.Long> implements IWeightsLong {

			Long(IWeightsLong idxWeights, IndexIntIdMap map) {
				super(idxWeights, map);
			}

			@Override
			IWeightsLong idxWeights() {
				return (IWeightsLong) super.idxWeights();
			}

			@Override
			public long get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, long weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public long defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Float extends IntIdToIndexWeights<java.lang.Float> implements IWeightsFloat {

			Float(IWeightsFloat idxWeights, IndexIntIdMap map) {
				super(idxWeights, map);
			}

			@Override
			IWeightsFloat idxWeights() {
				return (IWeightsFloat) super.idxWeights();
			}

			@Override
			public float get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, float weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public float defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Double extends IntIdToIndexWeights<java.lang.Double> implements IWeightsDouble {

			Double(IWeightsDouble idxWeights, IndexIntIdMap map) {
				super(idxWeights, map);
			}

			@Override
			IWeightsDouble idxWeights() {
				return (IWeightsDouble) super.idxWeights();
			}

			@Override
			public double get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, double weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public double defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Bool extends IntIdToIndexWeights<Boolean> implements IWeightsBool {

			Bool(IWeightsBool idxWeights, IndexIntIdMap map) {
				super(idxWeights, map);
			}

			@Override
			IWeightsBool idxWeights() {
				return (IWeightsBool) super.idxWeights();
			}

			@Override
			public boolean get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, boolean weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public boolean defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

		static class Char extends IntIdToIndexWeights<Character> implements IWeightsChar {

			Char(IWeightsChar idxWeights, IndexIntIdMap map) {
				super(idxWeights, map);
			}

			@Override
			IWeightsChar idxWeights() {
				return (IWeightsChar) super.idxWeights();
			}

			@Override
			public char get(int index) {
				return idxWeights().get(id(index));
			}

			@Override
			public void set(int index, char weight) {
				idxWeights().set(id(index), weight);
			}

			@Override
			public char defaultWeight() {
				return idxWeights().defaultWeight();
			}
		}

	}

	@SuppressWarnings("unchecked")
	private static <K, T, WeightsT extends IWeights<T>> WeightsT idToIndexWeightsWrapper(Weights<K, T> weights,
			IndexIdMap<K> map) {
		if (weights instanceof IWeights && map instanceof IndexIntIdMap) {
			IWeights<T> weights0 = (IWeights<T>) weights;
			IndexIntIdMap map0 = (IndexIntIdMap) map;
			if (weights instanceof WeightsByte) {
				return (WeightsT) new IntIdToIndexWeights.Byte((IWeightsByte) weights0, map0);
			} else if (weights instanceof WeightsShort) {
				return (WeightsT) new IntIdToIndexWeights.Short((IWeightsShort) weights0, map0);
			} else if (weights instanceof WeightsInt) {
				return (WeightsT) new IntIdToIndexWeights.Int((IWeightsInt) weights0, map0);
			} else if (weights instanceof WeightsLong) {
				return (WeightsT) new IntIdToIndexWeights.Long((IWeightsLong) weights0, map0);
			} else if (weights instanceof WeightsFloat) {
				return (WeightsT) new IntIdToIndexWeights.Float((IWeightsFloat) weights0, map0);
			} else if (weights instanceof WeightsDouble) {
				return (WeightsT) new IntIdToIndexWeights.Double((IWeightsDouble) weights0, map0);
			} else if (weights instanceof WeightsBool) {
				return (WeightsT) new IntIdToIndexWeights.Bool((IWeightsBool) weights0, map0);
			} else if (weights instanceof WeightsChar) {
				return (WeightsT) new IntIdToIndexWeights.Char((IWeightsChar) weights0, map0);
			} else if (weights instanceof WeightsObj) {
				return (WeightsT) new IntIdToIndexWeights.Obj<>((IWeightsObj<T>) weights0, map0);
			}
		} else if (weights instanceof WeightsByte) {
			return (WeightsT) new IdToIndexWeights.Byte<>((WeightsByte<K>) weights, map);
		} else if (weights instanceof WeightsShort) {
			return (WeightsT) new IdToIndexWeights.Short<>((WeightsShort<K>) weights, map);
		} else if (weights instanceof WeightsInt) {
			return (WeightsT) new IdToIndexWeights.Int<>((WeightsInt<K>) weights, map);
		} else if (weights instanceof WeightsLong) {
			return (WeightsT) new IdToIndexWeights.Long<>((WeightsLong<K>) weights, map);
		} else if (weights instanceof WeightsFloat) {
			return (WeightsT) new IdToIndexWeights.Float<>((WeightsFloat<K>) weights, map);
		} else if (weights instanceof WeightsDouble) {
			return (WeightsT) new IdToIndexWeights.Double<>((WeightsDouble<K>) weights, map);
		} else if (weights instanceof WeightsBool) {
			return (WeightsT) new IdToIndexWeights.Bool<>((WeightsBool<K>) weights, map);
		} else if (weights instanceof WeightsChar) {
			return (WeightsT) new IdToIndexWeights.Char<>((WeightsChar<K>) weights, map);
		} else if (weights instanceof WeightsObj) {
			return (WeightsT) new IdToIndexWeights.Obj<>((WeightsObj<K, T>) weights, map);
		}
		throw new AssertionError("unknown weights type: " + weights.getClass().getName());
	}

	/**
	 * Create a weights view that is accessed by the elements indices, given a weights container that is accessed by the
	 * element IDs.
	 *
	 * <p>
	 * The returned weights container is a view, namely modifying the original container change the new one, and vice
	 * versa.
	 *
	 * @param  <K>        the element (vertex or edge) identifiers type
	 * @param  <T>        the weight type
	 * @param  <WeightsT> the weights container, used to avoid casts of containers of primitive types such as
	 *                        {@link WeightsInt}, {@link WeightsDouble} ect. The user should expect
	 *                        {@link WeightsDouble} only if a {@link WeightsDouble} object was passed as an argument.
	 * @param  weights    a weights container that is accessed by the elements IDs
	 * @param  map        index-id map
	 * @return            a weights-view that is accessed by the elements indices
	 */
	@SuppressWarnings("unchecked")
	public static <K, T, WeightsT extends IWeights<T>> WeightsT idToIndexWeights(Weights<K, T> weights,
			IndexIdMap<K> map) {
		boolean immutableView = false;
		while (weights instanceof WeightsImpl.ImmutableView) {
			weights = ((WeightsImpl.ImmutableView<K, T>) weights).weights();
			immutableView = true;
		}

		IWeights<T> idxWeights;
		if (weights instanceof WeightsImpl.IntMapped) {
			if (map != ((WeightsImpl.IntMapped<?>) weights).indexMap)
				throw new IllegalArgumentException("wrong index-id map is used with weights container");
			idxWeights = ((WeightsImpl.IntMapped<T>) weights).weights;

		} else if (weights instanceof WeightsImpl.ObjMapped) {
			if (map != ((WeightsImpl.ObjMapped<?, ?>) weights).indexMap)
				throw new IllegalArgumentException("wrong index-id map is used with weights container");
			idxWeights = ((WeightsImpl.ObjMapped<K, T>) weights).weights;

		} else {
			idxWeights = idToIndexWeightsWrapper(weights, map);
		}

		if (immutableView)
			idxWeights = WeightsImpl.IntImmutableView.newInstance(idxWeights);
		return (WeightsT) idxWeights;
	}

	/**
	 * Create a weight function that accept elements indices, given a weight function that accept elements IDs.
	 *
	 * @param  <K> the type of elements IDs
	 * @param  w   a weight function that accept by elements IDs
	 * @param  map index-id map
	 * @return     a weight function that accept elements indices
	 */
	@SuppressWarnings("unchecked")
	public static <K> IWeightFunction idToIndexWeightFunc(WeightFunction<K> w, IndexIdMap<K> map) {
		if (w == null || w == WeightFunction.CardinalityWeightFunction
				|| w == IWeightFunction.CardinalityWeightFunction) {
			return null;

		} else if (w instanceof Weights) {
			/* The weight function is some implementation of a mapped weights container */
			/* Instead of re-mapping by wrapping the weight function, return the underlying index weights container */
			return (IWeightFunction) idToIndexWeights((Weights<K, ?>) w, map);

		} else if (w instanceof IWeightFunctionInt && map instanceof IndexIntIdMap) {
			/* Unknown int weight function, return a mapped wrapper */
			IWeightFunctionInt wInt = (IWeightFunctionInt) w;
			IndexIntIdMap map0 = (IndexIntIdMap) map;
			IWeightFunctionInt wIntMapped = idx -> wInt.weightInt(map0.indexToIdInt(idx));
			return wIntMapped;

		} else if (w instanceof IWeightFunction && map instanceof IndexIntIdMap) {
			/* Unknown weight function, return a mapped wrapper */
			IWeightFunction wInt = (IWeightFunction) w;
			IndexIntIdMap map0 = (IndexIntIdMap) map;
			IWeightFunction wMapped = idx -> wInt.weight(map0.indexToIdInt(idx));
			return wMapped;

		} else if (w instanceof WeightFunctionInt) {
			/* Unknown int weight function, return a mapped wrapper */
			WeightFunctionInt<K> wInt = (WeightFunctionInt<K>) w;
			IWeightFunctionInt wIntMapped = idx -> wInt.weightInt(map.indexToId(idx));
			return wIntMapped;

		} else {
			/* Unknown weight function, return a mapped wrapper */
			return idx -> w.weight(map.indexToId(idx));
		}
	}

	/**
	 * Create an integer weight function that accept elements indices, given a weight function that accept elements IDs.
	 *
	 * @param  <K> the type of elements IDs
	 * @param  w   an integer weight function that accept by elements IDs
	 * @param  map index-id map
	 * @return     an integer weight function that accept elements indices
	 */
	public static <K> IWeightFunctionInt idToIndexWeightFunc(WeightFunctionInt<K> w, IndexIdMap<K> map) {
		return (IWeightFunctionInt) idToIndexWeightFunc((WeightFunction<K>) w, map);
	}

}
