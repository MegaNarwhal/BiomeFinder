package us.blockbox.biomefinder;

import org.bukkit.Location;
import org.bukkit.World;

//Created 11/6/2016 5:38 AM
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

	public double distance(Coord var1){
		double diffX = var1.x - this.x;
		double diffZ = var1.z - this.z;
		return Math.sqrt(diffX * diffX + diffZ * diffZ);
	}

	public double distanceSquared(Coord var1){
		double diffX = var1.x - this.x;
		double diffZ = var1.z - this.z;
		return diffX * diffX + diffZ * diffZ;
	}

	public int distanceSquaredInt(Coord var1){
		int diffX = var1.x - this.x;
		int diffZ = var1.z - this.z;
		return diffX * diffX + diffZ * diffZ;
	}

	public Location asLocation(World w){
		return new Location(w,x,1,z);
	}
}
