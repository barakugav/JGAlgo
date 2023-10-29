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
import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.AbstractIntList;
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
	 * @param  indexIter an iterator of indices
	 * @param  map       index-id mapping
	 * @return           an iterator that iterate over the IDs matching the indices iterated by the original
	 *                   index-iterator
	 */
	public static IntIterator indexToIdIterator(IntIterator indexIter, IndexIntIdMap map) {
		return new IndexToIdIterator(indexIter, map);
	}

	/**
	 * Create an indices iterator from an iterator of IDs.
	 *
	 * @param  idIter an iterator of IDs
	 * @param  map    index-id mapping
	 * @return        an iterator that iterate over the indices matching the IDs iterated by the original ID-iterator
	 */
	public static IntIterator idToIndexIterator(IntIterator idIter, IndexIntIdMap map) {
		return new IdToIndexIterator(idIter, map);
	}

	private static class IndexToIdIterator implements IntIterator {

		private final IntIterator indexIt;
		private final IndexIntIdMap map;

		IndexToIdIterator(IntIterator idxIt, IndexIntIdMap map) {
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

	/**
	 * Create an {@link IEdgeIter} that return IDs of vertices and edges from an {@link IEdgeIter} that return indices
	 * of vertices and edges.
	 *
	 * @param  indexIter   an {@link IEdgeIter} that return indices of vertices and edges
	 * @param  verticesMap vertices index-id mapping
	 * @param  edgesMap    edges index-id mapping
	 * @return             {@link IEdgeIter} that return IDs of vertices and edges matching the indices of vertices and
	 *                     edges returned by the original index-iterator
	 */
	public static IEdgeIter indexToIdEdgeIter(IEdgeIter indexIter, IndexIntIdMap verticesMap, IndexIntIdMap edgesMap) {
		return new IndexToIdEdgeIter(indexIter, verticesMap, edgesMap);
	}

	private static class IndexToIdEdgeIter extends IndexToIdIterator implements IEdgeIter {
		private final IndexIntIdMap viMap;

		IndexToIdEdgeIter(IEdgeIter indexIt, IndexIntIdMap viMap, IndexIntIdMap eiMap) {
			super(indexIt, eiMap);
			this.viMap = Objects.requireNonNull(viMap);
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

	private static class IdToIndexIterator implements IntIterator {

		private final IntIterator idIt;
		private final IndexIntIdMap map;

		IdToIndexIterator(IntIterator idIt, IndexIntIdMap map) {
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
	 * Create an IDs collection from a collection of indices.
	 *
	 * @param  indexCollection a collection of indices
	 * @param  map             index-id mapping
	 * @return                 a collection that contain IDs matching the indices contained in the original
	 *                         index-collection
	 */
	public static IntCollection indexToIdCollection(IntCollection indexCollection, IndexIntIdMap map) {
		return new IndexToIdCollection(indexCollection, map);
	}

	private static class IndexToIdCollection extends AbstractIntCollection {

		final IntCollection indexC;
		final IndexIntIdMap map;

		IndexToIdCollection(IntCollection indexC, IndexIntIdMap map) {
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
			return new IndexToIdIterator(indexC.iterator(), map);
		}

		@Override
		public boolean contains(int key) {
			return indexC.contains(map.idToIndex(key));
		}

		@Override
		public boolean rem(int key) {
			return indexC.rem(map.idToIndex(key));
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
		return new IndexToIdSet(indexSet, map);
	}

	private static class IndexToIdSet extends IndexToIdCollection implements IntSet {
		IndexToIdSet(IntSet indexSet, IndexIntIdMap map) {
			super(indexSet, map);
		}

		@Deprecated
		@Override
		public boolean remove(int k) {
			return indexC.rem(map.idToIndex(k));
		}
	}

	/**
	 * Create an indices collection from a collection of IDs.
	 *
	 * @param  idCollection a collection of IDs
	 * @param  map          index-id mapping
	 * @return              a collection that contain indices matching the IDs contained in the original ID-collection
	 */
	public static IntCollection idToIndexCollection(IntCollection idCollection, IndexIntIdMap map) {
		return new IdToIndexCollection(idCollection, map);
	}

	private static class IdToIndexCollection extends AbstractIntCollection {

		private final IntCollection idC;
		private final IndexIntIdMap map;

		IdToIndexCollection(IntCollection idC, IndexIntIdMap map) {
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
			return new IdToIndexIterator(idC.iterator(), map);
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
	 * @param  indexList a list of indices
	 * @param  map       index-id mapping
	 * @return           a list that contain IDs matching the indices contained in the original index-list
	 */
	public static IntList indexToIdList(IntList indexList, IndexIntIdMap map) {
		return new IndexToIdList(indexList, map);
	}

	private static class IndexToIdList extends AbstractIntList {

		private final IntList indexList;
		private final IndexIntIdMap map;

		IndexToIdList(IntList indexList, IndexIntIdMap map) {
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
			indexList.clear();;
		}

		@Override
		public boolean contains(int key) {
			return indexList.contains(map.idToIndex(key));
		}

		@Override
		public boolean rem(int key) {
			return indexList.rem(map.idToIndex(key));
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

	private static abstract class IdToIndexWeights<W> implements IWeights<W> {
		private final IndexIntIdMap map;

		IdToIndexWeights(IndexIntIdMap map) {
			this.map = Objects.requireNonNull(map);
		}

		int id(int index) {
			return map.indexToIdInt(index);
		}

		static class Obj<W> extends IdToIndexWeights<W> implements IWeightsObj<W> {

			private final IWeightsObj<W> weights;

			Obj(IWeightsObj<W> weights, IndexIntIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public W get(int index) {
				return weights.get(id(index));
			}

			@Override
			public void set(int index, W weight) {
				weights.set(id(index), weight);
			}

			@Override
			public W defaultWeight() {
				return weights.defaultWeight();
			}
		}

		static class Byte extends IdToIndexWeights<java.lang.Byte> implements IWeightsByte {

			private final IWeightsByte weights;

			Byte(IWeightsByte weights, IndexIntIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public byte get(int index) {
				return weights.get(id(index));
			}

			@Override
			public void set(int index, byte weight) {
				weights.set(id(index), weight);
			}

			@Override
			public byte defaultWeight() {
				return weights.defaultWeight();
			}
		}

		static class Short extends IdToIndexWeights<java.lang.Short> implements IWeightsShort {

			private final IWeightsShort weights;

			Short(IWeightsShort weights, IndexIntIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public short get(int index) {
				return weights.get(id(index));
			}

			@Override
			public void set(int index, short weight) {
				weights.set(id(index), weight);
			}

			@Override
			public short defaultWeight() {
				return weights.defaultWeight();
			}
		}

		static class Int extends IdToIndexWeights<Integer> implements IWeightsInt {

			private final IWeightsInt weights;

			Int(IWeightsInt weights, IndexIntIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public int get(int index) {
				return weights.get(id(index));
			}

			@Override
			public void set(int index, int weight) {
				weights.set(id(index), weight);
			}

			@Override
			public int defaultWeight() {
				return weights.defaultWeight();
			}
		}

		static class Long extends IdToIndexWeights<java.lang.Long> implements IWeightsLong {

			private final IWeightsLong weights;

			Long(IWeightsLong weights, IndexIntIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public long get(int index) {
				return weights.get(id(index));
			}

			@Override
			public void set(int index, long weight) {
				weights.set(id(index), weight);
			}

			@Override
			public long defaultWeight() {
				return weights.defaultWeight();
			}
		}

		static class Float extends IdToIndexWeights<java.lang.Float> implements IWeightsFloat {

			private final IWeightsFloat weights;

			Float(IWeightsFloat weights, IndexIntIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public float get(int index) {
				return weights.get(id(index));
			}

			@Override
			public void set(int index, float weight) {
				weights.set(id(index), weight);
			}

			@Override
			public float defaultWeight() {
				return weights.defaultWeight();
			}
		}

		static class Double extends IdToIndexWeights<java.lang.Double> implements IWeightsDouble {

			private final IWeightsDouble weights;

			Double(IWeightsDouble weights, IndexIntIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public double get(int index) {
				return weights.get(id(index));
			}

			@Override
			public void set(int index, double weight) {
				weights.set(id(index), weight);
			}

			@Override
			public double defaultWeight() {
				return weights.defaultWeight();
			}
		}

		static class Bool extends IdToIndexWeights<Boolean> implements IWeightsBool {

			private final IWeightsBool weights;

			Bool(IWeightsBool weights, IndexIntIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public boolean get(int index) {
				return weights.get(id(index));
			}

			@Override
			public void set(int index, boolean weight) {
				weights.set(id(index), weight);
			}

			@Override
			public boolean defaultWeight() {
				return weights.defaultWeight();
			}
		}

		static class Char extends IdToIndexWeights<Character> implements IWeightsChar {

			private final IWeightsChar weights;

			Char(IWeightsChar weights, IndexIntIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public char get(int index) {
				return weights.get(id(index));
			}

			@Override
			public void set(int index, char weight) {
				weights.set(id(index), weight);
			}

			@Override
			public char defaultWeight() {
				return weights.defaultWeight();
			}
		}

	}

	private static <T, WeightsT extends IWeights<T>> WeightsT idToIndexWeights0(WeightsImpl<?> weights,
			IndexIntIdMap map) {
		IWeightsUnwrapper unwrapper = new IWeightsUnwrapper();
		IWeights<?> weights0 = unwrapper.unwrap(weights);

		if (!(weights0 instanceof WeightsImpl.IntMapped<?>))
			throw new IllegalArgumentException("weights of index graph used with non index graph");
		if (map != ((WeightsImpl.IntMapped<?>) weights0).indexMap)
			throw new IllegalArgumentException("wrong index-id map is used with weights container");
		WeightsImpl.IndexAbstract<?> weights00 = ((WeightsImpl.IntMapped<?>) weights0).weights;

		return unwrapper.rewrap(weights00);
	}

	/**
	 * Create a weights view that is accessed by the elements indices, given a weights container that is accessed by the
	 * element IDs.
	 * <p>
	 * The returned weights container is a view, namely modifying the original container change the new one, and vice
	 * versa.
	 *
	 * @param  <W>     the weight type
	 * @param  weights a weights container that is accessed by the elements IDs
	 * @param  map     index-id map
	 * @return         a weights-view that is accessed by the elements indices
	 */
	public static <W> IWeightsObj<W> idToIndexWeights(IWeightsObj<W> weights, IndexIntIdMap map) {
		if (weights instanceof WeightsImpl<?>) {
			/* The weights container is some implementation of a mapped weights container */
			/* Instead of re-mapping by wrapping the weights container, return the underlying index weights container */
			return idToIndexWeights0((WeightsImpl<?>) weights, map);

		} else {
			/* Unknown weight container, return a mapped wrapper */
			return new IdToIndexWeights.Obj<>(weights, map);
		}
	}

	/**
	 * Create a weights view that is accessed by the elements indices, given a weights container that is accessed by the
	 * element IDs.
	 * <p>
	 * The returned weights container is a view, namely modifying the original container change the new one, and vice
	 * versa.
	 *
	 * @param  weights a weights container that is accessed by the elements IDs
	 * @param  map     index-id map
	 * @return         a weights-view that is accessed by the elements indices
	 */
	public static IWeightsByte idToIndexWeights(IWeightsByte weights, IndexIntIdMap map) {
		if (weights instanceof WeightsImpl<?>) {
			/* The weights container is some implementation of a mapped weights container */
			/* Instead of re-mapping by wrapping the weights container, return the underlying index weights container */
			return idToIndexWeights0((WeightsImpl<?>) weights, map);

		} else {
			/* Unknown weight container, return a mapped wrapper */
			return new IdToIndexWeights.Byte(weights, map);
		}
	}

	/**
	 * Create a weights view that is accessed by the elements indices, given a weights container that is accessed by the
	 * element IDs.
	 * <p>
	 * The returned weights container is a view, namely modifying the original container change the new one, and vice
	 * versa.
	 *
	 * @param  weights a weights container that is accessed by the elements IDs
	 * @param  map     index-id map
	 * @return         a weights-view that is accessed by the elements indices
	 */
	public static IWeightsShort idToIndexWeights(IWeightsShort weights, IndexIntIdMap map) {
		if (weights instanceof WeightsImpl<?>) {
			/* The weights container is some implementation of a mapped weights container */
			/* Instead of re-mapping by wrapping the weights container, return the underlying index weights container */
			return idToIndexWeights0((WeightsImpl<?>) weights, map);

		} else {
			/* Unknown weight container, return a mapped wrapper */
			return new IdToIndexWeights.Short(weights, map);
		}
	}

	/**
	 * Create a weights view that is accessed by the elements indices, given a weights container that is accessed by the
	 * element IDs.
	 * <p>
	 * The returned weights container is a view, namely modifying the original container change the new one, and vice
	 * versa.
	 *
	 * @param  weights a weights container that is accessed by the elements IDs
	 * @param  map     index-id map
	 * @return         a weights-view that is accessed by the elements indices
	 */
	public static IWeightsInt idToIndexWeights(IWeightsInt weights, IndexIntIdMap map) {
		if (weights instanceof WeightsImpl<?>) {
			/* The weights container is some implementation of a mapped weights container */
			/* Instead of re-mapping by wrapping the weights container, return the underlying index weights container */
			return idToIndexWeights0((WeightsImpl<?>) weights, map);

		} else {
			/* Unknown weight container, return a mapped wrapper */
			return new IdToIndexWeights.Int(weights, map);
		}
	}

	/**
	 * Create a weights view that is accessed by the elements indices, given a weights container that is accessed by the
	 * element IDs.
	 * <p>
	 * The returned weights container is a view, namely modifying the original container change the new one, and vice
	 * versa.
	 *
	 * @param  weights a weights container that is accessed by the elements IDs
	 * @param  map     index-id map
	 * @return         a weights-view that is accessed by the elements indices
	 */
	public static IWeightsLong idToIndexWeights(IWeightsLong weights, IndexIntIdMap map) {
		if (weights instanceof WeightsImpl<?>) {
			/* The weights container is some implementation of a mapped weights container */
			/* Instead of re-mapping by wrapping the weights container, return the underlying index weights container */
			return idToIndexWeights0((WeightsImpl<?>) weights, map);

		} else {
			/* Unknown weight container, return a mapped wrapper */
			return new IdToIndexWeights.Long(weights, map);
		}
	}

	/**
	 * Create a weights view that is accessed by the elements indices, given a weights container that is accessed by the
	 * element IDs.
	 * <p>
	 * The returned weights container is a view, namely modifying the original container change the new one, and vice
	 * versa.
	 *
	 * @param  weights a weights container that is accessed by the elements IDs
	 * @param  map     index-id map
	 * @return         a weights-view that is accessed by the elements indices
	 */
	public static IWeightsFloat idToIndexWeights(IWeightsFloat weights, IndexIntIdMap map) {
		if (weights instanceof WeightsImpl<?>) {
			/* The weights container is some implementation of a mapped weights container */
			/* Instead of re-mapping by wrapping the weights container, return the underlying index weights container */
			return idToIndexWeights0((WeightsImpl<?>) weights, map);

		} else {
			/* Unknown weight container, return a mapped wrapper */
			return new IdToIndexWeights.Float(weights, map);
		}
	}

	/**
	 * Create a weights view that is accessed by the elements indices, given a weights container that is accessed by the
	 * element IDs.
	 * <p>
	 * The returned weights container is a view, namely modifying the original container change the new one, and vice
	 * versa.
	 *
	 * @param  weights a weights container that is accessed by the elements IDs
	 * @param  map     index-id map
	 * @return         a weights-view that is accessed by the elements indices
	 */
	public static IWeightsDouble idToIndexWeights(IWeightsDouble weights, IndexIntIdMap map) {
		if (weights instanceof WeightsImpl<?>) {
			/* The weights container is some implementation of a mapped weights container */
			/* Instead of re-mapping by wrapping the weights container, return the underlying index weights container */
			return idToIndexWeights0((WeightsImpl<?>) weights, map);

		} else {
			/* Unknown weight container, return a mapped wrapper */
			return new IdToIndexWeights.Double(weights, map);
		}
	}

	/**
	 * Create a weights view that is accessed by the elements indices, given a weights container that is accessed by the
	 * element IDs.
	 * <p>
	 * The returned weights container is a view, namely modifying the original container change the new one, and vice
	 * versa.
	 *
	 * @param  weights a weights container that is accessed by the elements IDs
	 * @param  map     index-id map
	 * @return         a weights-view that is accessed by the elements indices
	 */
	public static IWeightsBool indexWeightsFromWeights(IWeightsBool weights, IndexIntIdMap map) {
		if (weights instanceof WeightsImpl<?>) {
			/* The weights container is some implementation of a mapped weights container */
			/* Instead of re-mapping by wrapping the weights container, return the underlying index weights container */
			return idToIndexWeights0((WeightsImpl<?>) weights, map);

		} else {
			/* Unknown weight container, return a mapped wrapper */
			return new IdToIndexWeights.Bool(weights, map);
		}
	}

	/**
	 * Create a weights view that is accessed by the elements indices, given a weights container that is accessed by the
	 * elements IDs.
	 * <p>
	 * The returned weights container is a view, namely modifying the original container change the new one, and vice
	 * versa.
	 *
	 * @param  weights a weights container that is accessed by the elements IDs
	 * @param  map     index-id map
	 * @return         a weights-view that is accessed by the elements indices
	 */
	public static IWeightsChar idToIndexWeights(IWeightsChar weights, IndexIntIdMap map) {
		if (weights instanceof WeightsImpl<?>) {
			/* The weights container is some implementation of a mapped weights container */
			/* Instead of re-mapping by wrapping the weights container, return the underlying index weights container */
			return idToIndexWeights0((WeightsImpl<?>) weights, map);

		} else {
			/* Unknown weight container, return a mapped wrapper */
			return new IdToIndexWeights.Char(weights, map);
		}
	}

	/**
	 * Create a weight function that accept elements indices, given a weight function that accept elements IDs.
	 *
	 * @param  w   a weight function that accept by elements IDs
	 * @param  map index-id map
	 * @return     a weight function that accept elements indices
	 */
	public static IWeightFunction idToIndexWeightFunc(IWeightFunction w, IndexIntIdMap map) {
		if (w == null || w == IWeightFunction.CardinalityWeightFunction) {
			return w;

		} else if (w instanceof WeightsImpl<?>) {
			/* The weight function is some implementation of a mapped weights container */
			/* Instead of re-mapping by wrapping the weight function, return the underlying index weights container */
			return (IWeightFunction) idToIndexWeights0((WeightsImpl<?>) w, map);

		} else {
			/* Unknown weight function, return a mapped wrapper */
			if (w instanceof IWeightFunctionInt) {
				IWeightFunctionInt wInt = (IWeightFunctionInt) w;
				IWeightFunctionInt wIntMapped = idx -> wInt.weightInt(map.indexToIdInt(idx));
				return wIntMapped;
			} else {
				return idx -> w.weight(map.indexToIdInt(idx));
			}
		}
	}

	/**
	 * Create a weight function that accept elements indices, given a weight function that accept elements IDs.
	 *
	 * @param  w   a weight function that accept by elements IDs
	 * @param  map index-id map
	 * @return     a weight function that accept elements indices
	 */
	public static IWeightFunctionInt idToIndexWeightFunc(IWeightFunctionInt w, IndexIntIdMap map) {
		if (w == null || w == IWeightFunction.CardinalityWeightFunction) {
			return w;

		} else if (w instanceof WeightsImpl<?>) {
			/* The weight function is some implementation of a mapped weights container */
			/* Instead of re-mapping by wrapping the weight function, return the underlying index weights container */
			return (IWeightFunctionInt) idToIndexWeights0((WeightsImpl<?>) w, map);

		} else {
			/* Unknown weight function, return a mapped wrapper */
			return idx -> w.weightInt(map.indexToIdInt(idx));
		}
	}

	private static class IWeightsUnwrapper {
		private boolean immutableView;

		@SuppressWarnings("unchecked")
		<T, WeightsT extends IWeights<T>> WeightsT unwrap(WeightsImpl<?> weights) {
			IWeights<?> weights0 = weights;
			immutableView = weights0 instanceof WeightsImpl.IntImmutableView<?>;
			if (immutableView)
				weights0 = ((WeightsImpl.IntImmutableView<?>) weights0).weights;
			return (WeightsT) weights0;
		}

		@SuppressWarnings("unchecked")
		<T, WeightsT extends IWeights<T>> WeightsT rewrap(IWeights<?> weights) {
			if (immutableView)
				weights = WeightsImpl.IntImmutableView.newInstance(weights);
			return (WeightsT) weights;
		}

	}

}
