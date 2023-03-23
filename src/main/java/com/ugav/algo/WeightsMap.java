package com.ugav.algo;

import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

class WeightsMap {

	private WeightsMap() {
	}

	private static abstract class Abstract<E> extends WeightsAbstract<E> {

		private final Map<Integer, E> weights;

		Abstract(boolean isEdges, Map<Integer, E> weights) {
			super(isEdges);
			this.weights = weights;
		}

		Map<Integer, E> weights() {
			return weights;
		}

		@Override
		void keyAdd(int key) {
		}

		@Override
		public void clear() {
			weights.clear();
		}

		@Override
		public int hashCode() {
			return weights.hashCode();
		}

		@Override
		public String toString() {
			return weights.toString();
		}
	}

	static class Obj<E> extends Abstract<E> implements Weights<E> {

		private Obj(boolean isEdges, int expectedSize, E defVal) {
			super(isEdges, new Int2ObjectOpenHashMap<>(expectedSize));
			weights().defaultReturnValue(defVal);
		}

		static <E> Weights<E> ofEdges(int expectedSize, E defVal) {
			return new WeightsMap.Obj<>(true, expectedSize, defVal);
		}

		static <E> Weights<E> ofVertices(int expectedSize, E defVal) {
			return new WeightsMap.Obj<>(false, expectedSize, defVal);
		}

		@Override
		Int2ObjectMap<E> weights() {
			return (Int2ObjectMap<E>) super.weights();
		}

		@Override
		public E get(int key) {
			return weights().get(key);
		}

		@Override
		public void set(int key, E weight) {
			weights().put(key, weight);
		}

		@Override
		public E defaultVal() {
			return weights().defaultReturnValue();
		}

		@Override
		void keyRemove(int key) {
			weights().remove(key);
		}

		@Override
		void keySwap(int k1, int k2) {
			E v1 = weights().get(k1);
			E v2 = weights().get(k2);
			if (v1 != defaultVal())
				weights().put(k2, v1);
			if (v2 != defaultVal())
				weights().put(k1, v2);

		}

		@Override
		public boolean equals(Object other) {
			return this == other || (other instanceof WeightsMap.Obj<?> o && weights().equals(o.weights()));
		}
	}

	static class Int extends Abstract<Integer> implements Weights.Int, EdgeWeightFunc.Int {

		private Int(boolean isEdges, int expectedSize, int defVal) {
			super(isEdges, new Int2IntOpenHashMap(expectedSize));
			weights().defaultReturnValue(defVal);
		}

		static Weights.Int ofEdges(int expectedSize, int defVal) {
			return new WeightsMap.Int(true, expectedSize, defVal);
		}

		static Weights.Int ofVertices(int expectedSize, int defVal) {
			return new WeightsMap.Int(false, expectedSize, defVal);
		}

		@Override
		Int2IntMap weights() {
			return (Int2IntMap) super.weights();
		}

		@Override
		public int getInt(int key) {
			return weights().get(key);
		}

		@Override
		public void set(int key, int weight) {
			weights().put(key, weight);
		}

		@Override
		public int defaultValInt() {
			return weights().defaultReturnValue();
		}

		@Override
		void keyRemove(int key) {
			weights().remove(key);
		}

		@Override
		void keySwap(int k1, int k2) {
			int v1 = weights().get(k1);
			int v2 = weights().get(k2);
			if (v1 != defaultValInt())
				weights().put(k2, v1);
			if (v2 != defaultValInt())
				weights().put(k1, v2);
		}

		@Override
		public boolean equals(Object other) {
			// TODO equals with keys collection input
			return this == other || (other instanceof WeightsMap.Int o && weights().equals(o.weights()));
		}
	}

	static class Double extends Abstract<java.lang.Double> implements Weights.Double, EdgeWeightFunc {

		private Double(boolean isEdges, int expectedSize, double defVal) {
			super(isEdges, new Int2DoubleOpenHashMap(expectedSize));
			weights().defaultReturnValue(defVal);
		}

		static Weights.Double ofEdges(int expectedSize, double defVal) {
			return new WeightsMap.Double(true, expectedSize, defVal);
		}

		static Weights.Double ofVertices(int expectedSize, double defVal) {
			return new WeightsMap.Double(false, expectedSize, defVal);
		}

		@Override
		Int2DoubleMap weights() {
			return (Int2DoubleMap) super.weights();
		}

		@Override
		public double getDouble(int key) {
			return weights().get(key);
		}

		@Override
		public void set(int key, double weight) {
			weights().put(key, weight);
		}

		@Override
		public double defaultValDouble() {
			return weights().defaultReturnValue();
		}

		@Override
		void keyRemove(int key) {
			weights().remove(key);
		}

		@Override
		void keySwap(int k1, int k2) {
			double v1 = weights().get(k1);
			double v2 = weights().get(k2);
			if (v1 != defaultValDouble())
				weights().put(k2, v1);
			if (v2 != defaultValDouble())
				weights().put(k1, v2);
		}

		@Override
		public boolean equals(Object other) {
			return this == other || (other instanceof WeightsMap.Double o && weights().equals(o.weights()));
		}
	}

	static class Bool extends Abstract<Boolean> implements Weights.Bool {

		private Bool(boolean isEdges, int expectedSize, boolean defVal) {
			super(isEdges, new Int2BooleanOpenHashMap(expectedSize));
			weights().defaultReturnValue(defVal);
		}

		static Weights.Bool ofEdges(int expectedSize, boolean defVal) {
			return new WeightsMap.Bool(true, expectedSize, defVal);
		}

		static Weights.Bool ofVertices(int expectedSize, boolean defVal) {
			return new WeightsMap.Bool(false, expectedSize, defVal);
		}

		@Override
		Int2BooleanMap weights() {
			return (Int2BooleanMap) super.weights();
		}

		@Override
		public boolean getBool(int key) {
			return weights().get(key);
		}

		@Override
		public void set(int key, boolean weight) {
			weights().put(key, weight);
		}

		@Override
		public boolean defaultValBool() {
			return weights().defaultReturnValue();
		}

		@Override
		void keyRemove(int key) {
			weights().remove(key);
		}

		@Override
		void keySwap(int k1, int k2) {
			boolean v1 = weights().get(k1);
			boolean v2 = weights().get(k2);
			if (v1 != defaultValBool())
				weights().put(k2, v1);
			if (v2 != defaultValBool())
				weights().put(k1, v2);
		}

		@Override
		public boolean equals(Object other) {
			return this == other || (other instanceof WeightsMap.Bool o && weights().equals(o.weights()));
		}
	}

}
