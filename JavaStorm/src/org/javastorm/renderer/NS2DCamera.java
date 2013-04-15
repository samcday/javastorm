package org.javastorm.renderer;

import org.javastorm.Netstorm;

public class NS2DCamera extends Camera
{
	public NS2DCamera(NS2DRenderer renderer)
	{
		this.renderer = renderer;
	}

	public void move(int bx, int by)
	{
		int oldX = this.getX();
		int oldY = this.getY();

		super.move(bx, by);

		this.renderer.scroll(this.getX() - oldX, this.getY() - oldY);
	}

	public void setX(int x)
	{
		// Make sure the Camera stays in bounds.
		int viewportWidth = this.renderer.getWidth() / 2;
		int mapW = Netstorm.BOARDDIM_IN_TILES * Renderer.X_TILE_DIM;
		if (x < (viewportWidth))
		{
			super.setX(viewportWidth);
			return;
		}
		if (x > (mapW - viewportWidth))
		{
			super.setX(mapW - viewportWidth);
			return;
		}
		super.setX(x);
	}

	public void setY(int y)
	{
		// Make sure the Camera stays in bounds.
		int viewportHeight = this.renderer.getHeight() / 2;
		int mapH = Netstorm.BOARDDIM_IN_TILES * Renderer.Y_TILE_DIM;

		if (y < (viewportHeight))
		{
			super.setY(viewportHeight);
			return;
		}
		if (y > (mapH - viewportHeight))
		{
			super.setY(mapH - viewportHeight);
			return;
		}
		super.setY(y);
	}

	private NS2DRenderer renderer;
}
