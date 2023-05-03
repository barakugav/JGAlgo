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

import java.io.PrintStream;

// TODO remove
class DebugPrintsManager {

	private boolean enable;
	private PrintStream printStream;

	DebugPrintsManager() {
		this(false);
	}

	DebugPrintsManager(boolean enable) {
		this.enable = enable;
		printStream = System.out;
	}

	boolean isEnable() {
		return enable;
	}

	void setEnable(boolean enable) {
		this.enable = enable;
	}

	void setPrintStream(PrintStream printStream) {
		this.printStream = printStream;
	}

	void println() {
		if (enable)
			printStream.println();
	}

	void print(byte s) {
		if (enable)
			printStream.print(s);
	}

	void println(byte s) {
		if (enable)
			printStream.println(s);
	}

	void print(char s) {
		if (enable)
			printStream.print(s);
	}

	void println(char s) {
		if (enable)
			printStream.println(s);
	}

	void print(short s) {
		if (enable)
			printStream.print(s);
	}

	void println(short s) {
		if (enable)
			printStream.println(s);
	}

	void print(int s) {
		if (enable)
			printStream.print(s);
	}

	void println(int s) {
		if (enable)
			printStream.println(s);
	}

	void print(long s) {
		if (enable)
			printStream.print(s);
	}

	void println(long s) {
		if (enable)
			printStream.println(s);
	}

	void print(float s) {
		if (enable)
			printStream.print(s);
	}

	void println(float s) {
		if (enable)
			printStream.println(s);
	}

	void print(double s) {
		if (enable)
			printStream.print(s);
	}

	void println(double s) {
		if (enable)
			printStream.println(s);
	}

	void print(boolean s) {
		if (enable)
			printStream.print(s);
	}

	void println(boolean s) {
		if (enable)
			printStream.println(s);
	}

	void print(Object s) {
		if (enable)
			printStream.print(s);
	}

	void println(Object s) {
		if (enable)
			printStream.println(s);
	}

	void print(Object arg1, Object... args) {
		if (enable) {
			StringBuilder builder = new StringBuilder();
			builder.append(arg1);
			for (Object arg : args)
				builder.append(arg);
			printStream.print(builder.toString());
		}
	}

	void println(Object... args) {
		if (enable) {
			StringBuilder builder = new StringBuilder();
			for (Object arg : args)
				builder.append(arg);
			printStream.println(builder.toString());
		}
	}

	void format(String s, Object... args) {
		if (enable)
			printStream.format(s, args);
	}

	void printExec(Runnable exec) {
		if (enable)
			exec.run();
	}

}
