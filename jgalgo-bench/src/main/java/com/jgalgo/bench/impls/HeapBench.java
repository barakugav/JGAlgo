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

package com.jgalgo.bench.impls;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import com.jgalgo.bench.util.BenchUtils;
import com.jgalgo.bench.util.TestUtils.SeedGenerator;
import com.jgalgo.internal.ds.Heap;
import com.jgalgo.internal.ds.IntIntBinomialHeap;
import com.jgalgo.internal.ds.IntIntFibonacciHeap;
import com.jgalgo.internal.ds.IntIntPairingHeap;
import com.jgalgo.internal.ds.IntIntRedBlackTree;
import com.jgalgo.internal.ds.IntIntReferenceableHeap;
import com.jgalgo.internal.ds.IntIntSplayTree;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 0)
@State(Scope.Benchmark)
public class HeapBench {

	@Param({ "N=64 M=256", "N=512 M=4096", "N=4096 M=16384" })
	public String args;
	private List<List<Op>> sequences;
	private final int sequencesNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		int initialSize = Integer.parseInt(argsMap.get("N"));
		int opsNum = Integer.parseInt(argsMap.get("M"));

		final SeedGenerator seedGen = new SeedGenerator(0x88da246e71ef3dacL);
		Random rand = new Random(seedGen.nextSeed());
		sequences = new ObjectArrayList<>(sequencesNum);
		for (int gIdx = 0; gIdx < sequencesNum; gIdx++) {
			List<Op> sequence = new ObjectArrayList<>(initialSize + opsNum);
			for (int i = 0; i < initialSize; i++)
				sequence.add(new Op.Insert(rand.nextInt()));
			for (int i = 0; i < opsNum; i++) {
				int r = rand.nextInt(5);
				if (r < 2)
					sequence.add(new Op.Insert(rand.nextInt()));
				else if (r < 4)
					sequence.add(new Op.FindMin());
				else
					sequence.add(new Op.ExtractMin());
			}
			sequences.add(sequence);
		}
	}

	private static class Op {
		private static class Insert extends Op {
			final int x;

			Insert(int x) {
				this.x = x;
			}
		}

		private static class FindMin extends Op {
		}

		private static class ExtractMin extends Op {
		}
	}

	private void benchHeap(Heap.Builder heapBuilder, Blackhole blackhole) {
		Heap<Integer> heap = heapBuilder.build();

		List<Op> sequence = sequences.get(graphIdx.getAndUpdate(i -> (i + 1) % sequencesNum));
		for (Op op : sequence) {
			if (op instanceof Op.Insert) {
				heap.insert(Integer.valueOf(((Op.Insert) op).x));

			} else if (op instanceof Op.FindMin) {
				if (heap.isEmpty())
					continue;
				Integer min = heap.findMin();
				blackhole.consume(min);

			} else if (op instanceof Op.ExtractMin) {
				if (heap.isEmpty())
					continue;
				Integer min = heap.extractMin();
				blackhole.consume(min);

			} else {
				throw new IllegalArgumentException("unknown op: " + op);
			}
		}
	}

	@Benchmark
	public void Binary(Blackhole blackhole) {
		benchHeap(Heap.builder(), blackhole);
	}

	@Benchmark
	public void Pairing(Blackhole blackhole) {
		benchHeap(heapBuilder(IntIntPairingHeap::new), blackhole);
	}

	@Benchmark
	public void Fibonacci(Blackhole blackhole) {
		benchHeap(heapBuilder(IntIntFibonacciHeap::new), blackhole);
	}

	@Benchmark
	public void Binomial(Blackhole blackhole) {
		benchHeap(heapBuilder(IntIntBinomialHeap::new), blackhole);
	}

	@Benchmark
	public void RedBlackTree(Blackhole blackhole) {
		benchHeap(heapBuilder(IntIntRedBlackTree::new), blackhole);
	}

	@Benchmark
	public void SplayTree(Blackhole blackhole) {
		benchHeap(heapBuilder(IntIntSplayTree::new), blackhole);
	}

	@SuppressWarnings("unchecked")
	private static Heap.Builder heapBuilder(Function<IntComparator, IntIntReferenceableHeap> builder) {
		return new Heap.Builder() {
			@Override
			public <E> Heap<E> build(Comparator<? super E> cmp) {
				return (Heap<E>) new HeapFromReferenceableHeap(builder.apply((IntComparator) cmp));
			}
		};
	}

	static class HeapFromReferenceableHeap implements Heap<Integer> {

		private final IntIntReferenceableHeap heap;

		HeapFromReferenceableHeap(IntIntReferenceableHeap heap) {
			this.heap = heap;
		}

		@Override
		public Iterator<Integer> iterator() {
			return IterTools.map(heap.iterator(), IntIntReferenceableHeap.Ref::key);
		}

		@Override
		public void insert(Integer elm) {
			heap.insert(elm.intValue());
		}

		@Override
		public void insertAll(Collection<? extends Integer> elms) {
			for (Integer elm : elms)
				heap.insert(elm.intValue());
		}

		@Override
		public Integer findMin() {
			return Integer.valueOf(heap.findMin().key());
		}

		@Override
		public Integer extractMin() {
			return Integer.valueOf(heap.extractMin().key());
		}

		@Override
		public boolean remove(Integer elm) {
			IntIntReferenceableHeap.Ref ref = heap.find(elm.intValue());
			if (ref == null)
				return false;
			heap.remove(ref);
			return true;
		}

		@Override
		public void meld(Heap<? extends Integer> heap) {
			throw new UnsupportedOperationException("Unimplemented method 'meld'");
		}

		@Override
		public boolean isEmpty() {
			return heap.isEmpty();
		}

		@Override
		public boolean isNotEmpty() {
			return heap.isNotEmpty();
		}

		@Override
		public void clear() {
			heap.clear();
		}

		@Override
		public Comparator<? super Integer> comparator() {
			return heap.comparator();
		}
	}

}
