package org.javastorm.network.commands;

import org.javastorm.BoardCoord;
import org.javastorm.network.Connection;
import org.javastorm.types.Types;
import org.javastorm.types.Types.NSType;
import org.javastorm.util.MyByteBuffer;

public class ZCompressedUpdateSquid extends NSCommand
{
	public int getBaseFlags()
	{
		return baseFlags;
	}

	public void setBaseFlags(int baseFlags)
	{
		this.baseFlags = baseFlags;
	}

	public int getFrame()
	{
		return frame;
	}

	public void setFrame(int frame)
	{
		this.frame = frame;
	}

	public int getHitPoints()
	{
		return hitPoints;
	}

	public void setHitPoints(int hitPoints)
	{
		this.hitPoints = hitPoints;
	}

	public int getMainSquidFlags()
	{
		return mainSquidFlags;
	}

	public void setMainSquidFlags(int mainSquidFlags)
	{
		this.mainSquidFlags = mainSquidFlags;
	}

	public int getPlayerId()
	{
		return playerId;
	}

	public void setPlayerId(int playerId)
	{
		this.playerId = playerId;
	}

	public BoardCoord getPos()
	{
		return pos;
	}

	public void setPos(BoardCoord pos)
	{
		this.pos = pos;
	}

	public int getQ()
	{
		return q;
	}

	public void setQ(int q)
	{
		this.q = q;
	}

	public int getTypeNum()
	{
		return typeNum;
	}

	public void setTypeNum(int typeNum)
	{
		this.typeNum = typeNum;
	}

	public int getZOrder()
	{
		return zOrder;
	}

	public void setZOrder(int order)
	{
		zOrder = order;
	}

	public int getSid()
	{
		return sid;
	}

	public void setSid(int sid)
	{
		this.sid = sid;
	}

	public int getCommandID()
	{
		return Connection.ZCompressedUpdateSquid;
	}

	@Override
	public String[] outputDebug() {
		return new String[] {
			"mask = " + this.mask,
			"sid = " + this.sid,
			"pos = " + this.pos,
			"q = " + this.q,
			"typeNum = " + this.typeNum,
			"baseFlags = " + this.baseFlags,
			"playerId = " + this.playerId,
			"hitPoints = " + this.hitPoints,
			"zOrder = " + this.zOrder,
			"frame = " + this.frame,
			"mainSquidFlags = " + this.mainSquidFlags,
		};
	}

	public MyByteBuffer getCommandData()
	{

		MyByteBuffer buffer = new MyByteBuffer();

		MyByteBuffer data = new MyByteBuffer();
		data.allocate(18);

		this.mask = 0;
		this.dataLength = 0;

		this.decoding = false;
		this.typeStruct = Types.findByTypeNum(this.typeNum);

		for (int i = 0; i < 16; i++)
			this.handle(i, data);

		int packetSize = this.dataLength;

		// 10.80 upped the size of sid to an int.
		if (this._getMinorVersion() == 78)
		{
			packetSize += 5;
		}
		else
		{
			packetSize += 7;
		}

		buffer.allocate(packetSize);

		if (this._getMinorVersion() == 78)
			buffer.putShort(this.sid);
		else
			buffer.putInt(this.sid);

		buffer.putShort(this.mask);
		buffer.put(this.dataLength);

		data.setPosition(0);
		buffer.put(data, this.dataLength);

		return buffer;
	}

	public int getVersion()
	{
		return 1;
	}

	public void readCommandData(MyByteBuffer buffer)
	{
		this.sid = buffer.getShort();
		this.mask = buffer.getShort();

		buffer.skip(1);

		this.decoding = true;

		for (int i = 0; i < 16; i++)
			this.handle(i, buffer);

		this.typeStruct = Types.findByTypeNum(this.typeNum);
	}

	// This compression method is pretty clever.
	/*private void encode(int index, MyByteBuffer data)
	{
		
	}
	
	private void decode(int index, MyByteBuffer data)
	{
		
	}*/

