package org.javastorm.squids;

import org.javastorm.BoardCoord;
import org.javastorm.BoardCoordRect;
import org.javastorm.World;
import org.javastorm.battle.BattlePlayer;
import org.javastorm.network.commands.NSCommand;
import org.javastorm.network.commands.ZCompressedUpdateSquid;
import org.javastorm.renderer.Renderer;
import org.javastorm.shapes.ShapeFile.ArtData;
import org.javastorm.shapes.ShapeFile.VFXFrame;
import org.javastorm.types.Types;
import org.javastorm.types.Types.NSType;
import org.javastorm.types.Types.NSTypeFrame;
import org.javastorm.util.NSTrace;

public class MainSquid extends BaseSquid
{
	public MainSquid(World container, int sid)
	{
		super(container, sid);
		this.boundingBox = new BoardCoordRect();
	}

	public int getMainSquidFlags()
	{
		int mainFlags = 0;

		if (this.abstractFlag)
			mainFlags |= 1;
		if (this.overloadDrawFlag)
			mainFlags |= 1 << 1;
		if (this.selectedFlag)
			mainFlags |= 1 << 2;
		if (this.astralFlag)
			mainFlags |= 1 << 3;
		if (this.invisibleFlag)
			mainFlags |= 1 << 4;
		if (this.paralyzedFlag)
			mainFlags |= 1 << 5;
		if (this.notUsedAnymoreFlag)
			mainFlags |= 1 << 6;
		if (this.everPoppedIntoWorld)
			mainFlags |= 1 << 7;
		if (this.speedFlag)
			mainFlags |= 1 << 8;

		return mainFlags;
	}

	public void push(int popFlags)
	{
		super.push(popFlags);
		this._container.getSquidHash().remove(this);
	}

	public int calcHashLevel()
	{
		NSType ty = this.getTypeStruct();
		int hl = 0;
		if ((ty.getGenus() & NSType.gSTANDABLE) > 0)
		{
			hl = 0;
		}
		else
		{
			ArtData ad = ty.getArtDim(false, getFrame());
			if (ad != null)
			{
				float maxDim = Math.max(ad.getFootX(), ad.getFootY());
				if (maxDim <= 2.0f)
					hl = 1;
				else if (maxDim <= 4.0f)
					hl = 2;
				else
					hl = 3;
			}
		}
		setHashLevel(hl);
		return hl;
	}

	public void pop(int popFlags)
	{
		this.pop(this.pos, popFlags);
	}

	public void pop(BoardCoord pos, int popFlags)
	{
		this.pos = pos;

		if (!this.everPoppedIntoWorld)
		{
			popFlags |= BaseSquid.pfCREATED;
			this.everPoppedIntoWorld = true;
		}

		super.pop(popFlags);

		this.calcHashLevel();
		this._container.getSquidHash().put(this);
		this._container.dirtyMe(this);
	}

	public NSCommand transmitMyself()
	{
		ZCompressedUpdateSquid zcus = new ZCompressedUpdateSquid();
		zcus.setSid(this.getSid());
		zcus.setPos(this.getPos());
		zcus.setQ(this.getQ());
		zcus.setTypeNum(this.getType());
		zcus.setBaseFlags(this.getBaseFlags());

		if (this.getPlayer() != null)
			zcus.setPlayerId(this.getPlayer().getPlayerIndex());

		zcus.setHitPoints(this.getHitPoints());
		zcus.setZOrder(this.getZOrder());
		zcus.setFrame(this.getFrame());
		zcus.setMainSquidFlags(this.getMainSquidFlags());
		
		return zcus;
	}

	public void notifyCreate()
	{
		// Use default zordering.
		this.setZOrder(this.getTypeStruct().getZOrder());

		if (this.hasHitPoints())
			this.setHitPoints(this.getMaxHitPoints());
	}

	public void notifyTake()
	{
		this.setZOrder(this.getTypeStruct().getZOrder());

		if (this.hasHitPoints())
			this.setHitPoints(this.getMaxHitPoints());
	}

	public void draw(Renderer renderer, int x, int y)
	{
	}

	private void calculateBoundingBox()
	{
		// To make things easier for hashing, my head hurts.
		if (this.pos != null)
		{
			BoardCoord tl = new BoardCoord(pos);
			VFXFrame currentFrame = this.getCurrentFrame().getFrame();
			if (currentFrame != null)
			{
				tl.moveBy(-(currentFrame.getArtData().getFootX() + (this.getTypeStruct().getHotFootX() / Renderer.X_TILE_DIM)), -(currentFrame.getArtData().getFootY() + (this.getTypeStruct().getHotFootY() / Renderer.Y_TILE_DIM)));
				this.boundingBox.setTl(tl);
			}
		}
		else
			this.boundingBox.makeInvalid();
	}

