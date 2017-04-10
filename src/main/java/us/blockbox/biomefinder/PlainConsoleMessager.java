package us.blockbox.biomefinder;

import java.util.logging.Logger;

import static us.blockbox.biomefinder.BiomeFinder.plugin;

public class PlainConsoleMessager implements ConsoleMessager{

	private static final Logger log = plugin.getLogger();


	@Override
	public void warn(String... msg){
		for(final String s : msg){
			log.warning(s);
		}
	}

	@Override
	public void info(String... msg){
		for(final String s : msg){
			log.info(s);
		}
	}

	@Override
	public void success(String... msg){
		for(final String s : msg){
			log.info(s);
		}
	}
}
