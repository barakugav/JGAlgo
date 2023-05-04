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

package com.jgalgo;

/**
 * Metric Traveling Salesman Problem (TSP) algorithm.
 * <p>
 * Given a list of points, the traveling salesman problem asking what is the shortest tour to visit all points. The
 * metric version of TSP is a special case in which the distances satisfy the triangle inequality and the distances are
 * symmetric.
 * <p>
 * The problem itself is NP-hard, but various approximation algorithms exists.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Travelling_salesman_problem">Wikipedia</a>
 * @author Barak Ugav
 */
public interface TSPMetric {

	/**
	 * Compute the shortest tour that visit all points.
	 * <p>
	 * Note that this problem is NP-hard and therefore the result is only the best approximation the implementation
	 * could find.
	 *
	 * @param  distances \(n \times n\) table of distances between each two points. In the metric world every three
	 *                       vertices \(u,v,w\) should satisfy \(d[u,v] + d[v,w] \leq d[u,w]$
	 * @return           list of the \(n\) vertices ordered by the calculated path
	 */
	public int[] computeShortestTour(double[][] distances);

}
