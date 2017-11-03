package us.blockbox.biomefinder;

import net.milkbowl.vault.economy.Economy;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.blockbox.biomefinder.locale.BfLocale;
import us.blockbox.biomefinder.locale.BfMessage;

import java.util.regex.Pattern;

class BiomeSignHandler implements Listener{
	//	private static final DecimalFormat format = new DecimalFormat("0.#");
	private static final Pattern nonDecimal = Pattern.compile("[^0-9.]");
	private static final String SIGN_FIRST_LINE = "[BiomeTP]";
	private final Plugin plugin;
	private final BfLocale locale;
	private final Economy economy;
	private final TeleportManager tpManager;

	BiomeSignHandler(Plugin plugin,BfLocale locale,Economy economy,TeleportManager tpManager){
		this.plugin = plugin;
		this.locale = locale;
		this.economy = economy;
		this.tpManager = tpManager;
	}

	@EventHandler(ignoreCancelled = true)
	void onInteract(PlayerInteractEvent e){
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK){
			return;
		}
		final Block block = e.getClickedBlock();
		if(block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN){
			return;
		}
		final Sign sign = (Sign)block.getState();
		if(!ChatColor.stripColor(sign.getLine(0).trim()).equalsIgnoreCase(SIGN_FIRST_LINE)){
			return;
		}
		final Player p = e.getPlayer();
		final Biome biome = getSignBiome(sign.getLine(2));
		if(biome == null){
			return;
		}
		if(!p.hasPermission("biomefinder.sign." + biome.toString().toLowerCase())){
			p.sendMessage(locale.getMessage(BfMessage.PLAYER_NO_PERMISSION));
			return;
		}
		final double price = getPrice(sign);
		if(price <= 0 || economy == null){
			tpManager.tpToBiome(p,biome);
		}else{
			if(economy.getBalance(p) >= price){
				if(tpManager.tpToBiome(p,biome)){
					new BukkitRunnable(){
						@Override
						public void run(){
							if(price > 0){
								economy.withdrawPlayer(p,price);
								p.sendMessage(String.format(locale.getMessage(BfMessage.SIGN_ECON_CHARGED),economy.format(price)));
							}
						}
					}.runTaskAsynchronously(plugin);
				}
			}else{
				p.sendMessage(locale.getMessage(BfMessage.SIGN_ECON_FAILED));
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	void onSignChange(SignChangeEvent e){
		if(ChatColor.stripColor(e.getLine(0)).trim().equalsIgnoreCase("[BiomeTP]") && !isValidBiomeSign(e)){
			e.getBlock().breakNaturally();
		}
	}

	private boolean isValidBiomeSign(SignChangeEvent e){
		final int LINE_PRICE = 3;

		final Player p = e.getPlayer();
		if(!p.hasPermission("biomefinder.create")){
			p.sendMessage(ChatColor.GRAY + "You don't have permission.");
			return false;
		}
		if(getSignBiome(e.getLine(2)) == null){
			p.sendMessage(ChatColor.GRAY + "Invalid biome name.");
			return false;
		}
		final String priceLine = e.getLine(LINE_PRICE);
		if(!priceLine.trim().equals("")){
			if(!p.hasPermission("biomefinder.create.cost")){
				p.sendMessage(ChatColor.GRAY + "You don't have permission.");
				return false;
			}
			if(parsePrice(priceLine) == null){
				p.sendMessage(ChatColor.GRAY + "Invalid price.");
				return false;
			}
		}
		return true;
	}

	private Double parsePrice(String line){
		try{
			return Double.parseDouble(nonDecimal.matcher(line.trim()).replaceAll(""));
		}catch(NumberFormatException e){
			return null;
		}
	}

	private double getPrice(Sign sign){
		if(sign.getLine(3).trim().equals("")){
			return 0;
		}
		final Double price = parsePrice(sign.getLine(3));
		if(price == null){
			plugin.getLogger().warning("Incorrectly formatted teleport price on sign at " + sign.getLocation().toString());
			return -1;
		}
		return price;
	}

/*	private boolean enoughMoney(final Player p,final Sign sign){
		if(sign.getLine(3).trim().equals("")){
			return true;
		}
		if(econ == null){
			plugin.getLogger().warning("Sign costs are not working properly. Make sure you have an economy plugin enabled.");
			return false;
		}
		final Double price = parsePrice(sign.getLine(3));
		if(price == null){
			plugin.getLogger().warning("Incorrectly formatted teleport price on sign at " + sign.getLocation().toString());
			return false;
		}
		if(econ.getBalance(p) >= price){
			return true;
		}else{
			p.sendMessage(locale.getMessage(BfMessage.SIGN_ECON_FAILED));
		}
		return false;
	}*/

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