	public NSTypeFrame getCurrentFrame()
	{
		return this.getTypeStruct().getFrameInfo(this.frame);
	}

	public int getFrame()
	{
		return frame;
	}

	public int getCluster()
	{
		return this.getCurrentFrame().getSideOrientation() - 'A';
	}

	public void setFrame(int frame)
	{
		this._container.dirtyMe(this);
		this.frame = frame;
		this.calculateBoundingBox();
	}

	public void updateFrameIncrementModCluster(int amount)
	{
		//int cluster = this.getCluster();
	}

	public NSType.TypeFlags getTypeFlags()
	{
		return this.getTypeStruct().getTypeFlags();
	}

	public boolean hasHitPoints()
	{
		return this.getTypeFlags().hasHitPoints();
	}

	public boolean isAbstract()
	{
		return abstractFlag;
	}

	public void setAbstract(boolean abstractFlag)
	{
		this.abstractFlag = abstractFlag;
	}

	public boolean isAstral()
	{
		return astralFlag;
	}

	public void setAstral(boolean astralFlag)
	{
		this.astralFlag = astralFlag;
	}

	public boolean isEverPoppedIntoWorld()
	{
		return everPoppedIntoWorld;
	}

	public void setEverPoppedIntoWorld(boolean everPoppedIntoWorld)
	{
		this.everPoppedIntoWorld = everPoppedIntoWorld;
	}

	public boolean isInvisible()
	{
		return invisibleFlag;
	}

	public void setInvisible(boolean invisibleFlag)
	{
		this.invisibleFlag = invisibleFlag;
	}

	public boolean isNotUsedAnymore()
	{
		return notUsedAnymoreFlag;
	}

	public void setNotUsedAnymore(boolean notUsedAnymoreFlag)
	{
		this.notUsedAnymoreFlag = notUsedAnymoreFlag;
	}

	public boolean isOverloadDraw()
	{
		return overloadDrawFlag;
	}

	public void setOverloadDraw(boolean overloadDrawFlag)
	{
		this.overloadDrawFlag = overloadDrawFlag;
	}

	public boolean isParalyzed()
	{
		return paralyzedFlag;
	}

	public void setParalyzed(boolean paralyzedFlag)
	{
		this.paralyzedFlag = paralyzedFlag;
	}

	public boolean isSelected()
	{
		return selectedFlag;
	}

	public void setSelected(boolean selectedFlag)
	{
		this.selectedFlag = selectedFlag;
	}

	public boolean isSpeed()
	{
		return speedFlag;
	}

	public void setSpeed(boolean speedFlag)
	{
		this.speedFlag = speedFlag;
	}

	public BattlePlayer getPlayer()
	{
		return player;
	}

	public void setPlayer(BattlePlayer player)
	{
		this.player = player;
	}

	public BoardCoordRect getBoundingBox()
	{
		return this.boundingBox;
	}

	public BoardCoordRect getRect()
	{
		return this.getTypeStruct().getRect(this.getPos());
	}

	public BoardCoord getCenter()
	{
		int footX, footY;
		footX = this.getTypeStruct().getFootpadX();
		footY = this.getTypeStruct().getFootpadY();
		BoardCoord pos = getPos();
		return new BoardCoord(pos.getX() - (float) footX / 2.0f, pos.getY() - (float) footY / 2.0f);
	}

	public boolean isSurface()
	{
		return this.getTypeFlags().isSurface();
	}

	public boolean isUnit()
	{
		return this.getTypeStruct().getGroup() != Types.NO_GROUP;
	}

	public boolean hasLevel()
	{
		return this.isUnit() || this.getTypeFlags().usesMana() || this.getTypeStruct().getTypeName().equals("altar");
	}

	public BoardCoord getPos()
	{
		return pos;
	}

	public void setPos(BoardCoord pos)
	{
		this.pos = new BoardCoord(pos);
		this.boundingBox.setBr(this.pos);
		this.calculateBoundingBox();
	}

	public int getHitPoints()
	{
		return hitPoints;
	}

	public void setHitPoints(int hitPoints)
	{
		this.hitPoints = hitPoints;
	}

