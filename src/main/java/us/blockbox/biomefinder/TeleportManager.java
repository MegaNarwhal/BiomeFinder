package us.blockbox.biomefinder;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.blockbox.biomefinder.locale.BfLocale;
import us.blockbox.biomefinder.locale.BfMessage;

import java.util.*;
import java.util.logging.Logger;

public class TeleportManager{
	private static Random RANDOM = new Random();
	private final CacheManager cacheManager;
	private final BfLocale locale;
	private final Set<Material> danger;
	private final Logger log;

	public TeleportManager(CacheManager cacheManager,BfLocale locale,Set<Material> danger,Logger log){
		this.cacheManager = Objects.requireNonNull(cacheManager);
		this.locale = Objects.requireNonNull(locale);
		this.danger = EnumSet.copyOf(danger);
		this.log = Objects.requireNonNull(log);
	}

	public enum LocationPreference{
		NEAR,
		FAR,
		ANY
	}

	public boolean tpToBiome(CommandSender sender,Player target,Biome b,LocationPreference pref){
		final World w = target.getWorld();
		final Set<Coord> locSet = cacheManager.getCache(w).get(b);

		if(locSet == null || locSet.isEmpty()){
			sender.sendMessage(locale.getPrefix() + String.format(locale.getMessage(BfMessage.BIOME_LOCATIONS_MISSING),b.toString()));
			return false;
		}

		final List<Coord> locList;
		if(pref == LocationPreference.ANY){
			locList = new ArrayList<>(locSet);
		}else{
			locList = getSortedCoords(new Coord(target.getLocation()),locSet,pref);
		}

		final Location l = pickSafe(w,locList,pref != LocationPreference.ANY); //todo don't spawn players into side of block

		if(l == null){
			sender.sendMessage(locale.getMessage(BfMessage.BIOME_LOCATIONS_UNSAFE));
			return false;
		}
/*		p.setInvulnerable(true);
		new BukkitRunnable(){
			@Override
			public void run(){
				p.setInvulnerable(false);
			}
		}.runTaskLater(plugin,40L);*/
		final boolean teleSuccess = target.teleport(l);
		if(teleSuccess){
			String msg = locale.getPrefix() + String.format(locale.getMessage(BfMessage.PLAYER_TELEPORTED),locale.getFriendlyName(b),l.getBlockX(),l.getBlockZ());
			target.sendMessage(msg);
			if(sender != target){
				sender.sendMessage(msg);
			}
		}
		return teleSuccess;
	}

	public boolean tpToBiome(final Player p,final Biome b,final LocationPreference preference){
		return tpToBiome(p,p,b,preference);
	}

	public boolean tpToBiome(final Player p,final Biome b){
		return tpToBiome(p,b,LocationPreference.ANY);
	}

	private Location pickSafe(World world,List<Coord> coords,boolean sorted){
		Coord c;
//		int tries = 0;
		final int maxtries = coords.size();
		Location l;
		for(int tries = 0; tries < maxtries; tries++){
			c = coords.get(sorted ? tries : RANDOM.nextInt(maxtries - tries));
			l = c.asLocation(world);
			if(world.getEnvironment() == World.Environment.NETHER){
				int yNew = find2BlockTallY(world,l);
				if(yNew == -1){
					continue; //there is no open spot for player here
				}else{
					l.setY(yNew);
				}
			}else{
				l.setY(world.getHighestBlockYAt(l.getBlockX(),l.getBlockZ()) + 1);
			}
			if(isSafe(l)){
				return l;
			}
			coords.remove(c);
		}
		return null;
	}

	private static int find2BlockTallY(World world,Location l){
		final int x = l.getBlockX();
		final int z = l.getBlockZ();
		for(int y = 8; y < 126; y++){
			if(world.getBlockAt(x,y,z).getType() == Material.AIR && world.getBlockAt(x,y + 1,z).getType() == Material.AIR){
				return y;
			}
		}
		return -1;
	}

	private static List<Coord> getSortedCoords(final Coord playerCoord,Set<Coord> coords,LocationPreference distance){
		//		Comparator<Coord> comp = new Comparator<Coord>(){
//			@Override
//			public int compare(Coord o1,Coord o2){
//				return playerCoord.distanceSquared(o1) - playerCoord.distanceSquared(o2);
//			}
//		};//todo
		final CoordDistance[] cDists = new CoordDistance[coords.size()];
		int i = 0;
		for(final Coord c : coords){
			cDists[i] = (new CoordDistance(c,playerCoord.distanceSquared(c)));
			i++;
		}
		if(distance == LocationPreference.NEAR){
			Arrays.sort(cDists);
		}else if(distance == LocationPreference.FAR){
			Arrays.sort(cDists,Collections.<CoordDistance>reverseOrder());
		}
		final List<Coord> result = new ArrayList<>(cDists.length);
		for(final CoordDistance cd : cDists){
			result.add(cd.coord);
		}
		return result;
	}

	private boolean isSafe(Location loc){
		final World w = loc.getWorld();
		final int x = loc.getBlockX();
		final int y = loc.getBlockY() - 1;
		final int z = loc.getBlockZ();
		for(int xi = x - 1; xi <= x; xi++){
			for(int zi = z - 1; zi <= z; zi++){
				//log.info(xi + " " + y + " " + zi + " " + w.getBlockAt(xi,y,zi).getType().toString());
				for(int yi = -1; yi < 1; yi++){
//					System.out.println(xi + " " + yi + " " + zi);
					if(danger.contains(w.getBlockAt(xi,y + yi,zi).getType())){
						log.info("Unsafe teleport location at X: " + x + ", Z: " + z);
						return false;
					}
				}
			}
		}
		return true;
	}
}
