package us.blockbox.biomefinder.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.blockbox.biomefinder.BfConfig;
import us.blockbox.biomefinder.BiomeFinder;
import us.blockbox.biomefinder.ColoredConsoleMessager;
import us.blockbox.biomefinder.event.CacheBuildCompleteEvent;
import us.blockbox.biomefinder.event.CacheBuildStartEvent;

//Created 11/20/2016 1:11 AM
public class CacheBuildListener implements Listener{

	private static final BiomeFinder plugin = BiomeFinder.getPlugin();
	private final BfConfig bfc = plugin.getBfConfig();

	@EventHandler
	public void onCacheBuildStart(CacheBuildStartEvent e){
		plugin.getConsole().info(
				"====================================",
				"",
				"Building cache for: " + e.getWorld().getName(),
				"Points in each direction: " + bfc.getPoints(),
				"Point distance: " + bfc.getDistance(),
				"Center point: X " + e.getCenterX() + ", Z " + e.getCenterZ(),
				"",
				"===================================="
		);
	}

	@EventHandler
	public void onCacheBuildComplete(CacheBuildCompleteEvent e){
		plugin.getConsole().success(
				"Cache building complete for world " + e.getWorld().getName(),
				"Elapsed time: " + e.getTimeElapsed() + " seconds."
		);
	}
}
