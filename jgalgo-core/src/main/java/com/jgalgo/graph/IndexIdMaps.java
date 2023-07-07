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

		int index(int id) {
			return map.idToIndex(id);
		}

		static class Obj<W> extends IdToIndexWeights<W> {

			private final Weights<W> weights;

			Obj(Weights<W> weights, IndexIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public W get(int id) {
				return weights.get(index(id));
			}

			@Override
			public void set(int id, W weight) {
				weights.set(index(id), weight);
			}

			@Override
			public W defaultWeight() {
				return weights.defaultWeight();
			}
		}

		static class Byte extends IdToIndexWeights<java.lang.Byte> implements Weights.Byte {

			private final Weights.Byte weights;

			Byte(Weights.Byte weights, IndexIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public byte getByte(int id) {
				return weights.getByte(index(id));
			}

			@Override
			public void set(int id, byte weight) {
				weights.set(index(id), weight);
			}

			@Override
			public byte defaultWeightByte() {
				return weights.defaultWeightByte();
			}
		}

		static class Short extends IdToIndexWeights<java.lang.Short> implements Weights.Short {

			private final Weights.Short weights;

			Short(Weights.Short weights, IndexIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public short getShort(int id) {
				return weights.getShort(index(id));
			}

			@Override
			public void set(int id, short weight) {
				weights.set(index(id), weight);
			}

			@Override
			public short defaultWeightShort() {
				return weights.defaultWeightShort();
			}
		}

		static class Int extends IdToIndexWeights<Integer> implements Weights.Int {

			private final Weights.Int weights;

			Int(Weights.Int weights, IndexIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public int getInt(int id) {
				return weights.getInt(index(id));
			}

			@Override
			public void set(int id, int weight) {
				weights.set(index(id), weight);
			}

			@Override
			public int defaultWeightInt() {
				return weights.defaultWeightInt();
			}
		}

		static class Long extends IdToIndexWeights<java.lang.Long> implements Weights.Long {

			private final Weights.Long weights;

			Long(Weights.Long weights, IndexIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public long getLong(int id) {
				return weights.getLong(index(id));
			}

			@Override
			public void set(int id, long weight) {
				weights.set(index(id), weight);
			}

			@Override
			public long defaultWeightLong() {
				return weights.defaultWeightLong();
			}
		}

		static class Float extends IdToIndexWeights<java.lang.Float> implements Weights.Float {

			private final Weights.Float weights;

			Float(Weights.Float weights, IndexIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public float getFloat(int id) {
				return weights.getFloat(index(id));
			}

			@Override
			public void set(int id, float weight) {
				weights.set(index(id), weight);
			}

			@Override
			public float defaultWeightFloat() {
				return weights.defaultWeightFloat();
			}
		}

		static class Double extends IdToIndexWeights<java.lang.Double> implements Weights.Double {

			private final Weights.Double weights;

			Double(Weights.Double weights, IndexIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public double getDouble(int id) {
				return weights.getDouble(index(id));
			}

			@Override
			public void set(int id, double weight) {
				weights.set(index(id), weight);
			}

			@Override
			public double defaultWeightDouble() {
				return weights.defaultWeightDouble();
			}
		}

		static class Bool extends IdToIndexWeights<Boolean> implements Weights.Bool {

			private final Weights.Bool weights;

			Bool(Weights.Bool weights, IndexIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public boolean getBool(int id) {
				return weights.getBool(index(id));
			}

			@Override
			public void set(int id, boolean weight) {
				weights.set(index(id), weight);
			}

			@Override
			public boolean defaultWeightBool() {
				return weights.defaultWeightBool();
			}
		}

		static class Char extends IdToIndexWeights<Character> implements Weights.Char {

			private final Weights.Char weights;

			Char(Weights.Char weights, IndexIdMap map) {
				super(map);
				this.weights = Objects.requireNonNull(weights);
			}

			@Override
			public char getChar(int id) {
				return weights.getChar(index(id));
			}

			@Override
			public void set(int id, char weight) {
				weights.set(index(id), weight);
			}

			@Override
			public char defaultWeightChar() {
				return weights.defaultWeightChar();
			}
		}

	}

	private static <V, WeightsT extends WeightsImpl<V>> WeightsT idToIndexWeights0(Weights<?> weights, IndexIdMap map) {
		WeightsUnwrapper unwrapper = new WeightsUnwrapper();
		WeightsImpl<?> weights0 = unwrapper.unwrap((WeightsImpl<?>) weights);

		if (!(weights0 instanceof WeightsImpl.Mapped<?>))
			throw new IllegalArgumentException("weights of index graph used with non index graph");
		if (map != ((WeightsImpl.Mapped<?>) weights0).indexMap)
			throw new IllegalArgumentException("wrong index-id map is used with weights container");
		WeightsImpl.Index.Abstract<?> weights00 = ((WeightsImpl.Mapped<?>) weights0).weights;

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
	public static <W> Weights<W> idToIndexWeights(Weights<W> weights, IndexIdMap map) {
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
	public static Weights.Byte idToIndexWeights(Weights.Byte weights, IndexIdMap map) {
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
	public static Weights.Short idToIndexWeights(Weights.Short weights, IndexIdMap map) {
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
	public static Weights.Int idToIndexWeights(Weights.Int weights, IndexIdMap map) {
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
	public static Weights.Long idToIndexWeights(Weights.Long weights, IndexIdMap map) {
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
	public static Weights.Float idToIndexWeights(Weights.Float weights, IndexIdMap map) {
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
	public static Weights.Double idToIndexWeights(Weights.Double weights, IndexIdMap map) {
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
	public static Weights.Bool indexWeightsFromWeights(Weights.Bool weights, IndexIdMap map) {
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
	public static Weights.Char idToIndexWeights(Weights.Char weights, IndexIdMap map) {
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

	private static class WeightsUnwrapper {
		private boolean immutableView;

		@SuppressWarnings("unchecked")
		<V, WeightsT extends WeightsImpl<V>> WeightsT unwrap(WeightsImpl<?> weights) {
			WeightsImpl<?> weights0 = weights;
			immutableView = weights0 instanceof WeightsImpl.ImmutableView<?>;
			if (immutableView)
				weights0 = ((WeightsImpl.ImmutableView<?>) weights0).weights;
			return (WeightsT) weights0;
		}

		@SuppressWarnings("unchecked")
		<V, WeightsT extends WeightsImpl<V>> WeightsT rewrap(WeightsImpl<?> weights) {
			if (immutableView)
				weights = (WeightsImpl<?>) WeightsImpl.immutableView(weights);
			return (WeightsT) weights;
		}

	}

}
