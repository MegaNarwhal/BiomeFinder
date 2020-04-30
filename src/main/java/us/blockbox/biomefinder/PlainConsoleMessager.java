package us.blockbox.biomefinder;

import us.blockbox.biomefinder.api.ConsoleMessager;

import java.util.logging.Logger;

public class PlainConsoleMessager implements ConsoleMessager{
	private final Logger logger;

	public PlainConsoleMessager(Logger logger){
		this.logger = logger;
	}

	@Override
	public Logger getLogger(){
		return logger;
	}

	@Override
	public void warn(String... msg){
		for(final String s : msg){
			logger.warning(s);
		}
	}

	@Override
	public void info(String... msg){
		for(final String s : msg){
			logger.info(s);
		}
	}

	@Override
	public void success(String... msg){
		for(final String s : msg){
			logger.info(s);
		}
	}
}
