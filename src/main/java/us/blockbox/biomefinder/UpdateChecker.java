package us.blockbox.biomefinder;

import org.bukkit.plugin.Plugin;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;
import org.inventivetalent.update.spiget.comparator.VersionComparator;
import us.blockbox.biomefinder.api.ConsoleMessager;

import java.util.Objects;

final class UpdateChecker{
	private static final int RESOURCE_ID = 30892;
	private final Plugin plugin;
	private final ConsoleMessager console;

	UpdateChecker(Plugin plugin,ConsoleMessager console){
		this.plugin = Objects.requireNonNull(plugin);
		this.console = Objects.requireNonNull(console);
	}

	public void checkUpdate(){
		final SpigetUpdate updater = new SpigetUpdate(plugin,RESOURCE_ID);
		updater.setVersionComparator(VersionComparator.EQUAL);
//			updater.setVersionComparator(VersionComparator.SEM_VER);
		updater.checkForUpdate(new UpdateCallback(){
			@Override
			public void updateAvailable(String newVersion,String downloadUrl,boolean hasDirectDownload){
				console.warn("An update is available! You're running " + plugin.getDescription().getVersion() + ", the latest version is " + newVersion + ".",downloadUrl,"You can disable update checking in the config.yml.");
			}

			@Override
			public void upToDate(){
				console.success("You're running the latest version. You can disable update checking in the config.yml.");
			}
		});
	}
}