	public int getMaxHitPoints()
	{
		return this.getTypeStruct().getMaxHitPoints();
	}

	public void notifyDestroy(int popFlags)
	{
		if ((popFlags & pfDIE_SALVAGE) == 0)
		{
			BattlePlayer killer = whoKilledToPlayerId(popFlags);
			this._container.reward(this, killer, false);
		}
	}

	private BattlePlayer whoKilledToPlayerId(int popFlags)
	{
		popFlags &= pfWHO_KILLED_MASK;
		int whoKilled = popFlags >> pfSERVER_DESTROY_SHIFT;

		if (whoKilled != 0)
		{
			return this._container.getPlayer(whoKilled + (World.FIRST_REAL_PLAYER - 1));
		}
		return null;
	}

	public void setGraphNum(int graphNum)
	{
		this.graphNum = graphNum;
	}

	public int getGraphNum()
	{
		return this.graphNum;
	}

	public int getHashLevel()
	{
		return this.hashLevel;
	}

	public void setHashLevel(int hashLevel)
	{
		this.hashLevel = hashLevel;
	}

	public void dumpMyself()
	{
		super.dumpMyself();

		String containedInfo = "";
		String hpInfo = "";
		String surfaceInfo = "";
		String frameInfo = "";
		String idInfo = "";

		if (isContained())
		{
			containedInfo = String.format("(parent=~y%04d~n prev=~y%04d~n) ", this.getParent() != null ? this.getParent().getSid() : 0, (this.getPrev() != null ? this.getPrev().getSid() : 0));
		}
		if (hasHitPoints())
		{
			hpInfo = String.format("HP=%d ", getHitPoints());
		}
		if (isSurface())
		{
			surfaceInfo = String.format("GrNum=%d ", getGraphNum());
		}
		if (!super._container.suppressExtraDumpInfo)
		{
			frameInfo = String.format("fram=%d ", getFrame());
		}

		if ((getGenus() & NSType.gISLAND) == 1)
		{
			idInfo = String.format("islandId=%d ", getIslandId());
		}

		super._container.Trace.Trace(NSTrace.TL_TRACE, "~1     Main( %2.5f %2.5f) hl=%d q=%d r=%d z=%d %splId=%d %s%s\n Main %s%s%s%s%s%s%s%s%sgenus=%X\n", getPos().getX(), getPos().getY(), getHashLevel(), getQ(), getR(), getZOrder(), frameInfo, getPlayer() != null ? getPlayer().getPlayerIndex() : 0, hpInfo, surfaceInfo,

		isAstral() ? "~rAstral~n " : "", isAbstract() ? "~rAbstract~n " : "", isOverloadDraw() ? "OvDraw " : "", isSelected() ? "Selected " : "", isInvisible() ? "Invisible" : "", isParalyzed() ? "Paralyzed" : "", everPoppedIntoWorld ? "InWorld " : "~r!InWorld~n", containedInfo, idInfo, getGenus());
	}

	public int getCreationFlags()
	{
		int popFlags = 0;

		if (isAbstract() || isAstral())
		{
			if (isAbstract())
			{
				popFlags |= BaseSquid.pfCREATED_ABSTRACT;
			}
			if (isAstral())
			{
				popFlags |= BaseSquid.pfCREATED_ASTRAL;
			}
		}
		else
		{
			popFlags = BaseSquid.pfCREATED;
		}

		return popFlags;
	}

	public int getZOrder()
	{
		return zOrder;
	}

	public void setZOrder(int order)
	{
		zOrder = order;
	}

	public int getPixelPosX()
	{
		return pixelPosX;
	}

	public int getPixelPosY()
	{
		return pixelPosY;
	}

	public MainSquid getNext()
	{
		return next;
	}

	public void setNext(MainSquid next)
	{
		this.next = next;
	}

	public MainSquid getPrev()
	{
		return prev;
	}

	public void setPrev(MainSquid prev)
	{
		this.prev = prev;
	}

	private int hashLevel;

	private int graphNum;

	private int pixelPosX, pixelPosY;

	private int zOrder;

	private BattlePlayer player;

	private int hitPoints;

	private BoardCoord pos;

	private BoardCoordRect boundingBox;

	private int frame;

	private boolean abstractFlag;

	private boolean overloadDrawFlag;

	// We might consider moving the overloaded draw flag out 
	// into the type data, since things are usually either
	// overloaded or not whenever they're drawn. -Ken
	private boolean selectedFlag;

