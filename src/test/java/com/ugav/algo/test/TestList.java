package com.ugav.algo.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TestList {

	private static final Collection<Class<?>> RMQ_TESTS = List.of(
			RMQLookupTableTest.class,
			RMQPowerOf2TableTest.class,
			RMQPlusMinusOneTest.class,
			RMQLinearTest.class);

	private static final Collection<Class<?>> HEAP_TESTS = List.of(
			HeapBinaryTest.class,
			HeapBinomialTest.class,
			HeapFibonacciTest.class);

	private static final Collection<Class<?>> MISC_TESTS = List.of(
			UnionFindTest.class);

	private static final Collection<Class<?>> GRAPHS_TESTS = List.of(
			LCARMQTest.class,
			DijkstraTest.class);

	private static final Collection<Class<?>> MST_TESTS = List.of(
			MSTBoruvskaTest.class,
			MSTKruskalTest.class,
			MSTPrimTest.class,
			MSTFredmanTarjan1987Test.class);

	static final Collection<Class<?>> TEST_CLASSES;
	static {
		Collection<Class<?>> l = new ArrayList<>();
		l.addAll(RMQ_TESTS);
		l.addAll(HEAP_TESTS);
		l.addAll(MISC_TESTS);
		l.addAll(GRAPHS_TESTS);
		l.addAll(MST_TESTS);
		TEST_CLASSES = Collections.unmodifiableCollection(l);
	}

}
