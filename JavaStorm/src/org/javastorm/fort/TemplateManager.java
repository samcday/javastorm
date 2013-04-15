package org.javastorm.fort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.javastorm.territory.Territory;
import org.javastorm.util.MarkedByteBuffer;
import org.javastorm.util.MyByteBuffer;

public class TemplateManager extends DataManager
{
	public TemplateManager(String fileName)
	{
		this.fileName = fileName;
		this.templateVersion = 0;
		this.madeByDemo = false;
	}

	public void writeFile()
	{
		MyByteBuffer writeMe = this.join();
		// TODO: tie this in with NSFilesystem or something.

		try
		{
			FileOutputStream fout = new FileOutputStream(new File(this.fileName));

			for (int i = 0; i < writeMe.size(); i++)
				fout.write(writeMe.get());

			fout.close();
		}
		catch (Throwable t)
		{

		}
	}

	public boolean readFile(String fileName)
	{
		MarkedByteBuffer readMe = new MarkedByteBuffer();

		try
		{
			FileInputStream fin = new FileInputStream(new File(fileName));
			int fileSize = (int) fin.getChannel().size();
			readMe.allocate(fileSize);

			for (int i = 0; i < fileSize; i++)
			{
				readMe.put(fin.read());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (!this.validateData(readMe))
		{
			return false;
		}

		this.separate(readMe);

		return true;
	}

	public boolean validateData(MyByteBuffer data)
	{
		if (data.size() == 0)
			return false;

		data.setPosition(0);

		this.templateVersion = data.get();
		this.madeByDemo = data.get() == 254;

		boolean different = this.templateVersion != TEMPLATE_VERSION;

		return !different;
	}

	public String[] getSectionList()
	{
		if (this.sectionList == null)
		{
			String[] staticSections = new String[]
			{ "Subscriber", "State", "Mission", "CoreData", "Chaff", "Badges", "Territory", "TypeNames", "CompressedData", "Technology", "Money", "Deck", "Reserved2", "Reserved3", "Reserved4" };

			this.sectionList = new String[staticSections.length + Territory.MR_TERR_COUNT];
			int i;
			for (i = 0; i < staticSections.length; i++)
			{
				this.sectionList[i] = staticSections[i];
			}
			for (int j = 0; j < Territory.MR_TERR_COUNT; j++, i++)
			{
				this.sectionList[i] = "Terr" + j;
			}
		}

		return this.sectionList;
	}

	public void createValidation(MyByteBuffer data)
	{
		this.templateVersion = TEMPLATE_VERSION;
		data.put(this.templateVersion);

		data.put(this.madeByDemo ? 254 : 0);
	}

	private String[] sectionList;

	private String fileName;

	private int templateVersion;

	private boolean madeByDemo;

	public static final int TEMPLATE_VERSION = 70;
}
