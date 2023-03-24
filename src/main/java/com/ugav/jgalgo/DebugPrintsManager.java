package com.ugav.jgalgo;

import java.io.PrintStream;

//TODO remove
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

	void print(Object arg1, Object arg2) {
		if (enable)
			printStream.print(String.valueOf(arg1) + arg2);
	}

	void print(Object arg1, Object arg2, Object arg3) {
		if (enable)
			printStream.print(String.valueOf(arg1) + arg2 + arg3);
	}

	void print(Object arg1, Object arg2, Object arg3, Object arg4) {
		if (enable)
			printStream.print(String.valueOf(arg1) + arg2 + arg3 + arg4);
	}

	void print(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		if (enable)
			printStream.print(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5);
	}

	void print(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
		if (enable)
			printStream.print(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5 + arg6);
	}

	void print(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
		if (enable)
			printStream.print(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5 + arg6 + arg7);
	}

	void print(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
		if (enable)
			printStream.print(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5 + arg6 + arg7 + arg8);
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

	void println(Object arg1, Object arg2) {
		if (enable)
			printStream.println(String.valueOf(arg1) + arg2);
	}

	void println(Object arg1, Object arg2, Object arg3) {
		if (enable)
			printStream.println(String.valueOf(arg1) + arg2 + arg3);
	}

	void println(Object arg1, Object arg2, Object arg3, Object arg4) {
		if (enable)
			printStream.println(String.valueOf(arg1) + arg2 + arg3 + arg4);
	}

	void println(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		if (enable)
			printStream.println(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5);
	}

	void println(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
		if (enable)
			printStream.println(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5 + arg6);
	}

	void println(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
		if (enable)
			printStream.println(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5 + arg6 + arg7);
	}

	void println(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
			Object arg8) {
		if (enable)
			printStream.println(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5 + arg6 + arg7 + arg8);
	}

	void println(Object arg1, Object... args) {
		if (enable) {
			StringBuilder builder = new StringBuilder();
			builder.append(arg1);
			for (Object arg : args)
				builder.append(arg);
			printStream.println(builder.toString());
		}
	}

	void format(String s, Object arg1) {
		if (enable)
			printStream.format(s, arg1);
	}

	void format(String s, Object arg1, Object arg2) {
		if (enable)
			printStream.format(s, arg1, arg2);
	}

	void format(String s, Object arg1, Object arg2, Object arg3) {
		if (enable)
			printStream.format(s, arg1, arg2, arg3);
	}

	void format(String s, Object arg1, Object arg2, Object arg3, Object arg4) {
		if (enable)
			printStream.format(s, arg1, arg2, arg3, arg4);
	}

	void format(String s, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		if (enable)
			printStream.format(s, arg1, arg2, arg3, arg4, arg5);
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
