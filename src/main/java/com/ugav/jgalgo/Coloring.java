package com.ugav.jgalgo;

public interface Coloring {

	Coloring.Result calcColoring(UGraph g);

	interface Result {

		int colorsNum();

		int colorOf(int v);

	}

}
