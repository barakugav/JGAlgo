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

interface WeightsStringer {

	String getWeightAsString(int id);

	static WeightsStringer newInstance(Weights<?> weights) {
		return newInstance(weights, null, null);
	}

	static WeightsStringer newInstance(Weights<?> weights, String nonNumberPrefix, String nonNumberSuffix) {
		boolean nonNumberEnclosing = false;
		if (nonNumberPrefix != null || nonNumberSuffix != null) {
			if (nonNumberPrefix == null)
				nonNumberPrefix = "";
			if (nonNumberSuffix == null)
				nonNumberSuffix = "";
			nonNumberEnclosing = true;
		}

		if (weights instanceof Weights.Byte) {
			Weights.Byte ws = (Weights.Byte) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof Weights.Short) {
			Weights.Short ws = (Weights.Short) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof Weights.Int) {
			Weights.Int ws = (Weights.Int) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof Weights.Long) {
			Weights.Long ws = (Weights.Long) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof Weights.Float) {
			Weights.Float ws = (Weights.Float) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof Weights.Double) {
			Weights.Double ws = (Weights.Double) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof Weights.Bool) {
			Weights.Bool ws = (Weights.Bool) weights;
			if (nonNumberEnclosing) {
				String pre = nonNumberPrefix, post = nonNumberSuffix;
				return elm -> pre + String.valueOf(ws.get(elm)) + post;
			} else {
				return elm -> String.valueOf(ws.get(elm));
			}

		} else if (weights instanceof Weights.Char) {
			Weights.Char ws = (Weights.Char) weights;
			if (nonNumberEnclosing) {
				String pre = nonNumberPrefix, post = nonNumberSuffix;
				return elm -> pre + String.valueOf(ws.get(elm)) + post;
			} else {
				return elm -> String.valueOf(ws.get(elm));
			}

		} else if (weights instanceof Weights.Obj) {
			Weights.Obj<?> ws = (Weights.Obj<?>) weights;
			if (nonNumberEnclosing) {
				String pre = nonNumberPrefix, post = nonNumberSuffix;
				return elm -> pre + String.valueOf(ws.get(elm)) + post;
			} else {
				return elm -> String.valueOf(ws.get(elm));
			}

		} else {
			throw new IllegalArgumentException("Unsupported weights type: " + weights.getClass());
		}
	}

}
