package com.ugav.algo;

import java.io.PrintStream;

public class DebugPrintsManager {

	private boolean enable;
	private PrintStream printStream;

	public DebugPrintsManager() {
		this(false);
	}

	public DebugPrintsManager(boolean enable) {
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

	public void print(Object arg1, Object arg2) {
		if (enable)
			printStream.print(String.valueOf(arg1) + arg2);
	}

	public void print(Object arg1, Object arg2, Object arg3) {
		if (enable)
			printStream.print(String.valueOf(arg1) + arg2 + arg3);
	}

	public void print(Object arg1, Object arg2, Object arg3, Object arg4) {
		if (enable)
			printStream.print(String.valueOf(arg1) + arg2 + arg3 + arg4);
	}

	public void print(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		if (enable)
			printStream.print(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5);
	}

	public void print(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
		if (enable)
			printStream.print(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5 + arg6);
	}

	public void print(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
		if (enable)
			printStream.print(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5 + arg6 + arg7);
	}

	public void print(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
			Object arg8) {
		if (enable)
			printStream.print(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5 + arg6 + arg7 + arg8);
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

	public void println(Object arg1, Object arg2) {
		if (enable)
			printStream.println(String.valueOf(arg1) + arg2);
	}

	public void println(Object arg1, Object arg2, Object arg3) {
		if (enable)
			printStream.println(String.valueOf(arg1) + arg2 + arg3);
	}

	public void println(Object arg1, Object arg2, Object arg3, Object arg4) {
		if (enable)
			printStream.println(String.valueOf(arg1) + arg2 + arg3 + arg4);
	}

	public void println(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		if (enable)
			printStream.println(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5);
	}

	public void println(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
		if (enable)
			printStream.println(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5 + arg6);
	}

	public void println(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
		if (enable)
			printStream.println(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5 + arg6 + arg7);
	}

	public void println(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
			Object arg8) {
		if (enable)
			printStream.println(String.valueOf(arg1) + arg2 + arg3 + arg4 + arg5 + arg6 + arg7 + arg8);
	}

	public void println(Object arg1, Object... args) {
		if (enable) {
			StringBuilder builder = new StringBuilder();
			builder.append(arg1);
			for (Object arg : args)
				builder.append(arg);
			printStream.println(builder.toString());
		}
	}

	public void format(String s, Object arg1) {
		if (enable)
			printStream.format(s, arg1);
	}

	public void format(String s, Object arg1, Object arg2) {
		if (enable)
			printStream.format(s, arg1, arg2);
	}

	public void format(String s, Object arg1, Object arg2, Object arg3) {
		if (enable)
			printStream.format(s, arg1, arg2, arg3);
	}

	public void format(String s, Object arg1, Object arg2, Object arg3, Object arg4) {
		if (enable)
			printStream.format(s, arg1, arg2, arg3, arg4);
	}

	public void format(String s, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		if (enable)
			printStream.format(s, arg1, arg2, arg3, arg4, arg5);
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
