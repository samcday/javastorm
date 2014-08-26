package org.javastorm.tarc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import org.javastorm.util.MyDataInputStream;

// A class to manipulate .tarc files.
public class Tarc
{
	public boolean open(File file)
	{
		try
		{
			// Open the file.
			FileInputStream fin = new FileInputStream(file);
			int fileSize = (int) fin.getChannel().size();
			MyDataInputStream in = new MyDataInputStream(fin);
			// First 20 bytes seem to be magic values and a version number.
			in.skip(20);

			// How many files are contained in this tarc?
			this.numFiles = in.readInt();

			// Not sure.
			in.skip(4);

			// Offset from beginning of file where the table of offsets for the file list block starts. lol.
			in.skip(4);

			// This value lets us know where the file list starts. Offset from beginning of file.
			in.readInt();

			// How big is the file info block?
			in.readInt();

			// Next int is where data starts, again offset from start of file.
			int dataOffset = in.readInt();

			// The next block is basically a list of offsets into the filelist block.
			for (int i = 0; i < this.numFiles; i++)
			{
				in.skip(4);
			}

			// We're now up to the file list block.
			this.files = new String[this.numFiles];
			this.fileLens = new int[this.numFiles];
			this.fileNames = new String[this.numFiles];
			this.fileOffsets = new int[this.numFiles];

			int maxFileLen = 0;

			for (int i = 0; i < this.numFiles; i++)
			{
				// First up is an int telling us offset to get to file from start of datablock.
				this.fileOffsets[i] = in.readInt();

				// Next up is length of file.
				this.fileLens[i] = in.readInt();

				// Next up is filename.
				this.fileNames[i] = in.readLine();
				if (this.fileLens[i] > maxFileLen)
					maxFileLen = this.fileLens[i];
			}

			this.tarc = fin.getChannel().map(MapMode.READ_ONLY, dataOffset, fileSize - dataOffset);
			char fileDataMask[] = "mydoghasfleas".toCharArray();
			int fileDataMaskIndex = 0;

			StringBuilder builder = new StringBuilder(maxFileLen);

			for (int i = 0; i < this.numFiles; i++)
			{
				fileDataMaskIndex = 0;

				for (int j = 0; j < this.fileLens[i]; j++)
				{
					builder.append((char) (this.tarc.get() ^ fileDataMask[fileDataMaskIndex++]));

					if (fileDataMaskIndex == fileDataMask.length)
						fileDataMaskIndex = 0;
				}

				this.files[i] = builder.toString();
				builder.setLength(0);
			}
		}
		catch (IOException e)
		{
			return false;
		}
		return true;
	}

	public int getNumFiles()
	{
		return this.numFiles;
	}

	public String getFile(String filename)
	{
		for (int i = 0; i < this.numFiles; i++)
			if (this.fileNames[i].equalsIgnoreCase(filename))
				return this.files[i];

		return null;
	}

	public ByteBuffer getFileBuffer(String filename)
	{
		for (int i = 0; i < this.numFiles; i++)
			if (this.fileNames[i].equalsIgnoreCase(filename))
			{
				int oldLimit = this.tarc.limit();
				this.tarc.position(this.fileOffsets[i]);
				this.tarc.limit(this.fileLens[i]);

				ByteBuffer file = this.tarc.slice();

				this.tarc.position(0);
				this.tarc.limit(oldLimit);

				return file;
			}

		return null;
	}

	public String[] getFileList()
	{
		return this.fileNames;
	}

	private ByteBuffer tarc;

	private int numFiles; // How many files in this .tarc?

	private String files[]; // The actual files in this .tarc.

	private String fileNames[];

	private int fileLens[]; // The length of each file, for reading.

	private int fileOffsets[];
}