	private boolean astralFlag;

	// This is used to indicate that the squid is
	// not searchable.  For example, the cursor.
	// It will not be hashed into the xy hash and thus 
	// not be drawn.  
	// However, these will still be transmitted to 
	// the clients on pop or destroy as usual.
	private boolean invisibleFlag;

	// Tells whether we should draw the thing invisible or not.
	private boolean paralyzedFlag;

	// Am I allowed to move at the moment?
	private boolean notUsedAnymoreFlag;

	// This used to be called connectedFlag.
	private boolean everPoppedIntoWorld;

	// Now that board coords don't set themselves in their
	// constructor, this tells whether a squid is brand
	// new or not.
	private boolean speedFlag;

	// For the Squid Hashing, we maintain a doubly linked list.
	private MainSquid next;

	private MainSquid prev;

	// =============================
	// ZORDER STUFF HERE
	// =============================
	public static final int zoNONE = (-127);

	public static final int zoFALLING = (30);

	public static final int zoSTALAG = (20);

	public static final int zoCHALRING = (20);

	public static final int zoBATTLE = (10);

	public static final int zoEDGEFARM = (0);

	public static final int zoISLAND = (0);

	public static final int zoBRIDGE = (0);//= (-10);	// Changed to fix bridge sorting problem for

	// The case that an island is to the left of a bridge.  Because the
	// strip sorter only has one zorder, this was causing problems
	// So I cahanged all bridge and island to 0 and brdige connectors to -10
	public static final int zoBRIDGE_CONNECTOR = (-10);

	public static final int zoARTIFACTS = (-15);

	public static final int zoEMPLACEMENTS = (-20);

	public static final int zoILLEGAL_DITHER = (-21);

	public static final int zoFLARES = (-22);

	public static final int zoMISSILES = (-25);

	public static final int zoFLYER_SHADOWS = (-26);

	public static final int zoFLYERS = (-30);

	public static final int zoRISING = (-31);

	public static final int zoFENCE = (-35);

	public static final int zoMANAICON = (-36);

	public static final int zoUBERGUMP = (-40);

	public static final int zoMENUGUMP = (-60);

	public static final int zoDIALOGGUMP = (-80);

	public static final int zoLOOKGUMP = (-100);

	public static class ZOrderString
	{
		public ZOrderString(String zOrder, int zOrderValue)
		{
			this.zOrder = zOrder;
			this.zOrderValue = zOrderValue;
		}

		public String zOrder;

		public int zOrderValue;
	}

	public static final ZOrderString zOrderStrings[] = new ZOrderString[]
	{ new ZOrderString("zoNONE", zoNONE), new ZOrderString("zoFALLING", zoFALLING), new ZOrderString("zoSTALAG", zoSTALAG), new ZOrderString("zoCHALRING", zoCHALRING), new ZOrderString("zoBATTLE", zoBATTLE), new ZOrderString("zoEDGEFARM", zoEDGEFARM), new ZOrderString("zoISLAND", zoISLAND), new ZOrderString("zoBRIDGE", zoBRIDGE), new ZOrderString("zoBRIDGE_CONNECTOR", zoBRIDGE_CONNECTOR), new ZOrderString("zoARTIFACTS", zoARTIFACTS), new ZOrderString("zoEMPLACEMENTS", zoEMPLACEMENTS), new ZOrderString("zoILLEGAL_DITHER", zoILLEGAL_DITHER), new ZOrderString("zoFLARES", zoFLARES), new ZOrderString("zoMISSILES", zoMISSILES), new ZOrderString("zoFLYER_SHADOWS", zoFLYER_SHADOWS), new ZOrderString("zoFLYERS", zoFLYERS), new ZOrderString("zoFENCE", zoFENCE), new ZOrderString("zoMANAICON", zoMANAICON), new ZOrderString("zoUBERGUMP", zoUBERGUMP), new ZOrderString("zoMENUGUMP", zoMENUGUMP), new ZOrderString("zoDIALOGGUMP", zoDIALOGGUMP), new ZOrderString("zoLOOKGUMP", zoLOOKGUMP), new ZOrderString("zoRISING", zoRISING) };

	public static final int getZOrderFromString(String zOrder)
	{
		for (int i = 0; i < zOrderStrings.length; i++)
		{
			if (zOrderStrings[i].zOrder.equalsIgnoreCase(zOrder))
				return zOrderStrings[i].zOrderValue;
		}

		return 0;
	}
}
