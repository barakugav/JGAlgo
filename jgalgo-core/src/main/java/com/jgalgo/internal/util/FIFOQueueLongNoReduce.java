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

package com.jgalgo.internal.util;

import java.io.Serializable;
import java.util.NoSuchElementException;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongIterable;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;

/**
 * A duplication of {@link it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue}, but without the {@code reduce()} operation.
 * Namely, the internal buffer is never replaced with a smaller one.
 *
 * @author Barak Ugav
 */
public class FIFOQueueLongNoReduce implements LongPriorityQueue, Serializable, LongIterable {

	private static final long serialVersionUID = 0L;
	/** The standard initial capacity of a queue. */
	static final int INITIAL_CAPACITY = 4;
	/** The backing array. */
	transient long[] array;
	/** The current (cached) length of {@link #array}. */
	transient int length;
	/**
	 * The start position in {@link #array}. It is always strictly smaller than {@link #length}.
	 */
	transient int start;
	/**
	 * The end position in {@link #array}. It is always strictly smaller than {@link #length}. Might be actually smaller
	 * than {@link #start} because {@link #array} is used cyclically.
	 */
	transient int end;

	/**
	 * Creates a new empty queue with given capacity.
	 *
	 * @implNote          Because of inner limitations of the JVM, the initial capacity cannot exceed
	 *                    {@link it.unimi.dsi.fastutil.Arrays#MAX_ARRAY_SIZE} &minus; 1.
	 *
	 * @param    capacity the initial capacity of this queue.
	 */

	public FIFOQueueLongNoReduce(final int capacity) {
		if (capacity > it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE - 1)
			throw new IllegalArgumentException(
					"Initial capacity (" + capacity + ") exceeds " + (it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE - 1));
		if (capacity < 0)
			throw new IllegalArgumentException("Initial capacity (" + capacity + ") is negative");
		// We never build a queue with a zero-sized backing array; moreover, to
		// avoid resizing at the given capacity we need one additional element.
		array = new long[Math.max(1, capacity + 1)];
		length = array.length;
	}

	/**
	 * Creates a new empty queue with standard {@linkplain #INITIAL_CAPACITY initial capacity}.
	 */
	public FIFOQueueLongNoReduce() {
		this(INITIAL_CAPACITY);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec This implementation returns {@code null} (FIFO queues have no comparator).
	 */
	@Override
	public LongComparator comparator() {
		return null;
	}

	@Override
	public long dequeueLong() {
		if (start == end)
			throw new NoSuchElementException();
		final long t = array[start];
		if (++start == length)
			start = 0;
		return t;
	}

	/**
	 * Dequeues the {@linkplain PriorityQueue#last() last} element from the queue.
	 *
	 * @return                        the dequeued element.
	 * @throws NoSuchElementException if the queue is empty.
	 */
	long dequeueLastLong() {
		if (start == end)
			throw new NoSuchElementException();
		if (end == 0)
			end = length;
		final long t = array[--end];
		return t;
	}

	private final void resize(final int size, final int newLength) {
		final long[] newArray = new long[newLength];
		if (start >= end) {
			if (size != 0) {
				System.arraycopy(array, start, newArray, 0, length - start);
				System.arraycopy(array, 0, newArray, length - start, end);
			}
		} else
			System.arraycopy(array, start, newArray, 0, end - start);
		start = 0;
		end = size;
		array = newArray;
		length = newLength;
	}

	private final void expand() {
		resize(length, (int) Math.min(it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE, 2L * length));
	}

	@Override
	public void enqueue(long x) {
		array[end++] = x;
		if (end == length)
			end = 0;
		if (end == start)
			expand();
	}

	/**
	 * Enqueues a new element as the first element (in dequeuing order) of the queue.
	 *
	 * @param x the element to enqueue.
	 */
	void enqueueFirst(long x) {
		if (start == 0)
			start = length;
		array[--start] = x;
		if (end == start)
			expand();
	}

	@Override
	public long firstLong() {
		if (start == end)
			throw new NoSuchElementException();
		return array[start];
	}

	@Override
	public long lastLong() {
		if (start == end)
			throw new NoSuchElementException();
		return array[(end == 0 ? length : end) - 1];
	}

	@Override
	public void clear() {
		start = end = 0;
	}

	/** Trims the queue to the smallest possible size. */

	void trim() {
		final int size = size();
		final long[] newArray = new long[size + 1];
		if (start <= end)
			System.arraycopy(array, start, newArray, 0, end - start);
		else {
			System.arraycopy(array, start, newArray, 0, length - start);
			System.arraycopy(array, 0, newArray, length - start, end);
		}
		start = 0;
		length = (end = size) + 1;
		array = newArray;
	}

	@Override
	public int size() {
		final int apparentLength = end - start;
		return apparentLength >= 0 ? apparentLength : length + apparentLength;
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		s.defaultWriteObject();
		int size = size();
		s.writeInt(size);
		for (int i = start; size-- != 0;) {
			s.writeLong(array[i++]);
			if (i == length)
				i = 0;
		}
	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		end = s.readInt();
		array = new long[length = HashCommon.nextPowerOfTwo(end + 1)];
		for (int i = 0; i < end; i++)
			array[i] = s.readLong();
	}

	@Override
	public LongIterator iterator() {
		return new LongIterator() {

			int idx = start;

			@Override
			public boolean hasNext() {
				return idx != end;
			}

			@Override
			public long nextLong() {
				Assertions.hasNext(this);
				long ret = array[idx];
				if (++idx == length)
					idx = 0;
				return ret;
			}

		};
	}
}
