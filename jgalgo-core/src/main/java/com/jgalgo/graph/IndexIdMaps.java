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

/**
 * Static methods class for {@linkplain IndexIdMap index-id maps}.
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
	public static IntIterator indexToIdIterator(IntIterator indexIter, IndexIdMap map) {
		return new IndexToIdIterator(indexIter, map);
	}

	/**
	 * Create an indices iterator from an iterator of IDs.
	 *
	 * @param  idIter an iterator of IDs
	 * @param  map    index-id mapping
	 * @return        an iterator that iterate over the indices matching the IDs iterated by the original ID-iterator
	 */
	public static IntIterator idToIndexIterator(IntIterator idIter, IndexIdMap map) {
		return new IdToIndexIterator(idIter, map);
	}

	private static class IndexToIdIterator implements IntIterator {

		private final IntIterator indexIt;
		private final IndexIdMap map;

		IndexToIdIterator(IntIterator idxIt, IndexIdMap map) {
			this.indexIt = Objects.requireNonNull(idxIt);
			this.map = Objects.requireNonNull(map);
		}

		IntIterator indexIt() {
			return indexIt;
		}

		IndexIdMap map() {
			return map;
		}

		@Override
		public boolean hasNext() {
			return indexIt.hasNext();
		}

		@Override
		public int nextInt() {
			return map.indexToId(indexIt.nextInt());
		}

		@Override
		public void remove() {
			indexIt.remove();
		}

	}

	/**
	 * Create an {@link EdgeIter} that return IDs of vertices and edges from an {@link EdgeIter} that return indices of
	 * vertices and edges.
	 *
	 * @param  indexIter   an {@link EdgeIter} that return indices of vertices and edges
	 * @param  verticesMap vertices index-id mapping
	 * @param  edgesMap    edges index-id mapping
	 * @return             {@link EdgeIter} that return IDs of vertices and edges matching the indices of vertices and
	 *                     edges returned by the original index-iterator
	 */
	public static EdgeIter indexToIdEdgeIter(EdgeIter indexIter, IndexIdMap verticesMap, IndexIdMap edgesMap) {
		return new IndexToIdEdgeIter(indexIter, verticesMap, edgesMap);
	}

	private static class IndexToIdEdgeIter extends IndexToIdIterator implements EdgeIter {
		private final IndexIdMap viMap;

		IndexToIdEdgeIter(EdgeIter indexIt, IndexIdMap viMap, IndexIdMap eiMap) {
			super(indexIt, eiMap);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		EdgeIter indexIt() {
			return (EdgeIter) super.indexIt();
		}

		IndexIdMap eiMap() {
			return super.map();
		}

		@Override
		public int peekNext() {
			return eiMap().indexToId(indexIt().peekNext());
		}

		@Override
		public int source() {
			return viMap.indexToId(indexIt().source());
		}

		@Override
		public int target() {
			return viMap.indexToId(indexIt().target());
		}
	}

	private static class IdToIndexIterator implements IntIterator {

		private final IntIterator idIt;
		private final IndexIdMap map;

		IdToIndexIterator(IntIterator idIt, IndexIdMap map) {
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
	public static IntCollection indexToIdCollection(IntCollection indexCollection, IndexIdMap map) {
		return new IndexToIdCollection(indexCollection, map);
	}

	private static class IndexToIdCollection extends AbstractIntCollection {

		private final IntCollection indexC;
		private final IndexIdMap map;

		IndexToIdCollection(IntCollection indexC, IndexIdMap map) {
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
	 * Create an indices collection from a collection of IDs.
	 *
	 * @param  idCollection a collection of IDs
	 * @param  map          index-id mapping
	 * @return              a collection that contain indices matching the IDs contained in the original ID-collection
	 */
	public static IntCollection idToIndexCollection(IntCollection idCollection, IndexIdMap map) {
		return new IdToIndexCollection(idCollection, map);
	}

	private static class IdToIndexCollection extends AbstractIntCollection {

		private final IntCollection idC;
		private final IndexIdMap map;

		IdToIndexCollection(IntCollection idC, IndexIdMap map) {
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
			return idC.contains(map.indexToId(key));
		}

		@Override
		public boolean rem(int key) {
			return idC.rem(map.indexToId(key));
		}
	}

	/**
	 * Create an IDs list from a list of indices.
	 *
	 * @param  indexList a list of indices
	 * @param  map       index-id mapping
	 * @return           a list that contain IDs matching the indices contained in the original index-list
	 */
	public static IntList indexToIdList(IntList indexList, IndexIdMap map) {
		return new IndexToIdList(indexList, map);
	}

	private static class IndexToIdList extends AbstractIntList {

		private final IntList indexList;
		private final IndexIdMap map;

		IndexToIdList(IntList indexList, IndexIdMap map) {
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
			return map.indexToId(indexList.getInt(index));
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
			return map.indexToId(indexList.removeInt(index));
		}

	}

	private static abstract class IdToIndexWeights<W> implements Weights<W> {
		private final IndexIdMap map;

		IdToIndexWeights(IndexIdMap map) {
			this.map = Objects.requireNonNull(map);
		}

		int id(int index) {
			return map.indexToId(index);
		}

		static class Obj<W> extends IdToIndexWeights<W> implements WeightsObj<W> {

			private final WeightsObj<W> weights;

			Obj(WeightsObj<W> weights, IndexIdMap map) {
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

		static class Byte extends IdToIndexWeights<java.lang.Byte> implements WeightsByte {

			private final WeightsByte weights;

			Byte(WeightsByte weights, IndexIdMap map) {
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

		static class Short extends IdToIndexWeights<java.lang.Short> implements WeightsShort {

			private final WeightsShort weights;

			Short(WeightsShort weights, IndexIdMap map) {
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

		static class Int extends IdToIndexWeights<Integer> implements WeightsInt {

			private final WeightsInt weights;

			Int(WeightsInt weights, IndexIdMap map) {
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

		static class Long extends IdToIndexWeights<java.lang.Long> implements WeightsLong {

			private final WeightsLong weights;

			Long(WeightsLong weights, IndexIdMap map) {
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

		static class Float extends IdToIndexWeights<java.lang.Float> implements WeightsFloat {

			private final WeightsFloat weights;

			Float(WeightsFloat weights, IndexIdMap map) {
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

		static class Double extends IdToIndexWeights<java.lang.Double> implements WeightsDouble {

			private final WeightsDouble weights;

			Double(WeightsDouble weights, IndexIdMap map) {
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

		static class Bool extends IdToIndexWeights<Boolean> implements WeightsBool {

			private final WeightsBool weights;

			Bool(WeightsBool weights, IndexIdMap map) {
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

		static class Char extends IdToIndexWeights<Character> implements WeightsChar {

			private final WeightsChar weights;

			Char(WeightsChar weights, IndexIdMap map) {
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

	private static <V, WeightsT extends Weights<V>> WeightsT idToIndexWeights0(WeightsImpl<?> weights, IndexIdMap map) {
		WeightsUnwrapper unwrapper = new WeightsUnwrapper();
		Weights<?> weights0 = unwrapper.unwrap(weights);

		if (!(weights0 instanceof WeightsImpl.Mapped<?>))
			throw new IllegalArgumentException("weights of index graph used with non index graph");
		if (map != ((WeightsImpl.Mapped<?>) weights0).indexMap)
			throw new IllegalArgumentException("wrong index-id map is used with weights container");
		WeightsImpl.IndexAbstract<?> weights00 = ((WeightsImpl.Mapped<?>) weights0).weights;

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
	public static <W> WeightsObj<W> idToIndexWeights(WeightsObj<W> weights, IndexIdMap map) {
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
	public static WeightsByte idToIndexWeights(WeightsByte weights, IndexIdMap map) {
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
	public static WeightsShort idToIndexWeights(WeightsShort weights, IndexIdMap map) {
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
	public static WeightsInt idToIndexWeights(WeightsInt weights, IndexIdMap map) {
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
	public static WeightsLong idToIndexWeights(WeightsLong weights, IndexIdMap map) {
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
	public static WeightsFloat idToIndexWeights(WeightsFloat weights, IndexIdMap map) {
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
	public static WeightsDouble idToIndexWeights(WeightsDouble weights, IndexIdMap map) {
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
	public static WeightsBool indexWeightsFromWeights(WeightsBool weights, IndexIdMap map) {
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
	public static WeightsChar idToIndexWeights(WeightsChar weights, IndexIdMap map) {
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
	public static WeightFunction idToIndexWeightFunc(WeightFunction w, IndexIdMap map) {
		if (w == null || w == WeightFunction.CardinalityWeightFunction) {
			return w;

		} else if (w instanceof WeightsImpl<?>) {
			/* The weight function is some implementation of a mapped weights container */
			/* Instead of re-mapping by wrapping the weight function, return the underlying index weights container */
			return (WeightFunction) idToIndexWeights0((WeightsImpl<?>) w, map);

		} else {
			/* Unknown weight function, return a mapped wrapper */
			if (w instanceof WeightFunction.Int) {
				WeightFunction.Int wInt = (WeightFunction.Int) w;
				WeightFunction.Int wIntMapped = idx -> wInt.weightInt(map.indexToId(idx));
				return wIntMapped;
			} else {
				return idx -> w.weight(map.indexToId(idx));
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
	public static WeightFunction.Int idToIndexWeightFunc(WeightFunction.Int w, IndexIdMap map) {
		if (w == null || w == WeightFunction.CardinalityWeightFunction) {
			return w;

		} else if (w instanceof WeightsImpl<?>) {
			/* The weight function is some implementation of a mapped weights container */
			/* Instead of re-mapping by wrapping the weight function, return the underlying index weights container */
			return (WeightFunction.Int) idToIndexWeights0((WeightsImpl<?>) w, map);

		} else {
			/* Unknown weight function, return a mapped wrapper */
			return idx -> w.weightInt(map.indexToId(idx));
		}
	}

	private static class WeightsUnwrapper {
		private boolean immutableView;

		@SuppressWarnings("unchecked")
		<V, WeightsT extends Weights<V>> WeightsT unwrap(WeightsImpl<?> weights) {
			Weights<?> weights0 = weights;
			immutableView = weights0 instanceof WeightsImpl.ImmutableView<?>;
			if (immutableView)
				weights0 = ((WeightsImpl.ImmutableView<?>) weights0).weights;
			return (WeightsT) weights0;
		}

		@SuppressWarnings("unchecked")
		<V, WeightsT extends Weights<V>> WeightsT rewrap(Weights<?> weights) {
			if (immutableView)
				weights = WeightsImpl.ImmutableView.newInstance(weights);
			return (WeightsT) weights;
		}

	}

}
