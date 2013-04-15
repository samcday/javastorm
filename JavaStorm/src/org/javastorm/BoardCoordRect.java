package org.javastorm;

public class BoardCoordRect
{
	public BoardCoordRect()
	{
		this.tl = new BoardCoord();
		this.br = new BoardCoord();
		this.tl.makeInvalid();
		this.br.makeInvalid();
	}

	public BoardCoordRect(BoardCoord tl, BoardCoord br)
	{
		this.tl = new BoardCoord(tl);
		this.br = new BoardCoord(br);
	}

	public BoardCoordRect(BoardCoordRect rect)
	{
		this.tl = new BoardCoord(rect.getTl());
		this.br = new BoardCoord(rect.getBr());
	}

	public void round()
	{
		this.tl.roundDown();
		this.br.roundUp();
	}

	public boolean contains(BoardCoord bc)
	{
		if (bc.getX() >= tl.getX() && bc.getX() <= br.getX() && bc.getY() >= tl.getY() && bc.getY() <= br.getY())
			return true;

		return false;
	}

	public int area()
	{
		if (!this.isValid())
			return 0;

		return (int) ((br.getX() - tl.getX()) * (br.getY() - tl.getY()));
	}

	public void extend(float tlX, float tlY, float brX, float brY)
	{
		tl.moveBy(tlX, tlY);
		br.moveBy(brX, brY);
	}

	// This is a little obscure
	// I'm looking for whether the rects are next to each other horizontally
	// or vertically, but NOT diagonally. i.e. can a bridge piece in one connect
	// connect to the other. To calculate this, I extend one of them vertically
	// and horizontally. If one of these extensions causes an intersection but
	// the other doesn't, then its ok. (Diagonal connection causes two intersections.)

	public boolean connectsOrthagonally(BoardCoordRect bcr)
	{
		BoardCoordRect wideBcr;
		wideBcr = new BoardCoordRect(bcr);
		BoardCoordRect tallBcr;
		tallBcr = new BoardCoordRect(bcr);

		wideBcr.getTl().moveBy(-1.0f, 0);
		wideBcr.getBr().moveBy(1.0f, 0);
		tallBcr.getTl().moveBy(0, -1.0f);
		tallBcr.getBr().moveBy(0, +1.0f);

		int intersections = 0;
		if (this.intersection(wideBcr).isValid())
			intersections++;
		if (this.intersection(tallBcr).isValid())
			intersections++;

		return intersections == 1;
	}

	public BoardCoordRect intersection(BoardCoordRect bcr)
	{
		BoardCoord ntl = new BoardCoord(), nbr = new BoardCoord();

		ntl.setX(Math.max(tl.getX(), bcr.getTl().getX()));
		ntl.setY(Math.max(tl.getY(), bcr.getTl().getY()));
		nbr.setX(Math.min(br.getX(), bcr.getBr().getX()));
		nbr.setY(Math.min(br.getY(), bcr.getBr().getY()));

		if (ntl.getX() <= nbr.getX() && ntl.getY() <= nbr.getY())
			return new BoardCoordRect(ntl, nbr);
		else
			return new BoardCoordRect();
	}

	public boolean intersects(BoardCoordRect other)
	{
		BoardCoord tl = this.getTl();
		BoardCoord br = this.getBr();
		BoardCoord otherTl = other.getTl();
		BoardCoord otherBr = other.getBr();

		return !((tl.getX() > otherBr.getX()) || (br.getX() < otherTl.getX()) || (tl.getY() > otherBr.getY()) || (br.getY() < otherTl.getY()));
	}

	public boolean intersects(BoardCoord other)
	{
		return (other.getX() >= this.tl.getX() && other.getX() <= this.br.getX() && other.getY() >= this.tl.getY() && other.getY() <= this.br.getY());
	}

	public void makeInvalid()
	{
		this.tl.makeInvalid();
		this.br.makeInvalid();
	}

	public boolean isValid()
	{
		return this.tl.isValid() && this.br.isValid();
	}

	public void setBr(BoardCoord br)
	{
		this.br = br;
	}

	public BoardCoord getBr()
	{
		return this.br;
	}

	public void setTl(BoardCoord tl)
	{
		this.tl = tl;
	}

	public BoardCoord getTl()
	{
		return this.tl;
	}

	private BoardCoord tl;

	private BoardCoord br;
}
