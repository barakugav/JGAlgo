package com.jgalgo.test;

import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.jgalgo.Graph;
import com.jgalgo.Weights;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

@SuppressWarnings("boxing")
public class WeightsTest extends TestBase {

	@Test
	public void testWeightsObjects() {
		final long seed = 0xe7d6f0afb01000baL;
		BiFunction<Graph, Object, Weights<Object>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				Object.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryObject(), null, seed);
	}

	@Test
	public void testWeightsObjectsWithDefaultWeight() {
		final long seed = 0x71b6e5749ca06738L;
		Object defVal = new Object();
		BiFunction<Graph, Object, Weights<Object>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				Object.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryObject(), defVal, seed);
	}

	@Test
	public void testWeightsBytes() {
		final long seed = 0x484dd5b050fbb881L;
		BiFunction<Graph, Object, Weights<Byte>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				byte.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryByte(seed), (byte) 0, seed);
	}

	@Test
	public void testWeightsBytesWithDefaultWeight() {
		final long seed = 0xf14cb8abf6ed4f9fL;
		byte defVal = (byte) 0xc8;
		BiFunction<Graph, Object, Weights<Byte>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				byte.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryByte(seed), defVal, seed);
	}

	@Test
	public void testWeightsShorts() {
		final long seed = 0x2ee8e70daf324abfL;
		BiFunction<Graph, Object, Weights<Short>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				short.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryShort(seed), (short) 0, seed);
	}

	@Test
	public void testWeightsShortsWithDefaultWeight() {
		final long seed = 0x409d909a3947948fL;
		short defVal = 0x309b;
		BiFunction<Graph, Object, Weights<Short>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				short.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryShort(seed), defVal, seed);
	}

	@Test
	public void testWeightsInts() {
		final long seed = 0xc81e5634eed692fdL;
		BiFunction<Graph, Object, Weights<Integer>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				int.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryInt(seed), 0, seed);
	}

	@Test
	public void testWeightsIntsWithDefaultWeight() {
		final long seed = 0x2024360951336d19L;
		int defVal = 0x3006b813;
		BiFunction<Graph, Object, Weights<Integer>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				int.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryInt(seed), defVal, seed);
	}

	@Test
	public void testWeightsLongs() {
		final long seed = 0xa3183b96d809f3f0L;
		BiFunction<Graph, Object, Weights<Long>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				long.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryLong(seed), 0L, seed);
	}

	@Test
	public void testWeightsLongsWithDefaultWeight() {
		final long seed = 0x58fa7dd03fe7207eL;
		long defVal = 0x46c9069fec84a482L;
		BiFunction<Graph, Object, Weights<Long>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				long.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryLong(seed), defVal, seed);
	}

	@Test
	public void testWeightsFloats() {
		final long seed = 0x35801d78fc5ab86eL;
		BiFunction<Graph, Object, Weights<Float>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				float.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryFloat(seed), 0f, seed);
	}

	@Test
	public void testWeightsFloatsWithDefaultWeight() {
		final long seed = 0xa9963dbfc0a4462bL;
		float defVal = Float.intBitsToFloat(0xe7f2aa88);
		BiFunction<Graph, Object, Weights<Float>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				float.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryFloat(seed), defVal, seed);
	}

	@Test
	public void testWeightsDoubles() {
		final long seed = 0x32de9146baf98f13L;
		BiFunction<Graph, Object, Weights<Double>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				double.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryDouble(seed), 0.0, seed);
	}

	@Test
	public void testWeightsDoublesWithDefaultWeight() {
		final long seed = 0x5698af4847f1349eL;
		double defVal = Double.longBitsToDouble(0x0de33f798dd2ec5aL);
		BiFunction<Graph, Object, Weights<Double>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				double.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryDouble(seed), defVal, seed);
	}

	@Test
	public void testWeightsBools() {
		final long seed = 0x89698a740f504d87L;
		BiFunction<Graph, Object, Weights<Boolean>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				boolean.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryBool(seed), false, seed);
	}

	@Test
	public void testWeightsBoolsWithDefaultWeight() {
		final long seed = 0x98da961379cae813L;
		boolean defVal = true;
		BiFunction<Graph, Object, Weights<Boolean>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				boolean.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryBool(seed), defVal, seed);
	}

	@Test
	public void testWeightsChars() {
		final long seed = 0x8ede26a1638aef7dL;
		BiFunction<Graph, Object, Weights<Character>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				char.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryChar(seed), (char) 0, seed);
	}

	@Test
	public void testWeightsCharsWithDefaultWeight() {
		final long seed = 0x25b63aa72ff77460L;
		char defVal = (char) 0xa5fb;
		BiFunction<Graph, Object, Weights<Character>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key,
				char.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryChar(seed), defVal, seed);
	}

	private static <E> void testWeights(BiFunction<Graph, Object, Weights<E>> edgeWeightsAdder,
			Supplier<E> weightFactory, E defaultWeight, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seed);
		final int n = 1024;
		final int m = 5000;

		for (boolean removeEdges : new boolean[] { false, true }) {
			Graph g;
			if (removeEdges) {
				g = GraphsTestUtils.randGraph(n, m, GraphArrayWithFixEdgesIDsTest.graphImpl(), seedGen.nextSeed());
			} else {
				g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			}

			Weights<E> weights = edgeWeightsAdder.apply(g, "edgeWeight");
			assertEquals(defaultWeight, weights.defaultWeight());

			int[] edges = g.edges().toIntArray();
			int edgesLen = edges.length;
			Int2ObjectMap<E> assignedEdges = new Int2ObjectOpenHashMap<>();
			assignedEdges.defaultReturnValue(defaultWeight);
			while (assignedEdges.size() < g.edges().size() * 3 / 4) {
				int op = rand.nextInt(10);

				if (op == 9 && removeEdges) {
					int eIdx = rand.nextInt(edgesLen);
					int e = edges[eIdx];
					g.removeEdge(e);
					assignedEdges.remove(e);
					edges[eIdx] = edges[--edgesLen];
					continue;
				}

				if (op % 2 == 0) {

					// Choose random unassigned edge
					int e;
					do {
						e = edges[rand.nextInt(edgesLen)];
					} while (assignedEdges.containsKey(e));

					// assigned to it a new value
					E val = weightFactory.get();
					weights.set(e, val);
					assignedEdges.put(e, val);

				} else {
					int e = edges[rand.nextInt(edgesLen)];
					E actual = weights.get(e);
					E expected = assignedEdges.get(e);
					assertEquals(expected, actual);
				}
			}
		}
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

}
