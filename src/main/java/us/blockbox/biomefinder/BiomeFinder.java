package us.blockbox.biomefinder;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import us.blockbox.biomefinder.command.CommandBCacheBuild;
import us.blockbox.biomefinder.command.CommandBfTp;
import us.blockbox.biomefinder.command.CommandBiomeReload;
import us.blockbox.biomefinder.command.CommandBsearch;
import us.blockbox.biomefinder.command.tabcomplete.BiomeTabCompleter;
import us.blockbox.biomefinder.command.tabcomplete.CacheBuildCompleter;
import us.blockbox.biomefinder.listener.CacheBuildListener;
import us.blockbox.biomefinder.locale.BfLocale;

import java.util.EnumSet;
import java.util.Locale;
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

public final class BiomeFinder extends JavaPlugin{
	private static final Matcher UNDERSCORE = Pattern.compile("_").matcher("");
	public final String prefix = ChatColor.GREEN + "BFinder" + ChatColor.DARK_GRAY + "> ";
	private CacheManager cacheManager;
	private static BiomeFinder plugin;
	private Economy economy;
	private BfConfig bfc;
	private ConsoleMessager console;
	private TeleportManager teleportManager;
	private boolean uiLibEnabled;
	private boolean enabledCleanly = false;

	public static BiomeFinder getPlugin(){
		return plugin;
	}

	@Override
	public void onEnable(){
		try{
			Logger log = getLogger();
			plugin = this;
			uiLibEnabled = getServer().getPluginManager().isPluginEnabled("UILib");
			EnumSet<Material> danger = EnumSet.of(Material.FIRE,Material.LAVA,Material.STATIONARY_LAVA,Material.CACTUS);
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
			BfLocale locale = bfc.getLocale();
			if(bfc.getCheckUpdate()){
				new UpdateChecker(this,console).checkUpdate();
			}
			cacheManager = new CacheManager(bfc.loadBiomeCaches());
			teleportManager = new TeleportManager(cacheManager,locale,danger,log);
			setupCommands();
			setupEconomy();
			getServer().getPluginManager().registerEvents(new BiomeSignHandler(this,locale,economy,teleportManager),this);
			getServer().getPluginManager().registerEvents(new CacheBuildListener(console,bfc),this);
			enabledCleanly = true;
		}catch(Exception e){
			e.printStackTrace();
		}
		if(!enabledCleanly){
			getServer().getPluginManager().disablePlugin(this);
		}
	}

	private void setupCommands(){
		getCommand("bsearch").setExecutor(new CommandBsearch(this));
		getCommand("bcachebuild").setExecutor(new CommandBCacheBuild(this));
		getCommand("bcachebuild").setTabCompleter(new CacheBuildCompleter());
		getCommand("bftp").setExecutor(new CommandBfTp(this,bfc,cacheManager,teleportManager));
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

	public static Biome parseBiome(String biome){
		String biomeUppercase = biome.toUpperCase(Locale.US);
		try{
			return Biome.valueOf(biomeUppercase);
		}catch(IllegalArgumentException e){
			for(final Biome b1 : Biome.values()){
				if(UNDERSCORE.reset(b1.name()).replaceAll("").equals(biomeUppercase)){
					return b1;
				}
			}
			return null;
		}
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

	public TeleportManager getTeleportManager(){
		return teleportManager;
	}
}
