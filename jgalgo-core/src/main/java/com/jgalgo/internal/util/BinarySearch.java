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

import java.util.function.IntUnaryOperator;
import it.unimi.dsi.fastutil.ints.IntIntPair;

public class BinarySearch {

	private BinarySearch() {}

	public static int lowerBound(int from, int to, int key, IntUnaryOperator idx2key) {
		if (from > to)
			throw new IllegalArgumentException("from > to: " + from + " > " + to);
		return lowerBound0(from, to, key, idx2key);
	}

	private static int lowerBound0(int from, int to, int key, IntUnaryOperator idx2key) {
		for (int len = to - from; len > 0;) {
			int half = len / 2;
			int mid = from + half;
			if (idx2key.applyAsInt(mid) < key) {
				from = mid + 1;
				len = len - half - 1;
			} else {
				len = half;
			}
		}
		return from;
	}

	public static int upperBound(int from, int to, int key, IntUnaryOperator idx2key) {
		if (from > to)
			throw new IllegalArgumentException("from > to: " + from + " > " + to);
		return upperBound0(from, to, key, idx2key);
	}

	private static int upperBound0(int from, int to, int key, IntUnaryOperator idx2key) {
		for (int len = to - from; len > 0;) {
			int half = len >> 1;
			int mid = from + half;
			if (key < idx2key.applyAsInt(mid)) {
				len = half;
			} else {
				from = mid + 1;
				len = len - half - 1;
			}
		}
		return from;
	}

	public static IntIntPair equalRange(int from, int to, int key, IntUnaryOperator idx2key) {
		if (from > to)
			throw new IllegalArgumentException("from > to: " + from + " > " + to);
		for (int len = to - from; len > 0;) {
			int half = len / 2;
			int mid = from + half;
			int midKey = idx2key.applyAsInt(mid);
			if (midKey < key) {
				from = mid + 1;
				len = len - half - 1;
			} else if (key < midKey) {
				len = half;
			} else {
				int left = lowerBound0(from, mid, key, idx2key);
				int right = upperBound0(mid + 1, from + len, key, idx2key);
				return IntIntPair.of(left, right);
			}
		}
		return null;
	}

}
