package us.blockbox.biomefinder;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import us.blockbox.biomefinder.api.CacheManager;
import us.blockbox.biomefinder.api.ConsoleMessager;
import us.blockbox.biomefinder.api.Economy;
import us.blockbox.biomefinder.api.TeleportManager;
import us.blockbox.biomefinder.command.CommandBCacheBuild;
import us.blockbox.biomefinder.command.CommandBfTp;
import us.blockbox.biomefinder.command.CommandBiomeReload;
import us.blockbox.biomefinder.command.CommandBsearch;
import us.blockbox.biomefinder.command.tabcomplete.BiomeTabCompleter;
import us.blockbox.biomefinder.command.tabcomplete.CacheBuildCompleter;
import us.blockbox.biomefinder.listener.CacheBuildListener;
import us.blockbox.biomefinder.locale.BfLocale;
import us.blockbox.biomefinder.util.EnumUtils;

import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BiomeFinder extends JavaPlugin{
	private static final Matcher UNDERSCORE = Pattern.compile("_").matcher("");
	public final String prefix = ChatColor.GREEN + "BFinder" + ChatColor.DARK_GRAY + "> ";
	private CacheManager cacheManager;
	private static BiomeFinder plugin;
	private BfConfig bfc;
	private ConsoleMessager console;
	private TeleportManager teleportManager;
	private boolean enabledCleanly = false;

	public static BiomeFinder getPlugin(){
		return plugin;
	}

	@Override
	public void onEnable(){
		try{
			init();
			enabledCleanly = true;
		}catch(Exception e){
			e.printStackTrace();
		}
		if(!enabledCleanly){
			getServer().getPluginManager().disablePlugin(this);
		}
	}

	private void init(){
		Logger log = getLogger();
		plugin = this;
		Set<Material> danger = EnumUtils.getByNames(Material.class,"FIRE","LAVA","STATIONARY_LAVA","CACTUS","MAGMA","MAGMA_BLOCK");
		log.info("Danger materials: " + danger);
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
		cacheManager = new CacheManagerImpl(bfc.loadBiomeCaches());
		teleportManager = new TeleportManagerImpl(cacheManager,locale,danger,log);
		setupCommands();
		Economy economy = setupEconomy();
		getServer().getPluginManager().registerEvents(new BiomeSignHandler(this,locale,economy,teleportManager),this);
		getServer().getPluginManager().registerEvents(new CacheBuildListener(console,bfc),this);
	}

	private void setupCommands(){
		getCommand("bsearch").setExecutor(new CommandBsearch(this));
		getCommand("bcachebuild").setExecutor(new CommandBCacheBuild(this));
		getCommand("bcachebuild").setTabCompleter(new CacheBuildCompleter());
		getCommand("bftp").setExecutor(new CommandBfTp(bfc,cacheManager,teleportManager));
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

	private Economy setupEconomy(){
		if(getServer().getPluginManager().getPlugin("Vault") == null){
			return null;
		}
		final RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if(rsp == null){
			return null;
		}
		net.milkbowl.vault.economy.Economy provider = rsp.getProvider();
		if(provider == null){
			return null;
		}
		return new VaultEconomyImpl(provider);
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

	@Deprecated
	public boolean isUiLibEnabled(){
		return false;
	}

	public TeleportManager getTeleportManager(){
		return teleportManager;
	}
}
