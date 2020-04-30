package us.blockbox.biomefinder.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.blockbox.biomefinder.BfConfig;
import us.blockbox.biomefinder.api.ConsoleMessager;
import us.blockbox.biomefinder.event.CacheBuildCompleteEvent;
import us.blockbox.biomefinder.event.CacheBuildStartEvent;

import java.util.concurrent.TimeUnit;

public class CacheBuildListener implements Listener{

	private final ConsoleMessager console;
	private final BfConfig bfc;

	public CacheBuildListener(ConsoleMessager console,BfConfig bfc){
		this.console = console;
		this.bfc = bfc;
	}

	@EventHandler
	public void onCacheBuildStart(CacheBuildStartEvent e){
		console.info(
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
		double seconds = e.getDurationMillis() / (double)TimeUnit.SECONDS.toMillis(1);
		console.success(
				"Cache building complete for world " + e.getWorld().getName(),
				String.format("Elapsed time: %.2f seconds.",seconds)
		);
	}
}
