package us.blockbox.biomefinder;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.blockbox.biomefinder.locale.BfLocale;
import us.blockbox.biomefinder.locale.BfMessage;

import java.text.DecimalFormat;

import static us.blockbox.biomefinder.BiomeFinder.econ;

//Created 11/6/2016 2:21 AM
class BiomeSignHandler implements Listener{

	private final JavaPlugin plugin;
	private final String currencyName;
	private static final DecimalFormat format = new DecimalFormat("0.#");
	private static BfLocale locale = BfConfig.getLocale();

	BiomeSignHandler(JavaPlugin plugin){
		this.plugin = plugin;
		if(econ != null){
			final String currencyNameTemp = econ.currencyNamePlural();
			if(currencyNameTemp == null){
				currencyName = "";
			}else{
				currencyName = currencyNameTemp;
			}
		}else{
			currencyName = null;
		}
	}

	@EventHandler
	void onInteract(PlayerInteractEvent e){
		if(e.isCancelled() || !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			return;
		}
		final Block block = e.getClickedBlock();
		if(!block.getType().equals(Material.SIGN_POST) && !block.getType().equals(Material.WALL_SIGN)){
			return;
		}
		final Sign sign = (Sign)block.getState();
		if(!ChatColor.stripColor(sign.getLine(0)).trim().equalsIgnoreCase("[BiomeTP]")){
			return;
		}
		final Player p = e.getPlayer();

		Biome biome = getSignBiome(sign.getLine(2));
		if(biome == null){
			return;
		}

		if(!p.hasPermission("biomefinder.sign." + biome.toString().toLowerCase())){
			p.sendMessage(locale.getMessage(BfMessage.PLAYER_NO_PERMISSION));
			return;
		}

		final double price = getPrice(sign);
		if(price <= 0 || econ == null){
			BiomeFinder.tpToBiome(p,biome);
		}else{
			if(econ.getBalance(p) >= price){
				if(BiomeFinder.tpToBiome(p,biome)){
					new BukkitRunnable(){
						@Override
						public void run(){
							if(price > 0){
								econ.withdrawPlayer(p,price);
								p.sendMessage(String.format(locale.getMessage(BfMessage.SIGN_ECON_CHARGED),format.format(price),currencyName));
							}
						}
					}.runTaskAsynchronously(plugin);
				}
			}else{
				p.sendMessage(locale.getMessage(BfMessage.SIGN_ECON_FAILED));
			}
		}
	}

	@EventHandler
	void onSignChange(SignChangeEvent e){
		if(e.isCancelled()){
			return;
		}

		if(!ChatColor.stripColor(e.getLine(0)).trim().equalsIgnoreCase("[BiomeTP]")){
			return;
		}

		final Player p = e.getPlayer();
		if(!p.hasPermission("biomefinder.create")){
			e.getBlock().breakNaturally();
			p.sendMessage(ChatColor.GRAY + "You don't have permission.");
			return;
		}

		if(getSignBiome(e.getLine(2)) == null){
			e.getBlock().breakNaturally();
			p.sendMessage(ChatColor.GRAY + "Invalid biome name.");
			return;
		}

		if(!e.getLine(3).trim().equals("")){
			if(!p.hasPermission("biomefinder.create.cost")){
				p.sendMessage(ChatColor.GRAY + "You don't have permission.");
				e.getBlock().breakNaturally();
				return;
			}
			try{
				Double.parseDouble(e.getLine(3).trim().replaceAll("[^0-9.]",""));
			}catch(NumberFormatException ex){
				p.sendMessage(ChatColor.GRAY + "Invalid price.");
				e.getBlock().breakNaturally();
			}
		}
	}

	private double getPrice(Sign sign){
		if(sign.getLine(3).trim().equals("")){
			return 0;
		}

		final double price;
		try{
			price = Double.parseDouble(sign.getLine(3).trim().replaceAll("[^0-9.]",""));
		}catch(NumberFormatException ex){
			plugin.getLogger().warning("Incorrectly formatted teleport price on sign at " + sign.getLocation().toString());
			return -1;
		}

		return price;
	}

	private boolean enoughMoney(final Player p,final Sign sign){
		if(sign.getLine(3).trim().equals("")){
			return true;
		}
		if(econ == null){
			plugin.getLogger().warning("Sign costs are not working properly. Make sure you have an economy plugin enabled.");
			return false;
		}
		final double price;
		try{
			price = Double.parseDouble(sign.getLine(3).trim().replaceAll("[^0-9.]",""));
		}catch(NumberFormatException ex){
			plugin.getLogger().warning("Incorrectly formatted teleport price on sign at " + sign.getLocation().toString());
			return false;
		}

		if(econ.getBalance(p) >= price){
			return true;
		}else{
			p.sendMessage(locale.getMessage(BfMessage.SIGN_ECON_FAILED));
		}
		return false;
	}

	Biome getSignBiome(String signBiome){
		if(signBiome == null || signBiome.trim().equals("")){
			return null;
		}
		signBiome = signBiome.toUpperCase();
		Biome biome = BiomeFinder.parseBiome(signBiome);
		if(biome == null){
			for(Biome b : Biome.values()){
				if(b.toString().startsWith(signBiome) || b.toString().replace("_","").startsWith(signBiome)){
					biome = b;
					break;
				}
			}
			if(biome == null){
				return null;
			}
		}
		return biome;
	}
}
