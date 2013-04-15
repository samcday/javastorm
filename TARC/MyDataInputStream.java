import java.io.IOException;
import java.io.InputStream;

public class MyDataInputStream extends InputStream
{
	public MyDataInputStream(InputStream is)
	{
		this.is = is;
	}

	public int available() throws IOException
	{
		return this.is.available();
	}

	public int read() throws IOException
	{
		return this.is.read();
	}

	public void close() throws IOException
	{
		this.is.close();
	}

	public int readShort() throws IOException
	{
		int low = 0;
		int high = 0;

		low = this.is.read();
		high = this.is.read();

		return ((high & 0xFF) << 8) | (low & 0xFF);
	}

	public int readInt() throws IOException
	{
		int lowlow = 0;
		int lowhigh = 0;
		int highlow = 0;
		int highhigh = 0;

		lowlow = this.is.read();
		lowhigh = this.is.read();
		highlow = this.is.read();
		highhigh = this.is.read();

		return (((highhigh & 0xFF) << 24) | ((highlow & 0xFF) << 16) | ((lowhigh & 0xFF) << 8) | (lowlow & 0xFF));
	}

	public float readFloat() throws IOException
	{
		int lowlow = 0;
		int lowhigh = 0;
		int highlow = 0;
		int highhigh = 0;

		lowlow = this.is.read();
		lowhigh = this.is.read();
		highlow = this.is.read();
		highhigh = this.is.read();

		return Float.intBitsToFloat(((highhigh & 0xFF) << 24) | ((highlow & 0xFF) << 16) | ((lowhigh & 0xFF) << 8) | (lowlow & 0xFF));
	}

	public String readLine() throws IOException
	{
		String str = "";

		int tempChar;

		while ((tempChar = this.is.read()) != 0)
		{
			str = str.concat(Character.toString((char) tempChar));
		}

		return str;
	}

	private InputStream is;
}
