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

import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * A cut that partition the vertices of a graph into two sets.
 * <p>
 * Given a graph \(G=(V,E)\), a cut is a partition of \(V\) into two sets \(C, \bar{C} = V \setminus C\). Given a weight
 * function, the weight of a cut \((C,\bar{C})\) is the weight sum of all edges \((u,v)\) such that \(u\) is in \(C\)
 * and \(v\) is in \(\bar{C}\). When we say 'the cut' we often mean the first set \(C\).
 *
 * @see    MinimumCutST
 * @author Barak Ugav
 */
public interface Cut {

	/**
	 * Check whether a vertex is in the cut.
	 * <p>
	 * When we say 'the cut' we mean the first set \(C\) out of the cut partition into two sets \(C, \bar{C} = V
	 * \setminus C\).
	 *
	 * @param  vertex a vertex identifier
	 * @return        {@code true} if {@code vertex} is in the first set of the partition, \(C\)
	 */
	boolean containsVertex(int vertex);

	/**
	 * Get the collection of all the vertices in the cut.
	 * <p>
	 * When we say 'the cut' we mean the first set \(C\) out of the cut partition into two sets \(C, \bar{C} = V
	 * \setminus C\).
	 *
	 * @return a collection of all the vertices of the first set of the partition, \(C\)
	 */
	IntCollection vertices();

	/**
	 * Get the collection of all the edges that cross the cut.
	 * <p>
	 * Given a cut \(C, \bar{C} = V \setminus C\), the edges that <i>cross</i> the cut are edges \((u,v)\) such that
	 * \(u\) is in \(C\) and \(v\) is in \(\bar{C}\).
	 *
	 * @return a collection of all the edges that cross the cut partition
	 */
	IntCollection edges();

	/**
	 * Get the weight of the cut with respect to the given weight function.
	 * <p>
	 * Given a weight function, the weight of a cut \((C,\bar{C})\) is the weight sum of all edges \((u,v)\) such that
	 * \(u\) is in \(C\) and \(v\) is in \(\bar{C}\).
	 *
	 * @param  w an edge weight function
	 * @return   the sum of edge weights \((u,v)\) such that \(u\) is in \(C\) and \(v\) is in \(\bar{C}\)
	 */
	double weight(EdgeWeightFunc w);

}
