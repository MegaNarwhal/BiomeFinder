package us.blockbox.biomefinder;

import org.bukkit.World;
import org.bukkit.block.Biome;
import us.blockbox.biomefinder.api.CacheManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CacheManagerImpl implements CacheManager{
	private Map<World,Map<Biome,Set<Coord>>> biomeCache;
	private final Map<World,Map<Biome,Set<Coord>>> biomeCacheOriginal;

	CacheManagerImpl(Map<World,Map<Biome,Set<Coord>>> biomeCache){
		this.biomeCache = new HashMap<>(biomeCache);
		this.biomeCacheOriginal = Collections.unmodifiableMap(new HashMap<>(biomeCache));
	}

	@Override
	public void setCache(Map<World,Map<Biome,Set<Coord>>> cache){
		this.biomeCache = cache;
	}

	@Override
	public void setWorldCache(World world,Map<Biome,Set<Coord>> cache){
		biomeCache.put(world,cache);
	}

	@Override
	public Map<World,Map<Biome,Set<Coord>>> getCache(){
		return biomeCache;
	}

	@Override
	public Map<Biome,Set<Coord>> getCache(World world){
		return biomeCache.get(world);
	}

	@Override
	public Set<World> getCachedWorlds(){
		return biomeCache.keySet();
	}

	@Override
	public boolean hasCache(World world){
		return biomeCache.containsKey(world);
	}

	@Override
	@Deprecated
	public boolean isCacheUnchanged(World w){//todo test
		final Map<Biome,Set<Coord>> wCurrent = biomeCache.get(w);
		final Map<Biome,Set<Coord>> wOld = biomeCacheOriginal.get(w);
		return wCurrent.equals(wOld);
	}

	@Override
	public boolean isCacheUnchanged(){
		return biomeCache.equals(biomeCacheOriginal);
	}
}
