package us.blockbox.biomefinder;

import org.bukkit.Location;
import org.bukkit.World;

public class Coord{
	public final int x;
	public final int z;

	public Coord(int x,int z){
		this.x = x;
		this.z = z;
	}

	public Coord(Location location){
		this.x = location.getBlockX();
		this.z = location.getBlockZ();
	}

	public double distance(Coord coord){
		return Math.sqrt(distanceSquared(coord));
	}

	public int distanceSquared(Coord coord){
		int diffX = coord.x - this.x;
		int diffZ = coord.z - this.z;
		return diffX * diffX + diffZ * diffZ;
	}

	public Location asLocation(World w){
		return new Location(w,x,1,z);
	}

	@Override
	public boolean equals(Object o){
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		Coord coord = (Coord)o;

		return x == coord.x && z == coord.z;
	}

	@Override
	public int hashCode(){
		int result = x;
		result = 31 * result + z;
		return result;
	}
}
