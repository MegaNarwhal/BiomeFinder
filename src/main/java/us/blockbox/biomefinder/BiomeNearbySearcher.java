package us.blockbox.biomefinder;

import org.bukkit.Location;
import org.bukkit.block.Biome;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class BiomeNearbySearcher{
	private final Location location;
	private final int radius = BiomeFinder.getPlugin().getBfConfig().getNearbyRadius();

	public BiomeNearbySearcher(Location location){
		this.location = location;
	}

	public Map<Biome,Coord> search(){
		final Map<Biome,Coord> nearby = new EnumMap<>(Biome.class);
		final Coord p = new Coord(location);
		final int radiusSquared = radius * radius;
		//final int pointDistSquared = BfConfig.getDistance() * BfConfig.getDistance();
		for(Map.Entry<Biome,Set<Coord>> cacheEntry : BiomeFinder.biomeCache.get(location.getWorld()).entrySet()){
			for(Coord coord : cacheEntry.getValue()){
				final int dist = coord.distanceSquared(p);
				if(dist > radiusSquared){
					continue;
				}
				final Biome key = cacheEntry.getKey();
				if(nearby.containsKey(key)){
					if(nearby.get(key).distanceSquared(p) > dist){
						nearby.put(key,coord);
					}
				}else{
					nearby.put(key,coord);
				}
/*				if(dist <= pointDistSquared){
					break;
				}*/
			}
		}
		return nearby;
	}
}
