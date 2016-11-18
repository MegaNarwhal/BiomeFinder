package us.blockbox.biomefinder;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.blockbox.biomefinder.command.*;
import us.blockbox.biomefinder.command.tabcomplete.*;
import us.blockbox.biomefinder.locale.BfLocale;
import us.blockbox.biomefinder.locale.BfMessage;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static us.blockbox.biomefinder.BfConfig.*;

//Created 10/19/2016 12:29 AM
public class BiomeFinder extends JavaPlugin implements Listener{

	public static final String prefix = ChatColor.GREEN + "BFinder" + ChatColor.DARK_GRAY + "> ";
	static Map<World,Map<Biome,Set<Coord>>> biomeCache = new HashMap<>();
	static Map<World,Map<Biome,Set<Coord>>> biomeCacheOriginal;
	private static Logger log;
	final static Random rand = new Random();
	static JavaPlugin plugin;
	private static EnumSet<Material> danger = EnumSet.of(Material.FIRE,Material.LAVA,Material.STATIONARY_LAVA,Material.CACTUS);
	static Economy econ = null;
	private static BfLocale locale;

	/*1.2.3
	Make no permission message for signs use message from localization.
	/bftp and [BiomeTP] signs no longer require underscores.
	*/

	@Override
	public void onEnable(){
		log = getLogger();
		plugin = this;

		if(getServer().getBukkitVersion().startsWith("1.10") || getServer().getBukkitVersion().startsWith("1.11")){
			danger.add(Material.MAGMA);
		}

		BfConfig.getInstance();
		loadConfig();
		locale = BfConfig.getLocale();

		if(getCheckUpdate()){
			try{
				new SpigotUpdater(this,30892);
			}catch(IOException e){
				log.info("Failed to check for update.");
			}
		}

		getCommand("bsearch").setExecutor(new CommandBsearch(this));
		getCommand("bcachebuild").setExecutor(new CommandBCacheBuild(this));
		getCommand("bcachebuild").setTabCompleter(new CacheBuildCompleter());
		getCommand("bftp").setExecutor(new CommandBfTp());
		getCommand("bftp").setTabCompleter(new BiomeTabCompleter());
		getCommand("biomereload").setExecutor(new CommandBiomeReload(this));

		setupEconomy();

		loadBiomeCaches();

		getServer().getPluginManager().registerEvents(new BiomeSignHandler(this),this);
	}

	@Override
	public void onDisable(){
		getServer().getScheduler().cancelTasks(this);
		saveBiomeCaches();
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public static boolean hasCache(World world){
		return biomeCache.containsKey(world);
	}

	public static Map<Biome,Set<Coord>> getCache(World world){
		return biomeCache.get(world);
	}

	public static boolean tpToBiome(final Player p,final Biome b,final boolean nearby){
		final World w = p.getWorld();
		Set<Coord> locSet = biomeCache.get(w).get(b);

		if(locSet == null || locSet.isEmpty()){
			p.sendMessage(prefix + String.format(locale.getMessage(BfMessage.BIOME_LOCATIONS_MISSING),b.toString()));
			return false;
		}

		List<Coord> locList;
		if(nearby){
			locList = getNearbyCoords(new Coord(p.getLocation()),locSet);
		}else{
			locList = new ArrayList<>(locSet);
		}

		final Location l = pickSafe(w,locList,nearby);

		if(l == null){
			p.sendMessage(locale.getMessage(BfMessage.BIOME_LOCATIONS_UNSAFE));
			return false;
		}

		p.setInvulnerable(true);
		new BukkitRunnable(){
			@Override
			public void run(){
				p.setInvulnerable(false);
			}
		}.runTaskLater(plugin,40L);
		boolean teleSuccess = p.teleport(l);
		if(teleSuccess){
			p.sendMessage(prefix + String.format(locale.getMessage(BfMessage.PLAYER_TELEPORTED),b.toString(),l.getBlockX(),l.getBlockZ()));
		}
		return teleSuccess;
	}

	public static boolean tpToBiome(final Player p,final Biome b){
		return tpToBiome(p,b,false);
	}

	private static Location pickSafe(World world,List<Coord> coords,boolean nearby){
		Coord c;
		Location l = null;
		int tries = 0;
		final int maxtries = coords.size();

		while(tries < maxtries){
			c = coords.get(nearby ? tries : rand.nextInt(maxtries-tries));
			l = c.asLocation(world);
			if(world.getEnvironment() == World.Environment.NETHER){
				final int x = l.getBlockX();
				final int z = l.getBlockZ();
				for(int y = 8;y < 126;y++){
					if(world.getBlockAt(x,y,z).getType() == Material.AIR && world.getBlockAt(x,y+1,z).getType() == Material.AIR){
						l.setY(y);
						break;
					}
				}
			}else{
				l.setY(world.getHighestBlockYAt(l.getBlockX(),l.getBlockZ()) + 1);
			}
			if(isSafe(l)){
				break;
			}
			coords.remove(c);
			tries++;
		}

		if(tries >= maxtries ){
			return null;
		}
		return l;
	}

	private static List<Coord> getNearbyCoords(Coord playerCoord,Set<Coord> coords){
		CoordDistance[] cDists = new CoordDistance[coords.size()];
		int i = 0;
		for(Coord c : coords){
			cDists[i] = (new CoordDistance(c,playerCoord.distanceSquaredInt(c)));
			i++;
		}
		Arrays.sort(cDists);
		List<Coord> result = new ArrayList<>();
		for(CoordDistance cd : cDists){
			result.add(cd.coord);
		}
		if(result.isEmpty()){
			return null;
		}
		return result;
	}

	public static Biome parseBiome(String biome){
		Biome b;
		biome = biome.toUpperCase();
		try{
			b = Biome.valueOf(biome);
		}catch(IllegalArgumentException e){
			for(Biome b1 : Biome.values()){
				if(b1.toString().replace("_","").equals(biome)){
					return b1;
				}
			}
			return null;
		}
		return b;
	}

	private static boolean isSafe(Location loc){
		final World w = loc.getWorld();
		final int x = loc.getBlockX();
		final int y = loc.getBlockY() - 1;
		final int z = loc.getBlockZ();
		for(int xi = x-1;xi <= x;xi++){
			for(int zi = z-1;zi <= z;zi++){
					//log.info(xi + " " + y + " " + zi + " " + w.getBlockAt(xi,y,zi).getType().toString());
					if(danger.contains(w.getBlockAt(xi,y,zi).getType()) || danger.contains(w.getBlockAt(xi,y-1,zi).getType())){
						log.info("Unsafe teleport location at X: " + x + ", Z: " + z);
						return false;
					}
			}
		}

		return true;
	}
}
