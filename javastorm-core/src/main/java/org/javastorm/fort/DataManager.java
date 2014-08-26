package org.javastorm.fort;

import org.javastorm.util.MarkedByteBuffer;
import org.javastorm.util.MyByteBuffer;

public abstract class DataManager
{
	public DataManager()
	{
		this.current = null;
		this.root = null;
	}

	public void create()
	{
		String[] sectionList = this.getSectionList();

		for (String section : sectionList)
		{
			this.createSection(section);
		}
	}

	public Section createSection(String sectionName)
	{
		Section oldSection = this.findSection(sectionName);
		if (oldSection != null)
		{
			this.removeSection(oldSection);
		}

		this.addSection(new Section(sectionName));

		return this.current;
	}

	private void addSection(Section section)
	{
		if (this.root == null)
		{
			this.root = section;
		}
		else
		{
			Section temp = this.root;

			while (temp.getNext() != null)
			{
				temp = temp.getNext();
			}

			temp.setNext(section);
		}

		this.current = section;
	}

	private void removeSection(Section section)
	{
		if (this.root == section)
		{
			this.root = section.getNext();
		}
		else
		{
			Section temp = this.root;

			while (temp.getNext() != null && (temp.getNext() != section))
			{
				temp = temp.getNext();
			}

			temp.setNext(temp.getNext().getNext());
		}
	}

	public Section replaceSection(String replaceMe, String withMe)
	{
		Section oldSection = this.findSection(replaceMe);

		if (oldSection == null)
			this.removeSection(oldSection);

		Section newSection = this.findSection(withMe);
		newSection.setName(replaceMe);

		return this.current;
	}

	public Section copySection(DataManager srcData, String sectionName)
	{
		Section src = srcData.findSection(sectionName);
		Section dest = this.createSection(sectionName);

		dest.allocate(src.size());
		dest.put(src);
		dest.setPosition(0);

		return dest;
	}

	private Section findSection(String name)
	{
		this.current = this.root;

		while (this.current != null)
		{
			if (name.equalsIgnoreCase(this.current.getName()))
			{
				this.current.setPosition(0);
				return this.current;
			}

			this.current = this.current.getNext();
		}

		return null;
	}

	public Section getSection(String name, boolean mayBeEmpty)
	{
		Section s = this.findSection(name);

		if (!mayBeEmpty)
		{
			assert (s != null);
			assert (s.size() > 0);
		}

		s.setPosition(0);
		return s;
	}

	public void separate(MarkedByteBuffer stream)
	{
		String[] sectionList = this.getSectionList();

		assert (stream.position() == 2);

		stream.verifyMark();

		for (String section : sectionList)
		{
			int sectionLength = stream.getMark() - 2;
			Section s = new Section(section);

			s.allocate(sectionLength);
			s.put(stream, sectionLength);
			s.setPosition(0);

			Section killMe = this.findSection(section);
			if (killMe == null || s.size() != 0)
			{
				if (killMe != null)
				{
					this.removeSection(killMe);
				}
				this.addSection(s);
			}

			stream.verifyMark();
		}
	}

	public MyByteBuffer join()
	{
		String[] sectionList = this.getSectionList();

		Section s;

		// Calculate size.
		int size = 2;
		for (String section : sectionList)
		{
			s = this.findSection(section);
			size += s.size() + 2;
		}

		MarkedByteBuffer destStream = new MarkedByteBuffer();
		destStream.allocate(size);
		this.createValidation(destStream);

		for (String section : sectionList)
		{
			s = this.findSection(section);
			destStream.setMark();
			destStream.put(s, s.size());
		}
		destStream.endMark();
		destStream.truncate();
		destStream.setPosition(0);

		return destStream;
	}

	public abstract void createValidation(MyByteBuffer dest);

	public String[] getSectionList()
	{
		return null;
	}

	private Section root;

	private Section current;
}
