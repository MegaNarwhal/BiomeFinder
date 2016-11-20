package us.blockbox.biomefinder.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import us.blockbox.biomefinder.BfConfig;
import us.blockbox.biomefinder.ConsoleMessager;
import us.blockbox.biomefinder.event.CacheBuildCompleteEvent;
import us.blockbox.biomefinder.event.CacheBuildStartEvent;

import java.util.logging.Logger;

//Created 11/20/2016 1:11 AM
public class CacheBuildListener implements Listener{

	private final JavaPlugin plugin;

	public CacheBuildListener(JavaPlugin plugin){
		this.plugin = plugin;
	}

	@EventHandler
	public void onCacheBuildStart(CacheBuildStartEvent e){
		ConsoleMessager.info(
				"====================================",
				"",
				"Building cache for: " + e.getWorld().getName(),
				"Points in each direction: " + BfConfig.getPoints(),
				"Point distance: " + BfConfig.getDistance(),
				"Center point: X " + e.getCenterX() + ", Z " + e.getCenterZ(),
				"",
				"===================================="
		);
	}

	@EventHandler
	public void onCacheBuildComplete(CacheBuildCompleteEvent e){
		ConsoleMessager.success(
				"Cache building complete for world " + e.getWorld().getName(),
				"Elapsed time: " + e.getTimeElapsed() + " seconds."
		);
	}
}
