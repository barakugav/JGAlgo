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

	public void print(String s) {
		if (enable)
			printStream.print(s);
	}

	public void println(String s) {
		if (enable)
			printStream.println(s);
	}

	public void println() {
		if (enable)
			printStream.println();
	}

	public void print(byte s) {
		print(String.valueOf(s));
	}

	public void println(byte s) {
		println(String.valueOf(s));
	}

	public void print(char s) {
		print(String.valueOf(s));
	}

	public void println(char s) {
		println(String.valueOf(s));
	}

	public void print(short s) {
		print(String.valueOf(s));
	}

	public void println(short s) {
		println(String.valueOf(s));
	}

	public void print(int s) {
		print(String.valueOf(s));
	}

	public void println(int s) {
		println(String.valueOf(s));
	}

	public void print(long s) {
		print(String.valueOf(s));
	}

	public void println(long s) {
		println(String.valueOf(s));
	}

	public void print(float s) {
		print(String.valueOf(s));
	}

	public void println(float s) {
		println(String.valueOf(s));
	}

	public void print(double s) {
		print(String.valueOf(s));
	}

	public void println(double s) {
		println(String.valueOf(s));
	}

	public void print(boolean s) {
		print(String.valueOf(s));
	}

	public void println(boolean s) {
		println(String.valueOf(s));
	}

	public void print(Object s) {
		print(String.valueOf(s));
	}

	public void println(Object s) {
		println(String.valueOf(s));
	}

	public void printExec(Runnable exec) {
		if (enable)
			exec.run();
	}

}
