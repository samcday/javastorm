import java.io.File;
import java.io.FileWriter;

import org.javastorm.tarc.Tarc;

public class Main
{
	public static void main(String args[]) throws Exception
	{
		Tarc tarc = new Tarc();
		tarc.open(new File("C:\\Program Files (x86)\\NetstormLaunch\\package\\Netstorm.tarc"));

		String[] fileList = tarc.getFileList();
		for (int i = 0; i < tarc.getNumFiles(); i++)
		{
			System.out.println("Dumping " + fileList[i]);
			File file = new File("." + fileList[i]);
			new File(file.getPath().substring(0, file.getPath().lastIndexOf("\\"))).mkdirs();
			FileWriter writer = new FileWriter(file);
			writer.write(tarc.getFile(fileList[i]));
			writer.close();
		}
	}
}
