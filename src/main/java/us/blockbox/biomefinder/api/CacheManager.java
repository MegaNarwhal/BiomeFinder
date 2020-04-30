package us.blockbox.biomefinder.api;

import org.bukkit.World;
import org.bukkit.block.Biome;
import us.blockbox.biomefinder.Coord;

import java.util.Map;
import java.util.Set;

public interface CacheManager{
	void setCache(Map<World,Map<Biome,Set<Coord>>> cache);

	void setWorldCache(World world,Map<Biome,Set<Coord>> cache);

	Map<World,Map<Biome,Set<Coord>>> getCache();

	Map<Biome,Set<Coord>> getCache(World world);

	Set<World> getCachedWorlds();

	boolean hasCache(World world);

	/**
	 * @param w The World whose cache to check for changes
	 * @return True if the cache has not changed since it was loaded
	 * @deprecated This method hasn't been tested yet.
	 */
	@Deprecated
	boolean isCacheUnchanged(World w);

	/**
	 * @return True if the cache has not changed since it was loaded
	 */
	boolean isCacheUnchanged();
}