	private void handle(int index, MyByteBuffer data)
	{
		if (this.decoding)
		{
			if (!((this.mask & (1 << index)) != 0))
				return;
		}

		switch (index)
		{
			case MASK_POS_BYTE:
			{
				if (this.decoding)
				{
					this.pos = new BoardCoord(data.get(), data.get());
				}
				else
				{
					if (this.canBeByte(this.pos.getX()) && this.canBeByte(this.pos.getY()))
					{
						data.put((int) this.pos.getX());
						data.put((int) this.pos.getY());
						this.dataLength += 2;

						this.addToMask(MASK_POS_BYTE);
					}
				}

				break;
			}
			case MASK_POS_FLOAT:
			{
				if (this.decoding)
				{
					this.pos = new BoardCoord(data.getFloat(), data.getFloat());
				}
				else
				{
					if ((this.mask & (1 << (index - 1))) == 0)
					{
						data.putFloat(this.pos.getX());
						data.putFloat(this.pos.getY());
						this.dataLength += 8;

						this.addToMask(MASK_POS_FLOAT);
					}
				}
				break;
			}
			case MASK_Q_BYTE:
			{
				if (this.decoding)
				{
					this.q = data.get();
				}
				else
				{
					if (this.canBeByte(this.q))
					{
						data.put(this.q);
						this.dataLength++;
						this.addToMask(MASK_Q_BYTE);
					}
				}
				break;
			}
			case MASK_Q_SHORT:
			{
				if (this.decoding)
				{
					this.q = data.getShort();
				}
				else
				{
					if ((this.mask & (1 << (index - 1))) == 0)
					{
						data.putShort(this.q);
						this.dataLength += 2;
						this.addToMask(MASK_Q_SHORT);
					}
				}
				break;
			}
			case MASK_TYPENUM:
			{
				if (this.decoding)
				{
					this.typeNum = data.get();

					if (this.typeStruct.getTypeFlags().hasHitPoints())
					{
						this.hitPoints = this.typeStruct.getMaxHitPoints();
					}
					this.zOrder = this.typeStruct.getDefaultDepth();
				}
				else
				{
					if (this.typeNum != Types.findByTypeName("bridge").getTypeNum())
					{
						data.put(this.typeNum);
						this.dataLength++;
						this.addToMask(MASK_TYPENUM);
					}
				}
				break;
			}
			case MASK_BSF:
			{
				if (this.decoding)
				{
					this.baseFlags = data.get();
				}
				else
				{
					// If there's more than just the xmitjustcreated flag set, we insert the whole flags byte.
					if ((this.baseFlags & ~0x10) > 0)
					{
						data.put(this.baseFlags);
						this.dataLength++;
						this.addToMask(MASK_BSF);
					}
				}
				break;
			}
			case MASK_BSF_XMIT:
			{
				if (this.decoding)
				{
					this.baseFlags |= 0x10;
				}
				else
				{
					if ((this.baseFlags & 0x10) > 0)
					{
						this.addToMask(MASK_BSF_XMIT);
					}
				}
				break;
			}
			case MASK_PID_LAST:
			{
				// Not used in current release.
				break;
			}
			case MASK_PID:
			{
				if (this.decoding)
				{
					this.playerId = data.get();
				}
				else
				{
					data.put(this.playerId);
					this.dataLength++;
					this.addToMask(MASK_PID);
				}
				break;
			}
			case MASK_HP_BYTE:
			{
				if (this.decoding)
				{
					this.hitPoints = data.get();
				}
				else
				{
					if (this.typeStruct.getTypeFlags().hasHitPoints() && this.typeStruct.getMaxHitPoints() != this.hitPoints)
					{
						if (this.canBeByte(this.hitPoints))
						{
							data.put(this.hitPoints);
							this.dataLength++;
							this.addToMask(MASK_HP_BYTE);
						}
					}
				}
				break;
			}
			case MASK_HP_SHORT:
			{
				if (this.decoding)
				{
					this.hitPoints = data.getShort();
				}
				else
				{
					if (this.typeStruct.getTypeFlags().hasHitPoints() && this.typeStruct.getMaxHitPoints() != this.hitPoints)
					{
						if (!this.canBeByte(this.hitPoints))
						{
							data.putShort(this.hitPoints);
							this.dataLength += 2;
							this.addToMask(MASK_HP_SHORT);
						}
					}
				}
				break;
			}
			case MASK_ZORDER:
			{
				if (this.decoding)
				{
					this.zOrder = data.get();
				}
				else
				{
					if (this.zOrder != this.typeStruct.getDefaultDepth())
					{
						data.put(this.zOrder);
						this.dataLength++;
						this.addToMask(MASK_ZORDER);
					}
				}
				break;
			}
			case MASK_FRAME:
			{
				if (this.decoding)
				{
					this.frame = data.get();
				}
				else
				{
					if (this.frame != 0)
					{
						data.put(this.frame);
						this.dataLength++;
						this.addToMask(MASK_FRAME);
					}
				}
				break;
			}
			case MASK_MSF:
			{
				if (this.decoding)
				{
					this.mainSquidFlags = data.get();
				}
				else
				{
					data.put(this.mainSquidFlags);
					this.dataLength++;
					this.addToMask(MASK_MSF);
				}
				break;
			}
		}
	}

	private void addToMask(int index)
	{
		this.mask |= 1 << index;
	}

	private boolean canBeByte(float x)
	{
		return Math.floor(x) == x;
	}

	private boolean canBeByte(int x)
	{
		if ((x & 0xFF) == x)
			return true;

		return false;
	}

	private NSType typeStruct;

	private boolean decoding;

	private int mask;

	private int dataLength;

	private BoardCoord pos;

	private int q;

	private int sid;

	private int typeNum;

	private int baseFlags;

	private int playerId;

	private int hitPoints;

	private int zOrder;

	private int frame;

	private int mainSquidFlags;

	public static final int MASK_POS_BYTE = 0;

	public static final int MASK_POS_FLOAT = 1;

	public static final int MASK_Q_BYTE = 2;

	public static final int MASK_Q_SHORT = 3;

	public static final int MASK_TYPENUM = 4;

	public static final int MASK_BSF = 5;

	public static final int MASK_BSF_XMIT = 6;

	public static final int MASK_PID_LAST = 7;

	public static final int MASK_PID = 8;

	public static final int MASK_HP_BYTE = 9;

	public static final int MASK_HP_SHORT = 10;

	public static final int MASK_ZORDER = 11;

	public static final int MASK_FRAME = 12;

	public static final int MASK_MSF = 13;
}
