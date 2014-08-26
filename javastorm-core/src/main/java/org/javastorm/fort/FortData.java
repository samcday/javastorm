package org.javastorm.fort;

import org.javastorm.BoardCoord;
import org.javastorm.PlayerCoreData;
import org.javastorm.World;
import org.javastorm.Netstorm;
import org.javastorm.battle.BattlePlayer;
import org.javastorm.squids.MainSquid;
import org.javastorm.squids.SquidFinder;
import org.javastorm.territory.Chunk;
import org.javastorm.territory.ChunkCoord;
import org.javastorm.territory.ChunkMap;
import org.javastorm.territory.IslandList;
import org.javastorm.territory.TerritoryList;
import org.javastorm.types.Types;
import org.javastorm.types.Types.NSType;
import org.javastorm.types.Types.NSTypeFrame;
import org.javastorm.util.MarkedByteBuffer;
import org.javastorm.util.MyByteBuffer;

public class FortData
{
	public FortData(BattlePlayer player)
	{
		this.player = player;
	}

	public void create(String fileName, boolean neverWrite)
	{
		this.neverWrite = neverWrite;

		this.tm = new TemplateManager(fileName);
		this.tm.create();
	}

	public void open(String fileName)
	{
		this.myFileName = fileName;

		this.tm = new TemplateManager(this.myFileName);
		this.neverWrite = false;

		if (!this.tm.readFile(fileName))
		{
			System.out.println("Warning: couldn't find " + fileName);
			this.tm = null;
		}
	}

	public void receiveStrippedImage(MyByteBuffer stream)
	{
		assert(this.tm != null);

		int length = stream.size();
		stream.setPosition(0);

		MarkedByteBuffer src = new MarkedByteBuffer();
		src.allocate(length);
		src.put(stream);
		src.setPosition(0);

		boolean valid = this.tm.validateData(src);
		assert valid;

		this.tm.separate(src);
		// This will NON-DESTRUCTIVELY separate out the data and replace
		// ONLY the sections that are present (non-empty) in the stream.
		
		this.write();
	}

	public MyByteBuffer createStrippedImage()
	{
		TemplateManager dupe = new TemplateManager("NONAME");

		dupe.create();
		dupe.copySection(this.tm, "Subscriber");
		dupe.copySection(this.tm, "Territory");
		dupe.copySection(this.tm, "TypeNames");
		dupe.copySection(this.tm, "CoreData");
		dupe.copySection(this.tm, "Terr0");

		return dupe.join();
	}

	private void updateSubscriberSection(String nickname, int subId)
	{
		MyByteBuffer data = this.tm.createSection("Subscriber");
		data.allocate(4 + 2 + nickname.length() + 1);
		data.putInt(subId);
		data.putShort(nickname.length());
		data.putStr(nickname);
		data.put(0);
	}

	public String interpretNickname()
	{
		MyByteBuffer data = this.tm.getSection("Subscriber", false);
		data.getInt();
		int nicknameLength = data.getShort();
		return data.getString(nicknameLength);
	}

	public void updateNickname(String nickname)
	{
		int subId = this.interpretSubscriberID();

		this.updateSubscriberSection(nickname, subId);
	}

	public int interpretSubscriberID()
	{
		MyByteBuffer data = this.tm.getSection("Subscriber", false);
		return data.getInt();
	}

	public void updateSubscriberID(int subId)
	{
		String nickname = this.interpretNickname();

		this.updateSubscriberSection(nickname, subId);
	}

	public PlayerCoreData interpretCoreData()
	{
		MyByteBuffer data = this.tm.getSection("CoreData", false);
		PlayerCoreData coreData = new PlayerCoreData();
		coreData.read(data);

		return coreData;
	}

	public void updateCoreData(PlayerCoreData cd)
	{
		MyByteBuffer data = this.tm.getSection("CoreData", false);
		cd.write(data);
	}

	public TerritoryList interpretTerritory()
	{
		MyByteBuffer data = this.tm.getSection("Territory", false);
		TerritoryList terrList = new TerritoryList();

		terrList.read(data);

		return terrList;
	}

	public void updateTerritory(TerritoryList terrList)
	{
		MyByteBuffer data = this.tm.getSection("Territory", false);
		terrList.write(data);
	}

	public void interpretSquids(World world, int start, int end)
	{
		this.interpretTypeNames();

		for (int i = start; i < end; i++)
		{
			MyByteBuffer data = this.tm.getSection("Terr" + i, true);

			if (data.size() != 0)
			{
				this.squidVersion = data.get();
				this.interpretTerr(world, data, this.player, i);
			}
		}
	}

	private void interpretTerr(World world, MyByteBuffer data, BattlePlayer player, int terrID)
	{
		ChunkCoord cc = new ChunkCoord();
		int ix, iy;
		ChunkMap chunkMap = world.getChunkMap();
		IslandList islandList = world.getIslandList();

		for (iy = 0; iy < chunkMap.getYLen(); iy++)
		{
			for (ix = 0; ix < chunkMap.getXLen(); ix++)
			{
				if(data.available() == 0) return;

				cc.setX(ix);
				Chunk chunk = chunkMap.getChunk(cc);
				if (islandList.fullyOwned(chunk.getIslandId(), player, terrID))
				{
					this.interpretChunk(world, data, cc, player);
				}
			}
			cc.setY(iy);
		}
	}

