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
