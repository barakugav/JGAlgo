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

import java.io.PrintStream;

public class DebugPrinter {

	private boolean enable;
	private PrintStream printStream;

	public DebugPrinter(boolean enable) {
		this.enable = enable;
		printStream = System.out;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public void setPrintStream(PrintStream printStream) {
		this.printStream = printStream;
	}

	public void println() {
		if (enable)
			printStream.println();
	}

	public void print(byte s) {
		if (enable)
			printStream.print(s);
	}

	public void println(byte s) {
		if (enable)
			printStream.println(s);
	}

	public void print(char s) {
		if (enable)
			printStream.print(s);
	}

	public void println(char s) {
		if (enable)
			printStream.println(s);
	}

	public void print(short s) {
		if (enable)
			printStream.print(s);
	}

	public void println(short s) {
		if (enable)
			printStream.println(s);
	}

	public void print(int s) {
		if (enable)
			printStream.print(s);
	}

	public void println(int s) {
		if (enable)
			printStream.println(s);
	}

	public void print(long s) {
		if (enable)
			printStream.print(s);
	}

	public void println(long s) {
		if (enable)
			printStream.println(s);
	}

	public void print(float s) {
		if (enable)
			printStream.print(s);
	}

	public void println(float s) {
		if (enable)
			printStream.println(s);
	}

	public void print(double s) {
		if (enable)
			printStream.print(s);
	}

	public void println(double s) {
		if (enable)
			printStream.println(s);
	}

	public void print(boolean s) {
		if (enable)
			printStream.print(s);
	}

	public void println(boolean s) {
		if (enable)
			printStream.println(s);
	}

	public void print(Object s) {
		if (enable)
			printStream.print(s);
	}

	public void println(Object s) {
		if (enable)
			printStream.println(s);
	}

	public void print(Object arg1, Object... args) {
		if (enable) {
			StringBuilder builder = new StringBuilder();
			builder.append(arg1);
			for (Object arg : args)
				builder.append(arg);
			printStream.print(builder.toString());
		}
	}

	public void println(Object... args) {
		if (enable) {
			StringBuilder builder = new StringBuilder();
			for (Object arg : args)
				builder.append(arg);
			printStream.println(builder.toString());
		}
	}

	public void format(String s, Object... args) {
		if (enable)
			printStream.format(s, args);
	}

	public void printExec(Runnable exec) {
		if (enable)
			exec.run();
	}

}
