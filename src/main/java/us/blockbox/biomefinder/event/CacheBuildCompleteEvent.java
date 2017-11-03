package us.blockbox.biomefinder.event;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class CacheBuildCompleteEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
	private final World world;
	private final int centerX;
	private final int centerZ;
	private final long durationMillis;

	public CacheBuildCompleteEvent(World world,int centerX,int centerZ,long durationMillis){
		super();
		this.world = world;
		this.centerX = centerX;
		this.centerZ = centerZ;
		this.durationMillis = durationMillis;
	}

	@Override
	public HandlerList getHandlers(){
		return handlers;
	}

	public static HandlerList getHandlerList(){
		return handlers;
	}

	public World getWorld(){
		return world;
	}

	public int getCenterX(){
		return centerX;
	}

	public int getCenterZ(){
		return centerZ;
	}

	public long getDurationMillis(){
		return durationMillis;
	}
}
