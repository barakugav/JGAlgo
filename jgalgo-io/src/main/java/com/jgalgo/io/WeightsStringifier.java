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
package com.jgalgo.io;

import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.graph.WeightsByte;
import com.jgalgo.graph.WeightsChar;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsFloat;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsLong;
import com.jgalgo.graph.WeightsObj;
import com.jgalgo.graph.WeightsShort;

interface WeightsStringifier<K> {

	String getWeightAsString(K id);

	static <K> WeightsStringifier<K> newInstance(Weights<K, ?> weights) {
		return newInstance(weights, null, null);
	}

	@SuppressWarnings("unchecked")
	static <K> WeightsStringifier<K> newInstance(Weights<K, ?> weights, String nonNumberPrefix,
			String nonNumberSuffix) {
		boolean nonNumberEnclosing = false;
		if (nonNumberPrefix != null || nonNumberSuffix != null) {
			if (nonNumberPrefix == null)
				nonNumberPrefix = "";
			if (nonNumberSuffix == null)
				nonNumberSuffix = "";
			nonNumberEnclosing = true;
		}

		if (weights instanceof WeightsByte) {
			WeightsByte<K> ws = (WeightsByte<K>) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof WeightsShort) {
			WeightsShort<K> ws = (WeightsShort<K>) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof WeightsInt) {
			WeightsInt<K> ws = (WeightsInt<K>) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof WeightsLong) {
			WeightsLong<K> ws = (WeightsLong<K>) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof WeightsFloat) {
			WeightsFloat<K> ws = (WeightsFloat<K>) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof WeightsDouble) {
			WeightsDouble<K> ws = (WeightsDouble<K>) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof WeightsBool) {
			WeightsBool<K> ws = (WeightsBool<K>) weights;
			if (nonNumberEnclosing) {
				String pre = nonNumberPrefix, post = nonNumberSuffix;
				return elm -> pre + String.valueOf(ws.get(elm)) + post;
			} else {
				return elm -> String.valueOf(ws.get(elm));
			}

		} else if (weights instanceof WeightsChar) {
			WeightsChar<K> ws = (WeightsChar<K>) weights;
			if (nonNumberEnclosing) {
				String pre = nonNumberPrefix, post = nonNumberSuffix;
				return elm -> pre + String.valueOf(ws.get(elm)) + post;
			} else {
				return elm -> String.valueOf(ws.get(elm));
			}

		} else {
			WeightsObj<K, ?> ws = (WeightsObj<K, ?>) weights;
			if (nonNumberEnclosing) {
				String pre = nonNumberPrefix, post = nonNumberSuffix;
				return elm -> pre + String.valueOf(ws.get(elm)) + post;
			} else {
				return elm -> String.valueOf(ws.get(elm));
			}
		}
	}

}
