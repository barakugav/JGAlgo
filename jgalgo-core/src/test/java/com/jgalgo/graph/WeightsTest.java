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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

@SuppressWarnings("boxing")
public class WeightsTest extends TestBase {

	@Test
	public void testWeightsObjects() {
		final long seed = 0xe7d6f0afb01000baL;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Object>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, Object.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryObject(), null, seed);
	}

	@Test
	public void testWeightsObjectsWithDefaultWeight() {
		final long seed = 0x71b6e5749ca06738L;
		Object defVal = new Object();
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Object>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, Object.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryObject(), defVal, seed);
	}

	@Test
	public void testWeightsBytes() {
		final long seed = 0x484dd5b050fbb881L;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Byte>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, byte.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryByte(seed), (byte) 0, seed);
	}

	@Test
	public void testWeightsBytesWithDefaultWeight() {
		final long seed = 0xf14cb8abf6ed4f9fL;
		byte defVal = (byte) 0xc8;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Byte>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, byte.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryByte(seed), defVal, seed);
	}

	@Test
	public void testWeightsShorts() {
		final long seed = 0x2ee8e70daf324abfL;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Short>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, short.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryShort(seed), (short) 0, seed);
	}

	@Test
	public void testWeightsShortsWithDefaultWeight() {
		final long seed = 0x409d909a3947948fL;
		short defVal = 0x309b;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Short>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, short.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryShort(seed), defVal, seed);
	}

	@Test
	public void testWeightsInts() {
		final long seed = 0xc81e5634eed692fdL;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Integer>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, int.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryInt(seed), 0, seed);
	}

	@Test
	public void testWeightsIntsWithDefaultWeight() {
		final long seed = 0x2024360951336d19L;
		int defVal = 0x3006b813;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Integer>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, int.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryInt(seed), defVal, seed);
	}

	@Test
	public void testWeightsLongs() {
		final long seed = 0xa3183b96d809f3f0L;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Long>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, long.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryLong(seed), 0L, seed);
	}

	@Test
	public void testWeightsLongsWithDefaultWeight() {
		final long seed = 0x58fa7dd03fe7207eL;
		long defVal = 0x46c9069fec84a482L;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Long>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, long.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryLong(seed), defVal, seed);
	}

	@Test
	public void testWeightsFloats() {
		final long seed = 0x35801d78fc5ab86eL;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Float>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, float.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryFloat(seed), 0f, seed);
	}

	@Test
	public void testWeightsFloatsWithDefaultWeight() {
		final long seed = 0xa9963dbfc0a4462bL;
		float defVal = Float.intBitsToFloat(0xe7f2aa88);
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Float>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, float.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryFloat(seed), defVal, seed);
	}

	@Test
	public void testWeightsDoubles() {
		final long seed = 0x32de9146baf98f13L;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Double>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, double.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryDouble(seed), 0.0, seed);
	}

	@Test
	public void testWeightsDoublesWithDefaultWeight() {
		final long seed = 0x5698af4847f1349eL;
		double defVal = Double.longBitsToDouble(0x0de33f798dd2ec5aL);
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Double>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, double.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryDouble(seed), defVal, seed);
	}

	@Test
	public void testWeightsBools() {
		final long seed = 0x89698a740f504d87L;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Boolean>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, boolean.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryBool(seed), false, seed);
	}

	@Test
	public void testWeightsBoolsWithDefaultWeight() {
		final long seed = 0x98da961379cae813L;
		boolean defVal = true;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Boolean>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, boolean.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryBool(seed), defVal, seed);
	}

	@Test
	public void testWeightsChars() {
		final long seed = 0x8ede26a1638aef7dL;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Character>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, char.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryChar(seed), (char) 0, seed);
	}

	@Test
	public void testWeightsCharsWithDefaultWeight() {
		final long seed = 0x25b63aa72ff77460L;
		char defVal = (char) 0xa5fb;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Character>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, char.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryChar(seed), defVal, seed);
	}

	private static <T> void testWeights(
			BiFunction<Graph<Integer, Integer>, String, Weights<Integer, T>> edgeWeightsAdder,
			Supplier<T> weightFactory, T defaultWeight, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seed);

		foreachBoolConfig((intGraph, removeEdges, expand) -> {
			Graph<Integer, Integer> g =
					GraphsTestUtils.randGraph(1024, 5000, false, true, true, intGraph, seedGen.nextSeed());

			String wKey = "edgeWeight";
			Weights<Integer, T> weights = edgeWeightsAdder.apply(g, wKey);
			assertEquals(defaultWeight, weights.defaultWeightAsObj());

			if (expand) {
				/* cause the vertices weights container to expand */
				for (int n = g.vertices().size() * 2 + 1; g.vertices().size() < n;) {
					int v = rand.nextInt(n * 2);
					if (!g.vertices().contains(v))
						g.addVertex(v);
				}
				/* cause the edges weights container to expand */
				for (int m = g.edges().size() * 2 + 1; g.edges().size() < m;) {
					int e = rand.nextInt(m * 2);
					if (!g.edges().contains(e))
						g.addEdge(Graphs.randVertex(g, rand), Graphs.randVertex(g, rand), e);
				}
			}

			Int2ObjectMap<T> assignedEdges = new Int2ObjectOpenHashMap<>();
			assignedEdges.defaultReturnValue(defaultWeight);
			while (assignedEdges.size() < g.edges().size() * 3 / 4) {
				int op = rand.nextInt(10);

				if (op == 9 && removeEdges) {
					int e = rand.nextBoolean() ? Graphs.randEdge(g, rand)
							: g.indexGraphEdgesMap().indexToId(g.edges().size() - 1);
					g.removeEdge(e);
					assignedEdges.remove(e);
					continue;
				}

				if (op % 2 == 0) {

					// Choose random unassigned edge
					int e;
					do {
						e = Graphs.randEdge(g, rand);
					} while (assignedEdges.containsKey(e));

					// assigned to it a new value
					T val = weightFactory.get();
					weights.setAsObj(e, val);
					assignedEdges.put(e, val);

				} else {
					int e = Graphs.randEdge(g, rand);
					T val;
					if (weights instanceof WeightsByte) {
						@SuppressWarnings("unchecked")
						T actual0 = (T) Byte.valueOf(((WeightsByte<Integer>) weights).get(e));
						val = actual0;
					} else if (weights instanceof WeightsShort) {
						@SuppressWarnings("unchecked")
						T actual0 = (T) Short.valueOf(((WeightsShort<Integer>) weights).get(e));
						val = actual0;
					} else if (weights instanceof WeightsInt) {
						@SuppressWarnings("unchecked")
						T actual0 = (T) Integer.valueOf(((WeightsInt<Integer>) weights).get(e));
						val = actual0;
					} else if (weights instanceof WeightsLong) {
						@SuppressWarnings("unchecked")
						T actual0 = (T) Long.valueOf(((WeightsLong<Integer>) weights).get(e));
						val = actual0;
					} else if (weights instanceof WeightsFloat) {
						@SuppressWarnings("unchecked")
						T actual0 = (T) Float.valueOf(((WeightsFloat<Integer>) weights).get(e));
						val = actual0;
					} else if (weights instanceof WeightsDouble) {
						@SuppressWarnings("unchecked")
						T actual0 = (T) Double.valueOf(((WeightsDouble<Integer>) weights).get(e));
						val = actual0;
					} else if (weights instanceof WeightsBool) {
						@SuppressWarnings("unchecked")
						T actual0 = (T) Boolean.valueOf(((WeightsBool<Integer>) weights).get(e));
						val = actual0;
					} else if (weights instanceof WeightsChar) {
						@SuppressWarnings("unchecked")
						T actual0 = (T) Character.valueOf(((WeightsChar<Integer>) weights).get(e));
						val = actual0;
					} else {
						val = ((WeightsObj<Integer, T>) weights).get(e);
					}
					T valAsObj = weights.getAsObj(e);
					assertEquals(val, valAsObj);

					T expected = assignedEdges.get(e);
					assertEquals(expected, val);

					int e2;
					do {
						e2 = Graphs.randEdge(g, rand);
					} while (assignedEdges.containsKey(e2));

					if (weights instanceof WeightFunction) {
						@SuppressWarnings("unchecked")
						WeightFunction<Integer> weights0 = ((WeightFunction<Integer>) weights);
						double expectedNumber = ((Number) weights.getAsObj(e)).doubleValue();
						double actualNumber = weights0.weight(e);
						assertEquals(expectedNumber, actualNumber);
						assertEquals(Double.compare(weights0.weight(e), weights0.weight(e2)), weights0.compare(e, e2));
					}
					if (weights instanceof WeightFunctionInt) {
						@SuppressWarnings("unchecked")
						WeightFunctionInt<Integer> weights0 = ((WeightFunctionInt<Integer>) weights);
						int expectedNumber = ((Number) weights.getAsObj(e)).intValue();
						int actualNumber = weights0.weightInt(e);
						assertEquals(expectedNumber, actualNumber);

						@SuppressWarnings("deprecation")
						int actualNumber2 = (int) weights0.weight(e);
						@SuppressWarnings("deprecation")
						int actualNumber3 = (int) weights0.weight(Integer.valueOf(e));
						assertEquals(actualNumber, actualNumber2);
						assertEquals(actualNumber, actualNumber3);
					}
					if (weights instanceof IWeightFunction) {
						IWeightFunction weights0 = ((IWeightFunction) weights);
						double expectedNumber = ((Number) weights.getAsObj(e)).doubleValue();
						double actualNumber = weights0.weight(e);
						assertEquals(expectedNumber, actualNumber);

						@SuppressWarnings("deprecation")
						double actualNumber2 = weights0.weight(Integer.valueOf(e));
						assertEquals(actualNumber, actualNumber2);
					}
				}
			}

			int nonExistingEdge = GraphsTestUtils.nonExistingInt(g.edges(), rand);
			assertThrows(NoSuchEdgeException.class, () -> weights.getAsObj(-1));
			assertThrows(NoSuchEdgeException.class, () -> weights.getAsObj(nonExistingEdge));
			assertThrows(NoSuchEdgeException.class, () -> weights.setAsObj(-1, weightFactory.get()));
			assertThrows(NoSuchEdgeException.class, () -> weights.setAsObj(nonExistingEdge, weightFactory.get()));

			Weights<Integer, T> weightsImmutable = g.immutableView().getEdgesWeights(wKey);
			for (int e : g.edges())
				assertEquals(weights.getAsObj(e), weightsImmutable.getAsObj(e));
			assertEquals(weights.defaultWeightAsObj(), weightsImmutable.defaultWeightAsObj());
			assertThrows(UnsupportedOperationException.class, () -> {
				Iterator<Integer> eit = g.edges().iterator();
				Integer e1 = eit.next(), e2 = eit.next();
				weightsImmutable.setAsObj(e1, weightsImmutable.getAsObj(e2));
			});

			assertTrue(WeightsImpl.isEqual(g.edges(), weights, weights));
			assertTrue(WeightsImpl.isEqual(g.edges(), weights, weightsImmutable));
			assertTrue(WeightsImpl.isEqual(g.edges(), weightsImmutable, weightsImmutable));

			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			IWeights<T> indexWeights = IndexIdMaps.idToIndexWeights(weights, eiMap);
			Weights<Integer, T> weights2 = unknownImplementationWrap(weights);
			IWeights<T> indexWeights2 = IndexIdMaps.idToIndexWeights(weights2, eiMap);
			assertEquals(weights.defaultWeightAsObj(), indexWeights.defaultWeightAsObj());
			assertEquals(weights.defaultWeightAsObj(), indexWeights2.defaultWeightAsObj());
			for (int e : g.edges()) {
				switch (rand.nextInt(3)) {
					case 0:
						if (rand.nextBoolean()) {
							weights.setAsObj(e, weightFactory.get());
						} else if (weights instanceof WeightsByte) {
							@SuppressWarnings("unchecked")
							WeightsByte<Integer> weights0 = (WeightsByte<Integer>) weights;
							weights0.set(e, ((Byte) weightFactory.get()).byteValue());
						} else if (weights instanceof WeightsShort) {
							@SuppressWarnings("unchecked")
							WeightsShort<Integer> weights0 = (WeightsShort<Integer>) weights;
							weights0.set(e, ((Short) weightFactory.get()).shortValue());
						} else if (weights instanceof WeightsInt) {
							@SuppressWarnings("unchecked")
							WeightsInt<Integer> weights0 = (WeightsInt<Integer>) weights;
							weights0.set(e, ((Integer) weightFactory.get()).intValue());
						} else if (weights instanceof WeightsLong) {
							@SuppressWarnings("unchecked")
							WeightsLong<Integer> weights0 = (WeightsLong<Integer>) weights;
							weights0.set(e, ((Long) weightFactory.get()).longValue());
						} else if (weights instanceof WeightsFloat) {
							@SuppressWarnings("unchecked")
							WeightsFloat<Integer> weights0 = (WeightsFloat<Integer>) weights;
							weights0.set(e, ((Float) weightFactory.get()).floatValue());
						} else if (weights instanceof WeightsDouble) {
							@SuppressWarnings("unchecked")
							WeightsDouble<Integer> weights0 = (WeightsDouble<Integer>) weights;
							weights0.set(e, ((Double) weightFactory.get()).doubleValue());
						} else if (weights instanceof WeightsBool) {
							@SuppressWarnings("unchecked")
							WeightsBool<Integer> weights0 = (WeightsBool<Integer>) weights;
							weights0.set(e, ((Boolean) weightFactory.get()).booleanValue());
						} else if (weights instanceof WeightsChar) {
							@SuppressWarnings("unchecked")
							WeightsChar<Integer> weights0 = (WeightsChar<Integer>) weights;
							weights0.set(e, ((Character) weightFactory.get()).charValue());
						} else {
							WeightsObj<Integer, T> weights0 = (WeightsObj<Integer, T>) weights;
							weights0.set(e, weightFactory.get());
						}
						break;
					case 1:
						indexWeights.setAsObj(eiMap.idToIndex(e), weightFactory.get());
						break;
					case 2:
						indexWeights2.setAsObj(eiMap.idToIndex(e), weightFactory.get());
						break;
					default:
						break;
				}
			}
			for (int e : g.edges()) {
				assertEquals(weights.getAsObj(e), indexWeights.getAsObj(eiMap.idToIndex(e)));
				assertEquals(weights.getAsObj(e), indexWeights2.getAsObj(eiMap.idToIndex(e)));
			}

			/* clear graph, should clear weights as well */
			g.clear();
		});
	}

	private static Supplier<Object> weightFactoryObject() {
		return Object::new;
	}

	private static Supplier<Byte> weightFactoryByte(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Byte get() {
				return (byte) rand.nextInt();
			}
		};
	}

	private static Supplier<Short> weightFactoryShort(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Short get() {
				return (short) rand.nextInt();
			}
		};
	}

	private static Supplier<Integer> weightFactoryInt(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Integer get() {
				return rand.nextInt();
			}
		};
	}

	private static Supplier<Long> weightFactoryLong(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Long get() {
				return rand.nextLong();
			}
		};
	}

	private static Supplier<Float> weightFactoryFloat(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Float get() {
				return rand.nextFloat();
			}
		};
	}

	private static Supplier<Double> weightFactoryDouble(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Double get() {
				return rand.nextDouble();
			}
		};
	}

	private static Supplier<Boolean> weightFactoryBool(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Boolean get() {
				return rand.nextBoolean();
			}
		};
	}

	private static Supplier<Character> weightFactoryChar(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Character get() {
				return (char) rand.nextInt();
			}
		};
	}

	@Test
	public void weightsSum() {
		WeightFunction<Integer> w1 = e -> e * 2;
		WeightFunctionInt<Integer> w2 = e -> e * 2;
		IWeightFunction w3 = e -> e * 2;
		IWeightFunctionInt w4 = e -> e * 2;
		assertEquals(WeightFunction.weightSum(w1, range(6)), 30);
		assertEquals(WeightFunction.weightSum(w2, range(6)), 30);
		assertEquals(IWeightFunction.weightSum(w3, range(6)), 30);
		assertEquals(IWeightFunction.weightSum(w4, range(6)), 30);
		assertEquals(WeightFunction.weightSum(w3, new ArrayList<>(range(6))), 30);
		assertEquals(WeightFunction.weightSum(w4, new ArrayList<>(range(6))), 30);
		assertEquals(WeightFunction.weightSum(null, range(6)), 6);
		assertEquals(WeightFunction.weightSum(null, () -> range(6).iterator()), 6);
		assertEquals(IWeightFunction.weightSum(null, range(6)), 6);
		assertEquals(IWeightFunction.weightSum(null, () -> range(6).iterator()), 6);
	}

	@Test
	public void weightDeprecated() {
		IWeightFunction w1 = e -> e * 2;
		for (int i : range(20)) {
			@SuppressWarnings("deprecation")
			double val = w1.weight(Integer.valueOf(i));
			assertEquals(w1.weight(i), val);
		}

		IWeightFunctionInt w2 = e -> e * 2;
		for (int i : range(20)) {
			int expected = w2.weightInt(i);
			@SuppressWarnings("deprecation")
			double val1 = w2.weightInt(Integer.valueOf(i));
			@SuppressWarnings("deprecation")
			double val2 = w2.weight(i);
			@SuppressWarnings("deprecation")
			double val3 = w2.weight(Integer.valueOf(i));
			assertEquals(expected, val1);
			assertEquals(expected, val2);
			assertEquals(expected, val3);
		}
	}

	@Test
	public void compare() {
		final Random rand = new Random(0x38080271f0c3ae2dL);

		WeightFunction<Integer> w1 = e -> e * 2;
		for (int i = 0; i < 20; i++) {
			int x = rand.nextInt(100), y = rand.nextInt(100);
			int cExpected = Double.compare(w1.weight(x), w1.weight(y));
			int c1 = w1.compare(x, y);
			assertEquals(cExpected, c1);
		}

		WeightFunctionInt<Integer> w2 = e -> e * 2;
		for (int i = 0; i < 20; i++) {
			int x = rand.nextInt(100), y = rand.nextInt(100);
			int cExpected = Double.compare(w2.weightInt(x), w2.weightInt(y));
			int c1 = w2.compare(x, y);
			assertEquals(cExpected, c1);
		}

		IWeightFunction w3 = e -> e * 2;
		for (int i = 0; i < 20; i++) {
			int x = rand.nextInt(100), y = rand.nextInt(100);
			int cExpected = Double.compare(w3.weight(x), w3.weight(y));
			int c1 = w3.compare(x, y);
			@SuppressWarnings("deprecation")
			int c2 = w3.compare(Integer.valueOf(x), Integer.valueOf(y));
			assertEquals(cExpected, c1);
			assertEquals(cExpected, c2);
		}

		IWeightFunctionInt w4 = e -> e * 2;
		for (int i = 0; i < 20; i++) {
			int x = rand.nextInt(100), y = rand.nextInt(100);
			int cExpected = Double.compare(w4.weightInt(x), w4.weightInt(y));
			int c1 = w4.compare(x, y);
			@SuppressWarnings("deprecation")
			int c2 = w4.compare(Integer.valueOf(x), Integer.valueOf(y));
			assertEquals(cExpected, c1);
			assertEquals(cExpected, c2);
		}
	}

	@Test
	public void cardinalityWeightFunction() {
		assertEquals(1, WeightFunction.cardinalityWeightFunction().weightInt(new Object()));
		assertEquals(1, WeightFunction.cardinalityWeightFunction().weightInt(null));
		assertEquals(1, IWeightFunction.CardinalityWeightFunction.weightInt(0));
		assertEquals(1, IWeightFunction.CardinalityWeightFunction.weightInt(1));
		assertEquals(1, IWeightFunction.CardinalityWeightFunction.weightInt(-1));
	}

	@Test
	public void isCardinality() {
		assertTrue(WeightFunction.isCardinality(WeightFunction.cardinalityWeightFunction()));
		assertTrue(WeightFunction.isCardinality(IWeightFunction.CardinalityWeightFunction));
		assertTrue(WeightFunction.isCardinality(null));
		assertFalse(WeightFunction.isCardinality(e -> 8));
		assertFalse(WeightFunction.isCardinality(e -> 1));
	}

	@Test
	public void isInteger() {
		assertTrue(WeightFunction.isInteger(WeightFunction.cardinalityWeightFunction()));
		assertTrue(WeightFunction.isInteger(IWeightFunction.CardinalityWeightFunction));
		assertTrue(WeightFunction.isInteger(null));
		assertFalse(WeightFunction.isInteger(e -> 2.1));
		assertTrue(WeightFunction.isInteger((WeightFunctionInt<Object>) e -> 2));
	}

	@Test
	public void replaceNullWeightFunc() {
		assertTrue(WeightFunction.replaceNullWeightFunc((WeightFunction<?>) null) == WeightFunction
				.cardinalityWeightFunction());
		assertTrue(WeightFunction.replaceNullWeightFunc((WeightFunctionInt<?>) null) == WeightFunction
				.cardinalityWeightFunction());
		assertTrue(IWeightFunction
				.replaceNullWeightFunc((IWeightFunction) null) == IWeightFunction.CardinalityWeightFunction);
		assertTrue(IWeightFunction
				.replaceNullWeightFunc((IWeightFunctionInt) null) == IWeightFunction.CardinalityWeightFunction);
		WeightFunction<Integer> w1 = e -> 1;
		assertTrue(WeightFunction.replaceNullWeightFunc(w1) == w1);
		WeightFunctionInt<Integer> w2 = e -> 1;
		assertTrue(WeightFunction.replaceNullWeightFunc(w2) == w2);
		IWeightFunction w3 = e -> 1;
		assertTrue(IWeightFunction.replaceNullWeightFunc(w3) == w3);
		IWeightFunctionInt w4 = e -> 1;
		assertTrue(IWeightFunction.replaceNullWeightFunc(w4) == w4);
	}

	@Test
	public void nullKey() {
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = intGraph ? IntGraph.newDirected() : Graph.newDirected();
			assertThrows(NullPointerException.class, () -> g.addVerticesWeights(null, int.class));
			assertThrows(NullPointerException.class, () -> g.addEdgesWeights(null, int.class));
		});
		foreachBoolConfig(intBuilder -> {
			GraphBuilder<Integer, Integer> b = intBuilder ? IntGraphBuilder.directed() : GraphBuilder.directed();
			assertThrows(NullPointerException.class, () -> b.addVerticesWeights(null, int.class));
			assertThrows(NullPointerException.class, () -> b.addEdgesWeights(null, int.class));
		});
	}

	@SuppressWarnings("unchecked")
	private static <K, T> Weights<K, T> unknownImplementationWrap(Weights<K, T> w) {
		if (w instanceof IWeightsObj) {
			IWeightsObj<T> w0 = (IWeightsObj<T>) w;
			return (WeightsObj<K, T>) new IWeightsObj<T>() {
				@Override
				public T get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, T weight) {
					w0.set(element, weight);
				}

				@Override
				public T defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsByte) {
			IWeightsByte w0 = (IWeightsByte) w;
			return (Weights<K, T>) new IWeightsByte() {
				@Override
				public byte get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, byte weight) {
					w0.set(element, weight);
				}

				@Override
				public byte defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsShort) {
			IWeightsShort w0 = (IWeightsShort) w;
			return (Weights<K, T>) new IWeightsShort() {
				@Override
				public short get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, short weight) {
					w0.set(element, weight);
				}

				@Override
				public short defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsInt) {
			IWeightsInt w0 = (IWeightsInt) w;
			return (Weights<K, T>) new IWeightsInt() {
				@Override
				public int get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, int weight) {
					w0.set(element, weight);
				}

				@Override
				public int defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsLong) {
			IWeightsLong w0 = (IWeightsLong) w;
			return (Weights<K, T>) new IWeightsLong() {
				@Override
				public long get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, long weight) {
					w0.set(element, weight);
				}

				@Override
				public long defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsFloat) {
			IWeightsFloat w0 = (IWeightsFloat) w;
			return (Weights<K, T>) new IWeightsFloat() {
				@Override
				public float get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, float weight) {
					w0.set(element, weight);
				}

				@Override
				public float defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsDouble) {
			IWeightsDouble w0 = (IWeightsDouble) w;
			return (Weights<K, T>) new IWeightsDouble() {
				@Override
				public double get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, double weight) {
					w0.set(element, weight);
				}

				@Override
				public double defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsBool) {
			IWeightsBool w0 = (IWeightsBool) w;
			return (Weights<K, T>) new IWeightsBool() {
				@Override
				public boolean get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, boolean weight) {
					w0.set(element, weight);
				}

				@Override
				public boolean defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsChar) {
			IWeightsChar w0 = (IWeightsChar) w;
			return (Weights<K, T>) new IWeightsChar() {
				@Override
				public char get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, char weight) {
					w0.set(element, weight);
				}

				@Override
				public char defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsObj) {
			WeightsObj<K, T> w0 = (WeightsObj<K, T>) w;
			return new WeightsObj<>() {
				@Override
				public T get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, T weight) {
					w0.set(element, weight);
				}

				@Override
				public T defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsByte) {
			WeightsByte<K> w0 = (WeightsByte<K>) w;
			return (Weights<K, T>) new WeightsByte<K>() {
				@Override
				public byte get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, byte weight) {
					w0.set(element, weight);
				}

				@Override
				public byte defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsShort) {
			WeightsShort<K> w0 = (WeightsShort<K>) w;
			return (Weights<K, T>) new WeightsShort<K>() {
				@Override
				public short get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, short weight) {
					w0.set(element, weight);
				}

				@Override
				public short defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsInt) {
			WeightsInt<K> w0 = (WeightsInt<K>) w;
			return (Weights<K, T>) new WeightsInt<K>() {
				@Override
				public int get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, int weight) {
					w0.set(element, weight);
				}

				@Override
				public int defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsLong) {
			WeightsLong<K> w0 = (WeightsLong<K>) w;
			return (Weights<K, T>) new WeightsLong<K>() {
				@Override
				public long get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, long weight) {
					w0.set(element, weight);
				}

				@Override
				public long defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsFloat) {
			WeightsFloat<K> w0 = (WeightsFloat<K>) w;
			return (Weights<K, T>) new WeightsFloat<K>() {
				@Override
				public float get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, float weight) {
					w0.set(element, weight);
				}

				@Override
				public float defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsDouble) {
			WeightsDouble<K> w0 = (WeightsDouble<K>) w;
			return (Weights<K, T>) new WeightsDouble<K>() {
				@Override
				public double get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, double weight) {
					w0.set(element, weight);
				}

				@Override
				public double defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsBool) {
			WeightsBool<K> w0 = (WeightsBool<K>) w;
			return (Weights<K, T>) new WeightsBool<K>() {
				@Override
				public boolean get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, boolean weight) {
					w0.set(element, weight);
				}

				@Override
				public boolean defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsChar) {
			WeightsChar<K> w0 = (WeightsChar<K>) w;
			return (Weights<K, T>) new WeightsChar<K>() {
				@Override
				public char get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, char weight) {
					w0.set(element, weight);
				}

				@Override
				public char defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else {
			throw new AssertionError("unknown weights type: " + w.getClass().getName());
		}
	}
}
