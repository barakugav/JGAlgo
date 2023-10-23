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

import java.util.NoSuchElementException;
import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;

public class Range extends AbstractIntList {

	private final int from, to;

	private Range(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public static Range of(int to) {
		return new Range(0, to);
	}

	public static Range of(int from, int to) {
		return new Range(from, to);
	}

	@Override
	public int size() {
		return to - from;
	}

	@Override
	public boolean contains(int key) {
		return from <= key && key < to;
	}

	@Override
	public IntListIterator listIterator(int index) {
		return new IntListIterator() {

			int x;

			@Override
			public int previousInt() {
				if (x <= from)
					throw new NoSuchElementException();
				return --x;
			}

			@Override
			public int nextInt() {
				if (x >= to)
					throw new NoSuchElementException();
				return x++;
			}

			@Override
			public boolean hasPrevious() {
				return x > from;
			}

			@Override
			public boolean hasNext() {
				return x < to;
			}

			@Override
			public int nextIndex() {
				return x - from;
			}

			@Override
			public int previousIndex() {
				return x - from - 1;
			}

		};
	}

	@Override
	public IntList subList(int from, int to) {
		if (from == 0 && to == size())
			return this;
		ensureIndex(from);
		ensureIndex(to);
		return new Range(this.from + from, this.from + to);
	}

	@Override
	public int getInt(int index) {
		ensureIndex(index);
		return from + index;
	}

	@Override
	public int indexOf(int k) {
		return contains(k) ? k - from : -1;
	}

	@Override
	public int lastIndexOf(int k) {
		return contains(k) ? k - from : -1;
	}

}
