package com.ugav.algo.test;

import java.util.Arrays;
import java.util.Random;

public class Utils {

    static void printArr(int a[]) {
	printArr(a, true);
    }

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

    static int[] randArray(int n) {
	return randArray(n, 0, Integer.MAX_VALUE);
    }

    static int[] randArray(int n, int from, int to) {
	return randArray(new int[n], from, to);
    }

    static int[] randArray(int[] a, int from, int to) {
	Random rand = new Random();
	for (int i = 0; i < a.length; i++)
	    a[i] = rand.nextInt(to - from) + from;
	return a;
    }

    static int[] randPermutation(int n) {
	Random rand = new Random();

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

}