	private void interpretChunk(World world, MyByteBuffer data, ChunkCoord cc, BattlePlayer player)
	{
		IntCoord topLeft = new IntCoord(cc.getBoardCoord());

		if (data.get() != 99)
		{
			return;
		}

		int thingsToRead = data.getShort();

		for (int i = 0; i < thingsToRead; i++)
		{
			this.readSquid(world, data, topLeft, player);
		}
	}

	private void readSquid(World world, MyByteBuffer data, IntCoord root, BattlePlayer player)
	{
		int crd = data.get();
		int x = crd >> 4;
		int y = crd & 0x0F;
		int typeNum = data.get();
		
		BattlePlayer squidPlayer = player;

		System.out.println("Type: " + Types.findByTypeNum(typeNum).getName());

		// Wtfff.
		if (typeNum >= 255 - Netstorm.MAX_REAL_PLAYERS_PER_GAME)
		{
			squidPlayer = world.getPlayer(255 - typeNum);
			typeNum = Types.findByName("bridge").getTypeNum();
		}
		else
		{
			// TODO
			// typeNum = convertType( typeNum, 0 );
		}

		if (typeNum == 0)
		{
			data.skip(1);
			return;
		}

		if (typeNum == Types.findByTypeName("island").getTypeNum())
		{
			data.skip(1);
			return;
		}

		BoardCoord bc = new BoardCoord(root.getX() + x, root.getY() + y);

		MainSquid s = (MainSquid) world.createSquid(typeNum, World.alDEFAULT);
		if (s.getTypeFlags().saveFrame())
		{
			System.out.println("Frame.");
			s.setFrame(data.get());
		}

		if (s.getTypeFlags().saveQ8())
		{
			System.out.println("Q8.");
			s.setQ(data.get() | (s.getQ() & 0xFF00));
		}

		if (s.getTypeFlags().saveQ16())
		{
			System.out.println("Q16.");
			s.setQ(data.getShort());
		}

		if ((s.getGenus() & NSType.gBRIDGE) > 0)
		{
			System.out.println("Frame.");
			s.setFrame(data.get());
		}

		if (this.squidVersion == 0)
		{
			if (s.hasLevel())
			{
				System.out.println("Level.");
				s.setLevel(data.get());
			}
		}

		if (this.squidVersion >= 2)
		{
			if ((s.getGenus() & NSType.gFACTORY) > 0)
			{
				System.out.println("Level.");
				s.setLevel(data.get());
			}
		}

		if (this.squidVersion <= 0)
		{
			squidPlayer = player;
		}
		
		else if((s.getGenus() & NSType.gHAS_PLAYER_ID) > 0)
		{
			System.out.println("Player Id.");
			int playerID = data.get();
			squidPlayer = world.getPlayer(playerID);

			if(squidPlayer == null)
			{
				squidPlayer = world.getPlayer(Netstorm.FIRST_REAL_PLAYER);
			}
		}
		// TODO:
			/*if( !inEditMode && !inMission ) { // were in multiplayer
		 * and we have new rules!
		 * if(globalBattleOptions.simpleOptions[BO_MAPMODE]!=2) { squidPlayerId
		 * = playerId; } } }
		 */
		if(world.getBattleOptions().getMapMode() != 2)
		{
			squidPlayer = player;
		}

		if ((s.getGenus() & NSType.gALWAYS_INVALID_PLAYER_ID) > 0)
		{
			squidPlayer = null;
		}

		if (s.getTypeStruct().getTypeName().equals("islandStalag"))
		{
			assert(false);
		}

		s.setPlayer(squidPlayer);

		if (s.hasHitPoints())
		{
			s.setHitPoints(s.getMaxHitPoints());
		}

		if (s.getTypeFlags().matchFrame())
		{
			int islandSid = SquidFinder.findSquidGenus(world, bc, NSType.gISLAND);
			if (islandSid > 0)
			{
				MainSquid theIsle = (MainSquid) world.getSquid(islandSid);
				int frame = theIsle.getFrame();
				s.setFrame(frame == NSTypeFrame.EMPTY_FRAME ? 0 : frame);
			}
		}

		if (s.getTypeFlags().randFrame())
		{
			int frame = s.getTypeStruct().getAnyBestFrame('P', 'P');
			s.setFrame(frame);
		}

		s.pop(bc, MainSquid.pfFROM_FORT_DATA);

		if (s.getTypeFlags().container())
		{
			System.out.println("Container.");
			this.readContents(s.getSid(), data, MainSquid.pfFROM_FORT_DATA, false);
		}

		// TODO:
		/*
		 * if( inFortMode && !fullDeck ) { if( !(s.getGenus()&gLEGAL_IN_FORT) ||
		 * (!inEditMode && !inMission && (s.getGenus()&gDAIS)) &&
		 * s.getLevel()==1 ) { s.destroy(); } }
		 */
	}

	private void readContents(int containerSid, MyByteBuffer data, int popFlags, boolean mayNotExist)
	{
		int totalContents = data.get();

		// TODO		
		assert(totalContents == 0);
	}

	private void interpretTypeNames()
	{
		// TODO:
		MyByteBuffer data = this.tm.getSection("TypeNames", true);

		if (data.size() == 0)
		{
			return;
		}

		/*int typeCount =*/ data.get();
	}

	public MyByteBuffer copyTo()
	{
		return this.tm.join();
	}

	public void write()
	{
		if (!this.readOnly)
			if (!this.neverWrite)
				this.tm.writeFile();
	}

	private int squidVersion;
	private BattlePlayer player;
	private TemplateManager tm;
	private boolean readOnly;
	private boolean neverWrite;
	private String myFileName;
}
