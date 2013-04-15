package org.javastorm.renderer;

import java.awt.Panel;

import org.javastorm.BoardCoord;
import org.javastorm.Filesystem;
import org.javastorm.Rect;
import org.javastorm.ScreenCoord;
import org.javastorm.World;
import org.javastorm.squids.MainSquid;

public abstract class Renderer extends Panel
{
	private static final long serialVersionUID = 1L;

	public abstract boolean init(Filesystem fs, World world);

	public abstract void frame();

	public abstract Camera getCamera();

	public abstract void redrawArea(Rect rect);

	public abstract void redrawAll();

	public abstract void draw(MainSquid s, int x, int y, int frameNum);

	public abstract void setMasterOpacity(int alpha); // Forces opacity for all units.

	public abstract BoardCoord convertScreenCoord(ScreenCoord sc);

	public static final int X_TILE_DIM = (16);

	public static final int Y_TILE_DIM = (11);
}
