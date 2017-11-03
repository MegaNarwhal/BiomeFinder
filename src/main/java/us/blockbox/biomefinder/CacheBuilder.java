package us.blockbox.biomefinder;

import com.google.common.collect.Maps;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.blockbox.biomefinder.event.CacheBuildCompleteEvent;
import us.blockbox.biomefinder.event.CacheBuildStartEvent;

import java.util.*;
import java.util.logging.Logger;

public class CacheBuilder{
	private static final Random RANDOM = new Random();
	private static boolean buildRunning;
	private final Plugin plugin;
	private final BfConfig bfc;
	private final CacheManager cacheManager;
	private final World world;
	private final int centerX;
	private final int centerZ;
	private final int pointNumber;
	private final int pointDistance;
	private final int pointsPerRow;
	private final Map<Biome,Set<Coord>> biomeLocs = new EnumMap<>(Biome.class);
	private long startTime = -1;
	private final Logger log;
	private BiomeCoord[] temp;

	public CacheBuilder(Plugin plugin,BfConfig bfc,CacheManager cacheManager,World world,int centerX,int centerZ){
		this.plugin = plugin;
		this.bfc = bfc;
		this.cacheManager = cacheManager;
		this.world = world;
		this.centerX = centerX;
		this.centerZ = centerZ;
		this.pointNumber = this.bfc.getPoints();
		this.pointDistance = this.bfc.getDistance();
		this.pointsPerRow = (pointNumber * 2) + 1;
		this.log = plugin.getLogger();
		this.temp = new BiomeCoord[(pointsPerRow) * (pointsPerRow)];
	}

	public void start() throws IllegalStateException{
		if(CacheBuilder.buildRunning){
			throw new IllegalStateException("A cache build is already running!");
		}
		CacheBuilder.buildRunning = true;
		plugin.getServer().getPluginManager().callEvent(new CacheBuildStartEvent(world,centerX,centerZ));
		System.out.println("Using new cache builder");
		startTime = System.currentTimeMillis();
		new Worker(-bfc.getPoints()).runTask(plugin);
	}

	private class Worker extends BukkitRunnable{
		private final int x;

		private Worker(int x){
			this.x = x;
		}

		@Override
		public void run(){
			final String worldName = world.getName();
			int i = ((x + pointNumber) * pointsPerRow);
			for(int z = -pointNumber; z <= pointNumber; z++){
				final int x1 = x * pointDistance + centerX;
				final int z1 = z * pointDistance + centerZ;
				final Coord l = new Coord(x1,z1);
				final Biome b = world.getBiome(x1,z1);
				//log.info("Biome at " + x1 + "," + z1 + " is " + b.toString());
				temp[i] = new BiomeCoord(b,l);
				//world.getChunkAt(x1/16,z1/16).unload(true);
				i++;
			}

			final int numberDone = x + pointNumber;
			boolean shouldWait = false;
			if(numberDone % 8 == 0){
				log.info(String.format("Row %d/%d finished for world %s",numberDone,pointNumber * 2,worldName));
				if(numberDone % 4 == 0){
					shouldWait = true;
				}
			}

			if(x >= pointNumber){
				for(final Biome b : Biome.values()){
					Set<Coord> bCoords = getCoordsInBiome(b);
					if(!bCoords.isEmpty()){
						biomeLocs.put(b,bCoords);
					}
				}
				temp = null;
				log.info("Cleaning up points...");
				cleanupPoints(bfc.getBiomePointsMax());
				cacheManager.setWorldCache(world,Maps.immutableEnumMap(biomeLocs));
				for(final Map.Entry<Biome,Set<Coord>> bLoc : biomeLocs.entrySet()){
					log.info(bLoc.getKey().toString() + ": " + bLoc.getValue().size() + " entries");
				}
				bfc.saveBiomeCache(world);
				for(final Chunk chunk : world.getLoadedChunks()){
					if(!world.isChunkInUse(chunk.getX(),chunk.getZ())){
						chunk.unload(false);
					}
				}
				world.save();
				System.gc();
				buildRunning = false;
				CacheBuildCompleteEvent completeEvent = new CacheBuildCompleteEvent(world,centerX,centerZ,System.currentTimeMillis() - startTime);
				plugin.getServer().getPluginManager().callEvent(completeEvent);
			}else{
				Worker worker = new Worker(x + 1);
				if(shouldWait){
					worker.runTaskLater(plugin,2L);
				}else{
					worker.runTask(plugin);
				}
			}
		}

		private Set<Coord> getCoordsInBiome(Biome b){
			Set<Coord> bCoords = new HashSet<>();
			for(int i1 = 0; i1 < temp.length; i1++){
				BiomeCoord bc = temp[i1];
				if(bc != null && bc.biome == b){
					bCoords.add(bc.coord);
				}
			}
			return bCoords;
		}
	}

	private void cleanupPoints(int maxPoints){
		for(final Map.Entry<Biome,Set<Coord>> bLoc : biomeLocs.entrySet()){
			final Set<Coord> locSet = bLoc.getValue();
			final int size = locSet.size();
			if(size > maxPoints){
				final List<Coord> locList = new ArrayList<>(locSet);
				final Set<Coord> cleanedSet = new HashSet<>(maxPoints);
				final Set<Integer> alreadyAdded = new HashSet<>(size);
				while(cleanedSet.size() < maxPoints){
					final int i = RANDOM.nextInt(size);
					if(alreadyAdded.add(i)){
						cleanedSet.add(locList.get(i));
					}
				}
				bLoc.setValue(cleanedSet);
			}
		}
	}

	public static boolean isBuildRunning(){
		return buildRunning;
	}
}
