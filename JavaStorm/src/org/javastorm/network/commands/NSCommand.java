package org.javastorm.network.commands;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.javastorm.util.MyByteBuffer;

//Represents a NS Zacket.
public abstract class NSCommand {
	// Returns this command's id.
	public abstract int getCommandID();

	// Returns the client minor version.
	public int _getMinorVersion() {
		return this.minorVersion;
	}

	public void _setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}

	// Returns this command's version.
	public int getVersion() {
		return 1;
	}

	// Returns this command in raw byte data.
	public abstract MyByteBuffer getCommandData();

	// Reads in information for this command from the supplied buffer.
	public abstract void readCommandData(MyByteBuffer buffer);

	// Because I'm a lazy shithead, some of the zackets don't have either
	// read/write properly implemented.
	// So I'll just keep a reference to the raw buffer for sending/receiving
	// with the proxy.
	public void setCommandBuffer(MyByteBuffer buffer) {
		this.buffer = buffer;
	}

	/**
	 * Can be implemented by subtypes to output a list of all data contained in
	 * the zacket in a tidy and human readable fashion. Returns a string array,
	 * each element represents a line, which represents a property.
	 */
	public String[] outputDebug() {
		return new String[0];
	}

	public MyByteBuffer getCommandBuffer() {
		return this.buffer;
	}

	private static final long serialVersionUID = 6399426248033104389L;

	private MyByteBuffer buffer;

	private int minorVersion;
}