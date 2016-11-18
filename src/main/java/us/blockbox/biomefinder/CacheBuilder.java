package us.blockbox.biomefinder;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Logger;

import static us.blockbox.biomefinder.BiomeFinder.biomeCache;
import static us.blockbox.biomefinder.BiomeFinder.rand;

//Created 11/3/2016 11:49 PM
public class CacheBuilder extends BukkitRunnable{

	private final JavaPlugin plugin;
	private final World world;
	private static final int pointDistance = BfConfig.getDistance();
	private static final int pointNumber = BfConfig.getPoints();
	private final int x;
	private static Map<Biome,Set<Coord>> biomeLocs = new HashMap<>();
	private static final int pointsPerRow = pointNumber * 2 + 1;
	private static BiomeCoord[] temp = new BiomeCoord[(pointsPerRow) * (pointsPerRow)];
	public static boolean cacheBuildRunning;
	private final int centerX;
	private final int centerZ;
	public static long startTime;
	private final Logger log = BiomeFinder.plugin.getLogger();

	private CacheBuilder(JavaPlugin plugin,World world,int x,int centerX,int centerZ){
		this.plugin = plugin;
		this.world = world;
		this.x = x;
		this.centerX = centerX;
		this.centerZ = centerZ;
	}

	public CacheBuilder(JavaPlugin plugin,World world,int centerX,int centerZ){
		this.plugin = plugin;
		this.world = world;
		this.x = -pointNumber;
		this.centerX = centerX;
		this.centerZ = centerZ;
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
			log.info("Row " + String.valueOf(numberDone) + "/" + String.valueOf(pointNumber * 2) + " finished for world " + worldName);
			if(numberDone % 4 == 0){
				shouldWait = true;
			}
		}

		if(x >= pointNumber){
			Set<Coord> bCoords;
			for(Biome b : Biome.values()){
				bCoords = new HashSet<>();
				for(int i1 = 0; i1 < temp.length; i1++){
					if(temp[i1] != null && temp[i1].biome == b){
						bCoords.add(temp[i1].coord);
						temp[i1] = null;
					}
				}
				if(!bCoords.isEmpty()){
					biomeLocs.put(b,bCoords);
				}
			}
			temp = new BiomeCoord[(pointsPerRow) * (pointsPerRow)];
			log.info("Cleaning up points...");
			cleanupPoints(BfConfig.getBiomePointsMax());
			biomeCache.put(world,new HashMap<>(biomeLocs));
			for(Map.Entry<Biome,Set<Coord>> bLoc : biomeLocs.entrySet()){
				log.info(bLoc.getKey().toString() + ": " + bLoc.getValue().size() + " entries");
			}
			BfConfig.saveBiomeCache(world);
			for(Chunk chunk : world.getLoadedChunks()){
				if(!world.isChunkInUse(chunk.getX(),chunk.getZ())){
					chunk.unload(false);
				}
			}
			world.save();
			System.gc();
			biomeLocs.clear();
			double elapsed = (double)(System.currentTimeMillis() - startTime)/1000D;
			ConsoleMessager.success(
					"Cache building complete for world " + worldName,
					"Elapsed time: " + elapsed + " seconds."
			);
			startTime = 0;
			cacheBuildRunning = false;
			return;
		}

		if(shouldWait){
			new CacheBuilder(plugin,world,x + 1,centerX,centerZ).runTaskLater(plugin,2L);
		}else{
			new CacheBuilder(plugin,world,x + 1,centerX,centerZ).runTask(plugin);
		}
	}

	private static void cleanupPoints(int maxPoints){
		for(Map.Entry<Biome,Set<Coord>> bLoc : biomeLocs.entrySet()){
			final Set<Coord> locSet = bLoc.getValue();
			if(locSet.size() >= maxPoints){
				List<Coord> locList = new ArrayList<>(locSet);
				for(int i = locList.size(); i > maxPoints; i--){
					locList.remove(rand.nextInt(i));
				}
				biomeLocs.put(bLoc.getKey(),new HashSet<>(locList));
			}
		}
	}
}
