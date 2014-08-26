package org.javastorm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import org.javastorm.tarc.ByteBufferInputStream;
import org.javastorm.tarc.Tarc;

// Abstraction layer to find files.
public class Filesystem
{
	public boolean init(File nsBaseDir)
	{
		if (!nsBaseDir.exists())
		{
			return false;
		}

		this.baseDir = nsBaseDir;

		File tarcFile = new File(nsBaseDir, "netstorm.tarc");
		if (tarcFile.exists())
		{
			this.tarc = new Tarc();
			if (!this.tarc.open(tarcFile))
				return false;
		}

		return true;
	}

	// Opens file as Reader stream.
	public Reader open(String filename)
	{
		// First check the TARC, if we have one open.
		if (this.tarc != null)
		{
			filename = filename.replace('/', '\\');
			if (!filename.startsWith("\\"))
				filename = '\\' + filename;

			String data = this.tarc.getFile(filename);
			if (data != null)
			{
				StringReader buf = new StringReader(data);
				return buf;
			}
		}

		File file = new File(this.baseDir, filename);
		if (file.exists())
		{
			try
			{
				Reader in = new FileReader(file);
				return in;
			}
			catch (IOException ioe)
			{
				return null;
			}
		}

		return null;
	}

	// Opens file as stream.
	public InputStream openStream(String filename)
	{
		// First check the TARC, if we have one open.
		if (this.tarc != null)
		{
			filename = filename.replace('/', '\\');
			if (!filename.startsWith("\\"))
				filename = '\\' + filename;

			ByteBuffer data = this.tarc.getFileBuffer(filename);
			if (data != null)
			{
				ByteBufferInputStream buf = new ByteBufferInputStream(data);
				return buf;
			}
		}

		File file = new File(this.baseDir, filename);
		if (file.exists())
		{
			try
			{
				FileInputStream in = new FileInputStream(file);
				return in;
			}
			catch (IOException ioe)
			{
				return null;
			}
		}

		return null;
	}

	// Opens file as Buffer.
	public ByteBuffer openBuffer(String filename)
	{
		// First check the TARC, if we have one open.
		if (this.tarc != null)
		{
			filename = filename.replace('/', '\\');
			if (!filename.startsWith("\\"))
				filename = '\\' + filename;

			ByteBuffer buf = this.tarc.getFileBuffer(filename);
			if (buf != null)
			{
				return buf;
			}
		}

		File file = new File(this.baseDir, filename);
		if (file.exists())
		{
			try
			{
				FileInputStream fin = new FileInputStream(file);
				return fin.getChannel().map(MapMode.READ_ONLY, 0, fin.getChannel().size());
			}
			catch (IOException ioe)
			{
				return null;
			}
		}

		return null;
	}

	private File baseDir;

	private Tarc tarc;
}
