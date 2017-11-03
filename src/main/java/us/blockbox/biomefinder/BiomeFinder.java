package us.blockbox.biomefinder;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;
import org.inventivetalent.update.spiget.comparator.VersionComparator;
import us.blockbox.biomefinder.command.CommandBCacheBuild;
import us.blockbox.biomefinder.command.CommandBfTp;
import us.blockbox.biomefinder.command.CommandBiomeReload;
import us.blockbox.biomefinder.command.CommandBsearch;
import us.blockbox.biomefinder.command.tabcomplete.BiomeTabCompleter;
import us.blockbox.biomefinder.command.tabcomplete.CacheBuildCompleter;
import us.blockbox.biomefinder.listener.CacheBuildListener;
import us.blockbox.biomefinder.locale.BfLocale;
import us.blockbox.biomefinder.locale.BfMessage;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//todo copy defaults for locale
/*
1.2.8
Add "far" keyword to /bftp
Disable BiomeFinder if something fails on startup
1.2.7
Massive speed increase to point cleanup for large cache builds.
Fix players not being able to place signs that aren't BiomeTP signs.
 */

public class BiomeFinder extends JavaPlugin{
	public static final String prefix = ChatColor.GREEN + "BFinder" + ChatColor.DARK_GRAY + "> ";
	private static final Matcher underscore = Pattern.compile("_").matcher("");
	private static CacheManager cacheManager;
	private static Logger log;
	static final Random rand = new Random();
	private static BiomeFinder plugin;
	private static final EnumSet<Material> danger = EnumSet.of(Material.FIRE,Material.LAVA,Material.STATIONARY_LAVA,Material.CACTUS);
	private Economy economy;
	private static BfLocale locale;
	private BfConfig bfc;
	private ConsoleMessager console;
	private boolean uiLibEnabled;
	private boolean enabledCleanly = false;

	public static BiomeFinder getPlugin(){
		return plugin;
	}

	@Override
	public void onEnable(){
		try{
			log = getLogger();
			plugin = this;
			uiLibEnabled = Bukkit.getPluginManager().isPluginEnabled("UILib");
			final Material magma = Material.getMaterial("MAGMA");
			if(magma != null) danger.add(magma);
			bfc = new BfConfig(this);
			bfc.loadConfig();
			if(bfc.isLogColorEnabled()){
				console = new ColoredConsoleMessager(log);
			}else{
				console = new PlainConsoleMessager(log);
			}
			if(bfc.isVersionChanged()){
				console.warn("The config format has been changed. New options may have been added or new defaults set. Please regenerate your config to take advantage of any changes.");
			}
			locale = bfc.getLocale();
			if(bfc.getCheckUpdate()){
				checkUpdate();
			}
			cacheManager = new CacheManager(bfc.loadBiomeCaches());
			setupCommands();
			setupEconomy();
			getServer().getPluginManager().registerEvents(new BiomeSignHandler(this,economy),this);
			getServer().getPluginManager().registerEvents(new CacheBuildListener(),this);
			enabledCleanly = true;
		}catch(Exception e){
			e.printStackTrace();
		}
		if(!enabledCleanly){
			getServer().getPluginManager().disablePlugin(this);
		}
	}

	private void checkUpdate(){
		final SpigetUpdate updater = new SpigetUpdate(this,30892);
		updater.setVersionComparator(VersionComparator.EQUAL);
//			updater.setVersionComparator(VersionComparator.SEM_VER);
		updater.checkForUpdate(new UpdateCallback(){
			@Override
			public void updateAvailable(String newVersion,String downloadUrl,boolean hasDirectDownload){
				console.warn("An update is available! You're running " + getDescription().getVersion() + ", the latest version is " + newVersion + ".",downloadUrl,"You can disable update checking in the config.yml.");
			}

			@Override
			public void upToDate(){
				console.success("You're running the latest version. You can disable update checking in the config.yml.");
			}
		});
	}

	private void setupCommands(){
		getCommand("bsearch").setExecutor(new CommandBsearch(this));
		getCommand("bcachebuild").setExecutor(new CommandBCacheBuild(this));
		getCommand("bcachebuild").setTabCompleter(new CacheBuildCompleter());
		getCommand("bftp").setExecutor(new CommandBfTp());
		getCommand("bftp").setTabCompleter(new BiomeTabCompleter());
		getCommand("biomereload").setExecutor(new CommandBiomeReload());
	}

	@Override
	public void onDisable(){
		if(enabledCleanly){
			getServer().getScheduler().cancelTasks(this);
			bfc.saveBiomeCaches();
		}
	}

	private boolean setupEconomy(){
		if(getServer().getPluginManager().getPlugin("Vault") == null){
			return false;
		}
		final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if(rsp == null){
			return false;
		}
		economy = rsp.getProvider();
		return economy != null;
	}

	public enum LocationPreference{
		NEAR,
		FAR,
		ANY
	}

	public static boolean tpToBiome(CommandSender sender,Player target,Biome b,LocationPreference pref){
		final World w = target.getWorld();
		final Set<Coord> locSet = cacheManager.getCache(w).get(b);

		if(locSet == null || locSet.isEmpty()){
			sender.sendMessage(prefix + String.format(locale.getMessage(BfMessage.BIOME_LOCATIONS_MISSING),b.toString()));
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
			String msg = prefix + String.format(locale.getMessage(BfMessage.PLAYER_TELEPORTED),locale.getFriendlyName(b),l.getBlockX(),l.getBlockZ());
			target.sendMessage(msg);
			if(sender != target){
				sender.sendMessage(msg);
			}
		}
		return teleSuccess;
	}

	public static boolean tpToBiome(final Player p,final Biome b,final LocationPreference preference){
		return tpToBiome(p,p,b,preference);
	}

	public static boolean tpToBiome(final Player p,final Biome b){
		return tpToBiome(p,b,LocationPreference.ANY);
	}

	private static Location pickSafe(World world,List<Coord> coords,boolean sorted){
		Coord c;
//		int tries = 0;
		final int maxtries = coords.size();
		Location l;
		for(int tries = 0; tries < maxtries; tries++){
			c = coords.get(sorted ? tries : rand.nextInt(maxtries - tries));
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

	public static Biome parseBiome(String biome){
		final Biome b;
		biome = biome.toUpperCase(Locale.US);
		try{
			b = Biome.valueOf(biome);
		}catch(final IllegalArgumentException e){
			for(final Biome b1 : Biome.values()){
				if(underscore.reset(b1.name()).replaceAll("").equals(biome)){
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

	public CacheManager getCacheManager(){
		return cacheManager;
	}

	public BfConfig getBfConfig(){
		return bfc;
	}

	public ConsoleMessager getConsole(){
		return console;
	}

	public boolean isUiLibEnabled(){
		return uiLibEnabled;
	}
}
