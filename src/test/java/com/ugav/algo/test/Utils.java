package com.ugav.algo.test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class Utils {

	static void printArr(int a[]) {
		printArr(a, true);
	}

	@SuppressWarnings("boxing")
	static void printArr(int a[], boolean printIndicies) {
		for (int i = 0; i < a.length; i++)
			System.out.print("" + String.format("%03d", a[i]) + ", ");
		System.out.println();
		if (printIndicies) {
			for (int i = 0; i < a.length; i++)
				System.out.print("" + String.format("%03d", i) + ", ");
			System.out.println();
		}
	}

	static int[] randArray(int n, long seed) {
		return randArray(n, 0, Integer.MAX_VALUE, seed);
	}

	static int[] randArray(int n, int from, int to, long seed) {
		return randArray(new int[n], from, to, seed);
	}

	static int[] randArray(int[] a, int from, int to, long seed) {
		Random rand = new Random(seed ^ 0x64bf2cc6dd4c257eL);
		for (int i = 0; i < a.length; i++)
			a[i] = rand.nextInt(to - from) + from;
		return a;
	}

	static int[] randPermutation(int n, long seed) {
		Random rand = new Random(seed ^ 0xb281dc30ae96a316L);

		boolean[] possibleValuesBitmap = new boolean[n];
		Arrays.fill(possibleValuesBitmap, true);

		int[] possibleValues = new int[n];
		for (int i = 0; i < n; i++)
			possibleValues[i] = i;
		int possibleValuesArrLen = n;
		int possibleValuesSize = n;
		int nextShrink = possibleValuesSize / 2;
		int[] possibleValuesNext = new int[nextShrink];

		int[] a = new int[n];
		for (int i = 0; i < n; i++) {
			if (possibleValuesSize == nextShrink && nextShrink > 4) {
				for (int j = 0, k = 0; k < nextShrink; j++) {
					if (possibleValuesBitmap[j])
						possibleValuesNext[k++] = possibleValues[j];
				}
				int[] temp = possibleValues;
				possibleValues = possibleValuesNext;
				possibleValuesNext = temp;
				possibleValuesArrLen = possibleValuesSize;

				Arrays.fill(possibleValuesBitmap, true);
				nextShrink = possibleValuesSize / 2;
			}

			int idx;
			do {
				idx = rand.nextInt(possibleValuesArrLen);
			} while (!possibleValuesBitmap[idx]);

			a[i] = possibleValues[idx];
			possibleValuesBitmap[idx] = false;
			possibleValuesSize--;
		}

		return a;
	}

	static <E> Iterable<E> iterable(Iterator<E> it) {
		return new Iterable<>() {

			@Override
			public Iterator<E> iterator() {
				return it;
			}
		};
	}

}
