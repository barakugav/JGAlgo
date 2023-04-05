package com.jgalgo;

import java.io.PrintStream;

// TODO remove
interface DebugPrintable {

	/**
	 * Check if debug prints are enabled
	 *
	 * @return true if debug prints are enabled
	 */
	public boolean isDebugPrintEnable();

	/**
	 * Enable/disable debug prints
	 *
	 * @param enable if true, debug prints will be enabled
	 */
	public void setDebugPrintEnable(boolean enable);

	/**
	 * Set the print stream of the debug prints
	 *
	 * @param printStream new stream for debug prints
	 */
	public void setDebugPrintStream(PrintStream printStream);

}
