package org.javastorm.graph;

import org.javastorm.World;
import org.javastorm.squids.ConnectionFinder;
import org.javastorm.squids.LinearSquidFinder;
import org.javastorm.squids.MainSquid;
import org.javastorm.types.Types.NSType;

public class GraphManager
{
	public GraphManager(World world)
	{
		this.world = world;
	}

	public void initGraphs()
	{
		this.graphs = new Graph[INVALID_GRAPH];

		for (int i = 0; i < this.graphs.length; i++)
		{
			this.graphs[i] = new Graph(i);
		}

		this.resetGraphs();
	}

	public void resetGraphs()
	{
		for (int i = 0; i < this.graphs.length; i++)
		{
			this.graphs[i].init();
		}

		this.graphs[INVALID_GRAPH].setNumSurfaces(0x7DFD);
	}

	private void graphValidate()
	{
		assert (this.graphs[INVALID_GRAPH].getNumSurfaces() == 0x7DFD);
	}

	public Graph newGraph()
	{
		for (int i = 0; i < MAX_GRAPHS; i++)
		{
			if (!graphs[i].isInUse())
			{
				graphs[i].init();
				graphs[i].setInUse(true);
				return graphs[i];
			}
		}

		this.linearGraphFill(true);

		return this.newGraph();
	}

	// this gets graphs in their initial correct state once the island has
	// been built
	public void linearGraphFill()
	{
		this.linearGraphFill(false);
	}

	public void linearGraphFill(boolean garbageCollect)
	{
		this.graphValidate();

		if (garbageCollect)
			this.resetGraphs();

		Graph g = this.newGraph();
		int gi = g.getIndex();

		// step one - put all surfaces (that aren't underneath something) onto
		// an initial graph
		{
			LinearSquidFinder finder = new LinearSquidFinder(this.world);

			while (finder.isValid())
			{
				if (!(finder.s() instanceof MainSquid))
					continue;

				MainSquid s = (MainSquid) finder.s();

				if (s.isSurface())
				{
					if (garbageCollect || s.getGraphNum() == INVALID_GRAPH)
					{
						int spotFlags = this.world.spotFlags().getSpotFlags(s.getPos());

						if ((spotFlags & NSType.gWALK_BLOCKING) == 0)
						{
							s.setGraphNum(gi);
							g.incNumSurfaces();
						}
					}
				}

				finder.findNext();
			}
		}

		// step two - flood all surfaces that are still on the initial graph
		{
			LinearSquidFinder finder = new LinearSquidFinder(this.world);

			while (finder.isValid())
			{
				if (!(finder.s() instanceof MainSquid))
					continue;
				MainSquid s = (MainSquid) finder.s();

				if (s.isSurface() && s.getGraphNum() == gi)
				{
					Graph ng = this.newGraph();
					this.floodGraph(s, ng.getIndex());
				}
				finder.findNext();
			}
		}

		// step three, clear out the initial (scratch) graph
		g.init();
		//g.scanWorldToAssertNotInUse();

		this.graphValidate();
	}

	// breadth first flood fill of surfaces, setting to 
	// a new graph number
	private int floodGraph(MainSquid squid, int newGraphNum)
	{
		if (this.stack == null)
			this.stack = new MainSquid[FLOOD_STACK_SIZE];

		int d = 0; // d points to the next *available* slot
		int numFlooded = 0;
		this.stack[d++] = squid;

		while (d > 0)
		{
			MainSquid s = this.stack[d--];

			int oldGraphNumber = s.getGraphNum();

			if (oldGraphNumber == newGraphNum)
				continue;

			Graph oldG = null;
			if (oldGraphNumber != INVALID_GRAPH)
				oldG = this.graphs[oldGraphNumber];

			Graph newG = null;
			if (newGraphNum != INVALID_GRAPH)
			{
				newG = this.graphs[newGraphNum];
			}

			numFlooded++;
			if (oldG != null)
				oldG.decNumSurfaces();
			if (newG != null)
				newG.incNumSurfaces();

			s.setGraphNum(newGraphNum);

			ConnectionFinder cFinder = new ConnectionFinder(this.world, s, ConnectionFinder.CF_OPERATE_ON_SID_GRID);
			while (cFinder.isValid())
			{
				if (cFinder.s().getGraphNum() != newGraphNum)
				{
					stack[d++] = cFinder.s();
					assert (d < FLOOD_STACK_SIZE);
				}
				cFinder.findNext();
			}
		}

		this.graphValidate();
		return numFlooded;
	}

	private MainSquid stack[];

	private World world;

	private Graph[] graphs;

	public static final int MAX_GRAPHS = 251;

	public static final int INVALID_GRAPH = MAX_GRAPHS + 3;

	public static final int FLOOD_STACK_SIZE = 4096;
}
